package com.pranta.ecommerce.Service;


import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

import com.pranta.ecommerce.Dto.ProductRequestDto;
import com.pranta.ecommerce.Dto.ProductResponseDto;
import com.pranta.ecommerce.Entity.Category;
import com.pranta.ecommerce.Entity.Product;
import com.pranta.ecommerce.Exceptions.DuplicateResourceException;
import com.pranta.ecommerce.Exceptions.ResourceNotFoundException;
import com.pranta.ecommerce.Repository.CategoryRepository;
import com.pranta.ecommerce.Repository.ProductRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;

    private final CategoryRepository categoryRepository;

    public ProductResponseDto createProduct(ProductRequestDto dto) {

        Category category = categoryRepository.findById(dto.getCategory_id())
                    .orElseThrow(()-> new RuntimeException("Category not found"));

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

    public List<ProductResponseDto> getProductByCategory(Category category){

        return productRepository.findByCategory(category)
                    .stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
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

    @Transactional
    public ProductResponseDto updateProduct(Long id, ProductRequestDto dto) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product is not found with this id"));

        Category category = categoryRepository.findById(dto.getCategory_id())
                        .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        
        String productName = dto.getName().trim();

        if (productRepository.findByName(productName).isPresent()) {
            throw new DuplicateResourceException("Product with this name already exists");
        }

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setImageUrl(dto.getImageUrl());
        product.setCategory(category);

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

    private ProductResponseDto mapToResponse(Product product) {
        return new ProductResponseDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getImageUrl(),
                product.getStock(),
                product.getColor(),
                product.isAvailable(),
                product.getCategory()
        );
    }
}

