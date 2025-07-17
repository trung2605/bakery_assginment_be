package com.example.bakery.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCartItemRequest {
    private String cartId;
    private String cartItemId; // ID của mục giỏ hàng cần cập nhật
    private int quantity;
}