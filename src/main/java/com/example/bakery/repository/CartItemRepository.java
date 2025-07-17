package com.example.bakery.repository;

import com.example.bakery.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, String> {
    Optional<CartItem> findByCart_CartIdAndProduct_ProductId(String cartId, String productId);
    void deleteByCart_CartIdAndCartItemId(String cartId, String cartItemId);
}
