package com.gome.usercenter.view;

import android.annotation.NonNull;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.gome.usercenter.R;

import gome.app.GomeAlertDialog;

/**
 * Created by dongzq on 2017/10/16.
 */

public class ListAlertDialog extends GomeAlertDialog {
    private ListView mListView;
    private TextView mTitleView;
    private Context mContext;
    private String mTitle;
    private String[] mStrArray;
    private DialogInterface.OnClickListener mOnClickListener;

    public ListAlertDialog(@NonNull Context context, String title, @NonNull String[] strs, DialogInterface.OnClickListener listener) {
        super(context);
        mContext = context;
        mTitle = title;
        mStrArray = strs;
        mOnClickListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setContentView(R.layout.list_dialog_layout);
        ArrayAdapter adapter = new ArrayAdapter(mContext, R.layout.list_dialog_item, mStrArray);
        mTitleView = (TextView) findViewById(R.id.title);
        mListView = (ListView) findViewById(R.id.list);
        mListView.setAdapter(adapter);
        if (mTitle != null) {
            mTitleView.setText(mTitle);
        } else {
            mTitleView.setVisibility(View.GONE);
        }
        if (mOnClickListener != null) {
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    mOnClickListener.onClick(ListAlertDialog.this, position);
                    ListAlertDialog.this.dismiss();
                }
            });
        }
    }
}
