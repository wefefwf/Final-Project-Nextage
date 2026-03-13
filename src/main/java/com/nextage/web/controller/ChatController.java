package com.nextage.web.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import com.nextage.web.domain.ChatMessageDTO;
import com.nextage.web.domain.ChatRoomDTO;
import com.nextage.web.service.ChatService;
import com.nextage.web.userDetails.BusinessUserDetails;
import com.nextage.web.userDetails.CustomerUserDetails;

@Controller
public class ChatController {
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(ChatService chatService, SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping("/chat")
    public String chatPage(
            @RequestParam(value="roomId", required=false) Long roomId,
            Authentication authentication,
            Model model) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login"; 
        }

        Object principal = authentication.getPrincipal();
        Long myId = null;
        String viewName = null;
        String userType = null;

        if (principal instanceof CustomerUserDetails) {
            myId = ((CustomerUserDetails) principal).getCustomer().getCustomerId();
            viewName = "views/chat/customer-chat";
            userType = "CUSTOMER";
        } else if (principal instanceof BusinessUserDetails) {
            myId = ((BusinessUserDetails) principal).getBusiness().getBusinessId();
            viewName = "views/chat/business-chat";
            userType = "BUSINESS";
        } else {
            return "redirect:/login";
        }
        
        List<ChatRoomDTO> myRooms = chatService.getMyRooms(myId, userType);
        
        model.addAttribute("myRooms", myRooms);
        model.addAttribute("myId", myId);
        model.addAttribute("userType", userType);
        
        if(roomId != null) {
            boolean isAuthorized = myRooms.stream()
                    .anyMatch(room -> room.getRoomId().equals(roomId));
            
            if (!isAuthorized) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied");
            }
            
            model.addAttribute("messages", chatService.getRoomMessages(roomId));
            model.addAttribute("currentRoomId", roomId);
        }
        
        return viewName; 
    }

    @MessageMapping("/chat/send")
    public void sendMessage(ChatMessageDTO message) {
        chatService.saveMessage(message);
        messagingTemplate.convertAndSend("/sub/chat/room/" + message.getRoomId(), message);
    }
}