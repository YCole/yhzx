package com.gome.usercenter.activity;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.util.TypedValue;

import com.gome.usercenter.utils.DialogUtils;
import com.gome.usercenter.utils.NetworkStateObserver;
import com.gome.usercenter.view.CircleImageView;
import com.gome.usercenter.view.GomeScrollView;
import com.gome.usercenter.helper.HttpCallbackStringListener;
import com.gome.usercenter.helper.PermissionHelper;
import com.gome.usercenter.utils.AccountUtils;
import com.gome.usercenter.utils.ActivityUtils;
import com.gome.usercenter.utils.Constants;
import com.gome.usercenter.utils.ImageUtils;
import com.gome.usercenter.utils.JsonUtils;
import com.gome.usercenter.utils.LinkifyUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Calendar;

import com.gome.usercenter.R;
import com.gome.usercenter.utils.NetworkUtils;

import gome.app.GomeAlertDialog;

//add import for gome account start
import android.accounts.AccountManager;
import android.accounts.IAccountAuthenticator;
import android.accounts.IAccountAuthenticatorResponse;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
//add import for gome account end

public class HomeActivity extends BaseActivity {

    private static final String TAG = Constants.TAG_HEADER + "HomeActivity";

    private static final String LICENSE_PATH = "license_path";
    private View mGomeAccount;
    private GridView mTargetsView;
    private SimpleAdapter mTargetsAdapter;
    private List<Map<String, Object>> mTargetsList;
    private Context mContext;

    private CircleImageView mImageAvatar;
    private TextView mTextViewUserEmail;
    private TextView mTextViewUserLevel;

    //gome account start
    public Intent AccountIntent;
    public final String SERIALIZE_NAME = "accountInfo";
    public Handler mHandler = new Handler();
    static IAccountAuthenticator mAuthenticator = null;
    static AccountAuthenticatorResponse response = null;

    public static final int REQUEST_CODE_LOGIN = 1;
    public static final int REQUEST_CODE_LOGIN_OUT = 2;
    public static final int REQUEST_CODE_RESET_PWD = 3;
    public static final int REQUEST_CODE_REGISTER = 4;
    public static final int REQUEST_CODE_LOGIN_INFO = 5;
    public static final int REQUEST_CODE_WEIBO_LOGIN = 6;

    private Bitmap mBitmap = null;  //头像的bitmap

    String mServerToken = null;
    String mNickName = null;
    String mMallAddress = null;
    String mPhoneNo = null;
    String mGomeId = null;
    String mRegisterType = null;
    String mLoginPwd = null;
    String mLocalAvatarPath = null;
    String mSex = null;
    String mUserLevel = null;
    //gome account end

    private boolean mAllGranted;

    private boolean mServiceBindSuccess = false;
    private boolean mInitAccountService = false;
    private boolean mRegisterConnectivityChange = false;
    private String extra = null;

