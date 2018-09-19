package com.gome.usercenter.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMapUtils;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.gome.usercenter.R;
import com.gome.usercenter.helper.DialogHelper;
import com.gome.usercenter.helper.HttpCallbackStringListener;
import com.gome.usercenter.utils.AMapUtil;
import com.gome.usercenter.utils.ActivityUtils;
import com.gome.usercenter.utils.Constants;
import com.gome.usercenter.utils.DialogUtils;
import com.gome.usercenter.utils.JsonUtils;
import com.gome.usercenter.utils.LruCacheImageLoader;
import com.gome.usercenter.utils.NetworkStateObserver;
import com.gome.usercenter.utils.NetworkUtils;
import com.gome.usercenter.view.citypicker.CityPicker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gome.widget.GomeListView;

public class AfterSaleActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private static final String TAG = Constants.TAG_HEADER + "AfterSaleActivity";
    private static final int REFRESH_LISTVIEW = 1;
    private static final int SORT_DATA_BY_DISTANCE = 0;
    private static final int LOCATION_SETTINGS = 100;
    private FrameLayout mHeaderView;
    private GomeListView mListView;
    private TextView mEmptyView;
    private ImageView mMyLocationButton;
    private TextView mMyAddress;
    private RelativeLayout mExpandedCityPicker;

    private MaintenanceCenterAdapter mMaintenanceCenterAdapter;
    private List<Map<String, Object>> mMaintenanceCenterDataList;

    private String mCity = "徐汇区";
    private String mProvince = "上海市";
    private Context mContext;
    // lat:-90 ~ 90, lng:-180 ~ 180, so we set deafult value as 255, that means location failed.
    private static final double LOCATION_DEFAULT_VALUE= 255;

    private AMapLocationClientOption mLocationOption = new AMapLocationClientOption();

    public AMapLocationClient mLocationClient = null;

    private double mLocationLat = LOCATION_DEFAULT_VALUE;
    private double mLocationLng = LOCATION_DEFAULT_VALUE;
 
    private LruCacheImageLoader mCacheImageLoader;
    private NetworkStateObserver mNetworkStateObserver;
    private NetworkStateObserver.NetworkStateCallback mNetworkCallback = new NetworkStateObserver.NetworkStateCallback() {
        @Override
        public void onNetworkConnected() {
            ActivityUtils.dismissNetworkDialog();
            refreshMaintenanceCenterData();
        }

        @Override
        public void onNetworkDisconnected() {

        }
    };

    private Handler mUIhandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SORT_DATA_BY_DISTANCE:
                    CalculateDistanceTask calculateDistanceTask = new CalculateDistanceTask(mContext, mMaintenanceCenterDataList);
                    calculateDistanceTask.execute(mCity);
                case REFRESH_LISTVIEW:
                    mMaintenanceCenterAdapter.notifyDataSetChanged();
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    };

    public AMapLocationListener mLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation amapLocation) {
            if (amapLocation != null) {
                if (amapLocation.getErrorCode() == 0) {
                    //location succeed
                    mLocationClient.stopLocation();
                    mLocationLat = amapLocation.getLatitude();//get location latitude
                    mLocationLng = amapLocation.getLongitude();//get location longitude
                    mProvince = amapLocation.getProvince();
                    mCity = amapLocation.getCity();

                    /* modified-begin by zhiqiang.dong@gometech.com.cn 2017/11/09 PRODUCTION-5433 */
                    // For autonomous regions and municipalities like BeiJing, Shanghai, etc.
                    if (mCity.equals(mProvince)) {
                        mCity = amapLocation.getDistrict();
                    }
                    /* modified-end */
                } else {
                    onHandleLocationError(amapLocation);
                    Log.e(TAG, "location Error, ErrCode:"
                            + amapLocation.getErrorCode() + ", errInfo:"
                            + amapLocation.getErrorInfo());
                }
            }
            DialogUtils.progressDialogDismiss();
            refreshListView();
        }
    };

    private void onHandleLocationError(AMapLocation amapLocation) {
        switch (amapLocation.getErrorCode()) {
            case 4:
                ActivityUtils.setToastShow(mContext, R.string.location_failed_message4);
                break;
            case 13:
                ActivityUtils.setToastShow(mContext, R.string.location_failed_message13);
                break;
            case 18:
                ActivityUtils.setToastShow(mContext, R.string.location_failed_message18);
                break;
            case 19:
                ActivityUtils.setToastShow(mContext, R.string.location_failed_message19);
                break;
            default:
                ActivityUtils.setToastShow(mContext, R.string.location_failed_default_message);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setBody(R.layout.activity_after_sale);
        mCustomTitle.setText(getResources().getString(R.string.after_sale_service_station));
        mContext = this;
        mCacheImageLoader = LruCacheImageLoader.newInstance(mContext);

        initView();
        initLocation();
        //start location
        if (ActivityUtils.checkNetworkConnection(mContext, true)) {
            if (checkLocationAvailable()) {
                DialogUtils.setProgressingDialog(this, null,
                        getResources().getString(R.string.progress_dialog_locating));
                mLocationClient.startLocation();
            }
        }
        //check if local address file exsist
        if(ActivityUtils.isCacheDataExsist(mContext, Constants.FILE_CACHE_AFTER_SALE_ADDRESS)){
            Log.i(TAG, "local cache address data file exist, read data from cache");
            String result = ActivityUtils.readFromFileOneLine(mContext, Constants.FILE_CACHE_AFTER_SALE_ADDRESS);
        }else{
            Log.i(TAG, "local cache address data file does not exist, need get from server");
            initAfterSaleAddress();
        }
        mNetworkStateObserver = NetworkStateObserver.newInstance(mContext);
        mNetworkStateObserver.registerCallback(mNetworkCallback);
    }

    public boolean checkLocationAvailable() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            DialogHelper.createConfirmDialog(this, getResources().getString(R.string.dialog_location_message),
                    getResources().getString(R.string.dialog_allow), getResources().getString(R.string.dialog_forbidden),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(intent, LOCATION_SETTINGS);
                        }
                    }, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            refreshMaintenanceCenterData();
                        }
                    }, DialogHelper.NO_ICON);
            return false;
        } else {
            return true;
        }
    }

    protected void onResume() {
        super.onResume();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOCATION_SETTINGS) {
            LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                DialogUtils.setProgressingDialog(this, null,
                    getResources().getString(R.string.progress_dialog_locating));
                mLocationClient.startLocation();
            } else if (ActivityUtils.isNetworkConnected(mContext)){
                refreshMaintenanceCenterData();
            }
        } else if (requestCode == Constants.CHECK_NETWORK_REQUEST_CODE && ActivityUtils.checkNetworkConnection(mContext, true)) {
            refreshMaintenanceCenterData();
        }
    }

    private void initLocation() {
        //init location
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //set accuracy as hight，Battery_Saving is low-power mode，Device_Sensors is only device mode
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //set location interval, unit as ms
        //mLocationOption.setInterval(10000);
        //该方法默认为false。
        mLocationOption.setOnceLocation(true);

        //单位是毫秒，默认30000毫秒，如果网络不佳定时时间会加长，建议超时时间不要低于8000毫秒。
        mLocationOption.setHttpTimeOut(10000);

        //获取最近3s内精度最高的一次定位结果：
        //设置setOnceLocationLatest(boolean b)接口为true，启动定位时SDK会返回最近3s内精度最高的一次定位结果。如果设置其为true，setOnceLocation(boolean b)接口也会被设置为true，反之不会，默认为false。
        mLocationOption.setOnceLocationLatest(false);

        mLocationClient.setLocationOption(mLocationOption);
        //set location listener
        mLocationClient.setLocationListener(mLocationListener);
    }

    protected void onPause() {
        super.onPause();
    }

    protected void onDestroy() {
        super.onDestroy();
        //stop location
        if (mLocationClient.isStarted()) {
            mLocationClient.stopLocation();
        }
        mLocationClient.onDestroy();
        mCacheImageLoader.releaseLruCacheImageLoader();
        mNetworkStateObserver.unRegisterCallback(mNetworkCallback);
    }

    private void initView() {
        String[] from = {"name", "address"};
        int[] to = {R.id.title, R.id.address};

        // init header
        mHeaderView = (FrameLayout) findViewById(R.id.header);
        View header = this.getLayoutInflater().inflate(R.layout.after_sale_list_header, mHeaderView, false);
        mMyLocationButton = (ImageView) header.findViewById(R.id.my_location);
        mExpandedCityPicker = (RelativeLayout) header.findViewById(R.id.expanded_adress_picker);
        mMyAddress = (TextView) header.findViewById(R.id.my_address);
        mExpandedCityPicker.setOnClickListener(this);
        mHeaderView.addView(header, 0);

        mMyAddress.setText(mCity);
        // init list
        mListView = (GomeListView) findViewById(R.id.list);
        mListView.setDivider(null);
        mMaintenanceCenterDataList = new ArrayList<>();
        //refreshMaintenanceCenterData();
        //mMaintenanceCenterAdapter = new SimpleAdapter(mContext, mMaintenanceCenterDataList,
        //        R.layout.maintenance_center_item, from, to);
        mMaintenanceCenterAdapter = new MaintenanceCenterAdapter();
        mListView.setAdapter(mMaintenanceCenterAdapter);
        mListView.setOnItemClickListener(this);
        mEmptyView = (TextView) findViewById(R.id.empty_view);

    }

    private void refreshMaintenanceCenterData() {
        HttpCallbackStringListener listener = new HttpCallbackStringListener() {
            public void onFinish(String response) {
                DialogUtils.progressDialogDismiss();
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String resultcode = jsonObject.getString("resultcode");
                    if (!"200".equals(resultcode)) {
                        //ActivityUtils.setToastShow(mContext, R.string.network_data_unaccessable);
                        return;
                    }
                    JSONArray results = jsonObject.getJSONArray("results");
                    if (results.length() <= 0) {
                        mMaintenanceCenterDataList.clear();

                        Message msg = mUIhandler.obtainMessage();
                        msg.what = REFRESH_LISTVIEW;
                        mUIhandler.sendMessage(msg);
                        //ActivityUtils.setToastShow(getApplicationContext(), R.string.toast_no_maintenance_center_data);
                        return;
                    }
                    mMaintenanceCenterDataList.clear();
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject data = (JSONObject) results.opt(i);
                        Map<String, Object> map = new HashMap<>();
                        map.put("name", data.getString("name"));
                        map.put("address", data.getString("address"));
                        map.put("tel", data.getString("tel"));
                        map.put("maintenanceMargin", data.getString("maintenanceMargin"));
                        map.put("businessHours", data.getString("businessHours"));
                        map.put("base64Img", data.getString("networkImg"));
                        mMaintenanceCenterDataList.add(map);
                    }

                    Message msg = mUIhandler.obtainMessage();
                    msg.what = SORT_DATA_BY_DISTANCE;
                    mUIhandler.sendMessage(msg);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            public void onError(Exception e) {
                DialogUtils.progressDialogDismiss();
                //ActivityUtils.setToastShow(mContext, R.string.network_data_unaccessable);
            }
        };
        try {
            String request_url = Constants.WF_API_URL + Constants.URL_NETWORK_QUERY;
            String requestJsonString = NetworkUtils.requestBuilder(
                    new String[]{"province", "city"},
                    new String[]{mProvince, mCity}
            );

            DialogUtils.setProgressingDialog(this, null,
                    getResources().getString(R.string.progress_dialog_loading));

            NetworkUtils.doPost(this, request_url, listener, requestJsonString);
        }catch(JSONException e){
            Log.d(TAG, "build exception " + e);
        }catch(NoSuchAlgorithmException e){
            Log.d(TAG, "build exception " + e);
        }catch(UnsupportedEncodingException e){
            Log.d(TAG, "build exception " + e);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.expanded_adress_picker:
                showPickerDialog();
                break;
            default:break;
        }
    }

    private void showPickerDialog() {
        if(!ActivityUtils.isCacheDataExsist(mContext, Constants.FILE_CACHE_AFTER_SALE_ADDRESS)){
            initAfterSaleAddress();
            return;
        }
        CityPicker cityPicker = new CityPicker.Builder(mContext).textSize(15)
                .province(mProvince)
                .city(mCity)
                .build();
        cityPicker.setshowDistricCyclic(false);
        cityPicker.show();
        cityPicker.setOnCityItemClickListener(new CityPicker.OnCityItemClickListener() {
            public void onSelected(String... citySelected) {
                mProvince = citySelected[0];
                mCity = citySelected[1];
                refreshListView();
            }

            public void onCancel() {

            }
        });

    }

    private void refreshListView() {
        mMyAddress.setText(mCity);
        if (ActivityUtils.checkNetworkConnection(mContext, true)) {
            refreshMaintenanceCenterData();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent();
        intent.setClass(mContext, MaintenanceCenterDetailActivity.class);
        intent.putExtra("maintenance_center_data", (Serializable) mMaintenanceCenterDataList.get(position));
        intent.putExtra("city", mCity);
        intent.putExtra("province", mProvince);
        //intent.putExtra("latlng", new LatLng(mLocationLat, mLocationLng));
        startActivity(intent);
    }

    public class CalculateDistanceTask extends AsyncTask<String, Integer, Void> {

        private Context mContext;
        private List<Map<String, Object>> mList;
        private int mGeoCount = 0;
        public CalculateDistanceTask(Context context, List<Map<String, Object>> list) {
            super();
            mContext = context;
            mList = list;
        }

        protected Void doInBackground(final String... params) {
            //解析目的地址
            final GeocodeSearch geocoderSearch;
            geocoderSearch = new GeocodeSearch(mContext);
            GeocodeSearch.OnGeocodeSearchListener listener = new GeocodeSearch.OnGeocodeSearchListener() {
                GeocodeQuery query = new GeocodeQuery((String) ((Map<String, Object>) mList.get(mGeoCount)).get("address"), params[0]);
                public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {

                }

                public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {
                    GeocodeAddress address = (GeocodeAddress) geocodeResult.getGeocodeAddressList().get(0);
                    if (mCity.equals(address.getCity()) || mProvince.equals(address.getCity())) {// assert location parser is good or not
                        LatLonPoint latLonPoint = address.getLatLonPoint();
                        if (mLocationLng != LOCATION_DEFAULT_VALUE && mLocationLat != LOCATION_DEFAULT_VALUE) {
                            // location failed, so do not calculate distance
                            float locationDistance = AMapUtils.calculateLineDistance(new LatLng(mLocationLat, mLocationLng),
                                    AMapUtil.convertToLatLng(address.getLatLonPoint()));
                            ((Map<String, Object>) mList.get(mGeoCount)).put("distance", "" + locationDistance);
                        }
                        ((Map<String, Object>) mList.get(mGeoCount)).put("latitude", "" + latLonPoint.getLatitude());
                        ((Map<String, Object>) mList.get(mGeoCount)).put("longitude", "" + latLonPoint.getLongitude());
                    }
                    mGeoCount++;
                    if (mGeoCount < mList.size()) {
                        query.setLocationName((String) ((Map<String, Object>) mList.get(mGeoCount)).get("address"));
                        geocoderSearch.getFromLocationNameAsyn(query);
                    } else {
                        Collections.sort(mList, new Comparator() {
                            public int compare(Object o1, Object o2) {
                                Map<String, Object> map1 = (HashMap<String, Object>) o1;
                                Map<String, Object> map2 = (HashMap<String, Object>) o2;
                                String distance1 = (String) map1.get("distance");
                                String distance2 = (String) map2.get("distance");
                                if (distance1 == null || distance2 == null) {
                                    return 0;
                                }
                                if (Float.parseFloat(distance1)
                                        > Float.parseFloat(distance2)) {
                                    return 1;
                                }
                                if (Float.parseFloat(distance1)
                                        < Float.parseFloat(distance2)) {
                                    return -1;
                                }
                                return 0;
                            }
                        });
                        Message msg = mUIhandler.obtainMessage();
                        msg.what = REFRESH_LISTVIEW;
                        mUIhandler.sendMessage(msg);
                    }
                }
            };
            geocoderSearch.setOnGeocodeSearchListener(listener);
            GeocodeQuery query = new GeocodeQuery((String) ((Map<String, Object>) mList.get(mGeoCount)).get("address"), params[0]);
            geocoderSearch.getFromLocationNameAsyn(query);
            return null;
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
        String requestJsonString = getRequestJsonForAfterSaleAddress(this);
        String requestUrl = Constants.WF_API_URL + Constants.URL_AREA_LIST;
        NetworkUtils.doPost(this, requestUrl, new HttpCallbackStringListener() {
            @Override
            public void onFinish(String response) {
                Log.d(TAG, "response = " + response);
                parseAddressResult(response);
            }

            @Override
            public void onError(Exception e) {
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
            }else{
                ActivityUtils.setToastShow(getApplicationContext(), R.string.toast_ensure_your_network);
            }
        } catch (JSONException e) {
            Log.d(TAG, "parseAddressResult Json error");
            ActivityUtils.setToastShow(getApplicationContext(), R.string.toast_ensure_your_network);
            e.printStackTrace();
        }
    }

    public class MaintenanceCenterAdapter extends BaseAdapter {

        public int getCount() {
            return mMaintenanceCenterDataList.size();
        }

        public Object getItem(int position) {
            return mMaintenanceCenterDataList.get(position);
        }

        public long getItemId(int position) {
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.maintenance_center_item, parent, false);
                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.title);
                holder.address = (TextView) convertView.findViewById(R.id.address);
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            HashMap<String, Object> map = (HashMap<String, Object>) getItem(position);
            holder.name.setText((CharSequence) map.get("name"));
            holder.address.setText((CharSequence) map.get("address"));
            String base64 = (String) map.get("base64Img");
            mCacheImageLoader.displayImage(holder.icon, (String) map.get("address") + (String) map.get("name"),
                    base64, R.drawable.photo_n);
            return convertView;
        }
        class ViewHolder {
            TextView name;
            TextView address;
            ImageView icon;
        }
    }
}
