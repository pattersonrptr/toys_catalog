package com.toyscatalog.application.product;

import com.toyscatalog.api.v1.dto.ProductRequest;
import com.toyscatalog.api.v1.dto.ProductResponse;
import com.toyscatalog.api.v1.exception.ResourceNotFoundException;
import com.toyscatalog.domain.category.CategoryRepository;
import com.toyscatalog.domain.product.Product;
import com.toyscatalog.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public Page<ProductResponse> findAll(Pageable pageable) {
        return productRepository.findByActiveTrue(pageable)
                .map(ProductResponse::from);
    }

    public Page<ProductResponse> search(String search, Long categoryId,
                                        BigDecimal minPrice, BigDecimal maxPrice,
                                        Pageable pageable) {
        return productRepository.search(search, categoryId, minPrice, maxPrice, pageable)
                .map(ProductResponse::from);
    }

    public Page<ProductResponse> findByCategory(Long categoryId, Pageable pageable) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category", categoryId);
        }
        return productRepository.findByCategoryIdAndActiveTrue(categoryId, pageable)
                .map(ProductResponse::from);
    }

    public List<ProductResponse> findFeatured() {
        return productRepository.findByFeaturedTrueAndActiveTrue().stream()
                .map(ProductResponse::from)
                .toList();
    }

    public ProductResponse findById(Long id) {
        return productRepository.findById(id)
                .filter(Product::isActive)
                .map(ProductResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        var category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.categoryId()));

        var product = Product.builder()
                .name(request.name())
                .description(request.description())
                .brand(request.brand())
                .imageUrl(request.imageUrl())
                .price(request.price())
                .stockQuantity(request.stockQuantity())
                .featured(request.featured())
                .category(category)
                .build();

        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        var product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        var category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.categoryId()));

        product.setName(request.name());
        product.setDescription(request.description());
        product.setBrand(request.brand());
        product.setImageUrl(request.imageUrl());
        product.setPrice(request.price());
        product.setStockQuantity(request.stockQuantity());
        product.setFeatured(request.featured());
        product.setCategory(category);

        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional
    public void delete(Long id) {
        var product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        product.setActive(false);
        productRepository.save(product);
    }
}
