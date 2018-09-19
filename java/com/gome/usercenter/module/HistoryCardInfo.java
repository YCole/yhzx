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
 * Created by dongzq on 2017/7/18.
 */

public class HistoryCardInfo implements CardInfo{


    private String mReservationTime;
    private String mAccessType;
    private String mReservationType;
    private String mServiceType;
    private String mMaintenanceNumber = "";
    private String mReservationNumber = "";
    private String mCurrentState = "";
    private String mModelNumber = "";
    private String mReservationStation = "";
    private String mReturnMachineTime = "";
    private String mMoneyAmount = "";
    private String mUserName = "";
    private String mMobileNumber = "";
    private String mIssueDescription = "";

    private List<String> mHistoryCardInfos;

    private static int[] HISTORY_CARD_TITLES_FULL = {
            R.string.card_title_maintenance_number,
            R.string.card_title_reservation_number,
            R.string.card_title_current_state,
            //R.string.card_title_model_number,
            R.string.card_title_reservation_station,
            R.string.card_title_return_machine_time,
            R.string.card_title_money_amount,
            R.string.card_title_user_name,
            R.string.card_title_mobile_number,
            R.string.card_title_issue_description
    };
    private static int[] HISTORY_CARD_SUMMAY_APPEARANCE_FULL = {
            R.style.card_view_textSize_highlight_appearance,
            R.style.card_view_summary_default_appearance,
            R.style.card_view_blue_highlight_appearance,
            //R.style.card_view_summary_default_appearance,
            R.style.card_view_summary_default_appearance,
            R.style.card_view_summary_default_appearance,
            R.style.card_view_summary_default_appearance,
            R.style.card_view_summary_default_appearance,
            R.style.card_view_summary_default_appearance,
            R.style.card_view_summary_default_appearance
    };

    private static int[] HISTORY_CARD_TITLES_FOLD = {
            R.string.card_title_current_state,
            R.string.card_title_return_machine_time,
    };
    private static int[] HISTORY_CARD_SUMMAY_APPEARANCE_FOLD = {
            R.style.card_view_blue_highlight_appearance,
            R.style.card_view_summary_default_appearance
    };
    public HistoryCardInfo() {
        super();
    }

    public HistoryCardInfo(String maintenanceNumber, String reservationNumber, String currentState,
            String reservationStation, String returnMachineTime,
            String moneyAmount, String userName, String mobileNumber, String issueDescription) {
        super();
        this.mMaintenanceNumber = maintenanceNumber;
        this.mReservationNumber = reservationNumber;
        this.mCurrentState = currentState;
        //this.mModelNumber = modelNumber;
        this.mReservationStation = reservationStation;
        this.mReturnMachineTime = returnMachineTime;
        this.mMoneyAmount = moneyAmount;
        this.mUserName = userName;
        this.mMobileNumber = mobileNumber;
        this.mIssueDescription = issueDescription;
        initCardInfos();
    }

    public HistoryCardInfo(String repairNumber, String reservationNumber, String status,
                           String appointNetwork, String revertTime, String amount, String name, String tel,
                           String problemDetail, //String appointment, String accessType, String reservationType,
                           String serviceType) {
        super();
        this.mMaintenanceNumber = repairNumber;
        this.mReservationNumber = reservationNumber;
        this.mCurrentState = status;
        //this.mModelNumber = model;
        this.mReservationStation = appointNetwork;
        this.mReturnMachineTime = revertTime;
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
     * @return fold state info(2 items) when state = true otherwise all info
     */

    public List<String> getCardInfos(boolean state) {
        mHistoryCardInfos.clear();
        mHistoryCardInfos.add(ActivityUtils.insertSubStringInString(mMaintenanceNumber, " ", 4));
        mHistoryCardInfos.add(ActivityUtils.insertSubStringInString(mReservationNumber, " ", 4));
        mHistoryCardInfos.add(mCurrentState);
        //mHistoryCardInfos.add(mModelNumber);
        mHistoryCardInfos.add(mReservationStation);
        mHistoryCardInfos.add(mReturnMachineTime);
        mHistoryCardInfos.add(mMoneyAmount);
        mHistoryCardInfos.add(mUserName);
        mHistoryCardInfos.add(mMobileNumber);
        mHistoryCardInfos.add(mIssueDescription);
        if (state) {
            ArrayList<String> arrayList = new ArrayList<String>();
            arrayList.add(mHistoryCardInfos.get(2));
            arrayList.add(mHistoryCardInfos.get(5));
            return arrayList;
        } else {
            return mHistoryCardInfos;
        }
    }

    public static int[] getCardTitles(boolean state) {
        return state ? HISTORY_CARD_TITLES_FOLD : HISTORY_CARD_TITLES_FULL;
    }

    public static int[] getCardSummayAppearance(boolean state) {
        return state ? HISTORY_CARD_SUMMAY_APPEARANCE_FOLD : HISTORY_CARD_SUMMAY_APPEARANCE_FULL;
    }

    public void initCardInfos() {
        mHistoryCardInfos = new ArrayList();
        mHistoryCardInfos.add(ActivityUtils.insertSubStringInString(mMaintenanceNumber, " ", 4));
        mHistoryCardInfos.add(ActivityUtils.insertSubStringInString(mReservationNumber, " ", 4));
        mHistoryCardInfos.add(mCurrentState);
        //mHistoryCardInfos.add(mModelNumber);
        mHistoryCardInfos.add(mReservationStation);
        mHistoryCardInfos.add(mReturnMachineTime);
        mHistoryCardInfos.add(mMoneyAmount);
        mHistoryCardInfos.add(mUserName);
        mHistoryCardInfos.add(mMobileNumber);
        mHistoryCardInfos.add(mIssueDescription);
    }

    public int getCardInfoType() {
        return DynamicCardAdapter.HISTORY_CARD;
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
            Date date = format.parse(this.mReturnMachineTime);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public String toString() {
        return  "repairNumber : " + this.mMaintenanceNumber + ", reservationNumber : " + this.mReservationNumber
                + ", currentState : " + this.mCurrentState
                + ", reservationStation : " + this.mReservationStation + ", returnMachineTime : " + this.mReturnMachineTime
                + ", moneyAmount : " + this.mMoneyAmount + ", userName : " + this.mUserName
                + ", mobile : " + this.mMobileNumber + "issueDescription : " + this.mIssueDescription;
    }
}
