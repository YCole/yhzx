package com.gome.usercenter.utils;

import android.util.Log;

import com.gome.usercenter.module.VersionInfo;

import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtils {
	private static final String TAG = Constants.TAG_HEADER + "JsonUtils";

    public static boolean parseJsonResult(String strResult) {
        try {
            JSONObject jsonObj = new JSONObject(strResult);
            String resCode = getString(jsonObj, "resCode");
            String resMsg = getString(jsonObj, "resMsg");
            Log.d(TAG, "resCode = " + resCode);
            Log.d(TAG, "resMsg = " + resMsg);
            if(Constants.RESPONSE_CODE_SUCCESS.equals(resCode)){
                return true;
            }else{
                return false;
            }
        } catch (JSONException e) {
            Log.d(TAG, "Parse Json error");
            e.printStackTrace();
        }
        return false;
    }

	public static boolean parseApplyExperienceResult(String strResult) {
		try {
			JSONObject jsonObj = new JSONObject(strResult);
			String resCode = getString(jsonObj, "resCode");
			String resMsg = getString(jsonObj, "resMsg");
			Log.d(TAG, "resCode = " + resCode);
			Log.d(TAG, "resMsg = " + resMsg);
			if(Constants.RESPONSE_CODE_SUCCESS.equals(resCode)){
				return true;
			}else{
				return false;
			}
		} catch (JSONException e) {
			Log.d(TAG, "Parse Json error");
			e.printStackTrace();
		}
		return false;
	}

    public static VersionInfo parseCheckExperienceResult(String strResult) {
        VersionInfo versionInfo = new VersionInfo();
        try {
            JSONObject jsonObj = new JSONObject(strResult);
            String resCode = getString(jsonObj, "resCode");
            String resMsg = getString(jsonObj, "resMsg");
            String experienceRequest = getString(jsonObj, "experienceRequest");
            String publicDesc = getString(jsonObj, "publicDesc");
            String projectName = getString(jsonObj, "projectName");
            String publicDate = getString(jsonObj, "publicDate");
            String sourceVersionNo = getString(jsonObj, "sourceVersionNo");
            String targetVersionNo = getString(jsonObj, "targetVersionNo");
            String publicVersionNo = getString(jsonObj, "publicVersionNo");
            long packageSize = getLong(jsonObj, "packageSize");
            Log.d(TAG, "resCode = " + resCode);
            Log.d(TAG, "resMsg = " + resMsg);
            if(Constants.RESPONSE_CODE_SUCCESS.equals(resCode)){
                versionInfo.setResCode(resCode);
                versionInfo.setResMsg(resMsg);
                versionInfo.setExperienceRequest(experienceRequest);
                versionInfo.setPublicDesc(publicDesc);
                versionInfo.setProjectName(projectName);
                versionInfo.setPublicDate(publicDate);
                versionInfo.setSourceVersionNo(sourceVersionNo);
                versionInfo.setTargetVersionNo(targetVersionNo);
                versionInfo.setPublicVersionNo(publicVersionNo);
                versionInfo.setUpdatePackageSize(packageSize);
                return versionInfo;
            }else{
                return null;
            }
        } catch (JSONException e) {
            Log.d(TAG, "Parse Json error");
            e.printStackTrace();
        }
        return null;
    }

	public static String getString(JSONObject jsonObj, String key){
        if(jsonObj.has(key)) {
            try {
                return jsonObj.getString(key);
            } catch (JSONException e) {
                Log.e(TAG, "Parse Json error");
                e.printStackTrace();
            }
        }
        return null;
    }

    public static boolean getBoolean(JSONObject jsonObj, String key){
        if(jsonObj.has(key)) {
            try {
                return jsonObj.getBoolean(key);
            } catch (JSONException e) {
                Log.e(TAG, "Parse Json error");
                e.printStackTrace();
            }
        }
        return false;
    }

    public static int getInt(JSONObject jsonObj, String key){
        if(jsonObj.has(key)) {
            try {
                return jsonObj.getInt(key);
            } catch (JSONException e) {
                Log.e(TAG, "Parse Json error");
                e.printStackTrace();
            }
        }
        return -1;
    }

    public static long getLong(JSONObject jsonObj, String key){
        if(jsonObj.has(key)) {
            try {
                return jsonObj.getLong(key);
            } catch (JSONException e) {
                Log.e(TAG, "Parse Json error");
                e.printStackTrace();
            }
        }
        return -1;
    }

}
