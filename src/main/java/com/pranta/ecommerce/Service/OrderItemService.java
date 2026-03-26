package com.pranta.ecommerce.Service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.pranta.ecommerce.Dto.OrderItemResponseDto;
import com.pranta.ecommerce.Entity.CartItem;
import com.pranta.ecommerce.Entity.Order;
import com.pranta.ecommerce.Entity.OrderItem;

import com.pranta.ecommerce.Repository.OderItemRepository;
@Service
public class OrderItemService {

    @Autowired
    private OderItemRepository orderItemRepository;

    public List<OrderItemResponseDto> convertCartItemToOrderItem(List<CartItem> cartItems, Order order) {
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cartItems) {

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setPrice(cartItem.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setOrder(order);

            orderItems.add(orderItem);

        }
        orderItemRepository.saveAll(orderItems);

        return orderItems.stream()
            .map(this::mapToResponse)
            .toList();
    }

    private OrderItemResponseDto mapToResponse(OrderItem item) {
        return new OrderItemResponseDto(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getPrice(),
                item.getOrder().getId()
        );
    }
}
