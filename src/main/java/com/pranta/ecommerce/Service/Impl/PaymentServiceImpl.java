package com.pranta.ecommerce.Service.Impl;

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
import com.pranta.ecommerce.Exceptions.InvalidRequestException;
import com.pranta.ecommerce.Exceptions.ResourceNotFoundException;
import com.pranta.ecommerce.Exceptions.UnauthorizedAccessException;
import com.pranta.ecommerce.Repository.OrderRepository;
import com.pranta.ecommerce.Repository.PaymentRepository;
import com.pranta.ecommerce.Service.PaymentService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    
    private final RestTemplate restTemplate;
    private final SSLCommerzConfig sslCommerzConfig;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public PaymentResponseDto initiatePayment(Long orderId, String email) {
        log.info("Initiating payment for order ID: {} by user: {}", orderId, email);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        // Validate customer address
        if (order.getCustomer().getAddress() == null || order.getCustomer().getAddress().isEmpty() ||
            order.getCustomer().getCity() == null || order.getCustomer().getCity().isEmpty() ||
            order.getCustomer().getCountry() == null || order.getCustomer().getCountry().isEmpty()) {
            throw new InvalidRequestException("Please add complete address information before placing an order");
        }

        // Validate order ownership
        if (!order.getCustomer().getUser().getEmail().equals(email)) {
            throw new UnauthorizedAccessException("Unauthorized: This order does not belong to you");
        }

        // Validate order status
        if (!order.getStatus().equals(Order.OrderStatus.PENDING)) {
            throw new InvalidRequestException("Payment can only be initiated for pending orders. Current status: " + order.getStatus());
        }

        // Check for existing completed payment
        paymentRepository.findByOrderId(orderId).ifPresent(p -> {
            if (p.getStatus().equals(PaymentStatus.COMPLETED)) {
                throw new InvalidRequestException("Payment has already been completed for this order");
            }
        });

        // Generate unique transaction ID
        String transactionId = generateTransactionId(orderId);
        log.debug("Generated transaction ID: {}", transactionId);

        // Create payment record
        Payment payment = createPaymentRecord(order, transactionId);

        // Build SSLCommerz request parameters
        MultiValueMap<String, String> params = buildSSLCommerzRequestParams(order, transactionId);

        // Send request to SSLCommerz
        try {
            ResponseEntity<Map> response = sendSSLCommerzRequest(params);
            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null && "SUCCESS".equals(responseBody.get("status"))) {
                String sessionKey = (String) responseBody.get("sessionkey");
                String gatewayUrl = (String) responseBody.get("GatewayPageURL");

                // Update payment with session key
                payment.setSessionKey(sessionKey);
                paymentRepository.save(payment);

                log.info("SSLCommerz payment initiated successfully for order: {}", orderId);
                return new PaymentResponseDto(
                    "SUCCESS", 
                    gatewayUrl, 
                    sessionKey,
                    "Open the gatewayUrl in a browser to complete payment"
                );
            }

            log.error("SSLCommerz initiation failed for order {}: {}", orderId, responseBody);
            String errorMessage = extractErrorMessage(responseBody);
            return new PaymentResponseDto("FAILED", null, null, "Payment initiation failed: " + errorMessage);

        } catch (Exception e) {
            log.error("SSLCommerz error for order {}: {}", orderId, e.getMessage(), e);
            return new PaymentResponseDto("FAILED", null, null, "Payment initiation error: " + e.getMessage());
        }
    }

    @Override
    public boolean validatePayment(String valId) {
        log.info("Validating payment with validation ID: {}", valId);
        
        if (valId == null || valId.trim().isEmpty()) {
            log.warn("Validation ID is null or empty");
            return false;
        }

        String url = buildValidationUrl(valId);

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> body = response.getBody();

            if (body != null && "VALID".equals(body.get("status"))) {
                log.info("Payment validated successfully for validation ID: {}", valId);
                return true;
            }

            log.error("SSLCommerz validation failed for {}: {}", valId, body);
            return false;

        } catch (Exception e) {
            log.error("SSLCommerz validation error for {}: {}", valId, e.getMessage(), e);
            return false;
        }
    }


    private String generateTransactionId(Long orderId) {
        return "TXN" + orderId + "-" + System.currentTimeMillis();
    }

    private Payment createPaymentRecord(Order order, String transactionId) {
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setTransactionId(transactionId);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());
        return paymentRepository.save(payment);
    }

    private MultiValueMap<String, String> buildSSLCommerzRequestParams(Order order, String transactionId) {
        var customer = order.getCustomer();
        var user = customer.getUser();

        // Get backend base URL from config or use default
        String backendBase = sslCommerzConfig.getBaseUrl() != null 
                ? sslCommerzConfig.getBaseUrl() 
                : "https://your-ngrok-url.ngrok-free.app/api/payment";

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        
        // Store credentials
        params.add("store_id", sslCommerzConfig.getStoreId());
        params.add("store_passwd", sslCommerzConfig.getStorePassword());
        
        // Order details
        params.add("total_amount", String.valueOf(order.getTotalAmount()));
        params.add("currency", "BDT");
        params.add("tran_id", transactionId);
        
        // URLs
        params.add("success_url", backendBase + "/success");
        params.add("fail_url", backendBase + "/fail");
        params.add("cancel_url", backendBase + "/cancel");
        
        // Customer details
        params.add("cus_name", user.getName());
        params.add("cus_email", user.getEmail());
        params.add("cus_Phone", customer.getPhone() != null ? customer.getPhone() : "N/A");
        params.add("cus_add1", customer.getAddress());
        params.add("cus_city", customer.getCity());
        params.add("cus_country", customer.getCountry());
        
        // Additional information
        params.add("shipping_method", "NO");
        params.add("prod_name", "Order #" + order.getId());
        params.add("prod_category", "General");
        params.add("prod_profile", "general");
        
        return params;
    }

    private HttpEntity<MultiValueMap<String, String>> createHttpEntity(MultiValueMap<String, String> params) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return new HttpEntity<>(params, headers);
    }

    private ResponseEntity<Map> sendSSLCommerzRequest(MultiValueMap<String, String> params) {
        String url = sslCommerzConfig.getBaseUrl() + sslCommerzConfig.getInitUrl();
        HttpEntity<MultiValueMap<String, String>> entity = createHttpEntity(params);
        return restTemplate.postForEntity(url, entity, Map.class);
    }

    private String buildValidationUrl(String valId) {
        return sslCommerzConfig.getBaseUrl() + sslCommerzConfig.getValidationUrl()
                + "?val_id=" + valId
                + "&store_id=" + sslCommerzConfig.getStoreId()
                + "&store_passwd=" + sslCommerzConfig.getStorePassword()
                + "&v=1&format=json";
    }

    @SuppressWarnings("unchecked")
    private String extractErrorMessage(Map<String, Object> responseBody) {
        if (responseBody == null) {
            return "No response from payment gateway";
        }
        
        String errorMessage = (String) responseBody.get("failed_reason");
        if (errorMessage == null || errorMessage.isEmpty()) {
            errorMessage = (String) responseBody.get("error");
        }
        if (errorMessage == null || errorMessage.isEmpty()) {
            errorMessage = "Unknown error occurred";
        }
        return errorMessage;
    }
}