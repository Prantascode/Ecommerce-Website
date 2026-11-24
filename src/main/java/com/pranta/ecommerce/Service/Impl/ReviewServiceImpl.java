package com.pranta.ecommerce.Service.Impl;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.pranta.ecommerce.Dto.Request.ReviewRequestDto;
import com.pranta.ecommerce.Dto.Request.ReviewUpdateRequestDto;
import com.pranta.ecommerce.Dto.Response.ReviewResponseDto;
import com.pranta.ecommerce.Entity.Customer;
import com.pranta.ecommerce.Entity.Order;
import com.pranta.ecommerce.Entity.OrderItem;
import com.pranta.ecommerce.Entity.Product;
import com.pranta.ecommerce.Entity.Review;
import com.pranta.ecommerce.Entity.User;
import com.pranta.ecommerce.Exceptions.DuplicateResourceException;
import com.pranta.ecommerce.Exceptions.ResourceNotFoundException;
import com.pranta.ecommerce.Exceptions.UnauthorizedAccessException;
import com.pranta.ecommerce.Repository.CustomerRepository;
import com.pranta.ecommerce.Repository.OrderItemRepository;
import com.pranta.ecommerce.Repository.OrderRepository;
import com.pranta.ecommerce.Repository.ProductRepository;
import com.pranta.ecommerce.Repository.ReviewRepository;
import com.pranta.ecommerce.Repository.UserRepository;
import com.pranta.ecommerce.Service.ReviewService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {
    
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final OrderItemRepository orderItemRepository;  

    @Override
    @Transactional
    public ReviewResponseDto reviewOrderedProduct(String email, Long productId, ReviewRequestDto requestDto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Customer customer = customerRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
                
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (reviewRepository.existsByCustomerAndProduct(customer, product)) {
            throw new DuplicateResourceException("You have already reviewed this product");
        }

        boolean verifiedPurchase = isVerifiedPurchase(customer, product);
        
        Review review = new Review();
        review.setProduct(product);
        review.setCustomer(customer);
        review.setRating(requestDto.getRating());
        review.setComment(requestDto.getComment());
        review.setCreatedAt(LocalDateTime.now());
        review.setHelpfulCount(0);
        review.setVerifiedPurchase(verifiedPurchase);

        Review savedReview = reviewRepository.save(review);

        updateProductAverageRating(product);

        return mapToResponseDto(savedReview);
    }

    @Override
    public ReviewResponseDto getOwnReview(String email, Long productId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with " + email));
        
        Customer customer = customerRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Review review = reviewRepository.findByCustomerAndProduct(customer, product)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found for this product"));

        return mapToResponseDto(review);
    }

    @Override
    public List<ReviewResponseDto> getAllReview(Long productId) {
        List<Review> reviews = reviewRepository.findAllReviewsByProductId(productId);

        if (reviews.isEmpty()) {
            return Collections.emptyList();
        }

        return reviews.stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    @Override
    @Transactional
    public ReviewResponseDto editReview(Long productId, String email, ReviewUpdateRequestDto requestDto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Customer customer = customerRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Review review = reviewRepository.findByCustomerAndProduct(customer, product)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found for this product"));
        
        if (requestDto.getRating() != null) {
            review.setRating(requestDto.getRating());
        }

        if (requestDto.getComment() != null && !requestDto.getComment().trim().isEmpty()) {
            review.setComment(requestDto.getComment());
        }
        
        review.setUpdateAt(LocalDateTime.now());

        Review updatedReview = reviewRepository.save(review);

        updateProductAverageRating(product);

        return mapToResponseDto(updatedReview);
    }

    @Override
    @Transactional
    public void deleteReview(Long productId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Customer customer = customerRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Review review = reviewRepository.findByCustomerAndProduct(customer, product)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found for this product"));

        reviewRepository.delete(review);

        updateProductAverageRating(product);

        log.info("Review {} deleted by customer {}. Product rating updated.", 
                review.getId(), customer.getId());
    }

    private boolean isVerifiedPurchase(Customer customer, Product product) {
        List<Order> orders = orderRepository.findByCustomer(customer);
        
        for (Order order : orders) {
            if (order.getStatus() == Order.OrderStatus.DELIVERED) {
                List<OrderItem> orderItems = orderItemRepository.findByOrder(order);
                for (OrderItem item : orderItems) {
                    if (item.getProduct().getId().equals(product.getId())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void updateProductAverageRating(Product product) {
        List<Review> reviews = reviewRepository.findByProduct(product);
        if (reviews.isEmpty()) {
            product.setAverageRating(0.0);
            product.setTotalReviews(0);
        } else {
            double average = reviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);
            product.setAverageRating(average);
            product.setTotalReviews(reviews.size());
        }
        productRepository.save(product);
    }

    private ReviewResponseDto mapToResponseDto(Review review) {
        ReviewResponseDto dto = new ReviewResponseDto();
        dto.setId(review.getId());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setHelpfulCount(review.getHelpfulCount());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setVerifiedPurchase(review.getVerifiedPurchase());
        
        String customerName = review.getCustomer() != null && review.getCustomer().getUser() != null 
                ? review.getCustomer().getUser().getName() 
                : "Anonymous";
        dto.setCustomerName(customerName);
        
        dto.setProductId(review.getProduct().getId());
        
        return dto;
    }
}