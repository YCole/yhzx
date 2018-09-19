package com.gome.usercenter.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gome.usercenter.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ActivityUtils {
	private static final String TAG = Constants.TAG_HEADER + "ActivityUtils";

	private static Toast mToast;
	public static void setToastShow(Context context, int resId){
		showToast(context, context.getString(resId));
	}
	public static void showToast(Context context, String msg) {
		if (mToast == null) {
			mToast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
		} else {
			mToast.setText(msg);
		}
		LinearLayout linearLayout = (LinearLayout)mToast.getView();
		TextView tv = (TextView)linearLayout.findViewById(com.android.internal.R.id.message);
		tv.setSingleLine(false);
		mToast.show();
	}
	public static void setToastOff() {
		if (null != mToast) {
			mToast.cancel();
			mToast = null;
		}
	}

	public static boolean isNetworkConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);  
			NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();  
			if (mNetworkInfo != null) {
				return mNetworkInfo.isAvailable();  
			}
		}
		return false;  
	}

    static Dialog mNetworkDialog = null;

    public static boolean checkNetworkConnection(Context context) {
        if (!isNetworkConnected(context)) {
            Log.d(TAG, "network not connected");
            if (mNetworkDialog == null) {
                mNetworkDialog = DialogUtils.setNetworkNotifyDialog(context);
            } else {
                mNetworkDialog.show();
            }
            return false;
        } else {
            Log.d(TAG, "network connected");
        }
        return true;
    }

    public static void dismissNetworkDialog() {
        if (mNetworkDialog != null && mNetworkDialog.isShowing()) {
            mNetworkDialog.dismiss();
        }
        mNetworkDialog = null;
    }

    /* modified-begin by zhiqiang.dong@gometech.com.cn for GMOS2X1-511 */
    public static void dismissNetworkDialog(Context context) {
        if (mNetworkDialog != null && mNetworkDialog.isShowing()) {
            Context baseContext = ((ContextThemeWrapper) mNetworkDialog.getContext()).getBaseContext();
            /* modified-begin by zhiqiang.dong@gometech.com.cn for PRODUCTION-5921 */
            if (baseContext instanceof ContextThemeWrapper) {
                baseContext = ((ContextThemeWrapper) baseContext).getBaseContext();
            }
            /* modified-end */
            if (context.equals(baseContext)) {
                mNetworkDialog.dismiss();
                mNetworkDialog = null;
            }
        }
    }

    /**
     * @param finish finish activity when dismiss dialog if true, otherwise just dismiss dialog
     * @param context activity which pops this dialog
     */
    public static boolean checkNetworkConnection(Context context, boolean finish) {
        if (!isNetworkConnected(context)) {
            Log.d(TAG, "network not connected");
            /* modified-begin by zhiqiang.dong@gometech.com.cn for PRODUCTION-5921 */
            Context base = null;
            if (mNetworkDialog != null) {
                base = ((ContextThemeWrapper) mNetworkDialog.getContext()).getBaseContext();
                if (base instanceof ContextThemeWrapper) {
                    base = ((ContextThemeWrapper) base).getBaseContext();
                }
            }
            if (mNetworkDialog != null && base != null && context.equals(base)) {
            /* modified-end */
                mNetworkDialog.show();
            } else {
                mNetworkDialog = DialogUtils.setNetworkNotifyDialog(context, finish);
            }
            return false;
        } else {
            Log.d(TAG, "network connected");
        }
        return true;
    }
    /* modified-end */

	public static boolean isNetworkAvailable(Context context) {
	 ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

	 if (connectivityManager == null) {
		 return false;
	 } else {
		 NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();

		 if (networkInfo != null && networkInfo.length > 0) {
			 for (int i = 0; i < networkInfo.length; i++) {
				 if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED) {
					 Log.i(TAG, "network available");
					 return true;
				 }
			 }
		 }
	 }
	 Log.i(TAG, "network not available");
	 return false;
	}

	public static boolean isForeground(Activity activity) {
		return isForeground(activity, activity.getClass().getName());
	}

	public static boolean isDeviceHasSimCard(Context context) {
		TelephonyManager telMgr = (TelephonyManager)
				context.getSystemService(Context.TELEPHONY_SERVICE);
		int simState = telMgr.getSimState();
		boolean result = true;
		switch (simState) {
			case TelephonyManager.SIM_STATE_ABSENT:
				result = false;
				break;
			case TelephonyManager.SIM_STATE_UNKNOWN:
				result = false;
				break;
		}
		Log.d(TAG, result ? "had" : "no" + " sim card detected");
		return result;
	}

	/**
	 * 判断某个界面是否在前台
	 *
	 * @param context   Context
	 * @param className 界面的类名
	 * @return 是否在前台显示
	 */
	public static boolean isForeground(Context context, String className) {
		if (context == null || TextUtils.isEmpty(className))
			return false;
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
		if (list != null && list.size() > 0) {
			ComponentName cpn = list.get(0).topActivity;
			if (className.equals(cpn.getClassName()))
				return true;
		}
		return false;
	}

	/**
	 *
	 * 判断设备是否被root过
	 * @return boolean
	 *
	 */
	private final static int kSystemRootStateUnknow=-1;
	private final static int kSystemRootStateDisable=0;
	private final static int kSystemRootStateEnable=1;
	private static int systemRootState=kSystemRootStateUnknow;
	public static String isRootSystem() {
		if(isDebugMode())
			return "0";
		if (systemRootState == kSystemRootStateEnable) {
			return "1";
		} else if (systemRootState == kSystemRootStateDisable) {
			return "0";
		}
		File f = null;
		final String kSuSearchPaths[] = { "/system/bin/", "/system/xbin/",
				"/system/sbin/", "/sbin/", "/vendor/bin/" };
		try {
			for (int i = 0; i < kSuSearchPaths.length; i++) {
				f = new File(kSuSearchPaths[i] + "su");
				if (f != null && f.exists()) {
					systemRootState = kSystemRootStateEnable;
					return "1";
				}
			}
		} catch (Exception e) {
		}
		systemRootState = kSystemRootStateDisable;
		return "0";
	}

	public static String getDeviceID(Context context) {
		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		String imei = tm.getDeviceId();
		if(null == imei || imei.length() <= 0 ) {
			Log.d(TAG, "invalid imei = " + imei);
			if(isDebugMode()){
				Log.d(TAG, "debug mode set imei invalid00000000");
				imei = "invalid00000000";
			}else{
				//20170908 add for invalid imei start
				Log.d(TAG, "user mode set imei invalid00000000");
				imei = "invalid00000000";
				//20170908 add for invalid imei end
			}
		}
		return imei;
	}

	public static String getEncryptJsonString(String jsonString){
		String encryptJsonString = null;
		try {
			encryptJsonString = DesUtil.encrypt3DES(jsonString);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return encryptJsonString;
	}

	public static String getDescryptJsonString(String result){
		String discryptJsonString = null;
		try {
			discryptJsonString = DesUtil.decrypt3DES(result);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return discryptJsonString;
	}

	public static String getVersionInternal() {
		return SystemProperties.get(Constants.BUILD_NUMBER_INTERNAL_RELEASE, Constants.BUILD_INFO_DEFAULT_VALUE);
	}

	public static boolean isAgreementDialogHadShown(Context context){
		SharedPreferences dsp = PreferenceManager.getDefaultSharedPreferences(context);
		boolean shown = dsp.getBoolean(Constants.PREF_AGREEMENT_DIALOG_HAD_SHOWN, false);
		return shown;
	}

	public static void setAgreementDialogHadShown(Context context, boolean shown){
		SharedPreferences dsp = PreferenceManager.getDefaultSharedPreferences(context);
		dsp.edit().putBoolean(Constants.PREF_AGREEMENT_DIALOG_HAD_SHOWN, shown).apply();
	}

	/**
	 * 判断字符串是否邮箱
	 * @param email
	 * @return
	 */
	public static boolean isEmail(String email){
		if (null==email || "".equals(email)) return false;
		//Pattern p = Pattern.compile("\\w+@(\\w+.)+[a-z]{2,3}"); //简单匹配
		Pattern p =  Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");//复杂匹配
		Matcher m = p.matcher(email);
		return m.matches();
	}
	/**
	 * 判断字符串是否手机号
	 * @param mobiles
	 * @return
	 */
	public static boolean isMobileNO(String mobiles){
		/* modified-begin by zhiqiang.dong@gometech.com */
		//fix phone number 17368467365 not match
		/*目前国内运营商在用号段
		130、131、132、133、134、135、136、137、138、139
		145、147
		150、151、152、153、155、156、157、158、159
		170 171 173 176 177 178
		180 181 182 183 184 185 186 187 188 189
		 */
		Pattern p = Pattern.compile("^((13[0-9])|14[5|7]|(15[^4,\\D])|17[0|1|2|3|6|7|8]|(18[0-9]))\\d{8}$");
		/* modified-end */
		Matcher m = p.matcher(mobiles);
		boolean match = m.matches();
		Log.i(TAG, "isMobileNO() match:"+match);
		return match;
	}

	public static String getCurrentTime() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd-HH.mm.ss");
		Date curDate = new Date(System.currentTimeMillis());
		String str = formatter.format(curDate);
		return str;
	}

	public static void cacheApplicationData(Context context, String file, String data){
		File f = new File(context.getFilesDir(), file);
		try {
			OutputStream outputStream = new FileOutputStream(f);
			outputStream.write(data.getBytes());
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean isCacheDataExsist(Context context, String file){
		File f = new File(context.getFilesDir(), file);
		return f.exists();
	}

	public static void deleteCacheFile(Context context, String file){
		File f = new File(context.getFilesDir(), file);
		if(f.exists()) f.delete();
	}

	public static String readFromFileOneLine(Context context, String filename) {
		File f = new File(context.getFilesDir(), filename);
		BufferedReader br = null;
		String s = "";
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					f)));

			s = br.readLine();

		} catch (FileNotFoundException e) {
			Log.e(TAG,  e.toString());
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			Log.e(TAG, e.toString());
			e.printStackTrace();
		} catch (NullPointerException e) {
			Log.e(TAG, e.toString());
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(TAG, e.toString());
			e.printStackTrace();
		}

		if (br != null) {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return s;
	}

	public static void debugLog(String log) {
		Log.d(TAG, log);
		File sd = Environment.getExternalStorageDirectory();
		String fileName = sd.getPath() + File.separator + "UserCenter.log";
		try  {
			FileWriter writer = new  FileWriter(fileName, true);
			writer.write(getCurrentTime() + ": " + log + "\n");
			writer.close();
		} catch  (IOException e) {
			e.printStackTrace();
		}

	}

    /**
     * @param dest target string to be inserted
     * @param str insert string
     * @param interval insert interval
     * @return result string
     */
    public static String insertSubStringInString(String dest, String str, int interval) {
        StringBuilder builder = new StringBuilder();
        int count = dest.length() % interval == 0 ? dest.length() / interval -1 : dest.length() / interval;
        int i;
        for (i = 0; i < count; i++) {
            builder.append(dest.substring(interval * i, interval * i + interval)).append(str);
        }
        builder.append(dest.substring(interval * i));
        return builder.toString();
    }

	public static String getSummaryText(String content){
		content = content.replace("<em>", "");
		content = content.replace("</em>", "");
		content = content.replace("<br>", "");
		content = content.replace("</br>", "");
		content = content.replace("&nbsp;", "");
		content = content.replace("<div>", "");
		content = content.replace("</div>", "");
		content = content.replace("<p>", "");
		content = content.replace("</p>", "<br>");
		content = content.replace("<br/><br>", "");
		return content;
	}
    public static String trimSpace(String source) {
		source = source.trim();
		while (source.startsWith(" ")) {
			source = source.substring(1, source.length()).trim();
		}
		while (source.endsWith(" ")) {
			source = source.substring(0, source.length() - 1).trim();
		}
		return source;
	}

	/**
	 * 将12345678901 改成  123****8901 显示
	 * @param phoneNum
	 * @return
	 */
	public static String convertPhoneNumWithStar(String phoneNum){
		String str = phoneNum;
		if(phoneNum.length() == 11){
			str = phoneNum.substring(0, 3)+"****"+phoneNum.substring(7, 11);
		}
		return str;
	}
	/**
	 * 邮箱隐藏中间1-3位
	 * 如： 12@qq.com => 1*@qq.com  123@qq.com => 1*3@qq.com   1234@qq.com => 1**4@qq.com
	 *  12345@qq.com => 1***5@qq.com    123456@qq.com => 12**56@qq.com
	 * @param email
	 * @return
	 */
	public static String convertEmailWithStar(String email){
		String strPrefix = email.substring(0,email.indexOf("@"));
		String strPrefixHead;
		String strPrefixTail;
		String strTail = email.substring(email.indexOf("@"));//@以后的字符串包括@
		String strConvert = null;
		switch (strPrefix.length()) {
			case 1:
				strConvert = email;
				break;
			case 2:
				strPrefixHead = strPrefix.substring(0, 1);
				strConvert = strPrefixHead+"*"+strTail;
				break;
			case 3:
				strPrefixHead = strPrefix.substring(0, 1);
				strPrefixTail = strPrefix.substring(2, 3);
				strConvert = strPrefixHead+"*"+strPrefixTail+strTail;
				break;
			default:
				if(strPrefix.length()%2 ==0){ //偶数
					strConvert = strPrefix.substring(0, strPrefix.length()/2-1)+"**"+strPrefix.substring(strPrefix.length()/2+1)+strTail;
				}else{ //奇数
					strConvert = strPrefix.substring(0, strPrefix.length()/2-1)+"***"+strPrefix.substring(strPrefix.length()/2+2)+strTail;
				}
				break;
		}
		Log.i(TAG, "convertEmailWithStar() prefix:"+strPrefix+" strTail:"+strTail+" strConvert:"+strConvert);
		return strConvert;
	}
    /**
     * 发送隐藏 虚拟按键栏广播，一般开机向导模式调用
     * @param context
     * @return
     */
    public static void sendHideNavigationBarBrocast(Context context){
        Log.i(TAG,"sendHideNavigationBarBrocast() ");
        Intent intentAction = new Intent();
        intentAction.setPackage("com.android.systemui");
        intentAction.setAction("hide_navigationbar");
        context.sendBroadcast(intentAction);
    }

	//bugfix GMOS-9487 start
	/**
	 * 设置添加屏幕的背景透明度
	 * @param bgAlpha
	 */
	public static void setBackgroundAlpha(Context context, float bgAlpha)
	{
		Activity activity = (Activity) context;
		WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
		lp.alpha = bgAlpha; //0.0-1.0
		activity.getWindow().setAttributes(lp);
	}

	/**
	 * 设置不同状态的navigationbar color
	 * @param popupStatus
	 */
	public static void setNavigationBarColor(Context context, boolean popupStatus){
		Activity activity = (Activity) context;
		if(popupStatus){
			activity.getWindow().setNavigationBarColor(context.getResources().getColor(R.color.dark_background));
		}else{
			activity.getWindow().setNavigationBarColor(context.getResources().getColor(R.color.light_navigationbar_color));
		}
	}
	//bugfix GMOS-9487 end

	public static boolean isDebugMode(){
		boolean isDebug = false;
		isDebug = ("userdebug").equals(android.os.Build.TYPE)|| ("eng").equals(android.os.Build.TYPE);
		Log.d(TAG, "isDebug = " + isDebug);
		return isDebug;
	}
}
