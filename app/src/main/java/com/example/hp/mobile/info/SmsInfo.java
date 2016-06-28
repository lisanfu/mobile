package com.example.hp.mobile.info;

/**
 * Created by hp on 2016/6/24.
 */
public class SmsInfo {
    private String address;
    private String date;
    private String body;
    private String state;

    public void setAddress(String address) {
        this.address = address;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getAddress() {
        return address;
    }

    public String getDate() {
        return date;
    }

    public String getBody() {
        return body;
    }

    public String getState() {
        return state;
    }
}
