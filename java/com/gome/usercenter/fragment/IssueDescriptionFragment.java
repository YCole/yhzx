package com.gome.usercenter.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gome.usercenter.R;
import com.gome.usercenter.activity.BaseActivity;
import com.gome.usercenter.utils.ActivityUtils;
import com.gome.usercenter.utils.Constants;
import com.gome.usercenter.view.FlowLayoutView;

import java.util.ArrayList;

/**
 * Created by dongzq on 2017/7/13.
 */

public class IssueDescriptionFragment extends Fragment //implements AdapterView.OnItemClickListener
{
    private final String TAG = Constants.TAG_HEADER + "IssueDescriptionFragment";
    private EditText mIssueEdit;
    //private GridView mIssueGridView;
    private ArrayList<String> mSelectedPositions;
    private Button mOkBtn;
    private static final int FAILURE_ISSUE = 100;
    private static final String ISSUE_DESCRIPTION = "issue_description";
    private static final String SELECT_DESCRIPTION = "select_description";

    /***/
    private String[] mVals;
    private LayoutInflater mInflater;
    private FlowLayoutView mFlowLayout;
    private String textSelect;
    private Context mContext;
    private TextView mMaxLength;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        ((BaseActivity)getActivity()).setCustomTitle(R.string.issue_desciption_fragment_label);
        mContext = getActivity();
    }

    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_issue_description, container, false);
        mIssueEdit = (EditText) view.findViewById(R.id.issue_edit);
        mOkBtn = (Button) view.findViewById(R.id.ok);
        mMaxLength = (TextView) view.findViewById(R.id.max_length);

        mIssueEdit.addTextChangedListener(OnIssueEditTextWatcher);
        mSelectedPositions = new ArrayList<String>();

        mVals = getResources().getStringArray(R.array.issue_shortcuts);

        Intent intent = getActivity().getIntent();
        String storeText = intent.getStringExtra(ISSUE_DESCRIPTION);
        String textSelect = intent.getStringExtra(SELECT_DESCRIPTION);


        if(textSelect != null){
            for(int i=0; i<mVals.length; i++){
                if(textSelect.contains(mVals[i]))
                    mSelectedPositions.add(mVals[i]);
            }
        }
        if (storeText != null) {
            mIssueEdit.setText(storeText);
            /* modified-begin by zhiqiang.dong@gometech.com for PRODUCTION-2350 */
            mIssueEdit.setSelection(storeText.length());
            /* modified-end */
        }

        //mIssueGridView = (GridView) view.findViewById(R.id.issue_gridview);
        mOkBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String text = ActivityUtils.trimSpace(mIssueEdit.getText().toString());
                SharedPreferences dsp = PreferenceManager.getDefaultSharedPreferences(mContext);
                dsp.edit().putString("Missue_description", text).apply();
                if ("".equals(text)) {
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.edit_not_null),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent();
                intent.putExtra(ISSUE_DESCRIPTION, text);
                Log.d(TAG, "mSelectedPositions.size() = " + mSelectedPositions.size());
                if(mSelectedPositions.size() > 0 ){
                    intent.putExtra(SELECT_DESCRIPTION, mSelectedPositions.toString());
                    SharedPreferences dsp1 = PreferenceManager.getDefaultSharedPreferences(mContext);
                    dsp1.edit().putString("Mselect_description", mSelectedPositions.toString()).apply();
                }else{
                    intent.putExtra(SELECT_DESCRIPTION, "");
                }
                getActivity().setResult(Activity.RESULT_OK, intent);
                getActivity().finish();
            }
        });

        setCommitButtonState();

        mInflater = inflater;
        mFlowLayout = (FlowLayoutView) view.findViewById(R.id.flow_layout_view);
        initView();
        return view;
    }

    private void initView() {
        /*
        ArrayAdapter adapter = new ArrayAdapter<String>(getActivity(), R.layout.issue_gridview_item,
                getResources().getStringArray(R.array.issue_shortcuts));
        mIssueGridView.setAdapter(adapter);
        mIssueGridView.setOnItemClickListener(this);
        */

        for (int i = 0; i < mVals.length; i++) {
            final TextView tv = (TextView) mInflater.inflate(
                    R.layout.item_flow_layout, mFlowLayout, false);
            tv.setText(mVals[i]);
            final String item_string = tv.getText().toString();
            if(mSelectedPositions.contains(item_string)){
                tv.setBackgroundResource(R.drawable.selectd_bg);
                tv.setTextColor(mContext.getResources().getColor(R.color.gome_button_focus_text_color));
            }else{
                tv.setBackgroundResource(R.drawable.unselected_bg);
                tv.setTextColor(mContext.getResources().getColor(R.color.ic_default_back_text_info_color));
            }
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mSelectedPositions.contains(item_string)){
                        mSelectedPositions.remove(item_string);
                        tv.setBackgroundResource(R.drawable.unselected_bg);
                        tv.setTextColor(mContext.getResources().getColor(R.color.ic_default_back_text_info_color));
                    } else {
                        mSelectedPositions.add((item_string));
                        tv.setBackgroundResource(R.drawable.selectd_bg);
                        tv.setTextColor(mContext.getResources().getColor(R.color.gome_button_focus_text_color));
                    }
                    setCommitButtonState();
                }
            });
            mFlowLayout.addView(tv);
        }
    }
/*
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if (mSelectedPositions.contains((String) mIssueGridView.getAdapter().getItem(position))) {
            mSelectedPositions.remove((String) mIssueGridView.getAdapter().getItem(position));
            view.setBackgroundResource(R.drawable.unselected_bg);
            Log.d("dongzq", "unselect " + mIssueGridView.getAdapter().getItem(position));
        } else {
            mSelectedPositions.add((String) mIssueGridView.getAdapter().getItem(position));
            view.setBackgroundResource(R.drawable.selectd_bg);
            Log.d("dongzq", "select " + mIssueGridView.getAdapter().getItem(position));
        }
    }
    */

    private TextWatcher OnIssueEditTextWatcher = new TextWatcher() {
        private CharSequence temp="";
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // TODO Auto-generated method stub
            temp = s;
            mMaxLength.setText(s.length() + getResources().getString(R.string.count_text_200));
            setCommitButtonState();
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
            // TODO Auto-generated method stub
        }

        @Override
        public void afterTextChanged(Editable s) {
            // TODO Auto-generated method stub
            //if (temp.length() >= 500) {
            //    ActivityUtils.setToastShow(mContext, R.string.toast_out_of_max_length);
            //}
        }
    };

    private void setCommitButtonState(){
        String issue = mIssueEdit.getText().toString();
        boolean state = !TextUtils.isEmpty(issue);
        mOkBtn.setEnabled(state);
    }
}
