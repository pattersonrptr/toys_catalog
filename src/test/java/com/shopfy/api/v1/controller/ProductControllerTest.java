package com.shopfy.api.v1.controller;

import com.shopfy.api.v1.dto.CategoryResponse;
import com.shopfy.api.v1.dto.ProductResponse;
import com.shopfy.application.product.ProductService;
import com.shopfy.infrastructure.security.JwtService;
import com.shopfy.infrastructure.security.ShopfyUserDetailsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        value = ProductController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class
        }
)
class ProductControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean ProductService productService;
    // Necessário porque SecurityConfig (carregado pelo scan) depende destes beans
    @MockitoBean JwtService jwtService;
    @MockitoBean ShopfyUserDetailsService userDetailsService;

    @Test
    @DisplayName("GET /api/v1/products should return 200 with product list")
    void getProducts_returns200() throws Exception {
        var category = new CategoryResponse(1L, "Action Figure", null, true);
        var product = new ProductResponse(
                1L, "Dohko de Libra", "Descrição", "TOEY", "img.jpg",
                new BigDecimal("195.98"), 10, true, true, true,
                category, LocalDateTime.now(), null
        );
        when(productService.findAll(any())).thenReturn(new PageImpl<>(List.of(product), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Dohko de Libra"))
                .andExpect(jsonPath("$.content[0].price").value(195.98));
    }
}
