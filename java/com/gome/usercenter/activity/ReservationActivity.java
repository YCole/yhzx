package com.gome.usercenter.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.text.InputType;
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
 * Created by dongzq on 2017/7/10.
 */

public class ReservationActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = Constants.TAG_HEADER + "ReservationActivity";

    public static final String EXTRA_SHOW_FRAGMENT = "show_fragment";
    private static final String EXTRA_TITLE_RESID = "extra_title_resid";
    private static final int FAILURE_ISSUE = 100;
    private static final int CHOOSE_STATION = 101;
    private static final String ISSUE_DESCRIPTION = "issue_description";
    private static final String SELECT_DESCRIPTION = "select_description";
    private static final String SELECTED_STATION = "selected_station";
    private static final String MCHOOSE_STATION = "mchoose_station";
    private static final String MARRIVAL_TIME = "marrival_time";
    private static final String MISSUE_DESCRIPTION = "missue_issue";
    private static final String MSELECT_DESCRIPTION = "mselect_description";
    private static final String MRESERVATION_TYPE = "mreservation_type";

    private ReservationListItem mMachineInformation;
//    private ReservationListItem mDistractBelong;
    private ReservationListItem mChooseStation;
    private ReservationListItem mArrivalTime;
    private ReservationListItem mFailureIssue;
    private ReservationListItem mReservationService_type;
//    private ReservationListItem mMaintenanceMode;
//    private ReservationListItem mUserName;
//    private ReservationListItem mMobileNumber;

    private String mChoose;
    private String mArrival;
    private String mFailure;
    private String mReservation;
    public static ReservationActivity instance;

    private ScrollView mScrollView;

    private GomeProgressDialog mProgDialog;

//    private Button mCommitBtn;

    private Button mNextBtn;
    private View mActivityRootView;
    private int mGuessKeybordHeight;


    private Context mContext;

    private ReservationCardInfo mReservationCardInfo;

    private JSONObject mReservationJson;

    private int mDialogSelected = -1;

    private Handler mUIHandler = new Handler();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setBody(R.layout.activity_reservation);
        mCustomTitle.setText(R.string.reserve_title);
        mContext = this;
        instance = this;
        initView();
    }

    private void initView() {
        mScrollView = (ScrollView) findViewById(R.id.scroll_view);
//        mMachineInformation = (ReservationListItem) findViewById(R.id.machine_information);
//        mMachineInformation.setMoreIconInvisible();
        mChooseStation = (ReservationListItem) findViewById(R.id.choose_station);
        mArrivalTime = (ReservationListItem) findViewById(R.id.arrival_time);
        mFailureIssue = (ReservationListItem) findViewById(R.id.failure_issue);
        mReservationService_type = (ReservationListItem) findViewById(R.id.reservation_service_type);
        mNextBtn = (Button) findViewById(R.id.next_step);
        mGuessKeybordHeight = getWindowManager().getDefaultDisplay().getHeight() / 4;
        mChooseStation.setOnClickListener(this);
        mArrivalTime.setOnClickListener(this);
        mFailureIssue.setOnClickListener(this);
        mReservationService_type.setOnClickListener(this);
        mNextBtn.setOnClickListener(this);
        mReservationCardInfo = new ReservationCardInfo();
        mReservationJson = new JSONObject();
        Intent intent = getIntent();
        if (intent.hasExtra("city") && intent.hasExtra("province") && (intent.hasExtra("address"))) {
            mReservationCardInfo.setCity(intent.getStringExtra("city"));
            mReservationCardInfo.setProvince(intent.getStringExtra("province"));
            mReservationCardInfo.setReservationStation(intent.getStringExtra("address"));
            mChooseStation.setSummary(mReservationCardInfo.getReservationStation());
        }
//        Build build = new Build();
//        String model = build.MODEL;
//        mMachineInformation.setSummary(model);
//        mReservationCardInfo.setModelNumber(mMachineInformation.getSummary());
    }



    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.machine_information:
