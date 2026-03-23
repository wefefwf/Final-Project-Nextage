package com.nextage.web.service;

import com.nextage.web.domain.NoticeDTO;
import com.nextage.web.mapper.NoticeMapper;
import java.util.List;
import lombok.Generated;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NoticeService {
   private final NoticeMapper noticeMapper;

   public List<NoticeDTO> getVisibleNotices(String role) {
      return this.noticeMapper.findAllVisible(role);
   }

   public List<NoticeDTO> getAllNotices() {
      return this.noticeMapper.findAll();
   }

   public NoticeDTO getNoticeById(Long noticeId) {
      return this.noticeMapper.findById(noticeId);
   }

   public void write(NoticeDTO notice) {
      this.noticeMapper.insert(notice);
   }

   public void edit(NoticeDTO notice) {
      this.noticeMapper.update(notice);
   }

   public void remove(Long noticeId) {
      this.noticeMapper.delete(noticeId);
   }

   @Transactional(
      readOnly = true
   )
   public List<NoticeDTO> getSearchList(String target, String keyword) {
      return this.noticeMapper.getSearchList(target, keyword);
   }

   @Generated
   public NoticeService(NoticeMapper noticeMapper) {
      this.noticeMapper = noticeMapper;
   }
}
