package com.zeus.chatme.chatSection;

import androidx.annotation.Keep;

@Keep
public class Chat {

    private String sender;
    private String receiver;
    private String message, messageURL, messageId;
    private Long timestamp;
    private boolean isseen;
    private String type;
    private int uneadMessagesCount;

    public Chat(String sender, String receiver, String message, Long timestamp, boolean isseen, String type) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.timestamp = timestamp;
        this.isseen = isseen;
        this.type = type;
    }

    public Chat(String sender, String receiver, String message,String type) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.type = type;
    }

    public Chat() {

    }

    public int getUneadMessagesCount() {
        return uneadMessagesCount;
    }

    public void setUneadMessagesCount(int uneadMessagesCount) {
        this.uneadMessagesCount = uneadMessagesCount;
    }

    public String getMessageURL() {
        return messageURL;
    }

    public void setMessageURL(String messageURL) {
        this.messageURL = messageURL;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isIsseen() {
        return isseen;
    }

    public void setIsseen(boolean isseen) {
        this.isseen = isseen;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

