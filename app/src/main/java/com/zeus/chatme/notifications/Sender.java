package com.zeus.chatme.notifications;

import androidx.annotation.Keep;

@Keep
public class Sender {
    private Data data;
    private String to;

    public Sender(Data data, String to) {
        this.data = data;
        this.to = to;
    }
}
