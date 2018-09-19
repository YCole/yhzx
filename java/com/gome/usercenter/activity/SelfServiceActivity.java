package com.gome.usercenter.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.gome.usercenter.R;
import com.gome.usercenter.utils.Constants;
import com.gome.usercenter.view.FlowLayoutView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelfServiceActivity extends BaseActivity {

    private static final String TAG = Constants.TAG_HEADER + "SelfServiceActivity";
/*
    private GridView mOptimizationGrid;
    private GridView mSecurityGrid;
    private GridView mManagerGrid;
*/
    private List<Map<String, Object>> mOptimizationServices;
    private List<Map<String, Object>> mSecurityServices;
    private List<Map<String, Object>> mManagerServices;

    private FlowLayoutView mFlowLayoutOptimization;
    private FlowLayoutView mFlowLayoutSecurity;
    private FlowLayoutView mFlowLayoutManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setBody(R.layout.activity_self_service);
        mCustomTitle.setText(getResources().getString(R.string.self_service));
        initGridView();
    }

    private void initGridView() {
        //mOptimizationGrid = (GridView) findViewById(R.id.optimization_services);
        //mSecurityGrid = (GridView) findViewById(R.id.security_services);
        //mManagerGrid = (GridView) findViewById(R.id.manager_services);

        mFlowLayoutOptimization = (FlowLayoutView) findViewById(R.id.flow_layout_optimization);
        mFlowLayoutSecurity = (FlowLayoutView) findViewById(R.id.flow_layout_security);
        mFlowLayoutManager = (FlowLayoutView) findViewById(R.id.flow_layout_manager);
        initData();
        String[] from = {"label"};
        int[] to = {R.id.label};
        /*
        mOptimizationGrid.setAdapter(new SimpleAdapter(this, mOptimizationServices, R.layout.self_service_item,
                from, to));
        mOptimizationGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map map = mOptimizationServices.get(position);
                if (map != null && map.get("packageName") != null && map.get("className") != null) {
                    Intent intent = new Intent();
                    intent.setClassName((String) map.get("packageName"), (String) map.get("className"));
                    Log.d(TAG, "intent = " + intent);
                    startActivity(intent);
                }
            }
        });*/
        String[] optimizationServicesLabelArray = getResources().getStringArray(R.array.optimization_services_label);
        for (int i = 0; i < optimizationServicesLabelArray.length; i++) {
            final TextView tv = (TextView) getLayoutInflater().inflate(
                    R.layout.item_flow_layout_selfservice, mFlowLayoutOptimization, false);
            tv.setText(optimizationServicesLabelArray[i]);
            final Map map = mOptimizationServices.get(i);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (map != null && map.get("packageName") != null && map.get("className") != null) {
                        Intent intent = new Intent();
                        intent.setClassName((String) map.get("packageName"), (String) map.get("className"));
                        Log.d(TAG, "intent = " + intent);
                        startActivity(intent);
                    }
                }
            });
            mFlowLayoutOptimization.addView(tv);
        }
