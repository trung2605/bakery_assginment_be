package com.example.bakery.model;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "Branches")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Branch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Tự động tăng ID
    @Column(name = "id")
    private Integer id; // INT AUTO_INCREMENT

    @Column(name = "name", length = 255, nullable = false)
    private String name; // VARCHAR(255) NOT NULL

    @Column(name = "address", length = 500, nullable = false)
    private String address; // VARCHAR(500) NOT NULL

    @Column(name = "hotline", length = 20)
    private String hotline; // VARCHAR(20)

    @Column(name = "map_url", length = 1000)
    private String mapUrl; // VARCHAR(1000)
}