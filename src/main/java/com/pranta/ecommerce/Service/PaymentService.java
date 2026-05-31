package com.pranta.ecommerce.Service;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.pranta.ecommerce.Config.SSLCommerzConfig;
import com.pranta.ecommerce.Dto.PaymentResponseDto;
import com.pranta.ecommerce.Entity.Order;
import com.pranta.ecommerce.Entity.Payment;
import com.pranta.ecommerce.Entity.Payment.PaymentStatus;
import com.pranta.ecommerce.Repository.OrderRepository;
import com.pranta.ecommerce.Repository.PaymentRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    private final RestTemplate restTemplate;
    private final SSLCommerzConfig sslCommerzConfig;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public PaymentResponseDto initiatePayment(Long orderId, String email) {

        
        Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found with this Id"));

        if (order.getCustomer().getAddress().isEmpty()&& order.getCustomer().getCity().isEmpty() && order.getCustomer().getCountry().isEmpty()) {
            throw new RuntimeException("Please add an address before placing an order");
        }

        if (!order.getCustomer().getUser().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized: This order does not belong to you");
        }

        if (!order.getStatus().equals(Order.OrderStatus.PENDING)) {
            throw new RuntimeException("Payment can only be initiated for pending orders");
        }

        paymentRepository.findByOrderId(orderId).ifPresent(p -> {
            if (p.getStatus().equals(PaymentStatus.COMPLETED)) {
                throw new RuntimeException("Payment has already been completed for this order");
            }
        });

        String transactionId = "TXN" + orderId + "-" + System.currentTimeMillis(); // Generate a unique transaction ID

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setTransactionId(transactionId);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        var customer = order.getCustomer();
        var user = customer.getUser();

        String backendBase = "https://your-ngrok-url.ngrok-free.app/api/payment";

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("store_id", sslCommerzConfig.getStoreId());
        params.add("store_passwd", sslCommerzConfig.getStorePassword());
        params.add("total_amount", String.valueOf(order.getTotalAmount()));
        params.add("currency", "BDT");

        params.add("tran_id", transactionId);
        params.add("success_url", backendBase + "/success");
        params.add("fail_url", backendBase + "/fail");
        params.add("cancel_url", backendBase + "/cancel");

        params.add("cus_name", user.getName());
        params.add("cus_email", user.getEmail());
        params.add("cus_Phone", customer.getPhone());
        params.add("cus_add1", customer.getAddress());
        params.add("cus_city",  customer.getCity());
        params.add("cus_country", customer.getCountry());

        params.add("shipping_method", "NO");
        params.add("prod_name", "Order #" + orderId);
        params.add("prod_category", "General");
        params.add("prod_profile", "general");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

        String url = sslCommerzConfig.getBaseUrl() + sslCommerzConfig.getInitUrl();

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            Map<String, Object> body = response.getBody();

            if (body != null && "SUCCESS".equals(body.get("status"))) {
                String sessionKey = (String) body.get("sessionkey");
                String gatewayUrl = (String) body.get("GatewayPageURL");

                payment.setSessionKey(sessionKey);
                paymentRepository.save(payment);

                return new PaymentResponseDto("SUCCESS", gatewayUrl, sessionKey,
                        "Open the gatewayUrl in a browser to complete payment");
            }

            log.error("SSLCommerz init failed: {}", body);
            return new PaymentResponseDto("FAILED", null, null, "Payment initiation failed");

        } catch (Exception e) {
            log.error("SSLCommerz error: {}", e.getMessage());
            return new PaymentResponseDto("FAILED", null, null, e.getMessage());
        }

    }

    public boolean validatePayment(String ValId){
        String url = sslCommerzConfig.getBaseUrl() + sslCommerzConfig.getValidationUrl() 
                        + "?val_id=" + ValId 
                        + "&store_id=" + sslCommerzConfig.getStoreId() 
                        + "&store_passwd=" + sslCommerzConfig.getStorePassword() 
                        + "&v=1&format=json";

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> body = response.getBody();

            if (body != null && "VALID".equals(body.get("status"))) {
                return true;
            }

            log.error("SSLCommerz validation failed: {}", body);
            return false;

        } catch (Exception e) {
            log.error("SSLCommerz validation error: {}", e.getMessage());
            return false;
        }
    }
}
