package com.gome.usercenter.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gome.usercenter.R;
import com.gome.usercenter.helper.HttpCallbackStringListener;
import com.gome.usercenter.module.VersionInfo;
import com.gome.usercenter.utils.AccountUtils;
import com.gome.usercenter.utils.ActivityUtils;
import com.gome.usercenter.utils.Constants;
import com.gome.usercenter.utils.DialogUtils;
import com.gome.usercenter.utils.JsonUtils;
import com.gome.usercenter.utils.NetworkStateObserver;
import com.gome.usercenter.utils.NetworkUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import gome.app.GomeAlertDialog;

public class ExperienceVersionActivity extends Activity implements View.OnClickListener {

    private final String TAG = Constants.TAG_HEADER + "ExperienceVersionActivity";

    private RelativeLayout top_title_layout;
    private ImageButton tb_back;

    private TextView mAgreeDisclaimer;
    private TextView mRequestStatus;
    private TextView mProcessText;
    private TextView mExperienceStatus;
    private CheckBox mDisclaimerCheckbox;
    private FrameLayout mFrameBody;
    private Button mRequestForExperienceButton;
    private Button mSystemUpdateButton;
    private Context mContext;
    //private LinearLayout mLayoutFooter;
    private RelativeLayout mLayoutFooterCheckbox;
    private LinearLayout request_button_margin;

    private LinearLayout mWebViewParent;
    private WebView mWebView;

    private TextView mExperienceVersionIntroduce;

    public Handler mHandler = new Handler();

    private static final String LICENSE_PATH = "license_path";

