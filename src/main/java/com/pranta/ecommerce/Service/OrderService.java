package com.pranta.ecommerce.Service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pranta.ecommerce.Dto.OrderItemResponseDto;
import com.pranta.ecommerce.Dto.OrderRequestDto;
import com.pranta.ecommerce.Dto.OrderResponseDto;
import com.pranta.ecommerce.Entity.Cart;
import com.pranta.ecommerce.Entity.Order;
import com.pranta.ecommerce.Entity.OrderItem;
import com.pranta.ecommerce.Repository.CartRepository;
import com.pranta.ecommerce.Repository.OrderRepository;

import jakarta.transaction.Transactional;

@Service
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private OrderItemService orderItemService;

    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto dto ){
        
        Cart cart = cartRepository.findByUserId(dto.getUserId())
                    .orElseThrow(() -> new RuntimeException("Cart not found"));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }
        
        Order order = new Order();
        order.setUser(cart.getUser());
        order.setTotalAmount(cart.getGrandTotal());
        order.setStatus("PENDING");
        order.setOrderDate(LocalDateTime.now());
        Order savedOrder = orderRepository.save(order);

        List<OrderItemResponseDto> orderItemDtos = orderItemService
            .convertCartItemToOrderItem(cart.getItems(), savedOrder);

        cart.getItems().clear();
        cartRepository.save(cart);

        return mapToResponse(savedOrder, orderItemDtos);
    }

    //Order histroy by user
    public List<OrderResponseDto> getOrderDetailsByUserId(Long userId){
        List<Order> orders =  orderRepository.findAllByUserIdOrderByOrderDateDesc(userId);
                return orders.stream()
            .map(order -> {
                List<OrderItemResponseDto> items = mapToOrderItem(order.getItems());
                return mapToResponse(order, items);
            })
            .toList();              
    }

    // get order by id
    public OrderResponseDto getOrderById(Long orderId){
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));

        List<OrderItemResponseDto> items = mapToOrderItem(order.getItems());

        return mapToResponse(order, items);
    }
    //update order
    public OrderResponseDto updateOrderStatus(Long orderId, String newStatus){
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        order.setStatus(newStatus);
        Order updateOrder = orderRepository.save(order);

        return mapToResponse(updateOrder, mapToOrderItem(updateOrder.getItems()));
    }


    private OrderResponseDto mapToResponse(Order order, List<OrderItemResponseDto> orderItems){
        return new OrderResponseDto(
            order.getId(),
            order.getUser().getId(),
            order.getTotalAmount(),
            order.getStatus(),
            order.getOrderDate(),
            orderItems

        );
    }

    private List<OrderItemResponseDto> mapToOrderItem(List<OrderItem> items){
        return items.stream()
                .map(item -> new OrderItemResponseDto(
                    item.getId(),
                    item.getProduct().getId(),
                    item.getProduct().getName(),
                    item.getQuantity(),
                    item.getPrice(),
                    item.getOrder().getId()
                )).toList();
    }
}
