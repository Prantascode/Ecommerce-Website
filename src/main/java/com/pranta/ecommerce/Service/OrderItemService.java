package com.pranta.ecommerce.Service;

import java.util.List;

import com.pranta.ecommerce.Dto.Response.OrderItemResponseDto;
import com.pranta.ecommerce.Entity.CartItem;
import com.pranta.ecommerce.Entity.Order;

public interface OrderItemService {
    
    List<OrderItemResponseDto> convertCartItemToOrderItem(List<CartItem> cartItems, Order order);
}