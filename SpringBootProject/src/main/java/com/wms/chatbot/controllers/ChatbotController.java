package com.wms.chatbot.controllers;

import com.wms.chatbot.dtos.ChatbotRequestDto;
import com.wms.chatbot.dtos.ChatbotResponseDto;
import com.wms.chatbot.services.ChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for chatbot endpoints.
 */
@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {
    private final ChatbotService chatbotService;

    /**
     * POST endpoint for chatbot queries.
     * @param dto ChatbotRequestDto
     * @return ChatbotResponseDto
     */
    @PostMapping("/ask")
    public ResponseEntity<ChatbotResponseDto> ask(@RequestBody ChatbotRequestDto dto) {
        return ResponseEntity.ok(chatbotService.processQuery(dto));
    }
}
