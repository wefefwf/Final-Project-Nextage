package com.nextage.web.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.nextage.web.domain.ChatMessageDTO;
import com.nextage.web.domain.ChatRoomDTO;

@Mapper
public interface ChatMapper {
    List<ChatRoomDTO> findRoomsByUserId(@Param("userId") Long userId, @Param("userType") String userType);
    
    List<ChatRoomDTO> findAllRooms();

    List<ChatMessageDTO> findMessagesByRoomId(Long roomId);

    List<ChatMessageDTO> findMessagesByRoomIdPaged(@Param("roomId") Long roomId, @Param("limit") int limit, @Param("offset") int offset);

    void insertMessage(ChatMessageDTO message);

    ChatRoomDTO findRoomById(Long roomId);

    ChatRoomDTO findRoomByBidId(Long bidId);

    void insertChatRoom(ChatRoomDTO chatRoomDTO);

    void updateReadStatus(@Param("roomId") Long roomId, @Param("userType") String userType);
    
    int countUnread(@Param("roomId") Long roomId, @Param("userType") String userType);
}