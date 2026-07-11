package com.pranta.ecommerce.Controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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

import com.pranta.ecommerce.Dto.Request.ProductRequestDto;
import com.pranta.ecommerce.Dto.Response.PageResponseDto;
import com.pranta.ecommerce.Dto.Response.ProductResponseDto;
import com.pranta.ecommerce.Dto.Response.ProductStockResponseDto;
import com.pranta.ecommerce.Entity.Brand;
import com.pranta.ecommerce.Entity.Category;
import com.pranta.ecommerce.Repository.ProductRepository;
import com.pranta.ecommerce.Service.ProductService;
import com.pranta.ecommerce.Util.PaginationHelper;

import io.swagger.v3.oas.annotations.Operation;
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

    private final PaginationHelper paginationHelper;

    @Operation(
        summary = "Create Product",
        description = "Creates a new product"
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ProductResponseDto> createProduct(
            @Valid @RequestBody ProductRequestDto dto) {
        return new ResponseEntity<>(
                productService.createProduct(dto),
                HttpStatus.CREATED
        );
    }

    @Operation(
        summary = "Get all Product",
        description = "Anyone can get all product"
    )
    @GetMapping
    public ResponseEntity<PageResponseDto<ProductResponseDto>> getAllProducts(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "asc") String sortDir

    ) {

        Pageable pageable = paginationHelper.buildPageable(page,size,sortBy,sortDir);
        Page<ProductResponseDto> products = productService.getAllProducts(pageable);

        return ResponseEntity.ok(PageResponseDto.from(products));
    }

    @Operation(
        summary = "Get Product by id",
        description = "Anyone can get product by id"
    )
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

     @Operation(
        summary = "Get Product by name",
        description = "Anyone can get product by name"
    )
    @GetMapping("/search")
    public ResponseEntity<ProductResponseDto> getProductByName(@RequestParam String name) {
        return ResponseEntity.ok(productService.getProductByName(name));
    }

     @Operation(
        summary = "Get Product by product price range",
        description = "Anyone can get product by price range"
    )
    @GetMapping("/price/search")
    public ResponseEntity<PageResponseDto<ProductResponseDto>> getProductByPriceRange(
            @RequestParam double minPrice,
            @RequestParam double maxPrice,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
                
                Pageable pageable = paginationHelper.buildPageable(page, size, sortBy, sortDir);

                Page<ProductResponseDto> products = productService.getProductByPriceRange(minPrice, maxPrice, pageable);

        return ResponseEntity.ok(PageResponseDto.from(products));
    }

     @Operation(
        summary = "Get Product by category search",
        description = "Anyone can get product by category search"
    )
    @GetMapping("/category/search")
    public ResponseEntity<PageResponseDto<ProductResponseDto>> getProductByCategory(
            @RequestParam Category category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
                Pageable pageable = paginationHelper.buildPageable(page, size, sortBy, sortDir);
                Page<ProductResponseDto> products = productService.getProductByCategory(category,pageable);
        return ResponseEntity.ok(PageResponseDto.from(products));
    }

     @Operation(
        summary = "Get Product by barnd",
        description = "Anyone can get product by brand"
    )
    @GetMapping("/brand/search")
    public ResponseEntity<PageResponseDto<ProductResponseDto>> getProductByBrand(
            @RequestParam Brand brand,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

                Pageable pageable = paginationHelper.buildPageable(page, size, sortBy, sortDir);

                Page<ProductResponseDto> products = productService.getProductByBrand(brand,pageable);
        return ResponseEntity.ok(PageResponseDto.from(products));
    }

     @Operation(
        summary = "Get Product by color",
        description = "Anyone can get product by color"
    )
    @GetMapping("/color/search")
    public ResponseEntity<PageResponseDto<ProductResponseDto>> getProductByColor(
            @RequestParam String color,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

                Pageable pageable = paginationHelper.buildPageable(page, size, sortBy, sortDir);

                Page<ProductResponseDto> products = productService.getProductsByColor(color,pageable);
        return ResponseEntity.ok(PageResponseDto.from(products));
    }

     @Operation(
        summary = "Get discounted Product",
        description = "Anyone can get discounted product"
    )
    @GetMapping("/discounted")
    public ResponseEntity<PageResponseDto<ProductResponseDto>> getDiscountedProducts(Pageable pageable) {
        Page<ProductResponseDto> products = productService.getDiscountedProducts(pageable);
        return ResponseEntity.ok(PageResponseDto.from(products));
    }

     @Operation(
        summary = "Get available Product",
        description = "Anyone can get available product"
    )
    @GetMapping("/available")
    public ResponseEntity<PageResponseDto<ProductResponseDto>> getAvailableProducts(
            @RequestParam(required = false, defaultValue = "false") boolean onSale,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Pageable pageable = paginationHelper.buildPageable(page, size, sortBy, sortDir);
        
       Page<ProductResponseDto> products =
            productService.getAllProducts(pageable);

    List<ProductResponseDto> filteredProducts =
            products.getContent()
                    .stream()
                    .filter(ProductResponseDto::isAvailable)
                    .filter(product -> !onSale || product.isHasActiveDiscount())
                    .toList();

    Page<ProductResponseDto> filteredPage =
            new PageImpl<>(
                    filteredProducts,
                    pageable,
                    filteredProducts.size()
            );
                
        
        return ResponseEntity.ok(PageResponseDto.from(filteredPage));
    }

     @Operation(
        summary = "Get stock out Product",
        description = "Admin can get stock out product"
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/stock-out")
    public ResponseEntity<?> getOutOfStockProduct(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Pageable pageable = paginationHelper.buildPageable(page, size, sortBy, sortDir);
        Page<ProductResponseDto> products = productService.getOutOfStockProducts(pageable);

        return ResponseEntity.ok(PageResponseDto.from(products));
    }

     @Operation(
        summary = "Get Product stock by id",
        description = "Admin can get product stock by id"
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{productId}/stock")
    public ResponseEntity<ProductStockResponseDto> getStockByProductId(
            @PathVariable Long productId) {
        return ResponseEntity.ok(productService.getStockByProduct(productId));
    }

    @Operation(
        summary = "Get low stock Product",
        description = "Admin can get low stock product"
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/low-stock")
    public ResponseEntity<PageResponseDto<ProductResponseDto>> getStockLimitResponse(
            @RequestParam(defaultValue = "5") int threshold,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Pageable pageable = paginationHelper.buildPageable(page, size, sortBy, sortDir);

        Page<ProductResponseDto> products = productService.getStockLimitResponse(threshold,pageable);

        return ResponseEntity.ok(PageResponseDto.from(products));
    }

    @Operation(
        summary = "Update Product",
        description = "Admin can update product"
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDto> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequestDto dto) {
        return ResponseEntity.ok(productService.updateProduct(id, dto));
    }

    @Operation(
        summary = "Update Product Stock",
        description = "Admin can update product Stock"
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/stock/{productId}")
    public ResponseEntity<ProductResponseDto> updateStockQuantity(
            @PathVariable Long productId,
            @RequestParam int quantity) {
        return ResponseEntity.ok(productService.updateStock(quantity, productId));
    }

    @Operation(
        summary = "Delete Product",
        description = "Admin can delete product Stock"
    )
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    
}