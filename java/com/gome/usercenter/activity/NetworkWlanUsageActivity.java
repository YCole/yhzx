package com.gome.usercenter.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.gome.usercenter.R;

import org.w3c.dom.Text;

/**
 * Created by jianfeng.xue on 2017/7/13.
 */

public class NetworkWlanUsageActivity extends BaseActivity implements View.OnClickListener {

    private Button mBtnToSettings;

    private TextView mMessageTitle;
    private ImageView mImageWlan;
    private TextView mMessageTip1;
    private TextView mMessageTip2;
    private TextView mMessageTip3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setBody(R.layout.activity_wlan_usage);
        mCustomTitle.setText(getResources().getString(R.string.self_wlan_activity));

        mBtnToSettings = (Button) findViewById(R.id.button_to_settings);
        mBtnToSettings.setOnClickListener(this);
        mImageWlan  = (ImageView) findViewById(R.id.image_wlan);
        mImageWlan.setVisibility(View.VISIBLE);

        mMessageTitle = (TextView) findViewById(R.id.message_title);
        mMessageTip1 = (TextView) findViewById(R.id.message_content_1);
        mMessageTip2 = (TextView) findViewById(R.id.message_content_2);
        mMessageTip3 = (TextView) findViewById(R.id.message_content_3);

        mMessageTitle.setText(getString(R.string.string_wlan_service_title));
        mMessageTip1.setText(getString(R.string.string_wlan_service_tip1));
        mMessageTip2.setText(getString(R.string.string_wlan_service_tip2));
        mMessageTip3.setText(getString(R.string.string_wlan_service_tip3));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_to_settings:
                Intent intent = new Intent();
                intent.setClassName("com.android.settings", "com.android.settings.Settings$WifiSettingsActivity");
                startActivity(intent);
                break;
        }
    }
}
