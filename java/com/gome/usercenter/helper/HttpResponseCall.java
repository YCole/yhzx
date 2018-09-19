package com.gome.usercenter.helper;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class HttpResponseCall<T> {
    Handler mHandler;

    public HttpResponseCall(Context context, final HttpCallbackModelListener listener) {
        Looper looper = context.getMainLooper();
        mHandler = new Handler(looper) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if ( msg.what == 0 ) {
                    listener.onFinish(msg.obj);
                } else if ( msg.what == 1 ) {
                    listener.onError(( Exception ) msg.obj);
                }
            }
        };
    }


    public HttpResponseCall(Context context, final HttpCallbackStringListener listener) {
        Looper looper = context.getMainLooper();
        mHandler = new Handler(looper) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if ( msg.what == 0 ) {
                    listener.onFinish(msg.obj.toString());
                } else if ( msg.what == 1 ) {
                    listener.onError(( Exception ) msg.obj);
                }
            }
        };
    }

    public void doSuccess(T response) {
        Message message = Message.obtain();
        message.obj = response;
        message.what = 0;
        mHandler.sendMessage(message);
    }

    public void doFail(Exception e) {
        Message message = Message.obtain();
        message.obj = e;
        message.what = 1;
        mHandler.sendMessage(message);
    }
}
