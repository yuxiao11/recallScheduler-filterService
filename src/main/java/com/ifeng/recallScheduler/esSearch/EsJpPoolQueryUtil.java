package com.ifeng.recallScheduler.esSearch;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.ifeng.recallScheduler.bloomFilter.BloomFilter;
import com.ifeng.recallScheduler.constant.GyConstant;
import com.ifeng.recallScheduler.item.Document;
import com.ifeng.recallScheduler.logUtil.ServiceLogUtil;
import com.ifeng.recallScheduler.logUtil.StackTraceUtil;
import com.ifeng.recallScheduler.request.RequestInfo;
import com.ifeng.recallScheduler.user.RecordInfo;
import com.ifeng.recallScheduler.user.UserModel;
import com.ifeng.recallScheduler.utils.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.common.lucene.search.function.FunctionScoreQuery;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import javax.swing.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.ifeng.recallScheduler.constant.GyConstant.Symb_Split_IKV;
import static com.ifeng.recallScheduler.utils.UserUtils.weightFilter;
import static org.elasticsearch.index.query.QueryBuilders.*;


/**
 * Created by lilg1 on 2018/5/11.
 */

@Service
public class EsJpPoolQueryUtil {

    private final static Logger logger = LoggerFactory.getLogger(EsJpPoolQueryUtil.class);

    private final static String INDEX = "preload-jppool";

    private final static String TYPE = "all";

    private final static String[] needFetchSource = {"docId",
            "title",
            "date",
            "docType",
            "hotBoost",
            "other",
            "simId",
            "source",
            "updatetime",
            "importDate",
            "groupId",
            "url",
            "timeSensitive",
            "topic1",
            "specialParam",
            "partCategory"};

    private final static String[] noNeedFetchSource = {};


    @Autowired
    private TransportClient client;


    /**
     * 获取所有的精品池文档
     *
     * @param size
     * @param hours
     * @return
     */
    public List<Document> getAllJpPoolDocuments(int size, int hours) {
        logger.info("start loading jpPool");
        QueryBuilder qb = termQuery("other", "jpPool");
        SortBuilder sort = SortBuilders.fieldSort("hotBoost").order(SortOrder.DESC);
        List<Document> result = queryDocuments(size, hours, qb, sort);
        logger.info("end loading jpPool");
        return result;
    }

    /**
     * 获取所有的长效精品池文档
     *
     * @param size
     * @param hours
     * @return
     */
    public List<Document> getCxJpPoolDocuments(int size, int hours) {
        logger.info("start loading jpPool");
        QueryBuilder qb = termQuery("other", "jpCx");
        SortBuilder sort = SortBuilders.fieldSort("hotBoost").order(SortOrder.DESC);
        List<Document> result = queryDocuments(size, hours, qb, sort);
        logger.info("end loading jpPool");
        return result;
    }

    /**
     * 获取所有的精品池焦点图数据
     *
     * @param size
     * @param hours
     * @return
     */
    public List<Document> getAllFocusPicDocument(int size, int hours) {
        logger.info("start loading jp focus");
        QueryBuilder qb = termQuery("other", "focuspic");
        SortBuilder sort = SortBuilders.fieldSort("date").order(SortOrder.DESC);
        List<Document> result = queryDocuments(size, hours, qb, sort);
        logger.info("end loading jp focus");
        return result;
    }


    public List<Document> getAllJpPreDocument(int size, int hours) {
        RangeQueryBuilder dateRange = rangeQuery("date").gte("now-" + hours + "h/h");

        SortBuilder sort = SortBuilders.fieldSort("date").order(SortOrder.DESC);
        QueryBuilder qb = boolQuery()
                .must(dateRange)
                .must(termQuery("other", "top_can"))
                .mustNot(termsQuery("other", "focuspic"))
                .must(termQuery("available", true));

        SearchResponse searchResponse = client.prepareSearch(INDEX)
                .setFetchSource(needFetchSource, noNeedFetchSource)
                .setQuery(qb)
                .addSort(sort)
                .setSize(size).get();

        List<Document> result = new ArrayList<>();

        try {
            convert2Documents(searchResponse, result);
        } catch (Exception e) {
            logger.error("query jpPool err:{}", e);
        }

        return result;
    }