//                // Modified-begin by zhiqiang.dong@gometech.com.cn, 2017/12/06, PRODUCTION-10326
//                hideSoftInput();
//                final String[] machineInformationArray
//                        = mContext.getResources().getStringArray(R.array.machine_information);
//                showListDialog(R.id.machine_information, machineInformationArray, mMachineInformation);
//                break;
            case R.id.choose_station:
                Intent intent2 = new Intent(mContext, SubActivity.class);
                intent2.putExtra(SubActivity.EXTRA_SHOW_FRAGMENT, "com.gome.usercenter.fragment.StationSelectFragment");
                intent2.putExtra(SubActivity.EXTRA_TITLE_RESID, R.string.reserve_title);
                if(!"".equals(mReservationCardInfo.getReservationStation())){
                    intent2.putExtra(SELECTED_STATION, mReservationCardInfo.getReservationStation());
                }
                startActivityForResult(intent2, CHOOSE_STATION);
                break;
            case R.id.arrival_time:
                // Modified-begin by zhiqiang.dong@gometech.com.cn, 2017/12/06, PRODUCTION-10326
                hideSoftInput();
                showDataPickerDialog();
                break;
            case R.id.failure_issue:
                Intent intent = new Intent(mContext, SubActivity.class);
                intent.putExtra(SubActivity.EXTRA_SHOW_FRAGMENT, "com.gome.usercenter.fragment.IssueDescriptionFragment");
                if (!"".equals(mReservationCardInfo.getIssueDescription())) {
                    intent.putExtra(ISSUE_DESCRIPTION, mReservationCardInfo.getIssueDescription());
                }
                if (!"".equals(mReservationCardInfo.getTextSelect())) {
                    intent.putExtra(SELECT_DESCRIPTION, mReservationCardInfo.getTextSelect());
                }
                startActivityForResult(intent, FAILURE_ISSUE);
                break;
            case R.id.reservation_service_type:
                // Modified-begin by zhiqiang.dong@gometech.com.cn, 2017/12/06, PRODUCTION-10326
                hideSoftInput();
                final String[] serviceTypeArray = mContext.getResources().getStringArray(R.array.maintenance_service_type);
                showListDialog(R.id.reservation_service_type, serviceTypeArray, mReservationService_type);
                break;

            case R.id.next_step:
                gotoReservationAndCommitActivity();
                break;
            default:break;
        }
    }
    /* Modified-begin by zhiqiang.dong@gometech.com.cn, 2017/12/06, PRODUCTION-10326 */
    private void hideSoftInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(mScrollView.getWindowToken(), 0);
        }
    }

    void gotoReservationAndCommitActivity(){
        Log.d(TAG, "go to ReservationAndCommitActivity");
        if(!ActivityUtils.checkNetworkConnection(ReservationActivity.this, false)){
            return;
        }
        if(AccountUtils.getGomeAccountLoginState(this)){
            Intent intent = new Intent();
            intent.setClass(this, ReservationAndCommitActivity.class);
            // startActivity(intent);
            intent.putExtra(MCHOOSE_STATION, mChooseStation.getSummary());
            intent.putExtra(MARRIVAL_TIME, mArrivalTime.getSummary());
            intent.putExtra(MRESERVATION_TYPE, mReservationService_type.getSummary());
            startActivityForResult(intent, Constants.RESERVATION_REQUEST_CODE);
        }else{
            showLoginDialog();
        }
    }
    

    protected void onResume() {
        super.onResume();
    }


    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        mChoose = mContext.getResources().getString(R.string.default_summary_station);
        mArrival = mContext.getResources().getString(R.string.default_summary_reservation_time);
        mFailure = mContext.getResources().getString(R.string.default_summary_issue_description);
        mReservation = mContext.getResources().getString(R.string.default_summary_service_type);
        if(hasFocus){
            if(!mChoose.equals(mChooseStation.getSummary() ) && !mArrival.equals(mArrivalTime.getSummary()) && !mFailure.equals(mFailureIssue.getSummary())
                    && !mReservation.equals(mReservationService_type.getSummary())){
                mNextBtn.setEnabled(true);
            }else{
                mNextBtn.setEnabled(false);
            }
        }
    }




    private void showDataPickerDialog() {
        final TimePicker timePicker = new TimePicker.Builder(mContext).textSize(15)
                .title(mContext.getResources().getString(R.string.reserve_timepicker))
                .Date("1月1日")
                .Time("08:00-10:00")
                .build();
        timePicker.show();
        timePicker.setOnTimeItemClickListener(new TimePicker.OnTimeItemClickListener() {
            public void onSelected(String... selected) {
                // modified-begin by zhiqiang.dong, bugfix GMOS-4273
                mReservationCardInfo.setReservationTime(selected[0] + selected[1] + " " + selected[2]);
                // modified-end
                mArrivalTime.setSummary(mReservationCardInfo.getReservationTime());
            }
            public void onCancel() {

            }
        });
    }

    private void showListDialog(final int type, final String[] array, final ReservationListItem item) {
        List arrayList = Arrays.asList(array);
        GomeAlertDialog.Builder builder = new GomeAlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.string_service_type))
                .setSingleChoiceItems(array, arrayList.indexOf(item.getSummary()), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        Log.d(TAG, "setSingleChoiceItems");
                        mDialogSelected = which;
                        item.setSummary(array[mDialogSelected]);
                        mReservationCardInfo.setServiceType(array[mDialogSelected]);
                        mReservationService_type.setSummary(array[mDialogSelected]);
                        // delay dismiss, so the animation can be show
                        mUIHandler.postDelayed(new Runnable() {
                            public void run() {
                                dialog.dismiss();
                            }
                        }, 500);
                        //dialog.dismiss();
                    }})
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "setNeutralButton");
                        mDialogSelected = -1;
                    }})
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        if (mDialogSelected != -1) {
                            Log.d(TAG, "array[mDialogSelected] = " + array[mDialogSelected]);
                            item.setSummary(array[mDialogSelected]);
                            mReservationCardInfo.setServiceType(array[mDialogSelected]);
                            mReservationService_type.setSummary(array[mDialogSelected]);
						}
                    }
                });
        GomeAlertDialog dialog = builder.create();
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.setCancelable(true);
        dialog.show();
    }

    private void showCityPickerDialog() {
        CityPicker cityPicker = new CityPicker.Builder(mContext).textSize(15)
                .province("上海市")
                .city("上海市")
                .build();
        cityPicker.setshowDistricCyclic(false);
        cityPicker.show();
        cityPicker.setOnCityItemClickListener(new CityPicker.OnCityItemClickListener() {
            public void onSelected(String... citySelected) {
                mReservationCardInfo.setProvince(citySelected[0]);
                mReservationCardInfo.setCity(citySelected[1]);
//                mDistractBelong.setSummary(mReservationCardInfo.getCity());
                mReservationCardInfo.setReservationStation("");
                mChooseStation.setDefaultSummary(R.string.default_summary_station);
            }
            public void onCancel() {

            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FAILURE_ISSUE && resultCode == RESULT_OK) {
            if (data.getStringExtra(ISSUE_DESCRIPTION) != null && !"".equals(data.getStringExtra(ISSUE_DESCRIPTION))) {
                mReservationCardInfo.setIssueDescription(data.getStringExtra(ISSUE_DESCRIPTION));
                mFailureIssue.setSummary(R.string.edit_completed);
            } else {
                mReservationCardInfo.setIssueDescription(data.getStringExtra(ISSUE_DESCRIPTION));
                mFailureIssue.setSummary("");
            }

            if( data.getStringExtra(SELECT_DESCRIPTION)!= null ) {
                mReservationCardInfo.setTextSelect(data.getStringExtra(SELECT_DESCRIPTION));
            }
        } else if (requestCode == CHOOSE_STATION && resultCode == RESULT_OK) {
            mReservationCardInfo.setReservationStation(data.getStringExtra(SELECTED_STATION));
            mChooseStation.setSummary(mReservationCardInfo.getReservationStation());
        }
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

    /**
     * 无缓存地址数据，从服务器重新读取
     * */
    private void initAfterSaleAddress(){
        if(!ActivityUtils.isNetworkConnected(this)){
            return;
        }

        Log.d(TAG, "initAfterSaleAddress()");
        DialogUtils.setProgressingDialog(this, null,
                getResources().getString(R.string.progress_dialog_loading));

        String requestJsonString = getRequestJsonForAfterSaleAddress(this);
        String requestUrl = Constants.WF_API_URL + Constants.URL_AREA_LIST;
        NetworkUtils.doPost(this, requestUrl, new HttpCallbackStringListener() {
            @Override
            public void onFinish(String response) {
                Log.d(TAG, "response = " + response);
                DialogUtils.progressDialogDismiss();
                parseAddressResult(response);
            }

            @Override
            public void onError(Exception e) {
                DialogUtils.progressDialogDismiss();
                Log.d(TAG, "e = " + e.toString());
            }
        }, requestJsonString);
    }

    /**
     * 获取地址列表的参数
     * */
    private String getRequestJsonForAfterSaleAddress(Context context){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("sign", Constants.SIGN_AREA_LIST);
            Log.d(TAG, "params = " + jsonObject.toString());
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return jsonObject.toString();
    }

    /**
     * 将地址数据写入本地缓存文件
     * */
    private void parseAddressResult(String result){
        try {
            JSONObject jsonObj = new JSONObject(result);
            String resCode = JsonUtils.getString(jsonObj, "resultcode");
            String resMsg = JsonUtils.getString(jsonObj, "message");
            Log.d(TAG, "resultcode = " + resCode);
            Log.d(TAG, "message = " + resMsg);
            if(Constants.WF_RESPONSE_CODE_SUCCESS.equals(resCode)){
                Log.i(TAG, "FILE_CACHE_AFTER_SALE_ADDRESS, speed up loading");
                ActivityUtils.cacheApplicationData(mContext, Constants.FILE_CACHE_AFTER_SALE_ADDRESS, result);
                showCityPickerDialog();
            }else{
                ActivityUtils.setToastShow(getApplicationContext(), R.string.toast_ensure_your_network);
            }
        } catch (JSONException e) {
            Log.d(TAG, "parseAddressResult Json error");
            ActivityUtils.setToastShow(getApplicationContext(), R.string.toast_ensure_your_network);
            e.printStackTrace();
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
}