    private ConnectivityChangeRecevier recevier = new ConnectivityChangeRecevier();
    private class ConnectivityChangeRecevier extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG,"Connectivity change action ?" + action);
            if("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
                if(ActivityUtils.isNetworkConnected(mContext)) {
                    doAuthFromService(Constants.KEY_AUTH_TOKEN_TYPE_GET_LOGIN_STATE);
                    mUIhandler.sendEmptyMessage(1);
                }
            }
        }
    }

    private Handler mUIhandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    DialogUtils.setProgressingDialog(mContext, null, mContext.getResources().getString(R.string.account_progress_dialog_loading));
                    break;
                case 2:
                    DialogUtils.progressDialogDismiss();
                    break;
                case 3:
                    doAuthFromService(Constants.KEY_AUTH_TOKEN_TYPE_GET_LOCAL_LOGIN_STATE);
                    ActivityUtils.setToastShow(mContext, R.string.account_progress_dialog_error);
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    };

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Constants.ACTION_START_ACTIVITY_LOGIN.equals(action)) {
                Log.d(TAG, "ACTION_START_ACTIVITY_LOGIN");
                extra = intent.getStringExtra(Constants.KEY_START_MODE_TARGET_CLASS);
                startLoginActivity();
            } else if(Constants.ACTION_GOME_ACCOUNT_UPDATE_INFO.equals(action)){
                Log.d(TAG, "ACTION_GOME_ACCOUNT_UPDATE_INFO");
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

                Log.d(TAG, "mReceiver update account info");
                updateGomeAccountView(Constants.KEY_SERVER_AUTHEN_SUCCESS);
            } else if(Constants.ACTION_GOME_ACCOUNT_LOGOUT.equals(action)){
                Log.d(TAG, "ACTION_GOME_ACCOUNT_LOGOUT");
                updateGomeAccountView(Constants.KEY_SERVER_AUTHEN_LOGIN_TIMEOUT);
            } else if(Constants.ACTION_REFRESH_ACCOUNT_INFO.equals(action)){
                Log.d(TAG, "ACTION_REFRESH_ACCOUNT_INFO");
                updateGomeAccountView(Constants.KEY_SERVER_AUTHEN_SUCCESS);
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "version: " + Constants.VERSION);
        setContentView(R.layout.activity_user_center);
        mContext = this;
        initView();

        mAllGranted = false;

        PermissionHelper.init(HomeActivity.this);

        if (mAllGranted) {
            showAgreementDialog();
        } else {
            List<String> permissionsRequestList = PermissionHelper.getInstance()
                    .getAllUngrantedPermissions();
            if (permissionsRequestList.size() > 0) {
                PermissionHelper.getInstance().requestPermissions(permissionsRequestList,
                        mPermissionCallback);
            } else {
                showAgreementDialog();
            }
        }
        if(mInitAccountService){
            initAfterSaleAddress();
            initGomeAccountService();
            registerNetworkChange();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_START_ACTIVITY_LOGIN);
        filter.addAction(Constants.ACTION_GOME_ACCOUNT_LOGOUT);
        filter.addAction(Constants.ACTION_GOME_ACCOUNT_UPDATE_INFO);
        filter.addAction(Constants.ACTION_REFRESH_ACCOUNT_INFO);
        mContext.registerReceiver(mReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        if (mServiceBindSuccess && conn != null) {
            unbindService(conn);
        }
        mContext.unregisterReceiver(mReceiver);
        response = null;
        mAuthenticator = null;
        PermissionHelper.getInstance().release();
        if(mRegisterConnectivityChange){
            getApplicationContext().unregisterReceiver(recevier);
        }
    }

    private void registerNetworkChange(){
        getApplicationContext().registerReceiver(recevier,
            new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        mRegisterConnectivityChange = true;
    }

    private void initGomeAccountService(){
        if(null == mAuthenticator){
            bindAuthenService();
        }else{
            doAuthFromService(Constants.KEY_AUTH_TOKEN_TYPE_GET_LOGIN_STATE);
            mUIhandler.sendEmptyMessage(1);
        }
    }

    private void showAgreementDialog() {
        if(ActivityUtils.isAgreementDialogHadShown(getApplication().getBaseContext())){
            mInitAccountService = true;
            return;
        }

        final GomeAlertDialog.Builder builder = new GomeAlertDialog.Builder(this).setTitle(R.string.user_agreement)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        if(!ActivityUtils.isAgreementDialogHadShown(getApplication().getBaseContext())){
                            Log.d(TAG, "dialog cancel finish()");
                            finish();
                        }
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityUtils.setAgreementDialogHadShown(getApplication().getBaseContext(), true);
                        initAfterSaleAddress();
                        initGomeAccountService();
                        registerNetworkChange();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });

        TypedValue value = new TypedValue();
        getResources().getValue(R.dimen.gome_padding_size_365dp, value, true);
        int needHeight = (int)TypedValue.complexToFloat(value.data);

        View view = LayoutInflater.from(this).inflate(R.layout.dlg_agreement, null);
        GomeScrollView scroll_view =(GomeScrollView) view.findViewById(R.id.scroll_view);
        scroll_view.setScrollViewHeight(needHeight);
        TextView agreementDescription = (TextView) view.findViewById(R.id.user_agreement_decription);
        final StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append(getText(R.string.user_agreement_decription));
        // modified by zhiqiang.dong@gometech.com.cn, 20171025
        LinkifyUtils.linkify(mContext, agreementDescription, contentBuilder, new LinkifyUtils.OnClickListener() {
            @Override
            public void onClick(int id) {
                Intent intent = new Intent();
                intent.setClassName("com.gome.usercenter", "com.gome.usercenter.activity.ShowPolicyActivity");
                switch (id) {
                    case 0:
                        intent.putExtra(LICENSE_PATH, Constants.PATH_USER_AGREEMENT);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent);
                        break;
                    case 1:
                        intent.putExtra(LICENSE_PATH, Constants.PATH_PRIVACY_POLICY);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent);
                        break;
                    default:break;
                }
            }
        });
        builder.setView(view);

        //GMOS-4855 start
        //builder.setCancelable(false);
        //GMOS-4855 end
        GomeAlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void initView() {
        setCentralTitleVisible();

        mGomeAccount = findViewById(R.id.gome_account);

        mImageAvatar = (CircleImageView) mGomeAccount.findViewById(R.id.user_image);
        mTextViewUserEmail = (TextView) mGomeAccount.findViewById(R.id.account_title);
        mTextViewUserLevel = (TextView) mGomeAccount.findViewById(R.id.account_summary);

        mTargetsView = (GridView) findViewById(R.id.user_center_targets);
        initAdapterData();
        String[] from = {"label", "icon"};
        int[] to = {R.id.label,R.id.icon};
        mTargetsAdapter = new SimpleAdapter(this, mTargetsList, R.layout.center_targets_item, from, to);
        mTargetsView.setAdapter(mTargetsAdapter);
        mTargetsView.setOnItemClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(AdapterView<?> parent, View view, int position, long id) {
                Map map = mTargetsList.get(position);
                if (map != null && map.get("packageName") != null && map.get("className") != null) {
                    Intent intent = new Intent();
                    intent.setClassName((String) map.get("packageName"), (String) map.get("className"));
                    if (map.get("className").equals("com.gome.usercenter.activity.ShowPolicyActivity")) {
                        intent.putExtra(LICENSE_PATH, Constants.PATH_SERVICE_POLICY);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    }
                    startActivity(intent);
                }

            }
        });
        mGomeAccount.setOnClickListener(ButtonOnClickListener);
    }

    private void initAdapterData() {
        String[] targetsPackageName = getResources().getStringArray(R.array.center_targets_package_name);
        String[] targetsClassName = getResources().getStringArray(R.array.center_targets_class_name);
        TypedArray iconArray = getResources().obtainTypedArray(R.array.center_targets_icon);
        String[] labelArray = getResources().getStringArray(R.array.center_targets_label);
        mTargetsList = new ArrayList<>();
        for (int i = 0; i < labelArray.length ; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("label", labelArray[i]);
            if (i < iconArray.length()) {
                map.put("icon", iconArray.getResourceId(i, R.drawable.smile));
            } else {
                map.put("icon", R.drawable.smile);
            }
            if (i < targetsClassName.length) {
                map.put("packageName", targetsPackageName[i]);
                map.put("className", targetsClassName[i]);
            } else {
                map.put("packageName", null);
                map.put("className", null);
            }
            mTargetsList.add(map);
        }
    }

    void updateGomeAccountView(final String token){
        Log.d(TAG, "updateGomeAccountView token = " + token);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                if((Constants.KEY_SERVER_AUTHEN_SUCCESS).equals(token)){
                    Log.d(TAG, "login success");
                    String avatarPath = AccountUtils.getGomeAccountAvatarPathValue(mContext);
                    mBitmap = ImageUtils.getAvatarBitmapFromLocal(avatarPath);
                    String userPhoneNumber = AccountUtils.getGomeAccountPhoneNoValue(mContext);
                    String userEmail = AccountUtils.getGomeAccountEmailAddressValue(mContext);
                    String userId = AccountUtils.getGomeAccountIdValue(mContext);
                    String nickName = AccountUtils.getGomeAccountNickNameValue(mContext);
                    if(null!=userEmail && userEmail.length()>0) {
                        mTextViewUserLevel.setText(ActivityUtils.convertEmailWithStar(userEmail));
                    }
                    if(null!=userPhoneNumber && userPhoneNumber.length()>0) {
                        mTextViewUserLevel.setText(ActivityUtils.convertPhoneNumWithStar(userPhoneNumber));
                    }
                    if(null!=nickName && !nickName.isEmpty()) {
                        mTextViewUserEmail.setText(nickName);
                    } else {
                        mTextViewUserEmail.setText(R.string.nick_name_is_null);
                    }
                    //if(null!=userId){
                    //    mTextViewUserLevel.setText(userId);
                    //}
                    if(null != mBitmap){
                        mImageAvatar.setImageBitmap(mBitmap);
                    }
                }else{
                    showDefaultGomeAccountView();
                    Log.d(TAG, "login failed or logout");
                }

            }
        });
    }

    void showDefaultGomeAccountView(){
        mTextViewUserEmail.setText(getResources().getString(R.string.gome_account_title));
        mTextViewUserLevel.setText(getResources().getString(R.string.gome_accout_summary));
        mImageAvatar.setImageResource(R.drawable.gome_icon_usercenter_person);
    }
    
    View.OnClickListener ButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.gome_account:
                    Log.d(TAG, "start gome account service activity");
                    if (!AccountUtils.getGomeAccountLoginState(mContext)) {
                        if (!ActivityUtils.checkNetworkConnection(mContext, false)) {
                            return;
                        }
                        extra = Constants.VALUE_TARGET_HOME;
                        startLoginActivity();
                    } else {
                        startAccountInfoActivity();
                    }
                    break;
            }
        }
    };

    public abstract class NoDoubleClickListener implements AdapterView.OnItemClickListener{
        public static final int MIN_CLICK_DELAY_TIME = 500;
        private long lastClickTime = 0;
        public abstract void onNoDoubleClick(AdapterView<?> adapterView, View view, int i, long l);
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            long currentTime = Calendar.getInstance().getTimeInMillis();
            if (currentTime - lastClickTime > MIN_CLICK_DELAY_TIME){
                lastClickTime = currentTime;
                onNoDoubleClick(adapterView, view, i, l);
            }
        }
    }

    private void startLoginActivity(){
        doAuthFromService(Constants.KEY_AUTH_TOKEN_TYPE_LOGIN);
    }

    private void startAccountInfoActivity(){
        doAuthFromService(Constants.KEY_AUTH_TOKEN_TYPE_LOGIN_INFO);
    }

    private void bindAuthenService(){

        if(null == mAuthenticator){
            Log.i(TAG, "bindAuthenService 111");
            Intent intent = new Intent();
            intent.setAction("android.accounts.AccountAuthenticator");
            intent.setPackage("com.gome.gomeaccountservice");
            mServiceBindSuccess = bindService(intent, conn, Service.BIND_AUTO_CREATE);
        }

    }

    private void doAuthFromService(String authTokenType){
        if(null == mAuthenticator){
            bindAuthenService();
        }
        if(null != authTokenType && null != mAuthenticator){
            try {
                response = new AccountAuthenticatorResponse();
                Bundle bundle = new Bundle();
                mAuthenticator.getAuthToken(response, null,authTokenType, null);

            } catch (Exception e) {
                // TODO Auto-generated catch block
                Log.e(TAG,"doAuthFromServer error:"+e.toString());
                e.printStackTrace();
            }
        }
    }
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mAuthenticator = IAccountAuthenticator.Stub.asInterface(service);
            Log.d(TAG,"onServiceConnected: " + mAuthenticator);
            doAuthFromService(Constants.KEY_AUTH_TOKEN_TYPE_GET_LOGIN_STATE);
            mUIhandler.sendEmptyMessage(1);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(TAG,"onServiceDisconnected");
            mAuthenticator = null;
        }
    };

    private class AccountAuthenticatorResponse extends IAccountAuthenticatorResponse.Stub{
        public AccountAuthenticatorResponse(){
        }
        @Override
        public void onResult(Bundle result) {
            String authToken = result.getString(AccountManager.KEY_AUTHTOKEN);
            String authtokenType = result.getString(Constants.PARAM_AUTHTOKEN_TYPE);
            String accountName = result.getString(AccountManager.KEY_ACCOUNT_NAME);
            String accountType = result.getString(AccountManager.KEY_ACCOUNT_TYPE);
            AccountIntent = (Intent)result.getParcelable(AccountManager.KEY_INTENT);

            if(null != authtokenType){
                if(authtokenType.equals(Constants.KEY_AUTH_TOKEN_TYPE_GET_LOGIN_STATE)){
                    Log.i(TAG, "KEY_AUTH_TOKEN_TYPE_GET_LOGIN_STATE");
                    mUIhandler.sendEmptyMessage(2);
                    if(null != authToken && authToken.equals(Constants.KEY_SERVER_AUTHEN_SUCCESS)){
                        refreshGomeAccountPref(result);
                        Log.d(TAG, "update all sp value");
                    }else{
                        String mServerResCode = result.getString(Constants.KEY_SERVER_RESULT_CODE);
                        Log.i(TAG, "mServerResCode = " + mServerResCode);
                        if(Constants.SERVER_TIMEOUT_RESULT_CODE.equals(mServerResCode)){
                            mUIhandler.sendEmptyMessage(3);
                        }
                        AccountUtils.clearGomeAccountValue(mContext);
                    }
                    updateGomeAccountView(authToken);

                }else if(authtokenType.equals(Constants.KEY_AUTH_TOKEN_TYPE_LOGIN)){
                    Log.i(TAG, "KEY_AUTH_TOKEN_TYPE_LOGIN");
                    if(null != AccountIntent){
                        if(extra != null){
                            AccountIntent.setAction(Constants.USER_CENTER_MODE);
                            AccountIntent.putExtra(Constants.KEY_START_MODE_LOGIN_FEEDBACK, extra);
                            extra = null;
                        }else{
                            AccountIntent.setAction(Constants.USER_CENTER_MODE);
                        }
                        startActivityForResult(AccountIntent,REQUEST_CODE_LOGIN);
                    }
                }else if(authtokenType.equals(Constants.KEY_AUTH_TOKEN_TYPE_LOGIN_INFO)){
                    Log.i(TAG, "KEY_AUTH_TOKEN_TYPE_LOGIN_INFO");
                    if(null != AccountIntent){
                        startActivityForResult(AccountIntent,REQUEST_CODE_LOGIN_INFO);
                    }
                }else if(authtokenType.equals(Constants.KEY_AUTH_TOKEN_TYPE_GET_LOCAL_LOGIN_STATE)){
                    Log.i(TAG, "KEY_AUTH_TOKEN_TYPE_GET_LOCAL_LOGIN_STATE");
                    if(null != authToken && authToken.equals(Constants.KEY_SERVER_AUTHEN_SUCCESS)){
                        refreshGomeAccountPref(result);
                        Log.d(TAG, "update all sp value with local token");
                    }else{
                        AccountUtils.clearGomeAccountValue(mContext);
                    }
                    updateGomeAccountView(authToken);
                }
            }

        }
        @Override
        public void onRequestContinued(){
            Log.i(TAG, "AccountAuthenticatorResponse.onRequestContinued");
        }
        @Override
        public void onError(int errorCode, String errorMessage){
            Log.i(TAG, "AccountAuthenticatorResponse.onError errorCode:"+errorCode+" errorMessage:"+errorMessage);
        }

    }


    // / M: add for runtime permission check @{
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        Log.d(TAG, " onRequestPermissionsResult " + requestCode);
        PermissionHelper.getInstance().onPermissionsResult(requestCode, permissions, grantResults);
    }

    private PermissionHelper.PermissionCallback mPermissionCallback =
            new PermissionHelper.PermissionCallback() {
                public void onPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
                    if (grantResults != null && grantResults.length > 0) {
                        mAllGranted = true;
                        for (int i = 0; i < grantResults.length; i++) {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                mAllGranted = false;
                                Log.d(TAG, permissions[i] + " is not granted !");
                                break;
                            }
                        }
                        if (!mAllGranted) {
                            String toastStr =
                                    getString(R.string.denied_required_permission);
                            Toast.makeText(getApplicationContext(), toastStr, Toast.LENGTH_SHORT).show();
                            finish();
                        }
                        showAgreementDialog();
                    }
                }
            };
    
    private void initAfterSaleAddress(){
        String token = AccountUtils.getGomeAccountTokenValue(getApplicationContext());
        if(null == token){
            return;
        }
        if(!ActivityUtils.isNetworkConnected(this)){
            return;
        }

        Log.d(TAG, "initAfterSaleAddress()");
        String requestJsonString = getRequestJsonForAfterSaleAddress(this);
        String requestUrl = Constants.WF_API_URL + Constants.URL_AREA_LIST;
        NetworkUtils.doPost(this, requestUrl, new HttpCallbackStringListener() {
            @Override
            public void onFinish(String response) {
                parseAddressResult(response);
            }

            @Override
            public void onError(Exception e) {
                Log.d(TAG, "e = " + e.toString());
            }
        }, requestJsonString);
    }

    private String getRequestJsonForAfterSaleAddress(Context context){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("sign", Constants.SIGN_AREA_LIST);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return jsonObject.toString();
    }

    private void parseAddressResult(String result){
        try {
            JSONObject jsonObj = new JSONObject(result);
            String resCode = JsonUtils.getString(jsonObj, "resultcode");
            String resMsg = JsonUtils.getString(jsonObj, "message");
            Log.d(TAG, "result code = " + resCode);
            if(Constants.WF_RESPONSE_CODE_SUCCESS.equals(resCode)){
                Log.i(TAG, "cache address, speed up loading");
                ActivityUtils.cacheApplicationData(mContext, Constants.FILE_CACHE_AFTER_SALE_ADDRESS, result);
            }else{

            }
        } catch (JSONException e) {
            Log.d(TAG, "parseAddressResult Json error");
            e.printStackTrace();
        }
    }

    private void refreshGomeAccountPref(Bundle result){
        mServerToken = result.getString(Constants.KEY_SERVER_TOKEN);
        mNickName = result.getString(Constants.KEY_ACCOUNT_NAME);
        mMallAddress = result.getString(Constants.KEY_ACCOUNT_EMAIL);
        mPhoneNo = result.getString(Constants.KEY_ACCOUNT_PHONE_NUMBER);
        mGomeId = result.getString(Constants.KEY_ACCOUNT_GOME_ID);
        mRegisterType = result.getString(Constants.KEY_ACCOUNT_REGISTER_TYPE);
        mLoginPwd = result.getString(Constants.KEY_ACCOUNT_PWD);
        mSex = result.getString(Constants.KEY_ACCOUNT_SEX);
        mUserLevel = result.getString(Constants.KEY_ACCOUNT_USER_LEVEL);
        mLocalAvatarPath = result.getString(Constants.KEY_LOCAL_AVATAR_PATH);

        SharedPreferences dsp = PreferenceManager.getDefaultSharedPreferences(mContext);
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
