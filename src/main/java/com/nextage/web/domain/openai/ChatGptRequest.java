package com.nextage.web.domain.openai;

import java.util.List;

public class ChatGptRequest {
    private String model;
    private List<ChatGptMessage> messages;

    public ChatGptRequest() {}

    public ChatGptRequest(String model, List<ChatGptMessage> messages) {
        this.model = model;
        this.messages = messages;
    }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public List<ChatGptMessage> getMessages() { return messages; }
    public void setMessages(List<ChatGptMessage> messages) { this.messages = messages; }
}