// Trong file: src/main/java/com/example/bakery/service/ProductService.java
package com.example.bakery.service; // Đã cập nhật package path

import com.example.bakery.model.Product; // Import lớp Product Entity
import com.example.bakery.repository.ProductRepository; // Import ProductRepository
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Lấy tất cả sản phẩm.
     * @return Danh sách tất cả sản phẩm.
     */
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * Lấy thông tin sản phẩm theo ID.
     * @param productId ID của sản phẩm.
     * @return Optional chứa đối tượng Product nếu tìm thấy, hoặc Optional.empty().
     */
    public Optional<Product> getProductById(String productId) {
        return productRepository.findById(productId);
    }

    /**
     * Lưu (thêm mới hoặc cập nhật) một sản phẩm.
     * Bao gồm logic kiểm tra định dạng productId và stockQuantity.
     * @param product Đối tượng Product cần lưu.
     * @return Đối tượng Product đã được lưu.
     * @throws IllegalArgumentException nếu productId hoặc stockQuantity không hợp lệ.
     */
    public Product saveProduct(Product product) {
        // Kiểm tra định dạng productId
        if (!product.getProductId().matches("PRD....")) {
            throw new IllegalArgumentException("Product ID phải có định dạng 'PRD____' (ví dụ: PRD0001).");
        }

        // Kiểm tra stockQuantity không âm
        if (product.getStockQuantity() < 0) {
            throw new IllegalArgumentException("Số lượng tồn kho không thể âm.");
        }

        // Nếu là sản phẩm mới, kiểm tra trùng lặp ID (mặc dù PRIMARY KEY đã xử lý)
        if (productRepository.existsById(product.getProductId()) && productRepository.findById(product.getProductId()).isEmpty()) {
            // Logic này có thể phức tạp hơn nếu bạn muốn update và đảm bảo không có ID trùng
            // JpaRepository.save() sẽ tự động update nếu ID đã tồn tại, thêm mới nếu chưa.
        }

        return productRepository.save(product);
    }

    /**
     * Cập nhật số lượng tồn kho của một sản phẩm.
     * @param productId ID của sản phẩm.
     * @param newStockQuantity Số lượng tồn kho mới.
     * @return Optional chứa Product đã cập nhật nếu thành công, Optional.empty() nếu không tìm thấy sản phẩm.
     * @throws IllegalArgumentException nếu số lượng tồn kho mới là âm.
     */
    public Optional<Product> updateProductStock(String productId, Integer newStockQuantity) {
        if (newStockQuantity < 0) {
            throw new IllegalArgumentException("Số lượng tồn kho không thể âm.");
        }
        return productRepository.findById(productId).map(product -> {
            product.setStockQuantity(newStockQuantity);
            return productRepository.save(product);
        });
    }

    /**
     * Lấy danh sách sản phẩm theo danh mục.
     * @param category Tên danh mục.
     * @return Danh sách sản phẩm trong danh mục đó.
     */
    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    /**
     * Lấy danh sách sản phẩm có giá lớn hơn hoặc bằng.
     * @param price Giá tối thiểu.
     * @return Danh sách sản phẩm.
     */
    public List<Product> getProductsByPriceGreaterThanEqual(BigDecimal price) {
        return productRepository.findByPriceGreaterThanEqual(price);
    }

    /**
     * Xóa một sản phẩm theo ID.
     * @param productId ID của sản phẩm cần xóa.
     */
    public void deleteProduct(String productId) {
        if (!productRepository.existsById(productId)) {
            throw new IllegalArgumentException("Sản phẩm với ID '" + productId + "' không tồn tại.");
        }
        productRepository.deleteById(productId);
    }
}