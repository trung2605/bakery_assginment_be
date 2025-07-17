package com.example.bakery.repository;

import com.example.bakery.model.Cart;
import com.example.bakery.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, String> {
     Optional<Cart> findByUser(User user); // Bỏ comment nếu có User entity
}