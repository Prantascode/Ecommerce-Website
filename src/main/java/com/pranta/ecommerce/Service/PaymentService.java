package com.pranta.ecommerce.Service;

import com.pranta.ecommerce.Dto.Response.PaymentResponseDto;

public interface PaymentService {
    
    PaymentResponseDto initiatePayment(Long orderId, String email);
    
    boolean validatePayment(String valId);
}