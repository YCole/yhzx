package com.gome.usercenter.helper;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;

import com.gome.usercenter.utils.Constants;

import gome.app.GomeAlertDialog;

/**
 * Created by jfxue on 2017/7/11.
 */


public class DialogHelper {
    private static final String TAG = Constants.TAG_HEADER + "DialogHelper";

    public static final int NO_ICON = -1;

    public static void createMessageDialog(Context context, String title, /*String message,*/
                                             String btnName, OnClickListener listener, int iconId)
    {
        GomeAlertDialog.Builder alertDialog = new GomeAlertDialog.Builder(context);

        if (iconId != NO_ICON)
        {
            alertDialog.setIcon(iconId);
        }
        alertDialog.setTitle(title);
        //alertDialog.setMessage(message);
        alertDialog.setPositiveButton(btnName, listener);
        //GomeAlertDialog dialog = alertDialog.create();
        //alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setCancelable(false);
        //alertDialog.show();
        Dialog dialog = alertDialog.create();
        dialog.show();
    }

    public static Dialog createConfirmDialog(final Context context, String title, /*String message,*/
                                             String positiveBtnName, String negativeBtnName, OnClickListener positiveBtnListener,
                                             OnClickListener negativeBtnListener, int iconId) {
        GomeAlertDialog.Builder alertDialog = new GomeAlertDialog.Builder(
                context);

        if(iconId != NO_ICON){
            alertDialog.setIcon(iconId);
        }
        if(title != null){
            alertDialog.setTitle(title);
        }
        //alertDialog.setMessage(message);
        //alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setPositiveButton(positiveBtnName, positiveBtnListener);
        alertDialog.setNegativeButton(negativeBtnName, negativeBtnListener);
		alertDialog.setCancelable(false);
        //alertDialog.show();
        Dialog dialog = alertDialog.create();
        dialog.show();
        return dialog;
    }

    public static Dialog createConfirmDialog(final Context context, String title, /*String message,*/
                                             String positiveBtnName, String negativeBtnName, OnClickListener positiveBtnListener,
                                             OnClickListener negativeBtnListener, int iconId, boolean cancelable) {
        GomeAlertDialog.Builder alertDialog = new GomeAlertDialog.Builder(
                context);

        if(iconId != NO_ICON){
            alertDialog.setIcon(iconId);
        }
        if(title != null){
            alertDialog.setTitle(title);
        }
        //alertDialog.setMessage(message);
        //alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setPositiveButton(positiveBtnName, positiveBtnListener);
        alertDialog.setNegativeButton(negativeBtnName, negativeBtnListener);
        alertDialog.setCancelable(cancelable);
        //alertDialog.show();
        GomeAlertDialog dialog = alertDialog.create();
        dialog.setCanceledOnTouchOutside(cancelable);
        dialog.setCancelable(cancelable);
        dialog.show();
        return dialog;
    }
}
