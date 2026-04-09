package com.pranta.ecommerce.Controller;


import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pranta.ecommerce.Dto.OrderResponseDto;
import com.pranta.ecommerce.Dto.OrderUpdateRequestDto;
import com.pranta.ecommerce.Entity.Order.OrderStatus;
import com.pranta.ecommerce.Service.OrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

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

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<OrderResponseDto>> getAllOrders(){
        List<OrderResponseDto> response = orderService.getAllOrder();
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
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderResponseDto> updateOrderStatus(@PathVariable Long orderId,@Valid @RequestBody OrderUpdateRequestDto request){
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId,request.getStatus()));
    }

    @PreAuthorize("hasRole('USER')")
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponseDto> cancelOrder(@PathVariable Long orderId,Authentication authentication){
        String email = authentication.getName();
        return ResponseEntity.ok(orderService.cancelOrder(orderId, email));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("filter/{status}")
    public ResponseEntity<List<OrderResponseDto>> filterByStatus(@PathVariable OrderStatus status){
        return ResponseEntity.ok(orderService.filterByStatus(status));
    }
}
