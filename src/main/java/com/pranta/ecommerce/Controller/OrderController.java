package com.pranta.ecommerce.Controller;


import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.pranta.ecommerce.Dto.OrderRequestDto;
import com.pranta.ecommerce.Dto.OrderResponseDto;
import com.pranta.ecommerce.Service.OrderService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/create")
    public ResponseEntity<OrderResponseDto> createOrder(Authentication authentication){

        String email = authentication.getName();
        OrderResponseDto response = orderService.createOrder(email);
        return new ResponseEntity<>(response,HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping("/myOrder")
    public ResponseEntity<List<OrderResponseDto>> myOrder(Authentication authentication){
        String email = authentication.getName();
        List<OrderResponseDto> response = orderService.getMyOrder(email);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDto> getMyOrderById(
            @PathVariable Long orderId,
            Authentication authentication) {

        String email = authentication.getName();
        return ResponseEntity.ok(orderService.getMyOrderById(orderId, email));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update/{orderId}")
    public ResponseEntity<OrderResponseDto> updateOrderStatus(@PathVariable Long orderId,@RequestBody Map<String, String> request){
        String newStatus = request.get("status");
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, newStatus));
    }
}
