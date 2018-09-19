package com.gome.usercenter.activity;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps2d.model.LatLng;
import com.amap.api.services.core.LatLonPoint;
import com.gome.usercenter.R;
import com.gome.usercenter.utils.ActivityUtils;
import com.gome.usercenter.utils.LruCacheImageLoader;
import com.gome.usercenter.view.ListAlertDialog;

import java.text.DecimalFormat;
import java.util.Map;

import gome.app.GomeAlertDialog;

public class MaintenanceCenterDetailActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "MaintenanceDetail";
    private Map<String, Object> mDataMap;
    private TextView mTitle;
    private TextView mTelephone;
    private TextView mAddress;
    private TextView mMaintenanceMargin;
    private TextView mBusinessHours;
    private TextView mDistance;
    private Button mReserveBtn;
    private ImageView mIcon;

    private String mAddressName;
    private String mCityName;
    private String mProvinceName;

    private LatLng mLocationLatlng;

    private Context mContext;

    private LruCacheImageLoader mCacheImageLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setBody(R.layout.activity_maintenance_center_detail);
        mContext = this;
        mCustomTitle.setText(getResources().getString(R.string.after_sale_service_station));
        mCacheImageLoader = LruCacheImageLoader.newInstance(mContext);
        init();
    }

    private void init() {
        Intent intent = getIntent();
        mDataMap = (Map<String, Object>) intent.getSerializableExtra("maintenance_center_data");
        //mLocationLatlng = (LatLng) intent.getParcelableExtra("latlng");
        if (mDataMap ==  null) {
            Log.e(TAG, "data error");
            finish();
        }
        mAddressName = (String) mDataMap.get("name");
        mCityName = intent.getStringExtra("city");
        mProvinceName = intent.getStringExtra("province");
        mTitle = (TextView) findViewById(R.id.title);
        mTelephone = (TextView) findViewById(R.id.tel);
        mAddress = (TextView) findViewById(R.id.address);
        mMaintenanceMargin = (TextView) findViewById(R.id.maintenance_margin);
        mBusinessHours = (TextView) findViewById(R.id.business_hours);
        mDistance = (TextView) findViewById(R.id.distance);
        mReserveBtn = (Button) findViewById(R.id.reserve_btn);
        mIcon = (ImageView) findViewById(R.id.icon);

        mCacheImageLoader.displayImage(mIcon, (String) (String) mDataMap.get("address") + (String) mDataMap.get("name"),
                (String) mDataMap.get("base64Img"), R.drawable.photo_d);
        mTitle.setText((CharSequence) mDataMap.get("name"));
        mTelephone.setText((CharSequence) mDataMap.get("tel"));
        mAddress.setText((CharSequence) mDataMap.get("address"));
        mMaintenanceMargin.setText((CharSequence) mDataMap.get("maintenanceMargin"));
        mBusinessHours.setText((CharSequence) mDataMap.get("businessHours"));
        if (mDataMap.get("distance") != null) {
            mDistance.setText(formatDistance(Float.parseFloat((String) mDataMap.get("distance"))));
        } else {
            mDistance.setText(R.string.unknow);
        }
        //((RelativeLayout) findViewById(R.id.tel_layout)).setOnClickListener(this);
        ((RelativeLayout) findViewById(R.id.address_layout)).setOnClickListener(this);
        mReserveBtn.setOnClickListener(this);
        ((LinearLayout) findViewById(R.id.title_layout)).setOnClickListener(this);
    }

    private String formatDistance(float distance) {
        StringBuilder builder = new StringBuilder();
        DecimalFormat decimalFormat = new DecimalFormat(".0");
        if (distance < 1000) {
            return builder.append(decimalFormat.format(distance)).append("m").toString();
        } else {
            return builder.append(decimalFormat.format(distance / 1000)).append("km").toString();
        }
    }

    private void openMap() {
        //LatLonPoint point = (LatLonPoint) mDataMap.get("latlon");
        if (mDataMap.get("latitude") == null || mDataMap.get("longitude") == null) {
            Toast.makeText(mContext, getResources().getString(R.string.unknow_address), Toast.LENGTH_SHORT).show();
            return;
        }
        LatLonPoint point = new LatLonPoint(Float.parseFloat((String) mDataMap.get("latitude")),
                Float.parseFloat((String) mDataMap.get("longitude")));
        Uri uri = Uri.parse("geo:" + point.getLatitude() + "," + point.getLongitude());
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Uri uri1 = Uri.parse("http://uri.amap.com/marker?position="
                    + point.getLongitude() + "," + point.getLatitude());
            Intent intentBrowser = new Intent(Intent.ACTION_VIEW, uri1);
            startActivity(intentBrowser);
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tel_layout:
                Intent intent = new Intent(Intent.ACTION_DIAL);
                Uri uri = Uri.parse("tel://" + mTelephone.getText());
                intent.setData(uri);
                startActivity(intent);
                break;
            case R.id.address_layout:
                openMap();
                break;
            case R.id.reserve_btn:
                Intent reserveIntent = new Intent(this, ReservationActivity.class);
                reserveIntent.putExtra("city", mCityName);
                reserveIntent.putExtra("province", mProvinceName);
                reserveIntent.putExtra("address", mAddressName);
                startActivity(reserveIntent);
                break;
            case R.id.title_layout:
                showDialogOrDirectCall();
                break;
            default:break;
        }
    }

    private void showDialogOrDirectCall() {
        String tel = (String) mDataMap.get("tel");
        if (tel == null || tel.isEmpty()) {
            Log.e(TAG, "no contact information");
            return;
        }
        String[] tels = tel.split("[^0-9 -]");
        if (tels.length == 0) {
            Log.e(TAG, "no contact information");
            return;
        }
        if (tels.length == 1) {
            directCall(ActivityUtils.trimSpace(tels[0]));
        } else {
            /* modified-begin by zhiqiang.dong@gometech.com PRODUCTION-2483 */
            /*
            GomeAlertDialog.Builder builder = new GomeAlertDialog.Builder(mContext);
            builder.setItems(tels, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    directCall(tels[which]);
                }
            })
            .setTitle(R.string.choose_phone_number_title);
            builder.create().show();*/
            ListAlertDialog dialog = new ListAlertDialog(this, getResources().getString(R.string.choose_phone_number_title),
                    tels, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    directCall(tels[which]);
                }
            });
            dialog.show();
            /* modified-end */
        }
    }

    private void directCall(String s) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        Uri uri = Uri.parse("tel://" + s);
        intent.setData(uri);
        startActivity(intent);
    }

}
