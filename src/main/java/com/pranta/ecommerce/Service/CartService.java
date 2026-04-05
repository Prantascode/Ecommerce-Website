package com.pranta.ecommerce.Service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.pranta.ecommerce.Dto.CartItemRequestDto;
import com.pranta.ecommerce.Dto.CartItemResponseDto;
import com.pranta.ecommerce.Dto.CartResponseDto;
import com.pranta.ecommerce.Entity.Cart;
import com.pranta.ecommerce.Entity.CartItem;
import com.pranta.ecommerce.Entity.Product;
import com.pranta.ecommerce.Entity.User;
import com.pranta.ecommerce.Repository.CartItemRepository;
import com.pranta.ecommerce.Repository.CartRepository;
import com.pranta.ecommerce.Repository.ProductRepository;
import com.pranta.ecommerce.Repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartService {
    
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    @Transactional
    public CartItemResponseDto addToCart(CartItemRequestDto request, String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartRepository.findByUserId(user.getId())
            .orElseGet(() -> {
                Cart newCart = new Cart();
                newCart.setUser(user);
                newCart.setGrandTotal(BigDecimal.ZERO);
                return cartRepository.save(newCart);
            });

        Product product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> new RuntimeException("Product not found"));
        

        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())
            .orElseGet(() -> {
                CartItem newItem = new CartItem();
                newItem.setCart(cart);
                newItem.setProduct(product);
                newItem.setQuantity(0);
                return newItem;
            });
            
        int newQuantity = item.getQuantity() + request.getQuantity();
        
        if (product.getStock() < newQuantity) {
            throw new RuntimeException("Not enough stock");
        }

        item.setPrice(product.getPrice());
        item.setQuantity(item.getQuantity() + request.getQuantity());
        item.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));

        CartItem savedItem = cartItemRepository.save(item);

        updateCartGrandTotal(cart);

        return mapToDto(savedItem);
    }

    public CartResponseDto getCartByUserEmail(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartRepository.findByUserId(user.getId())
            .orElseThrow(() -> new RuntimeException("Cart not found"));

        List<CartItemResponseDto> itemDtos = cart.getItems()
            .stream()
            .map(this::mapToDto)
            .toList();

        BigDecimal grandTotal = itemDtos.stream()
            .map(CartItemResponseDto::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponseDto(itemDtos, grandTotal);
    }

    @Transactional
    public CartItemResponseDto updateCartItemQuantity(String email, Long productId, Integer newQuantity) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartRepository.findByUserId(user.getId())
            .orElseThrow(() -> new RuntimeException("Cart not found"));

        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
            .orElseThrow(() -> new RuntimeException("Item not found"));

        item.setQuantity(newQuantity);
        item.setTotalPrice(item.getPrice().multiply(BigDecimal.valueOf(newQuantity)));

        CartItem savedItem = cartItemRepository.save(item);

        updateCartGrandTotal(cart);

        return mapToDto(savedItem);
    }

    @Transactional
    public void clearCart(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartRepository.findByUserId(user.getId())
            .orElseThrow(() -> new RuntimeException("Cart not found"));

        cartItemRepository.deleteByCartId(cart.getId());

        cart.setGrandTotal(BigDecimal.ZERO);
        cartRepository.save(cart);
    }

    @Transactional
    private void updateCartGrandTotal(Cart cart) {
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());

        BigDecimal grandTotal = items.stream()
            .map(CartItem::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        cart.setGrandTotal(grandTotal);
        cartRepository.save(cart);
    }

    private CartItemResponseDto mapToDto(CartItem item) {
        CartItemResponseDto dto = new CartItemResponseDto();
        dto.setId(item.getId());
        dto.setProductId(item.getProduct().getId());
        dto.setProductName(item.getProduct().getName());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getPrice());
        dto.setTotalPrice(item.getTotalPrice());
        return dto;
    }
}