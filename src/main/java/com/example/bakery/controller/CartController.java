package com.example.bakery.controller;


import com.example.bakery.dto.AddItemToCartRequest;
import com.example.bakery.dto.CartDto;
import com.example.bakery.dto.UpdateCartItemRequest;
import com.example.bakery.model.Cart;
import com.example.bakery.model.CartItem;
import com.example.bakery.service.CartService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/carts")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("/{cartId}")
    public ResponseEntity<CartDto> getCart(@PathVariable String cartId) {
        CartDto cartDto = cartService.getCart(cartId);
        return ResponseEntity.ok(cartDto);
    }

    @PostMapping("/items")
    public ResponseEntity<CartDto> addItemToCart(@RequestBody String rawBody) throws JsonProcessingException {
        log.info("Raw request body: {}", rawBody);
        ObjectMapper mapper = new ObjectMapper();
        AddItemToCartRequest request = mapper.readValue(rawBody, AddItemToCartRequest.class);
        log.info("Parsed request: {}", request);
        CartDto cartDto = cartService.addItemToCart(request);
        return ResponseEntity.ok(cartDto);
    }

    @PutMapping("/items")
    public ResponseEntity<CartDto> updateItemQuantity(@RequestBody UpdateCartItemRequest request) {
        CartDto cartDto = cartService.updateCartItemQuantity(request);
        return ResponseEntity.ok(cartDto);
    }

    @DeleteMapping("/{cartId}/items/{cartItemId}")
    public ResponseEntity<CartDto> removeItemFromCart(@PathVariable String cartId, @PathVariable String cartItemId) {
        CartDto cartDto = cartService.removeCartItem(cartId, cartItemId);
        return ResponseEntity.ok(cartDto);
    }

    @DeleteMapping("/{cartId}")
    public ResponseEntity<Void> clearCart(@PathVariable String cartId) {
        cartService.clearCart(cartId);
        return ResponseEntity.noContent().build();
    }
}