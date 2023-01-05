package com.zeus.chatme.notifications;

import androidx.annotation.Keep;

@Keep
public class Data {
    private String user;
    private int icon;
    private String body;
    private String title;
    private String sent;
    private String type;

    public Data() {
    }

    public Data(String user, int icon, String body, String title, String sent, String type) {
        this.user = user;
        this.icon = icon;
        this.body = body;
        this.title = title;
        this.sent = sent;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSent() {
        return sent;
    }

    public void setSent(String sent) {
        this.sent = sent;
    }
}
