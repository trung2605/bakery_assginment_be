package com.example.bakery.dto;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDto {
    private String cartItemId;
    private String productId;
    private String productName; // Để hiển thị tên sản phẩm
    private String imageUrl; // Để hiển thị hình ảnh sản phẩm
    private BigDecimal priceAtAddition; // Giá sản phẩm tại thời điểm thêm vào giỏ
    private int quantity;
    private BigDecimal subtotal; // quantity * priceAtAddition
}