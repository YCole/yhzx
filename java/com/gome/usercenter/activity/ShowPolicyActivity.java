package com.gome.usercenter.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.gome.usercenter.R;
import com.gome.usercenter.utils.ActivityUtils;
import com.gome.usercenter.utils.Constants;
import com.gome.usercenter.utils.DialogUtils;
import com.gome.usercenter.utils.NetworkStateObserver;

import java.util.Locale;

public class ShowPolicyActivity extends BaseActivity {

    private static final String TAG = Constants.TAG_HEADER + "ShowPolicyActivity";

    private static final String WEBVIEW_MODE = "webview_mode";
    private static final String LICENSE_PATH = "license_path";
    private static final String COMMON_PAGE = "common_page";
    private static final String GUIDELINE_PAGE = "guideline_page";
    private static final String THIRD_PARTY_TITLE = "title";
    private static final String KEY_LOCAL_ACCOUNT_START_MODE = "localAccountStartMode";  //账号启动模式
    private static final String BOOT_WIZARD_MODE= "bootWizard";
    private String mLicensePath;
    private String mTitle;

    private WebView mWebView;

    private ViewGroup mParent;

    private String mode;
    private String mGuideLine;
    private Dialog mNetworkDialog ;

    private String mAccountStartMode = null;  //账号启动模式
    private String page;
    private String url_gome_common_problem_base = Constants.QINIU_BASE_URL + "feedback/problem";
    private String url_gome_common_problem_language = ".html?language=";
    private String url_gome_common_problem_dpi = "&dpi=";



    private String title;

    private static final String APP_CACAHE_DIRNAME = "/app_webview";
    private String url_gome_common_policy_base = Constants.QINIU_BASE_URL;

    private Context mContext;

    private NetworkStateObserver mNetworkStateObserver;
    private NetworkStateObserver.NetworkStateCallback mNetworkCallback = new NetworkStateObserver.NetworkStateCallback() {
        @Override
        public void onNetworkConnected() {
            dismissNetworkDialog();
            initData();
        }

        @Override
        public void onNetworkDisconnected() {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setBody(R.layout.activity_show_policy);

        mContext = this;

        initData();

        mNetworkStateObserver = NetworkStateObserver.newInstance(mContext);
        mNetworkStateObserver.registerCallback(mNetworkCallback);
    }
    @Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.i(TAG,"onResume() mAccountStartMode:"+mAccountStartMode);
		if(BOOT_WIZARD_MODE.equals(mAccountStartMode)){
		   sendHideNavigationBarBrocast(ShowPolicyActivity.this);
           //getWindow().getDecorView().showNavigationBarDivider(false);
		}
    }
    
    private void initData(){

        if(!checkNetworkConnection(this, true)){
            return;
        }

        Intent intent = getIntent();
//        intent.setClassName("com.gome.usercenter", "com.gome.usercenter.activity.GuideLineActivity");
        mode = intent.getStringExtra(WEBVIEW_MODE);
        mAccountStartMode = intent.getStringExtra(KEY_LOCAL_ACCOUNT_START_MODE);
        Log.e(TAG, "initData() mAccountStartMode:"+mAccountStartMode);
        
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = getResources().getConfiguration().getLocales().get(0);
        } else {
            locale = getResources().getConfiguration().locale;
        }
        String lang = locale.getLanguage();
        String country = locale.getCountry();

        if(lang.equals("en")) {
            country = "US";
        }else if(lang.equals("zh")){
            if(null!=country && country.isEmpty()){
                country = "CN";
            }
        }

