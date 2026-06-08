package com.pranta.ecommerce.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pranta.ecommerce.Dto.ProductRequestDto;
import com.pranta.ecommerce.Dto.ProductResponseDto;
import com.pranta.ecommerce.Dto.ProductStockResponseDto;
import com.pranta.ecommerce.Entity.Brand;
import com.pranta.ecommerce.Entity.Category;
import com.pranta.ecommerce.Service.ProductService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/products")
@Tag(
    name = "Products",
    description = "Products Management API's"
)
public class ProductController {

    private final ProductService productService;


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ProductResponseDto> createProduct(
            @Valid @RequestBody ProductRequestDto dto) {
        return new ResponseEntity<>(
                productService.createProduct(dto),
                HttpStatus.CREATED
        );
    }

    @GetMapping
    public ResponseEntity<List<ProductResponseDto>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<ProductResponseDto> getProductByName(@RequestParam String name) {
        return ResponseEntity.ok(productService.getProductByName(name));
    }

    @GetMapping("/price/search")
    public ResponseEntity<List<ProductResponseDto>> getProductByPriceRange(
            @RequestParam double minPrice,
            @RequestParam double maxPrice) {
        return ResponseEntity.ok(productService.getProductByPriceRange(minPrice, maxPrice));
    }

    @GetMapping("/category/search")
    public ResponseEntity<List<ProductResponseDto>> getProductByCategory(
            @RequestParam Category category) {
        return ResponseEntity.ok(productService.getProductByCategory(category));
    }

    @GetMapping("/brand/search")
    public ResponseEntity<List<ProductResponseDto>> getProductByBrand(
            @RequestParam Brand brand) {
        return ResponseEntity.ok(productService.getProductByBrand(brand));
    }

    @GetMapping("/color/search")
    public ResponseEntity<List<ProductResponseDto>> getProductByColor(
            @RequestParam String color) {
        return ResponseEntity.ok(productService.getProductsByColor(color));
    }

    @GetMapping("/discounted")
    public ResponseEntity<List<ProductResponseDto>> getDiscountedProducts() {
        List<ProductResponseDto> products = productService.getDiscountedProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/available")
    public ResponseEntity<List<ProductResponseDto>> getAvailableProducts(
            @RequestParam(required = false, defaultValue = "false") boolean onSale) {
        
        List<ProductResponseDto> products = productService.getAllProducts()
                .stream()
                .filter(ProductResponseDto::isAvailable)
                .filter(product -> !onSale || product.isHasActiveDiscount())
                .toList();
        
        return ResponseEntity.ok(products);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/stock-out")
    public ResponseEntity<?> getOutOfStockProduct() {
        List<ProductResponseDto> products = productService.getOutOfStockProducts();

        if (products.isEmpty()) {
            return ResponseEntity.ok("No out of stock products");
        }
        return ResponseEntity.ok(products);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{productId}/stock")
    public ResponseEntity<ProductStockResponseDto> getStockByProductId(
            @PathVariable Long productId) {
        return ResponseEntity.ok(productService.getStockByProduct(productId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/low-stock")
    public ResponseEntity<List<ProductResponseDto>> getStockLimitResponse(
            @RequestParam(defaultValue = "5") int threshold) {
        return ResponseEntity.ok(productService.getStockLimitResponse(threshold));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDto> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequestDto dto) {
        return ResponseEntity.ok(productService.updateProduct(id, dto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/stock/{productId}")
    public ResponseEntity<ProductResponseDto> updateStockQuantity(
            @PathVariable Long productId,
            @RequestParam int quantity) {
        return ResponseEntity.ok(productService.updateStock(quantity, productId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}