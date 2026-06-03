package com.example.skillora_platform.notification.listener;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.skillora_platform.commerce.entity.Order;
import com.example.skillora_platform.commerce.entity.PaymentTransaction;
import com.example.skillora_platform.commerce.repository.OrderRepository;
import com.example.skillora_platform.commerce.repository.PaymentTransactionRepository;
import com.example.skillora_platform.notification.entity.NotificationType;
import com.example.skillora_platform.notification.event.PaymentFailedEvent;
import com.example.skillora_platform.notification.event.PaymentPaidEvent;
import com.example.skillora_platform.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PaymentNotificationListener {

    private final OrderRepository orderRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final NotificationService notificationService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentFailed(PaymentFailedEvent event) {
        Order order = orderRepository.findById(event.orderId()).orElse(null);
        if (order == null) {
            return;
        }
        notificationService.createNotification(
                order.getUser().getId(),
                NotificationType.PAYMENT_FAILED,
                "Thanh toán thất bại",
                "Thanh toán cho đơn hàng #" + order.getId() + " chưa thành công.",
                paymentData(order, event.paymentTransactionId(), event.reason())
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentPaid(PaymentPaidEvent event) {
        Order order = orderRepository.findById(event.orderId()).orElse(null);
        if (order == null) {
            return;
        }
        notificationService.createNotification(
                order.getUser().getId(),
                NotificationType.PAYMENT_PAID,
                "Thanh toán thành công",
                "Thanh toán cho đơn hàng #" + order.getId() + " đã hoàn tất.",
                paymentData(order, event.paymentTransactionId(), null)
        );
    }

    private Map<String, Object> paymentData(Order order, Long paymentTransactionId, String reason) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("orderId", order.getId());
        if (paymentTransactionId != null) {
            data.put("paymentTransactionId", paymentTransactionId);
            paymentTransactionRepository.findById(paymentTransactionId)
                    .map(PaymentTransaction::getGateway)
                    .ifPresent(gateway -> data.put("gateway", gateway.name()));
        }
        if (reason != null && !reason.isBlank()) {
            data.put("reason", reason);
        }
        return data;
    }
}
