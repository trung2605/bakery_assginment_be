package com.example.bakery.dto;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDto {
    private String cartId;
    private String userId; // Có thể null nếu là giỏ hàng của khách
    private List<CartItemDto> cartItems;
    private int totalItems; // Tổng số lượng sản phẩm khác nhau trong giỏ
    private BigDecimal totalAmount;
}