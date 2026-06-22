package com.pranta.ecommerce.Controller;

import com.pranta.ecommerce.Dto.Request.DiscountRequestDto;
import com.pranta.ecommerce.Dto.Response.DiscountResponseDto;
import com.pranta.ecommerce.Exceptions.InvalidRequestException;
import com.pranta.ecommerce.Exceptions.ResourceNotFoundException;
import com.pranta.ecommerce.Service.DiscountService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/discounts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") 
@Tag(
    name = "Discount",
    description = "Discount Management API's"
)
public class DiscountController {

    private final DiscountService discountService;


    @Operation(
        summary = "Create Discount",
        description = "Admin can set a discount for product"
    )
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DiscountResponseDto> createDiscount(
            @Valid @RequestBody DiscountRequestDto discountRequestDto) {
        try {
            DiscountResponseDto createdDiscount = discountService.createDiscount(discountRequestDto);
            return new ResponseEntity<>(createdDiscount, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            throw new InvalidRequestException(e.getMessage());
        }
    }

    @Operation(
        summary = "Update Discount",
        description = "Admin can update the discount for product"
    )
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DiscountResponseDto> updateDiscount(
            @PathVariable Long id,
            @Valid @RequestBody DiscountRequestDto discountRequestDto) {
        try {
            DiscountResponseDto updatedDiscount = discountService.updateDiscount(id, discountRequestDto);
            return ResponseEntity.ok(updatedDiscount);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                throw new ResourceNotFoundException(e.getMessage());
            }
            throw new InvalidRequestException(e.getMessage());
        }
    }
    @Operation(
        summary = "Get Discount By id",
        description = "Admin can get a discount by id"
    )
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DiscountResponseDto> getDiscountById(@PathVariable Long id) {
        try {
            DiscountResponseDto discount = discountService.getDiscountById(id);
            return ResponseEntity.ok(discount);
        } catch (RuntimeException e) {
            throw new ResourceNotFoundException("Discount not found with id: " + id);
        }
    }

    @Operation(
        summary = "Get All Discount",
        description = "Anyone can get discount for product"
    )
    @GetMapping
    public ResponseEntity<List<DiscountResponseDto>> getAllDiscounts() {
        List<DiscountResponseDto> discounts = discountService.getAllDiscounts();
        return ResponseEntity.ok(discounts);
    }

    @Operation(
        summary = "Get Discount by product",
        description = "Anyone can get discount by product"
    )
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<DiscountResponseDto>> getDiscountsByProduct(
            @PathVariable Long productId) {
        List<DiscountResponseDto> discounts = discountService.getDiscountsByProduct(productId);
        return ResponseEntity.ok(discounts);
    }

    @Operation(
        summary = "Delete Discount",
        description = "Admin can delete a discount for product"
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteDiscount(@PathVariable Long id) {
        try {
            discountService.deleteDiscount(id);
            return ResponseEntity.ok(Map.of(
                "message", "Discount deleted successfully",
                "id", id.toString()
            ));
        } catch (RuntimeException e) {
            throw new ResourceNotFoundException("Discount not found with id: " + id);
        }
    }

    @Operation(
        summary = "Toggle Discount",
        description = "Admin can toggle a discount for product"
    )
    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DiscountResponseDto> toggleDiscountStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {
        try {
            DiscountResponseDto updatedDiscount = discountService.toggleDiscountStatus(id, active);
            return ResponseEntity.ok(updatedDiscount);
        } catch (RuntimeException e) {
            throw new ResourceNotFoundException("Discount not found with id: " + id);
        }
    }

    @Operation(
        summary = "Activate Discount",
        description = "Admin can activate a discount for product"
    )
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DiscountResponseDto> activateDiscount(@PathVariable Long id) {
        try {
            DiscountResponseDto activatedDiscount = discountService.toggleDiscountStatus(id, true);
            return ResponseEntity.ok(activatedDiscount);
        } catch (RuntimeException e) {
            throw new ResourceNotFoundException("Discount not found with id: " + id);
        }
    }

    @Operation(
        summary = "Deactivate Discount",
        description = "Admin can deactivate a discount for product"
    )
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DiscountResponseDto> deactivateDiscount(@PathVariable Long id) {
        try {
            DiscountResponseDto deactivatedDiscount = discountService.toggleDiscountStatus(id, false);
            return ResponseEntity.ok(deactivatedDiscount);
        } catch (RuntimeException e) {
            throw new ResourceNotFoundException("Discount not found with id: " + id);
        }
    }

    @Operation(
        summary = "Get All Active Discount Product",
        description = "Admin can get all discount for product"
    )
    @GetMapping("/product/{productId}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DiscountResponseDto>> getActiveDiscountsForProduct(
            @PathVariable Long productId) {
        List<DiscountResponseDto> discounts = discountService.getDiscountsByProduct(productId);
        List<DiscountResponseDto> activeDiscounts = discounts.stream()
                .filter(DiscountResponseDto::getCurrentlyActive)
                .toList();
        return ResponseEntity.ok(activeDiscounts);
    }

    @Operation(
        summary = "Get All Active Discount",
        description = "Admin can get all active discount for product"
    )
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DiscountResponseDto>> getAllActiveDiscounts() {
        List<DiscountResponseDto> activeDiscounts = discountService.getAllDiscounts()
                .stream()
                .filter(DiscountResponseDto::getCurrentlyActive)
                .toList();
        return ResponseEntity.ok(activeDiscounts);
    }
}