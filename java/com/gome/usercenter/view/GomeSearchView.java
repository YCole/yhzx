package com.gome.usercenter.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.gome.usercenter.R;
/**
 * Created by jianfeng.xue on 2017/9/30.
 */

public class GomeSearchView extends FrameLayout {

    public AutoCompleteTextView searchEditView;
    public ImageView searchTextClear;

    public GomeSearchView(Context context) {
        super(context);
    }

    public GomeSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context)
                .inflate(R.layout.search_view_layout, this);
        searchEditView = (AutoCompleteTextView) findViewById(R.id.search_edit_text);
        searchTextClear = (ImageView) findViewById(R.id.search_text_clear);
        searchTextClear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                searchEditView.setText("");
            }
        });
    }

    public GomeSearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

}