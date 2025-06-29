// Trong file: src/main/java/com/example/bakery/service/BranchService.java
package com.example.bakery.service; // Đã cập nhật package path

import com.example.bakery.model.Branch; // Import lớp Branch Entity
import com.example.bakery.repository.BranchRepository; // Import BranchRepository
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BranchService {

    private final BranchRepository branchRepository;

    @Autowired
    public BranchService(BranchRepository branchRepository) {
        this.branchRepository = branchRepository;
    }

    /**
     * Lấy tất cả các chi nhánh.
     * @return Danh sách tất cả chi nhánh.
     */
    public List<Branch> getAllBranches() {
        return branchRepository.findAll();
    }

    /**
     * Lấy thông tin chi nhánh theo ID.
     * @param id ID của chi nhánh.
     * @return Optional chứa đối tượng Branch nếu tìm thấy, hoặc Optional.empty().
     */
    public Optional<Branch> getBranchById(Integer id) {
        return branchRepository.findById(id);
    }

    /**
     * Lưu (thêm mới hoặc cập nhật) một chi nhánh.
     * Bao gồm kiểm tra trùng lặp tên chi nhánh.
     * @param branch Đối tượng Branch cần lưu.
     * @return Đối tượng Branch đã được lưu.
     * @throws IllegalArgumentException nếu tên chi nhánh đã tồn tại.
     */
    public Branch saveBranch(Branch branch) {
        // Kiểm tra trùng lặp tên chi nhánh (trừ trường hợp đang cập nhật chính nó)
        branchRepository.findByName(branch.getName()).ifPresent(existingBranch -> {
            if (branch.getId() == null || !existingBranch.getId().equals(branch.getId())) {
                throw new IllegalArgumentException("Tên chi nhánh '" + branch.getName() + "' đã tồn tại.");
            }
        });
        return branchRepository.save(branch);
    }

    /**
     * Lấy thông tin chi nhánh theo tên.
     * @param name Tên chi nhánh.
     * @return Optional chứa đối tượng Branch nếu tìm thấy, hoặc Optional.empty().
     */
    public Optional<Branch> getBranchByName(String name) {
        return branchRepository.findByName(name);
    }

    /**
     * Lấy danh sách chi nhánh theo hotline.
     * @param hotline Số hotline.
     * @return Danh sách các chi nhánh có hotline tương ứng.
     */
    public List<Branch> getBranchesByHotline(String hotline) {
        return branchRepository.findByHotline(hotline);
    }

    /**
     * Xóa một chi nhánh theo ID.
     * @param id ID của chi nhánh cần xóa.
     */
    public void deleteBranch(Integer id) {
        if (!branchRepository.existsById(id)) {
            throw new IllegalArgumentException("Chi nhánh với ID '" + id + "' không tồn tại.");
        }
        branchRepository.deleteById(id);
    }
}