        if(mode == null){
            String action = intent.getAction();
            if(action != null ){
                mLicensePath = action;
            }else {
                mLicensePath = intent.getStringExtra(LICENSE_PATH);
            }

            if (mLicensePath == null) {
                Log.e(TAG, "license path must not be null");
                finish();
            }

            title = intent.getStringExtra(THIRD_PARTY_TITLE);

            StringBuffer sLicenseFile = new StringBuffer();
            sLicenseFile
                    .append(url_gome_common_policy_base)
                    .append(mLicensePath).append("_").append(country);
            sLicenseFile.append(".html");

            /*
            if (mLicensePath.equals(Constants.PATH_PRIVACY_POLICY) && country.equals("CN")) {
                sLicenseFile.append(".html");
            } else {
                sLicenseFile.append(".htm");
            }
            */

            if(title != null){
                mTitle = title;
                setCustomCentralTitle(mTitle);
                setCustomBackIcon();
            }else{
                if(mLicensePath.equals(Constants.PATH_USER_AGREEMENT)){
                    //User agreement
                    mTitle = getResources().getString(R.string.user_agreement_title);
                    setCustomCentralTitle(mTitle);
                    setCustomBackIcon();
                }else if (mLicensePath.equals(Constants.PATH_PRIVACY_POLICY)){
                    //Privacy policy
                    mTitle = getResources().getString(R.string.gome_privacy_policy_title);
                    setCustomCentralTitle(mTitle);
                    setCustomBackIcon();
                }else if (mLicensePath.equals(Constants.PATH_SERVICE_POLICY)){
                    //Terms of service
                    mTitle = getResources().getString(R.string.service_policy);
                    mCustomTitle.setText(mTitle);
                }else if (mLicensePath.equals(Constants.PATH_DISCLAIMER_WARRANTY)){
                    //Disclaimer
                    mTitle = getResources().getString(R.string.disclaimer);
                    mCustomTitle.setText(mTitle);
                }
            }

            //initView("file:///android_asset/" + sLicenseFile.toString());
            initView(sLicenseFile.toString());

        }else if(mode.equals("show_common_problem")){
            page = intent.getStringExtra(COMMON_PAGE);
            if (page == null) {
                Log.e(TAG, "page must not be null");
                finish();
            }

            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            float dpi = dm.densityDpi;

            String language = "Chinese";
            if(country.equals("CN")){
            }else if(country.equals("TW")){
                language = "TraditionalChinese";
            }else if(country.equals("US")){
                language = "English";
            }

            StringBuffer sPath = new StringBuffer();
            mCustomTitle.setText(getResources().getString(R.string.feedback));
            sPath.append(url_gome_common_problem_base).append(page)
                    .append(url_gome_common_problem_language).append(language)
                    .append(url_gome_common_problem_dpi).append(dpi);
            String path = sPath.toString();
            initView(path);
        }else if(mode.equals("guideline_item")){
            Bundle bundle = intent.getExtras();
            String itemUrl = bundle.getString("url_guideline_item");
            String childItemTitle = bundle.getString("child_title");
            mCustomTitle.setText(childItemTitle);
            Log.d(TAG, "itemUrl = " + itemUrl);
            initView(itemUrl);
        }
    }

    private void initView(String path){

        //mWebView = (WebView) findViewById(R.id.webView);
        mParent = (ViewGroup) findViewById(R.id.webview_parent);
        mWebView = new WebView(mContext);
        mWebView.setVerticalScrollBarEnabled(false);
        mWebView.setHorizontalFadingEdgeEnabled(false);
        mWebView.setVerticalFadingEdgeEnabled(false);
        mWebView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        //20170908 add for webview cache start
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setDatabaseEnabled(true);
        String cacheDirPath = getFilesDir().getAbsolutePath() + APP_CACAHE_DIRNAME;
        mWebView.getSettings().setDatabasePath(cacheDirPath.replace("/files", ""));
        mWebView.getSettings().setAppCachePath(cacheDirPath.replace("/files", ""));
        mWebView.getSettings().setAppCacheEnabled(true);

        if (isNetworkAvailable(ShowPolicyActivity.this)) {
            mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        } else {
            mWebView.getSettings().setCacheMode(
                    WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }
        //20170908 add for webview cache end

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
             public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                 // *** NEVER DO THIS!!! ***
                 // super.onReceivedSslError(view, handler, error);
                 // let's ignore ssl error
                 handler.proceed();
             }
            /* modified-begin by zhiqiang.dong@gometech.com for PRODUCTION-2284 */
            /*
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if(url.startsWith("tel:")){
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    Uri uri = Uri.parse("tel://" + "4008988666");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setData(uri);
                    startActivity(intent);
                    return true;
                }else{
                    loadurl(view, url);
                }
                return false;
            }*/
            /* modified-end */
            public void onPageFinished(WebView view, String url) {
                DialogUtils.progressDialogDismiss();
            }
        });
        //PRODUCTION-506 start
        mWebView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return true;
            }
        });
        //PRODUCTION-506 end

        DialogUtils.setProgressingDialog(this, null,
                getResources().getString(R.string.url_dialog_loading));

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mWebView.loadUrl(path);
        mParent.addView(mWebView);
    }

    public void loadurl(final WebView webView, final String url) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl(url);
            }
        });
    }

     protected void onDestroy() {
         mNetworkStateObserver.unRegisterCallback(mNetworkCallback);
         if(null != mWebView){
             mWebView.stopLoading();
             mWebView.removeAllViews();
             mParent.removeView(mWebView);
             mWebView.destroy();
             mWebView = null;
         }
         DialogUtils.progressDialogDismiss();
         super.onDestroy();
     }

     public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
			//GMOS-9132 start
            if(null != mWebView && mWebView.canGoBack()){
			//GMOS-9132 end
                Log.i(TAG, "canGoBack, goBack");
                mWebView.goBack();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.CHECK_NETWORK_REQUEST_CODE && checkNetworkConnection(mContext, true)) {
            dismissNetworkDialog();
            initData();
        }
    }

    private void sendHideNavigationBarBrocast(Context context){
        Log.i(TAG,"sendHideNavigationBarBrocast() ");
        Intent intentAction = new Intent();
        intentAction.setPackage("com.android.systemui");
        intentAction.setAction("hide_navigationbar");
        context.sendBroadcast(intentAction);
    }

    private void dismissNetworkDialog() {
        if (mNetworkDialog != null && mNetworkDialog.isShowing()) {
            mNetworkDialog.dismiss();
        }
        mNetworkDialog = null;
    }

    private boolean checkNetworkConnection(Context context, boolean finish) {
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

    private boolean isNetworkAvailable(Context context) {
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

    private boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }


}
