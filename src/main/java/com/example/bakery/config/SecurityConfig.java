// src/main/java/com/example/bakery/config/SecurityConfig.java
package com.example.bakery.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        // Bỏ qua CSRF cho tất cả các đường dẫn bắt đầu bằng /api/
                        .ignoringRequestMatchers("/api/**") // <-- Dòng này là chìa khóa
                )
                .authorizeHttpRequests(auth -> auth
                        // Cho phép tất cả các request đến /api/**
                        .requestMatchers("/api/**").permitAll() // <-- Quan trọng: Đảm bảo các API này có thể truy cập công khai
                        // Các request khác (nếu có) có thể yêu cầu xác thực
                        .anyRequest().authenticated()
                );
        // .formLogin().disable() // Nếu không sử dụng form login mặc định
        // .httpBasic().disable(); // Nếu không sử dụng basic auth

        return http.build();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**") // Áp dụng cho tất cả các đường dẫn API
                        .allowedOrigins("http://localhost:3000") // Thay bằng origin frontend của bạn
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*") // Cho phép tất cả các headers
                        .allowCredentials(true); // Quan trọng nếu bạn gửi cookie (session, CSRF token)
            }
        };
    }




}