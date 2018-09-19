package com.gome.usercenter.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import gome.widget.GomeListView;

import android.widget.TextView;

import com.gome.usercenter.R;

/**
 * Created by dongzq on 2017/6/29.
 */

public class DynamicListView extends GomeListView {

    public enum LoadStatus {
        none, normal, loading, noMore
    }

    // 监听器
    private DynamicListViewListener mOnMoreListener;
    private LoadStatus mRefreshStatus = LoadStatus.none;
    private View mLodingView;
    private View mLoadMore;
    private TextView mHeadTitle;
    private TextView mNoMoreView;
    private View mHeaderView;

    private View mFooterView;
    private View mFooterParent;

    public DynamicListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initDynamicListView(context);
    }

    public DynamicListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initDynamicListView(context);
    }

    public DynamicListView(Context context) {
        super(context);
        initDynamicListView(context);
    }

    private void initDynamicListView(Context context) {
        //footer view
        mFooterParent = LayoutInflater.from(context).inflate(R.layout.dynamic_listview_footer, this, false);
        mFooterView = mFooterParent.findViewById(R.id.mFooter);
        mLodingView = mFooterView.findViewById(R.id.loading_view);
        mLoadMore = mFooterView.findViewById(R.id.load_more_button);
        mNoMoreView = (TextView) mFooterView.findViewById(R.id.no_more);
        this.addFooterView(mFooterParent);
        //header view
        mHeaderView = LayoutInflater.from(context).inflate(R.layout.dynamic_listview_header, this, false);
        this.addHeaderView(mHeaderView, null, false);
        mHeadTitle = (TextView) mHeaderView.findViewById(R.id.head_title);
        mLoadMore.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                doMore();
            }
        });
        doneMore();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }

    public void showHeaderView(boolean show) {
        mHeaderView.setVisibility(show ? VISIBLE : GONE);
    }

    public void showFooterView(boolean show) {
        mFooterView.setVisibility(show ? VISIBLE : GONE);
    }

    public void setHeadTitle(int resId) {
        mHeadTitle.setText(resId);
    }
    // 监听器操作

    public DynamicListViewListener getOnMoreListener() {
        return mOnMoreListener;
    }

    public void setOnMoreListener(DynamicListViewListener onMoreListener) {
        this.mOnMoreListener = onMoreListener;
    }

    /**
     * 开始加载更多
     */
    private void doMore(){
        this.setRefreshStatus(LoadStatus.loading);
        mOnMoreListener.onLoadingMore(this);
    }

    /**
     * 加载更多完成之后调用，用于取消加载更多的动画
     */
    public void doneMore() {
        this.setRefreshStatus(LoadStatus.normal);
    }

    public void setRefreshStatus(LoadStatus refreshStatus) {
        if (this.mRefreshStatus != refreshStatus) {
            this.mRefreshStatus = refreshStatus;
            if(refreshStatus == LoadStatus.loading){
                mLodingView.setVisibility(View.VISIBLE);
                mLoadMore.setVisibility(View.GONE);
                mNoMoreView.setVisibility(View.GONE);
            } else if (refreshStatus == LoadStatus.noMore) {
                mLodingView.setVisibility(View.GONE);
                mLoadMore.setVisibility(View.GONE);
                mNoMoreView.setVisibility(View.VISIBLE);
            } else{
                mLodingView.setVisibility(View.GONE);
                mLoadMore.setVisibility(View.VISIBLE);
                mNoMoreView.setVisibility(View.GONE);
            }
            this.invalidate();
        }
    }

    public interface DynamicListViewListener {
        boolean onLoadingMore(DynamicListView dynamicListView);
    }

}
