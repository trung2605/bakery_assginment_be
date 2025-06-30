// src/main/java/com/example/bakery/controller/ProductController.java
package com.example.bakery.controller;

import com.example.bakery.model.Product;
import com.example.bakery.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:5173", "http://localhost:5173"})
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Lấy tất cả sản phẩm có phân trang và sắp xếp.
     * GET /api/products?page=0&size=10&sortBy=productId&sortDirection=asc
     * @param page Số trang (mặc định 0).
     * @param size Kích thước trang (mặc định 10).
     * @param sortBy Thuộc tính để sắp xếp (mặc định "productId").
     * @param sortDirection Hướng sắp xếp (mặc định "asc").
     * @return Trang chứa danh sách sản phẩm.
     */
    @GetMapping
    public ResponseEntity<Page<Product>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "productId") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection.toUpperCase()), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> products = productService.getAllProducts(pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<Product> getProductById(@PathVariable String productId) {
        Optional<Product> product = productService.getProductById(productId);
        return product.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        try {
            Product createdProduct = productService.saveProduct(product);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
        } catch (IllegalArgumentException e) {
            // TODO: Trả về thông báo lỗi cụ thể hơn trong body của 400 Bad Request
            // Ví dụ: return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{productId}")
    public ResponseEntity<Product> updateProduct(@PathVariable String productId, @RequestBody Product productDetails) {
        Optional<Product> existingProductOptional = productService.getProductById(productId);
        if (existingProductOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Product existingProduct = existingProductOptional.get();
        existingProduct.setName(productDetails.getName());
        existingProduct.setDescription(productDetails.getDescription());
        existingProduct.setPrice(productDetails.getPrice());
        existingProduct.setStockQuantity(productDetails.getStockQuantity());
        existingProduct.setCategory(productDetails.getCategory());
        existingProduct.setImageUrl(productDetails.getImageUrl());
        existingProduct.setExpirationDate(productDetails.getExpirationDate());

        try {
            Product updatedProduct = productService.saveProduct(existingProduct);
            return ResponseEntity.ok(updatedProduct);
        } catch (IllegalArgumentException e) {
            // TODO: Trả về thông báo lỗi cụ thể hơn trong body của 400 Bad Request
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<HttpStatus> deleteProduct(@PathVariable String productId) {
        try {
            productService.deleteProduct(productId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{productId}/stock")
    public ResponseEntity<Product> updateProductStock(@PathVariable String productId, @RequestParam Integer newStock) {
        try {
            Optional<Product> updatedProduct = productService.updateProductStock(productId, newStock);
            return updatedProduct.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Lấy sản phẩm theo category có phân trang và sắp xếp.
     * GET /api/products/by-category?category={category}&page=0&size=10&sortBy=productId&sortDirection=asc
     */
    @GetMapping("/by-category")
    public ResponseEntity<Page<Product>> getProductsByCategory(
            @RequestParam String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "productId") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection.toUpperCase()), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> products = productService.getProductsByCategory(category, pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Lấy sản phẩm theo khoảng giá có phân trang và sắp xếp.
     * GET /api/products/by-price-range?minPrice={minPrice}&maxPrice={maxPrice}&page=0&size=10&sortBy=productId&sortDirection=asc
     */
    @GetMapping("/by-price-range")
    public ResponseEntity<Page<Product>> getProductsByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "productId") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        try {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection.toUpperCase()), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Product> products = productService.getProductsByPriceRange(minPrice, maxPrice, pageable);
            return ResponseEntity.ok(products);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * MỚI: Lấy sản phẩm có giá lớn hơn hoặc bằng một mức giá nhất định, có phân trang và sắp xếp.
     * Endpoint này có thể được sử dụng riêng hoặc tích hợp vào /by-price-range bằng cách không truyền maxPrice.
     * Tôi giữ nó ở đây như một lựa chọn rõ ràng.
     * GET /api/products/by-price-greater-than?minPrice={minPrice}&page=0&size=10&sortBy=productId&sortDirection=asc
     */
    @GetMapping("/by-price-greater-than")
    public ResponseEntity<Page<Product>> getProductsByPriceGreaterThanEqual(
            @RequestParam BigDecimal minPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "productId") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        try {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection.toUpperCase()), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Product> products = productService.getProductsByPriceGreaterThanEqual(minPrice, pageable);
            return ResponseEntity.ok(products);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Lấy sản phẩm theo category có phân trang và sắp xếp (endpoint cũ, nay điều chỉnh để trả về Page).
     * CHÚ Ý: Endpoint này có chức năng trùng lặp với /by-category, bạn có thể cân nhắc loại bỏ.
     * GET /api/products/by-category-sorted?category={category}&page=0&size=10&sortBy=productId&sortDirection=asc
     */
    @GetMapping("/by-category-sorted")
    public ResponseEntity<Page<Product>> getProductsByCategoryAndSorted(
            @RequestParam String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "productId") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection.toUpperCase()), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        // Dùng lại phương thức getProductsByCategory đã có trong ProductService
        Page<Product> products = productService.getProductsByCategory(category, pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Lấy tất cả sản phẩm có phân trang và sắp xếp (endpoint cũ, nay điều chỉnh để trả về Page).
     * CHÚ Ý: Endpoint này có chức năng trùng lặp với endpoint gốc GET /api/products, bạn có thể cân nhắc loại bỏ.
     * GET /api/products/sorted?page=0&size=10&sortBy=productId&sortDirection=asc
     */
    @GetMapping("/sorted")
    public ResponseEntity<Page<Product>> getAllProductsSorted(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "productId") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection.toUpperCase()), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        // Dùng lại phương thức getAllProducts đã có trong ProductService
        Page<Product> products = productService.getAllProducts(pageable);
        return ResponseEntity.ok(products);
    }

}