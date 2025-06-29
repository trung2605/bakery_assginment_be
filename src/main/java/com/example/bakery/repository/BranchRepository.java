// Trong file: src/main/java/com/example/bakery/repository/BranchRepository.java
package com.example.bakery.repository;

import com.example.bakery.model.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Integer> {
    Optional<Branch> findByName(String name);
    Optional<Branch> findByAddress(String address);
    List<Branch> findByHotline(String hotline);
    List<Branch> findByNameContainingIgnoreCase(String nameKeyword);
    // ... các phương thức khác nếu có
}