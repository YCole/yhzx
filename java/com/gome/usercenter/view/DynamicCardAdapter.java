package com.gome.usercenter.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gome.usercenter.R;
import com.gome.usercenter.module.HistoryCardInfo;
import com.gome.usercenter.module.MaintenanceCardInfo;
import com.gome.usercenter.module.ReservationCardInfo;

import java.util.List;

/**
 * Created by dongzq on 2017/7/17.
 */

public class DynamicCardAdapter extends BaseAdapter {

    public static final int RESERVATION_CARD = 1;
    public static final int MAINTENANCE_CARD = 2;
    public static final int HISTORY_CARD = 3;
    private Context mContext;
    private int mCardType;
    private List<String> mInfos;

    private boolean mFoldState = true;//true: fold, false: expand
    private boolean mSearch = false;

    public DynamicCardAdapter(Context context, int type, List<String> infos, boolean state, boolean search) {
        super();
        mContext = context;
        mCardType = type;
        mInfos = infos;
        mFoldState = state;
        mSearch = search;
    }

    public int getCount() {
        return mInfos.size();
    }

    public List<String> getInfos() {
        return mInfos;
    }

    public Object getItem(int position) {
        return mInfos.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    public void setFoldState(boolean state) {
        mFoldState = state;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder holder;
        if (convertView == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.dynamic_card_list_item, parent, false);
            holder = new ViewHolder();
            holder.title = (TextView) view.findViewById(R.id.title);
            holder.summary = (TextView) view.findViewById(R.id.summary);
            holder.divider = (View) view.findViewById(R.id.divider_line);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }
        if (mCardType == RESERVATION_CARD) {
            holder.title.setText(ReservationCardInfo.getCardTitles(mFoldState)[position]);
            holder.summary.setText((CharSequence) mInfos.get(position));
            holder.summary.setTextAppearance(ReservationCardInfo.getCardSummayAppearance(mFoldState)[position]);
            if(mSearch){
                if(position == (getCount()-1)){
                    holder.divider.setVisibility(View.INVISIBLE);
                }else{
                    holder.divider.setVisibility(View.VISIBLE);
                }
            }
        } else if (mCardType == MAINTENANCE_CARD) {
            holder.title.setText(MaintenanceCardInfo.getCardTitles(mFoldState)[position]);
            holder.summary.setText((CharSequence) mInfos.get(position));
            holder.summary.setTextAppearance(MaintenanceCardInfo.getCardSummayAppearance(mFoldState)[position]);
            if(mSearch){
                if(position == (getCount()-1)){
                    holder.divider.setVisibility(View.INVISIBLE);
                }else{
                    holder.divider.setVisibility(View.VISIBLE);
                }
            }
        } else {
            holder.title.setText(HistoryCardInfo.getCardTitles(mFoldState)[position]);
            holder.summary.setText((CharSequence) mInfos.get(position));
            holder.summary.setTextAppearance(HistoryCardInfo.getCardSummayAppearance(mFoldState)[position]);
            if(position == (getCount()-1)){
                holder.divider.setVisibility(View.INVISIBLE);
            }else{
                holder.divider.setVisibility(View.VISIBLE);
            }
            view.setBackgroundResource(R.drawable.shape_card_item_footer);
        }
        return view;
    }

    private class ViewHolder {
        TextView title = null;
        TextView summary = null;
        View divider = null;
    }
}
