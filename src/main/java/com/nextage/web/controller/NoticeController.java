package com.nextage.web.controller;

import com.nextage.web.domain.NoticeDTO;
import com.nextage.web.service.NoticeService;
import lombok.Generated;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class NoticeController {
   private final NoticeService noticeService;

   private String getUserType(Authentication authentication) {
      if (authentication != null && authentication.isAuthenticated()) {
         boolean isBusiness = authentication.getAuthorities().stream().anyMatch((role) -> role.getAuthority().startsWith("ROLE_B"));
         return isBusiness ? "business" : "customer";
      } else {
         return "default";
      }
   }

   @GetMapping({"/notice"})
   public String noticeList(@RequestParam(name = "target",required = false,defaultValue = "") String target, @RequestParam(name = "keyword",required = false,defaultValue = "") String keyword, Authentication authentication, Model model) {
      model.addAttribute("userType", this.getUserType(authentication));
      model.addAttribute("noticeList", this.noticeService.getSearchList(target, keyword));
      model.addAttribute("selectedTarget", target);
      model.addAttribute("keyword", keyword);
      return "views/notice/notice";
   }

   @GetMapping({"/notice/detail/{noticeId}"})
   public String customerNoticeDetail(Authentication authentication, @PathVariable("noticeId") Long noticeId, Model model) {
      model.addAttribute("userType", this.getUserType(authentication));
      model.addAttribute("notice", this.noticeService.getNoticeById(noticeId));
      return "views/notice/notice-detail";
   }

   @PreAuthorize("hasAnyRole('CADMIN', 'BADMIN')")
   @GetMapping({"/notice/write"})
   public String writeForm(Authentication authentication, Model model) {
      model.addAttribute("userType", this.getUserType(authentication));
      model.addAttribute("notice", new NoticeDTO());
      return "views/notice/notice-form";
   }

   @PreAuthorize("hasAnyRole('CADMIN', 'BADMIN')")
   @PostMapping({"/notice/write"})
   public String writeProc(NoticeDTO notice) {
      this.noticeService.write(notice);
      return "redirect:/notice";
   }

   @PreAuthorize("hasAnyRole('CADMIN', 'BADMIN')")
   @GetMapping({"/notice/edit/{noticeId}"})
   public String editForm(@PathVariable("noticeId") Long noticeId, Authentication authentication, Model model) {
      model.addAttribute("userType", this.getUserType(authentication));
      model.addAttribute("notice", this.noticeService.getNoticeById(noticeId));
      return "views/notice/notice-form";
   }

   @PreAuthorize("hasAnyRole('CADMIN', 'BADMIN')")
   @PostMapping({"/notice/edit/{noticeId}"})
   public String editProc(@PathVariable("noticeId") Long noticeId, NoticeDTO notice) {
      notice.setNoticeId(noticeId);
      this.noticeService.edit(notice);
      return "redirect:/notice";
   }

   @PreAuthorize("hasAnyRole('CADMIN', 'BADMIN')")
   @PostMapping({"/notice/delete/{noticeId}"})
   public String deleteProc(@PathVariable("noticeId") Long noticeId) {
      this.noticeService.remove(noticeId);
      return "redirect:/notice";
   }

   @Generated
   public NoticeController(NoticeService noticeService) {
      this.noticeService = noticeService;
   }
}
