package com.gome.usercenter.utils;

import android.content.Context;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import com.gome.usercenter.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dongzq on 2017/6/30.
 */

public class LinkifyUtils {
    private static final String PLACE_HOLDER_LINK_BEGIN = "LINK_BEGIN";
    private static final String PLACE_HOLDER_LINK_END = "LINK_END";

    private LinkifyUtils() {
    }

    /** Interface that handles the click event of the link */
    public interface OnClickListener {
        void onClick(int id);
    }

    /**
     * Applies the text into the {@link TextView} and part of it a clickable link.
     * The text surrounded with "LINK_BEGIN" and "LINK_END" will become a clickable link. Only
     * supports at most one link.
     * @return true if the link has been successfully applied, or false if the original text
     *         contains no link place holders.
     */
    // modified by zhiqiang.dong@gometech.com.cn, 20171025
    public static boolean linkify(Context context, Spannable spannableContent, final int beginIndex, final int endIndex,
                                  final OnClickListener listener, final int id) {
        ClickableSpan spannableLink = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                listener.onClick(id);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
                ds.setColor(context.getResources().getColor(R.color.gome_base_color_dark_blue));
            }
        };
        spannableContent.setSpan(spannableLink, beginIndex, endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return true;
    }

    // modified by zhiqiang.dong@gometech.com.cn, 20171025
    public static boolean linkify(Context context, TextView textView, StringBuilder text, final OnClickListener listener) {
        List<Map<String, Object>> indexList = new ArrayList<>();
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            // Remove place-holders from the string and record their positions
            final int beginIndex = text.indexOf(PLACE_HOLDER_LINK_BEGIN);
            if (beginIndex == -1) {
                break;
            }
            text.delete(beginIndex, beginIndex + PLACE_HOLDER_LINK_BEGIN.length());
            final int endIndex = text.indexOf(PLACE_HOLDER_LINK_END);
            if (endIndex == -1) {
                break;
            }
            text.delete(endIndex, endIndex + PLACE_HOLDER_LINK_END.length());
            HashMap<String, Object> map = new HashMap<>();
            map.put("begin", beginIndex);
            map.put("end", endIndex);
            map.put("id", i);
            indexList.add(map);
        }

        textView.setText(text.toString(), TextView.BufferType.SPANNABLE);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        Spannable spannableContent = (Spannable) textView.getText();

        for (int i = 0; i < indexList.size(); i++) {
            // modified by zhiqiang.dong@gometech.com.cn, 20171025
            linkify(context, spannableContent, (Integer) indexList.get(i).get("begin"),
                     (Integer) indexList.get(i).get("end"), listener, (Integer) indexList.get(i).get("id"));
        }
        return true;
    }
}
