package com.nextage.web.domain.openai;

import java.util.List;

public class ChatGptResponse {
    private List<Choice> choices;
    public List<Choice> getChoices() { return choices; }
    public void setChoices(List<Choice> choices) { this.choices = choices; }
    public static class Choice {
        private ChatGptMessage message;
        public ChatGptMessage getMessage() { return message; }
        public void setMessage(ChatGptMessage message) { this.message = message; }
    }
}