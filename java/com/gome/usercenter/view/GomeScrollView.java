package com.gome.usercenter.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * Created by jianfeng.xue on 2017/9/7.
 */

public class GomeScrollView extends ScrollView {
    private int scrollViewHeight;

    public int getScrollViewHeight() {
        return scrollViewHeight;
    }

    public void setScrollViewHeight(int scrollViewHeight) {
        this.scrollViewHeight = scrollViewHeight;
    }

    public GomeScrollView(Context context) {
        super(context);
    }

    public GomeScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public GomeScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (scrollViewHeight > -1) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(scrollViewHeight,
                    MeasureSpec.AT_MOST);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}