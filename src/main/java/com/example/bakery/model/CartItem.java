package com.example.bakery.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString; // Tạm thời loại bỏ @ToString hoặc loại trừ trường 'cart'
import org.hibernate.proxy.HibernateProxy;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "cart_items")
@Getter
@Setter
// !!! QUAN TRỌNG: Loại bỏ @ToString hoặc loại trừ các trường liên quan đến quan hệ hai chiều.
// Nếu không, toString() cũng có thể gây StackOverflowError.
@ToString(exclude = {"cart"}) // Loại trừ cart khỏi toString()
public class CartItem {

    @Id
    private String cartItemId;

    @ManyToOne(fetch = FetchType.LAZY) // LUÔN LUÔN dùng LAZY cho ManyToOne để tránh tải dữ liệu không cần thiết
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY) // LUÔN LUÔN dùng LAZY cho ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private int quantity;

    @Column(name = "price_at_addition", precision = 10, scale = 2) // Giả định kiểu dữ liệu cho giá
    private BigDecimal priceAtAddition;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (o instanceof HibernateProxy) {
            o = ((HibernateProxy) o).getHibernateLazyInitializer().getImplementation();
        }
        if (getClass() != o.getClass()) return false;
        CartItem cartItem = (CartItem) o;
        // Chỉ so sánh dựa trên Primary Key (cartItemId)
        return cartItemId != null && Objects.equals(cartItemId, cartItem.cartItemId);
    }

    @Override
    public final int hashCode() {
        // Chỉ tính hashCode dựa trên Primary Key (cartItemId)
        return cartItemId != null ? Objects.hash(cartItemId) : 0;
    }
}