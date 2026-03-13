package com.nextage.web.service;

import java.util.List;
import org.springframework.stereotype.Service;

import com.nextage.web.domain.ChatMessageDTO;
import com.nextage.web.domain.ChatRoomDTO;
import com.nextage.web.mapper.ChatMapper;

@Service
public class ChatService {
    
    private final ChatMapper chatMapper;

    public ChatService(ChatMapper chatMapper) {
        this.chatMapper = chatMapper;
    }

    public List<ChatRoomDTO> getMyRooms(Long myId, String userType) {
        return chatMapper.selectMyChatRooms(myId, userType);
    }

    public List<ChatMessageDTO> getRoomMessages(Long roomId) {
        return chatMapper.selectMessages(roomId);
    }

    public void saveMessage(ChatMessageDTO message) {
        chatMapper.insertMessage(message);
    }
}