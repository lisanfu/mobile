package com.example.hp.mobile.info;

/**
 * Created by hp on 2016/6/24.
 */
public class PhoneInfo {
    private String phoneNum;
    private String date;
    private String state;
   public void setPhoneNum(String phoneNum)
    {
        this.phoneNum=phoneNum;
    }
    public String getPhoneNum()
    {
        return phoneNum;
    }
    public void setDate(String date)
    {
        this.date=date;
    }
    public String getDate()
    {
        return date;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getState() {

        return state;
    }
}
