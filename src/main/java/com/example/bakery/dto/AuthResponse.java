// src/main/java/com/example/bakery/dto/AuthResponse.java
package com.example.bakery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private String role; // Hoặc Integer role
    private String cartId; // Optional: Nếu bạn triển khai JWT
    private String successMessage; // Thêm trường này
    private String errorMessage;   // Và trường này
}