package com.gome.usercenter.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.gome.usercenter.R;
import com.gome.usercenter.helper.HttpCallbackStringListener;
import com.gome.usercenter.module.HistoryFeedbackInfo;
import com.gome.usercenter.utils.AccountUtils;
import com.gome.usercenter.utils.ActivityUtils;
import com.gome.usercenter.utils.Constants;
import com.gome.usercenter.utils.DialogUtils;
import com.gome.usercenter.utils.JsonUtils;
import com.gome.usercenter.utils.NetworkStateObserver;
import com.gome.usercenter.utils.NetworkUtils;
import com.gome.usercenter.view.DynamicListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gome.app.GomeAlertDialog;

public class FeedbackActivity extends BaseActivity implements DynamicListView.DynamicListViewListener, View.OnClickListener {

    private final String TAG = Constants.TAG_HEADER + "FeedbackActivity";

    private DynamicListView mAllFeedbacksListView;
    private DynamicListView mMyFeedbacksListView;
    private List<Map<String, Object>> mAllFeedbacksData;
    private List<Map<String, Object>> mMyFeedbacksData;
    private List<HistoryFeedbackInfo> myHistoryDataFromServer;
    private int mAllFeedbacksCount;
    private int mMyFeedbacksCount;
    private View mFeedbackClick;
    private Context mContext;
    private FeedbackAdapter mMyFeedbackAdapter;
    private SimpleAdapter mAllFeedbackAdapter;

    private View all_feedbacks_footer;

    private boolean mOnlyShowAllFeedback = false;

    private int ALL_FEEDBACK_DEFALUT_LOAD_COUNT = 4;
    private int LOAD_COUNT_INCREASEMENT = Integer.MAX_VALUE >> 2;

    public final int REQUEST_FEEDBACK_TO_SERVER = 1;
    public final int REQUEST_FEEDBACK_HISTORY = 2;
    enum STATE{
        LOGIN, LOGOUT
    }

    public Handler mHandler = new Handler();

    public static boolean gotoGomeAccountService = false;

