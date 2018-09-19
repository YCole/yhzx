package com.gome.usercenter.view.citypicker.utils;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.gome.usercenter.utils.ActivityUtils;
import com.gome.usercenter.view.citypicker.model.CityModel;
import com.gome.usercenter.view.citypicker.model.DistrictModel;
import com.gome.usercenter.view.citypicker.model.ProvinceModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dongzq on 2017/7/6.
 */

public class JSONParser {


    private static final String TAG = "JSONParser";
    private Context mContext;
    private List<ProvinceModel> mProvinceList = new ArrayList<ProvinceModel>();

    private static JSONParser sJSONParser;

    protected JSONParser(Context context) {
        super();
        mContext = context;
    }
    // May be we should use application context, so the activity can be gc when it has be destroyed
    public static JSONParser newInstance(Context context) {
        if (sJSONParser == null) {
            sJSONParser = new JSONParser(context);
        }
        return sJSONParser;
    }

    public List<ProvinceModel> getProvinceList() {
        return mProvinceList;
    }

    public boolean parse(String file) throws IOException, JSONException {
        String jsonData = ActivityUtils.readFromFileOneLine(mContext, file);
        /*
        AssetManager assetManager = mContext.getAssets();
        StringBuilder jsonData = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(assetManager.open(file)));
            String line;
            while ((line = br.readLine()) != null) {
                jsonData.append(line);
            }
        //parserJSON2List(jsonData.toString(), mProvinceList);
        //parserJSONFromServer2List(jsonData.toString(), mProvinceList);
        */
        parserJSONFromServer2List(jsonData, mProvinceList);
        return true;
    }


    private void parserJSON2List(String json, List<ProvinceModel> datas) throws JSONException {
            JSONObject jsonObject = new JSONObject(json);
            JSONObject root = jsonObject.getJSONObject("root");
            JSONArray provinces = root.getJSONArray("province");
            if (provinces.length() > 0) {
                mProvinceList.clear();
            }
            for (int i = 0; i < provinces.length(); i++) {
                JSONObject province = (JSONObject) provinces.opt(i);
                if (province == null) {
                    continue;
                }
                ProvinceModel provinceModel = new ProvinceModel();
                provinceModel.setName(province.getString("name"));
                provinceModel.setCityList(new ArrayList<CityModel>());
                JSONArray cities = province.getJSONArray("city");
                for (int j = 0; j < cities.length(); j++) {
                    JSONObject city = (JSONObject) cities.opt(j);
                    if (city ==  null) {
                        continue;
                    }
                    CityModel cityModel = new CityModel();
                    cityModel.setName(city.getString("name"));
                    cityModel.setDistrictList(new ArrayList<DistrictModel>());
                    JSONArray districts = city.getJSONArray("district");
                    for (int k = 0; k < districts.length(); k++) {
                        JSONObject district = (JSONObject) districts.opt(k);
                        if (district == null) {
                            continue;
                        }
                        DistrictModel districtModel = new DistrictModel();
                        districtModel.setName(district.getString("name"));
                        districtModel.setZipcode(district.getString("zipcode"));
                        cityModel.getDistrictList().add(districtModel);
                    }
                    provinceModel.getCityList().add(cityModel);
                }
                mProvinceList.add(provinceModel);
            }
    }
    private void parserJSONFromServer2List(String json, List<ProvinceModel> datas) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        JSONArray provinces = jsonObject.getJSONArray("results");
        if (provinces.length() > 0) {
            mProvinceList.clear();
        }
        for (int i = 0; i < provinces.length(); i++) {
            JSONObject province = (JSONObject) provinces.opt(i);
            if (province == null) {
                continue;
            }
            ProvinceModel provinceModel = new ProvinceModel();
            provinceModel.setName(province.getString("province"));
            provinceModel.setCityList(new ArrayList<CityModel>());
            String cities = province.getString("city");
            String[] cityArray = cities.split(",");
            for (int j = 0; j < cityArray.length; j++) {
                String city = cityArray[j];
                CityModel cityModel = new CityModel();
                cityModel.setName(city);
                provinceModel.getCityList().add(cityModel);
            }
            mProvinceList.add(provinceModel);
        }
    }
}
