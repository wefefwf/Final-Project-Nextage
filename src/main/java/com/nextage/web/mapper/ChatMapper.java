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
}