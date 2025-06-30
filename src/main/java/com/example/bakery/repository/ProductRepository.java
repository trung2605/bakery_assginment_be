// src/main/java/com/example/bakery/repository/ProductRepository.java
package com.example.bakery.repository;

import com.example.bakery.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page; // Import Page

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {

    // Các phương thức cũ giữ nguyên
    List<Product> findByCategory(String category);
    List<Product> findByPriceGreaterThanEqual(BigDecimal price);
    List<Product> findByCategory(String category, Sort sort);
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    // MỚI: Các phương thức hỗ trợ phân trang
    // findAll đã có sẵn trong JpaRepository trả về Page<Product>
    Page<Product> findByCategory(String category, Pageable pageable);
    Page<Product> findByPriceGreaterThanEqual(BigDecimal price, Pageable pageable);
    Page<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

     Page<Product> findByCategoryAndPriceBetween(String category, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
}