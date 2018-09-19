package com.gome.usercenter.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dongzq on 2017/8/25.
 * Observer network state, so we can do something when network connected or disconnected
 */

public class NetworkStateObserver {

    private static final String TAG = Constants.TAG_HEADER + "NetworkStateObserver";
    private List<NetworkStateCallback> mCallbacks = new ArrayList<NetworkStateCallback>();
    private boolean mNetworkState = false;
    private static NetworkStateObserver sNetworkStateObserver;
    private Context mContext;

    private BroadcastReceiver mNetworkStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "action = " + intent.getAction());
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mCallbacks.size() == 0 || networkInfo == null) {
                Log.d(TAG, "reason : " + "callbacks.size = " + mCallbacks.size() + ", networkInfo = " + networkInfo);
                mNetworkState = false;
                return;
            }
            boolean oldState = mNetworkState;
            mNetworkState = networkInfo.isConnected();
            if (!oldState && mNetworkState) {
                for (NetworkStateCallback callback : mCallbacks) {
                    callback.onNetworkConnected();
                }
            } else if (oldState && !mNetworkState) {
                for (NetworkStateCallback callback : mCallbacks) {
                    callback.onNetworkDisconnected();
                }
            }
        }
    };

    public NetworkStateObserver(Context context) {
        super();
        mContext = context.getApplicationContext();
        ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        mNetworkState = mNetworkInfo == null ? false : mNetworkInfo.isConnected();
    }

    public static NetworkStateObserver newInstance(Context context) {
        if (sNetworkStateObserver == null) {
            sNetworkStateObserver = new NetworkStateObserver(context);
        }
        return sNetworkStateObserver;
    }

    /**
     * registerCallback
     * register BroadcastReceiver first if never register a callbackwe
     * @param callback
     */
    public void registerCallback(NetworkStateCallback callback) {
        if (mCallbacks.size() == 0) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            mContext.registerReceiver(mNetworkStateReceiver, filter);
        }
        mCallbacks.add(callback);
    }

    /**
     * unRegisterCallback
     * unregister BroadcastReceiver if there is no callback remained
     * @param callback
     */
    public void unRegisterCallback(NetworkStateCallback callback) {
        if (mCallbacks.size() == 0) {
            return;
        }
        mCallbacks.remove(callback);
        if (mCallbacks.size() == 0) {
            mContext.unregisterReceiver(mNetworkStateReceiver);
        }
    }


    public interface NetworkStateCallback {
        void onNetworkConnected();
        void onNetworkDisconnected();
    }
}
