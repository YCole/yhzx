package com.gome.usercenter.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.gome.usercenter.R;

/**
 * Created by jianfeng.xue on 2017/7/12.
 */

public class NetworkSelfServiceActivity extends BaseActivity implements View.OnClickListener {

    private RelativeLayout mLayoutData;
    private RelativeLayout mLayoutWifi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setBody(R.layout.activity_network_self_service);
        mCustomTitle.setText(getResources().getString(R.string.self_network));

        mLayoutData = (RelativeLayout) findViewById(R.id.layout_data_next);
        mLayoutWifi = (RelativeLayout) findViewById(R.id.layout_wifi_next);
        mLayoutData.setOnClickListener(this);
        mLayoutWifi.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_data_next:
                toActivity(NetworkDataUsageActivity.class);
                break;
            case R.id.layout_wifi_next:
                toActivity(NetworkWlanUsageActivity.class);
                break;
        }
    }

    private void toActivity(Class className){
        Intent intent = new Intent();
        intent.setClass(this, className);
        startActivity(intent);
    }

}
