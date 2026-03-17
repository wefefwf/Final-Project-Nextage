package com.nextage.web.controller;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
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
    public String chatPage(@RequestParam(value="roomId", required=false) Long roomId, Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) return "redirect:/login";

        Object principal = authentication.getPrincipal();
        Long myId = null;
        String userType = null;
        String viewName = null;

        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_BADMIN"));

        if (isAdmin) {
            userType = "BADMIN";
            viewName = "views/chat/business-chat";
            myId = 0L;
        } else if (principal instanceof CustomerUserDetails) {
            myId = ((CustomerUserDetails) principal).getCustomer().getCustomerId();
            userType = "CUSTOMER";
            viewName = "views/chat/customer-chat";
        } else if (principal instanceof BusinessUserDetails) {
            myId = ((BusinessUserDetails) principal).getBusiness().getBusinessId();
            userType = "BUSINESS";
            viewName = "views/chat/business-chat";
        } else {
            return "redirect:/login";
        }

        List<ChatRoomDTO> myRooms = chatService.getMyRooms(myId, userType);
        if (roomId != null) {
            if (!isAdmin) {
                boolean isAuthorized = myRooms.stream().anyMatch(room -> room.getRoomId().equals(roomId));
                if (!isAuthorized) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
                chatService.updateReadStatus(roomId, userType);
            }
            model.addAttribute("messages", chatService.getRoomMessages(roomId));
            model.addAttribute("currentRoomId", roomId);
        }
        model.addAttribute("myRooms", myRooms);
        model.addAttribute("myId", myId);
        model.addAttribute("userType", userType);
        return viewName;
    }

    @PostMapping("/chat/get-or-create")
    @ResponseBody
    public ResponseEntity<Long> getOrCreateRoom(@RequestBody ChatRoomDTO chatRoomDTO, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        ChatRoomDTO existingRoom = chatService.getRoomByBidId(chatRoomDTO.getBidId());
        if (existingRoom != null) return ResponseEntity.ok(existingRoom.getRoomId());
        chatService.createChatRoom(chatRoomDTO);
        return ResponseEntity.ok(chatRoomDTO.getRoomId());
    }

    @MessageMapping("/chat/send")
    public void sendMessage(ChatMessageDTO message) {
        if (message.getMessageType() == null) message.setMessageType("TEXT");
        chatService.saveMessage(message);
        messagingTemplate.convertAndSend("/sub/chat/room/" + message.getRoomId(), message);
        ChatRoomDTO room = chatService.getRoomById(message.getRoomId());
        if (room != null) {
            String receiverType = "CUSTOMER".equals(message.getSenderType()) ? "BUSINESS" : "CUSTOMER";
            Long receiverId = "CUSTOMER".equals(message.getSenderType()) ? room.getBusinessId() : room.getCustomerId();
            messagingTemplate.convertAndSend("/sub/chat/user/" + receiverType + "/" + receiverId, message);
        }
    }

    @PostMapping("/chat/upload")
    @ResponseBody
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return ResponseEntity.badRequest().body("");
        try {
            String uploadDir = System.getProperty("user.dir") + "/src/main/resources/static/images/chat/";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();
            String savedFilename = UUID.randomUUID().toString() + file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            file.transferTo(new File(uploadDir + savedFilename));
            return ResponseEntity.ok("/images/chat/" + savedFilename);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}