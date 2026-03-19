package com.nextage.web.service;

import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nextage.web.domain.ChatMessageDTO;
import com.nextage.web.domain.ChatRoomDTO;
import com.nextage.web.mapper.ChatMapper;

@Service
@Transactional
public class ChatService {
    private final ChatMapper chatMapper;

    public ChatService(ChatMapper chatMapper) {
        this.chatMapper = chatMapper;
    }

    public List<ChatRoomDTO> getMyRooms(Long userId, String userType) {
        return chatMapper.findRoomsByUserId(userId, userType);
    }

    public List<ChatRoomDTO> getAllRooms() {
        return chatMapper.findAllRooms();
    }

    public List<ChatMessageDTO> getRoomMessages(Long roomId) {
        return chatMapper.findMessagesByRoomId(roomId);
    }

    public List<ChatMessageDTO> getRoomMessagesPaged(Long roomId, int page) {
        int limit = 20;
        int offset = page * limit;
        List<ChatMessageDTO> messages = chatMapper.findMessagesByRoomIdPaged(roomId, limit, offset);
        Collections.reverse(messages);
        return messages;
    }

    public void saveMessage(ChatMessageDTO message) {
        chatMapper.insertMessage(message);
    }

    public ChatRoomDTO getRoomById(Long roomId) {
        return chatMapper.findRoomById(roomId);
    }

    public ChatRoomDTO getRoomByBidId(Long bidId) {
        return chatMapper.findRoomByBidId(bidId);
    }

    public void createChatRoom(ChatRoomDTO chatRoomDTO) {
        chatMapper.insertChatRoom(chatRoomDTO);
    }

    public void updateReadStatus(Long roomId, String userType) {
        chatMapper.updateReadStatus(roomId, userType);
    }
    
    public int getUnreadCount(Long roomId, String userType) {
        return chatMapper.countUnread(roomId, userType);
    }
}