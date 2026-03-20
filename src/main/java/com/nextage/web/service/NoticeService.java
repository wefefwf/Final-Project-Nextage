package com.nextage.web.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.nextage.web.domain.NoticeDTO;
import com.nextage.web.mapper.NoticeMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeMapper noticeMapper;

    public List<NoticeDTO> getVisibleNotices(String role) {
        return noticeMapper.findAllVisible(role);
    }

    public List<NoticeDTO> getAllNotices() {
        return noticeMapper.findAll();
    }

    public NoticeDTO getNoticeById(Long noticeId) {
        return noticeMapper.findById(noticeId);
    }

    public void write(NoticeDTO notice) {
        noticeMapper.insert(notice);
    }

    public void edit(NoticeDTO notice) {
        noticeMapper.update(notice);
    }

    public void remove(Long noticeId) {
        noticeMapper.delete(noticeId);
    }
}