package com.shopfy.domain.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByActiveTrue(Pageable pageable);

    Page<Product> findByCategoryIdAndActiveTrue(Long categoryId, Pageable pageable);

    List<Product> findByFeaturedTrueAndActiveTrue();

    Page<Product> findByStockQuantityGreaterThanAndActiveTrue(int minStock, Pageable pageable);

    @Query("""
            SELECT p FROM Product p
            WHERE p.active = true
            AND (:search IS NULL OR
                 LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR
                 LOWER(p.brand) LIKE LOWER(CONCAT('%', :search, '%')) OR
                 LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')))
            AND (:categoryId IS NULL OR p.category.id = :categoryId)
            AND (:minPrice IS NULL OR p.price >= :minPrice)
            AND (:maxPrice IS NULL OR p.price <= :maxPrice)
            """)
    Page<Product> search(
            @Param("search") String search,
            @Param("categoryId") Long categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );
}
