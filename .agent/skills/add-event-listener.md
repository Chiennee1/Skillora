# Skill: Add Event Listener

> Add Spring event-driven communication between modules.

## Template

### Event Class
```java
package com.example.skillora_platform.{module}.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class {Event}Event extends ApplicationEvent {
    private final Long entityId;

    public {Event}Event(Object source, Long entityId) {
        super(source);
        this.entityId = entityId;
    }
}
```

### Publishing Event
```java
@Service
@RequiredArgsConstructor
public class EnrollmentService {
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void enroll(...) {
        // ... enrollment logic
        eventPublisher.publishEvent(new CourseEnrolledEvent(this, enrollment.getId()));
    }
}
```

### Listening to Event
```java
package com.example.skillora_platform.notification.listener;

@Component
@RequiredArgsConstructor
public class EnrollmentNotificationListener {
    private final NotificationService notificationService;

    @EventListener
    @Async
    public void onCourseEnrolled(CourseEnrolledEvent event) {
        notificationService.sendEnrollmentConfirmation(event.getEntityId());
    }
}
```
