package com.pranta.ecommerce.Service;

import com.pranta.ecommerce.Dto.PaymentResponseDto;

public interface PaymentService {
    
    PaymentResponseDto initiatePayment(Long orderId, String email);
    
    boolean validatePayment(String valId);
}