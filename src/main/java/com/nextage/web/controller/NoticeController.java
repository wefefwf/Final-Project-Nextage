package com.nextage.web.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.nextage.web.domain.NoticeDTO;
import com.nextage.web.service.NoticeService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;


    private String getUserType(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "default"; 
        }
        boolean isBusiness = authentication.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().startsWith("ROLE_B"));
        return isBusiness ? "business" : "customer";
    }


 
    // 1. 공지사항 목록

    @GetMapping("/notice")
    public String noticeList(Authentication authentication, Model model) {
        model.addAttribute("userType", getUserType(authentication));
        model.addAttribute("noticeList", noticeService.getAllNotices());
   

        return "views/notice/notice";
    }
    
    @GetMapping("/notice/detail/{noticeId}")
    public String customerNoticeDetail(Authentication authentication,@PathVariable("noticeId") Long noticeId, Model model) {
    	model.addAttribute("userType", getUserType(authentication));
        model.addAttribute("notice", noticeService.getNoticeById(noticeId));
        return "views/notice/notice-detail";
    }
    // 2. 공지사항 글 작성 
    @PreAuthorize("hasAnyRole('CADMIN', 'BADMIN')")
    @GetMapping("/notice/write")
    public String writeForm(Authentication authentication, Model model) {
        model.addAttribute("userType", getUserType(authentication));
        return "views/notice/notice-write"; 
    }

    @PreAuthorize("hasAnyRole('CADMIN', 'BADMIN')")
    @PostMapping("/notice/write")
    public String writeProc(NoticeDTO notice) {
        noticeService.write(notice);
        
        return "redirect:/notice"; 
    }

    // 3. 공지사항 글 수정
    @PreAuthorize("hasAnyRole('CADMIN', 'BADMIN')")
    @GetMapping("/notice/edit/{noticeId}")
    public String editForm(@PathVariable("noticeId") Long noticeId, Authentication authentication, Model model) {
        model.addAttribute("userType", getUserType(authentication));
        
        // 기존 글 내용 불러오기
        model.addAttribute("notice", noticeService.getNoticeById(noticeId));
        
        return "views/notice/notice-edit";
    }

    @PreAuthorize("hasAnyRole('CADMIN', 'BADMIN')")
    @PostMapping("/notice/edit/{noticeId}")
    public String editProc(@PathVariable("noticeId") Long noticeId, NoticeDTO notice) {
        notice.setNoticeId(noticeId);
        noticeService.edit(notice);
        
        return "redirect:/notice";
    }

   
    // 4. 공지사항 글 삭제 
    @PreAuthorize("hasAnyRole('CADMIN', 'BADMIN')")
    @PostMapping("/notice/delete/{noticeId}")
    public String deleteProc(@PathVariable("noticeId") Long noticeId) {
        noticeService.remove(noticeId);
        
        return "redirect:/notice";
    }
}