package com.nextage.web.domain.openai;

public class CsChatRequest {
    private String message;
    private String userType;

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }
}