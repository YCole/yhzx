package com.gome.usercenter.module;

/**
 * Created by jianfeng.xue on 2017/7/26.
 */

public class HistoryFeedbackInfo {

    public String messageId;
    public String feedModule;
    public String feedTitle;
    public String feedContent;
    public String feedImgs;
    public String feedTime;
    public String updateTime;
    public String feedHandleStatus;
    public String feedHandleContent;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getFeedModule() {
        return feedModule;
    }

    public void setFeedModule(String feedModule) {
        this.feedModule = feedModule;
    }

    public String getFeedHandleStatus() {
        return feedHandleStatus;
    }

    public void setFeedHandleStatus(String feedHandleStatus) {
        this.feedHandleStatus = feedHandleStatus;
    }

    public String getFeedHandleContent() {
        return feedHandleContent;
    }

    public void setFeedHandleContent(String feedHandleContent) {
        this.feedHandleContent = feedHandleContent;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getFeedTime() {
        return feedTime;
    }

    public void setFeedTime(String feedTime) {
        this.feedTime = feedTime;
    }

    public String getFeedTitle() {
        return feedTitle;
    }

    public void setFeedTitle(String feedTitle) {
        this.feedTitle = feedTitle;
    }

    public String getFeedContent() {
        return feedContent;
    }

    public void setFeedContent(String feedContent) {
        this.feedContent = feedContent;
    }

    public String getFeedImgs() {
        return feedImgs;
    }

    public void setFeedImgs(String feedImgs) {
        this.feedImgs = feedImgs;
    }

}
