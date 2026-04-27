package com.pranta.ecommerce.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.pranta.ecommerce.Dto.OrderItemResponseDto;
import com.pranta.ecommerce.Dto.OrderResponseDto;
import com.pranta.ecommerce.Entity.Cart;
import com.pranta.ecommerce.Entity.Order;
import com.pranta.ecommerce.Entity.OrderItem;
import com.pranta.ecommerce.Entity.Product;
import com.pranta.ecommerce.Entity.User;
import com.pranta.ecommerce.Entity.Order.OrderStatus;
import com.pranta.ecommerce.Exceptions.InvalidRequestException;
import com.pranta.ecommerce.Exceptions.ResourceNotFoundException;
import com.pranta.ecommerce.Exceptions.UnauthorizedAccessException;
import com.pranta.ecommerce.Repository.CartRepository;
import com.pranta.ecommerce.Repository.OrderRepository;
import com.pranta.ecommerce.Repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class OrderService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final OrderItemService orderItemService;

    @Transactional
    public OrderResponseDto createOrder(String email){

        User user = userRepository.findByEmail(email)
                    .orElseThrow(()-> new ResourceNotFoundException("User not found with this email"));
        
        Cart cart = cartRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cart not found with this Id"));

        if (cart.getItems().isEmpty()) {
            throw new InvalidRequestException("Cart is empty");
        }
        
        Order order = new Order();
        order.setUser(cart.getUser());
        order.setTotalAmount(cart.getGrandTotal());
        order.setStatus(OrderStatus.PENDING);
        order.setOrderDate(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);

        for (var cartItem : cart.getItems()) {
            
            Product product = cartItem.getProduct();

            if (product.getStock() < cartItem.getQuantity()) {
                throw new InvalidRequestException("Not enough stock for product : " +product.getName());
            }

            product.setStock(product.getStock() - cartItem.getQuantity());
            
        }
        List<OrderItemResponseDto> orderItemDtos = orderItemService
            .convertCartItemToOrderItem(cart.getItems(), savedOrder);

        cart.getItems().clear();
        cartRepository.save(cart);

        return mapToResponse(savedOrder, orderItemDtos);
    }

    public List<OrderResponseDto> getMyOrder(String email){

        User user = userRepository.findByEmail(email)
                    .orElseThrow(()-> new ResourceNotFoundException("User not found with this Id"));

        List<Order> orders =  orderRepository.findAllByUserIdOrderByOrderDateDesc(user.getId());
        return orders.stream()
            .map(this::convertToDto)
            .toList();              
    }

    public List<OrderResponseDto> getAllOrder(){

        List<Order> orders = orderRepository.findAll();

        if (orders.isEmpty()) {
            return Collections.emptyList();
        }
        return orders.stream()
                .map(this::convertToDto)
                .toList();
    }

     public OrderResponseDto getMyOrderById(Long orderId, String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with this Id"));

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with this Id"));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("You are not allowed to access this order");
        }

        return convertToDto(order);
    }
    
    //update order
    @Transactional
    public OrderResponseDto updateOrderStatus(Long orderId, OrderStatus newStatus){
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with this order Id"));
        
        if (newStatus.equals(OrderStatus.CANCELLED) && order.getStatus() != OrderStatus.CANCELLED) {
            restoreStock(order);
        }
        
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new InvalidRequestException("Cancelled order cannot be updated");
        }

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new InvalidRequestException("Delivered order cannot be updated");
        }
        
        order.setStatus(newStatus);
        Order updateOrder = orderRepository.save(order);

        return convertToDto(updateOrder);
    }

    @Transactional
    public OrderResponseDto cancelOrder(Long orderId, String email) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with this order Id"));

        if (!order.getUser().getEmail().equals(email)) {
            throw new UnauthorizedAccessException("You are not allowed to cancel this order");
        }

        if (order.getStatus().equals(OrderStatus.SHIPPED) || order.getStatus().equals(OrderStatus.DELIVERED)) {
            throw new InvalidRequestException("Order cannot be cancelled now");
        }

        if (order.getStatus().equals(OrderStatus.CANCELLED)) {
            throw new InvalidRequestException("Order is already cancelled");
        }

        restoreStock(order);

        order.setStatus(OrderStatus.CANCELLED);

        Order savedOrder = orderRepository.save(order);
        
        return convertToDto(savedOrder);
    }


    public List<OrderResponseDto> filterByStatus(OrderStatus status){
       List<Order> orders = orderRepository.findByStatus(status);

       if (orders.isEmpty()) {
            return Collections.emptyList();
       }
       
      return orders.stream()
            .map(this::convertToDto)
            .toList();
    }

    private void restoreStock(Order order) {
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
        }
    }

    private OrderResponseDto convertToDto(Order order) {
        List<OrderItemResponseDto> items = mapToOrderItem(order.getItems());
        return mapToResponse(order, items);
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
