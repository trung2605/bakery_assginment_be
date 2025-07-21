// src/main/java/com/example/bakery/service/UserService.java
package com.example.bakery.service;

import com.example.bakery.model.Cart;
import com.example.bakery.model.User;
import com.example.bakery.repository.CartRepository;
import com.example.bakery.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Import Transactional

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private IdGeneratorService idGeneratorService;


    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public User registerUser(User user) {
        if (user.getUserId() == null) {
            user.setUserId(idGeneratorService.generateUserId());
        }
        if (existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email đã được sử dụng.");
        }
        // Băm mật khẩu nếu chưa được băm
        if (!user.getPasswordHash().startsWith("$2a$")) { // Kiểm tra xem mật khẩu đã được băm chưa
            user.setPasswordHash(hashPassword(user.getPasswordHash()));
        }
        User savedUser = userRepository.save(user);

        // Tạo giỏ hàng trống cho user mới
        Optional<Cart> existingCart = cartRepository.findByUser(savedUser);
        if (existingCart.isEmpty()) {
            Cart cart = new Cart();
            cart.setCartId(idGeneratorService.generateCartId());
            cart.setUser(savedUser);
            cartRepository.save(cart);
        }

        return savedUser;
    }

    public boolean existsByPhone(String phone) {
        return userRepository.existsByPhone(phone);
    }


    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }

    public Optional<User> authenticateUser(String identifier, String password) {
        Optional<User> user = userRepository.findByEmail(identifier);
        if (user.isEmpty()) {
            user = userRepository.findById(identifier);
        }
        if (user.isPresent() && passwordEncoder.matches(password, user.get().getPasswordHash())) {
            return user;
        }
        return Optional.empty();
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(String userId) {
        return userRepository.findById(userId);
    }

    @Transactional
    public User saveUser(User user) {
        if (user.getUserId() == null) {
            user.setUserId(idGeneratorService.generateUserId());
        }
        if (existsByEmail(user.getEmail()) && !getUserById(user.getUserId()).map(u -> u.getEmail().equals(user.getEmail())).orElse(false)) {
            throw new IllegalArgumentException("Email đã được sử dụng bởi user khác.");
        }
        // Băm mật khẩu nếu cần
        if (user.getPasswordHash() != null && !user.getPasswordHash().startsWith("$2a$")) {
            user.setPasswordHash(hashPassword(user.getPasswordHash()));
        }
        User savedUser = userRepository.save(user);

        // Tạo giỏ hàng nếu user mới và chưa có giỏ hàng
        if (getUserById(user.getUserId()).isEmpty()) {
            Optional<Cart> existingCart = cartRepository.findByUser(savedUser);
            if (existingCart.isEmpty()) {
                Cart cart = new Cart();
                cart.setCartId(idGeneratorService.generateCartId());
                cart.setUser(savedUser);
                cartRepository.save(cart);
            }
        }

        return savedUser;
    }

    @Transactional
    public void deleteUser(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User không tồn tại: " + userId);
        }
        userRepository.deleteById(userId);
        // Ghi chú: Giỏ hàng sẽ được tự động đặt user_id thành NULL do ràng buộc ON DELETE SET NULL
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> getUsersByRole(Integer role) {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() != null && user.getRole().equals(role != 0))
                .toList();
    }
}