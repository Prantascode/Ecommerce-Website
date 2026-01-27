package com.pranta.ecommerce.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pranta.ecommerce.Dto.OderItemRequestDto;
import com.pranta.ecommerce.Dto.OrderItemResponseDto;
import com.pranta.ecommerce.Entity.Order;
import com.pranta.ecommerce.Entity.OrderItem;
import com.pranta.ecommerce.Entity.Product;
import com.pranta.ecommerce.Repository.OderItemRepository;
import com.pranta.ecommerce.Repository.OrderRepository;
import com.pranta.ecommerce.Repository.ProductRepository;

@Service
public class OderItemService {

    @Autowired
    private OderItemRepository orderItemRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ProductRepository productRepository;

    public OrderItemResponseDto createOrderItem(OderItemRequestDto dto) {

        Order order = orderRepository.findById(dto.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(dto.getQuantity());
        orderItem.setPrice(product.getPrice()); 
        OrderItem savedItem = orderItemRepository.save(orderItem);

        return mapToResponse(savedItem);
    }

   
    public List<OrderItemResponseDto> getItemsByOrder(Order order) {
        return orderItemRepository.findByOrder(order)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private OrderItemResponseDto mapToResponse(OrderItem item) {
        return new OrderItemResponseDto(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getPrice()
        );
    }
}
