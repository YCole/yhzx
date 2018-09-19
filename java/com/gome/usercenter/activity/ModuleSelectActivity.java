package com.gome.usercenter.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gome.usercenter.R;
import com.gome.usercenter.utils.Constants;

import gome.widget.GomeListView;

/**
 * Created by jianfeng.xue on 2017/8/1.
 */

public class ModuleSelectActivity extends BaseActivity {
    private final String TAG = Constants.TAG_HEADER + "ModuleSelectActivity";
    private GomeListView mListView;
    private Map<Integer, Boolean> isSelected;

    private List beSelectedData = new ArrayList();

    ListAdapter adapter;

    private List moduleListArray = null;
    private String[] moduleArray;
    private Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setBody(R.layout.activity_module_select);
        setCustomCentralTitle(getResources().getString(R.string.choose_module));

        mContext = this;

        mListView = (GomeListView) this.findViewById(R.id.list);
        moduleArray = getResources().getStringArray(R.array.feedbacks_module);
        moduleListArray = new ArrayList();
        for(int i=0;i<moduleArray.length;i++){
            moduleListArray.add(moduleArray[i]);
        }
        initList();

    }

    void initList(){
        if (moduleListArray == null || moduleListArray.size() == 0)
            return;
        if (isSelected != null)
            isSelected = null;
        isSelected = new HashMap<Integer, Boolean>();
        for (int i = 0; i < moduleListArray.size(); i++) {
            isSelected.put(i, false);
        }
        if (beSelectedData.size() > 0) {
            beSelectedData.clear();
        }
        adapter = new ListAdapter(this, moduleListArray);
        mListView.setAdapter(adapter);
        mListView.setChoiceMode(GomeListView.CHOICE_MODE_SINGLE);
        adapter.notifyDataSetChanged();
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Log.d(TAG, moduleListArray.get(position).toString());
                Intent intent = new Intent();
                intent.putExtra("module", moduleListArray.get(position).toString());
                setResult(RESULT_OK, intent);
            }
        });

    }

    class ListAdapter extends BaseAdapter {

        private Context context;
        private List cs;
        private LayoutInflater inflater;

        public ListAdapter(Context context, List data) {
            this.context = context;
            this.cs = data;
            initLayoutInflater();
        }

        void initLayoutInflater() {
            inflater = LayoutInflater.from(context);
        }

        public int getCount() {
            return cs.size();
        }

        public Object getItem(int position) {
            return cs.get(position);
        }

        public long getItemId(int position) {
            return 0;
        }

        public View getView(int position1, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            View view = null;
            final int position = position1;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.module_select_adapter,
                        null);
                holder = new ViewHolder();
                holder.checkBox = (CheckBox) convertView
                        .findViewById(R.id.item_cb_section);
                holder.module = (TextView) convertView
                        .findViewById(R.id.textview_module);
                convertView.setTag(holder);
            } else {
                view = convertView;
                holder = (ViewHolder) view.getTag();
            }
            holder.checkBox.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    boolean cu = !isSelected.get(position);
                    for(Integer p : isSelected.keySet()) {
                        isSelected.put(p, false);
                    }
                    isSelected.put(position, cu);
                    ListAdapter.this.notifyDataSetChanged();
                    beSelectedData.clear();
                    if(cu) beSelectedData.add(cs.get(position));
                }
            });
            holder.module.setText(moduleArray[position]);
            holder.checkBox.setChecked(isSelected.get(position));
            return convertView;
        }
    }

    class ViewHolder {
        CheckBox checkBox;
        TextView module;
    }
}