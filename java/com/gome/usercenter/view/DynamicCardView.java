package com.gome.usercenter.view;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.gome.usercenter.R;
import com.gome.usercenter.module.CardInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jianfeng.xue on 2017/7/14.
 */

public class DynamicCardView  extends ListView {


    private View mHeaderView;
    private View mFooterView;
    private View mFooterParent;
    private DynamicCardAdapter mAdapter;
    private Context mContext;
    private TextView mFoldBtn;
    private boolean mFoldState = true;
    private CardInfo mCardViewInfo;
    private int mCardType;

    private TextView mHeaderText;
    private TextView mCardBtn;

    private DynamicCardFooterClickListener mFooterListener;
    private String mKey;

    private boolean mSearch = false;

    public DynamicCardView(Context context, CardInfo cardInfo, int type, boolean fold) {
        super(context);
        mContext = context;
        mCardViewInfo = cardInfo;
        mCardType = type;
        mFoldState = fold;
        initAdapter(mCardViewInfo.getCardInfos(mFoldState));
        initDynamicCardView(context);
    }
    public DynamicCardView(Context context, CardInfo cardInfo, boolean isSearch, int type) {
        super(context);
        mContext = context;
        mCardViewInfo = cardInfo;
        mCardType = type;
        mSearch = isSearch;
        initAdapter(mCardViewInfo.getCardInfos(mFoldState));
        initDynamicCardView(context);
    }

    private void initDynamicCardView(Context context) {
        initFooterView();
        initHeaderView();
        this.setBackgroundResource(R.drawable.shape_custom_card_item);
    }
    /**
     * @author dongzq
     * created at 2017/7/19
     * init card view header according to card type
     */
    private void initHeaderView() {
        mHeaderView = LayoutInflater.from(mContext).inflate(R.layout.card_item_header, this, false);
        mHeaderText = (TextView) mHeaderView.findViewById(R.id.list_item_header_card_text);
        switch (mCardType) {
            case DynamicCardAdapter.RESERVATION_CARD:
                mHeaderView.setBackgroundResource(R.drawable.shape_reservation_card_item_header);
                mHeaderText.setText(R.string.reservation_card_title);
                break;
            case DynamicCardAdapter.MAINTENANCE_CARD:
                mHeaderView.setBackgroundResource(R.drawable.shape_maintenance_card_item_header);
                mHeaderText.setText(R.string.maintenance_card_title);
                break;
            case DynamicCardAdapter.HISTORY_CARD:
                mHeaderView.setBackgroundResource(R.drawable.shape_history_card_item_header);
                mHeaderText.setText(R.string.history_card_title);
                break;
            default:break;
        }
        mFoldBtn = (TextView) mHeaderView.findViewById(R.id.btn_card_header);
        mFoldBtn.setText(mFoldState ? R.string.expand_card_view : R.string.fold_card_view);
        mFoldBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mFoldState = !mFoldState;
                mFoldBtn.setText(mFoldState ? R.string.expand_card_view : R.string.fold_card_view);
                mAdapter.setFoldState(mFoldState);
                foldExpandCard();
            }
        });
        this.addHeaderView(mHeaderView);
    }

    private void initFooterView() {
        mFooterParent = LayoutInflater.from(mContext).inflate(R.layout.card_item_footer, this, false);
        mFooterView = mFooterParent.findViewById(R.id.mFooter);
        mFooterParent.setBackgroundResource(R.drawable.shape_card_item_footer);
        mCardBtn = (TextView) mFooterView.findViewById(R.id.btn_card_spread);
        switch (mCardType) {
            case DynamicCardAdapter.RESERVATION_CARD:
                mFooterView.setVisibility(VISIBLE);
                mCardBtn.setText(R.string.reservation_cancel);
                mCardBtn.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        if (mFooterListener != null) {
                            mFooterListener.onFooterClick(mKey, mCardType);
                        }
                    }
                });
                break;
            case DynamicCardAdapter.MAINTENANCE_CARD:
                mFooterView.setVisibility(VISIBLE);
                mCardBtn.setText(R.string.contact_customer_service);
                mCardBtn.setTextColor(mContext.getResources().getColor(R.color.gome_base_color_dark_blue));
                mCardBtn.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        Uri uri = Uri.parse("tel://400-898-8666");
                        intent.setData(uri);
                        mContext.startActivity(intent);
                    }
                });
                break;
            case DynamicCardAdapter.HISTORY_CARD:
                mFooterView.setVisibility(GONE);
                break;
            default:break;
        }
        this.addFooterView(mFooterParent);
    }

    public void setFooterViewVisible(boolean state) {
        mFooterView.setVisibility(state ? VISIBLE : GONE);
    }
    /**
     * @author dongzq
     * created at 2017/7/19
     * @param infos have the full card info, futher more adapterShowInfos only have the info which will show.
     * infos and adapterShowInfos should be different object (important)
     */
    private void initAdapter(List<String> infos) {
        // we have two list
        // adapterShowInfos is the source data of adapter and we can change it so the listview will cahnge
        // infos is the cardInfo, which have all infos, and we should keep it not changed.
        List<String> adapterShowInfos = new ArrayList<String>();
        adapterShowInfos.addAll(infos);
        DynamicCardAdapter adapter = new DynamicCardAdapter(mContext, mCardType, adapterShowInfos, mFoldState, mSearch);
        setAdapter(adapter);
        mAdapter = adapter;
    }

    protected void foldExpandCard() {
        List<String> infos = mAdapter.getInfos();
        infos.clear();
        infos.addAll(mCardViewInfo.getCardInfos(mFoldState));
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }

    public void setKey(String key) {
        mKey = key;
    }

    public String getKey() {
        return mKey;
    }

    public void setOnFooterClickListener(DynamicCardFooterClickListener listener) {
        mFooterListener = listener;
    }

    public interface DynamicCardFooterClickListener {
        void onFooterClick(String key, int type);
    }

}
