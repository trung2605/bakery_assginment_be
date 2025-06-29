package com.example.bakery.controller;

import com.example.bakery.model.Branch;
import com.example.bakery.service.BranchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/branches")
public class BranchController {

    private final BranchService branchService;

    @Autowired
    public BranchController(BranchService branchService) {
        this.branchService = branchService;
    }

    @GetMapping
    public ResponseEntity<List<Branch>> getAllBranches() {
        List<Branch> branches = branchService.getAllBranches();
        return ResponseEntity.ok(branches);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Branch> getBranchById(@PathVariable Integer id) {
        Optional<Branch> branch = branchService.getBranchById(id);
        return branch.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Branch> createBranch(@RequestBody Branch branch) {
        try {
            Branch createdBranch = branchService.saveBranch(branch);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdBranch);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null); // Trả về 400 Bad Request
            // TODO: Cải thiện phản hồi lỗi cho client
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Branch> updateBranch(@PathVariable Integer id, @RequestBody Branch branchDetails) {
        Optional<Branch> existingBranchOptional = branchService.getBranchById(id);
        if (existingBranchOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Branch existingBranch = existingBranchOptional.get();
        // Cập nhật thông tin từ branchDetails vào existingBranch
        existingBranch.setName(branchDetails.getName());
        existingBranch.setAddress(branchDetails.getAddress());
        existingBranch.setHotline(branchDetails.getHotline());
        existingBranch.setMapUrl(branchDetails.getMapUrl());

        try {
            Branch updatedBranch = branchService.saveBranch(existingBranch);
            return ResponseEntity.ok(updatedBranch);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
            // TODO: Cải thiện phản hồi lỗi cho client
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteBranch(@PathVariable Integer id) {
        try {
            branchService.deleteBranch(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/by-name")
    public ResponseEntity<Branch> getBranchByName(@RequestParam String name) {
        Optional<Branch> branch = branchService.getBranchByName(name);
        return branch.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/by-hotline")
    public ResponseEntity<List<Branch>> getBranchesByHotline(@RequestParam String hotline) {
        List<Branch> branches = branchService.getBranchesByHotline(hotline);
        return ResponseEntity.ok(branches);
    }
}