package com.nextage.web.controller;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

// ⚠️ 이 경로들이 실제 파일 위치와 정확히 맞아야 에러가 사라집니다.
import com.nextage.web.domain.ChatMessageDTO;
import com.nextage.web.domain.ChatRoomDTO;
import com.nextage.web.service.ChatService;

@Controller
public class ChatController {
    private final ChatService chatService;

    
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/chat")
    public String chatPage(@RequestParam(value="roomId", required=false) Long roomId, Model model) {
        Long myId = 101L; 
        
        
        model.addAttribute("myRooms", chatService.getMyRooms(myId));
        model.addAttribute("myId", myId);
        
        if(roomId != null) {
            
            model.addAttribute("messages", chatService.getRoomMessages(roomId));
            model.addAttribute("currentRoomId", roomId);
        }
        
        return "views/chat/chat"; 
    }
}