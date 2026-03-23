package com.nextage.web.mapper;

import com.nextage.web.domain.NoticeDTO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface NoticeMapper {
   List<NoticeDTO> findAllVisible(String var1);

   List<NoticeDTO> findAll();

   List<NoticeDTO> getSearchList(@Param("target") String var1, @Param("keyword") String var2);

   NoticeDTO findById(Long var1);

   void insert(NoticeDTO var1);

   void update(NoticeDTO var1);

   void delete(Long var1);
}
