package com.ifeng.recallScheduler.utils;

import com.google.common.collect.Lists;
import com.ifeng.recallScheduler.constant.GyConstant;
import com.ifeng.recallScheduler.item.Document;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 处理ikv的特征信息
 * Created by wupeng1 on 2016/10/25.
 */
public class ToolAssit {

    private static final Log log = LogFactory.getLog(ToolAssit.class);


    /**
     * 补全c和et特征词信息
     *
     * @param doc
     * @param features
     */
    public static void fillFeature(Document doc, Map<String, List<String>> features) {
        if(doc==null){
            return;
        }
        if (features != null) {
            List<String> cList = features.get(GyConstant.Symb_C_ReadableFeatures);
            List<String> etList = features.get(GyConstant.Symb_ET_ReadableFeatures);
            List<String> scList = features.get(GyConstant.Symb_SC_ReadableFeatures);
            doc.setDevCList(cList);
            doc.setDevEtList(etList);
            doc.setDevScList(scList);
        }
    }


    /**
     * 获取ikv中的 c分类信息
     * @param input
     * @return
     */
    public static List<String> getCFeature(String input) {
        if (StringUtils.isBlank(input)) return Lists.newArrayList();
        List<String> cList = new ArrayList<>();
        String[] featureArray = input.split("\\|!\\|");
        for (int i = 0; i < featureArray.length; i++) {
            String feature = featureArray[i];
            String[] item = feature.split("=");
            if (item.length != 2)
                continue;
            String featureType = item[0];
            String featureValue = item[1];
            if ("c".equals(featureType))
                cList.add(featureValue);
        }
        return cList;
    }


    /** 获取 hbase中  wemediaLevel
     *
     * @param input
     * @return
     */
    public static String getWemediaLevel(String input) {

        String wemediaLevel = "";

        if (StringUtils.isBlank(input)){
            return wemediaLevel;
        }

        String[] featureArray = input.split("\\|!\\|");
        if(featureArray == null || featureArray.length == 0){
            return wemediaLevel;
        }
        for (int i = 0; i < featureArray.length; i++) {
            String feature = featureArray[i];
            String[] item = feature.split("=");
            if (item.length != 2)
                continue;
            String featureType = item[0];
            String featureValue = item[1];
            if ( GyConstant.Symb_wemediaLevel_ItemOther.equalsIgnoreCase(featureType)){
                wemediaLevel = featureValue;
                break;
            }

        }
        return wemediaLevel;
    }








    public static Map<String, List<String>> getFeatures(String input) {
        if (StringUtils.isBlank(input)) return null;
        Map<String, List<String>> featruesMap = new HashMap<>();
        String[] featureArray = input.split("\\|!\\|");
        for (int i = 0; i < featureArray.length; i++) {
            String feature = featureArray[i];
            String[] item = feature.split("=");
            if (item.length != 2)
                continue;
            String featureType = item[0];
            String featureValue = item[1];
            List<String> type = featruesMap.getOrDefault(featureType, new ArrayList<>());
            type.add(featureValue);
            featruesMap.put(featureType, type);
        }
        return featruesMap;
    }




}
