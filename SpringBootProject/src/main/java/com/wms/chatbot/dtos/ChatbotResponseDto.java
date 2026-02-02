package com.wms.chatbot.dtos;

import lombok.Data;

/**
 * DTO for chatbot responses.
 */
@Data
public class ChatbotResponseDto {
    private String answer;
    private double confidence;
    private boolean fallbackToHuman;
}
