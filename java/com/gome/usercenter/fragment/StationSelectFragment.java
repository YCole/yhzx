package com.gome.usercenter.fragment;


import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.gome.usercenter.R;
import com.gome.usercenter.helper.DialogHelper;
import com.gome.usercenter.helper.HttpCallbackStringListener;
import com.gome.usercenter.utils.ActivityUtils;
import com.gome.usercenter.utils.Constants;
import com.gome.usercenter.utils.DialogUtils;
import com.gome.usercenter.utils.JsonUtils;
import com.gome.usercenter.utils.NetworkStateObserver;
import com.gome.usercenter.utils.NetworkUtils;
import com.gome.usercenter.view.citypicker.CityPicker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Fragment to choose station
 */
public class StationSelectFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener {
    private final String TAG = Constants.TAG_HEADER + "StationSelectFragment";
    private static final int REFRESH_LISTVIEW = 1;
    private static final int REFRESH_SELECTION = 2;
    private static final String SELECTED_STATION = "selected_station";
    private static final int LOCATION_SETTINGS = 100;

    private static final double LOCATION_DEFAULT_VALUE= 255;
    private double mLocationLat = LOCATION_DEFAULT_VALUE;
    private double mLocationLng = LOCATION_DEFAULT_VALUE;

    private String mProvince = "上海市";
    private String mCity = "徐汇区";
    private Context mContext;

    private String mChoose = "";
    private int choose = -1;
    private ListView mListView;
    private FrameLayout mHeaderView;
    private ImageView mMyLocationButton;
    private TextView mMyAddress;
    private RelativeLayout mExpandedCityPicker;

    private SimpleAdapter mMaintenanceCenterAdapter;
    private List<Map<String, Object>> mMaintenanceCenterDataList;

    private int mSelected = -1;

    private TextView mCityTextView;


    private AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
    public AMapLocationClient mLocationClient = null;
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
                case REFRESH_LISTVIEW:
                    mMaintenanceCenterAdapter.notifyDataSetChanged();

                    Message message = mUIhandler.obtainMessage(); 
                    message.what = REFRESH_SELECTION;
                    mUIhandler.sendMessage(message);
                    break;
                case REFRESH_SELECTION:
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

    public StationSelectFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        Intent intent = getActivity().getIntent();
//        mProvince = intent.getStringExtra("province");
//        mCity = intent.getStringExtra("city");
        mChoose = intent.getStringExtra(SELECTED_STATION);
        Log.d(TAG, "mChoose = " + mChoose);
        initLocation();
        if (ActivityUtils.checkNetworkConnection(mContext, true)) {
            if (checkLocationAvailable()) {
                DialogUtils.setProgressingDialog(mContext, null,
                        getResources().getString(R.string.progress_dialog_locating));
                mLocationClient.startLocation();
            }
        }

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