    /**
     * 获取长效的精品池编辑优选数据
     * @param size
     * @return
     */
    public List<Document> getAllJpPreLongDocument(int size) {
        RangeQueryBuilder dateRange = rangeQuery("date").gte("now-168h/h").lte("now-72h/h");

        SortBuilder sort = SortBuilders.fieldSort("date").order(SortOrder.DESC);

        QueryBuilder qb = boolQuery()
                .must(dateRange)
                .must(termQuery("other", "top_can"))
                .mustNot(termsQuery("other", "focuspic"))
                .must(termQuery("available", true));

        SearchResponse searchResponse = client.prepareSearch(INDEX)
                .setFetchSource(needFetchSource, noNeedFetchSource)
                .setQuery(qb)
                .addSort(sort)
                .setSize(size).get();

        List<Document> result = new ArrayList<>();

        try {
            convert2Documents(searchResponse, result);
        } catch (Exception e) {
            logger.error("query jpPool err:{}", e);
        }

        return result;
    }


    /**
     * 获取所有长效精品池文章
     */
    public List<Document> getAllJpLongtermDocument(int size) {
        logger.info("start loading jp longterm");
        QueryBuilder qb = termQuery("other", "jpCx");
        List<Document> result = queryDocuments(size, qb, SortBuilders.fieldSort("hotBoost").order(SortOrder.DESC));
        logger.info("end loading jp longterm");
        return result;
    }


    /**
     * 查询特定时间范围内的精品池ES文章
     *
     * @param size
     * @param hours
     * @param termQuery
     * @return
     */
    private List<Document> queryDocuments(int size, int hours, QueryBuilder termQuery, SortBuilder sort) {
        RangeQueryBuilder dateRange = rangeQuery("date").gte("now-" + hours + "h/h");

        QueryBuilder qb = boolQuery()
                .must(dateRange)
                .must(termQuery)
                .must(termQuery("available", true));

        SearchResponse searchResponse = client.prepareSearch(INDEX)
                .setFetchSource(needFetchSource, noNeedFetchSource)
                .setQuery(qb)
                .addSort(sort)
                .setSize(size).get();

        List<Document> result = new ArrayList<>();

        try {
            convert2Documents(searchResponse, result);
        } catch (Exception e) {
            logger.error("query jpPool err:{}", e);
        }

        return result;
    }

    /**
     * 查询精品池ES,不带时间范围限制
     *
     * @param size
     * @param termQuery
     * @return
     */
    private List<Document> queryDocuments(int size, QueryBuilder termQuery, SortBuilder sort) {
        QueryBuilder qb = boolQuery().must(termQuery);

        SearchResponse searchResponse = client.prepareSearch(INDEX)
                .setFetchSource(needFetchSource, noNeedFetchSource)
                .setQuery(qb)
                .addSort(sort)
                .setSize(size).get();

        List<Document> result = new ArrayList<>();
        try {
            convert2Documents(searchResponse, result);
        } catch (Exception e) {
            logger.error("query all jpPool err:{}", e);
        }
        return result;
    }


    /**
     * @param searchResponse
     * @param result
     * @throws ParseException
     */
    private void convert2Documents(SearchResponse searchResponse, List<Document> result) throws ParseException {

        if (searchResponse == null) {
            return;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            Map<String, Object> map = hit.getSourceAsMap();
            Document document = new Document();
            document.setDocId((String) map.get("docId"));
            document.setTitle((String) map.get("title"));
            String dateStr = (String) map.get("date");
            Date date = sdf.parse(dateStr);
            document.setDate(date);
            String importDateStr = (String) map.get("importDate");
            Date importDate = sdf.parse(importDateStr);
            document.setImportDate(importDate);
            document.setDocType((String) map.get("docType"));
            document.setHotBoost(String.valueOf(map.get("hotBoost")));

            //拼接others字符串
            StringBuilder othersBuilder = new StringBuilder();
            String other = (String) map.get("other");
            String url = (String) map.get("url");
            String simId = (String) map.get("simId");
            document.setSimId(simId);
            othersBuilder.append("simID=").append(simId).append(Symb_Split_IKV)
                    .append("url=").append(url).append(Symb_Split_IKV).append(other);
            document.setOthers(othersBuilder.toString());
            Object groupId = map.get("groupId");
            if (groupId != null) {
                document.setGroupId(String.valueOf(groupId));
            }

            //specialParam有值时设置，主要是直播使用
            String specialParam = (String) map.get("specialParam");
            if (StringUtils.isNotBlank(specialParam)) {
                Map<String, Object> extMap = new HashMap<>();
                extMap.put(GyConstant.specialParam, specialParam);
                document.setExt(extMap);
            }
            document.setHotLevel("");
            document.setDocChannel("");
            document.setScore(hit.getScore() + "");

            document.setSource((String) map.get("source"));
            document.setUpdatetime((Long) map.get("updatetime"));
            document.setTimeSensitive((String) map.get("timeSensitive"));
            if (map.get("partCategory") != null) {
                document.setPartCategory((String) map.get("partCategory"));
            }

            if (map.get("sc") != null) {
                document.setSc((String) map.get("sc"));
            }
            //添加topic1 到 devCList
            String topic1 = map.get("topic1") == null ? "" : (String) map.get("topic1");
            if (StringUtils.isNotBlank(topic1)) {
                topic1 = StringUtils.substringBefore(topic1, GyConstant.Symb_Caret);
                List<String> cList = new ArrayList<String>();
                cList.add(topic1);
                document.setDevCList(cList);
            }
            result.add(document);
        }
    }

