package com.gome.usercenter.module;

import java.util.List;

/**
 * Created by dongzq on 2017/7/18.
 */

/**
 * Card info interface, this will public the method  of some ***CardInfo to DynamicCardView
 * This will make it simple for us to use Object polymorphism
 */
public interface CardInfo {

    List<String> getCardInfos(boolean state);
    void initCardInfos();
    int getCardInfoType();
    String getReservationNumber();
    String getMaintenanceNumber();

    long getLatestProcessTime();

    String toString();
}
