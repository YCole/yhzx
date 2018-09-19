package com.gome.usercenter.module;

import com.gome.usercenter.utils.Constants;
import com.gome.usercenter.view.DynamicCardAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Created by dongzq on 2017/7/28.
 */

public class CardListInfo {
    private final String TAG = Constants.TAG_HEADER + "CardListInfo";
    private List<CardInfo> mCardList;

    public CardListInfo() {
        super();
        mCardList = new ArrayList();
    }

    public void resolveJsonDatas(String data) {
        if (data == null || data.isEmpty()) {
            return;
        }
        try {
            JSONObject json = new JSONObject(data);
            JSONArray jsonArray = json.getJSONArray("results");
            for (int i = 0; jsonArray != null && i < jsonArray.length(); i++) {
                JSONObject jsonObject = (JSONObject) jsonArray.opt(i);
                String reservationNumber = jsonObject.getString("reservationNumber");
                String repairNumber = jsonObject.getString("repairNumber");
                String status = jsonObject.getString("status");
                //String model = jsonObject.getString("model");
                String appointNetwork = jsonObject.getString("appointNetwork");
                String appointment = jsonObject.getString("appointment");
                String acceptTime = jsonObject.getString("acceptTime");
                String problemDetail = jsonObject.getString("problemDetail");
                String serviceType = jsonObject.getString("serviceType");
                String revertTime = jsonObject.getString("revertTime");
                String amount = jsonObject.getString("amount");
                String name = jsonObject.getString("name");
                String tel = jsonObject.getString("tel");
                if ("".equals(repairNumber)) {
                    // ReservationCardInfo
                    ReservationCardInfo reservationCardInfo = new ReservationCardInfo(reservationNumber, appointment,
                            appointNetwork, name, tel, problemDetail);
                    mCardList.add(reservationCardInfo);
                } else if ("".equals(revertTime)) {
                    // MaintenanceCardInfo
                    MaintenanceCardInfo maintenanceCardInfo = new MaintenanceCardInfo(repairNumber, reservationNumber,
                            status, appointNetwork, acceptTime, amount, name, tel, problemDetail);
                    mCardList.add(maintenanceCardInfo);
                } else {
                    // HistoryCardInfo
                    HistoryCardInfo historyCardInfo = new HistoryCardInfo(repairNumber, reservationNumber, status,
                            appointNetwork, revertTime, amount, name, tel, problemDetail);
                    mCardList.add(historyCardInfo);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public List<CardInfo> getCardList() {
        return mCardList;
    }

    public void updateCardList(String repair, String reserve) {
        mCardList.clear();
        resolveReserveJsonDatas(reserve);
        resolveRepqirJsonDatas(repair);
        sortCardListByNumber();
    }

    private void resolveRepqirJsonDatas(String repair) {
        if (repair == null || repair.isEmpty()) {
            return;
        }
        try {
            JSONObject json = new JSONObject(repair);
            JSONArray jsonArray = json.getJSONArray("results");
            for (int i = 0; jsonArray != null && i < jsonArray.length(); i++) {
                JSONObject jsonObject = (JSONObject) jsonArray.opt(i);
                String reservationNumber = jsonObject.getString("reservationNumber");
                String repairNumber = jsonObject.getString("repairNumber");
                String status = jsonObject.getString("status");
                //String model = jsonObject.getString("model");
                String appointNetwork = jsonObject.getString("serviceNetwork");
/*
                String appointment = jsonObject.getString("appointment");
*/
                String acceptTime = jsonObject.getString("acceptTime");
                if (acceptTime != null && !acceptTime.isEmpty()) {
                    acceptTime = acceptTime.substring(0, acceptTime.indexOf('.'));
                }
                String problemDetail = jsonObject.getString("problemDetail");
/*
                String accessType = jsonObject.getString("accessType");
                String reservationType = jsonObject.getString("reservationType");
*/
                String serviceType = jsonObject.getString("serviceType");
                String revertTime = jsonObject.getString("revertTime");
                if (revertTime != null && !revertTime.isEmpty()) {
                    revertTime = revertTime.substring(0, revertTime.indexOf('.'));
                }
                String amount = jsonObject.getString("amount");
                String name = jsonObject.getString("name");
                String tel = jsonObject.getString("tel");
                if ("".equals(revertTime)) {
                    // MaintenanceCardInfo
                    MaintenanceCardInfo maintenanceCardInfo = new MaintenanceCardInfo(repairNumber, reservationNumber,
                            status, appointNetwork, acceptTime, amount, name, tel, problemDetail,
                            //appointment, accessType, reservationType,
                            serviceType);
                    mCardList.add(maintenanceCardInfo);
                } else {
                    // HistoryCardInfo
                    HistoryCardInfo historyCardInfo = new HistoryCardInfo(repairNumber, reservationNumber, status,
                            appointNetwork, revertTime, amount, name, tel, problemDetail,
                            //appointment, accessType, reservationType,
                            serviceType);
                    mCardList.add(historyCardInfo);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void resolveReserveJsonDatas(String reserve) {
        if (reserve == null || reserve.isEmpty()) {
            return;
        }
        try {
            JSONObject json = new JSONObject(reserve);
            JSONArray jsonArray = json.getJSONArray("results");
            for (int i = 0; jsonArray != null && i < jsonArray.length(); i++) {
                JSONObject jsonObject = (JSONObject) jsonArray.opt(i);
                String reservationNumber = jsonObject.getString("reservationNumber");
                //String model = jsonObject.getString("model");
                String appointNetwork = jsonObject.getString("appointNetwork");
                String appointment = jsonObject.getString("appointment");
                if (appointment != null && !appointment.isEmpty()) {
                    appointment = appointment.substring(0, appointment.indexOf('.'));
                }
                String accessType = jsonObject.getString("accessType");
                String reservationType = jsonObject.getString("reservationType");
                String serviceType = jsonObject.getString("serviceType");
                String problemDetail = jsonObject.getString("problemDetail");
                String name = jsonObject.getString("name");
                String tel = jsonObject.getString("tel");
                // ReservationCardInfo
                ReservationCardInfo reservationCardInfo = new ReservationCardInfo(reservationNumber, appointment,
                        appointNetwork, name, tel, problemDetail, accessType, reservationType, serviceType);
                mCardList.add(reservationCardInfo);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // sort rule:
    // 1. by card type, position: reservation > maintenance > history
    // 2. with same reservation number or same maintenance number should have same position
    // 3. by date, the position will be font with latest date
    private void sortCardListByNumber() {
        HashMap<String, HashMap<String, Object>> cardGroupMap = new HashMap<String, HashMap<String, Object>>();
        for (int i = 0; i < mCardList.size(); i ++) {
            // find group key
            String number = ((CardInfo) mCardList.get(i)).getReservationNumber();
            if ("".equals(number)) {
                number = ((CardInfo) mCardList.get(i)).getMaintenanceNumber();
            }

            // find group
            HashMap<String, Object> group = cardGroupMap.get(number);

            // if there is no group accroding to the key, then create new and add the group into cardGroupMap
            if (group == null) {
                group = new HashMap<String, Object>();
                List<CardInfo> list = new ArrayList<CardInfo>();
                group.put("list", list);
                cardGroupMap.put(number, group);
            }
            ((List<CardInfo>) group.get("list")).add((CardInfo) mCardList.get(i));
        }

        List<HashMap<String, Object>> groupList = new ArrayList<HashMap<String, Object>>(cardGroupMap.values());
        for (HashMap<String, Object> group : groupList) {
            Collections.sort((List<CardInfo>)group.get("list"), new Comparator() {
                public int compare(Object o1, Object o2) {
                    CardInfo card1 = (CardInfo) o1;
                    CardInfo card2 = (CardInfo) o2;
                    if (card1.getCardInfoType() < card2.getCardInfoType()) {
                        return -1;
                    }
                    if (card1.getCardInfoType() > card2.getCardInfoType()) {
                        return 1;
                    }
                    return 0;
                }
            });
            List<CardInfo> list = (List<CardInfo>) group.get("list");
            if (list == null || list.size() < 1) {
                continue;
            }
            tryRemoveDuplicateReservationCard(list);
            CardInfo latestCard = (CardInfo) list.get(list.size() - 1);
            group.put("time", new Long(latestCard.getLatestProcessTime()));
        }
        Collections.sort(groupList, new Comparator() {
            public int compare(Object o1, Object o2) {
                HashMap<String, Object> group1 = (HashMap<String, Object>) o1;
                HashMap<String, Object> group2 = (HashMap<String, Object>) o2;
                Long time1 = (Long) group1.get("time");
                Long time2 = (Long) group2.get("time");
                int result = time1.compareTo(time2);
                return result == 1 ? -1 : result == -1 ? 1 : 0;
            }
        });
        mCardList.clear();
        for (HashMap<String, Object> group : groupList) {
            for (CardInfo card : (List<CardInfo>) group.get("list")) {
                mCardList.add(card);
            }
        }
    }

    private void tryRemoveDuplicateReservationCard(List<CardInfo> list) {
        if (list.size() > 1) {
            CardInfo card1 = (CardInfo) list.get(0);
            CardInfo card2 = (CardInfo) list.get(1);
            if (card1.getCardInfoType() == DynamicCardAdapter.RESERVATION_CARD
                    && card2.getCardInfoType() == DynamicCardAdapter.MAINTENANCE_CARD) {
                list.remove(0);
            }
        }
    }
}