/*
        mSecurityGrid.setAdapter(new SimpleAdapter(this, mSecurityServices, R.layout.self_service_item,
                from, to));
        mSecurityGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map map = mSecurityServices.get(position);
                if (map != null && map.get("packageName") != null && map.get("className") != null) {
                    Intent intent = new Intent();
                    intent.setClassName((String) map.get("packageName"), (String) map.get("className"));
                    Log.d(TAG, "intent = " + intent);
                    startActivity(intent);
                }
            }
        });
        */
        String[] securityServicesLabelArray = getResources().getStringArray(R.array.security_services_label);
        for (int i = 0; i < securityServicesLabelArray.length; i++) {
            final TextView tv = (TextView) getLayoutInflater().inflate(
                    R.layout.item_flow_layout_selfservice, mFlowLayoutSecurity, false);
            tv.setText(securityServicesLabelArray[i]);
            final Map map = mSecurityServices.get(i);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (map != null && map.get("packageName") != null && map.get("className") != null) {
                        Intent intent = new Intent();
                        intent.setClassName((String) map.get("packageName"), (String) map.get("className"));
                        if(((String) map.get("className")).equals("com.gome.security.activity.MainActivity")){
                            Log.d(TAG, "need specific flag");
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        }
                        Log.d(TAG, "intent = " + intent);
                        startActivity(intent);
                    }
                }
            });
            mFlowLayoutSecurity.addView(tv);
        }
        /*
        mManagerGrid.setAdapter(new SimpleAdapter(this, mManagerServices, R.layout.self_service_item,
                from, to));
        mManagerGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map map = mManagerServices.get(position);
                if (map != null && map.get("packageName") != null && map.get("className") != null) {
                    if(!((String) map.get("packageName")).isEmpty()){
                        Intent intent = new Intent();
                        intent.setClassName((String) map.get("packageName"), (String) map.get("className"));
                        Log.d(TAG, "intent = " + intent);
                        startActivity(intent);
                    }
                }
            }
        });*/
        String[] managerServicesLabelArray = getResources().getStringArray(R.array.manager_services_label);
        for (int i = 0; i < managerServicesLabelArray.length; i++) {
            final TextView tv = (TextView) getLayoutInflater().inflate(
                    R.layout.item_flow_layout_selfservice, mFlowLayoutManager, false);
            tv.setText(managerServicesLabelArray[i]);
            final Map map = mManagerServices.get(i);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (map != null && map.get("packageName") != null && map.get("className") != null) {
                        if(!((String) map.get("packageName")).isEmpty()){
                            Intent intent = new Intent();
                            intent.setClassName((String) map.get("packageName"), (String) map.get("className"));
                            Log.d(TAG, "intent = " + intent);
                            startActivity(intent);
                        }
                    }
                }
            });
            mFlowLayoutManager.addView(tv);
        }
    }

    private void initData() {

        String[] targetsOptimizationPackageName = getResources().getStringArray(R.array.optimization_targets_package_name);
        String[] targetsOptimizationClassName = getResources().getStringArray(R.array.optimization_targets_class_name);

        String[] targetsSecurityPackageName = getResources().getStringArray(R.array.security_targets_package_name);
        String[] targetsSecurityClassName = getResources().getStringArray(R.array.security_targets_class_name);

        String[] targetsManagerPackageName = getResources().getStringArray(R.array.manager_targets_package_name);
        String[] targetsManagerClassName = getResources().getStringArray(R.array.manager_targets_class_name);

        mOptimizationServices = new ArrayList<>();
        mSecurityServices = new ArrayList<>();
        mManagerServices = new ArrayList<>();
        String[] optimizationServicesLabelArray = getResources().getStringArray(R.array.optimization_services_label);
        String[] securityServicesLabelArray = getResources().getStringArray(R.array.security_services_label);
        String[] managerServicesLabelArray = getResources().getStringArray(R.array.manager_services_label);
        for (int i = 0; i < optimizationServicesLabelArray.length; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("label", optimizationServicesLabelArray[i]);
            if (i < targetsOptimizationClassName.length) {
                map.put("packageName", targetsOptimizationPackageName[i]);
                map.put("className", targetsOptimizationClassName[i]);
            } else {
                map.put("packageName", null);
                map.put("className", null);
            }
            mOptimizationServices.add(map);
        }
        for (int i = 0; i < securityServicesLabelArray.length; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("label", securityServicesLabelArray[i]);
            if (i < targetsSecurityPackageName.length) {
                map.put("packageName", targetsSecurityPackageName[i]);
                map.put("className", targetsSecurityClassName[i]);
            } else {
                map.put("packageName", null);
                map.put("className", null);
            }
            mSecurityServices.add(map);
        }
        for (int i = 0; i < managerServicesLabelArray.length; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("label", managerServicesLabelArray[i]);
            if (i < targetsManagerPackageName.length) {
                map.put("packageName", targetsManagerPackageName[i]);
                map.put("className", targetsManagerClassName[i]);
            } else {
                map.put("packageName", null);
                map.put("className", null);
            }
            mManagerServices.add(map);
        }
    }
}
