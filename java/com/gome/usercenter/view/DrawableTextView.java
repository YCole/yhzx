package com.gome.usercenter.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.TextView;

import com.gome.usercenter.R;

/**
 * Created by dongzq on 2017/8/3.
 */

public class DrawableTextView extends TextView {

    private int mLeftDrawableWidth;
    private int mLeftDrawableHeight;

    public DrawableTextView(Context context) {
        this(context, null);
    }

    public DrawableTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0, 0);
    }

    public DrawableTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.DrawableTextView);
        mLeftDrawableWidth = ta.getDimensionPixelSize(R.styleable.DrawableTextView_drawableLeftWidth,
                context.getResources().getDimensionPixelSize(R.dimen.gome_list_tiny_icon_size));
        mLeftDrawableHeight = ta.getDimensionPixelSize(R.styleable.DrawableTextView_drawableLeftHeight,
                context.getResources().getDimensionPixelSize(R.dimen.gome_list_tiny_icon_size));
        ta.recycle();
        final Resources.Theme theme = context.getTheme();
        TypedArray a = theme.obtainStyledAttributes(
                attrs, com.android.internal.R.styleable.TextView, defStyleAttr, defStyleRes);
        Drawable drawableLeft = a.getDrawable(com.android.internal.R.styleable.TextView_drawableLeft);
        setCompoundDrawablesWithIntrinsicBounds(drawableLeft, null, null, null);
        a.recycle();
    }

    public void setCompoundDrawablesWithIntrinsicBounds(Drawable left, Drawable top, Drawable right, Drawable bottom) {

        if (left != null) {
            left.setBounds(0, 0, mLeftDrawableWidth, mLeftDrawableHeight);
        }
        if (right != null) {
            right.setBounds(0, 0, right.getIntrinsicWidth(), right.getIntrinsicHeight());
        }
        if (top != null) {
            top.setBounds(0, 0, top.getIntrinsicWidth(), top.getIntrinsicHeight());
        }
        if (bottom != null) {
            bottom.setBounds(0, 0, bottom.getIntrinsicWidth(), bottom.getIntrinsicHeight());
        }
        setCompoundDrawables(left, top, right, bottom);
    }
}
