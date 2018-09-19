package com.gome.usercenter.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;

import com.gome.usercenter.R;
import com.gome.usercenter.utils.Constants;
import com.gome.usercenter.view.TipItemExpandeAdapter;


import java.util.ArrayList;
import java.util.List;



public class GuideLineActivity extends BaseActivity implements ExpandableListView.OnChildClickListener{

    private static final String TAG = Constants.TAG_HEADER + "GuideLine";

    private ImageButton mSearchCancelButton = null;
    private EditText mSearchEditText = null;
    private static final String WEBVIEW_MODE = "webview_mode";
    private static final String GUIDELINE_PAGE = "guideline_page";

    private ExpandableListView mListView = null;
    private TipItemExpandeAdapter mAdapter = null;
    private List<List<String>> mData = new ArrayList<List<String>>();
    private int[] mGroupArrays = new int[] {
            R.array.system_ui_item,
            R.array.facial_recognition_item,
            R.array.safe_mode_item,
            R.array.my_keychain_item,
            R.array.app_lock_item};

    private String url_guide_line = Constants.GUIDELINE_ITEM_URL + "MachineGuide/C72/item";

    private static final int INIT_BUTTON_INPUT_CANCEL = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setBody(R.layout.activity_guide_line);
        mCustomTitle.setText(getResources().getString(R.string.guide_line_title));
        initData();
        initView();
    }

    private void initView() {
        mSearchCancelButton = (ImageButton) findViewById( R.id.image_button_cancel);
        mSearchEditText = (EditText) findViewById( R.id.edit_city_name);

        mSearchCancelButton.setOnClickListener(new OnClickListener(INIT_BUTTON_INPUT_CANCEL));

        ExpandableListView lv = (ExpandableListView) findViewById(android.R.id.list);
        lv.setSaveEnabled(true);
        lv.setItemsCanFocus(true);
        lv.setTextFilterEnabled(true);

        mListView = lv;
        mListView.setGroupIndicator(null);
        mAdapter = new TipItemExpandeAdapter(this, mData);
        mListView.setAdapter(mAdapter);
        //mListView.setDividerHeight(0);
        mListView.setDescendantFocusability(ExpandableListView.FOCUS_AFTER_DESCENDANTS);
        mListView.setOnChildClickListener(this);
        mListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                for (int i = 0; i < mAdapter.getGroupCount(); i++) {
                    if (groupPosition != i) {
                        mListView.collapseGroup(i);
                    }
                }
            }
        });


    }


    private void initData() {
/*
        TypedArray ar = getResources().obtainTypedArray(R.array.function_icon);
        int len = ar.length();
        int[] mGroupArrays = new int[len];
        for (int i = 0; i < len; i++)
            mGroupArrays[i] = ar.getResourceId(i, 0);
        ar.recycle();
*/
        for (int i = 0; i < mGroupArrays.length; i++) {
            List<String> list = new ArrayList<String>();
            String[] childs = getStringArray(mGroupArrays[i]);
            for (int j = 0; j < childs.length; j++) {
                list.add(childs[j]);
            }
            mData.add(list);
        }
    }

    private String[] getStringArray(int resId) {
        return getResources().getStringArray(resId);
    }


   /* class ActionBarOnClick implements View.OnClickListener {
        int temp = INIT_ACTIONBAR_NUM;

        public ActionBarOnClick(int indexs) {
            temp = indexs;
        }

        @Override
        public void onClick(View v) {
            switch (temp) {
                case INIT_ACTIONBAR_EXTRA_NUM:
                    finish();
                    break;
                default:
                    break;
            }
        }
    }*/

    class OnClickListener implements View.OnClickListener {
        int temp = INIT_BUTTON_INPUT_CANCEL;
        public OnClickListener(int indexs) {
            temp = indexs;
        }

        @Override
        public void onClick(View v) {
            switch (temp) {
                case INIT_BUTTON_INPUT_CANCEL:
                    View view = getWindow().peekDecorView();
                    if (view != null) {
                        InputMethodManager inputmanger = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputmanger.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public boolean onChildClick(ExpandableListView parent, View v,
                                int groupPosition, int childPosition, long id) {
        // TODO Auto-generated method stub
        StringBuffer url = new StringBuffer();
        url.append(url_guide_line).append(groupPosition).append("/").append(childPosition).append(".html");
        String mPath = url.toString();
        Log.d(TAG, "guideline_item0 = " + mPath);
        String mtitle = mData.get(groupPosition).get(childPosition);
        Log.d(TAG, "title = " + mtitle);
        Intent intent = new Intent();
        intent.setClassName("com.gome.usercenter", "com.gome.usercenter.activity.ShowPolicyActivity");
        intent.setClass(GuideLineActivity.this, ShowPolicyActivity.class);
        intent.putExtra(WEBVIEW_MODE, "guideline_item");
        intent.putExtra(GUIDELINE_PAGE, String.valueOf(childPosition));
        intent.putExtra("url_guideline_item", mPath);
        intent.putExtra("child_title", mtitle);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        return true;
    }
}
