package com.pranta.ecommerce.Service.Impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.pranta.ecommerce.Dto.Request.ProductRequestDto;
import com.pranta.ecommerce.Dto.Response.DiscountResponseDto;
import com.pranta.ecommerce.Dto.Response.ProductResponseDto;
import com.pranta.ecommerce.Dto.Response.ProductStockResponseDto;
import com.pranta.ecommerce.Entity.Brand;
import com.pranta.ecommerce.Entity.Category;
import com.pranta.ecommerce.Entity.Discount;
import com.pranta.ecommerce.Entity.Product;
import com.pranta.ecommerce.Exceptions.DuplicateResourceException;
import com.pranta.ecommerce.Exceptions.ResourceNotFoundException;
import com.pranta.ecommerce.Repository.BrandRepository;
import com.pranta.ecommerce.Repository.CategoryRepository;
import com.pranta.ecommerce.Repository.ProductRepository;
import com.pranta.ecommerce.Service.ProductService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;

    @Override
    public ProductResponseDto createProduct(ProductRequestDto dto) {
        log.info("Creating new product: {}", dto.getName());

        Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + dto.getCategoryId()));

        Brand brand = brandRepository.findById(dto.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + dto.getBrandId()));

        String productName = dto.getName().trim();

        if (productRepository.findByName(productName).isPresent()) {
            throw new DuplicateResourceException("Product with name '" + productName + "' already exists");
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
        log.info("Product created successfully with id: {}", savedProduct.getId());

        return mapToResponse(savedProduct);
    }

    @Override
    public List<ProductResponseDto> getAllProducts() {
        log.debug("Fetching all products");
        List<Product> products = productRepository.findAll();
        
        if (products.isEmpty()) {
            log.warn("No products found in the database");
            return Collections.emptyList();
        }
        
        return products.stream()
                        .map(this::mapToResponse)
                        .collect(Collectors.toList());
    }

    @Override
    public ProductResponseDto getProductById(Long id) {
        log.debug("Fetching product by id: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        return mapToResponse(product);
    }

    @Override
    public ProductResponseDto getProductByName(String name) {
        log.debug("Fetching product by name: {}", name);
        Product product = productRepository.findByName(name)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with name: " + name));
        
        return mapToResponse(product);
    }

    @Override
    public List<ProductResponseDto> getProductByPriceRange(double minPrice, double maxPrice) {
        log.debug("Fetching products by price range: {} - {}", minPrice, maxPrice);
        List<Product> products = productRepository.findByPriceBetween(minPrice, maxPrice);

        if (products.isEmpty()) {
            log.warn("No products found in price range: {} - {}", minPrice, maxPrice);
            return Collections.emptyList();
        }
        return products.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<ProductResponseDto> getProductByCategory(Category category) {
        log.debug("Fetching products by category: {}", category.getName());
        List<Product> products = productRepository.findByCategory(category);
        
        if (products.isEmpty()) {
            log.warn("No products found for category: {}", category.getName());
            return Collections.emptyList();
        }
        
        return products.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponseDto> getProductByBrand(Brand brand) {
        log.debug("Fetching products by brand: {}", brand.getName());
        List<Product> products = productRepository.findByBrand(brand);
        
        if (products.isEmpty()) {
            log.warn("No products found for brand: {}", brand.getName());
            return Collections.emptyList();
        }
        
        return products.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponseDto> getProductsByColor(String color) {
        log.debug("Fetching products by color: {}", color);
        List<Product> products = productRepository.findByColor(color);
                    
        if (products.isEmpty()) {
            log.warn("No products found with color: {}", color);
            return Collections.emptyList();
        }
        return products.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<ProductResponseDto> getOutOfStockProducts() {
        log.debug("Fetching out of stock products");
        List<Product> products = productRepository.findByStock(0);

        if (products.isEmpty()) {
            log.warn("No out of stock products found");
            return Collections.emptyList();
        }
        return products.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<ProductResponseDto> getDiscountedProducts() {
        log.debug("Fetching products with active discounts");
        List<Product> products = productRepository.findAll();
        
        List<ProductResponseDto> discountedProducts = products.stream()
                .filter(this::hasActiveDiscount)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        
        if (discountedProducts.isEmpty()) {
            log.warn("No products with active discounts found");
        }
        
        return discountedProducts;
    }

    @Override
    public ProductStockResponseDto getStockByProduct(Long productId) {
        log.debug("Fetching stock for product id: {}", productId);
        Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        return new ProductStockResponseDto(
            product.getId(),
            product.getName(),
            product.getStock()
        );
    }

    @Override
    @Transactional
    public List<ProductResponseDto> getStockLimitResponse(int threshold) {
        log.debug("Fetching products with stock less than or equal to: {}", threshold);
        List<Product> products = productRepository.findByStockLessThanEqual(threshold);

        if (products.isEmpty()) {
            log.warn("No products found with stock <= {}", threshold);
            return Collections.emptyList();
        }

        return products.stream()
                    .map(this::mapToResponse)
                    .toList();
    }

    @Override
    @Transactional
    public ProductResponseDto updateProduct(Long id, ProductRequestDto dto) {
        log.info("Updating product with id: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        Category category = categoryRepository.findById(dto.getCategoryId())
                        .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + dto.getCategoryId()));

        Brand brand = brandRepository.findById(dto.getBrandId())
                        .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + dto.getBrandId()));

        // Check for duplicate name only if the name is being changed
        if (!product.getName().equals(dto.getName().trim())) {
            String productName = dto.getName().trim();
            if (productRepository.findByName(productName).isPresent()) {
                throw new DuplicateResourceException("Product with name '" + productName + "' already exists");
            }
        }

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setImageUrl(dto.getImageUrl());
        product.setCategory(category);
        product.setColor(dto.getColor());
        product.setBrand(brand);

        Product updatedProduct = productRepository.save(product);
        log.info("Product updated successfully with id: {}", updatedProduct.getId());

        return mapToResponse(updatedProduct);
    }

    @Override
    @Transactional
    public ProductResponseDto updateStock(int quantity, Long productId) {
        log.info("Updating stock for product id: {} to quantity: {}", productId, quantity);
        
        Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
    
        product.setStock(quantity);
        Product savedProduct = productRepository.save(product);
        log.info("Stock updated successfully for product id: {}", productId);

        return mapToResponse(savedProduct);
    }

    @Override
    public void deleteProduct(Long id) {
        log.info("Deleting product with id: {}", id);
        
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
        log.info("Product deleted successfully with id: {}", id);
    }

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
        
        // Get only active discounts
        List<Discount> productDiscounts = product.getDiscounts();
        List<DiscountResponseDto> activeDiscountDtos = new java.util.ArrayList<>();
        
        if (productDiscounts != null && !productDiscounts.isEmpty()) {
            // Filter only active discounts
            List<Discount> activeDiscounts = productDiscounts.stream()
                    .filter(Discount::isDiscountCurrentlyActive)
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