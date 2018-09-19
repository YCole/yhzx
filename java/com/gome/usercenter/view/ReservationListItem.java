package com.gome.usercenter.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gome.usercenter.R;
import com.gome.usercenter.utils.ActivityUtils;

/**
 * Created by dongzq on 2017/7/10.
 */

public class ReservationListItem extends RelativeLayout {
    private TextView mTitle;
    private TextView mSummary;
    private ImageView mMore;
    private EditText mEdit;
    private String mTitleText;
    private Context mContext;


    public ReservationListItem(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context, attrs);
    }

    public ReservationListItem(Context context) {
        this(context, null);
    }

    public ReservationListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        View view = View.inflate(context, R.layout.view_reservation_listitem, this);
        mContext = context;
        mTitle = (TextView) findViewById(R.id.title);
        mSummary = (TextView) findViewById(R.id.summary);
        mMore = (ImageView) findViewById(R.id.more);
        mEdit = (EditText) findViewById(R.id.edit);
        mEdit.setTextAppearance(R.style.singlelist_summary_high_light_appearance);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ReservationListItem);
        mTitle.setText(ta.getText(R.styleable.ReservationListItem_title));
        mSummary.setText(ta.getText(R.styleable.ReservationListItem_summary));
        mMore.setVisibility(ta.getBoolean(R.styleable.ReservationListItem_show_more, true) ? VISIBLE : GONE);
//        enableHighLight();
    }

    public void setSummary(int resId) {
        mSummary.setText(resId);
        mSummary.setTextAppearance(R.style.singlelist_summary_high_light_appearance);
    }

    public void setSummary(String summary) {
        mSummary.setText(summary);
        mSummary.setTextAppearance(R.style.singlelist_summary_high_light_appearance);
    }

    public void setDefaultSummary(int resId) {
        mSummary.setText(resId);
        mSummary.setTextAppearance(R.style.singlelist_summary_appearance);
    }

    public void setDefaultSummary(String summary) {
        mSummary.setText(summary);
        mSummary.setTextAppearance(R.style.singlelist_summary_appearance);
    }

    public void setErrorSummary(int resId) {
        if (mEdit.getVisibility() == VISIBLE) {
            mEdit.setHint(resId);
            mEdit.setHintTextColor(getResources().getColor(R.color.gome_base_color_red));
        } else {
            mSummary.setText(resId);
            mSummary.setTextAppearance(R.style.singlelist_summary_error_appearance);
        }
    }

    public void setErrorSummary(String summary) {
        if (mEdit.getVisibility() == VISIBLE) {
            mEdit.setHint(summary);
            mEdit.setHintTextColor(getResources().getColor(R.color.gome_base_color_red));
        } else {
            mSummary.setText(summary);
            mSummary.setTextAppearance(R.style.singlelist_summary_error_appearance);
        }
    }

    public void setEditVisible(boolean visible) {
        mEdit.setVisibility(visible ? View.VISIBLE : View.GONE);
        mSummary.setVisibility(visible ? View.GONE : View.VISIBLE);
    }
    public void setSummaryVisible(boolean visible) {
        mSummary.setVisibility(visible ? View.VISIBLE : View.GONE);
        mEdit.setVisibility(visible ? View.GONE : View.VISIBLE);
    }

    public void setMoreIconInvisible() {
        mMore.setVisibility(View.GONE);
    }

    public void setEditHint(String hint) {
        mEdit.setHint(hint);
    }

    private void initView(Context context) {
        View.inflate(context, R.layout.view_reservation_listitem, this);
    }

    public EditText getEdit() {
        return mEdit;
    }

    public String getSummary() {
        if (mEdit.getVisibility() == VISIBLE) {
            return ActivityUtils.trimSpace(mEdit.getText().toString());
        } else {
            return mSummary.getText().toString();
        }
    }

    public void setSummaryColor(int res) {
        if (mEdit.getVisibility() == VISIBLE) {
            mEdit.setHintTextColor(res);
        } else {
            mSummary.setTextColor(res);
        }
    }

    public void enableHighLight() {
        mTitleText = (String) mTitle.getText();
        StringBuilder title = new StringBuilder();
        title.append(mTitleText).append("*");
        SpannableStringBuilder ssb = new SpannableStringBuilder(title.toString());
        ssb.setSpan(new TextAppearanceSpan(mContext, R.style.edit_text_highlight_appearance), title.toString().length() - 1, title.toString().length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        mTitle.setText(ssb);
    }

    public void disableHighLight() {
        mTitle.setText(mTitleText);
    }
    public String getTitle() {
        return mTitle.getText().toString();
    }

    public void setEditDefaultText(String text) {
        mEdit.setText(text);
        int index = TextUtils.isEmpty(text) ? 0 : text.length() - 1;
        mEdit.setSelection(index);
    }
}
