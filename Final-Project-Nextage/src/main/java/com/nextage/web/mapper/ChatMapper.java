package com.nextage.web.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.nextage.web.domain.ChatMessageDTO;
import com.nextage.web.domain.ChatRoomDTO;

@Mapper
public interface ChatMapper {
    List<ChatRoomDTO> selectMyChatRooms(@Param("myId") Long myId, @Param("userType") String userType);
    List<ChatMessageDTO> selectMessages(Long roomId);
    void insertMessage(ChatMessageDTO message);
    void updateMessageReadStatus(@Param("roomId") Long roomId, @Param("userType") String userType);
    ChatRoomDTO selectRoomById(Long roomId);
    List<ChatMessageDTO> selectPastMessages(@Param("roomId") Long roomId, @Param("lastMessageId") Long lastMessageId);
    int selectTotalUnreadCount(@Param("myId") Long myId, @Param("userType") String userType);
    
    Long selectRoomByBidId(Long bidId);
    void insertChatRoom(ChatRoomDTO chatRoomDTO);
}