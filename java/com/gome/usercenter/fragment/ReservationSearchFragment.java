package com.gome.usercenter.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.gome.usercenter.R;
import com.gome.usercenter.activity.ReservationAndMaintenanceActivity;
import com.gome.usercenter.utils.ActivityUtils;
import com.gome.usercenter.utils.Constants;
import com.gome.usercenter.utils.NetworkUtils;
import com.gome.usercenter.utils.GMToastUtils;
import com.gome.usercenter.view.GomeSearchView;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * Created by dongzq on 2017/7/26.
 */

public class ReservationSearchFragment extends Fragment{

    private static final String TAG = Constants.TAG_HEADER + "ReservationSearchFragment";;
    private GomeSearchView mSearchView;
    private boolean mSearchMenuItemExpanded = false;
    private Handler mUIHandler;
    private Context mContext;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reservation_search, null);

        mSearchView = (GomeSearchView) view.findViewById(R.id.search);
        mSearchView.searchEditView.setInputType(InputType.TYPE_CLASS_PHONE);
        mSearchView.searchEditView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View arg0, int keyCode, KeyEvent keyEvent) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    ((InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
                            view.getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                    String searchText = mSearchView.searchEditView.getText().toString();
                    searchWithSearchInfo(searchText);
                }
                return false;
            }
        });

        mSearchView.searchEditView.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                mSearchView.searchTextClear.setVisibility(TextUtils.isEmpty(arg0) ? View.GONE : View.VISIBLE);
                if(TextUtils.isEmpty(arg0)){
                    Log.d(TAG, "search text is empty, refresh data");
                    Message msg = mUIHandler.obtainMessage();
                    msg.what = ReservationAndMaintenanceActivity.REFRESH_DATA_AFTER_SEARCH;
                    mUIHandler.sendMessage(msg);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
            }
        });

        Locale locale = getResources().getConfiguration().getLocales().get(0);
        String lang = locale.getLanguage();
        if(lang.equals("en")) {
            SpannableString s1 = new SpannableString(getResources().getString(R.string.search_hint));
            AbsoluteSizeSpan editTitleSize = new AbsoluteSizeSpan(getResources().getDimensionPixelOffset(R.dimen.gome_custom_text_size_13sp));
            s1.setSpan(editTitleSize, 0, s1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            mSearchView.searchEditView.setHint(s1);
        }

        String searchText = mSearchView.searchEditView.getText().toString();
        mSearchView.searchTextClear.setVisibility(TextUtils.isEmpty(searchText) ? View.GONE : View.VISIBLE);
        return view;
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = getActivity();
        if (context instanceof ReservationAndMaintenanceActivity) {
            mUIHandler = ((ReservationAndMaintenanceActivity) context).getUIHandler();
        }  else {
            Log.d(TAG, "attach on wrong activity");
        }
    }

    public void onResume() {
        super.onResume();
        mSearchView.clearFocus();
    }

    private void searchWithSearchInfo(String query){
        if (!ActivityUtils.isMobileNO(query)) {
            GMToastUtils.showShort(R.string.toast_input_infomation_correct_format);
            return;
        }
        try {
            String repairRequest = NetworkUtils.requestBuilder(new String[]{"gomeAccount", "tel", "imei"},
                    new String[]{"", query, ""});
            String reserveRequest = NetworkUtils.requestBuilder(new String[]{"gomeAccount", "tel", "imei"},
                    new String[]{"", query, ""});
            Message msg = mUIHandler.obtainMessage();
            msg.what = ReservationAndMaintenanceActivity.START_REQUEST_DATA_FROM_SERVER;
            Bundle bundle = new Bundle();
            bundle.putString("repair_request", repairRequest);
            bundle.putString("reserve_request", reserveRequest);
            msg.setData(bundle);
            mUIHandler.sendMessage(msg);
            mSearchView.clearFocus();
            return;
        } catch (JSONException e) {
            Log.d(TAG, "Build request faild caused by Exception: " + e);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
    public void clearSearchText() {
        mSearchView.searchEditView.setText("");
    }
}
