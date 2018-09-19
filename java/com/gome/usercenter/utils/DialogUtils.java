package com.gome.usercenter.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;

import com.gome.usercenter.R;
import com.gome.usercenter.activity.BaseActivity;
import com.gome.usercenter.activity.ExperienceVersionActivity;
import com.gome.usercenter.activity.HomeActivity;
import com.gome.usercenter.activity.ShowPolicyActivity;
import com.gome.usercenter.helper.DialogHelper;

import gome.app.GomeProgressDialog;

/**
 * Created by jfxue on 2017/7/11.
 */

public class DialogUtils {
    private static final String TAG = Constants.TAG_HEADER + "DialogUtils";
    /*
    public static void setAlertLoginDialog(final Context context, final boolean finish){
        final Activity activity = (Activity)context;
        String title_message = context.getString(R.string.dialog_login_alert_apply_for_experience);
        try {
            if(activity instanceof FeedbackActivity){
                title_message = context.getString(R.string.dialog_login_alert_feedback_your_suggestion);
            }else if(activity instanceof ExperienceVersionActivity){
                title_message = context.getString(R.string.dialog_login_alert_apply_for_experience);
            }else if(activity instanceof ReservationAndMaintenanceActivity){
                title_message = context.getString(R.string.dialog_login_alert_apply_for_reservation);
            }else{
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        DialogHelper.createConfirmDialog(context, title_message,
                context.getString(R.string.proceedTo), context.getString(R.string.dialog_cancel),
                new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        // TODO Auto-generated method stub
                        Intent intentBroadcast = new Intent(Constants.ACTION_START_ACTIVITY_LOGIN);
                        try {
                            if(activity instanceof FeedbackActivity){
                                intentBroadcast.putExtra(Constants.KEY_START_MODE_TARGET_CLASS,
                                        Constants.VALUE_TARGET_FEED_BACK_TO_SERVER);
                                FeedbackActivity.gotoGomeAccountService = true;
                                Log.d(TAG, "set FeedbackActivity gotoGomeAccountService true");
                            }else if(activity instanceof ExperienceVersionActivity){
                                intentBroadcast.putExtra(Constants.KEY_START_MODE_TARGET_CLASS,
                                        Constants.VALUE_TARGET_EXPERIENCE_VERSION);
                                ExperienceVersionActivity.gotoGomeAccountService = true;
                                Log.d(TAG, "set ExperienceVersionActivity gotoGomeAccountService true");
                            }else if(activity instanceof ReservationAndMaintenanceActivity){
                                intentBroadcast.putExtra(Constants.KEY_START_MODE_TARGET_CLASS,
                                        Constants.VALUE_TARGET_RESERVATION);
                                ReservationAndMaintenanceActivity.gotoGomeAccountService = true;
                                Log.d(TAG, "set ReservationAndMaintenanceActivity gotoGomeAccountService true");
                            }else{
                                Log.d(TAG, "set gotoGomeAccountService false");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        context.sendBroadcast(intentBroadcast);
                    }
                }, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        // TODO Auto-generated method stub
                        if(finish){
                            Activity activity = (Activity)context;
                            activity.finish();
                        }
                    }
                }, DialogHelper.NO_ICON);
    }*/
	
    public static Dialog setNetworkNotifyDialog(final Context context){
        return DialogHelper.createConfirmDialog(context, context.getString(R.string.dialog_title_no_network),
                context.getString(R.string.txt_set_network), context.getString(R.string.dialog_cancel),
                new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        // TODO Auto-generated method stub
                        Intent intent = new Intent();
                        intent.setClassName("com.android.settings", "com.android.settings.Settings");
                        if (context instanceof BaseActivity) {
                            ((BaseActivity) context).startActivityForResult(intent, Constants.CHECK_NETWORK_REQUEST_CODE);
                        } else if(context instanceof ExperienceVersionActivity){
                            ((ExperienceVersionActivity) context).startActivityForResult(intent, Constants.CHECK_NETWORK_REQUEST_CODE);
                        }else {
                            context.startActivity(intent);
                        }
                    }
                }, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        // TODO Auto-generated method stub
                        if(context instanceof HomeActivity){
                            //do nothing
                        }else{
                            Activity activity = (Activity) context;
                            activity.finish();
                        }
                    }
                }, DialogHelper.NO_ICON);
    }

    /* modified-begin by zhiqiang.dong@gometech.com.cn for GMOS2X1-511 */
    public static Dialog setNetworkNotifyDialog(final Context context, final boolean finish){
        Dialog dialog = DialogHelper.createConfirmDialog(context, context.getString(R.string.dialog_title_no_network),
                context.getString(R.string.txt_set_network), context.getString(R.string.dialog_cancel),
                new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        // TODO Auto-generated method stub
                        Intent intent = new Intent();
                        intent.setClassName("com.android.settings", "com.android.settings.Settings");
                        if (context instanceof BaseActivity) {
                            ((BaseActivity) context).startActivityForResult(intent, Constants.CHECK_NETWORK_REQUEST_CODE);
                        } else if(context instanceof ExperienceVersionActivity){
                            ((ExperienceVersionActivity) context).startActivityForResult(intent, Constants.CHECK_NETWORK_REQUEST_CODE);
                        }else {
                            context.startActivity(intent);
                        }
                    }
                }, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        // TODO Auto-generated method stub
                    }
                }, DialogHelper.NO_ICON);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(!finish);
        // clicking on back or cancel button may finish activity
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if(finish) {
                    Activity activity = (Activity) context;
                    activity.finish();
                }
            }
        });
        return dialog;
    }
    /* modified-end */

    static GomeProgressDialog preparingPackageProgressDialog;
    public static void setProgressingDialog(final Context context, String title, String message){
        if(preparingPackageProgressDialog != null && preparingPackageProgressDialog.isShowing()) {
            return;
        }
        preparingPackageProgressDialog = new GomeProgressDialog(context);
        preparingPackageProgressDialog.setProgressStyle(GomeProgressDialog.STYLE_SPINNER);
        if(title!=null){
            preparingPackageProgressDialog.setTitle(title);
        }
        preparingPackageProgressDialog.setMessage(message);
        preparingPackageProgressDialog.setIndeterminate(false);
        if (context instanceof ShowPolicyActivity) {
            final ShowPolicyActivity activity = (ShowPolicyActivity) context;
            activityFinishWhenCancelProgressDialog(activity);
        } else if(context instanceof HomeActivity){
            final HomeActivity activity = (HomeActivity) context;
            activityFinishWhenCancelProgressDialog(activity);
        }else {
            preparingPackageProgressDialog.setCancelable(false);
            preparingPackageProgressDialog.setCanceledOnTouchOutside(true);
        }
        if (context instanceof Activity && !((Activity)context).isFinishing()){
            preparingPackageProgressDialog.show();
        }
    }

    public static void activityFinishWhenCancelProgressDialog(final Activity activity){
        preparingPackageProgressDialog.setCancelable(true);
        preparingPackageProgressDialog.setCanceledOnTouchOutside(false);
        preparingPackageProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (!activity.isDestroyed()) {
                    activity.finish();
                }
            }
        });
    }

    public static void progressDialogDismiss(){
        Log.d(TAG, "progressDialogDismiss");
        if(preparingPackageProgressDialog != null && preparingPackageProgressDialog.isShowing()) {
            Log.d(TAG, "dismiss");
            preparingPackageProgressDialog.dismiss();
            preparingPackageProgressDialog = null;
        }
    }


}
