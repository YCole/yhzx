package com.gome.usercenter.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gome.usercenter.R;
import com.gome.usercenter.fragment.ReservationSearchFragment;
import com.gome.usercenter.helper.DialogHelper;
import com.gome.usercenter.helper.HttpCallbackStringListener;
import com.gome.usercenter.module.CardInfo;
import com.gome.usercenter.module.CardListInfo;
import com.gome.usercenter.utils.NetworkStateObserver;
import com.gome.usercenter.utils.AccountUtils;
import com.gome.usercenter.utils.ActivityUtils;
import com.gome.usercenter.utils.Constants;
import com.gome.usercenter.utils.DialogUtils;
import com.gome.usercenter.utils.NetworkUtils;
import com.gome.usercenter.view.DynamicCardAdapter;
import com.gome.usercenter.view.DynamicCardView;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import gome.app.GomeAlertDialog;

public class ReservationAndMaintenanceActivity extends BaseActivity implements View.OnClickListener, DynamicCardView.DynamicCardFooterClickListener {

    public static final int DATA_REFRESHED = 1;
    public static final int START_REQUEST_DATA_FROM_SERVER = 2;
    public static final int RECEIVED_DATA_FROM_SERVER = 3;
    private static final int GET_DATA_FROM_SERVER_FAILED = 4;
    public static final int REFRESH_DATA_AFTER_SEARCH = 5;
    private final String TAG = Constants.TAG_HEADER + "ReservationAndMaintenanceActivity";

    private LinearLayout mReservationListview;
    private TextView mApplyReservation;
    private TextView reservation_status_string;
    private ReservationSearchFragment mSearchFragment;

    private Context mContext;

    private CardListInfo mCardListInfo;

    private int margin_bottom ;
    private int margin_top ;
    private int margin_left ;
    private int margin_right ;

    public static boolean gotoGomeAccountService = false;

    private String mRepairResponse = null;
    private String mReserveResponse = null;
    private String mRepairRequest;
    private String mReserveRequest;
    private String mRa;
    private String mRb;

    private boolean mSearchResult = false;

    private boolean mTimeoutFlag = false;
    private NetworkStateObserver mNetworkStateObserver;
    private NetworkStateObserver.NetworkStateCallback mNetworkCallback = new NetworkStateObserver.NetworkStateCallback() {
        @Override
        public void onNetworkConnected() {
            ActivityUtils.dismissNetworkDialog();
            initDataWithGomeAccount();
        }

        @Override
        public void onNetworkDisconnected() {

        }
    };

