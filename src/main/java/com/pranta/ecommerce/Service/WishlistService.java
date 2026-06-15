package com.pranta.ecommerce.Service;


import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.pranta.ecommerce.Dto.WishlistRequestDto;
import com.pranta.ecommerce.Dto.WishlistResponseDto;
import com.pranta.ecommerce.Entity.Product;
import com.pranta.ecommerce.Entity.User;
import com.pranta.ecommerce.Entity.Wishlist;
import com.pranta.ecommerce.Exceptions.InvalidRequestException;
import com.pranta.ecommerce.Exceptions.ResourceNotFoundException;
import com.pranta.ecommerce.Exceptions.UnauthorizedAccessException;
import com.pranta.ecommerce.Repository.ProductRepository;
import com.pranta.ecommerce.Repository.UserRepository;
import com.pranta.ecommerce.Repository.WishlistRepository;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor
public class WishlistService {
    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public WishlistResponseDto createWishlist(String email,Long productId,WishlistRequestDto request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Wishlist wishlist = new Wishlist();
        wishlist.setWishlistName(request.getWishlistName());
        wishlist.setUser(user);
        wishlist.setProduct(product);

        Wishlist saved = wishlistRepository.save(wishlist);

        return mapToResponse(saved);
    } 

    public WishlistResponseDto getWishlistById(Long id,String email){

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Wishlist wishlist = wishlistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Wishlist not found"));
        if (!wishlist.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("Access Denied");
        }
        return mapToResponse(wishlist);
    }
    public List<WishlistResponseDto> getAllWishlists(String email){
       
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

    public WishlistResponseDto updateWishlist(String email,Long productId,
            WishlistRequestDto request){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Wishlist wishlist = wishlistRepository.findWishlistsByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wishlist not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        wishlist.setWishlistName(request.getWishlistName());
        wishlist.setProduct(product);

        Wishlist updatedWishlist = wishlistRepository.save(wishlist);

        return mapToResponse(updatedWishlist);
    }

    public String deleteWishlist(String email,Long id){

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Wishlist wishlist = wishlistRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Wishlist not found"));

        if (!wishlist.getUser().getId().equals(user.getId())) {
            throw new InvalidRequestException("Access Denied");
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
