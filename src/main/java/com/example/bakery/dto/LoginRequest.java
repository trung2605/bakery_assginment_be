// src/main/java/com/example/bakery/dto/LoginRequest.java
package com.example.bakery.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Email hoặc User ID không được để trống")
    private String identifier; // Có thể là email hoặc userId

    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;
}