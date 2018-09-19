package com.gome.usercenter;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.gome.usercenter.activity.HomeActivity;
import com.gome.usercenter.utils.AccountUtils;
import com.gome.usercenter.utils.ActivityUtils;
import com.gome.usercenter.utils.Constants;
import com.gome.usercenter.receiver.UserCenterAccountReceiver;

import java.io.File;

/**
 * Created by jianfeng.xue on 2017/7/21.
 */

public class UserCenterApplication extends Application implements
            Application.ActivityLifecycleCallbacks {
    private static final String TAG = Constants.TAG_HEADER + "UserCenterApplication";
    private static Context context;
    private boolean mMainActivityActive;
    private UserCenterAccountReceiver accountReceiver = new UserCenterAccountReceiver();

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(this);
        context = getApplicationContext();
        IntentFilter accountFilter = new IntentFilter();
        accountFilter.addAction(Constants.ACTION_GOME_ACCOUNT_LOGOUT);
        accountFilter.addAction(Constants.ACTION_GOME_ACCOUNT_LOGIN);
        accountFilter.addAction(Constants.ACTION_GOME_ACCOUNT_UPDATE_INFO);
        getApplicationContext().registerReceiver(accountReceiver, accountFilter);
    }

    public static Context getContext() {
            return context;
    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (activity instanceof HomeActivity) {
            mMainActivityActive = true;
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {
        if (activity instanceof HomeActivity) {
            mMainActivityActive = false;
        }
    }

    public boolean isMainActivityActive() {
            return mMainActivityActive;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onActivityResumed(Activity activity) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onActivityPaused(Activity activity) {
        // TODO Auto-generated method stub
        ActivityUtils.setToastOff();
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        // TODO Auto-generated method stub
        if (activity instanceof HomeActivity) {
            ActivityUtils.setToastOff();
            AccountUtils.clearGomeAccountValue(getApplicationContext());
            ActivityUtils.deleteCacheFile(getApplicationContext(), Constants.FILE_CACHE_FEEDBACK_HISTORY);
            ActivityUtils.deleteCacheFile(getApplicationContext(), Constants.FILE_CACHE_AFTER_SALE_ADDRESS);
            clearWebViewCache();
        }
    }

    private final String APP_CACAHE_DIRNAME = "/app_webview";
    public void clearWebViewCache(){
        try {
            deleteDatabase("webview.db");
            deleteDatabase("webviewCache.db");
        } catch (Exception e) {
            e.printStackTrace();
        }

        File appCacheDir = new File((getFilesDir().getAbsolutePath()+ APP_CACAHE_DIRNAME).replace("/files", ""));
        File webviewCacheDir = new File(getCacheDir().getAbsolutePath()+"/org.chromium.android_webview");

        if(webviewCacheDir.exists()){
            deleteFile(webviewCacheDir);
        }
        if(appCacheDir.exists()){
            deleteFile(appCacheDir);
        }
    }

    public void deleteFile(File file) {
        if(file.exists()) {
            if(file.isFile()) {
                file.delete();
            }else if(file.isDirectory()) {
                File files[] = file.listFiles();
                for(int i = 0; i < files.length; i++) {
                    deleteFile(files[i]);
                }
            }
            file.delete();
        }else{
            Log.e(TAG, "delete file no exists "+ file.getAbsolutePath());
        }
    }

}

