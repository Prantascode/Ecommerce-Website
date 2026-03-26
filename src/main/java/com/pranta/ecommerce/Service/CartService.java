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

   public CartItemResponseDto addToCart(CartItemRequestDto request) {
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new RuntimeException("User Not Found"));

        Cart cart = cartRepository.findByUserId(request.getUserId())
            .orElseGet(() -> {
                Cart newCart = new Cart();
                newCart.setUser(user);
                return cartRepository.save(newCart);
            });

        Product product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> new RuntimeException("Product Not Found"));

        CartItem item = cartItemRepository
            .findByCartIdAndProductId(cart.getId(), product.getId())
            .orElse(new CartItem());

        if (item.getId() == null) {
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(request.getQuantity());
        } else {
            item.setQuantity(item.getQuantity() + request.getQuantity());
        }
        
        item.setPrice(product.getPrice()); 
        BigDecimal total = product.getPrice().multiply(new BigDecimal(item.getQuantity()));
        item.setTotalPrice(total);

        CartItem savedItem = cartItemRepository.save(item);
           
        BigDecimal newGrandTotal = cart.getItems().stream()
            .map(CartItem::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        cart.setGrandTotal(newGrandTotal);
        cartRepository.save(cart);

        return mapToDto(savedItem);
    }
    public CartResponseDto getCartByUserId(Long userId){
        Cart cart = cartRepository.findByUserId(userId)
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

    public CartItemResponseDto updateCartItemQuantity(Long userId,Long productId,Integer newQauntity){

        Cart cart = cartRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Cart not found"));
        
        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
            .orElseThrow(() -> new RuntimeException("Item not found"));

        item.setQuantity(newQauntity);
        item.setTotalPrice(item.getPrice().multiply(new BigDecimal(newQauntity)));

        return mapToDto(cartItemRepository.save(item));
    }
    @Transactional
    public void clearCart(Long userId){
        Cart cart = cartRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Cart not found"));

        cartItemRepository.deleteByCartId(cart.getId());
    }

    private CartItemResponseDto mapToDto(CartItem item){
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
