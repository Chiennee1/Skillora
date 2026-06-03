package com.example.skillora_platform.notification.listener;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.skillora_platform.commerce.entity.Order;
import com.example.skillora_platform.commerce.repository.OrderRepository;
import com.example.skillora_platform.notification.entity.NotificationType;
import com.example.skillora_platform.notification.event.OrderCancelledEvent;
import com.example.skillora_platform.notification.event.OrderCreatedEvent;
import com.example.skillora_platform.notification.event.OrderPaidEvent;
import com.example.skillora_platform.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CommerceNotificationListener {

    private final OrderRepository orderRepository;
    private final NotificationService notificationService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCreated(OrderCreatedEvent event) {
        Order order = orderRepository.findById(event.orderId()).orElse(null);
        if (order == null) {
            return;
        }
        notificationService.createNotification(
                order.getUser().getId(),
                NotificationType.ORDER_CREATED,
                "Đơn hàng đã được tạo",
                "Đơn hàng #" + order.getId() + " đã được tạo thành công.",
                Map.of(
                        "orderId", order.getId(),
                        "status", order.getStatus().name(),
                        "totalAmount", order.getTotalAmount()
                )
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCancelled(OrderCancelledEvent event) {
        Order order = orderRepository.findById(event.orderId()).orElse(null);
        if (order == null) {
            return;
        }
        notificationService.createNotification(
                order.getUser().getId(),
                NotificationType.ORDER_CANCELLED,
                "Đơn hàng đã bị hủy",
                "Đơn hàng #" + order.getId() + " đã được hủy.",
                Map.of("orderId", order.getId())
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderPaid(OrderPaidEvent event) {
        Order order = orderRepository.findById(event.orderId()).orElse(null);
        if (order == null) {
            return;
        }
        notificationService.createNotification(
                order.getUser().getId(),
                NotificationType.ORDER_PAID,
                "Thanh toán thành công",
                "Đơn hàng #" + order.getId() + " đã được thanh toán.",
                Map.of(
                        "orderId", order.getId(),
                        "totalAmount", order.getTotalAmount()
                )
        );
    }
}
