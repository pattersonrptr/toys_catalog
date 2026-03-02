package com.toyscatalog.api.v1.controller;

import com.toyscatalog.api.v1.dto.ProductRequest;
import com.toyscatalog.api.v1.dto.ProductResponse;
import com.toyscatalog.application.product.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product catalog management")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "List all active products (paginated)")
    public Page<ProductResponse> findAll(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return productService.findAll(pageable);
    }

    @GetMapping("/search")
    @Operation(summary = "Search products with filters")
    public Page<ProductResponse> search(
            @Parameter(description = "Text search on name, brand or description")
            @RequestParam(required = false) String q,
            @Parameter(description = "Filter by category ID")
            @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Minimum price")
            @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Maximum price")
            @RequestParam(required = false) BigDecimal maxPrice,
            @PageableDefault(size = 20) Pageable pageable) {
        return productService.search(q, categoryId, minPrice, maxPrice, pageable);
    }

    @GetMapping("/featured")
    @Operation(summary = "List featured products")
    public List<ProductResponse> featured() {
        return productService.findFeatured();
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "List products by category")
    @ApiResponse(responseCode = "404", description = "Category not found")
    public Page<ProductResponse> findByCategory(
            @PathVariable Long categoryId,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return productService.findByCategory(categoryId, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a product by ID")
    @ApiResponse(responseCode = "404", description = "Product not found")
    public ProductResponse findById(@PathVariable Long id) {
        return productService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new product")
    public ProductResponse create(@RequestBody @Valid ProductRequest request) {
        return productService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a product")
    public ProductResponse update(@PathVariable Long id,
                                  @RequestBody @Valid ProductRequest request) {
        return productService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete a product")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
