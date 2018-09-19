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
import com.gome.usercenter.view.citypicker.wheel.OnWheelChangedListener;
import com.gome.usercenter.view.citypicker.wheel.WheelView;
import com.gome.usercenter.view.citypicker.wheel.adapters.ArrayWheelAdapter;

import android.graphics.drawable.BitmapDrawable;
import com.gome.usercenter.utils.ActivityUtils;
/**
 * Created by jianfeng.xue on 2017/8/1.
 */

public class ModulePicker implements CanShow, OnWheelChangedListener {

    private Context context;

    private PopupWindow popwindow;

    private View popview;

    private WheelView mViewModule;

    private Button mBtnOK;

    private Button mBtnCancel;

    private RelativeLayout rll;

    /**
     * 所有模块
     */
    protected String[] mModuleDatas;

    /**
     * 当前模块的名称
     */
    protected String mCurrentModuleName;

    private ModulePicker.OnModuleItemClickListener listener;

    public interface OnModuleItemClickListener {
        void onSelected(String moduleSelected);
        void onCancel();
    }

    public void setOnModuleItemClickListener(ModulePicker.OnModuleItemClickListener listener) {
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
     * 滚轮是否循环滚动
     */
    private boolean isModuleCyclic = false;

    /**
     * item间距
     */
    private int padding = 5;

    /**
     * 第一次默认的模块
     */
    private String defaultModuleName = "";

    /**
     * 标题
     */
    private String mTitle = "";

    /**
     * 设置popwindow的背景
     */
    private int backgroundPop = 0x66000000;

    private ModulePicker(ModulePicker.Builder builder) {
        this.textColor = builder.textColor;
        this.textSize = builder.textSize;
        this.visibleItems = builder.visibleItems;
        this.isModuleCyclic = builder.isModuleCyclic;
        this.context = builder.mContext;
        this.padding = builder.padding;
        this.mTitle = builder.mTitle;

        this.defaultModuleName = builder.defaultModuleName;

        this.backgroundPop = builder.backgroundPop;

        LayoutInflater layoutInflater = LayoutInflater.from(context);
        popview = layoutInflater.inflate(R.layout.pop_modulepicker, null);

        mViewModule = (WheelView) popview.findViewById(R.id.id_module);

        mBtnOK = (Button) popview.findViewById(R.id.button_positive);
        mBtnCancel = (Button) popview.findViewById(R.id.button_negative);

        rll = (RelativeLayout) popview.findViewById(R.id.ll_root);
// bugfix GMOS-9487 start
        popwindow = new PopupWindow(popview, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
//        popwindow.setBackgroundDrawable(new ColorDrawable(backgroundPop));
// bugfix GMOS-9487 end
//        popwindow.setAnimationStyle(R.style.AnimBottom);
        popwindow.setTouchable(true);
        popwindow.setOutsideTouchable(false);
        popwindow.setFocusable(true);
        View titleBackground = popview.findViewById(R.id.ll_title_background);
        Rect rect = new Rect();
        popwindow.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    titleBackground.getGlobalVisibleRect(rect);
                    int x = (int) event.getX();
                    int y = (int) event.getY();
                    if (!rect.contains(x, y)) {
                        popwindow.dismiss();
                        return true;
                    }
                }
                return false;
            }
        });
        // bugfix GMOS-9487 start
        popwindow.setBackgroundDrawable(new BitmapDrawable());
        ActivityUtils.setBackgroundAlpha(context, 0.45f);
        ActivityUtils.setNavigationBarColor(context, true);
        popwindow.setOnDismissListener(new poponDismissListener());
        // bugfix GMOS-9487 end

        //初始化城市数据
        initModuleDatas(context);

        // 添加change事件
        mViewModule.addChangingListener(this);

        mBtnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onSelected(mCurrentModuleName);
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
         * 省滚轮是否循环滚动
         */
        private boolean isModuleCyclic = false;

        private Context mContext;

        /**
         * item间距
         */
        private int padding = 5;


        /**
         * 第一次默认的显示模块
         */
        private String defaultModuleName = "";

        /**
         * 标题
         */
        private String mTitle = "";

        /**
         * 设置popwindow的背景
         */
        private int backgroundPop = 0x8c000000;

        public Builder(Context context) {
            this.mContext = context;
            this.defaultModuleName = context.getResources().getString(R.string.string_module_crash);
            this.mTitle = context.getResources().getString(R.string.choose_module);
        }

        /**
         * 设置标题
         *
         * @param mtitle
         * @return
         */
        public ModulePicker.Builder title(String mtitle) {
            this.mTitle = mtitle;
            return this;
        }

        /**
         * 第一次默认的显示模块
         *
         * @param defaultModuleName
         * @return
         */
        public ModulePicker.Builder module(String defaultModuleName) {
            this.defaultModuleName = defaultModuleName;
            return this;
        }

        /**
         * item文字颜色
         *
         * @param textColor
         * @return
         */
        public ModulePicker.Builder textColor(int textColor) {
            this.textColor = textColor;
            return this;
        }

        /**
         * item文字大小
         *
         * @param textSize
         * @return
         */
        public ModulePicker.Builder textSize(int textSize) {
            this.textSize = textSize;
            return this;
        }

        /**
         * item间距
         *
         * @param itemPadding
         * @return
         */
        public ModulePicker.Builder itemPadding(int itemPadding) {
            this.padding = itemPadding;
            return this;
        }

        public ModulePicker build() {
            ModulePicker ModulePicker = new ModulePicker(this);
            return ModulePicker;
        }

    }

    private void setUpData() {
        int moduleDefault = -1;
        if (!TextUtils.isEmpty(defaultModuleName) && mModuleDatas.length > 0) {
            for (int i = 0; i < mModuleDatas.length; i++) {
                if (mModuleDatas[i].contains(defaultModuleName)) {
                    moduleDefault = i;
                    break;
                }
            }
        }
        ArrayWheelAdapter arrayWheelAdapter = new ArrayWheelAdapter<String>(context, mModuleDatas);
        mViewModule.setViewAdapter(arrayWheelAdapter);
        if (-1 != moduleDefault) {
            mViewModule.setCurrentItem(moduleDefault);
        }
        // 设置可见条目数量
        mViewModule.setVisibleItems(visibleItems);
        mViewModule.setCyclic(isModuleCyclic);
        arrayWheelAdapter.setPadding(padding);
        arrayWheelAdapter.setTextColor(textColor);
        arrayWheelAdapter.setTextSize(textSize);
    }

    /**
     * 初始化模块信息
     */

    protected void initModuleDatas(Context context) {
        mModuleDatas = context.getResources().getStringArray(R.array.feedbacks_module);
    }

    @Override
    public void setType(int type) {
    }

    @Override
    public void show() {
        if (!isShow()) {
            setUpData();
            popwindow.showAtLocation(popview, Gravity.BOTTOM, 0, 0);
        }
    }

    @Override
    public void hide() {
        if (isShow()) {
            popwindow.dismiss();
        }
    }

    @Override
    public boolean isShow() {
        return popwindow.isShowing();
    }

    @Override
    public void onChanged(WheelView wheel, int oldValue, int newValue) {
        // TODO Auto-generated method stub
        if (wheel == mViewModule) {
            updateModule();
        }
    }

    private void updateModule(){
        int pCurrent = mViewModule.getCurrentItem();
        mCurrentModuleName = mModuleDatas[pCurrent];
    }

    // bugfix GMOS-9487 start
    class poponDismissListener implements PopupWindow.OnDismissListener{
        @Override
        public void onDismiss() {
            // TODO Auto-generated method stub
            ActivityUtils.setBackgroundAlpha(context, 1f);
            ActivityUtils.setNavigationBarColor(context, false);
        }
    }
    // bugfix GMOS-9487 end
}

