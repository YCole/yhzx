package com.gome.usercenter.module;

/**
 * Created by jianfeng.xue on 2017/7/13.
 */

public class VersionInfo {

    private String mResCode;
    private String mResMsg;

    private String mExperienceRequest;

    private String mPublicDesc;
    private String mProjectName;
    private String mPublicDate;
    private String mPublicVersionNo;
    private String mSourceVersionNo;
    private String mTargetVersionNo;

    private long mUpdatePackageSize;

    public void setResCode(String mResCode) {
        this.mResCode = mResCode;
    }

    public void setResMsg(String mResMsg) {
        this.mResMsg = mResMsg;
    }

    public void setPublicDesc(String mPublicDesc) {
        this.mPublicDesc = mPublicDesc;
    }

    public void setProjectName(String mProjectName) {
        this.mProjectName = mProjectName;
    }

    public void setPublicDate(String mPublicDate) {
        this.mPublicDate = mPublicDate;
    }

    public void setPublicVersionNo(String mPublicVersionNo) {
        this.mPublicVersionNo = mPublicVersionNo;
    }

    public void setSourceVersionNo(String mSourceVersionNo) {
        this.mSourceVersionNo = mSourceVersionNo;
    }

    public void setTargetVersionNo(String mTargetVersionNo) {
        this.mTargetVersionNo = mTargetVersionNo;
    }

    public void setUpdatePackageSize(long updatePackageSize) {
        this.mUpdatePackageSize = updatePackageSize;
    }

    public String getExperienceRequest() {
        return mExperienceRequest;
    }

    public void setExperienceRequest(String mExperienceRequest) {
        this.mExperienceRequest = mExperienceRequest;
    }

    public String getResCode() {
        return mResCode;
    }

    public String getResMsg() {
        return mResMsg;
    }

    public String getPublicDesc() {
        return mPublicDesc;
    }

    public String getProjectName() {
        return mProjectName;
    }

    public String getPublicDate() {
        return mPublicDate;
    }

    public String getPublicVersionNo() {
        return mPublicVersionNo;
    }

    public String getSourceVersionNo() {
        return mSourceVersionNo;
    }

    public String getTargetVersionNo() {
        return mTargetVersionNo;
    }

    public long getUpdatePackageSize() {
        return mUpdatePackageSize;
    }
}
