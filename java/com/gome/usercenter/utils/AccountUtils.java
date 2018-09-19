package com.gome.usercenter.utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by dongzq on 2017/6/28.
 */

public class AccountUtils {
    private static final String TAG = Constants.TAG_HEADER + "AccountUtils";

    public static String getGomeAccountTokenValue(Context context){
        SharedPreferences dsp = PreferenceManager.getDefaultSharedPreferences(context);
        if(dsp.contains(Constants.PREF_GOME_ACCOUNT_TOKEN)){
            String token = dsp.getString(Constants.PREF_GOME_ACCOUNT_TOKEN, null);
            return token;
        }else{
            return null;
        }
    }

    public static String getGomeAccountPhoneNoValue(Context context){
        SharedPreferences dsp = PreferenceManager.getDefaultSharedPreferences(context);
        if(dsp.contains(Constants.PREF_GOME_ACCOUNT_PHONENO)){
            String phoneNo = dsp.getString(Constants.PREF_GOME_ACCOUNT_PHONENO, null);
            return phoneNo;
        }else{
            return null;
        }
    }

    public static String getGomeAccountIdValue(Context context){
        SharedPreferences dsp = PreferenceManager.getDefaultSharedPreferences(context);
        if(dsp.contains(Constants.PREF_GOME_ACCOUNT_ID)){
            String id = dsp.getString(Constants.PREF_GOME_ACCOUNT_ID, null);
            return id;
        }else{
            return null;
        }
    }

    public static String getGomeAccountEmailAddressValue(Context context){
        SharedPreferences dsp = PreferenceManager.getDefaultSharedPreferences(context);
        if(dsp.contains(Constants.PREF_GOME_ACCOUNT_MAILADDRESS)){
            String mail = dsp.getString(Constants.PREF_GOME_ACCOUNT_MAILADDRESS, null);
            return mail;
        }else{
            return null;
        }
    }

    public static String getGomeAccountNickNameValue(Context context){
        SharedPreferences dsp = PreferenceManager.getDefaultSharedPreferences(context);
        if(dsp.contains(Constants.PREF_GOME_ACCOUNT_NICKNAME)){
            String nickName = dsp.getString(Constants.PREF_GOME_ACCOUNT_NICKNAME, null);
            return nickName;
        }else{
            return null;
        }
    }

    public static String getGomeAccountAvatarPathValue(Context context){
        SharedPreferences dsp = PreferenceManager.getDefaultSharedPreferences(context);
        if(dsp.contains(Constants.PREF_GOME_ACCOUNT_LOCALAVATAR)){
            String path = dsp.getString(Constants.PREF_GOME_ACCOUNT_LOCALAVATAR, null);
            return path;
        }else{
            return null;
        }
    }

    public static void clearGomeAccountValue(Context context){
        SharedPreferences dsp = PreferenceManager.getDefaultSharedPreferences(context);
        dsp.edit()
                .putString(Constants.PREF_GOME_ACCOUNT_TOKEN, null)
                .putString(Constants.PREF_GOME_ACCOUNT_NICKNAME, null)
                .putString(Constants.PREF_GOME_ACCOUNT_MAILADDRESS, null)
                .putString(Constants.PREF_GOME_ACCOUNT_PHONENO, null)
                .putString(Constants.PREF_GOME_ACCOUNT_ID, null)
                .putString(Constants.PREF_GOME_ACCOUNT_LOCALAVATAR, null)
                .apply();
        Log.d(TAG, "clear all sp value");

    }

    public static void setGomeAccountTokenValue(Context context, String token){
        Log.d(TAG, "setGomeAccountTokenValue = " + token);
        SharedPreferences dsp = PreferenceManager.getDefaultSharedPreferences(context);
        dsp.edit().putString(Constants.PREF_GOME_ACCOUNT_TOKEN, token).apply();
    }

    public static void upadteGomeAccountLoginState(Context context){
        Intent intentBroadcast = new Intent(Constants.ACTION_GET_ACTIVITY_LOGIN_STATE);
        context.sendBroadcast(intentBroadcast);
    }

    public static boolean getGomeAccountLoginState(Context context){
        if (null == getGomeAccountTokenValue(context)){
            return false;
        }else{
            return true;
        }
    }

    public static String getLatestGomeAccountName(Context context){
        Account latestAccount = null;
        final AccountManager am = AccountManager.get(context);
        Account [] accounts = am.getAccountsByType(Constants.ACCOUNT_TYPE);
        if(accounts.length == 0)
            return null;
        for(int i=0;i<accounts.length;i++){
            Account account =  accounts[i];
            latestAccount = account;
        }
        return latestAccount.name;
    }
}
