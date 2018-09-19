package com.gome.usercenter.module;

/**
 * Created by jianfeng.xue on 2017/5/8.
 */

public class FeedbackInfo {
    private String imei;
    private String version;
    private String date;
    private String userId;
    private String userName;

    public FeedbackInfo(String imei, String version, String date, String userId, String userName) {
        this.imei = imei;
        this.version = version;
        this.date = date;
        this.userId = userId;
        this.userName = userName;
    }

    public FeedbackInfo() {
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }


}
