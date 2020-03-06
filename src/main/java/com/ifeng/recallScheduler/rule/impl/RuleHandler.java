package com.ifeng.recallScheduler.rule.impl;


import com.ifeng.recallScheduler.utils.ToolAssit;
import com.ifeng.recallScheduler.bloomFilter.BloomFilter;
import com.ifeng.recallScheduler.constant.DocConstant;
import com.ifeng.recallScheduler.constant.GyConstant;
import com.ifeng.recallScheduler.constant.RecWhy;
import com.ifeng.recallScheduler.item.Document;
import com.ifeng.recallScheduler.request.RequestInfo;
import com.ifeng.recallScheduler.rule.RuleHandlerInterface;
import com.ifeng.recallScheduler.rule.RuleHandlerInterface1;
import com.ifeng.recallScheduler.rule.RuleHandlerInterface2;
import com.ifeng.recallScheduler.utils.BlackDocUtil;
import com.ifeng.recallScheduler.utils.ListUtil;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.collections.CollectionUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * Created by wupeng1 on 2017/6/29.
 * 规则过滤执行单元
 */
public enum RuleHandler {

    INSTANCE;

    static Logger logger = LoggerFactory.getLogger(RuleHandler.class);


    /**
     * 判断是黑名单的docid
     *
     * @param docId
     * @return
     */
    public boolean isBlackDocId(String docId) {
        return (BlackDocUtil.blackDocCache.getIfPresent(docId) != null);
    }


    /**
     * 布隆排重过滤,只check 不写入
     */
    public RuleHandlerInterface bloomcheck = (document, userid) -> {
        Document doc = (Document) document;
        String uid = (String) userid;
        return !BloomFilter.onlyCheck(uid, doc.getSimId(), doc.getGroupId());
    };

    /**
     *
     */
    public RuleHandlerInterface2 duplicateBloomCheck = (document, userid, remoteIdSets) -> {
        Document doc = (Document) document;
        String uid = (String) userid;
        Set<String> idSets = (Set<String>) remoteIdSets;
        //先检查本地，本地已推出，返回false
        if (BloomFilter.onlyCheck(uid, doc.getSimId(), doc.getGroupId())) {
            return false;
        } else {
            if (idSets != null && idSets.contains(doc.getSimId())) {
                //id包含，代表远程记录未推出
                return true;
            }
            //idSets为null, 访问远程服务问题，按本地未推出返回true，否则代表远程已推出，返回false
            return idSets == null;
        }
    };

    /**
     * 入布隆
     */
    public RuleHandlerInterface addToBloom = (document, userId) -> {
        Document doc = (Document) document;
        String uid = (String) userId;
        BloomFilter.onlyPut(uid, doc.getSimId(), doc.getGroupId());
        return true;
    };


