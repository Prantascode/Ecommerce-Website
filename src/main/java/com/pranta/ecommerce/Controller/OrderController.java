package com.pranta.ecommerce.Controller;

import java.net.http.HttpClient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.pranta.ecommerce.Dto.OrderRequestDto;
import com.pranta.ecommerce.Dto.OrderResponseDto;
import com.pranta.ecommerce.Service.OrderService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/create")
    public ResponseEntity<OrderResponseDto> createOrder(@Valid @RequestBody OrderRequestDto dto){
        return new ResponseEntity<>(orderService.createOrder(dto),HttpStatus.CREATED
    );
    }
}
