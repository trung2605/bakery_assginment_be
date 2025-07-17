package com.example.bakery.controller;

import com.example.bakery.dto.LoginRequest;
import com.example.bakery.dto.RegisterRequest;
import com.example.bakery.dto.AuthResponse;
import com.example.bakery.model.Cart;
import com.example.bakery.model.User;
import com.example.bakery.repository.CartRepository;
import com.example.bakery.repository.UserRepository;
import com.example.bakery.service.IdGeneratorService;
import com.example.bakery.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final IdGeneratorService idGeneratorService;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;

    @Autowired
    public AuthController(UserService userService, IdGeneratorService idGeneratorService, CartRepository cartRepository, UserRepository userRepository) {
        this.userService = userService;
        this.idGeneratorService = idGeneratorService;
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            if (userService.existsByEmail(registerRequest.getEmail())) {
                return ResponseEntity.badRequest().body(new AuthResponse(null, null, null, null, null, null, null, "Email đã được sử dụng."));
            }

            User newUser = new User();
            newUser.setUserId(idGeneratorService.generateUserId());
            newUser.setFirstName(registerRequest.getFirstName());
            newUser.setLastName(registerRequest.getLastName());
            newUser.setEmail(registerRequest.getEmail());
            newUser.setPhone(registerRequest.getPhone());
            newUser.setPasswordHash(userService.hashPassword(registerRequest.getPassword()));

            User registeredUser = userService.registerUser(newUser);

            String cartId;
            Optional<Cart> existingCart = cartRepository.findByUser(registeredUser);
            if (existingCart.isEmpty()) {
                Cart cart = new Cart();
                cart.setCartId(idGeneratorService.generateCartId());
                cart.setUser(userRepository.findById(registeredUser.getUserId()).orElseThrow());
                cartRepository.save(cart);
                cartId = cart.getCartId();
            } else {
                cartId = existingCart.get().getCartId();
            }

            AuthResponse response = new AuthResponse(
                    registeredUser.getUserId(),
                    registeredUser.getEmail(),
                    registeredUser.getFirstName(),
                    registeredUser.getLastName(),
                    registeredUser.getRole() != null && registeredUser.getRole() ? "ADMIN" : "USER",
                    cartId,
                    "Đăng ký thành công!",
                    null
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new AuthResponse(null, null, null, null, null, null, null, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new AuthResponse(null, null, null, null, null, null, null, "Lỗi server: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Optional<User> authenticatedUser = userService.authenticateUser(
                    loginRequest.getIdentifier(),
                    loginRequest.getPassword()
            );

            if (authenticatedUser.isPresent()) {
                User user = authenticatedUser.get();
                String cartId = cartRepository.findByUser(user)
                        .map(Cart::getCartId)
                        .orElseThrow(() -> new IllegalStateException("Không tìm thấy giỏ hàng cho user: " + user.getUserId()));

                AuthResponse response = new AuthResponse(
                        user.getUserId(),
                        user.getEmail(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getRole() != null && user.getRole() ? "ADMIN" : "USER",
                        cartId,
                        "Đăng nhập thành công!",
                        null
                );
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        new AuthResponse(null, null, null, null, null, null, null, "Email/User ID hoặc mật khẩu không chính xác.")
                );
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new AuthResponse(null, null, null, null, null, null, null, "Lỗi server: " + e.getMessage())
            );
        }
    }
}