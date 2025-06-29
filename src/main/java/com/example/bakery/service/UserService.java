// src/main/java/com/example/bakery/service/UserService.java
package com.example.bakery.service;

import com.example.bakery.model.User;
import com.example.bakery.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Import Transactional

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Lấy tất cả người dùng.
     * @return Danh sách tất cả người dùng.
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Lấy thông tin người dùng theo ID.
     * @param userId ID của người dùng.
     * @return Optional chứa đối tượng User nếu tìm thấy, hoặc Optional.empty() nếu không.
     */
    public Optional<User> getUserById(String userId) {
        return userRepository.findById(userId);
    }

    /**
     * Phương thức đăng ký người dùng mới với userId tự động tăng.
     * @param user Đối tượng User chứa thông tin đăng ký (mật khẩu chưa băm, userId sẽ được tạo).
     * @return Đối tượng User đã được lưu với mật khẩu đã băm và userId mới.
     * @throws IllegalArgumentException nếu email hoặc phone đã tồn tại.
     */
    @Transactional // Đảm bảo toàn bộ quá trình tạo ID và lưu là atomic
    public User registerUser(User user) {
        // Kiểm tra trùng lặp email và phone
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email '" + user.getEmail() + "' đã tồn tại.");
        }
        if (user.getPhone() != null && !user.getPhone().isEmpty() && userRepository.existsByPhone(user.getPhone())) {
            throw new IllegalArgumentException("Số điện thoại '" + user.getPhone() + "' đã tồn tại.");
        }

        // TẠO userId TỰ ĐỘNG Ở ĐÂY
        String newUserId = generateNextUserId();
        user.setUserId(newUserId); // Set userId đã tạo cho đối tượng User

        // Băm mật khẩu trước khi lưu vào CSDL
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));

        // Đặt ngày tạo nếu chưa có
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(LocalDateTime.now());
        }


        // Đặt vai trò mặc định nếu chưa có
        if (user.getRole() == null) {
            user.setRole(0); // 0 cho người dùng thường, 1 cho admin
        }

        return userRepository.save(user);
    }

    /**
     * Logic để tạo userId mới theo định dạng CUSxxxx.
     * Đây là một phương thức private hỗ trợ cho registerUser.
     * @return userId tiếp theo trong chuỗi.
     */
    private String generateNextUserId() {
        Optional<String> lastUserIdOptional = userRepository.findTopByUserIdOrderByUserIdDesc();

        String lastUserId = lastUserIdOptional.orElse("CUS0000"); // Nếu chưa có user nào, bắt đầu từ CUS0000
        int lastNumber = Integer.parseInt(lastUserId.substring(3)); // Lấy 4 chữ số cuối

        int nextNumber = lastNumber + 1;
        if (nextNumber > 9999) {
            throw new IllegalStateException("Đã đạt giới hạn User ID (CUS9999). Không thể tạo thêm.");
        }

        // Định dạng lại số thành chuỗi 4 chữ số có padding 0 ở đầu
        String nextUserId = String.format("CUS%04d", nextNumber);

        // Kiểm tra lại để tránh va chạm (race condition) trong môi trường đa luồng,
        // mặc dù @Transactional giúp giảm thiểu. Vẫn có thể cần synchronized
        // hoặc logic phức tạp hơn trong hệ thống lớn.
        if (userRepository.existsById(nextUserId)) {
            // Trường hợp cực hiếm nếu có race condition hoặc lỗi khác, thử lại hoặc báo lỗi
            throw new IllegalStateException("Generated User ID '" + nextUserId + "' already exists. Please try again.");
        }

        return nextUserId;
    }


    /**
     * Phương thức xác thực người dùng để đăng nhập.
     * @param identifier Email hoặc User ID của người dùng.
     * @param rawPassword Mật khẩu thô (chưa băm) từ người dùng.
     * @return Optional chứa đối tượng User nếu xác thực thành công, hoặc Optional.empty() nếu thất bại.
     */
    public Optional<User> authenticateUser(String identifier, String rawPassword) {
        Optional<User> userOptional;

        // Cố gắng tìm kiếm bằng email trước
        userOptional = userRepository.findByEmail(identifier);

        // Nếu không tìm thấy bằng email, thử tìm bằng userId
        if (userOptional.isEmpty()) {
            userOptional = userRepository.findById(identifier);
        }

        if (userOptional.isEmpty()) {
            return Optional.empty(); // Không tìm thấy người dùng nào với identifier này
        }

        User user = userOptional.get();

        // So sánh mật khẩu thô với mật khẩu đã băm trong CSDL
        if (passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            return Optional.of(user); // Xác thực thành công
        } else {
            return Optional.empty(); // Mật khẩu không khớp
        }
    }


    // Các phương thức khác (saveUser, findUserByEmail, getUsersByRole, deleteUser)
    // Cần xem xét lại saveUser nếu nó dùng để update userId.
    // Đối với `saveUser` (thường dùng cho cập nhật), nếu `userId` không được cung cấp,
    // nó sẽ tự động được gán bởi phương thức `generateNextUserId` khi gọi `registerUser`.
    // Nếu `saveUser` được gọi với một `User` có sẵn `userId`, nó sẽ cập nhật.
    // Trong trường hợp này, hãy để `saveUser` như cũ, chỉ tập trung vào `registerUser`.

    public User saveUser(User user) {
        // Phương thức này có thể được dùng cho việc cập nhật thông tin người dùng
        // không bao gồm mật khẩu hoặc mật khẩu đã được băm.

        // Kiểm tra trùng lặp email và phone khi CẬP NHẬT
        userRepository.findByEmail(user.getEmail()).ifPresent(existingUser -> {
            if (!existingUser.getUserId().equals(user.getUserId())) {
                throw new IllegalArgumentException("Email '" + user.getEmail() + "' đã tồn tại cho người dùng khác.");
            }
        });
        if (user.getPhone() != null && !user.getPhone().isEmpty()) {
            userRepository.findByPhone(user.getPhone()).ifPresent(existingUser -> {
                if (!existingUser.getUserId().equals(user.getUserId())) {
                    throw new IllegalArgumentException("Số điện thoại '" + user.getPhone() + "' đã tồn tại cho người dùng khác.");
                }
            });
        }
        // Ở đây không cần kiểm tra định dạng userId nữa vì nó đã được tạo hoặc tồn tại.
        // Và cũng không băm lại mật khẩu ở đây nếu không có yêu cầu cụ thể cập nhật mật khẩu.
        return userRepository.save(user);
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> getUsersByRole(Integer role) {
        return userRepository.findByRole(role);
    }

    public void deleteUser(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("Người dùng với ID '" + userId + "' không tồn tại.");
        }
        userRepository.deleteById(userId);
    }
}