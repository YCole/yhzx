package com.gome.usercenter.view.citypicker;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.gome.usercenter.R;
import com.gome.usercenter.utils.Constants;
import com.gome.usercenter.view.citypicker.wheel.OnWheelChangedListener;
import com.gome.usercenter.view.citypicker.wheel.WheelView;
import com.gome.usercenter.view.citypicker.wheel.adapters.ArrayWheelAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import android.util.Log;

import android.graphics.drawable.BitmapDrawable;
import com.gome.usercenter.utils.ActivityUtils;
/**
 * Created by dongzq on 2017/7/14.
 */

public class TimePicker implements CanShow, OnWheelChangedListener {
    private final String TAG = Constants.TAG_HEADER + "TimePicker";
    private Context mContext;

    private PopupWindow mPopwindow;

    private View mPopview;

    private Button mBtnOK;

    private Button mBtnCancel;

    private final WheelView mViewDate;

    private final WheelView mViewTime;

    private String mStartYear;

    /**
     * Current year
     */
    protected String mCurrentYear;

    /**
     * Current year and month selected
     */
    protected String mCurrentDate;

    /**
     * Current time select
     */
    protected String mCurrentTime;

    private OnTimeItemClickListener listener;
    private String[] mDateDatas;
    private String[] mTimeDatas;

    private int YEAR_FLAG = -1;
    public interface OnTimeItemClickListener {
        void onSelected(String... selected);
        void onCancel();
    }

