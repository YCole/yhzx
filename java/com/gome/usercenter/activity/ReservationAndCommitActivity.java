package com.gome.usercenter.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Toast;

import com.gome.usercenter.R;
import com.gome.usercenter.helper.HttpCallbackStringListener;
import com.gome.usercenter.module.ReservationCardInfo;
import com.gome.usercenter.utils.AccountUtils;
import com.gome.usercenter.utils.ActivityUtils;
import com.gome.usercenter.utils.Constants;
import com.gome.usercenter.utils.DialogUtils;
import com.gome.usercenter.utils.JsonUtils;
import com.gome.usercenter.utils.NetworkUtils;
import com.gome.usercenter.view.ReservationListItem;
import com.gome.usercenter.view.citypicker.CityPicker;
import com.gome.usercenter.view.citypicker.TimePicker;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import gome.app.GomeAlertDialog;
import gome.app.GomeProgressDialog;

import static com.gome.usercenter.utils.NetworkUtils.requestBuilder;


/**
 * Created by jiang.zhang on 2018/3/7.
 */

public class ReservationAndCommitActivity extends BaseActivity implements View.OnClickListener,TextWatcher {

    private static final String TAG = Constants.TAG_HEADER + "ReservationAndCommitActivity";

    public static final String EXTRA_SHOW_FRAGMENT = "show_fragment";

    private ReservationListItem mUserName;
    private ReservationListItem mMobileNumber;
    private String mMachineInformation;
    private String mChooseStation;
    private String mArrivalTime;
    private String mIssue_Description;
    private String mSelect_Description;
    private String mReservationService_type;
    private String mMaintenanceMode;

    private GomeProgressDialog mProgDialog;
    private Button mCommitBtn;
    private ScrollView mScrollView;

    private View mActivityRootView;
    private int mGuessKeybordHeight;

    private Context mContext;

    private ReservationCardInfo mReservationCardInfo;

    private JSONObject mReservationJson;

    private Handler mUIHandler = new Handler();