    public List<Document> queryDocumentsByPre(int needSize, UserModel userModel, RequestInfo requestInfo) {

        if (userModel == null) {
            return new ArrayList<>();
        }
        String uid = requestInfo.getUserId();
        Map<String, List<RecordInfo>> preMap = new HashMap<>();
        List<RecordInfo> DC_RecordInfos = weightFilter(userModel.getDcRecordList());
        List<RecordInfo> DSC_RecordInfos = weightFilter(userModel.getDscRecordList());
        List<RecordInfo> VC_RecordInfos = weightFilter(userModel.getVcRecordList());
        List<RecordInfo> VSC_RecordInfos = weightFilter(userModel.getVscRecordList());

        List<RecordInfo> DCAllRecordInfos = Lists.newArrayList();
        List<RecordInfo> VCAllRecordInfos = Lists.newArrayList();
        if (CollectionUtils.isEmpty(DC_RecordInfos) && CollectionUtils.isEmpty(DSC_RecordInfos) && CollectionUtils.isEmpty(VC_RecordInfos) && CollectionUtils.isEmpty(VSC_RecordInfos)) {
            return new ArrayList<>();
        }

        preMap.put("dc", DC_RecordInfos);
        preMap.put("dsc", DSC_RecordInfos);
        preMap.put("vc", VC_RecordInfos);
        preMap.put("vsc", VSC_RecordInfos);
        DCAllRecordInfos.addAll(DC_RecordInfos);
        DCAllRecordInfos.addAll(DSC_RecordInfos);
        VCAllRecordInfos.addAll(VC_RecordInfos);
        VCAllRecordInfos.addAll(VSC_RecordInfos);
        //计算 每个标签需要的文章条数
        Map<String, Integer> dTagMaps = dealTagNum(requestInfo, DCAllRecordInfos, 260);
        Map<String, Integer> vTagMaps = dealTagNum(requestInfo, VCAllRecordInfos, 140);
        DebugUtil.debugLog(requestInfo.isDebugUser(), "{} queryDocumentsByPre dTagMaps:{} vTagMaps:{}", requestInfo.getUserId()
                , JsonUtil.object2jsonWithoutException(dTagMaps), JsonUtil.object2jsonWithoutException(vTagMaps));
        List<RecordInfo> allList = new ArrayList<>();
        allList.addAll(DC_RecordInfos);
        allList.addAll(DSC_RecordInfos);
        allList.addAll(VC_RecordInfos);
        allList.addAll(VSC_RecordInfos);

        List<Document> docments = new ArrayList<>();
        for (Map.Entry<String, List<RecordInfo>> entry : preMap.entrySet()) {
            String key = entry.getKey();
            List<RecordInfo> recordInfos = entry.getValue();
            Map<String, Integer> tagMaps = Maps.newHashMap();
            if (key.equals("dc") || key.equals("dsc")) {
                tagMaps = dTagMaps;
            } else {
                tagMaps = vTagMaps;
            }
            if (CollectionUtils.isNotEmpty(recordInfos)) {
                String tag = "";
                for (RecordInfo r : recordInfos) {
                    try {
                        if (r == null || StringUtils.isBlank(r.getRecordName())) {
                            continue;
                        }
                        tag = r.getRecordName();
                        List<Document> docs = JpPoolCacheUtil.getFromCache(tag);
                        if (CollectionUtils.isEmpty(docs)) {
                            continue;
                        }
                        List<Document> resultDocs = new ArrayList<>();
                        int needNum = 0;
                        int count = 0;
                        if (MapUtils.isNotEmpty(tagMaps)) {
                            needNum = tagMaps.get(tag);
                        }
                        for (Document document : docs) {
                            if (count >= needNum) {
                                break;
                            }
                            if (BloomFilter.onlyCheck(uid, document.getSimId(), document.getGroupId())) {
                                continue;
                            }
                            resultDocs.add(document);
                            count++;
                        }
                        if (GyConstant.linuxLocalIp.equals(GyConstant.HotIp) && (key.equals("dc") || key.equals("vc"))) {
                            String docType = key.equals("dc") ? "docpic" : "video";
                            TagLackLogUtil.log("t:{},c:{},r:{}", tag, docType, resultDocs.size());
                        }
                        docments.addAll(resultDocs);
                    } catch (Exception e) {
                        logger.error("{} queryDocumentsByPre key:{} tag:{} dTagMaps:{} vTagMaps:{} getDoc error:{}", requestInfo.getUserId(), key, tag, JsonUtil.object2jsonWithoutException(dTagMaps), JsonUtil.object2jsonWithoutException(vTagMaps), StackTraceUtil.getStackTrace(e));
                    }

                }
            }
        }
        if (CollectionUtils.isNotEmpty(docments)) {
            Collections.sort(docments, DocUtil.hotBoostComparator);
            int subSize = Math.min(100, docments.size());
            DebugUtil.debugLog(requestInfo.isDebugUser(), "queryDocumentsByPre {} allList:{}", requestInfo.getUserId(), new Gson().toJson(allList));
            DebugUtil.debugLog(requestInfo.isDebugUser(), "queryDocumentsByPre {} esJpPool resultList:{}", requestInfo.getUserId(), new Gson().toJson(docments.subList(0, subSize)));
            return docments.subList(0, Math.min(needSize, docments.size()));
        }
        return new ArrayList<>();
    }

