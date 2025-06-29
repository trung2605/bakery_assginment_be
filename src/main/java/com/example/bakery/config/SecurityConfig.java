// src/main/java/com/example/bakery/config/SecurityConfig.java
package com.example.bakery.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(authorize -> authorize
                        // PHẢI ĐẶT TRƯỚC anyRequest().authenticated()
                        .requestMatchers(
                                "/api/auth/**",
                                "api/branches/**",
                                "api/products/**",
                                "api/users/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                );
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Thêm các nguồn gốc mà frontend của bạn đang chạy
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://127.0.0.1:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS")); // Cho phép các phương thức HTTP
        configuration.setAllowedHeaders(Arrays.asList("*")); // Cho phép tất cả các header
        configuration.setAllowCredentials(true); // Quan trọng nếu bạn gửi cookie hoặc header Authorization
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Áp dụng cấu hình này cho tất cả các đường dẫn
        return source;
    }
}