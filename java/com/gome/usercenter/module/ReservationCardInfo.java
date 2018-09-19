package com.gome.usercenter.module;

import com.gome.usercenter.R;
import com.gome.usercenter.utils.ActivityUtils;
import com.gome.usercenter.view.DynamicCardAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by dongzq on 2017/7/17.
 */

public class ReservationCardInfo implements CardInfo {

    private String mReservationType;
    private String mAccessType;
    private String mReservationNumber = "";
    // format: 1��1�� 08:00-10:00
    private String mReservationTime = "";
    private String mReservationStation = "";
    private String mModelNumber = "";
    private String mUserName = "";
    private String mMobileNumber = "";
    private String mIssueDescription = "";
    private String mServiceType = "";
    private String mMaintenanceMode = "";
    private String mProvince = "";
    private String mCity = "";
    private String mTextSelect = "";

    private List<String> mReservationCardInfos;

    private static int[] RESERVATION_CARD_TITLES_FULL = {
            R.string.card_title_reservation_number,
            R.string.card_title_reservation_time,
            R.string.card_title_reservation_station,
            //R.string.card_title_model_number,
            R.string.card_title_user_name,
            R.string.card_title_mobile_number,
            R.string.card_title_issue_description
    };
    private static int[] RESERVATION_CARD_SUMMAY_APPEARANCE_FULL = {
            R.style.card_view_textSize_highlight_appearance,
            R.style.card_view_summary_default_appearance,
            R.style.card_view_blue_highlight_appearance,
            //R.style.card_view_summary_default_appearance,
            R.style.card_view_summary_default_appearance,
            R.style.card_view_summary_default_appearance,
            R.style.card_view_summary_default_appearance
    };

    private static int[] RESERVATION_CARD_TITLES_FOLD = {
            R.string.card_title_reservation_time,
            R.string.card_title_reservation_station,
            //R.string.card_title_model_number
            R.string.card_title_user_name
    };
    private static int[] RESERVATION_CARD_SUMMAY_APPEARANCE_FOLD = {
            R.style.card_view_summary_default_appearance,
            R.style.card_view_blue_highlight_appearance,
            //R.style.card_view_summary_default_appearance
            R.style.card_view_summary_default_appearance
    };

    public ReservationCardInfo() {
        super();
    }

    public ReservationCardInfo(String reservationNumber, String reservationTime, String reservationStation,
            String userName, String mobileNumber, String issueDescription) {
        super();
        this.mReservationNumber = reservationNumber;
        this.mReservationTime = reservationTime;
        this.mReservationStation = reservationStation;
        //this.mModelNumber = modelNumber;
        this.mUserName = userName;
        this.mMobileNumber = mobileNumber;
        this.mIssueDescription = issueDescription;
        initCardInfos();
    }

    public ReservationCardInfo(String reservationNumber, String appointment, String appointNetwork,
                               String name, String tel, String problemDetail, String accessType, String reservationType,
                               String serviceType) {
        super();
        this.mReservationNumber = reservationNumber;
        this.mReservationTime = appointment;
        this.mReservationStation = appointNetwork;
        //this.mModelNumber = model;
        this.mUserName = name;
        this.mMobileNumber = tel;
        this.mIssueDescription = problemDetail;
        this.mAccessType = accessType;
        this.mReservationType = reservationType;
        this.mServiceType = serviceType;
        initCardInfos();

    }

    public void setTextSelect(String textSelect) {
        this.mTextSelect = textSelect;
    }

    public void setReservationNumber(String reservationNumber) {
        this.mReservationNumber = reservationNumber;
    }

    public void setReservationTime(String reservationTime) {
        this.mReservationTime = reservationTime;
    }

    public void setReservationStation(String reservationStation) {
        this.mReservationStation = reservationStation;
    }

//    public void setModelNumber(String modelNumber) {
//        this.mModelNumber = modelNumber;
//    }

    public void setServiceType(String serviceType) {
        this.mServiceType = serviceType;
    }

    public void setMaintenanceMode(String maintenanceMode) {
        this.mMaintenanceMode = maintenanceMode;
    }

    public void setUserName(String userName) {
        this.mUserName = userName;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mMobileNumber = mobileNumber;
    }

    public void setIssueDescription(String issueDescription) {
        this.mIssueDescription = issueDescription;
    }

    public String getTextSelect() {
        return mTextSelect;
    }

    public String getReservationNumber() {
        return mReservationNumber;
    }

