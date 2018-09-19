package com.gome.usercenter.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gome.usercenter.R;

import java.util.List;


public class TipItemExpandeAdapter extends BaseExpandableListAdapter {

    private Context mContext;
    private LayoutInflater mInflater = null;

    private String[] mGroupStrings = null;
    private int[] mDrawableResource = null;
    private List<List<String>> mChildData = null;
    TypedArray mTipIconArray;
    public TipItemExpandeAdapter(Context ctx, List<List<String>> list) {
        mContext = ctx;
        mInflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mGroupStrings = mContext.getResources().getStringArray(R.array.guide_line_title);
        mTipIconArray = mContext.getResources().obtainTypedArray(R.array.drawable_resources);
        mChildData = list;
    }

    public void setData(List<List<String>> list) {
        mChildData = list;
    }

    @Override
    public int getGroupCount() {
        // TODO Auto-generated method stub
        return mChildData.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        // TODO Auto-generated method stub
        return mChildData.get(groupPosition).size();
    }

    @Override
    public List<String> getGroup(int groupPosition) {
        // TODO Auto-generated method stub
        return mChildData.get(groupPosition);
    }

    @Override
    public String getChild(int groupPosition, int childPosition) {
        // TODO Auto-generated method stub
        return mChildData.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        // TODO Auto-generated method stub
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        // TODO Auto-generated method stub
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        GroupViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.guideline_list_item, null);

            holder = new GroupViewHolder();

            holder.mGroupTipIcon = (ImageView) convertView
                    .findViewById(R.id.tip_icon);
            holder.mGroupTipName = (TextView) convertView
                    .findViewById(R.id.tip_name);
            holder.mGroupTipNext = (ImageView) convertView
                    .findViewById(R.id.tip_next_pager);

            convertView.setTag(holder);
        } else {
            holder = (GroupViewHolder) convertView.getTag();
        }

        holder.mGroupTipIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        holder.mGroupTipNext.setScaleType(ImageView.ScaleType.FIT_CENTER);

//        holder.mGroupTipIcon.setVisibility(View.GONE);
        holder.mGroupTipName.setText(mGroupStrings[groupPosition]);
        holder.mGroupTipIcon.setImageResource(mTipIconArray.getResourceId(groupPosition, R.drawable.gome_icon_launcher_iris));
        if(isExpanded){
            holder.mGroupTipNext.setImageResource(R.drawable.arrow_up);
        }else{
            holder.mGroupTipNext.setImageResource(R.drawable.arrow_left);
        }
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.child_guideline_list_item, null);
        }
        ChildViewHolder holder = new ChildViewHolder();

        holder.mChildTipIcon = (ImageView) convertView
                .findViewById(R.id.tip_icon);
        holder.mChildTipName = (TextView) convertView
                .findViewById(R.id.tip_name);
        holder.mChildTipNext = (ImageView) convertView
                .findViewById(R.id.tip_next_pager);
            holder.mChildTipIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        holder.mChildTipNext.setScaleType(ImageView.ScaleType.FIT_CENTER);

        holder.mChildTipIcon.setVisibility(View.GONE);
        holder.mChildTipName.setText(getChild(groupPosition, childPosition));

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        // TODO Auto-generated method stub
        /* 很重要：实现ChildView点击事件，必须返回true */
        return true;
    }

    private class GroupViewHolder {
        ImageView mGroupTipIcon;
        TextView mGroupTipName;
        ImageView mGroupTipNext;
    }

    private class ChildViewHolder {
        ImageView mChildTipIcon;
        TextView mChildTipName;
        ImageView mChildTipNext;
    }

}