package com.pranta.ecommerce.Service.Impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.pranta.ecommerce.Dto.CartItemRequestDto;
import com.pranta.ecommerce.Dto.CartItemResponseDto;
import com.pranta.ecommerce.Dto.CartResponseDto;
import com.pranta.ecommerce.Entity.Cart;
import com.pranta.ecommerce.Entity.CartItem;
import com.pranta.ecommerce.Entity.Customer;
import com.pranta.ecommerce.Entity.Discount;
import com.pranta.ecommerce.Entity.Product;
import com.pranta.ecommerce.Entity.User;
import com.pranta.ecommerce.Exceptions.InvalidRequestException;
import com.pranta.ecommerce.Exceptions.ResourceNotFoundException;
import com.pranta.ecommerce.Repository.CartItemRepository;
import com.pranta.ecommerce.Repository.CartRepository;
import com.pranta.ecommerce.Repository.CustomerRepository;
import com.pranta.ecommerce.Repository.ProductRepository;
import com.pranta.ecommerce.Repository.UserRepository;
import com.pranta.ecommerce.Service.CartService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    @Override
    @Transactional
    public CartItemResponseDto addToCart(CartItemRequestDto request, String email) {
        User user = getUserByEmail(email);
        Customer customer = getCustomerByUser(user);
        
        Cart cart = getOrCreateCart(customer);
        
        Product product = getProductById(request.getProductId());
        
        validateProductAvailability(product);
        validateQuantity(request.getQuantity());
        
        CartItem item = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), product.getId())
                .orElse(null);
        
        int newQuantity = (item == null) ? request.getQuantity() : item.getQuantity() + request.getQuantity();
        
        validateStock(product, newQuantity);
        
        BigDecimal currentPrice = calculateCurrentProductPrice(product);
        
        if (item == null) {
            item = createNewCartItem(cart, product, currentPrice, request.getQuantity());
        } else {
            updateExistingCartItem(item, currentPrice, newQuantity);
        }
        
        CartItem savedItem = cartItemRepository.save(item);
        
        updateCartGrandTotal(cart);
        
        log.info("Added {} of product '{}' to cart for customer {}", 
            request.getQuantity(), product.getName(), customer.getId());
        
        return mapToCartItemResponseDto(savedItem);
    }

    @Override
    public CartResponseDto getCartByUserEmail(String email) {
        User user = getUserByEmail(email);
        Customer customer = getCustomerByUser(user);
        
        Cart cart = cartRepository.findByCustomerId(customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        
        refreshCart(cart);
        
        Cart updatedCart = cartRepository.findById(cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        List<CartItemResponseDto> itemDtos = updatedCart.getItems()
                .stream()
                .map(this::mapToCartItemResponseDto)
                .collect(Collectors.toList());
        
        return new CartResponseDto(itemDtos, updatedCart.getGrandTotal());
    }

    @Override
    @Transactional
    public CartItemResponseDto updateCartItemQuantity(String email, Long productId, Integer newQuantity) {
        if (newQuantity <= 0) {
            throw new InvalidRequestException("Quantity must be greater than zero");
        }
        
        User user = getUserByEmail(email);
        Customer customer = getCustomerByUser(user);
        Cart cart = getCartByCustomer(customer);
     
        CartItem item = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found in cart"));
        
        Product product = item.getProduct();
        
        validateStock(product, newQuantity);
        BigDecimal currentPrice = calculateCurrentProductPrice(product);
        item.setPrice(currentPrice);
        item.setQuantity(newQuantity);
        item.setTotalPrice(currentPrice.multiply(BigDecimal.valueOf(newQuantity)));
        
        CartItem savedItem = cartItemRepository.save(item);
      
        updateCartGrandTotal(cart);
        
        log.info("Updated quantity of product '{}' to {} for customer {}", 
            product.getName(), newQuantity, customer.getId());
        
        return mapToCartItemResponseDto(savedItem);
    }

    @Override
    @Transactional
    public void removeItemFromCart(String email, Long productId) {
        User user = getUserByEmail(email);
        Customer customer = getCustomerByUser(user);
        Cart cart = getCartByCustomer(customer);
        
        int deletedCount = cartItemRepository.deleteByCartIdAndProductId(cart.getId(), productId);
        
        if (deletedCount == 0) {
            throw new ResourceNotFoundException("Item not found in cart");
        }
        
        updateCartGrandTotal(cart);
        
        log.info("Removed product {} from cart for customer {}", productId, customer.getId());
    }

    @Override
    @Transactional
    public void clearCart(String email) {
        User user = getUserByEmail(email);
        Customer customer = getCustomerByUser(user);
        Cart cart = getCartByCustomer(customer);
        
        cartItemRepository.deleteByCartId(cart.getId());
        
        cart.setGrandTotal(BigDecimal.ZERO);
        cartRepository.save(cart);
        
        log.info("Cleared cart for customer {}", customer.getId());
    }

    @Override
    @Transactional
    public CartRefreshResult refreshCart(Cart cart) {
        if (cart == null) {
            return new CartRefreshResult(false, 0, 0, BigDecimal.ZERO);
        }
        
        log.debug("Refreshing cart ID: {}", cart.getId());
        
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
        
        if (items.isEmpty()) {
            cart.setGrandTotal(BigDecimal.ZERO);
            cartRepository.save(cart);
            return new CartRefreshResult(false, 0, 0, BigDecimal.ZERO);
        }
        
        boolean changed = false;
        int removedCount = 0;
        int priceUpdatedCount = 0;
        
        for (CartItem item : items) {
            Product product = item.getProduct();
            
            Product refreshedProduct = productRepository.findById(product.getId()).orElse(null);
            if (refreshedProduct == null) {
                cartItemRepository.delete(item);
                removedCount++;
                changed = true;
                log.debug("Removed item {} - product no longer exists", item.getId());
                continue;
            }
            
            if (!refreshedProduct.isAvailable()) {
                cartItemRepository.delete(item);
                removedCount++;
                changed = true;
                log.debug("Removed item {} - product out of stock", item.getId());
                continue;
            }
            
            if (refreshedProduct.getStock() < item.getQuantity()) {
                cartItemRepository.delete(item);
                removedCount++;
                changed = true;
                log.debug("Removed item {} - insufficient stock", item.getId());
                continue;
            }
           
            BigDecimal currentPrice = calculateCurrentProductPrice(refreshedProduct);
            if (item.getPrice().compareTo(currentPrice) != 0) {
                item.setPrice(currentPrice);
                item.setTotalPrice(currentPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
                cartItemRepository.save(item);
                priceUpdatedCount++;
                changed = true;
                log.debug("Updated price for item {} from {} to {}", 
                    item.getId(), item.getPrice(), currentPrice);
            }
        }
        
        updateCartGrandTotal(cart);
        
        if (changed) {
            log.info("Cart refreshed - Removed: {}, Price updates: {}", removedCount, priceUpdatedCount);
        }
        
        return new CartRefreshResult(changed, removedCount, priceUpdatedCount, cart.getGrandTotal());
    }

    @Override
    @Transactional
    public void validateCartForOrder(Cart cart) {
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new InvalidRequestException("Cart is empty");
        }
        
        log.info("Validating cart {} for order", cart.getId());
    
        refreshCart(cart);
        
        Cart refreshedCart = cartRepository.findById(cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        
        StringBuilder errors = new StringBuilder();
        boolean hasErrors = false;
        
        for (CartItem item : refreshedCart.getItems()) {
            Product product = item.getProduct();
            Product currentProduct = productRepository.findById(product.getId()).orElse(null);
            
            if (currentProduct == null) {
                errors.append(String.format("Product '%s' no longer exists. ", product.getName()));
                hasErrors = true;
                continue;
            }
            
            if (!currentProduct.isAvailable()) {
                errors.append(String.format("Product '%s' is out of stock. ", currentProduct.getName()));
                hasErrors = true;
                continue;
            }
            
            if (currentProduct.getStock() < item.getQuantity()) {
                errors.append(String.format("Insufficient stock for '%s'. Available: %d, Requested: %d. ", 
                    currentProduct.getName(), currentProduct.getStock(), item.getQuantity()));
                hasErrors = true;
                continue;
            }
            
            BigDecimal currentPrice = calculateCurrentProductPrice(currentProduct);
            if (item.getPrice().compareTo(currentPrice) != 0) {
                errors.append(String.format("Price changed for '%s'. Old: %s, New: %s. ", 
                    currentProduct.getName(), item.getPrice(), currentPrice));
                hasErrors = true;
            }
        }
        
        if (hasErrors) {
            throw new InvalidRequestException("Cannot place order: " + errors.toString());
        }
        
        log.info("Cart validated successfully. Total items: {}, Grand total: {}", 
            refreshedCart.getItems().size(), refreshedCart.getGrandTotal());
    }

    @Override
    public Cart getCartAndRefresh(String email) {
        User user = getUserByEmail(email);
        Customer customer = getCustomerByUser(user);
        
        Cart cart = cartRepository.findByCustomerId(customer.getId()).orElse(null);
        
        if (cart != null) {
            refreshCart(cart);
            cart = cartRepository.findById(cart.getId()).orElse(null);
        }
        
        return cart;
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private Customer getCustomerByUser(User user) {
        return customerRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found for user: " + user.getEmail()));
    }

    private Product getProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
    }

    private Cart getOrCreateCart(Customer customer) {
        return cartRepository.findByCustomerId(customer.getId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setCustomer(customer);
                    newCart.setGrandTotal(BigDecimal.ZERO);
                    log.info("Created new cart for customer: {}", customer.getId());
                    return cartRepository.save(newCart);
                });
    }

    private Cart getCartByCustomer(Customer customer) {
        return cartRepository.findByCustomerId(customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for customer"));
    }

    private void validateProductAvailability(Product product) {
        if (!product.isAvailable()) {
            throw new InvalidRequestException("Product '" + product.getName() + "' is out of stock");
        }
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new InvalidRequestException("Quantity must be greater than zero");
        }
    }

    private void validateStock(Product product, int requestedQuantity) {
        if (product.getStock() < requestedQuantity) {
            throw new InvalidRequestException(
                String.format("Not enough stock for '%s'. Available: %d, Requested: %d",
                    product.getName(), product.getStock(), requestedQuantity)
            );
        }
    }

    private CartItem createNewCartItem(Cart cart, Product product, BigDecimal price, int quantity) {
        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProduct(product);
        item.setPrice(price);
        item.setQuantity(quantity);
        item.setTotalPrice(price.multiply(BigDecimal.valueOf(quantity)));
        return item;
    }

    private void updateExistingCartItem(CartItem item, BigDecimal currentPrice, int newQuantity) {
        item.setPrice(currentPrice);
        item.setQuantity(newQuantity);
        item.setTotalPrice(currentPrice.multiply(BigDecimal.valueOf(newQuantity)));
    }

    private void updateCartGrandTotal(Cart cart) {
        BigDecimal grandTotal = recalculateGrandTotal(cart);
        cart.setGrandTotal(grandTotal);
        cartRepository.save(cart);
        log.debug("Updated cart {} grand total to: {}", cart.getId(), grandTotal);
    }

    private BigDecimal recalculateGrandTotal(Cart cart) {
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
        
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal grandTotal = items.stream()
                .map(item -> item.getTotalPrice() != null ? 
                    item.getTotalPrice() : 
                    item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        
        log.debug("Recalculated grand total for cart {}: {}", cart.getId(), grandTotal);
        
        return grandTotal;
    }

    private BigDecimal calculateCurrentProductPrice(Product product) {
        if (product.getDiscounts() == null || product.getDiscounts().isEmpty()) {
            return product.getPrice();
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        Discount bestDiscount = product.getDiscounts().stream()
                .filter(discount -> discount.getActive() && discount.isDiscountCurrentlyActive())
                .filter(discount -> {
                    LocalDateTime start = discount.getDiscountStartDate();
                    LocalDateTime end = discount.getDiscountEndDate();
                    return (start == null || !now.isBefore(start)) && 
                           (end == null || !now.isAfter(end));
                })
                .max((d1, d2) -> d1.getDiscountPercent().compareTo(d2.getDiscountPercent()))
                .orElse(null);
        
        if (bestDiscount == null) {
            return product.getPrice();
        }
        
        BigDecimal discountAmount = product.getPrice()
                .multiply(bestDiscount.getDiscountPercent())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        
        BigDecimal discountedPrice = product.getPrice().subtract(discountAmount);
        
        log.debug("Calculated discounted price for product {}: {} -> {}", 
            product.getName(), product.getPrice(), discountedPrice);
        
        return discountedPrice;
    }

    private CartItemResponseDto mapToCartItemResponseDto(CartItem item) {
        CartItemResponseDto dto = new CartItemResponseDto();
        dto.setId(item.getId());
        dto.setProductId(item.getProduct().getId());
        dto.setProductName(item.getProduct().getName());
        dto.setProductImage(item.getProduct().getImageUrl());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getPrice());
        dto.setTotalPrice(item.getTotalPrice());
        return dto;
    }
}