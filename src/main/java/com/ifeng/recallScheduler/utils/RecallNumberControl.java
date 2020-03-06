package com.ifeng.recallScheduler.utils;


import com.ifeng.recallScheduler.user.RecordInfo;

import java.util.List;
import java.util.Map;

/**
 * Created by geyl on 2017/11/9.
 */
public class RecallNumberControl {
    private int recallNumber = 500;
    private double sumWeight;
    private List<RecordInfo> list;
    private Map<String, Double> tagWithWeightMap;

    public RecallNumberControl(List<RecordInfo> list, Map<String, Double> tagWithWeightMap, int recallNumber) {
        this.list = list;
        this.tagWithWeightMap = tagWithWeightMap;
        this.recallNumber = recallNumber;
        init();
    }

    private void init() {
        for (RecordInfo recordInfo : list) {
            sumWeight += (recordInfo.getWeight() - 0.5);
        }
    }

    public int getRecallNumber(String tagName) {
        try {
            double s = (tagWithWeightMap.get(tagName) - 0.5) / sumWeight;  //归一化后的tag权重
            return (int) (Math.round(Math.log1p(s) * recallNumber));
        } catch (Exception e) {
            return 10;
        }
    }

}
