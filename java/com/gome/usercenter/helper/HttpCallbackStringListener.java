package com.gome.usercenter.helper;

public interface HttpCallbackStringListener {
    void onFinish(String response);
    void onError(Exception e);
}
