package com.example.bakery.dto.gemini;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List; // Import List

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotMessage {
    private String text;
    private String sender; // "user" or "bot"
    private String timestamp; // Ví dụ: "HH:mm:ss"
}