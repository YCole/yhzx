package com.gome.usercenter.helper;

public interface HttpCallbackModelListener<T> {
    void onFinish(T response);
    void onError(Exception e);
}
