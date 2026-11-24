package com.pranta.ecommerce.Service.Impl;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.pranta.ecommerce.Dto.Request.WishlistRequestDto;
import com.pranta.ecommerce.Dto.Response.WishlistResponseDto;
import com.pranta.ecommerce.Entity.Product;
import com.pranta.ecommerce.Entity.User;
import com.pranta.ecommerce.Entity.Wishlist;
import com.pranta.ecommerce.Exceptions.DuplicateResourceException;
import com.pranta.ecommerce.Exceptions.ResourceNotFoundException;
import com.pranta.ecommerce.Exceptions.UnauthorizedAccessException;
import com.pranta.ecommerce.Repository.ProductRepository;
import com.pranta.ecommerce.Repository.UserRepository;
import com.pranta.ecommerce.Repository.WishlistRepository;
import com.pranta.ecommerce.Service.WishlistService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {
    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public WishlistResponseDto createWishlist(String email, Long productId, WishlistRequestDto request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (wishlistRepository.existsByUserIdAndProductId(user.getId(), productId)) {
            throw new DuplicateResourceException("Product is already in your wishlist");
        }

        Wishlist wishlist = new Wishlist();
        wishlist.setWishlistName(request.getWishlistName());
        wishlist.setUser(user);
        wishlist.setProduct(product);

        Wishlist saved = wishlistRepository.save(wishlist);

        return mapToResponse(saved);
    } 

    @Override
    public WishlistResponseDto getWishlistById(Long id, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Wishlist wishlist = wishlistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Wishlist not found"));
        if (!wishlist.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("Access Denied");
        }
        return mapToResponse(wishlist);
    }

    @Override
    public List<WishlistResponseDto> getAllWishlists(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Wishlist> wishlist = wishlistRepository.findAllWishlistsByUserId(user.getId());

        if (wishlist.isEmpty()) {
            return Collections.emptyList();
        }
        return wishlist.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public WishlistResponseDto updateWishlist(String email, Long productId, WishlistRequestDto request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Wishlist wishlist = wishlistRepository.findByUserIdAndProductId(user.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("Wishlist item not found for this product"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        wishlist.setWishlistName(request.getWishlistName());
        wishlist.setProduct(product);

        Wishlist updatedWishlist = wishlistRepository.save(wishlist);

        return mapToResponse(updatedWishlist);
    }

    @Override
    @Transactional
    public String deleteWishlist(String email, Long id) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Wishlist wishlist = wishlistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Wishlist not found"));

        if (!wishlist.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("Access Denied");
        }
        wishlistRepository.delete(wishlist);

        return "Wishlist deleted successfully";
    }
    
    private WishlistResponseDto mapToResponse(Wishlist wishlist) {
        WishlistResponseDto dto = new WishlistResponseDto();

        dto.setId(wishlist.getId());
        dto.setWishlistName(wishlist.getWishlistName());
        dto.setUserId(wishlist.getUser().getId());
        dto.setUsername(wishlist.getUser().getName());
        dto.setCreatedAt(wishlist.getCreatedAt());
        dto.setUpdatedAt(wishlist.getUpdatedAt());
        dto.setProductId(wishlist.getProduct().getId());
        dto.setProductName(wishlist.getProduct().getName());

        return dto;
    }
}
