package com.gome.usercenter.module;

/**
 * Created by dongzq on 2017/7/11.
 */

public class MyIssueFeedbackInfo {
    String mIssueTitle;
    String mModule;
    String mIssueDetail;
    String mContactInfo;

    public MyIssueFeedbackInfo(String issueTitle, String module, String issueDetail, String contactInfo) {
        this.mIssueTitle = issueTitle;
        this.mModule = module;
        this.mIssueDetail = issueDetail;
        this.mContactInfo = contactInfo;
    }

    public void setIssueTtile(String mIssueTtile) {
        this.mIssueTitle = mIssueTtile;
    }

    public void setIssueDetail(String mIssueDetail) {
        this.mIssueDetail = mIssueDetail;
    }

    public void setModule(String mModule) {
        this.mModule = mModule;
    }

    public void setContactInfo(String mContactInfo) {
        this.mContactInfo = mContactInfo;
    }

    public String getIssueTtile() {
        return mIssueTitle;
    }

    public String getIssueDetail() {
        return mIssueDetail;
    }

    public String getModule() {
        return mModule;
    }

    public String getContactInfo() {
        return mContactInfo;
    }


}
