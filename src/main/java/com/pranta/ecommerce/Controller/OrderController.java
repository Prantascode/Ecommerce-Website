package com.pranta.ecommerce.Controller;


import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.pranta.ecommerce.Dto.OrderRequestDto;
import com.pranta.ecommerce.Dto.OrderResponseDto;
import com.pranta.ecommerce.Entity.Order;
import com.pranta.ecommerce.Service.OrderService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/create")
    public ResponseEntity<OrderResponseDto> createOrder(@Valid @RequestBody OrderRequestDto dto){
        return new ResponseEntity<>(orderService.createOrder(dto),HttpStatus.CREATED
    );
    }

    @GetMapping("/userId/{userId}")
    public ResponseEntity<List<OrderResponseDto>> getOrderDetailsByUserId(@PathVariable Long userId){
        return ResponseEntity.ok(orderService.getOrderDetailsByUserId(userId));
    }

    @GetMapping("/orderId/{orderId}")
    public ResponseEntity<OrderResponseDto> getOrderById(@PathVariable Long orderId){
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }

    @PutMapping("/update/{orderId}")
    public ResponseEntity<OrderResponseDto> updateOrderStatus(@PathVariable Long orderId,@RequestBody Map<String, String> request){
        String newStatus = request.get("status");
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, newStatus));
    }
}
