package com.gome.usercenter.module;

import com.gome.usercenter.R;
import com.gome.usercenter.utils.ActivityUtils;
import com.gome.usercenter.view.DynamicCardAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by dongzq on 2017/7/17.
 */

public class MaintenanceCardInfo implements CardInfo{

    private String mReservationTime;
    private String mAccessType;
    private String mReservationType;
    private String mServiceType;
    private String mMaintenanceNumber = "";
    private String mReservationNumber = "";
    private String mCurrentState = "";
    private String mModelNumber = "";
    private String mReservationStation = "";
    private String mProcessingTime = "";
    private String mReturnMachineTime = "";
    private String mMoneyAmount = "";
    private String mUserName = "";
    private String mMobileNumber = "";
    private String mIssueDescription = "";

    private List<String> mMaintenanceCardInfos;

    private static int[] MAINTENANCE_CARD_TITLES_FULL = {
            R.string.card_title_maintenance_number,
            R.string.card_title_reservation_number,
            R.string.card_title_current_state,
            //R.string.card_title_model_number,
            R.string.card_title_reservation_station,
            R.string.card_title_processing_time,
            R.string.card_title_money_amount,
            R.string.card_title_user_name,
            R.string.card_title_mobile_number,
            R.string.card_title_issue_description
    };
    private static int[] MAINTENANCE_CARD_SUMMAY_APPEARANCE_FULL = {
            R.style.card_view_textSize_highlight_appearance,
            R.style.card_view_summary_default_appearance,
            R.style.card_view_blue_highlight_appearance,
            //R.style.card_view_summary_default_appearance,
            R.style.card_view_blue_highlight_appearance,
            R.style.card_view_summary_default_appearance,
            R.style.card_view_summary_default_appearance,
            R.style.card_view_summary_default_appearance,
            R.style.card_view_summary_default_appearance,
            R.style.card_view_summary_default_appearance
    };

    private static int[] MAINTENANCE_CARD_TITLES_FOLD = {
            R.string.card_title_current_state,
            //R.string.card_title_model_number,
            R.string.card_title_reservation_station,
            R.string.card_title_processing_time
    };
    private static int[] MAINTENANCE_CARD_SUMMAY_APPEARANCE_FOLD = {
            R.style.card_view_blue_highlight_appearance,
            //R.style.card_view_summary_default_appearance,
            R.style.card_view_blue_highlight_appearance,
            R.style.card_view_summary_default_appearance
    };

    public MaintenanceCardInfo() {
        super();
    }

    public MaintenanceCardInfo(String maintenanceNumber, String reservationNumber, String currentState,
            String reservationStation, String processingTime,
            String moneyAmount, String userName, String mobileNumber, String issueDescription) {
        super();
        this.mMaintenanceNumber = maintenanceNumber;
        this.mReservationNumber = reservationNumber;
        this.mCurrentState = currentState;
        //this.mModelNumber = modelNumber;
        this.mReservationStation = reservationStation;
        this.mProcessingTime = processingTime;
        this.mMoneyAmount = moneyAmount;
        this.mUserName = userName;
        this.mMobileNumber = mobileNumber;
        this.mIssueDescription = issueDescription;
        initCardInfos();
    }

    public MaintenanceCardInfo(String repairNumber, String reservationNumber, String status,
                               String appointNetwork, String acceptTime, String amount, String name, String tel,
                               String problemDetail, //String appointment, String accessType, String reservationType,
                               String serviceType) {

        super();
        this.mMaintenanceNumber = repairNumber;
        this.mReservationNumber = reservationNumber;
        this.mCurrentState = status;
        //this.mModelNumber = model;
        this.mReservationStation = appointNetwork;
        this.mProcessingTime = acceptTime;
        this.mMoneyAmount = amount;
        this.mUserName = name;
        this.mMobileNumber = tel;
        this.mIssueDescription = problemDetail;
//        this.mReservationTime = appointment;
//        this.mAccessType = accessType;
//        this.mReservationType = reservationType;
        this.mServiceType = serviceType;
        initCardInfos();
    }

    /**
     * @author dongzq
     * created at 2017/7/19
     * @param state The DynamicCardView fold state
     * @return fold state info(4 items) when state = true otherwise all info
     */
    public List<String> getCardInfos(boolean state) {
        mMaintenanceCardInfos.clear();
        mMaintenanceCardInfos.add(ActivityUtils.insertSubStringInString(mMaintenanceNumber, " ", 4));
        mMaintenanceCardInfos.add(ActivityUtils.insertSubStringInString(mReservationNumber, " ", 4));
        mMaintenanceCardInfos.add(mCurrentState);
        //mMaintenanceCardInfos.add(mModelNumber);
        mMaintenanceCardInfos.add(mReservationStation);
        mMaintenanceCardInfos.add(mProcessingTime);
        mMaintenanceCardInfos.add(mMoneyAmount);
        mMaintenanceCardInfos.add(mUserName);
        mMaintenanceCardInfos.add(mMobileNumber);
        mMaintenanceCardInfos.add(mIssueDescription);
        if (state) {
            return mMaintenanceCardInfos.subList(2, 6);
        } else {
            return mMaintenanceCardInfos;
        }
    }

    public static int[] getCardTitles(boolean state) {
        return state ? MAINTENANCE_CARD_TITLES_FOLD : MAINTENANCE_CARD_TITLES_FULL;
    }

    public static int[] getCardSummayAppearance(boolean state) {
        return state ? MAINTENANCE_CARD_SUMMAY_APPEARANCE_FOLD : MAINTENANCE_CARD_SUMMAY_APPEARANCE_FULL;
    }

    public void initCardInfos() {
        mMaintenanceCardInfos = new ArrayList();
        mMaintenanceCardInfos.add(ActivityUtils.insertSubStringInString(mMaintenanceNumber, " ", 4));
        mMaintenanceCardInfos.add(ActivityUtils.insertSubStringInString(mReservationNumber, " ", 4));
        mMaintenanceCardInfos.add(mCurrentState);
        //mMaintenanceCardInfos.add(mModelNumber);
        mMaintenanceCardInfos.add(mReservationStation);
        mMaintenanceCardInfos.add(mProcessingTime);
        mMaintenanceCardInfos.add(mMoneyAmount);
        mMaintenanceCardInfos.add(mUserName);
        mMaintenanceCardInfos.add(mMobileNumber);
        mMaintenanceCardInfos.add(mIssueDescription);
    }

    public int getCardInfoType() {
        return DynamicCardAdapter.MAINTENANCE_CARD;
    }

    public String getReservationNumber() {
        return mReservationNumber;
    }

    public String getMaintenanceNumber() {
        return mMaintenanceNumber;
    }

    public long getLatestProcessTime() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");

        try {
            Date date = format.parse(this.mProcessingTime);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public String toString() {
        return  "repairNumber : " + this.mMaintenanceNumber + ", reservationNumber : " + this.mReservationNumber
                + ", currentState : " + this.mCurrentState + this.mModelNumber
                + ", reservationStation : " + this.mReservationStation + ", returnMachineTime : " + this.mReturnMachineTime
                + "processingTime : " + this.mProcessingTime + ", moneyAmount : " + this.mMoneyAmount
                + ", userName : " + this.mUserName + ", mobile : " + this.mMobileNumber
                + "issueDescription : " + this.mIssueDescription;
    }
}
