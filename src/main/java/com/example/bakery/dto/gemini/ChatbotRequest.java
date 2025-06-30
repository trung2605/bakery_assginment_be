package com.example.bakery.dto.gemini;

import com.example.bakery.dto.gemini.ChatbotMessage;
import lombok.Data; // Sử dụng Lombok để tự động tạo getter/setter/constructor

import java.util.ArrayList;
import java.util.List;

@Data // Lombok annotation để tạo getter, setter, equals, hashCode, toString
public class ChatbotRequest {
    private String userMessage;
    // Khởi tạo chatHistory là một ArrayList trống để tránh NullPointerException
    private List<ChatbotMessage> chatHistory = new ArrayList<>();

    // Constructor mặc định (cần thiết nếu có constructor khác)
    public ChatbotRequest() {
    }

    // Constructor với tất cả các trường (có thể được Lombok @AllArgsConstructor tạo ra)
    public ChatbotRequest(String userMessage, List<ChatbotMessage> chatHistory) {
        this.userMessage = userMessage;
        // Đảm bảo rằng nếu chatHistory được truyền vào là null, ta vẫn dùng ArrayList mới
        this.chatHistory = (chatHistory != null) ? chatHistory : new ArrayList<>();
    }
}