    public void setOnTimeItemClickListener(TimePicker.OnTimeItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * Default text color
     */
    public static final int DEFAULT_TEXT_COLOR = 0xFF585858;

    /**
     * Default text size
     */
    public static final int DEFAULT_TEXT_SIZE = 15;

    // Text settings
    private int textColor = DEFAULT_TEXT_COLOR;

    private int textSize = DEFAULT_TEXT_SIZE;

    /**
     * 滚轮显示的item个数
     */
    private static final int DEF_VISIBLE_ITEMS = 3;

    // Count of visible items
    private int visibleItems = DEF_VISIBLE_ITEMS;

    /**
     * 日期滚轮是否循环滚动
     */
    private boolean isDateCyclic = false;

    /**
     * 预约时间滚轮是否循环滚动
     */
    private boolean isTimeCyclic = false;

    /**
     * item间距
     */
    private int padding = 5;

    /**
     * 默认日期显示
     */
    private String defaultDate = "01月01日";

    /**
     * 默认预约时间
     */
    private String defaultTime = "00:00-04:00";

    /**
     * 默认标题
     */
    private String mTitle = "选择时间";

    /**
     * 设置popwindow的背景
     */
    private int backgroundPop = 0x66000000;

    private RelativeLayout rll;

    private TimePicker(TimePicker.Builder builder) {
        this.textColor = builder.textColor;
        this.textSize = builder.textSize;
        this.visibleItems = builder.visibleItems;
        this.isDateCyclic = builder.isDateCyclic;
        this.isTimeCyclic = builder.isTimeCyclic;
        this.mContext = builder.mContext;
        this.padding = builder.padding;
        this.mTitle = builder.mTitle;

        this.defaultDate = builder.defaultDate;
        this.defaultTime = builder.defaultTime;

        this.backgroundPop = builder.backgroundPop;

        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        mPopview = layoutInflater.inflate(R.layout.pop_timepicker, null);

        mViewDate = (WheelView) mPopview.findViewById(R.id.id_date);
        mViewTime = (WheelView) mPopview.findViewById(R.id.id_time);

        mBtnOK = (Button) mPopview.findViewById(R.id.button_positive);
        mBtnCancel = (Button) mPopview.findViewById(R.id.button_negative);

        rll = (RelativeLayout) mPopview.findViewById(R.id.ll_root);

//bugfix GMOS-9487 start
        mPopwindow = new PopupWindow(mPopview, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
//        popwindow.setBackgroundDrawable(new ColorDrawable(backgroundPop));
// bugfix GMOS-9487 end
        mPopwindow.setTouchable(true);
        mPopwindow.setOutsideTouchable(false);
        mPopwindow.setFocusable(true);
        View titleBackground = mPopview.findViewById(R.id.ll_title_background);
        Rect rect = new Rect();
        mPopwindow.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    titleBackground.getGlobalVisibleRect(rect);
                    int x = (int) event.getX();
                    int y = (int) event.getY();
                    if (!rect.contains(x, y)) {
                        mPopwindow.dismiss();
                        return true;
                    }
                }
                return false;
            }
        });
        //bugfix GMOS-9487 start
        mPopwindow.setBackgroundDrawable(new BitmapDrawable());
        ActivityUtils.setBackgroundAlpha(mContext, 0.45f);
        ActivityUtils.setNavigationBarColor(mContext, true);
        mPopwindow.setOnDismissListener(new poponDismissListener());
        // bugfix GMOS-9487 end
        //初始化日期和预约时间数据
        mTimeDatas = mContext.getResources().getStringArray(R.array.reserve_time);
        initDatas(mContext);

        // 添加change事件
        mViewDate.addChangingListener(this);
        mViewTime.addChangingListener(this);

        mBtnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onSelected(mCurrentYear + "年", mCurrentDate, mCurrentTime);
                hide();
            }
        });

        mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onCancel();
                hide();
            }
        });
    }

    private void initDatas(Context mContext) {

        List<String> dateList = new ArrayList<String>();
        Calendar c = Calendar.getInstance();
        int yearNow = c.get(Calendar.YEAR);
        int mounthNow = c.get(Calendar.MONTH);
        int dayNow = c.get(Calendar.DAY_OF_MONTH);

        c.set(Calendar.DAY_OF_MONTH, dayNow + 3);

        int yearAfter = c.get(Calendar.YEAR);
        int mounthAfter = c.get(Calendar.MONTH);
        int dayAfter = c.get(Calendar.DAY_OF_MONTH);
        mCurrentYear = yearAfter + "";
        mStartYear = mCurrentYear;
        if (yearAfter > yearNow) {
            dateList.add(yearAfter + "年");
            for (int j = dayAfter; j <= getDaysByYearMonth(yearAfter, mounthAfter + 1); j++) {
                StringBuilder builder = new StringBuilder();
                if (mounthAfter + 1 < 10) {
                    builder.append("0").append(mounthAfter + 1);
                } else {
                    builder.append(mounthAfter + 1);
                }
                builder.append("月");
                if (j < 10) {
                    builder.append("0").append(j);
                } else {
                    builder.append(j);
                }
                builder.append("日");
                dateList.add(builder.toString());
            }

            for (int i = mounthAfter + 2; i < 13; i++) {
                for (int j = 1; j <= getDaysByYearMonth(yearAfter, i); j++) {
                    StringBuilder builder = new StringBuilder();
                    if (i < 10) {
                        builder.append("0").append(i);
                    } else {
                        builder.append(i);
                    }
                    builder.append("月");
                    if (j < 10) {
                        builder.append("0").append(j);
                    } else {
                        builder.append(j);
                    }
                    builder.append("日");
                    dateList.add(builder.toString());
                }
            }
        } else {
            for (int j = dayAfter; j <= getDaysByMonth(mounthAfter + 1); j++) {
                StringBuilder builder = new StringBuilder();
                if (mounthAfter + 1 < 10) {
                    builder.append("0").append(mounthAfter + 1);
                } else {
                    builder.append(mounthAfter + 1);
                }
                builder.append("月");
                if (j < 10) {
                    builder.append("0").append(j);
                } else {
                    builder.append(j);
                }
                builder.append("日");
                dateList.add(builder.toString());
            }

            for (int i = mounthAfter + 2; i < 13; i++) {
                for (int j = 1; j <= getDaysByMonth(i); j++) {
                    StringBuilder builder = new StringBuilder();
                    if (i < 10) {
                        builder.append("0").append(i);
                    } else {
                        builder.append(i);
                    }
                    builder.append("月");
                    if (j < 10) {
                        builder.append("0").append(j);
                    } else {
                        builder.append(j);
                    }
                    builder.append("日");
                    dateList.add(builder.toString());
                }
            }
            YEAR_FLAG = dateList.size();
            dateList.add((yearAfter + 1) + "年");
            for (int i = 1; i < 13; i++) {
                for (int j = 1; j <= getDaysByYearMonth(yearAfter + 1, i); j++) {
                    StringBuilder builder = new StringBuilder();
                    if (i < 10) {
                        builder.append("0").append(i);
                    } else {
                        builder.append(i);
                    }
                    builder.append("月");
                    if (j < 10) {
                        builder.append("0").append(j);
                    } else {
                        builder.append(j);
                    }
                    builder.append("日");
                    dateList.add(builder.toString());
                }
            }
        }
        mDateDatas = new String[dateList.size()];
        mDateDatas = (String[]) dateList.toArray(mDateDatas);
        mCurrentDate = mDateDatas[0];
    }

    /**
     * 根据年 月 获取对应的月份 天数
     * */
    public static int getDaysByYearMonth(int year, int month) {

        Calendar a = Calendar.getInstance();
        a.set(Calendar.YEAR, year);
        a.set(Calendar.MONTH, month - 1);
        a.set(Calendar.DATE, 1);
        a.roll(Calendar.DATE, -1);
        int maxDate = a.get(Calendar.DATE);
        return maxDate;
    }

    /**
     * 根据 月 获取本年对应的月份 天数
     * */
    public static int getDaysByMonth(int month) {

        Calendar a = Calendar.getInstance();
        a.set(Calendar.MONTH, month - 1);
        a.set(Calendar.DATE, 1);
        a.roll(Calendar.DATE, -1);
        int maxDate = a.get(Calendar.DATE);
        return maxDate;
    }

    public static class Builder {
        /**
         * Default text color
         */
        public static final int DEFAULT_TEXT_COLOR = 0xE6000000;

        /**
         * Default text size
         */
        public static final int DEFAULT_TEXT_SIZE = 15;

        // Text settings
        private int textColor = DEFAULT_TEXT_COLOR;

        private int textSize = DEFAULT_TEXT_SIZE;

        /**
         * 滚轮显示的item个数
         */
        private static final int DEF_VISIBLE_ITEMS = 3;

        // Count of visible items
        private int visibleItems = DEF_VISIBLE_ITEMS;

        /**
         * 日期滚轮是否循环滚动
         */
        private boolean isDateCyclic = false;

        /**
         * 预约时间滚轮是否循环滚动
         */
        private boolean isTimeCyclic = false;

        private Context mContext;

        /**
         * item间距
         */
        private int padding = 5;


        /**
         * 默认显示日期
         */
        private String defaultDate = "01月01日";

        /**
         * 默认显示预约时间
         */
        private String defaultTime = "00:00-04:00";

        /**
         * 标题
         */
        private String mTitle = "选择时间";

        /**
         * 设置popwindow的背景
         */
        private int backgroundPop = 0x8c000000;

        public Builder(Context context) {
            this.mContext = context;
        }

        /**
         * 设置标题
         *
         * @param mtitle
         * @return
         */
        public Builder title(String mtitle) {
            this.mTitle = mtitle;
            return this;
        }

        /**
         * 默认显示日期
         *
         * @param defaultDate
         * @return
         */
        public Builder Date(String defaultDate) {
            this.defaultDate = defaultDate;
            return this;
        }

        /**
         * 默认显示预约时间
         *
         * @param defaultTime
         * @return
         */
        public Builder Time(String defaultTime) {
            this.defaultTime = defaultTime;
            return this;
        }

        /**
         * item文字颜色
         *
         * @param textColor
         * @return
         */
        public Builder textColor(int textColor) {
            this.textColor = textColor;
            return this;
        }

        /**
         * item文字大小
         *
         * @param textSize
         * @return
         */
        public Builder textSize(int textSize) {
            this.textSize = textSize;
            return this;
        }

        /**
         * item间距
         *
         * @param itemPadding
         * @return
         */
        public Builder itemPadding(int itemPadding) {
            this.padding = itemPadding;
            return this;
        }

        public TimePicker build() {
            TimePicker cityPicker = new TimePicker(this);
            return cityPicker;
        }

    }

    private void setUpData() {
        int dateDefault = -1;
        if (!TextUtils.isEmpty(defaultDate) && mDateDatas.length > 0) {
            for (int i = 0; i < mDateDatas.length; i++) {
                if (mDateDatas[i].contains(defaultDate)) {
                    dateDefault = i;
                    break;
                }
            }
        }
        ArrayWheelAdapter arrayWheelAdapter = new ArrayWheelAdapter<String>(mContext, mDateDatas);
        mViewDate.setViewAdapter(arrayWheelAdapter);
        if (-1 != dateDefault) {
            mViewDate.setCurrentItem(dateDefault);
        }
        // 设置可见条目数量
        mViewDate.setVisibleItems(visibleItems);
        mViewTime.setVisibleItems(visibleItems);
        mViewDate.setCyclic(isDateCyclic);
        mViewTime.setCyclic(isTimeCyclic);
        arrayWheelAdapter.setPadding(padding);
        arrayWheelAdapter.setTextColor(textColor);
        arrayWheelAdapter.setTextSize(textSize);
        if (mCurrentDate.endsWith("年")) {
            mViewDate.setCurrentItem(1, false);
        }
        updateTime();
    }

    /**
     * 更新预约时间WheelView的信息
     */
    private void updateTime() {
        int timeDefault = -1;
        if (!TextUtils.isEmpty(defaultTime) && mTimeDatas.length > 0) {
            for (int i = 0; i < mTimeDatas.length; i++) {
                if (mTimeDatas[i].contains(defaultTime)) {
                    timeDefault = i;
                    break;
                }
            }
        }

        ArrayWheelAdapter timeWheel = new ArrayWheelAdapter<String>(mContext, mTimeDatas);
        // 设置可见条目数量
        timeWheel.setTextColor(textColor);
        timeWheel.setTextSize(textSize);
        mViewTime.setViewAdapter(timeWheel);
        if (-1 != timeDefault) {
            mViewTime.setCurrentItem(timeDefault);
        } else {
            mViewTime.setCurrentItem(0);
        }

        timeWheel.setPadding(padding);
        mCurrentTime = mTimeDatas[0];
    }

    public void setType(int var1) {

    }

    public void show() {
        setUpData();
        mPopwindow.showAtLocation(mPopview, Gravity.BOTTOM, 0, 0);
    }

    public void hide() {
        if (isShow()) {
            mPopwindow.dismiss();
        }
    }

    public boolean isShow() {
        return mPopwindow.isShowing();
    }

    public void onChanged(WheelView wheel, int oldValue, int newValue) {
        if (wheel == mViewDate) {
            updateTime();
            mCurrentDate = mDateDatas[newValue];
            //fix PRODUCTION-351 start
            if(YEAR_FLAG != -1){
                if(newValue == YEAR_FLAG){
                    // modified-begin by zhiqiang.dong, bugfix GMOS-4273
                    if(newValue == 0){
                        wheel.setCurrentItem(1, false);
                        mCurrentDate = mDateDatas[1];
                    }else if(newValue > 0){
                        int direction = newValue - oldValue;
                        int increasement = direction > 0 ? 1 : -1;
                        wheel.setCurrentItem(newValue + increasement, false);
                        mCurrentYear = increasement == 1 ? String.valueOf(Integer.parseInt(mStartYear) + 1) : mStartYear;
                        mCurrentDate = mDateDatas[newValue + increasement];
                    }
                    // modified-end
                }else if(newValue > YEAR_FLAG){
                    mCurrentYear =  String.valueOf(Integer.parseInt(mStartYear) + 1);
                }else if(newValue < YEAR_FLAG){
                    mCurrentYear = mStartYear;
                }
            }
            //fix PRODUCTION-351 end
        } else if (wheel == mViewTime) {
            mCurrentTime = mTimeDatas[newValue];
        }
    }

    //bugfix GMOS-9487 start
    class poponDismissListener implements PopupWindow.OnDismissListener{
        @Override
        public void onDismiss() {
            // TODO Auto-generated method stub
            ActivityUtils.setBackgroundAlpha(mContext, 1f);
            ActivityUtils.setNavigationBarColor(mContext, false);
        }
    }
    // bugfix GMOS-9487 end
}
