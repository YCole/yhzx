package com.gome.usercenter.module;

/**
 * Created by dongzq on 2017/7/11.
 */

public class ExperienceVersionInfo {

    enum ExperienceVersionApplyState {
        applying, applied
    }

    private ExperienceVersionApplyState mState;

    public void setState(ExperienceVersionApplyState mState) {
        this.mState = mState;
    }

    public ExperienceVersionApplyState getState() {
        return this.mState;
    }
}
