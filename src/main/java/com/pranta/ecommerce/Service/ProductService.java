package com.pranta.ecommerce.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.pranta.ecommerce.Dto.DiscountResponseDto;
import com.pranta.ecommerce.Dto.ProductRequestDto;
import com.pranta.ecommerce.Dto.ProductResponseDto;
import com.pranta.ecommerce.Dto.ProductStockResponseDto;
import com.pranta.ecommerce.Entity.Brand;
import com.pranta.ecommerce.Entity.Category;
import com.pranta.ecommerce.Entity.Discount;
import com.pranta.ecommerce.Entity.Product;
import com.pranta.ecommerce.Exceptions.DuplicateResourceException;
import com.pranta.ecommerce.Exceptions.ResourceNotFoundException;
import com.pranta.ecommerce.Repository.BrandRepository;
import com.pranta.ecommerce.Repository.CategoryRepository;
import com.pranta.ecommerce.Repository.ProductRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;

    public ProductResponseDto createProduct(ProductRequestDto dto) {

        Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Brand brand = brandRepository.findById(dto.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Brand not found"));

        String productName = dto.getName().trim();

        if (productRepository.findByName(productName).isPresent()) {
            throw new DuplicateResourceException("Product with this name already exists");
        }

        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setImageUrl(dto.getImageUrl());
        product.setStock(dto.getStock());
        product.setColor(dto.getColor());
        product.setCategory(category);
        product.setBrand(brand);

        Product savedProduct = productRepository.save(product);

        return mapToResponse(savedProduct);
    }

    public List<ProductResponseDto> getAllProducts() {

        List<Product> products = productRepository.findAll();
        if (products.isEmpty()) {
            throw new ResourceNotFoundException("No products found!");
        }
        return products.stream()
                        .map(this::mapToResponse)
                        .collect(Collectors.toList());
    }

    public ProductResponseDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product is not found with this id"));

        return mapToResponse(product);
    }

    public ProductResponseDto getProductByName(String name) {
        Product product = productRepository.findByName(name)
                    .orElseThrow(() -> new ResourceNotFoundException("Product is not found with this name"));
        
        return mapToResponse(product);
    }

    public List<ProductResponseDto> getProductByPriceRange(double minPrice, double maxPrice) {
        List<Product> products = productRepository.findByPriceBetween(minPrice, maxPrice);

        if (products.isEmpty()) {
            return Collections.emptyList();
        }
        return products.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<ProductResponseDto> getProductByCategory(Category category) {
        return productRepository.findByCategory(category)
                    .stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
    }

    public List<ProductResponseDto> getProductByBrand(Brand brand) {
        return productRepository.findByBrand(brand)
                    .stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
    }

    public List<ProductResponseDto> getProductsByColor(String color) {
        List<Product> products = productRepository.findByColor(color);
                    
        if (products.isEmpty()) {
            return Collections.emptyList();
        }
        return products.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<ProductResponseDto> getOutOfStockProducts() {
        List<Product> products = productRepository.findByStock(0);

        if (products.isEmpty()) {
            return Collections.emptyList();
        }
        return products.stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Get products with active discounts (on sale)
     */
    public List<ProductResponseDto> getDiscountedProducts() {
        List<Product> products = productRepository.findAll();
        
        return products.stream()
                .filter(product -> hasActiveDiscount(product))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ProductStockResponseDto getStockByProduct(Long productId) {
        Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product Not found with this ID"));

        return new ProductStockResponseDto(
            product.getId(),
            product.getName(),
            product.getStock()
        );
    }

    @Transactional
    public List<ProductResponseDto> getStockLimitResponse(int threshold) {
        List<Product> products = productRepository.findByStockLessThanEqual(threshold);

        return products.stream()
                    .map(this::mapToResponse)
                    .toList();
    }

    @Transactional
    public ProductResponseDto updateProduct(Long id, ProductRequestDto dto) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product is not found with this id"));

        Category category = categoryRepository.findById(dto.getCategoryId())
                        .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Brand brand = brandRepository.findById(dto.getBrandId())
                        .orElseThrow(() -> new ResourceNotFoundException("Brand not found"));

        // Check for duplicate name only if the name is being changed
        if (!product.getName().equals(dto.getName().trim())) {
            String productName = dto.getName().trim();
            if (productRepository.findByName(productName).isPresent()) {
                throw new DuplicateResourceException("Product with this name already exists");
            }
        }

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setImageUrl(dto.getImageUrl());
        product.setCategory(category);
        product.setColor(dto.getColor());
        product.setBrand(brand);

        return mapToResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponseDto updateStock(int quantity, Long productId) {
        Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product is not found with this id"));
    
        product.setStock(quantity);
        Product saveProduct = productRepository.save(product);

        return mapToResponse(saveProduct);
    }

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    // ─────────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────────

    private ProductResponseDto mapToResponse(Product product) {
        ProductResponseDto dto = new ProductResponseDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setImageUrl(product.getImageUrl());
        dto.setStock(product.getStock());
        dto.setColor(product.getColor());
        dto.setAvailable(product.isAvailable());
        dto.setCategory(product.getCategory());
        dto.setBrand(product.getBrand());
        dto.setAverageRating(product.getAverageRating());
        dto.setTotalReviews(product.getTotalReviews());
        
        // FIXED: Only get active discounts instead of all discounts
        List<Discount> productDiscounts = product.getDiscounts();
        List<DiscountResponseDto> activeDiscountDtos = new java.util.ArrayList<>();
        
        if (productDiscounts != null && !productDiscounts.isEmpty()) {
            // Filter only active discounts
            List<Discount> activeDiscounts = productDiscounts.stream()
                    .filter(Discount::isDiscountCurrentlyActive)  // Only currently active discounts
                    .collect(Collectors.toList());
            
            if (!activeDiscounts.isEmpty()) {
                // Map only active discounts to DTOs
                activeDiscountDtos = activeDiscounts.stream()
                        .map(this::convertToDiscountDto)
                        .collect(Collectors.toList());
                dto.setDiscounts(activeDiscountDtos);
                
                // Find the best active discount (highest percentage)
                DiscountResponseDto bestActiveDiscount = activeDiscountDtos.stream()
                        .max((d1, d2) -> d1.getDiscountPercent().compareTo(d2.getDiscountPercent()))
                        .orElse(null);
                
                dto.setActiveDiscount(bestActiveDiscount);
                dto.setHasActiveDiscount(bestActiveDiscount != null);
                
                // Calculate discounted price using best active discount
                if (bestActiveDiscount != null) {
                    BigDecimal discountedPrice = calculateDiscountedPrice(
                            product.getPrice(), 
                            bestActiveDiscount.getDiscountPercent()
                    );
                    dto.setDiscountedPrice(discountedPrice);
                } else {
                    dto.setDiscountedPrice(product.getPrice());
                }
            } else {
                // No active discounts
                dto.setDiscounts(Collections.emptyList());
                dto.setActiveDiscount(null);
                dto.setHasActiveDiscount(false);
                dto.setDiscountedPrice(product.getPrice());
            }
        } else {
            dto.setDiscounts(Collections.emptyList());
            dto.setActiveDiscount(null);
            dto.setHasActiveDiscount(false);
            dto.setDiscountedPrice(product.getPrice());
        }
        
        return dto;
    }

    private DiscountResponseDto convertToDiscountDto(Discount discount) {
        DiscountResponseDto dto = new DiscountResponseDto();
        dto.setId(discount.getId());
        dto.setName(discount.getName());
        dto.setDiscountPercent(discount.getDiscountPercent());
        dto.setActive(discount.getActive());
        dto.setCurrentlyActive(discount.isDiscountCurrentlyActive());
        dto.setDiscountStartDate(discount.getDiscountStartDate());
        dto.setDiscountEndDate(discount.getDiscountEndDate());
        dto.setProductId(discount.getProduct() != null ? discount.getProduct().getId() : null);
        dto.setProductName(discount.getProduct() != null ? discount.getProduct().getName() : null);
        
        // Calculate discounted price for this specific discount
        if (discount.isDiscountCurrentlyActive() && discount.getProduct() != null) {
            BigDecimal discountedPrice = calculateDiscountedPrice(
                    discount.getProduct().getPrice(),
                    discount.getDiscountPercent()
            );
            dto.setDiscountedPrice(discountedPrice);
        }
        
        return dto;
    }

    /**
     * Calculate the price after applying discount
     * Example: price=100, discountPercent=20 -> returns 80.00
     */
    private BigDecimal calculateDiscountedPrice(BigDecimal originalPrice, BigDecimal discountPercent) {
        if (originalPrice == null || discountPercent == null) {
            return originalPrice;
        }
        
        // Calculate discount amount
        BigDecimal discountAmount = originalPrice.multiply(discountPercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        
        // Subtract discount from original price
        return originalPrice.subtract(discountAmount).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Check if a product has any active discount
     */
    private boolean hasActiveDiscount(Product product) {
        if (product.getDiscounts() == null || product.getDiscounts().isEmpty()) {
            return false;
        }
        
        return product.getDiscounts().stream()
                .anyMatch(Discount::isDiscountCurrentlyActive);
    }
}