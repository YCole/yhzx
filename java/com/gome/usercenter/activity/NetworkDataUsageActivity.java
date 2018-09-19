package com.gome.usercenter.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.gome.usercenter.R;
import com.gome.usercenter.utils.ActivityUtils;

/**
 * Created by jianfeng.xue on 2017/7/13.
 */

public class NetworkDataUsageActivity extends BaseActivity implements View.OnClickListener {

    private Button mBtnToSettings;

    private TextView mMessageTitle;
    private TextView mMessageTip1;
    private TextView mMessageTip2;
    private TextView mMessageTip3;
    private TextView mMessageTip4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setBody(R.layout.activity_wlan_usage);
        mCustomTitle.setText(getResources().getString(R.string.self_data_activity));

        mBtnToSettings = (Button) findViewById(R.id.button_to_settings);
        mBtnToSettings.setOnClickListener(this);

        mMessageTitle = (TextView) findViewById(R.id.message_title);
        mMessageTip1 = (TextView) findViewById(R.id.message_content_1);
        mMessageTip2 = (TextView) findViewById(R.id.message_content_2);
        mMessageTip3 = (TextView) findViewById(R.id.message_content_3);
        mMessageTip4 = (TextView) findViewById(R.id.message_content_4);

        mMessageTitle.setText(getString(R.string.string_mobile_data_service_title));
        mMessageTip1.setText(getString(R.string.string_mobile_data_service_tip1));
        mMessageTip2.setText(getString(R.string.string_mobile_data_service_tip2));
        mMessageTip3.setText(getString(R.string.string_mobile_data_service_tip3));
        mMessageTip4.setText(getString(R.string.string_mobile_data_service_tip4));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_to_settings:
                if(ActivityUtils.isDeviceHasSimCard(this)) {
                    Intent intent = new Intent();
                    //intent.setClassName("com.android.settings", "com.android.settings.Settings$SimSettingsActivity");
                    //intent.setClassName("com.gome.security", "com.gome.security.datausagemnglibrary.activity.DataUsageActivity");
                    intent.setClassName("com.android.phone", "com.android.phone.MobileNetworkSettings");
                    startActivity(intent);
                }else{
                    ActivityUtils.setToastShow(this, R.string.string_no_sim_card_found);
                }
                break;
        }
    }
}

