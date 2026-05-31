package com.pranta.ecommerce.Service;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

import com.pranta.ecommerce.Dto.ProductDiscountRequestDto;
import com.pranta.ecommerce.Dto.ProductRequestDto;
import com.pranta.ecommerce.Dto.ProductResponseDto;
import com.pranta.ecommerce.Dto.ProductStockResponseDto;
import com.pranta.ecommerce.Entity.Brand;
import com.pranta.ecommerce.Entity.Category;
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

        Category category = categoryRepository.findById(dto.getCategory_id())
                    .orElseThrow(()-> new ResourceNotFoundException("Category not found"));

        Brand brand = brandRepository.findById(dto.getBrand_id())
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

    public ProductResponseDto getProductByName(String name){
        Product product = productRepository.findByName(name)
                    .orElseThrow(() -> new ResourceNotFoundException("Product is not found with this name"));
        
        return mapToResponse(product);
    }

    public List<ProductResponseDto> getProductByPriceRange(double minPrice, double maxPrice){
        List<Product> products = productRepository.findByPriceBetween(minPrice, maxPrice);

        if (products.isEmpty()) {
            return Collections.emptyList();
        }
        return products.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<ProductResponseDto> getProductByCategory(Category category){

        return productRepository.findByCategory(category)
                    .stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
    }

    public List<ProductResponseDto> getProductByBrand(Brand brand){

        return productRepository.findByBrand(brand)
                    .stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
    }

    public List<ProductResponseDto> getProductsByColor(String color){

        List<Product> products = productRepository.findByColor(color);
                    
        if (products.isEmpty()) {
            return Collections.emptyList();
        }
        return products.stream()
                .map(this::mapToResponse)
                .toList();


    }

    public List<ProductResponseDto> getOutOfStockProducts(){
        List<Product> products = productRepository.findByStock(0);

        if (products.isEmpty()) {
            return Collections.emptyList();
        }
        return products.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ProductStockResponseDto getStockByProduct(Long productId){
        Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product Not found with this ID"));

        
        return new ProductStockResponseDto(
            product.getId(),
            product.getName(),
            product.getStock()
        );
    }

    @Transactional
    public List<ProductResponseDto> getStockLimitResponse(int threshold){
        List<Product> products = productRepository.findByStockLessThanEqual(threshold);

        return products.stream()
                    .map(this::mapToResponse)
                    .toList();
    }

    @Transactional
    public ProductResponseDto updateProduct(Long id, ProductRequestDto dto) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product is not found with this id"));

        Category category = categoryRepository.findById(dto.getCategory_id())
                        .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Brand brand = brandRepository.findById(dto.getBrand_id())
                        .orElseThrow(() -> new ResourceNotFoundException("Brand not found"));

        
        String productName = dto.getName().trim();

        if (productRepository.findByName(productName).isPresent()) {
            throw new DuplicateResourceException("Product with this name already exists");
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
    public ProductResponseDto updateStock(int quantity, Long productId){
        Product product = productRepository.findById(productId)
                    .orElseThrow(()-> new ResourceNotFoundException("Product is not found with this id"));
    
        product.setStock(quantity);
        Product saveProduct = productRepository.save(product);

        return mapToResponse(saveProduct);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }


    @Transactional
    public ProductResponseDto applyDiscount(Long productId, ProductDiscountRequestDto dto) {
        Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with this id"));

        if(dto.getDiscountStartDate() != null && dto.getDiscountEndDate() != null){
           if (!dto.getDiscountStartDate().isBefore(dto.getDiscountEndDate())) {
                throw new IllegalArgumentException("Discount start date must be before end date");
           }
           if (dto.getDiscountEndDate().isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("End date must be in the future");
           }
        }

        product.setDiscountPercent(dto.getDiscountPercent());
        product.setIsDiscounted(dto.getIsDiscounted());
        product.setDiscountStartDate(dto.getDiscountStartDate());
        product.setDiscountEndDate(dto.getDiscountEndDate());

        return mapToResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponseDto removeDiscount(Long productId) {
        Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with this id"));

        product.setDiscountPercent(BigDecimal.ZERO);
        product.setIsDiscounted(false);
        product.setDiscountStartDate(null);
        product.setDiscountEndDate(null);

        return mapToResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponseDto toggleDiscount(Long productId, boolean isDiscounted) {
        Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with this id"));

        product.setIsDiscounted(isDiscounted);
        return mapToResponse(productRepository.save(product));
    }

    private ProductResponseDto mapToResponse(Product product) {
        BigDecimal orginalPrice = product.getPrice();
        BigDecimal discountedPrice = product.getDiscountedPrice();
        BigDecimal savedAmount = orginalPrice.subtract(discountedPrice);
        return new ProductResponseDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getImageUrl(),
                product.getStock(),
                product.getColor(),
                product.isAvailable(),
                product.getCategory(),
                product.getBrand(),
                product.getDiscountPercent(),
                product.getIsDiscounted(),
                product.isDiscountCurrentlyActive(),
                discountedPrice,
                savedAmount,
                product.getDiscountStartDate(),
                product.getDiscountEndDate()
        );
    }
}

