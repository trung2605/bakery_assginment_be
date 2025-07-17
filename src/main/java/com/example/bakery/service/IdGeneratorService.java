package com.example.bakery.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Service để sinh các ID có định dạng CHAR(7) cho các bảng.
 * LƯU Ý QUAN TRỌNG:
 * - Cơ chế này (sử dụng AtomicLong) chỉ đảm bảo tính duy nhất TRONG MỘT PHIÊN CHẠY của ứng dụng.
 * - Nếu ứng dụng khởi động lại, counter sẽ reset về 0, dẫn đến trùng lặp ID.
 * - Trong môi trường sản phẩm thực tế, bạn cần một cơ chế lưu trữ counter vào database
 * (ví dụ: một bảng 'id_counters' riêng) và cập nhật nó một cách an toàn (sử dụng LOCK hoặc transaction).
 * - Hoặc cân nhắc sử dụng UUID.randomUUID().toString() và đổi kiểu cột trong DB thành VARCHAR(36)
 * nếu bạn không cần ID ngắn gọn theo format này.
 */
@Service
public class IdGeneratorService {


    @PersistenceContext
    private EntityManager entityManager;

    public String generateId(String table, String column, String prefix, int length) {
        String query = String.format("SELECT MAX(CAST(SUBSTRING(%s, %d) AS UNSIGNED)) FROM %s WHERE %s LIKE '%s%%'",
                column, prefix.length() + 1, table, column, prefix);
        Long maxId = (Long) entityManager.createNativeQuery(query).getSingleResult();
        maxId = (maxId == null) ? 0 : maxId;
        long newId = maxId + 1;
        return String.format("%s%0" + (length - prefix.length()) + "d", prefix, newId);
    }

    public String generateCartId() {
        return generateId("carts", "cart_id", "CRT", 7);
    }

    public String generateCartItemId() {
        return generateId("cart_items", "cart_item_id", "CIT", 7);
    }

    public String generateUserId() {
        return generateId("users", "user_id", "CUS", 7); // Sửa từ "id" thành "user_id"
    }
}