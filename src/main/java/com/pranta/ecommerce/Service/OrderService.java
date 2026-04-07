package com.pranta.ecommerce.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
import com.pranta.ecommerce.Repository.CartRepository;
import com.pranta.ecommerce.Repository.OrderRepository;
import com.pranta.ecommerce.Repository.ProductRepository;
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
    private final ProductRepository productRepository;

    @Transactional
    public OrderResponseDto createOrder(String email){

        User user = userRepository.findByEmail(email)
                    .orElseThrow(()-> new RuntimeException("User not found"));
        
        Cart cart = cartRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new RuntimeException("Cart not found"));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }
        
        Order order = new Order();
        order.setUser(cart.getUser());
        order.setTotalAmount(cart.getGrandTotal());
        order.setStatus(order.getStatus());
        order.setOrderDate(LocalDateTime.now());
        Order savedOrder = orderRepository.save(order);

        for (var cartItem : cart.getItems()) {
            
            Product product = cartItem.getProduct();

            if (product.getStock() < cartItem.getQuantity()) {
                throw new RuntimeException("Not enough stock for product : " +product.getName());
            }

            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);
        }
        List<OrderItemResponseDto> orderItemDtos = orderItemService
            .convertCartItemToOrderItem(cart.getItems(), savedOrder);

        cart.getItems().clear();
        cartRepository.save(cart);

        return mapToResponse(savedOrder, orderItemDtos);
    }

    public List<OrderResponseDto> getMyOrder(String email){

        User user = userRepository.findByEmail(email)
                    .orElseThrow(()-> new RuntimeException("User not found"));

        List<Order> orders =  orderRepository.findAllByUserIdOrderByOrderDateDesc(user.getId());
                return orders.stream()
            .map(order -> {
                List<OrderItemResponseDto> items = mapToOrderItem(order.getItems());
                return mapToResponse(order, items);
            })
            .toList();              
    }

    public List<OrderResponseDto> getAllOrder(){
        List<Order> orders = orderRepository.findAll();

        List<OrderResponseDto> responseList = new ArrayList<>();

         for (Order order : orders) {
            List<OrderItemResponseDto> items = mapToOrderItem(order.getItems());

            OrderResponseDto response = mapToResponse(order, items);

            responseList.add(response);
        }

        return responseList;
    }

     public OrderResponseDto getMyOrderById(Long orderId, String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You are not allowed to access this order");
        }

        List<OrderItemResponseDto> items = mapToOrderItem(order.getItems());
        return mapToResponse(order, items);
    }
    
    //update order
    @Transactional
    public OrderResponseDto updateOrderStatus(Long orderId, OrderStatus newStatus){
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Cancelled order cannot be updated");
        }

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new RuntimeException("Delivered order cannot be updated");
        }
        
        order.setStatus(newStatus);
        Order updateOrder = orderRepository.save(order);

        return mapToResponse(updateOrder, mapToOrderItem(updateOrder.getItems()));
    }

    @Transactional
public OrderResponseDto cancelOrder(Long orderId, String email) {
    Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));

    if (!order.getUser().getEmail().equals(email)) {
        throw new RuntimeException("You are not allowed to cancel this order");
    }

    if (order.getStatus().equals(OrderStatus.SHIPPED) || order.getStatus().equals(OrderStatus.DELIVERED)) {
        throw new RuntimeException("Order cannot be cancelled now");
    }

    if (order.getStatus().equals(OrderStatus.CANCELLED)) {
        throw new RuntimeException("Order is already cancelled");
    }

    for (OrderItem item : order.getItems()) {
        Product product = item.getProduct();
        product.setStock(product.getStock() + item.getQuantity());
    }

    order.setStatus(OrderStatus.CANCELLED);

    Order savedOrder = orderRepository.save(order);

    List<OrderItemResponseDto> items = mapToOrderItem(savedOrder.getItems());
    return mapToResponse(savedOrder, items);
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
