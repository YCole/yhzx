package com.gome.usercenter.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.gome.usercenter.utils.AccountUtils;
import com.gome.usercenter.utils.ActivityUtils;
import com.gome.usercenter.utils.Constants;

/**
 * Created by jianfeng.xue on 2017/7/21.
 */

public class UserCenterAccountReceiver extends BroadcastReceiver {
    private final String TAG = Constants.TAG_HEADER + "UserCenterAccountReceiver";
    @Override
    public void onReceive(final Context context, Intent intent) {
        /*
        if (UserHandle.myUserId() != 0) {
            return;
        }
        */
        String action = intent.getAction();
        if (Constants.ACTION_GOME_ACCOUNT_LOGOUT.equals(action)) {
            Log.d(TAG, "ACTION_GOME_ACCOUNT_LOGOUT");
            AccountUtils.clearGomeAccountValue(context);
        } else if (Constants.ACTION_GOME_ACCOUNT_LOGIN.equals(action)) {
            Log.d(TAG, "ACTION_GOME_ACCOUNT_LOGIN");
            updateGomeAccountInfo(intent, context);

            Intent intentBroadcast = new Intent(Constants.ACTION_REFRESH_ACCOUNT_INFO);
            context.sendBroadcast(intentBroadcast);
        } else if (Constants.ACTION_GOME_ACCOUNT_UPDATE_INFO.equals(action)) {
            Log.d(TAG, "ACTION_GOME_ACCOUNT_UPDATE_INFO");
            updateGomeAccountInfo(intent, context);

            Intent intentBroadcast = new Intent(Constants.ACTION_UPDATE_ACCOUNT_INFO_LOCAL);
            context.sendBroadcast(intentBroadcast);
        }
    }

    private void updateGomeAccountInfo(Intent intent, Context context){
        String mServerToken = intent.getStringExtra(Constants.KEY_SERVER_TOKEN);
        String mNickName = intent.getStringExtra(Constants.KEY_ACCOUNT_NAME);
        String mMallAddress = intent.getStringExtra(Constants.KEY_ACCOUNT_EMAIL);
        String mPhoneNo = intent.getStringExtra(Constants.KEY_ACCOUNT_PHONE_NUMBER);
        String mGomeId = intent.getStringExtra(Constants.KEY_ACCOUNT_GOME_ID);
        String mLocalAvatarPath = intent.getStringExtra(Constants.KEY_LOCAL_AVATAR_PATH);

        SharedPreferences dsp = PreferenceManager.getDefaultSharedPreferences(context);
        if(null != mServerToken){
            dsp.edit().putString(Constants.PREF_GOME_ACCOUNT_TOKEN, mServerToken).apply();
        }
        if(null != mNickName){
            dsp.edit().putString(Constants.PREF_GOME_ACCOUNT_NICKNAME, mNickName).apply();
        }
        if(null != mMallAddress){
            dsp.edit().putString(Constants.PREF_GOME_ACCOUNT_MAILADDRESS, mMallAddress).apply();
        }
        if(null != mPhoneNo){
            dsp.edit().putString(Constants.PREF_GOME_ACCOUNT_PHONENO, mPhoneNo).apply();
        }
        if(null != mGomeId){
            dsp.edit().putString(Constants.PREF_GOME_ACCOUNT_ID, mGomeId).apply();
        }
        if(null != mLocalAvatarPath){
            dsp.edit().putString(Constants.PREF_GOME_ACCOUNT_LOCALAVATAR, mLocalAvatarPath).apply();
        }
    }
}
