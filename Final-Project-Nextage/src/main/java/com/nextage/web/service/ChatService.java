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

    public void updateReadStatus(Long roomId, String userType) {
        chatMapper.updateMessageReadStatus(roomId, userType);
    }

    public int getTotalUnreadCount(Long myId, String userType) {
        return chatMapper.selectTotalUnreadCount(myId, userType);
    }

    public List<ChatMessageDTO> getPastMessages(Long roomId, Long lastMessageId) {
        return chatMapper.selectPastMessages(roomId, lastMessageId);
    }

    public ChatRoomDTO getRoomById(Long roomId) {
        return chatMapper.selectRoomById(roomId);
    }

    public ChatRoomDTO getRoomByBidId(Long bidId) {
        Long roomId = chatMapper.selectRoomByBidId(bidId);
        if (roomId == null) return null;
        return chatMapper.selectRoomById(roomId);
    }

    public void createChatRoom(ChatRoomDTO chatRoomDTO) {
        chatMapper.insertChatRoom(chatRoomDTO);
    }
}