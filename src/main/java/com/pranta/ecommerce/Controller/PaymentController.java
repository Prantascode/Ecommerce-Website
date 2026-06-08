package com.pranta.ecommerce.Controller;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pranta.ecommerce.Dto.PaymentResponseDto;
import com.pranta.ecommerce.Entity.Order;
import com.pranta.ecommerce.Entity.Payment;
import com.pranta.ecommerce.Exceptions.ResourceNotFoundException;
import com.pranta.ecommerce.Repository.OrderRepository;
import com.pranta.ecommerce.Repository.PaymentRepository;
import com.pranta.ecommerce.Service.PaymentService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
@Slf4j
@Tag(
    name = "Payment",
    description = "Payment Management API's"
)
public class PaymentController {
    
    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/initiate/{orderId}")
    public ResponseEntity<PaymentResponseDto> initiatePayment(@PathVariable Long orderId, Authentication authentication) {
        String email = authentication.getName();
        PaymentResponseDto response = paymentService.initiatePayment(orderId, email);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/success")
    @Transactional
    public ResponseEntity<Map<String, Object>> paymentSuccess(
            @RequestParam Map<String, String> params) {

        String tranId = params.get("tran_id");
        String valId  = params.get("val_id");

        log.info("Payment success callback received for transaction: {}", tranId);

        Payment payment = paymentRepository.findByTransactionId(tranId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        boolean isValid = paymentService.validatePayment(valId);

        if (isValid) {
            payment.setValId(valId);
            payment.setStatus(Payment.PaymentStatus.COMPLETED);
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            Order order = payment.getOrder();
            order.setStatus(Order.OrderStatus.CONFIRMED);
            orderRepository.save(order);

            log.info("Payment validated. Order #{} marked as CONFIRMED", order.getId());

            return ResponseEntity.ok(Map.of(
                "status",        "SUCCESS",
                "message",       "Payment completed and order confirmed",
                "orderId",       order.getId(),
                "transactionId", tranId
            ));
        }

        payment.setStatus(Payment.PaymentStatus.FAILED);
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        log.warn("Payment validation failed for transaction: {}", tranId);

        return ResponseEntity.badRequest().body(Map.of(
            "status",        "FAILED",
            "message",       "Payment validation failed",
            "transactionId", tranId
        ));
    }

    // ----------------------------------------------------------------
    // Fail — SSLCommerz POSTs here on payment failure
    // ----------------------------------------------------------------
    @PostMapping("/fail")
    @Transactional
    public ResponseEntity<Map<String, Object>> paymentFail(
            @RequestParam Map<String, String> params) {

        String tranId = params.get("tran_id");

        log.warn("Payment failed for transaction: {}", tranId);

        paymentRepository.findByTransactionId(tranId).ifPresent(p -> {
            p.setStatus(Payment.PaymentStatus.FAILED);
            p.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(p);
        });

        return ResponseEntity.badRequest().body(Map.of(
            "status",        "FAILED",
            "message",       "Payment was not successful",
            "transactionId", tranId
        ));
    }

    // ----------------------------------------------------------------
    // Cancel — SSLCommerz POSTs here if user cancels
    // ----------------------------------------------------------------
    @PostMapping("/cancel")
    @Transactional
    public ResponseEntity<Map<String, Object>> paymentCancel(
            @RequestParam Map<String, String> params) {

        String tranId = params.get("tran_id");

        log.info("Payment cancelled for transaction: {}", tranId);

        paymentRepository.findByTransactionId(tranId).ifPresent(p -> {
            p.setStatus(Payment.PaymentStatus.CANCELED);
            p.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(p);
        });

        return ResponseEntity.ok(Map.of(
            "status",        "CANCELLED",
            "message",       "Payment was cancelled by the user",
            "transactionId", tranId
        ));
    }
}