    private int mDialogSelected = -1;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setBody(R.layout.activity_reservation_commit);
        mCustomTitle.setText(R.string.reserve_title);
        mContext = this;
        initView();
    }

    private void initView(){
        mScrollView = (ScrollView) findViewById(R.id.scroll_view);
        mUserName = (ReservationListItem) findViewById(R.id.user_name);
        mMobileNumber = (ReservationListItem) findViewById(R.id.mobile_number);
        mCommitBtn = (Button) findViewById(R.id.commit_btn);
        mUserName.setEditVisible(true);
        mUserName.setEditHint(getResources().getString(R.string.reserve_username_hint));
        mUserName.getEdit().setMaxLines(1);
        mUserName.getEdit().setSingleLine(true);
        mUserName.getEdit().setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        mMobileNumber.setEditVisible(true);

        mMobileNumber.getEdit().setInputType(InputType.TYPE_CLASS_PHONE);
        mActivityRootView = findViewById(android.R.id.content);
        mGuessKeybordHeight = getWindowManager().getDefaultDisplay().getHeight() / 4;
        mActivityRootView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (oldBottom != 0 && bottom != 0 && (oldBottom - bottom) > mGuessKeybordHeight) {
                    mUIHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            int[] location = new int[2];
                            mMobileNumber.getLocationInWindow(location);
                            mScrollView.scrollTo(location[0], location[1] + mScrollView.getScrollY());
                        }
                    });
                }
            }
        });
        mCommitBtn.setOnClickListener(this);
        mUserName.setOnClickListener(this);
        mUserName.getEdit().addTextChangedListener(this);
        mMobileNumber.setOnClickListener(this);
        mMobileNumber.getEdit().addTextChangedListener(this);
        mReservationCardInfo = new ReservationCardInfo();
        mReservationJson = new JSONObject();
        Intent intent = getIntent();
        mChooseStation = intent.getStringExtra("mchoose_station");
        mArrivalTime = intent.getStringExtra("marrival_time");
        mIssue_Description = PreferenceManager.getDefaultSharedPreferences(mContext).getString("Missue_description", "");
        mSelect_Description = PreferenceManager.getDefaultSharedPreferences(mContext).getString("Mselect_description", "");
        mReservationService_type = intent.getStringExtra("mreservation_type");
        mMaintenanceMode = mContext.getResources().getString(R.string.maintenance_mode_content);
        mCommitBtn.setEnabled(false);
        //Build build = new Build();
        //String model = build.MODEL;
        String mPhoneNumber = AccountUtils.getGomeAccountPhoneNoValue(mContext);
        if (!"".equals(mPhoneNumber)){
            mMobileNumber.setEditDefaultText(mPhoneNumber);
        }else {
            mMobileNumber.setEditHint(getResources().getString(R.string.reserve_mobile_number_hint));
        }
        //mMachineInformation = model;
        //mReservationCardInfo.setModelNumber(mMachineInformation);
        mReservationCardInfo.setReservationStation(mChooseStation);
        mReservationCardInfo.setIssueDescription(mIssue_Description);
        mReservationCardInfo.setTextSelect(mSelect_Description);
        mReservationCardInfo.setReservationTime(mArrivalTime);
        mReservationCardInfo.setMobileNumber(mMobileNumber.getSummary());
        mReservationCardInfo.setUserName(mUserName.getSummary());
        mReservationCardInfo.setMaintenanceMode(mMaintenanceMode);
    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.user_name:
                mUserName.getEdit().setCursorVisible(true);
                break;
            case R.id.mobile_number:
                mMobileNumber.getEdit().setCursorVisible(true);
                break;
            case R.id.commit_btn:
                mReservationCardInfo.setUserName(mUserName.getSummary());
                mReservationCardInfo.setMobileNumber(mMobileNumber.getSummary());
                if (mCommitBtn.isClickable()){
                    if(!ActivityUtils.checkNetworkConnection(mContext, false)){
                        return;
                    }
                    
                    if(!AccountUtils.getGomeAccountLoginState(this)){
                        showLoginDialog();
                        return;
                    }
                    commitReservationMessage();
                }
                break;
            default:break;
        }
    }


    private void commitReservationMessage() {
        if (!ActivityUtils.isMobileNO(mMobileNumber.getEdit().getText().toString())) {
            mScrollView.scrollTo(mMobileNumber.getScrollX(), mMobileNumber.getScrollY());
            mReservationCardInfo.setMobileNumber("");
            Toast.makeText(mContext, getResources().getString(R.string.toast_input_infomation_correct_format),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        mReservationCardInfo.setUserName(mUserName.getSummary());
        mReservationCardInfo.setMobileNumber(mMobileNumber.getSummary());

        HttpCallbackStringListener listener = new HttpCallbackStringListener() {
            public void onFinish(String response) {
                dissmissProgressDialog();
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String resultcode = jsonObject.getString("resultcode");
                    if (!"200".equals(resultcode)) {
                        Log.d(TAG, "mResultcode = " + resultcode);
                        return;
                    }
                    String results = jsonObject.getString("results");
                    JSONObject jsonResult = new JSONObject(results);
                    String reservationNumber = jsonResult.getString("reservationNumber");

                    Intent intent = new Intent(mContext, SubActivity.class);
                    intent.putExtra(EXTRA_SHOW_FRAGMENT, "com.gome.usercenter.fragment.ReservationSuccessFragment");
                    mReservationJson.put("reservationNumber", reservationNumber);
                    String value = mReservationJson.toString(4);
                    intent.putExtra("reservation_json", value);
                    startActivity(intent);
                    finish();
                    ReservationActivity.instance.finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            public void onError(Exception e) {
                Log.d(TAG, "commitReservationMessage error : " + e);
            }
        };
        tryCommitReservationMessage(mReservationCardInfo, listener);
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mUserName.setEditDefaultText("");
    }

    private void tryCommitReservationMessage(final ReservationCardInfo mReservationCardInfo,
            final HttpCallbackStringListener listener) {
        showProgressDialog();
        // the thread will simulate Asynchronous network request
        // rember replace with real Asynchronous network request if serve is accessible
        new Thread(new Runnable() {
            public void run() {
                String gomeAccount = AccountUtils.getGomeAccountIdValue(mContext);
                Log.d(TAG, "gomeaccount1 = " + gomeAccount);
                String imei = ActivityUtils.getDeviceID(mContext);
                String model = (new Build()).MODEL;
                String appointNetwork = mReservationCardInfo.getReservationStation();
                String problemDetail = mReservationCardInfo.getTextSelect() + mReservationCardInfo.getIssueDescription();

                String appointment = mReservationCardInfo.getReservationTime();
                Log.d(TAG, "appointment1 = " + appointment);
                // modified-begin by zhiqiang.dong, bugfix GMOS-4273
                appointment = appointment.substring(0, appointment.indexOf("-")).replace("年","-").replace("月","-").replace("日", "") + ":00";
                // modified-end
                Log.d(TAG, "appiontment2 =" + appointment);
                String name = mReservationCardInfo.getUserName();
                String tel = mReservationCardInfo.getMobileNumber();
                String accessType = "TYPE1";//always TYPE1[app]

                final String[] serviceTypeArray = mContext.getResources().getStringArray(R.array.maintenance_service_type);
                String serviceType = "TYPE1";
                String type = mReservationCardInfo.getServiceType();
                if(type.equals(serviceTypeArray[1])){
                    serviceType = "TYPE2";
                }else if(type.equals(serviceTypeArray[2])){
                    serviceType = "TYPE3";
                }
                String reservationType = "TYPE1";//always TYPE1[到店维修]

                try {
                    mReservationJson.put("gomeAccount", gomeAccount);
                    mReservationJson.put("imei", imei);
                    mReservationJson.put("model", model);
                    mReservationJson.put("appointNetwork", appointNetwork);
                    mReservationJson.put("problemDetail", problemDetail);
                    mReservationJson.put("appointment", appointment);
                    mReservationJson.put("name", name);
                    mReservationJson.put("tel", tel);
                    mReservationJson.put("accessType", accessType);
                    mReservationJson.put("reservationType", reservationType);
                    mReservationJson.put("serviceType", serviceType);

                    String request_url = Constants.WF_API_URL + Constants.URL_RESERVE_APPLY;
                    String requestJsonString = requestBuilder(
                            new String[]{"gomeAccount", "imei", "model", "appointNetwork", "problemDetail","appointment","name","tel","accessType","reservationType","serviceType"},
                            new String[]{gomeAccount, imei, model, appointNetwork, problemDetail, appointment, name, tel, accessType, reservationType, serviceType}
                    );
                    NetworkUtils.doPost(mContext, request_url, listener, requestJsonString);
                }catch(JSONException e){
                    Log.d(TAG, "build exception " + e);
                }catch(NoSuchAlgorithmException e){
                    Log.d(TAG, "build exception " + e);
                }catch(UnsupportedEncodingException e){
                    Log.d(TAG, "build exception " + e);
                }

            }
        }).start();
    }


    /**
     * 显示进度框
     */
    private void showProgressDialog() {
        if (mProgDialog == null) {
            mProgDialog = new GomeProgressDialog(this);
        }
        mProgDialog.setProgressStyle(GomeProgressDialog.STYLE_SPINNER);
        mProgDialog.setIndeterminate(false);
        mProgDialog.setCancelable(false);
        mProgDialog.setCanceledOnTouchOutside(false);
        mProgDialog.setMessage(mContext.getResources().getString(R.string.commit_dlg_message));
        mProgDialog.show();
    }

    /**
     * 隐藏进度框
     */
    private void dissmissProgressDialog() {
        if (mProgDialog != null) {
            mProgDialog.dismiss();
        }
    }


    private GomeAlertDialog loginDialog;
    private void showLoginDialog(){
        if(loginDialog != null && loginDialog.isShowing()) {
            return;
        }
        final GomeAlertDialog.Builder builder = new GomeAlertDialog.Builder(this)
                .setTitle(R.string.dialog_login_alert_apply_for_reservation)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                })
                .setPositiveButton(R.string.proceedTo, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
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

    private void dismissLoginDialog(){
        if(loginDialog != null && loginDialog.isShowing()){
            loginDialog.dismiss();
            loginDialog = null;
        }
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    public void afterTextChanged(Editable s) {
        if(mUserName.getEdit().getText().toString().trim().length()> 0 && mMobileNumber.getEdit().length() > 0 ){
            mCommitBtn.setEnabled(true);
        }else{
            mCommitBtn.setEnabled(false);
            mMobileNumber.setEditHint(getResources().getString(R.string.reserve_mobile_number_hint));
        }
    }
}