    private Handler mUIHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DATA_REFRESHED:
                    Log.d(TAG, "yuyue4");
                    mRepairResponse = null;
                    mReserveResponse = null;
                    DialogUtils.progressDialogDismiss();
                    refreshView();
                    break;
                case START_REQUEST_DATA_FROM_SERVER:
                    mRepairResponse = null;
                    mReserveResponse = null;
                    mRepairRequest = msg.getData().getString("repair_request");
                    mReserveRequest = msg.getData().getString("reserve_request");
                    if (mRepairRequest != null && mReserveRequest != null) {
                        mSearchResult = true;
                        tryGetDataFromServer();
                        reservation_status_string.setText(getResources().getString(R.string.reservation_maintenance_no_information));
                    }
                    break;
                case RECEIVED_DATA_FROM_SERVER:
                    if (msg.getData().getString("repair_response") != null) {
                        mRepairResponse = msg.getData().getString("repair_response");
                    }
                    if (msg.getData().getString("reserve_response") != null) {
                        mReserveResponse = msg.getData().getString("reserve_response");
                    }
                    Log.d(TAG, "repair response :[" + mRepairResponse + "], reservation reponse: [" + mReserveResponse + "]");
                    if (mRepairResponse != null && mReserveResponse != null) {
                            mRa = mRepairResponse;
                            mRb = mReserveResponse;
                            mRepairResponse = null;
                            mReserveResponse = null;
                            UpdateDataList(mRa, mRb);
                    }
                    break;
                case GET_DATA_FROM_SERVER_FAILED:
                    mRepairResponse = null;
                    mReserveResponse = null;
                    DialogUtils.progressDialogDismiss();
                    break;
                case REFRESH_DATA_AFTER_SEARCH:
                    initDataWithGomeAccount();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };

    private void UpdateDataList(String mRa, String mRb) {
        new Thread(new Runnable() {
            public void run() {
                mCardListInfo.updateCardList(mRa, mRb);
                Message msg = mUIHandler.obtainMessage();
                msg.what = DATA_REFRESHED;
                mUIHandler.sendMessage(msg);
            }
        }).start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setBody(R.layout.activity_reservation_maintenance);
        mCustomTitle.setText(R.string.appointment_maintenance_title);
        mContext = this;

        margin_bottom = (int) getResources().getDimensionPixelOffset(R.dimen.gome_padding_size_5dp);
        margin_top = (int) getResources().getDimensionPixelOffset(R.dimen.gome_padding_size_5dp);
        margin_left = (int) getResources().getDimensionPixelOffset(R.dimen.gome_padding_size_5dp);
        margin_right = (int) getResources().getDimensionPixelOffset(R.dimen.gome_padding_size_5dp);

        mReservationListview = (LinearLayout) findViewById(R.id.reservation_listview);
        mApplyReservation = (TextView) findViewById(R.id.apply_reservation);
        mApplyReservation.setOnClickListener(this);
        mReservationListview = (LinearLayout) findViewById(R.id.reservation_listview);

        reservation_status_string = (TextView) findViewById(R.id.reservation_status_string);

        mSearchFragment = (ReservationSearchFragment) getFragmentManager().findFragmentById(R.id.search_fragment);

        mCardListInfo = new CardListInfo();
        mNetworkStateObserver = NetworkStateObserver.newInstance(mContext);
        mNetworkStateObserver.registerCallback(mNetworkCallback);
        initDataWithGomeAccount();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mNetworkStateObserver.unRegisterCallback(mNetworkCallback);
    }

    public Handler getUIHandler() {
        return mUIHandler;
    }
    private void initDataWithGomeAccount() {
        if(!ActivityUtils.checkNetworkConnection(mContext, false)){

            return;
        }
        if(!AccountUtils.getGomeAccountLoginState(mContext)){
            Log.d(TAG, "yuyue3");
            reservation_status_string.setVisibility(View.VISIBLE);
            reservation_status_string.setText(getResources().getString(R.string.reservation_maintenance_gome_account_notification));
            mReservationListview.removeAllViews();
            return;
        }
        String account = AccountUtils.getGomeAccountIdValue(mContext);
        Log.d(TAG, "account1 = " + account);
        if (account == null) {
            Log.d(TAG, "access account failed, please check your gome account");
            reservation_status_string.setVisibility(View.VISIBLE);
            reservation_status_string.setText(getResources().getString(R.string.reservation_maintenance_gome_account_notification));
            mReservationListview.removeAllViews();
            return;
        }
        try {
            String repairRequest = NetworkUtils.requestBuilder(new String[]{"gomeAccount", "tel", "imei"},
                    new String[]{account, "", ""});
            String reserveRequest = NetworkUtils.requestBuilder(new String[]{"gomeAccount", "tel", "imei"},
                    new String[]{account, "", ""});
            mRepairRequest = repairRequest;
            mReserveRequest = reserveRequest;
            if (mRepairRequest != null && mReserveRequest != null) {
                mSearchResult =  false;
                tryGetDataFromServer();
                reservation_status_string.setText(getResources().getString(R.string.reservation_maintenance_no_maintenance));
            }
        } catch (JSONException e) {
            Log.d(TAG, "Build request faild caused by Exception: " + e);
        } catch (NoSuchAlgorithmException e) {
            Log.d(TAG, "Build request faild caused by Exception: " + e);
        } catch (UnsupportedEncodingException e) {
            Log.d(TAG, "Build request faild caused by Exception: " + e);
        }
    }


    public void tryGetDataFromServer() {
        if(!ActivityUtils.checkNetworkConnection(this, false)){
            return;
        }
        HttpCallbackStringListener repairListener = new HttpCallbackStringListener() {
            public void onFinish(String response) {
                Message msg = mUIHandler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putString("repair_response", NetworkUtils.requestSuccess(response) ?  response : "");
                msg.setData(bundle);
                msg.what = RECEIVED_DATA_FROM_SERVER;
                mUIHandler.sendMessage(msg);
            }

            public void onError(Exception e) {
                if (e instanceof SocketTimeoutException
                        || e instanceof java.net.UnknownHostException) {
                    /* modified-begin by zhiqiang.dong@gometech.com for PRODUCTION-2423 */
                    if (mTimeoutFlag) {
                        mTimeoutFlag = false;
                        Toast.makeText(mContext, getResources().getString(R.string.network_timeout), Toast.LENGTH_SHORT).show();
                    } else {
                        mTimeoutFlag = true;
                    }
                    /*modified-end*/
                }
                Log.d(TAG, "get data from server failed, url = [" + Constants.URL_REPAIR_QUERY + "], request = [" + mRepairRequest + "], error: " + e);
                Message msg = mUIHandler.obtainMessage();
                msg.what = GET_DATA_FROM_SERVER_FAILED;
                mUIHandler.sendMessage(msg);
            }
        };
        HttpCallbackStringListener reservationListener = new HttpCallbackStringListener() {
            public void onFinish(String response) {
                Log.d(TAG, "reservation response: [" + response + "]");
                Message msg = mUIHandler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putString("reserve_response", NetworkUtils.requestSuccess(response) ? response : "");
                msg.setData(bundle);
                msg.what = RECEIVED_DATA_FROM_SERVER;
                mUIHandler.sendMessage(msg);
            }

            public void onError(Exception e) {
                if (e instanceof SocketTimeoutException
                        || e instanceof java.net.UnknownHostException) {
                    /* modified-begin by zhiqiang.dong@gometech.com for PRODUCTION-2423 */
                    if (mTimeoutFlag) {
                        mTimeoutFlag = false;
                        Toast.makeText(mContext, getResources().getString(R.string.network_timeout), Toast.LENGTH_SHORT).show();
                    } else {
                        mTimeoutFlag = true;
                    }
                    /* modified-end */
                }
                Log.d(TAG, "get data from server failed, url = [" + Constants.URL_RESERVE_QUERY + "], request = [" + mReserveRequest + "], error: " + e);
                Message msg = mUIHandler.obtainMessage();
                msg.what = GET_DATA_FROM_SERVER_FAILED;
                mUIHandler.sendMessage(msg);
            }
        };
        DialogUtils.setProgressingDialog(this, null, getResources().getString(R.string.refreshing_data));
        NetworkUtils.doPost(mContext, Constants.WF_API_URL + Constants.URL_REPAIR_QUERY, repairListener, mRepairRequest);
        NetworkUtils.doPost(mContext, Constants.WF_API_URL + Constants.URL_RESERVE_QUERY, reservationListener, mReserveRequest);
    }

    public void refreshView() {
        DialogUtils.progressDialogDismiss();
        List<CardInfo> cardInfos = new ArrayList<CardInfo>();
        cardInfos.addAll(mCardListInfo.getCardList());
        mReservationListview.removeAllViews();

        if (cardInfos.isEmpty()) {
            reservation_status_string.setVisibility(View.VISIBLE);
			// GMOS-3214 remove this toast
            //Toast.makeText(mContext, getResources().getString(R.string.no_data_query), Toast.LENGTH_SHORT).show();
        }else{
            reservation_status_string.setVisibility(View.INVISIBLE);
            Log.d(TAG, "yuyue2" );
            for (CardInfo cardInfo :  cardInfos) {
                if(cardInfo != null && cardInfo.getCardInfoType() != DynamicCardAdapter.HISTORY_CARD){
                    DynamicCardView cardView = new DynamicCardView(mContext, cardInfo, mSearchResult, cardInfo.getCardInfoType());
                    cardView.setElevation(5f);
                    cardView.setSelector(new ColorDrawable(Color.TRANSPARENT));
                    cardView.setKey(cardInfo.getReservationNumber());
                    cardView.setOnFooterClickListener(this);
                    cardView.setDivider(null);
                    mReservationListview.addView(cardView);
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) cardView.getLayoutParams();
                    params.setMargins(margin_left, margin_top, margin_right, margin_bottom);
                    cardView.setLayoutParams(params);
                    cardView.setFooterViewVisible(!mSearchResult);
                }
            }

            for (CardInfo cardInfo :  cardInfos) {
                if(cardInfo != null && cardInfo.getCardInfoType() == DynamicCardAdapter.HISTORY_CARD){
                    DynamicCardView cardView = new DynamicCardView(mContext, cardInfo, mSearchResult, cardInfo.getCardInfoType());
                    cardView.setElevation(5f);
                    cardView.setSelector(new ColorDrawable(Color.TRANSPARENT));
                    cardView.setKey(cardInfo.getReservationNumber());
                    cardView.setOnFooterClickListener(this);
                    cardView.setDivider(null);
                    mReservationListview.addView(cardView);
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) cardView.getLayoutParams();
                    params.setMargins(margin_left, margin_top, margin_right, margin_bottom);
                    cardView.setLayoutParams(params);
                }
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.CHECK_NETWORK_REQUEST_CODE) {
            if (ActivityUtils.checkNetworkConnection(mContext, false)) {
                initDataWithGomeAccount();
            }
        } else if (requestCode == Constants.RESERVATION_REQUEST_CODE) {
            mSearchFragment.clearSearchText();
            //initDataWithGomeAccount();
        }
    }

    protected void onResume(){
        super.onResume();
        if(gotoGomeAccountService){
            initDataWithGomeAccount();
            gotoGomeAccountService = false;
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.apply_reservation:
                gotoReservationActivity();
                break;
            default:break;
        }
    }

    void gotoReservationActivity(){
        Log.d(TAG, "go to ReservationActivity");
        if(!ActivityUtils.checkNetworkConnection(ReservationAndMaintenanceActivity.this, false)){
            return;
        }
        if(AccountUtils.getGomeAccountLoginState(this)){
            Intent intent = new Intent();
            intent.setClass(this, ReservationActivity.class);
            // startActivity(intent);
            startActivityForResult(intent, Constants.RESERVATION_REQUEST_CODE);
        }else{
            showLoginDialog();
        }
    }

    public void onFooterClick(String key, int type) {
        setCancelAppointementDialog(mContext, key);
    }

    private void cancelAppointement(String number) throws UnsupportedEncodingException, NoSuchAlgorithmException, JSONException {
        if(!ActivityUtils.checkNetworkConnection(this, false)){
            return;
        }

        String token = AccountUtils.getGomeAccountTokenValue(getApplicationContext());
        if(null == token){
            return;
        }
        String account = AccountUtils.getGomeAccountIdValue(mContext);
        if (account == null) {
            Log.d(TAG, "access account failed, please check your gome account");
            return;
        }

        String imei = ActivityUtils.getDeviceID(mContext);
        String request = NetworkUtils.requestBuilder(new String[]{"gomeAccount", "imei", "reservationNumber"},
                new String[]{account, imei, number});
        HttpCallbackStringListener listener = new HttpCallbackStringListener() {
            public void onFinish(String response) {
                if (NetworkUtils.requestSuccess(response)) {
                    initDataWithGomeAccount();
                    Toast.makeText(mContext, getResources().getString(R.string.appointment_cancel_success), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mContext, getResources().getString(R.string.appointment_cancel_failed), Toast.LENGTH_SHORT).show();
                }
            }

            public void onError(Exception e) {
                Log.d(TAG, "cancel failed, Exception : " + e);
                Toast.makeText(mContext, getResources().getString(R.string.appointment_cancel_failed), Toast.LENGTH_SHORT).show();
            }
        };
        
        NetworkUtils.doPost(mContext, Constants.WF_API_URL + Constants.URL_RESERVE_CANCEL, listener, request);
    }

    private void setCancelAppointementDialog(final Context context, final String key){
        if(!AccountUtils.getGomeAccountLoginState(this)){
            ActivityUtils.setToastShow(mContext, R.string.gome_account_token_invalid);
            Intent intentBroadcast = new Intent(Constants.ACTION_START_ACTIVITY_LOGIN);
            sendBroadcast(intentBroadcast);
            return;
        }
        DialogHelper.createConfirmDialog(context, context.getString(R.string.dialog_message_cancel_reservation),
                context.getString(R.string.dialog_yes), context.getString(R.string.dialog_no),
                new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        // TODO Auto-generated method stub
                        try {
                            cancelAppointement(key);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        // TODO Auto-generated method stub

                    }
                }, DialogHelper.NO_ICON, true);
    }

    private GomeAlertDialog loginDialog;
    private void showLoginDialog(){
        if(loginDialog != null && loginDialog.isShowing()) {
            return;
        }
        final GomeAlertDialog.Builder builder = new GomeAlertDialog.Builder(this)
                .setTitle(R.string.dialog_login_alert_apply_for_reservation)
                .setPositiveButton(R.string.proceedTo, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intentBroadcast = new Intent(Constants.ACTION_START_ACTIVITY_LOGIN);
                        intentBroadcast.putExtra(Constants.KEY_START_MODE_TARGET_CLASS,
                                Constants.VALUE_TARGET_RESERVATION);
                        gotoGomeAccountService = true;
                        sendBroadcast(intentBroadcast);
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
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
