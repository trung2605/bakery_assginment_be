package com.example.bakery.controller; // Package path cho Controller

import com.example.bakery.model.User; // Import User Entity
import com.example.bakery.service.UserService; // Import UserService
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus; // Để trả về các mã trạng thái HTTP
import org.springframework.http.ResponseEntity; // Để trả về phản hồi HTTP đầy đủ
import org.springframework.web.bind.annotation.*; // Các annotation cho REST Controller

import java.util.List;
import java.util.Optional;

@RestController // Đánh dấu đây là một REST Controller
@RequestMapping("/api/users") // Định nghĩa base URL cho tất cả các endpoint trong Controller này
public class UserController {

    private final UserService userService;

    @Autowired // Tiêm UserService vào Controller
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Lấy tất cả người dùng.
     * GET /api/users
     * @return ResponseEntity chứa danh sách người dùng và trạng thái HTTP OK.
     */
    @GetMapping // Ánh xạ yêu cầu GET đến /api/users
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users); // Trả về 200 OK với danh sách người dùng
    }

    /**
     * Lấy thông tin người dùng theo ID.
     * GET /api/users/{userId}
     * @param userId ID của người dùng.
     * @return ResponseEntity chứa User và trạng thái HTTP OK, hoặc NOT_FOUND nếu không tìm thấy.
     */
    @GetMapping("/{userId}") // Ánh xạ yêu cầu GET đến /api/users/{userId}
    public ResponseEntity<User> getUserById(@PathVariable String userId) { // Lấy userId từ URL path
        Optional<User> user = userService.getUserById(userId);
        return user.map(ResponseEntity::ok) // Nếu tìm thấy, trả về 200 OK với User
                .orElseGet(() -> ResponseEntity.notFound().build()); // Nếu không, trả về 404 Not Found
    }

    /**
     * Tạo người dùng mới.
     * POST /api/users
     * @param user Đối tượng User được gửi trong request body.
     * @return ResponseEntity chứa User đã tạo và trạng thái HTTP CREATED.
     */
    @PostMapping // Ánh xạ yêu cầu POST đến /api/users
    public ResponseEntity<User> createUser(@RequestBody User user) { // Nhận đối tượng User từ request body
        try {
            User createdUser = userService.saveUser(user);
            // Trả về 201 Created cùng với location của tài nguyên mới
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (IllegalArgumentException e) {
            // Xử lý các lỗi validation từ Service layer
            return ResponseEntity.badRequest().build(); // Trả về 400 Bad Request
            // Trong ứng dụng thực tế, bạn có thể trả về một đối tượng lỗi chi tiết hơn
        }
    }

    /**
     * Cập nhật thông tin người dùng.
     * PUT /api/users/{userId}
     * @param userId ID của người dùng cần cập nhật.
     * @param userDetails Đối tượng User với thông tin cập nhật trong request body.
     * @return ResponseEntity chứa User đã cập nhật và trạng thái HTTP OK, hoặc NOT_FOUND/BAD_REQUEST.
     */
    @PutMapping("/{userId}") // Ánh xạ yêu cầu PUT đến /api/users/{userId}
    public ResponseEntity<User> updateUser(@PathVariable String userId, @RequestBody User userDetails) {
        Optional<User> existingUserOptional = userService.getUserById(userId);
        if (existingUserOptional.isEmpty()) {
            return ResponseEntity.notFound().build(); // 404 Not Found nếu người dùng không tồn tại
        }

        User existingUser = existingUserOptional.get();
        // Cập nhật thông tin từ userDetails vào existingUser
        existingUser.setFirstName(userDetails.getFirstName());
        existingUser.setLastName(userDetails.getLastName());
        existingUser.setEmail(userDetails.getEmail());
        existingUser.setPhone(userDetails.getPhone());
        existingUser.setPasswordHash(userDetails.getPasswordHash()); // Cẩn thận khi cập nhật mật khẩu!
        existingUser.setRole(userDetails.getRole());

        try {
            User updatedUser = userService.saveUser(existingUser); // Sử dụng save để cập nhật
            return ResponseEntity.ok(updatedUser); // 200 OK
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build(); // 400 Bad Request cho lỗi validation
        }
    }

    /**
     * Xóa người dùng theo ID.
     * DELETE /api/users/{userId}
     * @param userId ID của người dùng cần xóa.
     * @return ResponseEntity với trạng thái HTTP NO_CONTENT nếu thành công, hoặc NOT_FOUND.
     */
    @DeleteMapping("/{userId}") // Ánh xạ yêu cầu DELETE đến /api/users/{userId}
    public ResponseEntity<HttpStatus> deleteUser(@PathVariable String userId) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity.noContent().build(); // 204 No Content nếu xóa thành công
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build(); // 404 Not Found nếu người dùng không tồn tại để xóa
        }
    }

    /**
     * Lấy người dùng theo email.
     * GET /api/users/by-email?email={email}
     * @param email Địa chỉ email của người dùng.
     * @return ResponseEntity chứa User và trạng thái HTTP OK, hoặc NOT_FOUND.
     */
    @GetMapping("/by-email") // Ví dụ sử dụng @RequestParam
    public ResponseEntity<User> getUserByEmail(@RequestParam String email) {
        Optional<User> user = userService.findUserByEmail(email);
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Lấy danh sách người dùng theo vai trò.
     * GET /api/users/by-role?role={role}
     * @param role Vai trò của người dùng.
     * @return ResponseEntity chứa danh sách người dùng và trạng thái HTTP OK.
     */
    @GetMapping("/by-role")
    public ResponseEntity<List<User>> getUsersByRole(@RequestParam Integer role) {
        List<User> users = userService.getUsersByRole(role);
        return ResponseEntity.ok(users);
    }
}