    public Map<String, Integer> dealTagNum(RequestInfo requestInfo, List<RecordInfo> recordInfos, int number) {
        Map<String, Integer> resultMap = Maps.newHashMap();
        if (CollectionUtils.isEmpty(recordInfos)) {
            return resultMap;
        }
        long start = System.currentTimeMillis();
        try {
            Map<String, Double> tagAndWeightMap = new HashMap<>(); //画像标签和权重
            for (RecordInfo recordInfo : recordInfos) {
                try {
                    tagAndWeightMap.put(recordInfo.getRecordName(), recordInfo.getWeight());
                } catch (Exception e) {
                    logger.error("{} dealTagNum tagAndWeightMap error:{}", requestInfo.getUserId(), e);
                }
            }
            Set<String> tags = tagAndWeightMap.keySet();
            RecallNumberControl recallNumberControl = new RecallNumberControl(recordInfos, tagAndWeightMap, number);
            for (String tag : tags) {
                int num = recallNumberControl.getRecallNumber(tag); //当前tag应召回的数量
                if (num > 0) {
                    resultMap.put(tag, num);
                } else {
                    resultMap.put(tag, 0);
                }
            }
        } catch (Exception e) {
            logger.error("{} dealTagNum error:{}", requestInfo.getUserId(), e);
        }
        ServiceLogUtil.debug("{},jppool dealTagNum:{}", requestInfo.getUserId(), (System.currentTimeMillis() - start));
        return resultMap;
    }

