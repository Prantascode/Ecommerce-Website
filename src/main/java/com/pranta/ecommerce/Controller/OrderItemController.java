package com.pranta.ecommerce.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pranta.ecommerce.Dto.OderItemRequestDto;
import com.pranta.ecommerce.Dto.OrderItemResponseDto;
import com.pranta.ecommerce.Service.OderItemService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/oder_items")
public class OrderItemController {
    
    @Autowired
    private OderItemService orderItemService;

    @PostMapping
    public ResponseEntity<OrderItemResponseDto> createOrderItem(
            @Valid @RequestBody OderItemRequestDto dto) {

        return new ResponseEntity<>(
                orderItemService.createOrderItem(dto),
                HttpStatus.CREATED
        );
    }
}
