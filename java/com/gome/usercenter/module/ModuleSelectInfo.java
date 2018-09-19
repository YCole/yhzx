package com.gome.usercenter.module;

/**
 * Created by jianfeng.xue on 2017/8/1.
 */

public class ModuleSelectInfo {
    private boolean isChecked;
    private String msg = "";

    public ModuleSelectInfo(){

    }
    public ModuleSelectInfo(boolean isCheched) {
        this.isChecked = isCheched;
    }
    public boolean isChecked() {
        return isChecked;
    }
    public void setChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }
    public String getMsg() {
        return msg;
    }
    public void setMsg(String msg) {
        this.msg = msg;
    }

}
