package com.gome.usercenter.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.gome.usercenter.R;
import com.gome.usercenter.utils.ActivityUtils;
import com.gome.usercenter.utils.Constants;
import com.gome.usercenter.utils.DialogUtils;

/**
 * Created by dongzq on 2017/7/7.
 */

public abstract class BaseActivity extends Activity{

    private final String TAG = Constants.TAG_HEADER + "BaseActivity";

    ActionBar mActionBar;
    FrameLayout mLayBody;
    ImageButton mImageBack;
    TextView mCustomTitle;
    TextView mCustomCentralTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //setTheme(com.gome.R.style.Theme_GOME_Light);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        /*< PRODUCTION-2406 shengzhong 20171012 begin */
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        /* PRODUCTION-2406 shengzhong 20171012 end > */
        mLayBody = (FrameLayout) findViewById(R.id.base_lay_boby);

        initCustomActionBar();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // modified by zhiqiang.dong@gometech.com
        // Just dismiss the NetworkDialog popped by current activity
        ActivityUtils.dismissNetworkDialog(this);
        ActivityUtils.setToastOff();
        // modified by zhiqiang.dong@gometech.com for PRODUCTION-2407
        DialogUtils.progressDialogDismiss();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    final protected View setBody(int id){
        mLayBody.removeAllViews();
        View view = getLayoutInflater().inflate(id,null,false);
        mLayBody.addView(view);
        return view;
    }

    protected void setCentralTitleVisible(){
        if(mCustomCentralTitle != null) {
            mCustomCentralTitle.setVisibility(View.VISIBLE);
        }
        if(mImageBack != null) {
            mImageBack.setVisibility(View.INVISIBLE);
        }
    }

    private boolean initCustomActionBar() {
        mActionBar = getActionBar();
        if (mActionBar == null) {
            return false;
        } else {
            mActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            mActionBar.setDisplayShowCustomEnabled(true);
            mActionBar.setCustomView(R.layout.toolbar_layout);
            mCustomCentralTitle =  (TextView) mActionBar.getCustomView().findViewById(R.id.base_actionbar_central_title);
            mCustomTitle = (TextView) mActionBar.getCustomView().findViewById(R.id.base_actionbar_title);
            mImageBack = (ImageButton) mActionBar.getCustomView().findViewById(R.id.base_actionBar_back);
            mImageBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    finish();
                }
            });
            return true;
        }
    }

    public void setCustomTitle(String title) {
        if(mCustomTitle != null){
            mCustomTitle.setText(title);
        }
    }

    public void setCustomTitle(int titleRes) {
        if(mCustomTitle != null){
            mCustomTitle.setText(titleRes);
        }
    }

    public void setCustomCentralTitle(String title) {
        if(mCustomCentralTitle != null) {
            mCustomCentralTitle.setVisibility(View.VISIBLE);
            mCustomCentralTitle.setText(title);
        }
    }

    public void setCustomCentralTitle(int titleRes) {
        if(mCustomCentralTitle != null) {
            mCustomCentralTitle.setVisibility(View.VISIBLE);
            mCustomCentralTitle.setText(titleRes);
        }
    }

    public void setCustomBackIcon() {
        if(mImageBack != null) {
            mImageBack.setBackground(getDrawable(R.drawable.ic_gome_icon_cancel));
        }
    }
}
