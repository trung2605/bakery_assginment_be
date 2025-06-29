// src/main/java/com/example/bakery/controller/AuthController.java
package com.example.bakery.controller;

import com.example.bakery.dto.LoginRequest;
import com.example.bakery.dto.RegisterRequest;
import com.example.bakery.dto.AuthResponse; // Nếu bạn dùng AuthResponse
import com.example.bakery.model.User; // Import User Entity
import com.example.bakery.service.UserService;
import jakarta.validation.Valid; // Để sử dụng @Valid cho validation DTO
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth") // Base URL cho các API xác thực
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            // Chuyển đổi RegisterRequest DTO sang User Entity
            User newUser = new User();
            newUser.setFirstName(registerRequest.getFirstName());
            newUser.setLastName(registerRequest.getLastName());
            newUser.setEmail(registerRequest.getEmail());
            newUser.setPhone(registerRequest.getPhone());
            newUser.setPasswordHash(registerRequest.getPassword()); // Mật khẩu thô từ DTO

            User registeredUser = userService.registerUser(newUser);

            // Xây dựng AuthResponse
            AuthResponse response = new AuthResponse(
                    registeredUser.getUserId(),
                    registeredUser.getEmail(),
                    registeredUser.getFirstName(),
                    registeredUser.getLastName(),
                    registeredUser.getRole() != null ? String.valueOf(registeredUser.getRole()) : "USER", // Chuyển đổi Integer sang String cho tiện
                    "Đăng ký thành công!",
                    null // JWT token sẽ được thêm vào đây nếu triển khai
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            // Trả về lỗi 400 Bad Request với thông báo lỗi cụ thể
            return ResponseEntity.badRequest().body(new AuthResponse(null, null, null, null, null, e.getMessage(), null));
        }
    }

    /**
     * Endpoint đăng nhập người dùng.
     * POST /api/auth/login
     * @param loginRequest DTO chứa thông tin đăng nhập từ client.
     * @return ResponseEntity chứa thông tin người dùng và token (nếu có) hoặc lỗi.
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Optional<User> authenticatedUser = userService.authenticateUser(
                    loginRequest.getIdentifier(),
                    loginRequest.getPassword()
            );

            if (authenticatedUser.isPresent()) {
                User user = authenticatedUser.get();
                // TODO: Tạo JWT Token ở đây nếu bạn triển khai xác thực JWT
                String jwtToken = "mock_jwt_token_example_for_" + user.getUserId(); // Placeholder

                AuthResponse response = new AuthResponse(
                        user.getUserId(),
                        user.getEmail(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getRole() != null ? String.valueOf(user.getRole()) : "USER",
                        "Đăng nhập thành công!",
                        jwtToken
                );
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse(null, null, null, null, null, "Email/User ID hoặc mật khẩu không chính xác.", null));
            }
        } catch (Exception e) {
            // Bắt các ngoại lệ chung (ví dụ: lỗi hệ thống)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new AuthResponse(null, null, null, null, null, "Đã xảy ra lỗi server: " + e.getMessage(), null));
        }
    }

    // GHI CHÚ: Đối với các lỗi validation từ @Valid, Spring sẽ tự động
    // trả về 400 Bad Request. Bạn có thể tùy chỉnh phản hồi này bằng
    // @ControllerAdvice hoặc @ExceptionHandler.
    // Ví dụ:
    /*
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
        MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.badRequest().body(errors);
    }
    */
}