package com.toyscatalog.api.v1.controller;

import com.toyscatalog.api.v1.dto.CategoryRequest;
import com.toyscatalog.api.v1.dto.CategoryResponse;
import com.toyscatalog.application.category.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Product category management")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "List all active categories")
    public List<CategoryResponse> findAll() {
        return categoryService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a category by ID")
    @ApiResponse(responseCode = "404", description = "Category not found")
    public CategoryResponse findById(@PathVariable Long id) {
        return categoryService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new category")
    public CategoryResponse create(@RequestBody @Valid CategoryRequest request) {
        return categoryService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a category")
    public CategoryResponse update(@PathVariable Long id,
                                   @RequestBody @Valid CategoryRequest request) {
        return categoryService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete a category")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
