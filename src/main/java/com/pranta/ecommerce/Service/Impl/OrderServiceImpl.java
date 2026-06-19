package com.pranta.ecommerce.Service.Impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.pranta.ecommerce.Dto.OrderItemResponseDto;
import com.pranta.ecommerce.Dto.OrderResponseDto;
import com.pranta.ecommerce.Entity.Cart;
import com.pranta.ecommerce.Entity.Customer;
import com.pranta.ecommerce.Entity.Order;
import com.pranta.ecommerce.Entity.OrderItem;
import com.pranta.ecommerce.Entity.Product;
import com.pranta.ecommerce.Entity.User;
import com.pranta.ecommerce.Entity.Order.OrderStatus;
import com.pranta.ecommerce.Exceptions.InvalidRequestException;
import com.pranta.ecommerce.Exceptions.ResourceNotFoundException;
import com.pranta.ecommerce.Exceptions.UnauthorizedAccessException;
import com.pranta.ecommerce.Repository.CartRepository;
import com.pranta.ecommerce.Repository.CustomerRepository;
import com.pranta.ecommerce.Repository.OrderRepository;
import com.pranta.ecommerce.Repository.ProductRepository;
import com.pranta.ecommerce.Repository.UserRepository;
import com.pranta.ecommerce.Service.CartService;
import com.pranta.ecommerce.Service.OrderItemService;
import com.pranta.ecommerce.Service.OrderService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderServiceImpl implements OrderService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final OrderItemService orderItemService;
    private final CartService cartService;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public OrderResponseDto createOrder(String email) {
        log.info("Creating order for user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        Customer customer = customerRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found for user: " + email));
        
        if (customer.getAddress() == null || customer.getAddress().isEmpty()) {
            throw new InvalidRequestException("Please add an address before placing an order");
        }
        
        Cart cart = cartRepository.findByCustomerId(customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for customer ID: " + customer.getId()));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new InvalidRequestException("Cannot create order with empty cart");
        }
        
        cartService.validateCartForOrder(cart);

        Order order = new Order();
        order.setCustomer(cart.getCustomer());
        order.setTotalAmount(cart.getGrandTotal());
        order.setStatus(OrderStatus.PENDING);
        order.setOrderDate(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);
        log.info("Order created with ID: {}", savedOrder.getId());

        for (var cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();

            if (product.getStock() < cartItem.getQuantity()) {
                throw new InvalidRequestException("Not enough stock for product: " + product.getName() 
                        + ". Available: " + product.getStock() + ", Requested: " + cartItem.getQuantity());
            }

            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);
        }
        
        List<OrderItemResponseDto> orderItemDtos = orderItemService
                .convertCartItemToOrderItem(cart.getItems(), savedOrder);

        cart.getItems().clear();
        cart.setGrandTotal(BigDecimal.ZERO);
        cartRepository.save(cart);

        log.info("Order created successfully with ID: {} for user: {}", savedOrder.getId(), email);
        return mapToResponse(savedOrder, orderItemDtos);
    }

    @Override
    @Transactional
    public List<OrderResponseDto> getMyOrder(String email) {
        log.debug("Fetching orders for user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        Customer customer = customerRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found for user: " + email));

        List<Order> orders = orderRepository.findAllByCustomerOrderByOrderDateDesc(customer);
        
        if (orders.isEmpty()) {
            log.warn("No orders found for user: {}", email);
            return Collections.emptyList();
        }
        
        return orders.stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    @Transactional
    public List<OrderResponseDto> getAllOrder() {
        log.debug("Fetching all orders");

        List<Order> orders = orderRepository.findAll();

        if (orders.isEmpty()) {
            log.warn("No orders found in the system");
            return Collections.emptyList();
        }
        
        return orders.stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    @Transactional
    public OrderResponseDto getMyOrderById(Long orderId, String email) {
        log.debug("Fetching order {} for user: {}", orderId, email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        
        Customer customer = customerRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found for user: " + email));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        if (!order.getCustomer().getId().equals(customer.getId())) {
            throw new UnauthorizedAccessException("You are not authorized to access this order");
        }

        return convertToDto(order);
    }
    
    @Override
    @Transactional
    public OrderResponseDto updateOrderStatus(Long orderId, OrderStatus newStatus) {
        log.info("Updating order {} status to: {}", orderId, newStatus);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
        
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new InvalidRequestException("Cannot update cancelled order");
        }

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new InvalidRequestException("Cannot update delivered order");
        }

        if (newStatus == OrderStatus.CANCELLED && order.getStatus() != OrderStatus.CANCELLED) {
            restoreStock(order);
        }
        
        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);
        
        log.info("Order {} status updated to: {}", orderId, newStatus);
        return convertToDto(updatedOrder);
    }

    @Override
    @Transactional
    public OrderResponseDto cancelOrder(Long orderId, String email) {
        log.info("Cancelling order {} for user: {}", orderId, email);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
        
        if (!order.getCustomer().getUser().getEmail().equals(email)) {
            throw new UnauthorizedAccessException("You are not authorized to cancel this order");
        }

        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new InvalidRequestException("Order cannot be cancelled in " + order.getStatus() + " status");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new InvalidRequestException("Order is already cancelled");
        }

        restoreStock(order);

        order.setStatus(OrderStatus.CANCELLED);
        Order savedOrder = orderRepository.save(order);
        
        log.info("Order {} cancelled successfully by user: {}", orderId, email);
        return convertToDto(savedOrder);
    }

    @Override
    @Transactional
    public List<OrderResponseDto> filterByStatus(OrderStatus status) {
        log.debug("Fetching orders with status: {}", status);
        
        List<Order> orders = orderRepository.findByStatus(status);

        if (orders.isEmpty()) {
            log.warn("No orders found with status: {}", status);
            return Collections.emptyList();
        }
        
        return orders.stream()
                .map(this::convertToDto)
                .toList();
    }

    private void restoreStock(Order order) {
        log.debug("Restoring stock for order: {}", order.getId());
        
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        }
        
        log.debug("Stock restored successfully for order: {}", order.getId());
    }

    private OrderResponseDto convertToDto(Order order) {
        List<OrderItemResponseDto> items = mapToOrderItem(order.getItems());
        return mapToResponse(order, items);
    }

    private OrderResponseDto mapToResponse(Order order, List<OrderItemResponseDto> orderItems) {
        return new OrderResponseDto(
            order.getId(),
            order.getCustomer().getId(),
            order.getTotalAmount(),
            order.getStatus(),
            order.getOrderDate(),
            orderItems
        );
    }

    private List<OrderItemResponseDto> mapToOrderItem(List<OrderItem> items) {
        return items.stream()
                .map(item -> new OrderItemResponseDto(
                    item.getId(),
                    item.getProduct().getId(),
                    item.getProduct().getName(),
                    item.getQuantity(),
                    item.getPrice(),
                    item.getOrder().getId()
                ))
                .toList();
    }
}