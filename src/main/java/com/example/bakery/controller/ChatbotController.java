package com.example.bakery.controller;

import com.example.bakery.dto.gemini.ChatbotMessage;
import com.example.bakery.dto.gemini.ChatbotRequest;
import com.example.bakery.service.GeminiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/chatbot")
@Slf4j
@CrossOrigin(origins = "http://localhost:3000") // Đảm bảo CORS cho React frontend
public class ChatbotController {

    private final GeminiService geminiService;

    public ChatbotController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping("/message")
    public ResponseEntity<ChatbotMessage> sendMessage(@RequestBody ChatbotRequest request) {
        String userMessage = request.getUserMessage();
        List<ChatbotMessage> chatHistory = request.getChatHistory(); // Nhận lịch sử chat từ frontend

        log.info("Received message from user: {}", userMessage);

        try {
            // Gọi GeminiService để lấy phản hồi
            // Chúng ta cần truyền lịch sử chat để Gemini duy trì ngữ cảnh
            String botResponseText = geminiService.sendMessageToGemini(userMessage, chatHistory);

            // Tạo tin nhắn phản hồi của bot
            ChatbotMessage botMessage = new ChatbotMessage(
                    botResponseText,
                    "bot",
                    LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
            );
            return ResponseEntity.ok(botMessage);

        } catch (IOException e) {
            log.error("Error communicating with Gemini API for chatbot: {}", e.getMessage(), e);
            // Trả về lỗi nếu có vấn đề
            ChatbotMessage errorMessage = new ChatbotMessage(
                    "Xin lỗi, tôi đang gặp vấn đề. Vui lòng thử lại sau.",
                    "bot",
                    LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
        }
    }

    // Endpoint để lấy tin nhắn chào mừng hoặc khởi tạo chat nếu cần
    @GetMapping("/greet")
    public ResponseEntity<ChatbotMessage> getGreetingMessage() {
        ChatbotMessage greeting = new ChatbotMessage(
                "Chào bạn! Tôi là trợ lý ảo của Dola Bakery. Tôi có thể giúp gì cho bạn về các loại bánh ngọt của chúng tôi?",
                "bot",
                LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        );
        return ResponseEntity.ok(greeting);
    }
}