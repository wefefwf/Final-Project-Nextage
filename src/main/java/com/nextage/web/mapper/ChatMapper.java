package com.nextage.web.mapper;

import com.nextage.web.domain.ChatMessageDTO;
import com.nextage.web.domain.ChatRoomDTO;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface ChatMapper {
    List<ChatRoomDTO> selectMyChatRooms(Long customerId);
    List<ChatMessageDTO> selectMessages(Long roomId);
}