    public static boolean gotoGomeAccountService = false;
    private NetworkStateObserver mNetworkStateObserver;
    private NetworkStateObserver.NetworkStateCallback mNetworkCallback = new NetworkStateObserver.NetworkStateCallback() {
        @Override
        public void onNetworkConnected() {
            ActivityUtils.dismissNetworkDialog();
            if(checkNetworkAccountEnv()){
                checkForExperienceVersion();
            }
        }

        @Override
        public void onNetworkDisconnected() {

        }
    };

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Constants.ACTION_UPDATE_ACCOUNT_INFO_LOCAL.equals(action)
                    || Constants.ACTION_REFRESH_ACCOUNT_INFO.equals(action)) {
                Log.d(TAG, "refresh ui");
                if(gotoGomeAccountService) {
                    if (checkNetworkAccountEnv()) {
                        checkForExperienceVersion();
                    }
                    gotoGomeAccountService = false;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setBackgroundTheme();
        setContentView(R.layout.activity_experience_version);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mContext = this;
        Log.d(TAG, "onCreate");
        initView();
        //default view
        setViewStatus(false);
        mExperienceStatus.setVisibility(View.INVISIBLE);

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);

        tb_back = (ImageButton)findViewById(R.id.ps_back_btn);
        tb_back.setOnClickListener(new MyStatusOnClick());
    }

    @Override
    protected void onResume() {
        super.onResume();
        //setViewStatus(true);
        Log.d(TAG, "onResume gotoGomeAccountService = " + gotoGomeAccountService);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_UPDATE_ACCOUNT_INFO_LOCAL);
        filter.addAction(Constants.ACTION_REFRESH_ACCOUNT_INFO);
        mContext.registerReceiver(mReceiver, filter);

        if(gotoGomeAccountService){
            checkNetworkAccountEnv();
        }

        if(checkNetworkAccountEnv()){
            checkForExperienceVersion();
        }
        //setViewStatus(true);

        mNetworkStateObserver = NetworkStateObserver.newInstance(mContext);
        mNetworkStateObserver.registerCallback(mNetworkCallback);
    }


    @Override
    protected void onPause() {
        super.onPause();
        mContext.unregisterReceiver(mReceiver);
        mNetworkStateObserver.unRegisterCallback(mNetworkCallback);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityUtils.dismissNetworkDialog();
        // modified by zhiqiang.dong@gometech.com for PRODUCTION-2407
        DialogUtils.progressDialogDismiss();
        mNetworkStateObserver.unRegisterCallback(mNetworkCallback);
        mWebView.stopLoading();
        mWebView.removeAllViews();
        mWebViewParent.removeView(mWebView);
        mWebView.destroy();
        mWebView = null;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.CHECK_NETWORK_REQUEST_CODE) {
            if(ActivityUtils.checkNetworkConnection(mContext, true)){
                if(checkNetworkAccountEnv()){
                    checkForExperienceVersion();
                }
            }
        }
    }

    private void setBackgroundTheme(){
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
    }

    class MyStatusOnClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            finish();
        }
    }

    private void initView() {
        mAgreeDisclaimer = (TextView) findViewById(R.id.disclaimer);
        mAgreeDisclaimer.setText(getSpanString());
        mAgreeDisclaimer.setMovementMethod(LinkMovementMethod.getInstance());
        mDisclaimerCheckbox = (CheckBox) findViewById(R.id.agree_disclaimer_checkbox);
        mDisclaimerCheckbox.setChecked(false);
        mFrameBody = (FrameLayout) findViewById(R.id.frame_body);
        mRequestStatus = (TextView) findViewById(R.id.request_status);
        mRequestStatus.setTextColor(mContext.getResources().getColor(R.color.dark_foreground));
        mExperienceStatus = (TextView) findViewById(R.id.experience_status);
        mProcessText = (TextView) findViewById(R.id.process_text);

        mRequestForExperienceButton = (Button) findViewById(R.id.request_button);
        mRequestForExperienceButton.setEnabled(mDisclaimerCheckbox.isChecked());
        mSystemUpdateButton = (Button) findViewById(R.id.update_button);
        mSystemUpdateButton.setVisibility(View.GONE);
        //mLayoutFooter = (LinearLayout) findViewById(R.id.experience_version_footer);
        mLayoutFooterCheckbox = (RelativeLayout) findViewById(R.id.experience_version_footer_checkbox);
        request_button_margin = (LinearLayout) findViewById(R.id.request_button_margin);
        request_button_margin.setVisibility(View.GONE);

        mExperienceVersionIntroduce = (TextView) findViewById(R.id.introduce_description);

        initWebView();

    }

    private void initWebView() {
        mWebViewParent = (LinearLayout) findViewById(R.id.webview_parent);
        mWebView = new WebView(getApplicationContext());
        mWebView.setVerticalScrollBarEnabled(false);
        mWebView.setHorizontalFadingEdgeEnabled(false);
        mWebView.setVerticalFadingEdgeEnabled(false);
        mWebView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        // Add by zhiqiang.dong@gometech.com, 20171024, transparent webview background
        mWebView.setBackgroundColor(0);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                // *** NEVER DO THIS!!! ***
                // super.onReceivedSslError(view, handler, error);
                // let's ignore ssl error
                handler.proceed();
            }
        });
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mWebViewParent.addView(mWebView);
    }

    private String parseIntroduceWithHtml(String targetVersionNo, long updatePackageSize, String publicDesc) {
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
        } else if (lang.equals("zh") && (country == null || country.isEmpty())) {
            country = "CN";
        }
        StringBuffer sLicenseFile = new StringBuffer();
        sLicenseFile.append(Constants.PATH_APPLY_FOR_BETA_VERSION).append("_").append(country).append(".html");
        try {
            InputStream inputStream = getAssets().open(sLicenseFile.toString());
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            String result = new String(buffer);
            result = result.replace("UPDATE_PACKAGE_NAME", targetVersionNo);
            result = result.replace("UPDATE_PACKAGE_SIZE", formatPackageSize(updatePackageSize));
            result = result.replace("UPDATE_PACKAGE_NEW",
                    ActivityUtils.getSummaryText(publicDesc.substring(publicDesc.indexOf("</p>") + "</p>".length())));
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private CharSequence getSpanString() {
        StringBuilder builder = new StringBuilder();
        SpannableString spannableString = new SpannableString(builder
                .append(getResources().getString(R.string.agree_disclaimer))
                .append(" ")
                .append(getResources().getString(R.string.disclaimer)));
        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent();
                intent.setClassName("com.gome.usercenter", "com.gome.usercenter.activity.ShowPolicyActivity");
                intent.putExtra(LICENSE_PATH, Constants.PATH_DISCLAIMER_WARRANTY);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }

            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        }, spannableString.length() - getResources().getString(R.string.disclaimer).length(),
                    spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }


    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.agree_disclaimer_checkbox:
                CheckBox cb = (CheckBox) view;
                mRequestForExperienceButton.setEnabled(cb.isChecked());
                break;
            case R.id.request_button:
                applyForExperienceVersion();
                break;
            case R.id.update_button:
                goToSystemUpdate();
                break;
            default:
                break;
        }
    }

    private boolean checkNetworkAccountEnv(){
        Log.d(TAG, "checkNetworkAccountEnv");
        if(!ActivityUtils.checkNetworkConnection(ExperienceVersionActivity.this, true)){
            DialogUtils.progressDialogDismiss();
            setViewStatus(false);
            mExperienceStatus.setVisibility(View.INVISIBLE);
            return false;
        }
        if(!AccountUtils.getGomeAccountLoginState(mContext)){
            DialogUtils.progressDialogDismiss();
            showLoginDialog();
            return false;
        }
        return true;
    }

    private void showDialogLoading(){
        DialogUtils.setProgressingDialog(mContext, null, getResources().getString(R.string.progress_dialog_loading));
    }

    private void showDialogApplying(){
        DialogUtils.setProgressingDialog(mContext, null, getResources().getString(R.string.experience_applying));
    }

    private void checkForExperienceVersion(){
        Log.d(TAG, "checkForExperienceVersion");
        dismissLoginDialog();
        showDialogLoading();

        String check_url = Constants.GOME_BASE_URL + "system/experiencecheck";
        String checkJsonString = getCheckExperienceJson(mContext);
        NetworkUtils.doPost(mContext, check_url, new HttpCallbackStringListener() {
            @Override
            public void onFinish(String response) {
                DialogUtils.progressDialogDismiss();
                String result = ActivityUtils.getDescryptJsonString(response);

                VersionInfo versionInfo = new VersionInfo();
                try {
                    JSONObject jsonObj = new JSONObject(result);
                    String resCode = JsonUtils.getString(jsonObj, "resCode");
                    String resMsg = JsonUtils.getString(jsonObj, "resMsg");
                    String experienceRequest = JsonUtils.getString(jsonObj, "experienceRequest");
                    String publicDesc = JsonUtils.getString(jsonObj, "publicDesc");
                    String projectName = JsonUtils.getString(jsonObj, "projectName");
                    String publicDate = JsonUtils.getString(jsonObj, "publicDate");
                    String sourceVersionNo = JsonUtils.getString(jsonObj, "sourceVersionNo");
                    String targetVersionNo = JsonUtils.getString(jsonObj, "targetVersionNo");
                    String publicVersionNo = JsonUtils.getString(jsonObj, "publicVersionNo");
                    long packageSize = JsonUtils.getLong(jsonObj, "packageSize");

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

                        updateViewByVersionInfo(versionInfo);

                    }else if(Constants.RESPONSE_CODE_TOKEN_INVAILD.equals(resCode)
                            || Constants.RESPONSE_CODE_NOT_LOGIN.equals(resCode)
                            || Constants.RESPONSE_CODE_PLEASE_LOGIN.equals(resCode)){
                        gotoGomeAccountService = true;
                        ActivityUtils.setToastShow(mContext, R.string.gome_account_token_invalid);
                        Intent intentBroadcast = new Intent(Constants.ACTION_START_ACTIVITY_LOGIN);
                        sendBroadcast(intentBroadcast);
                    }else{
                        setViewStatus(false);
                    }
                } catch (JSONException e) {
                    Log.d(TAG, "Parse Json error");
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Exception e) {
                Log.d(TAG, "e = " + e.toString());
                DialogUtils.progressDialogDismiss();
                ActivityUtils.setToastShow(getApplicationContext(), R.string.alert_network_unavaiable);
                setViewStatus(false);
                mExperienceStatus.setVisibility(View.INVISIBLE);
            }
        }, checkJsonString);

    }

    private void updateViewByVersionInfo(final VersionInfo versionInfo){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                /* modified-begin by zhiqiang.dong@gometech.com.cn PR: GMOS2X1-2189*/
                if(Constants.RESPONSE_CODE_SUCCESS.equals(versionInfo.getResCode())
                        && !"".equals(versionInfo.getTargetVersionNo())
                        && versionInfo.getTargetVersionNo() != null){
                /* modified-end */
                    Log.d(TAG, "RESPONSE_CODE_SUCCESS");
                    setViewStatus(true);
                    if(("1").equals(versionInfo.getExperienceRequest())){
                        mRequestStatus.setText(getString(R.string.request_experience_success));
                        mRequestStatus.setTextColor(mContext.getResources().getColor(R.color.gome_base_color_dark_blue));
                        /* modified by zhiqiang.dong@gometech.com.cn PR: GMOS2X1-2189*/
                        mRequestStatus.setVisibility(View.GONE);
                        mLayoutFooterCheckbox.setVisibility(View.GONE);
                        mRequestForExperienceButton.setEnabled(false);
                        request_button_margin.setVisibility(View.VISIBLE);
                        mSystemUpdateButton.setVisibility(View.VISIBLE);
                        mRequestForExperienceButton.setVisibility(View.GONE);
                    }else{
                        mRequestStatus.setText(getString(R.string.request_experience_failed));
                        mRequestStatus.setTextColor(mContext.getResources().getColor(R.color.dark_foreground));
                        /* modified by zhiqiang.dong@gometech.com.cn PR: GMOS2X1-2189*/
                        mRequestStatus.setVisibility(View.VISIBLE);
                        mRequestForExperienceButton.setEnabled(mDisclaimerCheckbox.isChecked());
                        mLayoutFooterCheckbox.setVisibility(View.VISIBLE);
                        request_button_margin.setVisibility(View.GONE);
                        mSystemUpdateButton.setVisibility(View.GONE);
                        mRequestForExperienceButton.setVisibility(View.VISIBLE);
                    }

                    Log.d(TAG, "versionInfo.getTargetVersionNo() = " + versionInfo.getTargetVersionNo());
                    if(versionInfo.getTargetVersionNo() != null){
                        String data = parseIntroduceWithHtml(versionInfo.getTargetVersionNo(),
                                versionInfo.getUpdatePackageSize(), versionInfo.getPublicDesc());
                        if(null!=mWebView) {
                            mWebView.loadDataWithBaseURL(null, data, "text/html", "utf-8", null);
                        }
                        //mExperienceVersionIntroduce.setText(introduceBuilder(versionInfo.getTargetVersionNo(),
                        //        versionInfo.getUpdatePackageSize(), versionInfo.getPublicDesc()));
                    }else{
                        if(null!=mWebViewParent) {
                            mWebViewParent.setVisibility(View.GONE);
                        }
                        //mExperienceVersionIntroduce.setVisibility(View.GONE);
                    }
                }else{
                    Log.d(TAG, "RESPONSE_CODE_FAIL");
                    setViewStatus(false);
                }
            }
        });
    }

    private String introduceBuilder(String targetVersionNo, long updatePackageSize, String publicDesc) {
        StringBuilder builder = new StringBuilder();
        builder.append(getResources().getString(R.string.experience_version_introduce_summary)).append("\n")
                .append(targetVersionNo).append("\n\n")
                .append(getResources().getString(R.string.experience_version_introduce_size)).append("\n")
                .append(formatPackageSize(updatePackageSize)).append("\n\n")
                //.append(getResources().getString(R.string.experience_version_introduce_description)).append("\n")
                .append(Html.fromHtml(ActivityUtils.getSummaryText(publicDesc)));
        return builder.toString();
    }

    private String formatPackageSize(double updatePackageSize) {
        StringBuilder builder = new StringBuilder();
        /*DecimalFormat decimalFormat = new DecimalFormat(".0");
        if (updatePackageSize < 1000) {
            return builder.append(updatePackageSize).append("K").toString();
        } else */
        if (updatePackageSize < 1000 * 1000) {
            return builder.append(String.format("%.01fK", ((float) updatePackageSize /1024))).toString();
        } else {
            return builder.append(String.format("%.01fM", ((float) updatePackageSize /1024 /1024))).toString();
        }
    }

    private void setViewStatus(boolean status){
        if(status){
            mProcessText.setVisibility(View.INVISIBLE);
            mFrameBody.setVisibility(View.VISIBLE);
            mExperienceStatus.setVisibility(View.INVISIBLE);
        }else{
            mProcessText.setVisibility(View.INVISIBLE);
            mFrameBody.setVisibility(View.INVISIBLE);
            mExperienceStatus.setVisibility(View.VISIBLE);
        }
    }

    private void applyForExperienceVersion(){
        if(!ActivityUtils.checkNetworkConnection(mContext, true)){
            setViewStatus(false);
            return;
        }
        if (!AccountUtils.getGomeAccountLoginState(this)) {
            showLoginDialog();
            return;
        }
        dismissLoginDialog();
        showDialogApplying();

        Log.d(TAG, "applyForExperienceVersion");
        String request_url = Constants.GOME_BASE_URL + "system/experienceapply";
        String requestJsonString = getApplyExperienceJson(this);
        NetworkUtils.doPost(this, request_url, new HttpCallbackStringListener() {
            @Override
            public void onFinish(String response) {
                DialogUtils.progressDialogDismiss();
                String result = ActivityUtils.getDescryptJsonString(response);

                try {
                    JSONObject jsonObj = new JSONObject(result);
                    String resCode = JsonUtils.getString(jsonObj, "resCode");
                    String resMsg = JsonUtils.getString(jsonObj, "resMsg");
                    Log.d(TAG, "resCode = " + resCode);
                    Log.d(TAG, "resMsg = " + resMsg);
                    if(Constants.RESPONSE_CODE_SUCCESS.equals(resCode)){
                        Log.d(TAG, "set request status blue");
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                mRequestStatus.setText(getString(R.string.request_experience_success));
                                mRequestStatus.setTextColor(mContext.getResources().getColor(R.color.gome_base_color_dark_blue));
                                mRequestForExperienceButton.setEnabled(false);
                                showUpdateSystemDialog();
                                mSystemUpdateButton.setVisibility(View.VISIBLE);
                                mRequestForExperienceButton.setVisibility(View.GONE);
                                mLayoutFooterCheckbox.setVisibility(View.GONE);
                                request_button_margin.setVisibility(View.VISIBLE);
                            }
                        });
                        setExperienceResult();
                    }else if(Constants.RESPONSE_CODE_TOKEN_INVAILD.equals(resCode)
                            || Constants.RESPONSE_CODE_NOT_LOGIN.equals(resCode)
                            || Constants.RESPONSE_CODE_PLEASE_LOGIN.equals(resCode)){
                        gotoGomeAccountService = true;
                        ActivityUtils.setToastShow(mContext, R.string.gome_account_token_invalid);
                        Intent intentBroadcast = new Intent(Constants.ACTION_START_ACTIVITY_LOGIN);
                        sendBroadcast(intentBroadcast);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onError(Exception e) {
                DialogUtils.progressDialogDismiss();
                Log.d(TAG, "e = " + e.toString());
            }
        }, requestJsonString);

    }

    public String getCheckExperienceJson(Context context){
        String imei = ActivityUtils.getDeviceID(context);
        String token = AccountUtils.getGomeAccountTokenValue(context);
        String curSystemVersion = ActivityUtils.getVersionInternal();
        String deviceRoot = ActivityUtils.isRootSystem();

        JSONObject jsonObject = new JSONObject();
        try{
            jsonObject.put("imei", imei);
            jsonObject.put("token", token);
            jsonObject.put("curSystemVersion", curSystemVersion);
            jsonObject.put("deviceRoot", "0");
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return ActivityUtils.getEncryptJsonString(jsonObject.toString());
    }

    public String getApplyExperienceJson(Context context){
        String imei = ActivityUtils.getDeviceID(context);
        String token = AccountUtils.getGomeAccountTokenValue(context);
        int experience = 1;
        JSONObject jsonObject = new JSONObject();
        try{
            jsonObject.put("imei", imei);
            jsonObject.put("token", token);
            jsonObject.put("experience", experience);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return ActivityUtils.getEncryptJsonString(jsonObject.toString());
    }

    private void setExperienceResult(){
        String ACTION_SET_EXPERIENCE_REQUEST = "com.gome.usercenter.ACTION_EXPERIENCE_REQUEST";
        String token = AccountUtils.getGomeAccountTokenValue(getApplicationContext());
        Log.d(TAG, "setExperienceResult token = " + token);
        if(null == token)
            return;
        Intent intentBadge = new Intent();
        intentBadge.setAction(ACTION_SET_EXPERIENCE_REQUEST);
        intentBadge.putExtra("token", token);
        Log.d(TAG, "send ACTION_SET_EXPERIENCE_REQUEST");
        this.sendBroadcast(intentBadge);
    }

    private GomeAlertDialog loginDialog;
    private void showLoginDialog(){
        if(loginDialog != null && loginDialog.isShowing()) {
            return;
        }
        final GomeAlertDialog.Builder builder = new GomeAlertDialog.Builder(this)
                .setTitle(R.string.dialog_login_alert_apply_for_experience)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                            finish();
                    }
                })
                .setPositiveButton(R.string.proceedTo, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        gotoGomeAccountService = true;
                        Intent intentBroadcast = new Intent(Constants.ACTION_START_ACTIVITY_LOGIN);
                        sendBroadcast(intentBroadcast);
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        loginDialog = builder.create();
        loginDialog.setCanceledOnTouchOutside(false);
        loginDialog.show();
    }

    private void showUpdateSystemDialog() {
        final GomeAlertDialog.Builder builder = new GomeAlertDialog.Builder(this)
                .setTitle(R.string.experience_version_apply_success_title)
                .setMessage(R.string.experience_version_apply_success_message)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setPositiveButton(R.string.system_update, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        goToSystemUpdate();
                    }
                });
        builder.create().show();
    }

    private void goToSystemUpdate() {
        Intent intent = new Intent();
        intent.setClassName("com.gome.fotasimple", "com.gome.fotasimple.activity.FotaActivity");
        startActivity(intent);
    }

    private void dismissLoginDialog(){
        if(loginDialog != null && loginDialog.isShowing()){
            loginDialog.dismiss();
            loginDialog = null;
        }
    }
}
