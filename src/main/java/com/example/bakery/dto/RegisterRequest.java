// src/main/java/com/example/bakery/dto/RegisterRequest.java
package com.example.bakery.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data // Lombok: Tự động tạo getters, setters, toString, equals, hashCode
public class RegisterRequest {

    @NotBlank(message = "Tên không được để trống")
    @Size(max = 100, message = "Tên không được vượt quá 100 ký tự")
    private String firstName;

    @NotBlank(message = "Họ không được để trống")
    @Size(max = 100, message = "Họ không được vượt quá 100 ký tự")
    private String lastName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    private String email;

    @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự")
    private String phone; // Có thể để null

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    @Size(max = 255, message = "Mật khẩu không được vượt quá 255 ký tự") // Để phù hợp với passwordHash VARCHAR(255)
    private String password;

    // Vai trò (role) có thể được thiết lập mặc định trong service
    // private Integer role; // Có thể thêm nếu bạn muốn client cung cấp role
}