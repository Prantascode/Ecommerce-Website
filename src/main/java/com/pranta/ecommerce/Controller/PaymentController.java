
package com.pranta.ecommerce.Controller;

import java.io.IOException;
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

import com.pranta.ecommerce.Dto.Response.PaymentResponseDto;
import com.pranta.ecommerce.Entity.Order;
import com.pranta.ecommerce.Entity.Payment;
import com.pranta.ecommerce.Exceptions.ResourceNotFoundException;
import com.pranta.ecommerce.Repository.OrderRepository;
import com.pranta.ecommerce.Repository.PaymentRepository;
import com.pranta.ecommerce.Service.CartService;
import com.pranta.ecommerce.Service.PaymentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/payments")
@Slf4j
@Tag(
    name = "Payment",
    description = "Payment Management API's"
)
public class PaymentController {

    private final PaymentService paymentService;
    private final CartService cartService;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public PaymentController(
            PaymentService paymentService,
            CartService cartService,
            PaymentRepository paymentRepository,
            OrderRepository orderRepository) {
        this.paymentService = paymentService;
        this.cartService = cartService;
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    private static final String FRONTEND_URL = "http://localhost:5173";

    @Operation(
        summary = "Initiate Payment",
        description = "User can Initiate order payment"
    )
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/initiate/{orderId}")
    public ResponseEntity<PaymentResponseDto> initiatePayment(
            @PathVariable Long orderId,
            Authentication authentication) {

        String email = authentication.getName();

        PaymentResponseDto response =
                paymentService.initiatePayment(orderId, email);

        return ResponseEntity.ok(response);
    }

    // SUCCESS CALLBACK

    @Operation(
        summary = "Payment Success",
        description = "Validates payment and redirects to frontend"
    )
    @PostMapping("/success")
    @Transactional
    public void paymentSuccess(
            @RequestParam Map<String, String> params,
            HttpServletResponse response) throws IOException {

        String tranId = params.get("tran_id");
        String valId = params.get("val_id");

        log.info("Payment success callback received: {}", tranId);

        Payment payment = paymentRepository.findByTransactionId(tranId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Payment not found"));

        boolean isValid = paymentService.validatePayment(valId);

        if (isValid) {

            payment.setValId(valId);
            payment.setStatus(Payment.PaymentStatus.COMPLETED);
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            Order order = payment.getOrder();

            order.setStatus(Order.OrderStatus.CONFIRMED);
            orderRepository.save(order);

            cartService.clearCart(
                    order.getCustomer().getUser().getEmail()
            );

            log.info(
                "Payment validated. Order #{} marked as CONFIRMED",
                order.getId()
            );

            response.sendRedirect(
                FRONTEND_URL + "/?payment=success&orderId=" + order.getId()
            );

            return;
        }

        payment.setStatus(Payment.PaymentStatus.FAILED);
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        log.warn("Payment validation failed: {}", tranId);

        response.sendRedirect(
            FRONTEND_URL + "/?payment=failed"
        );
    }

    // FAILURE CALLBACK

    @Operation(
        summary = "Payment Failed",
        description = "Updates payment status and redirects to frontend"
    )
    @PostMapping("/fail")
    @Transactional
    public void paymentFail(
            @RequestParam Map<String, String> params,
            HttpServletResponse response) throws IOException {

        String tranId = params.get("tran_id");

        log.warn("Payment failed: {}", tranId);

        paymentRepository.findByTransactionId(tranId).ifPresent(p -> {
            p.setStatus(Payment.PaymentStatus.FAILED);
            p.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(p);
        });

        response.sendRedirect(
            FRONTEND_URL + "/?payment=failed"
        );
    }

    // CANCEL CALLBACK

    @Operation(
        summary = "Payment Cancel",
        description = "Updates payment status and redirects to frontend"
    )
    @PostMapping("/cancel")
    @Transactional
    public void paymentCancel(
            @RequestParam Map<String, String> params,
            HttpServletResponse response) throws IOException {

        String tranId = params.get("tran_id");

        log.info("Payment cancelled: {}", tranId);

        paymentRepository.findByTransactionId(tranId).ifPresent(p -> {
            p.setStatus(Payment.PaymentStatus.CANCELED);
            p.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(p);
        });

        response.sendRedirect(
            FRONTEND_URL + "/?payment=cancelled"
        );
    }
}
