package com.example.bakery.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString; // Tạm thời loại bỏ @ToString hoặc loại trừ trường 'cartItems'
import org.hibernate.proxy.HibernateProxy; // Import này nếu bạn dùng lazy loading
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "carts")
@Getter
@Setter
@ToString(exclude = {"cartItems"}) // Loại trừ cartItems khỏi toString()
@EntityListeners(AuditingEntityListener.class) // Đảm bảo lớp này được lắng nghe cho Auditing
public class Cart {

    @Id
    private String cartId;

    @OneToOne(fetch = FetchType.LAZY) // Thường là LAZY để tránh tải User không cần thiết
    @JoinColumn(name = "user_id")
    private User user; // Có thể null nếu là giỏ hàng của khách

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CartItem> cartItems = new HashSet<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "last_modified_at", nullable = false)
    private LocalDateTime lastModifiedAt;

    public void addCartItem(CartItem item) {
        cartItems.add(item);
        item.setCart(this);
    }

    public void removeCartItem(CartItem item) {
        cartItems.remove(item);
        item.setCart(null);
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        // Xử lý proxy của Hibernate
        if (o instanceof HibernateProxy) {
            o = ((HibernateProxy) o).getHibernateLazyInitializer().getImplementation();
        }
        if (getClass() != o.getClass()) return false;
        Cart cart = (Cart) o;
        return cartId != null && Objects.equals(cartId, cart.cartId);
    }

    @Override
    public final int hashCode() {
        return cartId != null ? Objects.hash(cartId) : 0;
    }
}