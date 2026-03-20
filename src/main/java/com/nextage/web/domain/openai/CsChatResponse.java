package com.nextage.web.domain.openai;

public class CsChatResponse {
    private String reply;
    public CsChatResponse() {} // JSON 변환을 위해 추가됨
    public CsChatResponse(String reply) { this.reply = reply; }
    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }
}