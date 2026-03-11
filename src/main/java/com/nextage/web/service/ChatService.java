package com.nextage.web.service;

import java.util.List;
import org.springframework.stereotype.Service;

// 이 부분이 중요합니다! DTO 경로가 맞는지 확인하세요.
import com.nextage.web.domain.ChatMessageDTO;
import com.nextage.web.domain.ChatRoomDTO;
import com.nextage.web.mapper.ChatMapper;

@Service
public class ChatService {
    
    private final ChatMapper chatMapper;

    
    public ChatService(ChatMapper chatMapper) {
        this.chatMapper = chatMapper;
    }

    
    public List<ChatRoomDTO> getMyRooms(Long customerId) {
        return chatMapper.selectMyChatRooms(customerId);
    }

    
    public List<ChatMessageDTO> getRoomMessages(Long roomId) {
        return chatMapper.selectMessages(roomId);
    }
}