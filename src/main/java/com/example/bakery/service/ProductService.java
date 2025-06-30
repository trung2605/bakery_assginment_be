// src/main/java/com/example/bakery/service/ProductService.java
package com.example.bakery.service;

import com.example.bakery.model.Product;
import com.example.bakery.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page; // Import Page
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Các phương thức cũ (không phân trang) có thể giữ lại hoặc thay thế bằng phiên bản phân trang.
    // Để đơn giản, chúng ta sẽ chỉ sửa đổi các phương thức chính để hỗ trợ phân trang.
    // Nếu bạn muốn cả hai, hãy tạo overload.

    /**
     * Lấy tất cả sản phẩm có phân trang và sắp xếp.
     * @param pageable Đối tượng Pageable chứa thông tin phân trang và sắp xếp.
     * @return Trang chứa danh sách sản phẩm.
     */
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    /**
     * Lấy sản phẩm theo ID.
     * @param productId ID của sản phẩm.
     * @return Optional chứa sản phẩm nếu tìm thấy, rỗng nếu không.
     */
    public Optional<Product> getProductById(String productId) {
        return productRepository.findById(productId);
    }

    /**
     * Tạo ID sản phẩm tự động dựa trên định dạng PRODxxxx.
     * @return ID sản phẩm mới.
     */
    private String generateNextProductId() {
        // ... (giữ nguyên logic này) ...
        Optional<Product> lastProduct = productRepository.findAll(Sort.by(Sort.Direction.DESC, "productId"))
                .stream()
                .findFirst();

        if (lastProduct.isPresent()) {
            String lastId = lastProduct.get().getProductId();
            try {
                int num = Integer.parseInt(lastId.substring(4));
                return String.format("PROD%04d", num + 1);
            } catch (NumberFormatException e) {
                System.err.println("Product ID format invalid for auto-generation: " + lastId);
                return "PROD0001";
            }
        } else {
            return "PROD0001";
        }
    }

    /**
     * Lưu một sản phẩm mới hoặc cập nhật một sản phẩm hiện có.
     * @param product Đối tượng sản phẩm cần lưu.
     * @return Sản phẩm đã được lưu.
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ.
     */
    @Transactional
    public Product saveProduct(Product product) {
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty.");
        }
        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Product price must be a non-negative value.");
        }
        if (product.getCategory() == null || product.getCategory().trim().isEmpty()) {
            throw new IllegalArgumentException("Product category cannot be empty.");
        }
        if (product.getStockQuantity() == null || product.getStockQuantity() < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative.");
        }

        return productRepository.save(product);
    }

    /**
     * Xóa một sản phẩm theo ID.
     * @param productId ID của sản phẩm cần xóa.
     * @throws IllegalArgumentException nếu sản phẩm không tồn tại.
     */
    @Transactional
    public void deleteProduct(String productId) {
        if (!productRepository.existsById(productId)) {
            throw new IllegalArgumentException("Product not found with ID: " + productId);
        }
        productRepository.deleteById(productId);
    }

    /**
     * Cập nhật số lượng tồn kho của một sản phẩm.
     * @param productId ID của sản phẩm.
     * @param newStock Số lượng tồn kho mới.
     * @return Optional chứa sản phẩm đã cập nhật nếu tìm thấy, rỗng nếu không.
     * @throws IllegalArgumentException nếu số lượng tồn kho mới là âm.
     */
    @Transactional
    public Optional<Product> updateProductStock(String productId, Integer newStock) {
        if (newStock == null || newStock < 0) {
            throw new IllegalArgumentException("New stock quantity cannot be null or negative.");
        }

        return productRepository.findById(productId).map(product -> {
            product.setStockQuantity(newStock);
            return productRepository.save(product);
        });
    }

    /**
     * Lấy danh sách sản phẩm theo category có phân trang và sắp xếp.
     * @param category Tên category.
     * @param pageable Đối tượng Pageable.
     * @return Trang chứa danh sách sản phẩm.
     */
    public Page<Product> getProductsByCategory(String category, Pageable pageable) {
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty.");
        }
        return productRepository.findByCategory(category, pageable);
    }

    /**
     * Lấy danh sách sản phẩm có giá lớn hơn hoặc bằng một mức giá nhất định, có phân trang và sắp xếp.
     * @param minPrice Mức giá tối thiểu.
     * @param pageable Đối tượng Pageable.
     * @return Trang chứa danh sách sản phẩm.
     */
    public Page<Product> getProductsByPriceGreaterThanEqual(BigDecimal minPrice, Pageable pageable) {
        if (minPrice == null || minPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Minimum price must be a non-negative value.");
        }
        return productRepository.findByPriceGreaterThanEqual(minPrice, pageable);
    }

    /**
     * Lấy danh sách sản phẩm theo khoảng giá, có phân trang và sắp xếp.
     * @param minPrice Giá tối thiểu.
     * @param maxPrice Giá tối đa (có thể null).
     * @param pageable Đối tượng Pageable.
     * @return Trang chứa danh sách sản phẩm.
     */
    public Page<Product> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        if (minPrice == null || minPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Minimum price must be a non-negative value.");
        }
        if (maxPrice != null && maxPrice.compareTo(minPrice) < 0) {
            throw new IllegalArgumentException("Maximum price cannot be less than minimum price.");
        }

        if (maxPrice == null) {
            // Nếu maxPrice là null, nghĩa là "lớn hơn hoặc bằng minPrice"
            return productRepository.findByPriceGreaterThanEqual(minPrice, pageable);
        } else {
            return productRepository.findByPriceBetween(minPrice, maxPrice, pageable);
        }
    }

}