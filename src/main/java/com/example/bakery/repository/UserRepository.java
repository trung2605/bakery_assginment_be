// Trong file: src/main/java/com/example/bakery/repository/UserRepository.java
package com.example.bakery.repository;

import com.example.bakery.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List; // Import List
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
    List<User> findByRole(Boolean role);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    // ... các phương thức khác nếu có

    @Query("SELECT u.userId FROM User u WHERE u.userId LIKE 'CUS%' ORDER BY u.userId DESC LIMIT 1")
    Optional<String> findTopByUserIdOrderByUserIdDesc();
}