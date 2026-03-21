package com.pranta.ecommerce.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pranta.ecommerce.Dto.OderItemRequestDto;
import com.pranta.ecommerce.Dto.OrderItemResponseDto;
import com.pranta.ecommerce.Entity.Order;
import com.pranta.ecommerce.Service.OderItemService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/oder_items")
public class OrderItemController {
    
    @Autowired
    private OderItemService orderItemService;

    @PostMapping("/add")
    public ResponseEntity<OrderItemResponseDto> createOrderItem(
            @Valid @RequestBody OderItemRequestDto dto) {

        return new ResponseEntity<>(
                orderItemService.addOrderItem(dto),
                HttpStatus.CREATED
        );
    }
    @GetMapping
    public ResponseEntity<List<OrderItemResponseDto>> getItemsByOrder(@PathVariable Long id, Order order){
        return ResponseEntity.ok(orderItemService.getItemsByOrder(id,order));
    }
}