    /**
     * 根据用户画像获取个性化文章
     *
     * @param size
     * @param hours
     * @param userModel
     * @return
     */
    public List<Document> queryDocumentsByProfile(int size, int hours, UserModel userModel, RequestInfo requestInfo) {

        if (userModel == null) {
            return new ArrayList<>();
        }

        List<RecordInfo> allList = new ArrayList<>();
        List<RecordInfo> DC_RecordInfos = weightFilter(userModel.getDcRecordList());
        List<RecordInfo> DSC_RecordInfos = weightFilter(userModel.getDscRecordList());
        List<RecordInfo> VC_RecordInfos = weightFilter(userModel.getVcRecordList());
        List<RecordInfo> VSC_RecordInfos = weightFilter(userModel.getVscRecordList());

        if (CollectionUtils.isEmpty(DC_RecordInfos) && CollectionUtils.isEmpty(DSC_RecordInfos) && CollectionUtils.isEmpty(VC_RecordInfos) && CollectionUtils.isEmpty(VSC_RecordInfos)) {
            return new ArrayList<>();
        }

        RangeQueryBuilder rangeQuery = rangeQuery("date").gte("now-" + hours + "h/h");
        RangeQueryBuilder rangeQuery2 = rangeQuery("expireTime").gte("now");

        BoolQueryBuilder qb = boolQuery().must(QueryBuilders.constantScoreQuery(rangeQuery2)).must(QueryBuilders.constantScoreQuery(rangeQuery));

        //判断用户 兴趣 t1 是否截取
        if (DC_RecordInfos != null) {
            allList.addAll(DC_RecordInfos);
            for (RecordInfo recordInfo : DC_RecordInfos) {
                qb.should(QueryBuilders.constantScoreQuery(termQuery("c", recordInfo.getRecordName())));
            }
        }

        if (DSC_RecordInfos != null) {
            allList.addAll(DSC_RecordInfos);
            for (RecordInfo recordInfo : DSC_RecordInfos) {
                qb.should(QueryBuilders.constantScoreQuery(termQuery("sc", recordInfo.getRecordName())));
            }
        }

        if (VC_RecordInfos != null) {
            allList.addAll(VC_RecordInfos);
            for (RecordInfo recordInfo : VC_RecordInfos) {
                qb.should(QueryBuilders.constantScoreQuery(termQuery("c", recordInfo.getRecordName())));
            }
        }

        if (VSC_RecordInfos != null) {
            allList.addAll(VSC_RecordInfos);
            for (RecordInfo recordInfo : VSC_RecordInfos) {
                qb.should(QueryBuilders.constantScoreQuery(termQuery("sc", recordInfo.getRecordName())));
            }
        }
        SearchResponse searchResponse = null;
        try {
            qb.minimumShouldMatch(1);
            SearchRequestBuilder searchRequestBuilder = client.prepareSearch(INDEX)
                    .setQuery(qb)
                    .setSize(size)
                    .addSort("_score", SortOrder.DESC).addSort("hotBoost", SortOrder.DESC);
            searchResponse = searchRequestBuilder.get();
        } catch (Exception e) {
            logger.error("query jpPool searchResponse get err:{}", e);
        }


        List<Document> result = new ArrayList<>();
        try {
            convert2Documents(searchResponse, result);
        } catch (Exception e) {
            logger.error("query jpPool By Profile err:{}", e);
        }
        if (CollectionUtils.isNotEmpty(result)) {
            int subSize = Math.min(100, allList.size());
            DebugUtil.debugLog(requestInfo.isDebugUser(), "{} allList:{}", requestInfo.getUserId(), new Gson().toJson(allList));
            DebugUtil.debugLog(requestInfo.isDebugUser(), "{} esJpPool resultList:{}", requestInfo.getUserId(), new Gson().toJson(result.subList(0, subSize)));
        }

        return result;
    }


    public List<Document> queryDocumentsByTag(int size, int hours, String tag, RequestInfo requestInfo) {
        RangeQueryBuilder rangeQuery = rangeQuery("date").gte("now-" + hours + "h/h");
        BoolQueryBuilder qb = boolQuery().must(termQuery("available", true)).must(rangeQuery);

        if (tag != null) {
            qb.should(termQuery("topic1", tag));
        }

        qb.minimumShouldMatch(1);
        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(INDEX)
                .setQuery(qb)
                .setSize(size)
                .addSort("_score", SortOrder.DESC);
        SearchResponse searchResponse = searchRequestBuilder.get();

        List<Document> result = new ArrayList<>();
        try {
            convert2Documents(searchResponse, result);
        } catch (Exception e) {
            logger.error("query jpPool By Profile err:{}", e);
        }

        return result;
    }


    public static void main(String[] args) {
//        EsJpPoolQueryUtil queryUtil = new EsJpPoolQueryUtil();
//
//        UserModelAPI userModelAPI = new UserModelAPI();
//        UserModel userModel = userModelAPI.getUserModelSearch("b040c1cc6bcb416b874850e0a801acc4");
//        List<RecordInfo> t1RecordList = userModel.getVideoSubcateRecordList();
//        for (RecordInfo recordInfo : t1RecordList) {
//            System.out.println(recordInfo.getRecordName() + " " + recordInfo.getWeight());
//        }
//        RequestInfo requestInfo = new RequestInfo();
//        requestInfo.setUserId(userModel.getUserId());
//        List<Document> list = queryUtil.queryDocumentsByProfile(1000, 24, userModel,requestInfo );
//        System.out.println(list);
//        EsJpPoolClient jpPoolClient = new EsJpPoolClient();
//        TransportClient client = jpPoolClient.getClient();

//        List<Document> docs=JpPoolCacheUtil.getFromCache("军事");
//        Collections.sort(docs, DocUtil.hotBoostComparator);
//
//        System.out.println(docs);
        EsJpPoolQueryUtil esJpPoolQueryUtil = new EsJpPoolQueryUtil();
        List<Document> result = esJpPoolQueryUtil.getAllJpPreDocument(1000, 72);
        for (Document doc : result) {
            System.out.println(JsonUtil.object2jsonWithoutException(doc));
        }
    }
}