    private void refreshMaintenanceCenterData() {
        HttpCallbackStringListener listener = new HttpCallbackStringListener() {
            public void onFinish(String response) {
                DialogUtils.progressDialogDismiss();
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String resultcode = jsonObject.getString("resultcode");
                    if (!"200".equals(resultcode)) {
                        Toast.makeText(mContext, getResources().getString(R.string.network_data_unaccessable),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    JSONArray results = jsonObject.getJSONArray("results");
                    if (results.length() > 0) {
                        mMaintenanceCenterDataList.clear();
                    }
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject data = (JSONObject) results.opt(i);
                        Map<String, Object> map = new HashMap<>();
                        String name = data.getString("name");
                        map.put("name", name);
                        map.put("address", data.getString("address"));
                        map.put("tel", data.getString("tel"));
                        map.put("maintenanceMargin", data.getString("maintenanceMargin"));
                        map.put("businessHours", data.getString("businessHours"));
                        if(name.equals(mChoose)){
                            choose = i;
                            Log.d(TAG, "choose: " + choose);
                        }
                        mMaintenanceCenterDataList.add(map);
                    }
                    Message msg = mUIhandler.obtainMessage();
                    msg.what = REFRESH_LISTVIEW;
                    mUIhandler.sendMessage(msg);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            public void onError(Exception e) {
                /* modified-begin by zhiqiang.dong@gometech.com.cn, 20171025, GMOS2X1-778 */
                if(((Activity)mContext).isDestroyed()) {
                    return;
                }
                DialogUtils.progressDialogDismiss();
                Toast.makeText(mContext, mContext.getResources().getString(R.string.network_data_unaccessable),
                        Toast.LENGTH_SHORT).show();
                /* modified-end */
            }
        };
        try {
            String request_url = Constants.WF_API_URL + Constants.URL_NETWORK_QUERY;
            String requestJsonString = NetworkUtils.requestBuilder(
                    new String[]{"province", "city"},
                    new String[]{mProvince, mCity}
            );

            DialogUtils.setProgressingDialog(mContext, null,
                    getResources().getString(R.string.progress_dialog_loading));

            NetworkUtils.doPost(mContext, request_url, listener, requestJsonString);
        }catch(JSONException e){
            Log.d(TAG, "build exception " + e);
        }catch(NoSuchAlgorithmException e){
            Log.d(TAG, "build exception " + e);
        }catch(UnsupportedEncodingException e){
            Log.d(TAG, "build exception " + e);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_station_select, container, false);
        mListView = (ListView) view.findViewById(R.id.list);
//      mCityTextView = (TextView) view.findViewById(R.id.city);
//      mCityTextView.setText(mCity);
        init();
        initView(view,inflater);
        return view;
    }

    private void init() {
        String[] from = {"name", "address", "tel"};
        int[] to = {R.id.title, R.id.address, R.id.tel};
        mMaintenanceCenterDataList = new ArrayList<>();
        refreshMaintenanceCenterData();
        mMaintenanceCenterAdapter = new SimpleAdapter(mContext, mMaintenanceCenterDataList,
                R.layout.station_select_list_item, from, to);
        mListView.setDivider(null);
        mListView.setAdapter(mMaintenanceCenterAdapter);
        mListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        mListView.setOnItemClickListener(this);
    }

    private void initView(View view,LayoutInflater layoutInflater) {
        String[] from = {"name", "address"};
        int[] to = {R.id.title, R.id.address};

        // init header
        mHeaderView = (FrameLayout) view.findViewById(R.id.header);
        View header = layoutInflater.inflate(R.layout.after_sale_list_header, mHeaderView, false);
        mMyLocationButton = (ImageView) header.findViewById(R.id.my_location);
        mExpandedCityPicker = (RelativeLayout) header.findViewById(R.id.expanded_adress_picker);
        mMyAddress = (TextView) header.findViewById(R.id.my_address);
        mExpandedCityPicker.setOnClickListener(this);
        mHeaderView.addView(header, 0);
        mMyAddress.setText(mCity);
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

    /**
     * 无缓存地址数据，从服务器重新读取
     * */
    private void initAfterSaleAddress(){
        if(!ActivityUtils.isNetworkConnected(mContext)){
            return;
        }

        Log.d(TAG, "initAfterSaleAddress()");
        String requestJsonString = getRequestJsonForAfterSaleAddress(mContext);
        String requestUrl = Constants.WF_API_URL + Constants.URL_AREA_LIST;
        NetworkUtils.doPost(mContext, requestUrl, new HttpCallbackStringListener() {
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
                ActivityUtils.setToastShow(mContext.getApplicationContext(), R.string.toast_ensure_your_network);
            }
        } catch (JSONException e) {
            Log.d(TAG, "parseAddressResult Json error");
            ActivityUtils.setToastShow(mContext.getApplicationContext(), R.string.toast_ensure_your_network);
            e.printStackTrace();
        }
    }


    private void refreshListView() {
        mMyAddress.setText(mCity);
        if (ActivityUtils.checkNetworkConnection(mContext, true)) {
            refreshMaintenanceCenterData();
        }
    }


    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mSelected == -1) {
            ((RadioButton) view.findViewById(R.id.radio_btn)).setChecked(true);
            mSelected = position;
            mUIhandler.postDelayed(new Runnable() {
                public void run() {
                    Intent intent = new Intent();
                    intent.putExtra(SELECTED_STATION, (String) ((Map<String, Object>) mMaintenanceCenterDataList.get(mSelected)).get("name"));
                    getActivity().setResult(Activity.RESULT_OK, intent);
                    getActivity().finish();
                }
            }, 200);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOCATION_SETTINGS) {
            LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                DialogUtils.setProgressingDialog(mContext, null,
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
        mLocationClient = new AMapLocationClient(mContext.getApplicationContext());
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

    public boolean checkLocationAvailable() {
        LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            DialogHelper.createConfirmDialog(mContext, getResources().getString(R.string.dialog_location_message),
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

    public void onDestroy() {
        super.onDestroy();
        //stop location
        if (mLocationClient.isStarted()) {
            mLocationClient.stopLocation();
        }
        mLocationClient.onDestroy();
        mNetworkStateObserver.unRegisterCallback(mNetworkCallback);
    }

}
