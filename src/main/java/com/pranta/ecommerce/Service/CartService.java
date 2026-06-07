package com.pranta.ecommerce.Service;

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

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with this Id"));

        if (!product.isAvailable()) {
            throw new InvalidRequestException("Product is out of stock");
        }

        if (request.getQuantity() <= 0) {
            throw new InvalidRequestException("Quantity must be greater than zero");
        }

        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())
                .orElse(null);

        int newQuantity;
        if (item == null) {
            newQuantity = request.getQuantity();
            item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
        } else {
            newQuantity = item.getQuantity() + request.getQuantity();
        }

        if (product.getStock() < newQuantity) {
            throw new InvalidRequestException(
                String.format("Not enough stock. Available: %d, Requested: %d", 
                    product.getStock(), newQuantity)
            );
        }

        // Calculate current price with active discount
        BigDecimal currentPrice = calculateCurrentProductPrice(product);
        
        item.setPrice(currentPrice);
        item.setQuantity(newQuantity);
        item.setTotalPrice(currentPrice.multiply(BigDecimal.valueOf(newQuantity)));

        CartItem savedItem = cartItemRepository.save(item);
        
        // Refresh cart after adding
        refreshCart(cart);

        return mapToDto(savedItem);
    }

    public CartResponseDto getCartByUserEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with this email"));

        Customer customer = customerRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Cart cart = cartRepository.findByCustomerId(customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        // Refresh cart to update prices and remove invalid items
        refreshCart(cart);

        // Reload from DB after refresh
        Cart updatedCart = cartRepository.findById(cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        List<CartItemResponseDto> itemDtos = updatedCart.getItems()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return new CartResponseDto(itemDtos, updatedCart.getGrandTotal());
    }

    @Transactional
    public CartItemResponseDto updateCartItemQuantity(String email, Long productId, Integer newQuantity) {
        if (newQuantity <= 0) {
            throw new InvalidRequestException("Quantity must be greater than zero");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with this Id"));

        Customer customer = customerRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Cart cart = cartRepository.findByCustomerId(customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        Product product = item.getProduct();
        
        if (product.getStock() < newQuantity) {
            throw new InvalidRequestException(
                String.format("Not enough stock. Available: %d, Requested: %d", 
                    product.getStock(), newQuantity)
            );
        }

        // Update to current price
        BigDecimal currentPrice = calculateCurrentProductPrice(product);
        item.setPrice(currentPrice);
        item.setQuantity(newQuantity);
        item.setTotalPrice(currentPrice.multiply(BigDecimal.valueOf(newQuantity)));

        CartItem savedItem = cartItemRepository.save(item);
        
        // Refresh cart
        refreshCart(cart);

        return mapToDto(savedItem);
    }

    @Transactional
    public void removeItemFromCart(String email, Long productId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with this Id"));

        Customer customer = customerRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Cart cart = cartRepository.findByCustomerId(customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        cartItemRepository.deleteByCartIdAndProductId(cart.getId(), productId);
        
        // Refresh cart
        refreshCart(cart);
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

    // ============================================================
    // CORE REFRESH FUNCTION - Used by both Cart and Order services
    // ============================================================
    
    /**
     * Comprehensive cart refresh function that:
     * 1. Updates all item prices based on current active discounts
     * 2. Removes out-of-stock products
     * 3. Removes products with insufficient stock
     * 4. Recalculates grand total
     * 5. Returns refresh result with details about changes
     * 
     * @param cart Cart to refresh
     * @return CartRefreshResult containing details of what changed
     */
    @Transactional
    public CartRefreshResult refreshCart(Cart cart) {
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            return new CartRefreshResult(false, 0, 0, BigDecimal.ZERO);
        }
        
        log.info("Refreshing cart ID: {} for customer: {}", cart.getId(), cart.getCustomer().getId());
        
        boolean changed = false;
        int removedItemsCount = 0;
        int priceUpdatedCount = 0;
        BigDecimal oldGrandTotal = cart.getGrandTotal();
        
        // Create a copy of items to avoid concurrent modification
        List<CartItem> items = new java.util.ArrayList<>(cart.getItems());
        
        for (CartItem item : items) {
            Product product = item.getProduct();
            
            // Refresh product from database
            Product refreshedProduct = productRepository.findById(product.getId())
                    .orElse(null);
            
            // Case 1: Product no longer exists
            if (refreshedProduct == null) {
                cartItemRepository.delete(item);
                removedItemsCount++;
                changed = true;
                log.debug("Removed item {} - product no longer exists", item.getId());
                continue;
            }
            
            // Case 2: Product out of stock
            if (!refreshedProduct.isAvailable()) {
                cartItemRepository.delete(item);
                removedItemsCount++;
                changed = true;
                log.debug("Removed item {} - product {} is out of stock", 
                    item.getId(), refreshedProduct.getName());
                continue;
            }
            
            // Case 3: Insufficient stock for current quantity
            if (refreshedProduct.getStock() < item.getQuantity()) {
                cartItemRepository.delete(item);
                removedItemsCount++;
                changed = true;
                log.debug("Removed item {} - insufficient stock (need: {}, available: {})", 
                    item.getId(), item.getQuantity(), refreshedProduct.getStock());
                continue;
            }
            
            // Case 4: Price changed due to discount start/expiration
            BigDecimal currentPrice = calculateCurrentProductPrice(refreshedProduct);
            if (item.getPrice().compareTo(currentPrice) != 0) {
                BigDecimal oldPrice = item.getPrice();
                item.setPrice(currentPrice);
                item.setTotalPrice(currentPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
                cartItemRepository.save(item);
                priceUpdatedCount++;
                changed = true;
                log.debug("Updated price for item {} - from {} to {}", 
                    item.getId(), oldPrice, currentPrice);
            }
        }
        
        // Recalculate grand total if changes were made
        BigDecimal newGrandTotal = cart.getGrandTotal();
        if (changed) {
            newGrandTotal = recalculateGrandTotal(cart);
            cart.setGrandTotal(newGrandTotal);
            cartRepository.save(cart);
            log.info("Cart refreshed - Removed: {}, Price updates: {}, Old total: {}, New total: {}", 
                removedItemsCount, priceUpdatedCount, oldGrandTotal, newGrandTotal);
        }
        
        return new CartRefreshResult(changed, removedItemsCount, priceUpdatedCount, newGrandTotal);
    }
    
    /**
     * Refresh cart before order placement
     * This is stricter than regular refresh - it will throw exceptions for issues
     * 
     * @param cart Cart to validate for order
     * @throws InvalidRequestException if cart has invalid items for ordering
     */
    @Transactional
    public void validateAndRefreshCartForOrder(Cart cart) {
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new InvalidRequestException("Cart is empty");
        }
        
        log.info("Validating cart ID: {} for order placement", cart.getId());
        
        StringBuilder errors = new StringBuilder();
        boolean hasErrors = false;
        
        // First, refresh the cart to get latest prices and stock
        CartRefreshResult refreshResult = refreshCart(cart);
        
        if (refreshResult.isChanged()) {
            log.info("Cart was refreshed before order. Removed: {}, Price updates: {}", 
                refreshResult.getRemovedItemsCount(), refreshResult.getPriceUpdatedCount());
        }
        
        // Now validate each item for order placement
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            Product refreshedProduct = productRepository.findById(product.getId())
                    .orElse(null);
            
            if (refreshedProduct == null) {
                errors.append(String.format("Product '%s' no longer exists. ", product.getName()));
                hasErrors = true;
                continue;
            }
            
            if (!refreshedProduct.isAvailable()) {
                errors.append(String.format("Product '%s' is out of stock. ", refreshedProduct.getName()));
                hasErrors = true;
                continue;
            }
            
            if (refreshedProduct.getStock() < item.getQuantity()) {
                errors.append(String.format("Insufficient stock for '%s'. Available: %d, Requested: %d. ", 
                    refreshedProduct.getName(), refreshedProduct.getStock(), item.getQuantity()));
                hasErrors = true;
                continue;
            }
            
            // Verify price is still valid (should be updated by refreshCart)
            BigDecimal currentPrice = calculateCurrentProductPrice(refreshedProduct);
            if (item.getPrice().compareTo(currentPrice) != 0) {
                errors.append(String.format("Price changed for '%s'. Old: %s, New: %s. ", 
                    refreshedProduct.getName(), item.getPrice(), currentPrice));
                hasErrors = true;
            }
        }
        
        if (hasErrors) {
            throw new InvalidRequestException("Cannot place order: " + errors.toString());
        }
        
        log.info("Cart validated successfully for order. Total items: {}, Grand total: {}", 
            cart.getItems().size(), cart.getGrandTotal());
    }
    
    /**
     * Get cart by customer with automatic refresh
     */
    public Cart getCartAndRefresh(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Customer customer = customerRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        
        Cart cart = cartRepository.findByCustomerId(customer.getId())
                .orElse(null);
        
        if (cart != null) {
            refreshCart(cart);
        }
        
        return cart;
    }
    
    /**
     * Recalculate grand total from cart items
     */
    private BigDecimal recalculateGrandTotal(Cart cart) {
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
        
        BigDecimal grandTotal = items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        
        return grandTotal;
    }
    
    /**
     * Calculate current price considering active discounts
     */
    private BigDecimal calculateCurrentProductPrice(Product product) {
        if (product.getDiscounts() == null || product.getDiscounts().isEmpty()) {
            return product.getPrice();
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        // Find best active discount (highest percentage)
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
        
        return product.getPrice().subtract(discountAmount);
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
    
    // ============================================================
    // Inner Class for Refresh Result
    // ============================================================
    
    public static class CartRefreshResult {
        private final boolean changed;
        private final int removedItemsCount;
        private final int priceUpdatedCount;
        private final BigDecimal newGrandTotal;
        
        public CartRefreshResult(boolean changed, int removedItemsCount, int priceUpdatedCount, BigDecimal newGrandTotal) {
            this.changed = changed;
            this.removedItemsCount = removedItemsCount;
            this.priceUpdatedCount = priceUpdatedCount;
            this.newGrandTotal = newGrandTotal;
        }
        
        public boolean isChanged() { return changed; }
        public int getRemovedItemsCount() { return removedItemsCount; }
        public int getPriceUpdatedCount() { return priceUpdatedCount; }
        public BigDecimal getNewGrandTotal() { return newGrandTotal; }
    }
}