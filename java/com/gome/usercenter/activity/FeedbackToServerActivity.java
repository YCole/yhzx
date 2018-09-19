package com.gome.usercenter.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gome.usercenter.R;
import com.gome.usercenter.helper.HttpCallbackStringListener;
import com.gome.usercenter.utils.AccountUtils;
import com.gome.usercenter.utils.ActivityUtils;
import com.gome.usercenter.utils.Constants;
import com.gome.usercenter.utils.DialogUtils;
import com.gome.usercenter.utils.ImageUtils;
import com.gome.usercenter.utils.JsonUtils;
import com.gome.usercenter.utils.NetworkUtils;
import com.gome.usercenter.view.citypicker.ModulePicker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FeedbackToServerActivity extends BaseActivity {

    private final String TAG = Constants.TAG_HEADER + "FeedbackToServerActivity";

    private Context mContext;

    private RelativeLayout mLayoutSelectModule;
    private EditText mModuleText;
    private TextView mContactUs;

    private EditText mEditTitle;
    private EditText mEditContactInfo;
    private EditText mEditSuggestions;
    private Button mBtnSubmit;

    private ImageView mImageSelect;
    private ImageView mImageDelete;
    private LinearLayout mLayoutSelectImage;

    private TextView titleMaxLength;
    private TextView suggestionMaxLength;
    private TextView imageMaxLength;

    private String picBase64Format = null;
    public static final int PHOTOALBUMS = 0;
    public static final int CHOOSEMODULE = 1;
    private String pathImageSelect;

    private String moduleSelect = null;
    Bitmap bitmapAlbum = null;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    submitSuggestionsToServer();
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setBody(R.layout.activity_feedback_to_server);
        mCustomTitle.setText(getResources().getString(R.string.feedback));
        mContext = this;
        initView();
    }

    private void initView() {
        mLayoutSelectModule = (RelativeLayout) findViewById(R.id.layout_select_module);
        mLayoutSelectModule.setOnClickListener(new OnClickListener());
        mModuleText = (EditText) findViewById(R.id.module_text);
        mModuleText.setOnClickListener(new OnClickListener());

        mContactUs = (TextView) findViewById(R.id.contact_us_tel);
        //Linkify.addLinks(mContactUs, Linkify.MAP_ADDRESSES|Linkify.PHONE_NUMBERS);
        /*
        final StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append(getText(R.string.contact_us));
        LinkifyUtils.linkify(mContactUs, contentBuilder, new LinkifyUtils.OnClickListener() {
            @Override
            public void onClick(int id) {
                switch (id) {
                    case 0:
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        Uri uri = Uri.parse("tel://" + "4008988666");
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setData(uri);
                        startActivity(intent);
                        break;
                    default:break;
                }
            }
        });*/
        mContactUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                Uri uri = Uri.parse("tel://" + "4008988666");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(uri);
                startActivity(intent);
            }
        });

        mEditTitle = (EditText) findViewById(R.id.message_title);
        mEditTitle.addTextChangedListener(OnTitleEditTextWatcher);

        mEditContactInfo = (EditText) findViewById(R.id.message_contact_info);
        mEditSuggestions = (EditText) findViewById(R.id.message_detail);
        mEditSuggestions.addTextChangedListener(OnSuggestionEditTextWatcher);
        mEditSuggestions.setOnTouchListener(mTouchListener);

        mBtnSubmit = (Button) findViewById(R.id.button_submit);
        mBtnSubmit.setOnClickListener(new OnClickListener());

        mImageSelect = (ImageView) findViewById(R.id.image_show_picture);
        mImageSelect.setVisibility(View.INVISIBLE);
        //mImageSelect.setOnClickListener(new OnClickListener());

        mImageDelete = (ImageView) findViewById(R.id.image_delete);
        mImageDelete.setVisibility(View.INVISIBLE);
        mImageDelete.setOnClickListener(new OnClickListener());

        mLayoutSelectImage = (LinearLayout) findViewById(R.id.layout_select_image);
        mLayoutSelectImage.setOnClickListener(new OnClickListener());

        String title = mEditTitle.getText().toString().trim();
        mBtnSubmit.setEnabled(!TextUtils.isEmpty(title));

        titleMaxLength = (TextView) findViewById(R.id.title_max_length);
        suggestionMaxLength = (TextView) findViewById(R.id.suggestion_max_length);
        imageMaxLength = (TextView) findViewById(R.id.image_max_length);

    }

    public String getRequestJsonForFeedback(Context context){
        String title = mEditTitle.getText().toString();
        String contactWay = mEditContactInfo.getText().toString();
        String suggestion = mEditSuggestions.getText().toString();

        StringBuffer sModule = new StringBuffer();
        String module = null;
        if(null!=moduleSelect) {
            sModule.append("【").append(moduleSelect).append("】");
            module = sModule.toString();
        }

        String token = AccountUtils.getGomeAccountTokenValue(context);

        JSONObject jsonObject = new JSONObject();
        try{

            JSONArray feedImgs = new JSONArray();
            if(null==picBase64Format){
                feedImgs=null;
            }else{
                JSONObject avatar1 = new JSONObject();
                avatar1.put("avatar", picBase64Format);
                avatar1.put("avatarType", "jpeg");
                feedImgs.put(avatar1);
            }

            jsonObject.put("imei", ActivityUtils.getDeviceID(context));
            jsonObject.put("token", token);
            jsonObject.put("feedModule", module);
            jsonObject.put("feedTitle", title);
            jsonObject.put("feedContent", suggestion);
            jsonObject.put("contactWay", contactWay);
            jsonObject.put("feedImgs", feedImgs);

            picBase64Format = null;
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return jsonObject.toString();
    }

    private void checkNetworkEditCondition(){
        if(!ActivityUtils.checkNetworkConnection(FeedbackToServerActivity.this, false)){
            // modified by zhiqiang.dong@gometech.com for PRODUCTION-2436
            //ActivityUtils.setToastShow(getApplicationContext(), R.string.suggestion_send_fail);
            return;
        }
        if(!checkEditInfomation()){
            return;
        }

        Log.d(TAG, "checkIfShouldCompressBitmap()");
        DialogUtils.setProgressingDialog(this, null, getResources().getString(R.string.progress_feedback_to_server));
        checkIfShouldCompressBitmap();
    }

    private void checkIfShouldCompressBitmap(){
        if(null != bitmapAlbum){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Bitmap bmCompress = ImageUtils.compressLimitDecodeCompress(bitmapAlbum, 1024, 1080f, 1920f, 200);
                    //saveBitmapToSdCard(bmCompress, "/storage/emulated/0/", "alb_compress");
                    picBase64Format = ImageUtils.Bitmap2StrByBase64(bmCompress);
                    mHandler.sendEmptyMessage(1);
                }
            }).start();

        }else{
            submitSuggestionsToServer();
        }
    }

    public void submitSuggestionsToServer(){
        Log.d(TAG, "submitSuggestionsToServer()");
        String requestJsonString = getRequestJsonForFeedback(this);
        String requestUrl = Constants.GOME_BASE_URL + "system/feedback";
        NetworkUtils.doPost(this, requestUrl, new HttpCallbackStringListener() {
            @Override
            public void onFinish(String response) {
                DialogUtils.progressDialogDismiss();
                String result = ActivityUtils.getDescryptJsonString(response);
                Log.d(TAG, "result = " + result);

                try {
                    JSONObject jsonObj = new JSONObject(result);
                    String resCode = JsonUtils.getString(jsonObj, "resCode");
                    String resMsg = JsonUtils.getString(jsonObj, "resMsg");
                    Log.d(TAG, "resCode = " + resCode);
                    Log.d(TAG, "resMsg = " + resMsg);
                    if(Constants.RESPONSE_CODE_SUCCESS.equals(resCode)){
                        Intent intent = new Intent();
                        setResult(RESULT_OK, intent);
                        finish();
                    }else if(Constants.RESPONSE_CODE_TOKEN_INVAILD.equals(resCode)
                            || Constants.RESPONSE_CODE_NOT_LOGIN.equals(resCode)
                            || Constants.RESPONSE_CODE_PLEASE_LOGIN.equals(resCode)){
                        Intent intentBroadcast = new Intent(Constants.ACTION_START_ACTIVITY_LOGIN);
                        sendBroadcast(intentBroadcast);
                    }else if(Constants.RESPONSE_CODE_UPLOAD_PICTURE_FAIL.equals(resCode)
                            || Constants.RESPONSE_CODE_UPLOAD_PICTURE_FAIL.equals(resCode)){
                        ActivityUtils.setToastShow(getApplicationContext(), R.string.suggestion_send_fail);
                    }
                } catch (JSONException e) {
                    Log.d(TAG, "Parse Json error");
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Exception e) {
                DialogUtils.progressDialogDismiss();
                ActivityUtils.setToastShow(getApplicationContext(), R.string.suggestion_send_fail);
                Log.d(TAG, "e = " + e.toString());
            }
        }, requestJsonString);
    }

    boolean checkEditInfomation(){
        String title = mEditTitle.getText().toString();
        String contactInfo = mEditContactInfo.getText().toString();
        String suggestion = mEditSuggestions.getText().toString();

        if (TextUtils.isEmpty(title)
                //|| TextUtils.isEmpty(contactInfo) || TextUtils.isEmpty(suggestion)
                ) {
            ActivityUtils.setToastShow(getApplicationContext(), R.string.toast_input_infomation_is_null);
            return false;
        }
        if(!TextUtils.isEmpty(contactInfo)){
            if(!ActivityUtils.isEmail(contactInfo)  &&  !ActivityUtils.isMobileNO(contactInfo)){
                ActivityUtils.setToastShow(getApplicationContext(), R.string.toast_input_phone_or_email_correct_format);
                return false;
            }
        }
        return true;
    }

    class OnClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.button_submit){
                checkNetworkEditCondition();
            }else if(v.getId() == R.id.layout_select_image
//                    || v.getId() == R.id.image_show_picture
                    ){
                Intent openAlbumIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                openAlbumIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(openAlbumIntent, PHOTOALBUMS);
            }else if(v.getId() == R.id.image_delete){
                mLayoutSelectImage.setVisibility(View.VISIBLE);
                mImageSelect.setVisibility(View.INVISIBLE);
                mImageDelete.setVisibility(View.INVISIBLE);
                mImageSelect.setImageBitmap(null);
                picBase64Format = null;
                bitmapAlbum = null;
                imageMaxLength.setText(getString(R.string.hint_max_text_1));
            }else if(v.getId() == R.id.layout_select_module || v.getId()== R.id.module_text){
                closeKeyboard();
                showSelectModuleDialog();
                /*
                Intent chooseIntent = new Intent();
                chooseIntent.setClass(FeedbackToServerActivity.this, ModuleSelectActivity.class);
                startActivityForResult(chooseIntent, CHOOSEMODULE);*/
            }
        }
    }

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if(event.getAction() == MotionEvent.ACTION_DOWN
                    || event.getAction() == MotionEvent.ACTION_MOVE){
                //按下或滑动时请求父节点不拦截子节点
                v.getParent().requestDisallowInterceptTouchEvent(true);
            }
            if(event.getAction() == MotionEvent.ACTION_UP){
                //抬起时请求父节点拦截子节点
                v.getParent().requestDisallowInterceptTouchEvent(false);
            }
            return false;
        }
    };

    private void closeKeyboard() {
        View view = getWindow().peekDecorView();
        if (view != null) {
            InputMethodManager inputmanger = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputmanger.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Uri uri = null;
        switch (requestCode) {
            case PHOTOALBUMS:
                if(null == data)
                    break;

                uri = data.getData();

                String[] proj = { MediaStore.Images.Media.DATA, MediaStore.Images.Media.SIZE};
                Cursor cursor = managedQuery(uri, proj, null, null,null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                int size_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE);
                cursor.moveToFirst();
                pathImageSelect = cursor.getString(column_index);
                String size = cursor.getString(size_index);
                if(size != null && size.trim().length() != 0) {
                    long sizeL = Long.parseLong(size)/1024/1024;
                    if(sizeL >= 10){
                        ActivityUtils.setToastShow(getApplicationContext(), R.string.string_toast_picture_out_of_size);
                        return;
                    }
                }

                bitmapAlbum = BitmapFactory.decodeFile(pathImageSelect);

                mLayoutSelectImage.setVisibility(View.INVISIBLE);
                mImageSelect.setVisibility(View.VISIBLE);
                mImageDelete.setVisibility(View.VISIBLE);
                mImageSelect.setImageBitmap(bitmapAlbum);

                imageMaxLength.setText("1" + getString(R.string.count_text_1));
                break;
            case CHOOSEMODULE:
                if(null == data)
                    break;
                moduleSelect = data.getStringExtra("module");
                break;
            case Constants.CHECK_NETWORK_REQUEST_CODE:
                //ActivityUtils.checkNetworkConnection(FeedbackToServerActivity.this);
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public static void saveBitmapToSdCard(Bitmap mBitmap, String path, String picName){
        //this only for debug
        File f = new File(path+picName + ".jpg");
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
        try {
            fOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showSelectModuleDialog() {
        /*
        final String[] moduleArray = mContext.getResources().getStringArray(R.array.feedbacks_module);
        GomeAlertDialog.Builder builder = new GomeAlertDialog.Builder(this)
                .setItems(moduleArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        moduleSelect = moduleArray[which];
                        mModuleText.setText(moduleSelect);
                    }
                });
        GomeAlertDialog dialog = builder.create();
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.show();*/
        ModulePicker modulePicker = new ModulePicker.Builder(mContext).textSize(15)
                .module(getResources().getString(R.string.string_module_crash))
                .build();
        modulePicker.show();
        modulePicker.setOnModuleItemClickListener(new ModulePicker.OnModuleItemClickListener() {
            public void onSelected(String module) {
                if(null == module){
                    module = getResources().getString(R.string.string_module_crash);
                }
                moduleSelect = module;
                mModuleText.setText(moduleSelect);
            }
            public void onCancel() {
            }
        });
    }

    private TextWatcher OnTitleEditTextWatcher = new TextWatcher() {
        private CharSequence temp="";
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // TODO Auto-generated method stub
            temp = s;
            String title = mEditTitle.getText().toString().trim();
            mBtnSubmit.setEnabled(!TextUtils.isEmpty(title));
            titleMaxLength.setText(s.length() + getResources().getString(R.string.count_text_50));
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
            // TODO Auto-generated method stub
        }

        @Override
        public void afterTextChanged(Editable s) {
            // TODO Auto-generated method stub
        }
    };

    private TextWatcher OnSuggestionEditTextWatcher = new TextWatcher() {
        private CharSequence temp="";
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // TODO Auto-generated method stub
            temp = s;
            suggestionMaxLength.setText(s.length() + getResources().getString(R.string.count_text_500));
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
            // TODO Auto-generated method stub
        }

        @Override
        public void afterTextChanged(Editable s) {
            // TODO Auto-generated method stub
        }
    };

}