    private static final String WEBVIEW_MODE = "webview_mode";
    private static final String COMMON_PAGE = "common_page";
    private NetworkStateObserver mNetworkStateObserver;
    private NetworkStateObserver.NetworkStateCallback mNetworkCallback = new NetworkStateObserver.NetworkStateCallback() {
        @Override
        public void onNetworkConnected() {
            ActivityUtils.dismissNetworkDialog();
            refreshView(true);
        }

        @Override
        public void onNetworkDisconnected() {

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setBody(R.layout.activity_feedback);
        mCustomTitle.setText(getResources().getString(R.string.feedback));

        mContext = this;

        initAllView();
        initHistoryView();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                String account = AccountUtils.getLatestGomeAccountName(mContext);
                if(ActivityUtils.isCacheDataExsist(mContext, Constants.FILE_CACHE_FEEDBACK_HISTORY)){
                    if(!ActivityUtils.checkNetworkConnection(FeedbackActivity.this, false)){
                        return;
                    }
                    Log.i(TAG, "local cache data file exist, read data from cache");
                    String result = ActivityUtils.readFromFileOneLine(mContext, Constants.FILE_CACHE_FEEDBACK_HISTORY);
                    String savedAccount = result.substring(0, result.indexOf(';'));
                    // check account changed or not
                    if (savedAccount != null && savedAccount.equals(account)) {
                        reloadHistoryFeedbackData(result.substring(result.indexOf(';') + 1));
                        //initHistoryView();
                        showActivityViewByState(updateLoginState());
                    } else {
                        Log.i(TAG, "local cache data file is not for this user, need get from server again");
                        getUserFeedbackHistoryFromServer();
                    }
                }else{
                    Log.i(TAG, "local cache data file does not exist, need get from server again");
                    getUserFeedbackHistoryFromServer();
                }
            }
        });
        mNetworkStateObserver = NetworkStateObserver.newInstance(mContext);
        mNetworkStateObserver.registerCallback(mNetworkCallback);
    }

    protected void onResume(){
        super.onResume();
        if(gotoGomeAccountService){
            refreshView(true);
            gotoGomeAccountService = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mNetworkStateObserver.unRegisterCallback(mNetworkCallback);
    }

    private void initAllView() {
        mAllFeedbacksListView = (DynamicListView) findViewById(R.id.all_feedbacks);
        mMyFeedbacksListView = (DynamicListView) findViewById(R.id.my_feedbacks);
        all_feedbacks_footer = (View) findViewById(R.id.header_border_line);
        mAllFeedbacksListView.setDivider(null);
        mMyFeedbacksListView.setDivider(null);
        mMyFeedbacksListView.showFooterView(false);
        String[] from = {"feedTitle"};
        int[] to = {R.id.title};

        initData(updateLoginState());

        mAllFeedbackAdapter = new SimpleAdapter(this, mAllFeedbacksData, R.layout.feedback_item, from, to);
        mAllFeedbacksListView.setAdapter(mAllFeedbackAdapter);
        mAllFeedbacksListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position==0)
                    return;
//                if(mAllFeedbacksData.size()==(position-1)){
//                    mAllFeedbacksListView.setRefreshStatus(DynamicListView.LoadStatus.loading);
//                    loadingMore();
//                    return;
//                }
                if (!ActivityUtils.checkNetworkConnection(mContext, false)) {
                    return;
                }
                /*
                String feedTitle = (String) mAllFeedbacksData.get(position-1).get("feedTitle");
                String feedContent = (String) mAllFeedbacksData.get(position-1).get("feedContent");

                //show activity view
                Intent intent=new Intent();
                intent.setClass(FeedbackActivity.this, HistoryFeedbackActivity.class);
                intent.putExtra("feedTitle", feedTitle);
                intent.putExtra("feedContent", feedContent);
                startActivity(intent);*/

                //show web view
                Intent intent = new Intent();
                intent.setClassName("com.gome.usercenter", "com.gome.usercenter.activity.ShowPolicyActivity");
                intent.putExtra(WEBVIEW_MODE, "show_common_problem");
                intent.putExtra(COMMON_PAGE,  String.valueOf(position));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        mAllFeedbacksListView.setOnMoreListener(this);
        mAllFeedbacksListView.setHeadTitle(R.string.all_feedbacks);

        mFeedbackClick = findViewById(R.id.feedback);
        mFeedbackClick.setOnClickListener(this);

        mAllFeedbacksListView.showFooterView(false);
        all_feedbacks_footer.setVisibility(View.GONE);
        mMyFeedbacksListView.setVisibility(View.GONE);
        mMyFeedbacksListView.showFooterView(false);
    }

    private void initHistoryView() {
        mMyFeedbackAdapter = new FeedbackAdapter(this, R.layout.feedback_item, mMyFeedbacksData);
        mMyFeedbacksListView.setAdapter(mMyFeedbackAdapter);
        mMyFeedbacksListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position==0)
                    return;
                String messageId = (String) mMyFeedbacksData.get(position-1).get("id");
                String feedModule = (String) mMyFeedbacksData.get(position-1).get("feedModule");
                String feedTitle = (String) mMyFeedbacksData.get(position-1).get("feedTitle");
                String feedContent = (String) mMyFeedbacksData.get(position-1).get("feedContent");
                String feedImgs = (String) mMyFeedbacksData.get(position-1).get("feedImgs");
                String createTime = (String) mMyFeedbacksData.get(position-1).get("createTime");
                String feedHandleStatus = (String) mMyFeedbacksData.get(position-1).get("feedHandleStatus");
                String feedHandleContent = (String) mMyFeedbacksData.get(position-1).get("feedHandleContent");
                String updateTime = (String) mMyFeedbacksData.get(position-1).get("updateTime");

                Intent intent=new Intent();
                intent.setClass(FeedbackActivity.this, HistoryFeedbackActivity.class);
                intent.putExtra("id", messageId);
                intent.putExtra("feedModule", feedModule);
                intent.putExtra("feedTitle", feedTitle);
                intent.putExtra("feedContent", feedContent);
                intent.putExtra("feedImgs", feedImgs);
                intent.putExtra("createTime",createTime);
                intent.putExtra("feedHandleStatus",feedHandleStatus);
                intent.putExtra("feedHandleContent",feedHandleContent);
                intent.putExtra("updateTime",updateTime);
                startActivityForResult(intent, REQUEST_FEEDBACK_HISTORY);
            }
        });
        mMyFeedbacksListView.setOnMoreListener(this);
        mMyFeedbacksListView.setHeadTitle(R.string.my_feedback);

        showActivityViewByState(updateLoginState());
    }

    private STATE updateLoginState(){
        String token = AccountUtils.getGomeAccountTokenValue(getApplicationContext());
        if(null == token){
            return STATE.LOGOUT;
        }else{
            return STATE.LOGIN;
        }
    }

    private void showActivityViewByState(STATE state){
        if(state == STATE.LOGIN){
            //login state, show normal & history
            if(mMyFeedbacksData.size()==0){
                mAllFeedbacksListView.showFooterView(false);
                all_feedbacks_footer.setVisibility(View.GONE);
                mMyFeedbacksListView.setVisibility(View.GONE);
            }else{
//                mAllFeedbacksListView.showFooterView(true);
//                all_feedbacks_footer.setVisibility(View.VISIBLE);
                mAllFeedbacksListView.showFooterView(false);
                all_feedbacks_footer.setVisibility(View.GONE);
                mMyFeedbacksListView.setVisibility(View.VISIBLE);
            }
        }else{
            mAllFeedbacksListView.showFooterView(false);
            all_feedbacks_footer.setVisibility(View.GONE);
            mMyFeedbacksListView.setVisibility(View.GONE);
        }
    }

    private void initData(STATE state) {
        String[] allFeedbacksDataArray = getResources().getStringArray(R.array.all_feedbacks);
        String[] allFeedbacksDataDetailArray = getResources().getStringArray(R.array.all_feedbacks_detail);
        mAllFeedbacksData = new ArrayList<>();
        mMyFeedbacksData = new ArrayList<>();
        myHistoryDataFromServer = new ArrayList<HistoryFeedbackInfo>();
       /* if(state == STATE.LOGIN){
            ALL_FEEDBACK_DEFALUT_LOAD_COUNT = 4;
        }else{
            ALL_FEEDBACK_DEFALUT_LOAD_COUNT = allFeedbacksDataArray.length;
        }*/

        ALL_FEEDBACK_DEFALUT_LOAD_COUNT = allFeedbacksDataArray.length;

        int i;
        // when init first time, we'd batter load all feedback
        for (i = 0; i < allFeedbacksDataArray.length; i++) {
            Map<String, Object> map = new HashMap();
            map.put("feedTitle", allFeedbacksDataArray[i]);
            map.put("feedContent", allFeedbacksDataDetailArray[i]);
            mAllFeedbacksData.add(map);
        }
        mAllFeedbacksCount = i;
    }

    private void loadAllFeedbacksData(STATE state, int myFeedbacksSize) {
        String[] allFeedbacksDataArray = getResources().getStringArray(R.array.all_feedbacks);
        String[] allFeedbacksDataDetailArray = getResources().getStringArray(R.array.all_feedbacks_detail);
        /*if(state == STATE.LOGIN && myFeedbacksSize > 0){
            ALL_FEEDBACK_DEFALUT_LOAD_COUNT = 4;
        }else{
            ALL_FEEDBACK_DEFALUT_LOAD_COUNT = allFeedbacksDataArray.length;
        }*/

        ALL_FEEDBACK_DEFALUT_LOAD_COUNT = allFeedbacksDataArray.length;

        int i;
        mAllFeedbacksData.clear();
        for (i = 0; i < allFeedbacksDataArray.length && i < ALL_FEEDBACK_DEFALUT_LOAD_COUNT; i++) {
            Map<String, Object> map = new HashMap();
            map.put("feedTitle", allFeedbacksDataArray[i]);
            map.put("feedContent", allFeedbacksDataDetailArray[i]);
            mAllFeedbacksData.add(map);
        }
        mAllFeedbacksCount = i;
        mAllFeedbackAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onLoadingMore(DynamicListView dynamicListView) {
        switch (dynamicListView.getId()) {
            case R.id.all_feedbacks:
                loadingMore();
                break;
            default:break;
        }
        return true;
    }

    private void loadingMore(){
        /* modified-begin by zhiqiang.dong@gometech.com.cn 2017/11/08 GMOS2X1-1820 */
        AsyncTask loadingAllFeedbacksTask = new AsyncTask<Object, Object, Integer>() {
            List<Map<String, Object>> tempAllFeedbacks = new ArrayList<>();
            @Override
            protected Integer doInBackground(Object[] params) {
                String[] dataArray = getResources().getStringArray(R.array.all_feedbacks);
                String[] dataDetailArray = getResources().getStringArray(R.array.all_feedbacks_detail);
                //List<Map<String, Object>> tempAllFeedbacks = new ArrayList<>();
                /* modified-end */
                int i;
                for (i = mAllFeedbacksCount; i < mAllFeedbacksCount + LOAD_COUNT_INCREASEMENT
                        && i <  dataArray.length; i++) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("feedTitle", dataArray[i]);
                    map.put("feedContent", dataDetailArray[i]);
                    tempAllFeedbacks.add(map);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                /* modified-begin by zhiqiang.dong@gometech.com.cn 2017/11/08 GMOS2X1-1820 */
                //synchronized (mAllFeedbacksData) {
                //    mAllFeedbacksData.addAll(tempAllFeedbacks);
                //    mAllFeedbacksCount = i;
                //}
                /* modified-end */
                return i;
            }

            @Override
            protected void onPostExecute(Integer i) {
                /* modified-begin by zhiqiang.dong@gometech.com.cn 2017/11/08 GMOS2X1-1820 */
                mAllFeedbacksData.addAll(tempAllFeedbacks);
                mAllFeedbacksCount = i;
                mAllFeedbackAdapter.notifyDataSetChanged();
                /* modified-end */
                mAllFeedbacksListView.doneMore();
                mAllFeedbacksListView.showFooterView(false);
                all_feedbacks_footer.setVisibility(View.VISIBLE);
                //GMOS-2219 start
                /*
                mMyFeedbacksListView.setVisibility(View.GONE);
                mOnlyShowAllFeedback = true;
                */
                //GMOS-2219 end
            }
        };
        loadingAllFeedbacksTask.execute();
    }

    public void onBackPressed() {
        if (mOnlyShowAllFeedback) {
            mOnlyShowAllFeedback = false;
            refreshView(false);
            mAllFeedbacksListView.showFooterView(true);
            all_feedbacks_footer.setVisibility(View.VISIBLE);
            mMyFeedbacksListView.setVisibility(View.VISIBLE);
        } else{
            super.onBackPressed();
        }
    }

    /**
     * @param isFromServer
     *    true: get history data from server;
     *    false: only refresh local data
     * */
    private void refreshView(boolean isFromServer) {
        int i;
        String[] allFeedbacksDataArray = getResources().getStringArray(R.array.all_feedbacks);
        String[] allFeedbacksDataDetailArray = getResources().getStringArray(R.array.all_feedbacks_detail);
        // not load All feedbasks here, load after loading my feedbacks
        /*
        if(updateLoginState() == STATE.LOGIN){
            ALL_FEEDBACK_DEFALUT_LOAD_COUNT = 4;
        }else{
            ALL_FEEDBACK_DEFALUT_LOAD_COUNT = allFeedbacksDataArray.length;
        }

        mAllFeedbacksData.clear();
        for (i = 0; i < allFeedbacksDataArray.length && i < ALL_FEEDBACK_DEFALUT_LOAD_COUNT; i++) {
            Map<String, Object> map = new HashMap();
            map.put("feedTitle", allFeedbacksDataArray[i]);
            map.put("feedContent", allFeedbacksDataDetailArray[i]);
            mAllFeedbacksData.add(map);
        }
        mAllFeedbacksCount = i;
        mAllFeedbackAdapter.notifyDataSetChanged();*/
        String token = AccountUtils.getGomeAccountTokenValue(getApplicationContext());
        if(isFromServer){
            Log.i(TAG, "load history data from server");
            getUserFeedbackHistoryFromServer();
        }else if (null != token){
            String account = AccountUtils.getLatestGomeAccountName(mContext);
            if(ActivityUtils.isCacheDataExsist(mContext, Constants.FILE_CACHE_FEEDBACK_HISTORY)){
                Log.i(TAG, "local cache data file exist, read data from cache");
                String result = ActivityUtils.readFromFileOneLine(mContext, Constants.FILE_CACHE_FEEDBACK_HISTORY);
                String savedAccount = result.substring(0, result.indexOf(';'));
                // check account changed or not
                if (savedAccount != null && savedAccount.equals(account)) {
                    reloadHistoryFeedbackData(result.substring(result.indexOf(';') + 1));
                    mMyFeedbackAdapter.notifyDataSetChanged();
                } else {
                    Log.i(TAG, "local cache data file is not for this user, need get from server again");
                    getUserFeedbackHistoryFromServer();
                }
            }else{
                Log.i(TAG, "local cache data file does not exist, need get from server again");
                getUserFeedbackHistoryFromServer();
            }
        }
    }

    private void updateAllFeedbackView(){
        int i;
        String[] allFeedbacksDataArray = getResources().getStringArray(R.array.all_feedbacks);
        String[] allFeedbacksDataDetailArray = getResources().getStringArray(R.array.all_feedbacks_detail);

        ALL_FEEDBACK_DEFALUT_LOAD_COUNT = allFeedbacksDataArray.length;

        mAllFeedbacksData.clear();
        for (i = 0; i < allFeedbacksDataArray.length && i < ALL_FEEDBACK_DEFALUT_LOAD_COUNT; i++) {
            Map<String, Object> map = new HashMap();
            map.put("feedTitle", allFeedbacksDataArray[i]);
            map.put("feedContent", allFeedbacksDataDetailArray[i]);
            mAllFeedbacksData.add(map);
        }
        mAllFeedbacksCount = i;
        mAllFeedbackAdapter.notifyDataSetChanged();
        //fix ui show
        all_feedbacks_footer.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.feedback) {
            gotoFeedbackToServerActivity();
        }
    }

    private void gotoFeedbackToServerActivity(){
        Log.d(TAG, "goto feed back to server activity");
        if(AccountUtils.getGomeAccountLoginState(this)){
            Intent intent = new Intent();
            intent.setClass(mContext, FeedbackToServerActivity.class);
            startActivityForResult(intent, REQUEST_FEEDBACK_TO_SERVER);
        }else{
            showLoginDialog();
        }
    }

    private class FeedbackAdapter extends ArrayAdapter{
        private int mResId;
        private List<Map<String, Object>> mData;
        private LayoutInflater mInflater;
        public FeedbackAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List objects) {
            super(context, resource, objects);
            mResId = resource;
            mData = objects;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View view;
            if (mInflater == null) {
                mInflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }
            if (convertView == null) {
                view = mInflater.inflate(R.layout.feedback_item, parent, false);
            } else {
                view = convertView;
            }
            TextView title = (TextView) view.findViewById(R.id.title);
            ImageView unreadPoint = (ImageView) view.findViewById(R.id.unread_point);
            title.setText((String) mData.get(position).get("feedTitle"));
            unreadPoint.setVisibility(((String) mData.get(position).get("feedHandleStatus")).equals("0") ? View.GONE : View.VISIBLE);
            return view;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "requestCode = " + requestCode);
        switch (requestCode) {
            case REQUEST_FEEDBACK_TO_SERVER:
                Log.d(TAG, "resultCode = " + resultCode);
                refreshView(true);
                break;
            case Constants.CHECK_NETWORK_REQUEST_CODE:
                refreshView(true);
                break;
            case REQUEST_FEEDBACK_HISTORY:
                refreshView(true);
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void reloadHistoryFeedbackData(String result){
        int i=0;
        myHistoryDataFromServer.clear();
        mMyFeedbacksData.clear();
        myHistoryDataFromServer = getHisrotyFeedbackInfoList("list", result);
        Collections.reverse(myHistoryDataFromServer);
        Collections.sort(myHistoryDataFromServer, new Comparator() {
            public int compare(Object o1, Object o2) {
                HistoryFeedbackInfo hf1 = (HistoryFeedbackInfo) o1;
                HistoryFeedbackInfo hf2 = (HistoryFeedbackInfo) o2;

                if (Float.parseFloat((String) hf1.getUpdateTime())
                        > Float.parseFloat((String) hf2.getUpdateTime())) {
                    return -1;
                }
                if (Float.parseFloat((String) hf1.getUpdateTime())
                        < Float.parseFloat((String) hf2.getUpdateTime())) {
                    return 1;
                }
                return 0;
            }
        });

        for (i = 0; i < myHistoryDataFromServer.size(); i++) {
            if(myHistoryDataFromServer.get(i).getFeedHandleStatus().equals("1")){
                Map<String, Object> map = new HashMap();
                map.put("id", myHistoryDataFromServer.get(i).getMessageId());
                map.put("feedModule", myHistoryDataFromServer.get(i).getFeedModule());
                map.put("feedTitle", myHistoryDataFromServer.get(i).getFeedTitle());
                map.put("feedContent", myHistoryDataFromServer.get(i).getFeedContent());
                map.put("feedImgs", myHistoryDataFromServer.get(i).getFeedImgs());
                map.put("createTime", myHistoryDataFromServer.get(i).getFeedTime());
                map.put("feedHandleStatus", myHistoryDataFromServer.get(i).getFeedHandleStatus());
                map.put("feedHandleContent", myHistoryDataFromServer.get(i).getFeedHandleContent());
                map.put("updateTime", myHistoryDataFromServer.get(i).getUpdateTime());
                mMyFeedbacksData.add(map);
            }
        }

        for (i = 0; i < myHistoryDataFromServer.size(); i++) {
            if(myHistoryDataFromServer.get(i).getFeedHandleStatus().equals("0")){
                Map<String, Object> map = new HashMap();
                map.put("id", myHistoryDataFromServer.get(i).getMessageId());
                map.put("feedModule", myHistoryDataFromServer.get(i).getFeedModule());
                map.put("feedTitle", myHistoryDataFromServer.get(i).getFeedTitle());
                map.put("feedContent", myHistoryDataFromServer.get(i).getFeedContent());
                map.put("feedImgs", myHistoryDataFromServer.get(i).getFeedImgs());
                map.put("createTime", myHistoryDataFromServer.get(i).getFeedTime());
                map.put("feedHandleStatus", myHistoryDataFromServer.get(i).getFeedHandleStatus());
                map.put("feedHandleContent", myHistoryDataFromServer.get(i).getFeedHandleContent());
                map.put("updateTime", myHistoryDataFromServer.get(i).getUpdateTime());
                mMyFeedbacksData.add(map);
            }
        }
        mMyFeedbacksCount = i;
        mMyFeedbackAdapter.notifyDataSetChanged();

        loadAllFeedbacksData(updateLoginState(), mMyFeedbacksData.size());
    }

    public void getUserFeedbackHistoryFromServer(){
        if(!ActivityUtils.checkNetworkConnection(this, false)){
            return;
        }

        String token = AccountUtils.getGomeAccountTokenValue(getApplicationContext());
        if(null == token){
            return;
        }

        DialogUtils.setProgressingDialog(this, null, getResources().getString(R.string.progress_dialog_loading));

        Log.d(TAG, "getUserFeedbackHistoryFromServer()");
        String requestJsonString = getRequestJsonForFeedbackHistory(this);
        String requestUrl = Constants.GOME_BASE_URL + "system/feedlist";
        NetworkUtils.doPost(this, requestUrl, new HttpCallbackStringListener() {
            @Override
            public void onFinish(String response) {
                DialogUtils.progressDialogDismiss();
                String result = ActivityUtils.getDescryptJsonString(response);
                parseHistoryResult(result);
            }

            @Override
            public void onError(Exception e) {
                Log.d(TAG, "e = " + e.toString());
                ActivityUtils.setToastShow(getApplicationContext(), R.string.alert_network_unavaiable);
                updateAllFeedbackView();
                DialogUtils.progressDialogDismiss();
            }
        }, requestJsonString);
    }

    public String getRequestJsonForFeedbackHistory(Context context){
        String token = AccountUtils.getGomeAccountTokenValue(context);
        String imei = ActivityUtils.getDeviceID(context);

        JSONObject jsonObject = new JSONObject();
        try{
            jsonObject.put("imei", imei);
            jsonObject.put("token", token);
            jsonObject.put("pageNum", "1");
            jsonObject.put("pageSize", 100);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return ActivityUtils.getEncryptJsonString(jsonObject.toString());
    }

    private void parseHistoryResult(String result){
        try {
            JSONObject jsonObj = new JSONObject(result);
            String resCode = JsonUtils.getString(jsonObj, "resCode");
            String resMsg = JsonUtils.getString(jsonObj, "resMsg");
            Log.d(TAG, "resCode = " + resCode);
            Log.d(TAG, "resMsg = " + resMsg);
            if(Constants.RESPONSE_CODE_SUCCESS.equals(resCode)){
                Log.i(TAG, "cache data in local file, speed up loading");
                String account = AccountUtils.getLatestGomeAccountName(mContext);
                // save account information with cache
                ActivityUtils.cacheApplicationData(mContext, Constants.FILE_CACHE_FEEDBACK_HISTORY, account + ";" + result);
                reloadHistoryFeedbackData(result);
                // Just update show state, and there is no need to init history view again
                //initHistoryView();
                showActivityViewByState(updateLoginState());
            }else if(Constants.RESPONSE_CODE_TOKEN_INVAILD.equals(resCode)
                    || Constants.RESPONSE_CODE_NOT_LOGIN.equals(resCode)
                    || Constants.RESPONSE_CODE_PLEASE_LOGIN.equals(resCode)){
                gotoGomeAccountService = true;
                ActivityUtils.setToastShow(mContext, R.string.gome_account_token_invalid);
                Intent intentBroadcast = new Intent(Constants.ACTION_START_ACTIVITY_LOGIN);
                sendBroadcast(intentBroadcast);
            }
        } catch (JSONException e) {
            Log.d(TAG, "Parse Json error");
            e.printStackTrace();
        }
    }

    public List<HistoryFeedbackInfo> getHisrotyFeedbackInfoList(String key, String jsonString) {
        List<HistoryFeedbackInfo> list = new ArrayList<HistoryFeedbackInfo>();
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(key);
            Log.d(TAG, "jsonArray.length() = " + jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++) {
                HistoryFeedbackInfo historyFeedbackInfo = new HistoryFeedbackInfo();
                String info = jsonArray.getString(i);
                JSONObject objectInfo = new JSONObject(info);

                String messageId = JsonUtils.getString(objectInfo, "id");
                String feedModule = JsonUtils.getString(objectInfo, "feedModule");
                String feedTitle = JsonUtils.getString(objectInfo, "feedTitle");
                String feedContent = JsonUtils.getString(objectInfo, "feedContent");
                String feedImgs = JsonUtils.getString(objectInfo, "feedImgs");
                String createTime = JsonUtils.getString(objectInfo, "createTime");
                String feedHandleStatus = JsonUtils.getString(objectInfo, "feedHandleStatus");
                String feedHandleContent = JsonUtils.getString(objectInfo, "feedHandleContent");
                String updateTime = JsonUtils.getString(objectInfo, "updateTime");

                historyFeedbackInfo.setMessageId(messageId);
                historyFeedbackInfo.setFeedModule(feedModule);
                historyFeedbackInfo.setFeedTitle(feedTitle);
                historyFeedbackInfo.setFeedContent(feedContent);
                historyFeedbackInfo.setFeedImgs(feedImgs);
                historyFeedbackInfo.setFeedTime(createTime);
                historyFeedbackInfo.setFeedHandleStatus(feedHandleStatus);
                historyFeedbackInfo.setFeedHandleContent(feedHandleContent);
                historyFeedbackInfo.setUpdateTime(updateTime);
                list.add(historyFeedbackInfo);
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
        return list;
    }

    private GomeAlertDialog loginDialog;
    private void showLoginDialog(){
        if(loginDialog != null && loginDialog.isShowing()) {
            return;
        }
        final GomeAlertDialog.Builder builder = new GomeAlertDialog.Builder(this)
                .setTitle(R.string.dialog_login_alert_feedback_your_suggestion)
                .setPositiveButton(R.string.proceedTo, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intentBroadcast = new Intent(Constants.ACTION_START_ACTIVITY_LOGIN);
                        intentBroadcast.putExtra(Constants.KEY_START_MODE_TARGET_CLASS,
                                Constants.VALUE_TARGET_FEED_BACK_TO_SERVER);
                        gotoGomeAccountService = true;
                        sendBroadcast(intentBroadcast);
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        loginDialog = builder.create();
        loginDialog.setCanceledOnTouchOutside(false);
        loginDialog.show();
    }

    private void dismissLoginDialog(){
        if(loginDialog != null && loginDialog.isShowing()){
            loginDialog.dismiss();
            loginDialog = null;
        }
    }
}
