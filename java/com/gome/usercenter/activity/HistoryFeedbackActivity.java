package com.gome.usercenter.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.gome.usercenter.R;
import com.gome.usercenter.helper.HttpCallbackStringListener;
import com.gome.usercenter.utils.ActivityUtils;
import com.gome.usercenter.utils.Constants;
import com.gome.usercenter.utils.DialogUtils;
import com.gome.usercenter.utils.NetworkUtils;
import com.gome.usercenter.utils.TimeUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import gome.widget.GomeListView;

/**
 * Created by jianfeng.xue on 2017/7/26.
 */

public class HistoryFeedbackActivity extends BaseActivity {

    private final String TAG = Constants.TAG_HEADER + "HistoryFeedbackActivity";

    TextView text_title;
    ImageView image_view;
    TextView text_content;
    TextView text_time;

    View view_solution;
    LinearLayout layout_solution;

    Bitmap image;

    private GomeListView mListView;
    private SimpleAdapter mSolutionAdapter;
    private List<Map<String, Object>> mSolutionData;
    private TextView mEmptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setBody(R.layout.activity_history_feedback);
        mCustomTitle.setText(getResources().getString(R.string.my_feedback));

        text_title = (TextView) findViewById(R.id.title);
        image_view = (ImageView) findViewById(R.id.image);
        text_content = (TextView) findViewById(R.id.content);
        text_time = (TextView) findViewById(R.id.time);

        view_solution = (View) findViewById(R.id.view_solution);
        layout_solution = (LinearLayout) findViewById(R.id.layout_solution);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if(bundle!=null){
            String messageId = bundle.getString("id");
            String feedModule = bundle.getString("feedModule");
            String feedTitle = bundle.getString("feedTitle");
            String feedContent = bundle.getString("feedContent");
            String image_url = bundle.getString("feedImgs");
            String createTime = bundle.getString("createTime");
            String updateTime = bundle.getString("updateTime");
            String feedHandleStatus = bundle.getString("feedHandleStatus");
            String feedHandleContent = bundle.getString("feedHandleContent");


            StringBuffer sTitle = new StringBuffer();
            if(null !=feedModule){
                sTitle.append(feedModule);
            }
            if(null !=feedTitle){
                sTitle.append(feedTitle);
                text_title.setText(sTitle.toString());
            }

            if(null != messageId){
                if(null != feedHandleStatus && feedHandleStatus.equals("1")){
                    resetHandleStatus(messageId);
                }
            }

            StringBuffer sTime = new StringBuffer();
            if(null !=createTime && timet(createTime) != null){
                sTime.append(getString(R.string.history_time)).append(" ").append(timet(createTime));
                text_time.setText(sTime.toString());
            }else{
                text_time.setVisibility(View.GONE);
            }
            if(null !=feedContent){
                text_content.setText(feedContent);
            }else{
                text_content.setVisibility(View.GONE);
            }
            if(null != image_url){
                DialogUtils.setProgressingDialog(this, null, getResources().getString(R.string.progress_dialog_loading));
                new getImageFromServer().execute(image_url);
            }else{
                image_view.setVisibility(View.GONE);
            }
            if(null != feedHandleContent){
                view_solution.setVisibility(View.VISIBLE);
                layout_solution.setVisibility(View.VISIBLE);
                try{
                    JSONObject jsonObject = new JSONObject(feedHandleContent);
                    Iterator keys = jsonObject.keys();
                    mSolutionData = new ArrayList<>();
                    while (keys.hasNext()){
                        String key = String.valueOf(keys.next());
                        String value = jsonObject.getString(key);

                        Map<String, Object> map = new HashMap<>();
                        map.put("title", key+": ");
                        map.put("content", value);
                        Log.d(TAG, "map = " + map.toString());
                        mSolutionData.add(map);
                    }
                    Log.d(TAG, "mSolutionData = " + mSolutionData.toString());

                    String[] from = {"title", "content"};
                    int[] to = {R.id.title, R.id.content};
                    // init list
                    mListView = (GomeListView) findViewById(R.id.list);
                    mSolutionAdapter = new SimpleAdapter(getApplicationContext(), mSolutionData,
                            R.layout.custom_textview, from, to);
                    mEmptyView = (TextView) findViewById(R.id.empty_view);
                    mListView.setAdapter(mSolutionAdapter);
                }catch(JSONException e){
                    Log.d(TAG, "feedHandleContent e = " + e.toString());
                }
            }else{
                view_solution.setVisibility(View.INVISIBLE);
                layout_solution.setVisibility(View.INVISIBLE);
            }
        }
    }

    public Bitmap getImageInputStream(String imageurl){
        URL url;
        HttpURLConnection connection = null;
        Bitmap bitmap=null;
        try {
            url = new URL(imageurl);
            connection=(HttpURLConnection)url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            InputStream inputStream=connection.getInputStream();
            bitmap= BitmapFactory.decodeStream(inputStream);
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    Handler handler=new Handler(){
        public void handleMessage(android.os.Message msg) {
            if(msg.what==0x123){
                DialogUtils.progressDialogDismiss();
                image_view.setImageBitmap(image);
            }
        };
    };

    class getImageFromServer extends AsyncTask<String, Integer, Void> {
        protected Void doInBackground(String... params) {
            image = getImageInputStream((String)params[0]);
            return null;
        }

        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Message message=new Message();
            message.what=0x123;
            handler.sendMessage(message);
        }

    }

    public void saveImageLocal(Bitmap bitmap, String path){
        File file=new File(path);
        FileOutputStream fileOutputStream=null;

        if(!file.exists()){
            file.mkdir();
        }
        try {
            fileOutputStream = new FileOutputStream(path+"/"+System.currentTimeMillis()+".png");
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100,fileOutputStream);
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String timet(String timeString) {
        long time = Long.parseLong(timeString);
        try {
            return TimeUtils.longToString(time , "yyyy.MM.dd HH:mm");
        }catch(Exception e){
            return null;
        }
    }

    private void resetHandleStatus(String messageId){
        if(!ActivityUtils.isNetworkConnected(this)){
            return;
        }

        Log.d(TAG, "resetHandleStatus()");
        String requestUrl = Constants.GOME_BASE_URL + "system/updateHandleStatus?id=" + messageId;
        NetworkUtils.doGet(this, requestUrl, new HttpCallbackStringListener() {
            @Override
            public void onFinish(String response) {
                Log.d(TAG, "response = " + response);
                String result = ActivityUtils.getDescryptJsonString(response);
                Log.d(TAG, "result = " + result);
            }

            @Override
            public void onError(Exception e) {
                Log.d(TAG, "e = " + e.toString());
            }
        });
    }
}