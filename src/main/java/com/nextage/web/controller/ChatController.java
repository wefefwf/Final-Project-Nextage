package com.nextage.web.controller;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.nextage.web.domain.ChatMessageDTO;
import com.nextage.web.domain.ChatRoomDTO;
import com.nextage.web.service.ChatService;
import com.nextage.web.userDetails.BusinessUserDetails;
import com.nextage.web.userDetails.CustomerUserDetails;

import lombok.extern.slf4j.Slf4j;

@Slf4j
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
        Long myId = null; String userType = null; String viewName = null;
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_BADMIN") || a.getAuthority().equals("BADMIN"));

        if (isAdmin) {
            userType = "BADMIN"; viewName = "views/chat/business-chat"; myId = 0L;
        } else if (principal instanceof CustomerUserDetails) {
            myId = ((CustomerUserDetails) principal).getCustomer().getCustomerId(); userType = "CUSTOMER"; viewName = "views/chat/customer-chat";
        } else if (principal instanceof BusinessUserDetails) {
            myId = ((BusinessUserDetails) principal).getBusiness().getBusinessId(); userType = "BUSINESS"; viewName = "views/chat/business-chat";
        } else { return "redirect:/login"; }

        List<ChatRoomDTO> myRooms = isAdmin ? chatService.getAllRooms() : chatService.getMyRooms(myId, userType);
        if (roomId != null) {
        	if (!"BADMIN".equals(userType)) {
                chatService.updateReadStatus(roomId, userType);
            }
            model.addAttribute("messages", chatService.getRoomMessagesPaged(roomId, 0));
            model.addAttribute("currentRoomId", roomId);
        }
        model.addAttribute("myRooms", myRooms);
        model.addAttribute("myId", myId);
        model.addAttribute("userType", userType);
        return viewName;
    }

    @GetMapping("/chat/messages/paging")
    @ResponseBody
    public ResponseEntity<List<ChatMessageDTO>> getMessagesPaged(@RequestParam("roomId") Long roomId, @RequestParam(value = "page", defaultValue = "0") int page) {
        return ResponseEntity.ok(chatService.getRoomMessagesPaged(roomId, page));
    }

    @PostMapping("/chat/read/{roomId}")
    @ResponseBody
    public ResponseEntity<Void> readRoom(@PathVariable("roomId") Long roomId, @RequestParam("userType") String userType) {
        chatService.updateReadStatus(roomId, userType);
        return ResponseEntity.ok().build();
    }

    @MessageMapping("/chat/send")
    public void sendMessage(ChatMessageDTO message) {
        if (message.getMessageType() == null) message.setMessageType("TEXT");
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        message.setSendAt(now);
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
            String uploadDir = "D:/nextageImage/";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();
            String savedFilename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            file.transferTo(new File(uploadDir + savedFilename));
            return ResponseEntity.ok("/images/" + savedFilename);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/chat/global-info")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getGlobalInfo(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Object principal = authentication.getPrincipal();
        Long myId = null; String userType = null;
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_BADMIN") || a.getAuthority().equals("BADMIN"));
        if (isAdmin) { userType = "BADMIN"; myId = 0L;
        } else if (principal instanceof CustomerUserDetails) { myId = ((CustomerUserDetails) principal).getCustomer().getCustomerId(); userType = "CUSTOMER";
        } else if (principal instanceof BusinessUserDetails) { myId = ((BusinessUserDetails) principal).getBusiness().getBusinessId(); userType = "BUSINESS";
        }
        List<ChatRoomDTO> rooms = isAdmin ? chatService.getAllRooms() : chatService.getMyRooms(myId, userType);
        int totalUnread = rooms.stream().mapToInt(ChatRoomDTO::getUnreadCount).sum();
        Map<String, Object> result = new HashMap<>();
        result.put("myId", myId); result.put("userType", userType); result.put("unreadCount", totalUnread);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/chat/mini/rooms")
    @ResponseBody
    public ResponseEntity<List<ChatRoomDTO>> getMiniRooms(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Object principal = authentication.getPrincipal();
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_BADMIN") || a.getAuthority().equals("BADMIN"));
        if (isAdmin) return ResponseEntity.ok(chatService.getAllRooms());
        Long myId = null; String userType = null;
        if (principal instanceof CustomerUserDetails) { myId = ((CustomerUserDetails) principal).getCustomer().getCustomerId(); userType = "CUSTOMER";
        } else if (principal instanceof BusinessUserDetails) { myId = ((BusinessUserDetails) principal).getBusiness().getBusinessId(); userType = "BUSINESS";
        }
        return ResponseEntity.ok(chatService.getMyRooms(myId, userType));
    }

    @GetMapping("/chat/mini/messages")
    @ResponseBody
    public ResponseEntity<List<ChatMessageDTO>> getMiniMessages(@RequestParam("roomId") Long roomId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(chatService.getRoomMessages(roomId));
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
    
    @GetMapping("/chat/unread/{roomId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUnreadCount(
            @PathVariable("roomId") Long roomId,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated())
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Object principal = authentication.getPrincipal();
        String userType;
        if (principal instanceof CustomerUserDetails) {
            userType = "CUSTOMER";
        } else if (principal instanceof BusinessUserDetails) {
            userType = "BUSINESS";
        } else {
            userType = "BUSINESS";
        }

        int count = chatService.getUnreadCount(roomId, userType);
        return ResponseEntity.ok(Map.of("count", count));
    }
}