package com.pranta.ecommerce.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;

import com.pranta.ecommerce.Dto.CartItemRequestDto;
import com.pranta.ecommerce.Dto.CartItemResponseDto;
import com.pranta.ecommerce.Dto.CartResponseDto;
import com.pranta.ecommerce.Entity.Cart;
import com.pranta.ecommerce.Entity.CartItem;
import com.pranta.ecommerce.Entity.Customer;
import com.pranta.ecommerce.Entity.Product;
import com.pranta.ecommerce.Entity.User;
import com.pranta.ecommerce.Exceptions.InvalidRequestException;
import com.pranta.ecommerce.Exceptions.ResourceNotFoundException;
import com.pranta.ecommerce.Repository.CartItemRepository;
import com.pranta.ecommerce.Repository.CartRepository;
import com.pranta.ecommerce.Repository.CustomerRepository;
import com.pranta.ecommerce.Repository.ProductRepository;
import com.pranta.ecommerce.Repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartService {
    
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    @Transactional
    public CartItemResponseDto addToCart(CartItemRequestDto request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with this email"));

        Customer customer = customerRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Cart cart = cartRepository.findByCustomerId(customer.getId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setCustomer(customer);
                    newCart.setGrandTotal(BigDecimal.ZERO);
                    return cartRepository.save(newCart);
                });

        refreshCart(cart);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with this Id"));

        if (!product.isAvailable()) {
            throw new InvalidRequestException("Product is out of stock");
        }

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
            throw new InvalidRequestException("Not enough stock");
        }

        // Fix 1 & 2 — single price source, reuse newQuantity
        BigDecimal currentPrice = product.getDiscountedPrice();
        item.setPrice(currentPrice);
        item.setQuantity(newQuantity);
        item.setTotalPrice(currentPrice.multiply(BigDecimal.valueOf(newQuantity)));

        CartItem savedItem = cartItemRepository.save(item);
        updateCartGrandTotal(cart);

        return mapToDto(savedItem);
    }

    public CartResponseDto getCartByUserEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with this email"));

        Customer customer = customerRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Cart cart = cartRepository.findByCustomerId(customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        refreshCart(cart);

        // Fix 4 — reload from DB after refresh to get updated items and grandTotal
        Cart updatedCart = cartRepository.findById(cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        List<CartItemResponseDto> itemDtos = updatedCart.getItems()
                .stream()
                .map(this::mapToDto)
                .toList();

        return new CartResponseDto(itemDtos, updatedCart.getGrandTotal());
    }

    @Transactional
    public CartItemResponseDto updateCartItemQuantity(String email, Long productId, Integer newQuantity) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with this Id"));

        Customer customer = customerRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Cart cart = cartRepository.findByCustomerId(customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        refreshCart(cart);

        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        item.setQuantity(newQuantity);
        item.setTotalPrice(item.getPrice().multiply(BigDecimal.valueOf(newQuantity)));

        CartItem savedItem = cartItemRepository.save(item);
        updateCartGrandTotal(cart);

        return mapToDto(savedItem);
    }

    @Transactional
    public void clearCart(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with this Id"));

        Customer customer = customerRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Cart cart = cartRepository.findByCustomerId(customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        cartItemRepository.deleteByCartId(cart.getId());

        cart.setGrandTotal(BigDecimal.ZERO);
        cartRepository.save(cart);
    }

    @Transactional
    private void updateCartGrandTotal(Cart cart) {
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());

        BigDecimal grandTotal = items.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        cart.setGrandTotal(grandTotal);
        cartRepository.save(cart);
    }

    @Transactional
    private void refreshCart(Cart cart) {
        boolean changed = false;

        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();

            if (!product.isAvailable() || product.getStock() < item.getQuantity()) {
                cartItemRepository.delete(item);
                changed = true;
            } else {
                // Fix 3 — use getDiscountedPrice() which checks isDiscountCurrentlyActive() internally
                BigDecimal newPrice = product.getDiscountedPrice();
                if (item.getPrice().compareTo(newPrice) != 0) {
                    item.setPrice(newPrice);
                    item.setTotalPrice(newPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
                    cartItemRepository.save(item);
                    changed = true;
                }
            }
        }

        if (changed) {
            updateCartGrandTotal(cart);
        }
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