    /**
     * 本地Map数据过滤 规则
     * 用户画像loc（loc+generalloc）字段不空：根据文章locmap以及特征属性判断 如匹配上不过滤
     * 用户画像loc（loc+generalloc）为空：文章locmap以及特征属性只要还有地域信息 则都过滤
     */
    public RuleHandlerInterface2 localMapSetWithoutUserSub = (document, requestInfo, recomReason) -> {
        try{
            String reason=recomReason+"";
            if(StringUtils.isNotBlank(reason)&&(reason.contains(RecWhy.ReasonUserSearch)||reason.contains(RecWhy.ReasonUserSub))){
                return false;
            }
            Document doc = (Document) document; //document 实例
            RequestInfo request = (RequestInfo) requestInfo; //用户 实例

            String loc="";
            if(StringUtils.isNotBlank(request.getLoc())){
                loc=request.getLoc();
            }
            if(StringUtils.isNotBlank(request.getPermanentLoc())){
                loc=loc+request.getPermanentLoc();
            }
            if(doc==null){
                return false;
            }
            HashMap<String, HashSet<String>> locMap = doc.getLocMap();

            if(MapUtils.isNotEmpty(locMap)){
                if(StringUtils.isNotBlank(loc)){
                    Set<String> provinces = locMap.get(DocConstant.Doc_LocMap_Province); //获取文章 "省" 集合 后期可能会加入城市逻辑
                    if(CollectionUtils.isNotEmpty(provinces)){
                        if (provinces.size() == 1 && provinces.contains(GyConstant.province_unknown)) {
                            return false; //未知地理位置 不做过滤
                        }
                        for (String province : provinces) { //文章可能包含 多个地方属性
                            if (loc.contains(province)) {
                                return false; //当用户地理位置字符串 包含 某省信息 不做过滤
                            }
                        }
                        return true;
                    }
                }else{
                    Set<String> provinces = locMap.get(DocConstant.Doc_LocMap_Province); //获取文章 "省" 集合 后期可能会加入城市逻辑
                    if(CollectionUtils.isNotEmpty(provinces)){
                        if (provinces.size() == 1 && provinces.contains(GyConstant.province_unknown)) {
                            return false; //未知地理位置 不做过滤
                        }
                        return true;
                    }
                }
            }

            String readableFeatures = doc.getReadableFeatures();
            if(StringUtils.isNotBlank(readableFeatures)&&readableFeatures.contains("loc")){
                Pattern p = Pattern.compile("loc=(\\W+)\\|!\\|");
                Matcher m = p.matcher(readableFeatures);
                if (m.find()) {
                    String a = m.group(1);
                    if (GyConstant.province_unknown.equals(a)) {
                        return false; //未知地理位置 不做过滤
                    }

                    if (StringUtils.isNotBlank(loc)) {
                        return !loc.contains(a); //用户地理位置 包含文章城市 返回 false 不做过滤
                    } else {
                        return true;
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return false;
    };


    /**
     * 负反馈过滤
     */
    public RuleHandlerInterface feedBack = (document, requestInfo) -> {
        Document input = (Document) document;
        Map<String, List<String>> negMaps = ((RequestInfo) requestInfo).getNegMaps(); //TODO 负反馈过滤 来源？

        if (input == null || negMaps == null || StringUtils.isBlank(input.getReadableFeatures()) || negMaps.isEmpty()) //特征词
            return true;

        Map<String, List<String>> docFeaturesMap = ToolAssit.getFeatures(input.getReadableFeatures());
        if (docFeaturesMap == null || docFeaturesMap.isEmpty())
            return true;

        //先判断是否过滤来源
        if (negMaps.containsKey(GyConstant.UserNegs_Src) && StringUtils.isNotBlank(input.getSource())) {
            List<String> sourceList = negMaps.get(GyConstant.UserNegs_Src);
            if (sourceList.contains(input.getSource())) {
                return false;
            }
        }

        for (String str : negMaps.keySet()) {
            if (!docFeaturesMap.containsKey(str))
                continue;
            List<String> negfeaturelist = negMaps.get(str);
            List<String> docfeaturelist = docFeaturesMap.get(str);  //通过Document得到文章featurelist
            List<String> intersectionList = ListUtil.getIntersection(docfeaturelist, negfeaturelist);
            if (intersectionList != null && !intersectionList.isEmpty()) {
                return false;
            }
        }

        return true;
    };

    /**
     * 关键词过滤，安全通过（不需要被过滤）返回true，无法通过（需要被过滤）返回false
     */
    public RuleHandlerInterface keyword = (documentP, keywordMapP) -> {
        Document document = (Document) documentP;
        Map<String,Set<String>> keywordMap = (Map<String,Set<String>>) keywordMapP;
        //--------未来可将doc.getTitle().contains 优化为分词后匹配------
        for(Map.Entry<String,Set<String>> entry:keywordMap.entrySet()){
            Set<String> keywordSet = entry.getValue();
            for (String keyword : keywordSet) {
                if(document.getTitle().contains(keyword)){
                    return false;
                }
            }
        }

        return true;
    };

    /**
     * doc source过滤，安全通过（不需要被过滤）返回true，无法通过（需要被过滤）返回false
     */
    public RuleHandlerInterface docSource = (documentP, docSourceMapP) -> {
        Document document = (Document) documentP;
        String source=document.getSource();
        if (StringUtils.isBlank(source)) {
            return true;
        }

        Map<String,Set<String>> docSourceMap = (Map<String,Set<String>>) docSourceMapP;
        //--------未来可将doc.getTitle().contains 优化为分词后匹配------
        for(Map.Entry<String,Set<String>> entry:docSourceMap.entrySet()){
            Set<String> docSourceSet = entry.getValue();
            for (String docSource : docSourceSet) {
                if(document.getSource().equals(docSource)){
                    return false;
                }
            }
        }

        return true;
    };

    /**
     * 集合过滤
     * input -> 待过滤文章列表
     * collection -> 过滤集合
     */
    public RuleHandlerInterface1 removeDup = (input, collection) -> {
        List<Document> docs = (List<Document>) input;
        List<Document> collections = (List<Document>) collection;
        if (CollectionUtils.isEmpty(docs) || CollectionUtils.isEmpty(collections)) return docs;
        Set<String> simids = collections.stream().map(x -> x.getSimId()).collect(Collectors.toSet());
        return docs.stream().filter(x -> !simids.contains(x.getSimId())).collect(Collectors.toList());
    };

}
