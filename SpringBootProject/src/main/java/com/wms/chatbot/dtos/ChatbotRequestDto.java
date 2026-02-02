package com.wms.chatbot.dtos;

import lombok.Data;

/**
 * DTO for chatbot requests.
 */
@Data
public class ChatbotRequestDto {
    private String question;
    private Long userId;
}
