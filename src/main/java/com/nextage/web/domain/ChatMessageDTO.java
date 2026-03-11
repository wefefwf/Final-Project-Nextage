package com.nextage.web.domain;

public class ChatMessageDTO {
    private Long messageId;
    private Long roomId;
    private Long senderId;
    private String senderType;
    private String content;
    private String sendAt;

    public Long getMessageId() { return messageId; }
    public void setMessageId(Long messageId) { this.messageId = messageId; }
    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }
    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }
    public String getSenderType() { return senderType; }
    public void setSenderType(String senderType) { this.senderType = senderType; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getSendAt() { return sendAt; }
    public void setSendAt(String sendAt) { this.sendAt = sendAt; }
}