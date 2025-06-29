package com.example.bakery.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal; // Cho DECIMAL
// import jakarta.validation.constraints.Min; // Thêm nếu dùng validation cho stock_quantity

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @Column(name = "product_id", length = 7)
    // @Pattern(regexp = "PRD____", message = "Product ID must start with 'PRD' followed by 4 characters")
    private String productId; // CHAR(7)

    @Column(name = "name", length = 100)
    private String name; // VARCHAR(100)

    @Lob // Ánh xạ TEXT, BLOB types. Đối với TEXT, nó báo hiệu rằng đây là một trường lớn
    @Column(name = "description")
    private String description; // TEXT

    @Column(name = "price", precision = 10, scale = 0, nullable = false)
    private BigDecimal price; // DECIMAL(10,0)

    @Column(name = "stock_quantity")
    // @Min(value = 0, message = "Stock quantity cannot be negative") // validate ở mức ứng dụng
    private Integer stockQuantity; // INT

    @Column(name = "category", length = 100, nullable = false)
    private String category; // VARCHAR(100) NOT NULL

    @Column(name = "image_url", length = 255)
    private String imageUrl; // VARCHAR(255)

    @Column(name = "expiration_date", length = 50)
    private String expirationDate; // VARCHAR(50)

    // Nếu bạn muốn kiểm tra stock_quantity >= 0, nên làm ở service layer hoặc dùng @Min trên DTO/request object.
}