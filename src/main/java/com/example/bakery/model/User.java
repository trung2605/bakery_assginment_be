package com.example.bakery.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users") // Tên bảng trong database là Users
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @Column(name = "user_id", length = 7, nullable = false, unique = true)
    // Bỏ @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String userId;

    @Column(name = "first_name", length = 100)
    private String firstName; // first_name VARCHAR(100)

    @Column(name = "last_name", length = 100)
    private String lastName; // last_name VARCHAR(100)

    @Column(name = "email", length = 100, unique = true, nullable = false)
    private String email; // email VARCHAR(100) UNIQUE NOT NULL

    @Column(name = "phone", length = 20, unique = true)
    private String phone; // phone VARCHAR(20) UNIQUE

    @JsonIgnore // Không hiển thị password_hash khi trả về JSON
    @Column(name = "password_hash", nullable = false)
    private String passwordHash; // password_hash VARCHAR(255) NOT NULL

    @Column(name = "created_at")
    private LocalDateTime createdAt; // created_at DATETIME DEFAULT CURRENT_TIMESTAMP

    @Column(name = "role")
    private Boolean role = false;

    @PrePersist // Hàm được gọi trước khi entity được lưu vào database lần đầu
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}