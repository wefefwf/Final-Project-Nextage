package com.nextage.web.controller;

import com.nextage.web.domain.openai.CsChatRequest;
import com.nextage.web.domain.openai.CsChatResponse;
import com.nextage.web.service.ChatGptService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cs")
public class CsChatController {

    private final ChatGptService chatGptService;

    public CsChatController(ChatGptService chatGptService) {
        this.chatGptService = chatGptService;
    }

    @PostMapping("/chat")
    public ResponseEntity<CsChatResponse> chat(@RequestBody CsChatRequest request) {
        String reply = chatGptService.getChatResponse(request.getMessage(), request.getUserType());
        return ResponseEntity.ok(new CsChatResponse(reply));
    }
}