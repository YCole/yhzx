package com.gome.usercenter.activity;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;

import com.gome.usercenter.R;

/**
 * Created by dongzq on 2017/7/13.
 */

public class SubActivity extends BaseActivity {

    public static final String EXTRA_SHOW_FRAGMENT = "show_fragment";
    private static final String FRAGMENT_ARGS = "fragment_args";
    public static final String EXTRA_TITLE_RESID = "extra_title_resid";
    public static final String EXTRA_TITLE = "extra_title";
    private Fragment mFragment;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setBody(R.layout.subactivity_content);
        final Intent intent = getIntent();
        final String initialFragmentName = intent.getStringExtra(EXTRA_SHOW_FRAGMENT);
        final int initialTitleId = intent.getIntExtra(EXTRA_TITLE_RESID, -1);
        final String initialTitle = intent.getStringExtra(EXTRA_TITLE);
        final Bundle args = intent.getBundleExtra(FRAGMENT_ARGS);
        mFragment = switchToFragment(initialFragmentName, args, initialTitleId, initialTitle);
    }

    /**
     * Switch to a specific Fragment with Title
     */
    private Fragment switchToFragment(String fragmentName, Bundle args, int titleResId, CharSequence title) {

        Fragment f = Fragment.instantiate(this, fragmentName, args);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.content, f);
        if (titleResId > 0) {
            setCustomTitle(titleResId);
        } else if (title != null) {
            setCustomTitle((String) title);
        }
        transaction.commit();
        return f;
    }

    public void setSubActivityTitle(int id) {
        mCustomTitle.setText(id);
    }

}
