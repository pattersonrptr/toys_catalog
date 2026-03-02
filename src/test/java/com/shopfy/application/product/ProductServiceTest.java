package com.shopfy.application.product;

import com.shopfy.api.v1.dto.ProductRequest;
import com.shopfy.api.v1.exception.ResourceNotFoundException;
import com.shopfy.domain.category.Category;
import com.shopfy.domain.category.CategoryRepository;
import com.shopfy.domain.product.Product;
import com.shopfy.domain.product.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock ProductRepository productRepository;
    @Mock CategoryRepository categoryRepository;
    @InjectMocks ProductService productService;

    Category category;
    Product product;

    @BeforeEach
    void setUp() {
        category = Category.builder().id(1L).name("Action Figure").active(true).build();

        product = Product.builder()
                .id(1L)
                .name("Dohko de Libra")
                .brand("TOEY")
                .price(new BigDecimal("195.98"))
                .stockQuantity(10)
                .active(true)
                .category(category)
                .build();
    }

    @Test
    @DisplayName("findAll should return paginated active products")
    void findAll_returnsActivePage() {
        var pageable = PageRequest.of(0, 10);
        when(productRepository.findByActiveTrue(pageable))
                .thenReturn(new PageImpl<>(List.of(product)));

        var result = productService.findAll(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().name()).isEqualTo("Dohko de Libra");
    }

    @Test
    @DisplayName("findById should throw when product not found")
    void findById_throwsWhenNotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("create should persist product with correct data")
    void create_persistsProduct() {
        var request = new ProductRequest(
                "Dohko de Libra", "Descrição", "TOEY", "img.jpg",
                new BigDecimal("195.98"), 10, true, 1L
        );
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any())).thenReturn(product);

        var result = productService.create(request);

        assertThat(result.name()).isEqualTo("Dohko de Libra");
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("delete should deactivate product (soft delete)")
    void delete_softDeletesProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any())).thenReturn(product);

        productService.delete(1L);

        assertThat(product.isActive()).isFalse();
        verify(productRepository).save(product);
    }
}
