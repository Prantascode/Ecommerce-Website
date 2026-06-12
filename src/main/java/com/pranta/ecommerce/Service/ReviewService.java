package com.pranta.ecommerce.Service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.pranta.ecommerce.Dto.ReviewRequestDto;
import com.pranta.ecommerce.Dto.ReviewResponseDto;
import com.pranta.ecommerce.Entity.Customer;
import com.pranta.ecommerce.Entity.Order;
import com.pranta.ecommerce.Entity.OrderItem;
import com.pranta.ecommerce.Entity.Product;
import com.pranta.ecommerce.Entity.Review;
import com.pranta.ecommerce.Entity.User;
import com.pranta.ecommerce.Exceptions.ResourceNotFoundException;
import com.pranta.ecommerce.Repository.CustomerRepository;
import com.pranta.ecommerce.Repository.OrderItemRepository;
import com.pranta.ecommerce.Repository.OrderRepository;
import com.pranta.ecommerce.Repository.ProductRepository;
import com.pranta.ecommerce.Repository.ReviewRepository;
import com.pranta.ecommerce.Repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final OrderItemRepository orderItemRepository;  // Fixed typo

    public ReviewResponseDto reviewOrderedProduct(String email, Long productId, ReviewRequestDto requestDto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Customer customer = customerRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
                
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (reviewRepository.existsByCustomerAndProduct(customer, product)) {
            throw new RuntimeException("You have already reviewed this product");
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

    private boolean isVerifiedPurchase(Customer customer, Product product) {
        List<Order> orders = orderRepository.findByCustomer(customer);
        
        for (Order order : orders) {
            if (order.getStatus() != null && "DELIVERED".equals(order.getStatus().toString())) {
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