    public String getMaintenanceNumber() {
        return "";
    }

    public long getLatestProcessTime() {
        /*
        Calendar calendar = Calendar.getInstance();
        StringBuilder builder = new StringBuilder();
        builder.append(calendar.get(Calendar.YEAR));
        String[] times = this.mReservationTime.split("-");
        builder.append(times[0]);
        SimpleDateFormat format = new SimpleDateFormat("yyyyMM月dd日 HH:mm");
        */
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
        try {
            Date date = format.parse(this.mReservationTime);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public String getReservationTime() {
        return mReservationTime;
    }

    public String getReservationStation() {
        return mReservationStation;
    }

//    public String getModelNumber() {
//        return mModelNumber;
//    }

    public String getUserName() {
        return mUserName;
    }

    public String getMobileNumber() {
        return mMobileNumber;
    }

    public String getIssueDescription() {
        return mIssueDescription;
    }

    public String getServiceType() {
        return mServiceType;
    }

    public String getMaintenanceMode() {
        return mMaintenanceMode;
    }

    public String getCity() {
        return mCity;
    }
    public String getProvince() {
        return mProvince;
    }
    /**
     * @author dongzq
     * created at 2017/7/19
     * @param state The DynamicCardView fold state
     * @return fold state info(3 items) when state = true otherwise all info
     */
    public List<String> getCardInfos(boolean state) {
        mReservationCardInfos.clear();
        mReservationCardInfos.add(ActivityUtils.insertSubStringInString(mReservationNumber, " ", 4));
        mReservationCardInfos.add(getTimeFormat(mReservationTime));
        mReservationCardInfos.add(mReservationStation);
        //mReservationCardInfos.add(mModelNumber);
        mReservationCardInfos.add(mUserName);
        mReservationCardInfos.add(mMobileNumber);
        mReservationCardInfos.add(mIssueDescription);
        if (state) {
            return mReservationCardInfos.subList(1, 4);
        } else {
            return mReservationCardInfos;
        }
    }

    public static int[] getCardTitles(boolean state) {
        return state ? RESERVATION_CARD_TITLES_FOLD : RESERVATION_CARD_TITLES_FULL;
    }

    private String getTimeFormat(String time) {
        String mTime = time;
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = simpleDateFormat.parse(time);
            SimpleDateFormat sFormat = new SimpleDateFormat("yyy年MM月dd日(EEEE) hh:mm");
            mTime = sFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return mTime;
    }

    public static int[] getCardSummayAppearance(boolean state) {
        return state ? RESERVATION_CARD_SUMMAY_APPEARANCE_FOLD : RESERVATION_CARD_SUMMAY_APPEARANCE_FULL;
    }

    public void initCardInfos() {
        mReservationCardInfos = new ArrayList();
        mReservationCardInfos.add(ActivityUtils.insertSubStringInString(mReservationNumber, " ", 4));
        mReservationCardInfos.add(mReservationTime);
        mReservationCardInfos.add(mReservationStation);
        //mReservationCardInfos.add(mModelNumber);
        mReservationCardInfos.add(mUserName);
        mReservationCardInfos.add(mMobileNumber);
        mReservationCardInfos.add(mIssueDescription);
    }

    public int getCardInfoType() {
        return DynamicCardAdapter.RESERVATION_CARD;
    }

    public void setProvince(String province) {
        this.mProvince = province;
    }

    public void setCity(String city) {
        this.mCity = city;
    }

    public static ReservationCardInfo resolveJsonData(String json) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(json);
            String reservationNumber = jsonObject.getString("reservationNumber");
            //String model = jsonObject.getString("model");
            String appointNetwork = jsonObject.getString("appointNetwork");
            String appointment = jsonObject.getString("appointment");
            String problemDetail = jsonObject.getString("problemDetail");
            String name = jsonObject.getString("name");
            String tel = jsonObject.getString("tel");
            ReservationCardInfo reservationCardInfo = new ReservationCardInfo(reservationNumber, appointment, appointNetwork,
                    name, tel, problemDetail);
            return reservationCardInfo;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;

    }

    public String toString() {
        return "reservationNumber : " + this.mReservationNumber + ", reservationTime : " + this.mReservationTime
                + ", reservationStation : "+ this.mReservationStation
                + ", userName : " + this.mUserName  + ", mobile : " + this.mMobileNumber
                + "issueDescription : " + this.mIssueDescription;
    }
}
