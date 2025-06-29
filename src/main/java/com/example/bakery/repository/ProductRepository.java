// Trong ProductRepository.java
package com.example.bakery.repository;

import com.example.bakery.model.Product;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    Optional<Product> findByName(String name);
    List<Product> findByCategory(String category);
    List<Product> findByPriceGreaterThanEqual(BigDecimal price);
    List<Product> findByStockQuantityLessThanEqual(Integer stockQuantity);
    List<Product> findByCategory(String category, Sort sort);

    // SỬA ĐỔI Ở ĐÂY:
    // Cách 1A: Sử dụng Equals tường minh
    List<Product> findByStockQuantityEquals(Integer stockQuantity);
    // Hoặc Cách 1B: Đơn giản là bỏ từ "Equals" đi, Spring Data JPA ngầm định nó là Equals nếu không có toán tử khác
    // List<Product> findByStockQuantity(Integer stockQuantity); // Cũng hoạt động tốt

    // --- Các phương thức truy vấn tùy chỉnh khác (giữ nguyên) ---
    // @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    // List<Product> searchProducts(@Param("keyword") String keyword);
}