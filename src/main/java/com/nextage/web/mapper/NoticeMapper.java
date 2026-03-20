package com.nextage.web.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.nextage.web.domain.NoticeDTO;

@Mapper
public interface NoticeMapper {
    List<NoticeDTO> findAllVisible(String role);
    List<NoticeDTO> findAll();
    NoticeDTO findById(Long noticeId);
    void insert(NoticeDTO notice);
    void update(NoticeDTO notice);
    void delete(Long noticeId);
}
