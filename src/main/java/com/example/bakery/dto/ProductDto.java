package com.example.bakery.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private String productId;
    private String name;
    private BigDecimal price;
    private String description;
    private int stockQuantity;
    private List<String> imageUrls;
}