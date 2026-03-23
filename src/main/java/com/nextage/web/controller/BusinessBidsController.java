package com.nextage.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/business/bids")
public class BusinessBidsController {

    @GetMapping("/list")
    public String myBids() {
        return "views/bids/business-mypageBids";
    }
}
