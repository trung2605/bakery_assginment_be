package com.example.bakery.controller;

import com.example.bakery.model.Product;
import com.example.bakery.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
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
            return ResponseEntity.badRequest().build();
            // TODO: Trả về thông báo lỗi cụ thể hơn trong body của 400 Bad Request
        }
    }

    @PutMapping("/{productId}")
    public ResponseEntity<Product> updateProduct(@PathVariable String productId, @RequestBody Product productDetails) {
        Optional<Product> existingProductOptional = productService.getProductById(productId);
        if (existingProductOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Product existingProduct = existingProductOptional.get();
        // Cập nhật thông tin từ productDetails vào existingProduct
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
            return ResponseEntity.badRequest().build();
            // TODO: Trả về thông báo lỗi cụ thể hơn trong body của 400 Bad Request
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

    @PatchMapping("/{productId}/stock") // Sử dụng PATCH cho cập nhật một phần
    public ResponseEntity<Product> updateProductStock(@PathVariable String productId, @RequestParam Integer newStock) {
        try {
            Optional<Product> updatedProduct = productService.updateProductStock(productId, newStock);
            return updatedProduct.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/by-category")
    public ResponseEntity<List<Product>> getProductsByCategory(@RequestParam String category) {
        List<Product> products = productService.getProductsByCategory(category);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/by-price-greater-than")
    public ResponseEntity<List<Product>> getProductsByPriceGreaterThanEqual(@RequestParam BigDecimal minPrice) {
        List<Product> products = productService.getProductsByPriceGreaterThanEqual(minPrice);
        return ResponseEntity.ok(products);
    }
}