package com.gome.usercenter.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.gome.usercenter.R;
import com.gome.usercenter.activity.SubActivity;
import com.gome.usercenter.helper.DialogHelper;
import com.gome.usercenter.helper.HttpCallbackStringListener;
import com.gome.usercenter.module.ReservationCardInfo;
import com.gome.usercenter.utils.AccountUtils;
import com.gome.usercenter.utils.ActivityUtils;
import com.gome.usercenter.utils.Constants;
import com.gome.usercenter.utils.NetworkUtils;
import com.gome.usercenter.view.DynamicCardAdapter;
import com.gome.usercenter.view.DynamicCardView;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by dongzq on 2017/7/24.
 */

public class ReservationSuccessFragment extends Fragment {

    private static final String TAG = Constants.TAG_HEADER + "ReservationSuccessFragment";
    private Context mContext;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = (ViewGroup) inflater.inflate(R.layout.fragment_reservation_success, null);
        Intent intent = getActivity().getIntent();
        String reponse = intent.getStringExtra("reservation_json");
        ReservationCardInfo info = ReservationCardInfo.resolveJsonData(reponse);
        DynamicCardView cardView = new DynamicCardView(getActivity(), info, DynamicCardAdapter.RESERVATION_CARD, false);
        //fix GMOS-9489 start
        cardView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        //fix GMOS-9489 end
        cardView.setElevation(5f);
        cardView.setDivider(null);
        cardView.setKey(info.getReservationNumber());
        LinearLayout cardContainer = (LinearLayout) view.findViewById(R.id.card_container);
        cardContainer.addView(cardView);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) cardView.getLayoutParams();
        params.setMargins(getResources().getDimensionPixelOffset(R.dimen.gome_padding_size_5dp),
                getResources().getDimensionPixelOffset(R.dimen.gome_padding_size_5dp),
                getResources().getDimensionPixelOffset(R.dimen.gome_padding_size_5dp),
                getResources().getDimensionPixelOffset(R.dimen.gome_padding_size_5dp));
        cardView.setLayoutParams(params);
        cardView.setOnFooterClickListener(new DynamicCardView.DynamicCardFooterClickListener() {
            public void onFooterClick(String key, int type) {
                setCancelAppointementDialog(mContext, key);
            }
        });
        ((SubActivity) getActivity()).setSubActivityTitle(R.string.reserve_title);
        return view;
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    private void cancelAppointement(String number) throws UnsupportedEncodingException, NoSuchAlgorithmException, JSONException {
        if(!ActivityUtils.checkNetworkConnection(mContext, false)){
            return;
        }

        String token = AccountUtils.getGomeAccountTokenValue(getActivity().getApplicationContext());
        if(null == token){
            return;
        }

        String account = AccountUtils.getGomeAccountIdValue(mContext);
        Log.d(TAG, "account3 = " + account);
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
                    Toast.makeText(mContext, getResources().getString(R.string.appointment_cancel_success), Toast.LENGTH_SHORT).show();
                    getActivity().finish();
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
        if(!AccountUtils.getGomeAccountLoginState(mContext)){
            ActivityUtils.setToastShow(mContext, R.string.gome_account_token_invalid);
            Intent intentBroadcast = new Intent(Constants.ACTION_START_ACTIVITY_LOGIN);
            mContext.sendBroadcast(intentBroadcast);
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
}
