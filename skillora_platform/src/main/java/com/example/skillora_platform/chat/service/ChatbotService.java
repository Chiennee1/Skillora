package com.example.skillora_platform.chat.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.example.skillora_platform.chat.dto.ChatAskRequest;
import com.example.skillora_platform.chat.dto.ChatConversationResponse;
import com.example.skillora_platform.chat.dto.ChatMessageResponse;
import com.example.skillora_platform.chat.dto.ChatResponse;
import com.example.skillora_platform.chat.entity.ChatConversation;
import com.example.skillora_platform.chat.entity.ChatMessage;
import com.example.skillora_platform.chat.entity.ChatRole;
import com.example.skillora_platform.chat.repository.ChatConversationRepository;
import com.example.skillora_platform.chat.repository.ChatMessageRepository;
import com.example.skillora_platform.common.Constants;
import com.example.skillora_platform.common.PageResponse;
import com.example.skillora_platform.course.entity.Course;
import com.example.skillora_platform.course.entity.CourseStatus;
import com.example.skillora_platform.course.entity.Lesson;
import com.example.skillora_platform.course.repository.CourseRepository;
import com.example.skillora_platform.course.repository.LessonRepository;
import com.example.skillora_platform.course.service.CoursePermissionService;
import com.example.skillora_platform.enrollment.service.LearningAccessService;
import com.example.skillora_platform.exception.BusinessException;
import com.example.skillora_platform.exception.ResourceNotFoundException;
import com.example.skillora_platform.user.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotService {

    private static final int MAX_COURSE_CONTEXT_LESSONS = 12;

    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final CoursePermissionService permissionService;
    private final LearningAccessService learningAccessService;
    private final GeminiClient geminiClient;
    private final PlatformTransactionManager transactionManager;

    public ChatResponse ask(ChatAskRequest request, String actorEmail) {
        ChatRequestContext context = readOnlyTransaction(() -> buildChatRequestContext(request, actorEmail));
        GeminiReply reply = geminiClient.generate(context.systemInstruction(), context.messages());
        return writeTransaction(() -> persistChatResponse(context, reply));
    }

    @Transactional(readOnly = true)
    public PageResponse<ChatConversationResponse> listConversations(String actorEmail, int page, int size) {
        User actor = permissionService.requireActor(actorEmail);
        Pageable pageable = PageRequest.of(Math.max(page, 0), safeSize(size));
        Page<ChatConversationResponse> conversations = conversationRepository
                .findByUserIdOrderByUpdatedAtDesc(actor.getId(), pageable)
                .map(this::toConversationResponse);
        return PageResponse.from(conversations);
    }

    @Transactional(readOnly = true)
    public PageResponse<ChatMessageResponse> listMessages(
            Long conversationId,
            String actorEmail,
            int page,
            int size
    ) {
        User actor = permissionService.requireActor(actorEmail);
        findOwnedConversation(conversationId, actor.getId());
        Pageable pageable = PageRequest.of(Math.max(page, 0), safeSize(size));
        Page<ChatMessageResponse> messages = messageRepository
                .findByConversationIdOrderByCreatedAtAsc(conversationId, pageable)
                .map(this::toMessageResponse);
        return PageResponse.from(messages);
    }

    private ChatRequestContext buildChatRequestContext(ChatAskRequest request, String actorEmail) {
        User actor = permissionService.requireActor(actorEmail);
        String prompt = request.getMessage().trim();
        ChatConversation conversation = resolveConversation(request, actor);
        Course course = resolveCourse(request, conversation, actor);
        Long courseId = course == null ? null : course.getId();

        return new ChatRequestContext(
                actor.getId(),
                conversation.getId(),
                courseId,
                prompt,
                conversation.getId() == null ? buildTitle(prompt) : conversation.getTitle(),
                buildSystemInstruction(course, actor),
                buildGeminiMessages(conversation.getId(), prompt)
        );
    }

    private ChatResponse persistChatResponse(ChatRequestContext context, GeminiReply reply) {
        User actor = permissionService.requireActiveUser(context.actorId());
        Course course = context.courseId() == null ? null : requireAccessibleCourse(context.courseId(), actor);
        ChatConversation conversation = resolveConversationForWrite(context, actor, course);

        ChatMessage userMessage = messageRepository.save(ChatMessage.builder()
                .conversation(conversation)
                .role(ChatRole.USER)
                .content(context.prompt())
                .build());
        ChatMessage assistantMessage = messageRepository.save(ChatMessage.builder()
                .conversation(conversation)
                .role(ChatRole.ASSISTANT)
                .content(reply.text().trim())
                .model(reply.model())
                .tokensUsed(reply.tokensUsed())
                .build());

        log.debug("User {} sent chat message {} in conversation {}",
                actor.getId(), userMessage.getId(), conversation.getId());
        return ChatResponse.builder()
                .conversationId(conversation.getId())
                .conversationTitle(conversation.getTitle())
                .userMessage(toMessageResponse(userMessage))
                .assistantMessage(toMessageResponse(assistantMessage))
                .build();
    }

    private ChatConversation resolveConversationForWrite(ChatRequestContext context, User actor, Course course) {
        if (context.conversationId() == null) {
            ChatConversation conversation = ChatConversation.builder()
                    .user(actor)
                    .course(course)
                    .title(context.conversationTitle())
                    .build();
            return conversationRepository.save(conversation);
        }

        ChatConversation conversation = findOwnedConversation(context.conversationId(), actor.getId());
        Course existingCourse = conversation.getCourse();
        if (existingCourse != null) {
            requireAccessibleCourse(existingCourse.getId(), actor);
            if (course != null && !existingCourse.getId().equals(course.getId())) {
                throw new BusinessException("Conversation belongs to a different course", HttpStatus.BAD_REQUEST);
            }
        } else if (course != null) {
            conversation.setCourse(course);
        }
        conversation.setUpdatedAt(LocalDateTime.now());
        return conversationRepository.save(conversation);
    }

    private ChatConversation resolveConversation(ChatAskRequest request, User actor) {
        if (request.getConversationId() == null) {
            return ChatConversation.builder().user(actor).build();
        }
        return findOwnedConversation(request.getConversationId(), actor.getId());
    }

    private ChatConversation findOwnedConversation(Long conversationId, Long userId) {
        return conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Chat conversation not found with id: " + conversationId));
    }

    private Course resolveCourse(ChatAskRequest request, ChatConversation conversation, User actor) {
        Course existingCourse = conversation.getCourse();
        Long requestedCourseId = request.getCourseId();
        if (existingCourse != null) {
            if (requestedCourseId != null && !existingCourse.getId().equals(requestedCourseId)) {
                throw new BusinessException("Conversation belongs to a different course", HttpStatus.BAD_REQUEST);
            }
            return requireAccessibleCourse(existingCourse.getId(), actor);
        }
        if (requestedCourseId == null) {
            return null;
        }
        return requireAccessibleCourse(requestedCourseId, actor);
    }

    private Course requireAccessibleCourse(Long courseId, User actor) {
        Course course = courseRepository.findByIdAndDeletedAtIsNull(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
        if (course.getStatus() != CourseStatus.PUBLISHED && !permissionService.canManage(course, actor)) {
            throw new BusinessException("You do not have access to this course", HttpStatus.FORBIDDEN);
        }
        return course;
    }

    private List<GeminiMessage> buildGeminiMessages(Long conversationId, String prompt) {
        List<GeminiMessage> messages = new ArrayList<>();
        if (conversationId != null) {
            List<ChatMessage> history = new ArrayList<>(
                    messageRepository.findTop12ByConversationIdOrderByCreatedAtDesc(conversationId));
            Collections.reverse(history);
            history.stream()
                    .filter(message -> message.getRole() != ChatRole.SYSTEM)
                    .map(message -> new GeminiMessage(message.getRole(), message.getContent()))
                    .forEach(messages::add);
        }
        messages.add(new GeminiMessage(ChatRole.USER, prompt));
        return messages;
    }

    private String buildSystemInstruction(Course course, User actor) {
        StringBuilder instruction = new StringBuilder();
        instruction.append("""
                You are Skillora's learning assistant.
                Answer in the user's language, be concise, and focus on learning help.
                Do not claim access to private platform data unless it is included in this prompt.
                """);

        if (course == null) {
            return instruction.toString();
        }

        instruction.append("\nCourse context:\n")
                .append("Title: ").append(course.getTitle()).append('\n');
        appendIfPresent(instruction, "Subtitle", course.getSubtitle());
        appendIfPresent(instruction, "Description", course.getDescription());
        instruction.append("Level: ").append(course.getLevel()).append('\n')
                .append("Language: ").append(course.getLanguage()).append('\n');

        boolean canSeeProtectedContent = permissionService.canManage(course, actor)
                || learningAccessService.hasActiveEnrollment(actor.getId(), course.getId());
        if (canSeeProtectedContent) {
            appendProtectedLessonContext(instruction, course.getId());
        } else {
            instruction.append("The learner is not enrolled; do not reveal protected lesson content.\n");
        }
        return instruction.toString();
    }

    private void appendProtectedLessonContext(StringBuilder instruction, Long courseId) {
        List<Lesson> lessons = lessonRepository.findPublishedLessonsByCourseId(courseId);
        if (lessons.isEmpty()) {
            return;
        }
        instruction.append("Published lessons:\n");
        lessons.stream()
                .limit(MAX_COURSE_CONTEXT_LESSONS)
                .forEach(lesson -> instruction.append("- ")
                        .append(lesson.getTitle())
                        .append(" (")
                        .append(lesson.getType())
                        .append(")")
                        .append(trimLessonContent(lesson.getContent()))
                        .append('\n'));
    }

    private String trimLessonContent(String content) {
        if (content == null || content.isBlank()) {
            return "";
        }
        String normalized = content.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= 300) {
            return ": " + normalized;
        }
        return ": " + normalized.substring(0, 300) + "...";
    }

    private void appendIfPresent(StringBuilder builder, String label, String value) {
        if (value != null && !value.isBlank()) {
            builder.append(label).append(": ").append(value.trim()).append('\n');
        }
    }

    private String buildTitle(String prompt) {
        String normalized = prompt.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= 80) {
            return normalized;
        }
        return normalized.substring(0, 80);
    }

    private <T> T readOnlyTransaction(Supplier<T> supplier) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setReadOnly(true);
        return template.execute(status -> supplier.get());
    }

    private <T> T writeTransaction(Supplier<T> supplier) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        return template.execute(status -> supplier.get());
    }

    private int safeSize(int size) {
        return Math.min(Math.max(size, 1), Constants.MAX_PAGE_SIZE);
    }

    private ChatConversationResponse toConversationResponse(ChatConversation conversation) {
        Course course = conversation.getCourse();
        return ChatConversationResponse.builder()
                .id(conversation.getId())
                .courseId(course == null ? null : course.getId())
                .courseTitle(course == null ? null : course.getTitle())
                .title(conversation.getTitle())
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .build();
    }

    private ChatMessageResponse toMessageResponse(ChatMessage message) {
        return ChatMessageResponse.builder()
                .id(message.getId())
                .conversationId(message.getConversation().getId())
                .role(message.getRole())
                .content(message.getContent())
                .model(message.getModel())
                .tokensUsed(message.getTokensUsed())
                .createdAt(message.getCreatedAt())
                .build();
    }

    private record ChatRequestContext(
            Long actorId,
            Long conversationId,
            Long courseId,
            String prompt,
            String conversationTitle,
            String systemInstruction,
            List<GeminiMessage> messages
    ) {
    }
}
