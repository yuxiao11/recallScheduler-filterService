package com.ifeng.recallScheduler.itemUtil;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.reflect.TypeToken;
import com.ifeng.recallScheduler.utils.ToolAssit;
import com.ifeng.recallScheduler.constant.DocConstant;
import com.ifeng.recallScheduler.constant.GyConstant;
import com.ifeng.recallScheduler.enums.DocType;
import com.ifeng.recallScheduler.enums.SlideType;
import com.ifeng.recallScheduler.item.Document;
import com.ifeng.recallScheduler.logUtil.ServiceLogUtil;
import com.ifeng.recallScheduler.utils.DateUtils;
import com.ifeng.recallScheduler.utils.JsonUtil;
import com.ifeng.recallScheduler.utils.MathUtil;
import com.ifeng.recallScheduler.utils.StringUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.util.Bytes;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Created by jibin on 2017/7/30.
 */
@Service
public class DocClient {

    protected static final Logger log = LoggerFactory.getLogger(DocClient.class);

    private static Configuration conf = HBaseConfiguration.create();
    private static Connection con = null;
    private static String familyName = "info";
    private static String columnName = "jsonItemf";
    private static String familyNameF1 = "f1";
    private static final int HASH_CODE = 499;
    public static final String CONTENT_TableName = "news_itemf";
    public static final String INDEX_TableName = "news_itemf_index";

    private static final String clientPort = "2181";

    //新集群
//    private static final String quorum = "10.80.71.148,10.80.72.148,10.80.73.148,10.80.74.148,10.80.75.148";


    //kernel hbase
    private static final String quorum = "10.80.85.138,10.80.89.138,10.80.72.140,10.80.86.140,10.80.75.141";

    //老集群，备用
//    private static final String quorum = "10.80.5.155,10.80.6.155,10.80.7.155,10.80.8.155,10.80.9.155";



    //线下测试使用测试集群，性能很差，不要提交到线上
//    private static final String quorum = "10.80.5.155,10.80.6.155,10.80.7.155,10.80.8.155,10.80.9.155";

    private static String[] HBASE_COLUMNS = {
            DocConstant.Doc_Title,
            DocConstant.Doc_TimeSensitive,
            DocConstant.Doc_Time,
            DocConstant.Doc_Type,
            DocConstant.Doc_Other,
            DocConstant.Doc_SimId,
            DocConstant.Doc_GroupID,
            DocConstant.Doc_MediaId,
            DocConstant.Doc_Source,
            DocConstant.Doc_Url,
            DocConstant.Doc_Features,
            DocConstant.Doc_SpecialParam,
            DocConstant.Doc_PicFingerPrint,
            DocConstant.Doc_Category,
            DocConstant.Doc_rTag,
            DocConstant.Doc_MultiHead,
            DocConstant.Doc_DisType,
            DocConstant.Doc_sourceLevel,
            DocConstant.Doc_LocList,
            DocConstant.Doc_IsIfengVideo,
            DocConstant.Doc_PartCategory,
            DocConstant.Doc_PartCategoryExt,
            DocConstant.Doc_ExpireTime,
            DocConstant.Doc_LdaTopic,
            DocConstant.Doc_Performance
    };

    static {
        getConnection();
    }

    private static void getConnection() {
        conf.set("hbase.zookeeper.property.clientPort", clientPort);
        conf.set("hbase.zookeeper.quorum", quorum);

        conf.set("hbase.client.retries.number", "2");
        conf.set("hbase.rpc.timeout", "400");
        conf.set("hbase.client.operation.timeout", "600");

        try {
            con = ConnectionFactory.createConnection(conf);
        } catch (Exception e) {
            log.error("[HBase Init ERROR:{}" + e);
            e.printStackTrace();
        }
    }


    /**
     * 若多线程调用，应当创建自己独立的table并结束时释放
     */
    public Table getDocTable(String tableName) {
        Table reTable = null;
        try {
            if (con == null) {
                getConnection();
            }
            reTable = con.getTable(TableName.valueOf(tableName));
        } catch (Exception e) {
            log.error("init {} table error{}", tableName, e);
        }
        return reTable;
    }

    /**
     * 对传入的rowKey hash化避免热点问题
     *
     * @param rowKey
     * @return
     */
    private String getHashedID(String rowKey) {
        if (rowKey == null || rowKey.isEmpty()) {
            return null;
        }
        try {
            byte[] btInput = rowKey.getBytes();
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(btInput);
            byte[] resultByteArray = messageDigest.digest();
            int i = 0;
            for (int offset = 0; offset < resultByteArray.length; offset++) {
                i += Math.abs(resultByteArray[offset]);
            }

            int prefix = 1000 + i % HASH_CODE;

            StringBuilder sb = new StringBuilder();
            sb.append(prefix).append(GyConstant.Symb_Underline).append(rowKey);
            btInput = null;
            resultByteArray = null;
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("HBase Exception while getting hashed ID!!  {}",
                    e);
//            e.printStackTrace();
            return null;

        }

    }


    /**
     * 查询item库，获取item详细信息
     *
     * @param keyMap
     * @param cntTable
     * @return
     */
    public Map<String, Itemf> getItemBatch(Map<String, String> keyMap, Table cntTable, String[] columns) {

        Map<String, Itemf> reMap = Maps.newHashMap();
        List<Get> listGets = new ArrayList<Get>();
        long start = System.currentTimeMillis();
        try {
            for (String key : keyMap.keySet()) {
                try {
                    String cntID = keyMap.get(key);
                    String id = cntID.replace("cmpp_", "");
                    String rowKey = getHashedID(cntID);
                    if (StringUtils.isBlank(rowKey)) {
                        log.error("key get rowKey error: {} " + key);
                        continue;
                    }
                    Get get = new Get(Bytes.toBytes(rowKey));
                    for (String column : columns) {
                        get.addColumn(Bytes.toBytes(familyNameF1), Bytes.toBytes(column));
                    }
                    listGets.add(get);
                } catch (Exception e) {
                    continue;
                }
            }

            Result[] cntlist = cntTable.get(listGets);
            if (cntlist == null || cntlist.length < 1) {
                log.error("batch get cntlist list error.");
            } else {
                for (Result result : cntlist) {
                    try {
                        Map<String, String> map = new HashMap<>();
                        String docId = Bytes.toString(result.getRow());
                        for (String column : columns) {
                            String value = Bytes.toString(result.getValue(Bytes.toBytes(familyNameF1), Bytes.toBytes(column)));
                            if (Strings.isBlank(value)) {
                                continue;
                            }
                            map.put(column, value);
                        }
                        if (map.size() > 0) {
                            Itemf item = convertToItemf(map);
                            String key = Bytes.toString(result.getRow());
                            if (key != null && key.contains("_")) {
                                key = key.substring(key.indexOf("_") + 1);
                                reMap.put(key, item);
                            }
                        }
                    } catch (Exception e) {
                        log.error("batch getItem error {}", e);
                        continue;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("batch getItemBatch list error {}", e);
        } finally {
            long cost = System.currentTimeMillis() - start;
            ServiceLogUtil.debug("size:{},getItemByHbaseCost:{}", keyMap.size(), cost);
        }
        return reMap;
    }


    /**
     * 将map转换为Itemf
     *
     * @param map
     * @return
     */
    private Itemf convertToItemf(Map<String, String> map) {
        Itemf itemf = new Itemf();
        itemf.setTitle(map.get(DocConstant.Doc_Title));
        itemf.setPublishedTime(map.get(DocConstant.Doc_Time));
        itemf.setDocType(map.get(DocConstant.Doc_Type));
        itemf.setOther(map.get(DocConstant.Doc_Other));
        itemf.setSimId(map.get(DocConstant.Doc_SimId));
        itemf.setGroupId(map.get(DocConstant.Doc_GroupID));
        itemf.setMediaId(map.get(DocConstant.Doc_MediaId));
        itemf.setSource(map.get(DocConstant.Doc_Source));
        itemf.setSourceLevel(map.get(DocConstant.Doc_sourceLevel));
        itemf.setUrl(map.get(DocConstant.Doc_Url));
        itemf.setDisType(map.get(DocConstant.Doc_DisType));
        itemf.setIsIfengVideo(map.get(DocConstant.Doc_IsIfengVideo));
        itemf.setPartCategory(map.get(DocConstant.Doc_PartCategory));
        itemf.setPartCategoryExt(map.get(DocConstant.Doc_PartCategoryExt));
        itemf.setTimeSensitive(map.get(DocConstant.Doc_TimeSensitive));
        itemf.setExpireTime(map.get(DocConstant.Doc_ExpireTime));
        itemf.setRtag(map.get(DocConstant.Doc_rTag));

        String json = map.getOrDefault(DocConstant.Doc_Features, "[]");
        String locListJson = map.getOrDefault(DocConstant.Doc_LocList, "[]");

        ArrayList<Map<String, String>> featureMapList = new ArrayList<>();
        ArrayList<Map<String, String>> locListMapList = new ArrayList<>();
        try {
            featureMapList = JsonUtil.json2Object(json, new TypeToken<ArrayList<Map<String, String>>>() {}.getType());
            locListMapList = JsonUtil.json2Object(locListJson, new TypeToken<ArrayList<Map<String, String>>>() {}.getType());

        } catch (Exception e) {
            e.printStackTrace();
            log.error("convertToItemf error {}", e);
        }
        itemf.setSpecialParam(map.get(DocConstant.Doc_SpecialParam));
        itemf.setPicFingerprint(map.get(DocConstant.Doc_PicFingerPrint));
        ArrayList<String> categoryList = new ArrayList<>();
        String categoryStr = map.get(DocConstant.Doc_Category);
        if(StringUtils.isNotBlank(categoryStr)) {
            try {
                categoryStr = categoryStr.replace("[", "").replace("]", "");
                String[] categoryArr = categoryStr.split(",");
                categoryList.addAll(Arrays.asList(categoryArr));
            } catch (Exception e) {
                log.error("parse category str error: {}", e);
            }
        }
        itemf.setCategory(categoryList);
        itemf.setMultihead(map.get(DocConstant.Doc_MultiHead));
        itemf.setLda_topic(map.get(DocConstant.Doc_LdaTopic));
        if(map.get(DocConstant.Doc_Performance)!=null){
            itemf.setPerformance(map.get(DocConstant.Doc_Performance));
        }
        itemf = generateReadableFeature(itemf, featureMapList);
        itemf = generateLocMap(itemf, locListMapList);
        return itemf;
    }

    /**
     * 亚希提供的解析ReadableFeature的方法，按需要从hbase的features字段中截断
     *
     * @param item
     * @return
     */
    private Itemf generateReadableFeature(Itemf item, ArrayList<Map<String, String>> featureMapList ) {
        StringBuffer sbTmp = new StringBuffer();
        if (featureMapList == null || featureMapList.isEmpty()) return item;
        for (Map<String,String> map : featureMapList) {
            String feature = map.get("word");
            String type = map.get("type");
            float weight = 0f;
            try {
                weight = Float.valueOf(map.get("weight"));
            } catch (Exception e) {
                weight = 0f;
                e.printStackTrace();
            }
            //不可读，暂时不加入可读化表达
            if (Math.abs(weight) < 0.5f)
                continue;

            if (type.equals("c")
                    || type.equals("sc")
                    || type.equals("cn")
                    || type.equals("t")
                    || type.equals("e")
                    || type.equals("s1")) {
                sbTmp.append(type).append("=").append(feature).append("|!|");
            }
            if (type.equals("kb")) {
                sbTmp.append(type).append("=《").append(feature).append("》|!|");
            }
            if ((type.equals("loc") || type.equals("et") || type.equals("k") || type.equals("x"))
                    && weight >= 0.5) {
                sbTmp.append(type).append("=").append(feature).append("|!|");
            }
        }
        String readableFeatures = sbTmp.toString();
        item.setReadableFeatures(readableFeatures);
        return item;
    }

    /**
     * set 文章地理位置信息 拆分为 省和城市两个set集合
     * @param item
     * @param locListMapList
     * @return
     */
    private Itemf generateLocMap(Itemf item, ArrayList<Map<String, String>> locListMapList) {
        HashSet<String> provinceSet = new HashSet<String>();
        HashSet<String> citySet = new HashSet<String>();
        HashMap<String, HashSet<String>> locMap = new HashMap<String, HashSet<String>>();

        if (locListMapList == null || locListMapList.isEmpty()) return item;

        for (Map<String,String> map : locListMapList) {
            String locationStr = map.get("loc");
            float weight = 0f;
            try {
                weight = Float.valueOf(map.get("weight"));
            } catch (Exception e) {
                weight = 0f;
                e.printStackTrace();
            }
            //不可读，暂时不加入可读化表达
            if (Math.abs(weight) < 0.5f)
                continue;

            if (locationStr != null && !locationStr.isEmpty() && weight >= 0.5) {
                String[] locationArr = locationStr.split("->");
                if (locationArr.length > 0 && locationArr[0] != null && !locationArr[0].isEmpty()){
                    provinceSet.add(locationArr[0]);
                    if (locationArr.length > 1 && null != locationArr[1] && !locationArr[1].isEmpty()){
                        citySet.add(locationArr[1]);
                    }
                }
            }
        }

        if (!provinceSet.isEmpty()){
            locMap.put(DocConstant.Doc_LocMap_Province, provinceSet);
            locMap.put(DocConstant.Doc_LocMap_City, citySet);
        }

        item.setLocMap(locMap);
        return item;
    }


    /**
     * 第一步，先进行id转换，查询index库
     *
     * @param keys
     * @param indexTable
     * @return
     */
    public Map<String, String> getIndexBatch(List<String> keys, Table indexTable) {
        Map<String, String> reMap = Maps.newHashMap();
        //添加默认值
        for (String key : keys) {
            reMap.put(key, key);
        }

        List<Get> listGets = Lists.newArrayList();
        try {
            for (String key : keys) {
                try {
                    String rowKey = getHashedID(key);
                    if (rowKey == null) {
                        log.error("key get rowKey error: {} " + key);
                        continue;
                    }
                    Get get = new Get(Bytes.toBytes(rowKey));
                    listGets.add(get);
                } catch (Exception e) {
                    continue;
                }
            }

            Result[] indexlist = indexTable.get(listGets);

            if (indexlist == null || indexlist.length < 1) {
                log.error("batch get doc list error.");
            } else {
                for (int i = 0; i < indexlist.length; i++) {
                    Result result = indexlist[i];
                    try {
                        String cntID = Bytes.toString(result.getValue(
                                Bytes.toBytes(familyName),
                                Bytes.toBytes(columnName)));

                        if (StringUtils.isNotBlank(cntID)) {
                            cntID = cntID.replace("\"", "");
                            String key = Bytes.toString(result.getRow());
                            if (key != null && key.contains("_")) {
                                key = key.substring(key.indexOf("_") + 1);
                                reMap.put(key, cntID);
                            }
                        }
                    } catch (Exception e) {
                        log.error("Index parse {} ERROR:{}", keys.get(i), e);
//                        e.printStackTrace();
                        continue;
                    }
                }
            }
        } catch (Exception e) {
//            e.printStackTrace();
            log.error("getIndexBatch {} ERROR:", keys.toString(), e);
        }
        return reMap;
    }


    /**
     * 将ikv的item对象转换为Document
     *
     * @param docid
     * @param item
     * @return
     */
    public Document convertToDoc(String docid, Itemf item) {
        if (item == null) {
            return null;
        }
        Document doc = new Document();
        doc.setDocId(docid);
        doc.setTitle(item.getTitle());
        String publishedTime = item.getPublishedTime();
        if (StringUtils.isNotBlank(publishedTime)) {
            doc.setDate(DateUtils.strToDate(publishedTime));
        }
        doc.setDocType(item.getDocType());
        doc.setReadableFeatures(item.getReadableFeatures());
        doc.setPicFingerprint(item.getPicFingerprint());
        try{
            if(StringUtils.isNotBlank(item.getExpireTime())){
                doc.setTimeSensitive(item.getExpireTime());
            }else{
                doc.setTimeSensitive(item.getTimeSensitive());
            }
        }catch (Exception e){
            log.error("docid:{} convertToDoc error:{}",docid,e);
        }


        String disType = item.getDisType();
        doc.setDisType(disType);
        doc.setSourceLevel(item.getSourceLevel());
        doc.setCategory(item.getCategory());
        doc.setIsIfengVideo(item.getIsIfengVideo());
        doc.setRtag(item.getRtag());

        if (StringUtils.isNotBlank(item.getPartCategory())) {
            doc.setPartCategory(item.getPartCategory());
        }


        if (StringUtils.isNotBlank(item.getPartCategoryExt())) {
            doc.setPartCategoryExt(item.getPartCategoryExt());
        }


        HashMap<String, HashSet<String>> locMap = item.getLocMap();
        if (locMap != null && !locMap.isEmpty()){
            doc.setLocMap(locMap);
        }

        if (StringUtils.isBlank(item.getSimId())) {
            log.error("simId is null!!! docid:{}", docid);
        }

        if (StringUtils.isBlank(item.getPicFingerprint()) || GyConstant.nullStr.equals(item.getPicFingerprint())) {
            //为空的太多，少打一点日志
            if (MathUtil.getNum(100) < 2) {
                log.error("picFingerprint is null!!! docid:{},picFingerprint:{}", docid, item.getPicFingerprint());
            }
        }

        if(StringUtils.isNotBlank(item.getLda_topic())){
            doc.setLda_topic(item.getLda_topic());
            doc.setLdaTopicList(dealLdaTopicAsList(item.getLda_topic()));
        }

        if(StringUtils.isNotBlank(item.getPerformance())){
            doc.setPerformance(item.getPerformance());
        }
        doc.setSimId(item.getSimId());
        doc.setGroupId(item.getGroupId());
        doc.setMediaId(item.getMediaId());

        StringBuilder other = new StringBuilder();
        other.append("simID=").append(item.getSimId()).append(GyConstant.Symb_Split_IKV);
        other.append("url=").append(item.getUrl()).append(GyConstant.Symb_Split_IKV).append(item.getAuthLabel());
        doc.setOthers(other.toString());
        doc.setUrl(item.getUrl());

        //晓伟透传字段处理
        dealOtherStr(doc);
        //晓伟透传ext处理
        dealExt(doc, item);
        //标记双标题文章
        markDoubleTitle(doc, item);


        //文档类型为直播live时, 透传SpecialParam到others字段
        if (DocType.Live.getValue().equals(doc.getDocType())) {
            if (StringUtils.isBlank(item.getSpecialParam())) {
                doc.setOthers("");
            } else {
                doc.setOthers(item.getSpecialParam());
            }
        }

        //对doc添加online字段
        dealOnline(doc,item);

        doc.setSpecialParam(item.getSpecialParam());
        dealDevExtSlideType(doc, item);
        doc.setSource(item.getSource());
        dealFeatures(doc);

        String wemediaLevel = ToolAssit.getWemediaLevel(item.getOther());
        doc.setWemediaLevel(wemediaLevel);

        return doc;
    }


    /**
     * 对doc添加online字段
     *
     * @param doc
     */
    private void dealOnline(Document doc,Itemf item ){
        String other = item.getOther();
        String online = "";
        try {
            if(other != null){
                String[] otherArray = other.split("\\|!\\|");
                if(otherArray.length > 0){
                    for(String string : otherArray){
                        if(string.startsWith("online") && string.contains("=")){
                            String[] onlineArray = string.split("=");
                            if(onlineArray.length == 2){

                                online = onlineArray[1];
                            }
                        }
                    }
                }
            }
        }catch (Exception e){
            log.error("docid:{},simid:{},title:{},other:{},split doc other attribute error, Expcetion {}",doc.getDocId(),doc.getSimId(),doc.getTitle(),item.getOther(),e);
        }
        doc.setOnLine(online);
    }

    /**
     * 处理大图精品池逻辑,添加标记
     *
     * @param doc
     */
    private void dealDevExtSlideType(Document doc, Itemf item) {
        if (DocType.Slide.getValue().equals(doc.getDocType()) || DocType.Docpic.getValue().equals(doc.getDocType())) {
            String other = item.getOther();
            String slideType = null;
            if (other != null) {
                if (other.contains(SlideType.CxBigPicPool.getValue())) {
                    slideType = SlideType.CxBigPicPool.getValue();
                    dealBigPicPoolExt(doc, slideType);
                } else if (other.contains(SlideType.JpBigPicPool.getValue())) {
                    slideType = SlideType.JpBigPicPool.getValue();
                    dealBigPicPoolExt(doc, slideType);
                }
            }
            if (slideType != null) {
                Map<String, String> devExt = doc.getDevExt();
                if (devExt == null) {
                    devExt = new HashMap<>();
                }
                devExt.put(GyConstant.Key_SlideType, slideType);
                doc.setDevExt(devExt);
            }
        }
    }


    /**
     * 大图精品池打ext 上
     *
     * @param doc
     * @param slideType
     */
    private void dealBigPicPoolExt(Document doc, String slideType) {
        Map<String, Object> ext = doc.getExt();
        if (ext == null) {
            ext = Maps.newHashMap();
        }
        Map<String, String> fromChannel = Maps.newHashMap();
        fromChannel.put("fromChannel", slideType);
        String json = "{}";
        try {
            json = JsonUtil.object2jsonWithoutException(fromChannel);
        } catch (Exception e) {
            log.error("Convert BigPicPool to Json error, Expcetion {}", e);
        }
        ext.put(GyConstant.specialParam, json);
        doc.setExt(ext);
    }

    /**
     * 根据docid批量查询Document
     *
     * @param docIds
     * @param cntTable
     * @param indexTable
     * @return
     */
    public Map<String, Document> getDocBatch(List<String> docIds, Table cntTable, Table indexTable) {
        Map<String, Document> result = Maps.newHashMap();
        try {
            Map<String, String> ikvIdMap = Maps.newHashMap();
            List<String> ikvIdList = Lists.newArrayList();
            for (String docid : docIds) {
                if(StringUtils.isBlank(docid)){
                    continue;
                }

                String ikvid = null;
                if (docid.length() < GyConstant.GUID_Length && StringUtil.startWithNum(docid)) {
                    ikvid = GyConstant.IKV_Prefix + docid;
                } else {
                    ikvid = docid;
                }
                ikvIdMap.put(docid, ikvid);
                ikvIdList.add(ikvid);
            }


            Map<String, String> indexMap = getIndexBatch(ikvIdList, indexTable);

            //拆分成小段来查询

            List<Map<String, String>> indexMapList = getIndexMapList(indexMap, 100);

            Map<String, Itemf> itemResult = Maps.newHashMap();

            long start = System.currentTimeMillis();
            for (Map<String, String> tmp : indexMapList) {
                Map<String, Itemf> itemMap = getItemBatch(tmp, cntTable, HBASE_COLUMNS);
                if (itemMap != null && itemMap.size() > 0) {
                    itemResult.putAll(itemMap);
                }

                long cost = System.currentTimeMillis() - start;
                if (cost > GyConstant.MaxTimeOut_QueryHbaseDocOnline) {
                    log.info("getItemBatch break,size:{},cost:{}", docIds.size(), cost);
                    break;
                }

            }

            for (String docid : docIds) {
                String ikvid = ikvIdMap.get(docid);
                String indexid = indexMap.get(ikvid);
                Itemf item = itemResult.get(indexid);


                if (item == null) {
                    continue;
                }

                Document doc = convertToDoc(docid, item);
                result.put(docid, doc);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("getDocBatch ERROR:{}", e);
        }
        return result;
    }


    /**
     * 拆分成小段来查询,限定一批40个
     *
     * @param indexMap
     * @param maxsize
     * @return
     */
    private List<Map<String, String>> getIndexMapList(Map<String, String> indexMap, int maxsize) {
        List<Map<String, String>> indexMapList = Lists.newArrayList();

        Map<String, String> tmp = Maps.newHashMap();
        for (String ikvid : indexMap.keySet()) {
            if (tmp.size() > maxsize) {
                indexMapList.add(tmp);
                tmp = Maps.newHashMap();
            }
            tmp.put(ikvid, indexMap.get(ikvid));
        }
        if (tmp.size() > 0) {
            indexMapList.add(tmp);
        }
        return indexMapList;
    }


    /**
     * 补全doc的ikv特征信息
     *
     * @param doc
     */
    public static void dealFeatures(Document doc) {
        Map<String, List<String>> features = ToolAssit.getFeatures(doc.getReadableFeatures());
        ToolAssit.fillFeature(doc, features);
    }

    /**
     * 晓伟透传字段处理,处理字符串里面的字符串：例如:
     * "title":"今天"真开心"的大，妈"
     *
     * @param doc
     */
    public static void dealOtherStr(Document doc) {
        String othersNew = getOthersNew(doc);
        if (StringUtils.isNotBlank(othersNew)) {
            doc.setOthers(othersNew);
        }
    }

    /**
     * 透传晓伟字段,将透传内容放到ext中
     */
    public static void dealExt(Document doc, Itemf itemf) {
        doc.setSpecialParam(itemf.getSpecialParam());

        //将specialParam放至扩展字段
        if (StringUtils.isNotBlank(itemf.getSpecialParam())) {
            Map<String, Object> ext = doc.getExt();
            if (ext == null) {
                ext = Maps.newHashMap();
                doc.setExt(ext);
            }
            ext.put(GyConstant.specialParam, itemf.getSpecialParam());
        }

    }

    /**
     * 处理解析双标题
     *
     * @param doc
     * @param itemf
     */
    public static void markDoubleTitle(Document doc, Itemf itemf) {
        String multihead = itemf.getMultihead();

        if (StringUtils.isNotBlank(multihead)) {
            try {

                String key = "\"title\":";
                String title2Raw = multihead.substring(multihead.lastIndexOf(key) + key.length(), multihead.length());
                String title2 = title2Raw.substring(0, title2Raw.indexOf(","));

                //提取title2字符串，判断是否是双标题
                if (!title2.equals("\"\"")) {
                    doc.setIsDoubleTitle(true);
                }

            } catch (Exception e) {
                log.error("parse Hbase multihead failed, multihead:{}, error:{}", multihead, e);
            }
        }
    }

    /**
     * 去除other中的特殊符号：
     * "title":"今天"真开心"的大，妈"
     *
     * @param doc
     */
    public static String getOthersNew(Document doc) {
        String othersNew = null;
        if (doc != null) {
            String othersOld = doc.getOthers();
            if (StringUtils.isNotBlank(othersOld)) {
                othersNew = StringEscapeUtils.unescapeJava(othersOld);
            }
        }
        return othersNew;
    }



    /**
     * ldaTopic json 字符串 转化为 list 集合
     * @param ldaTopicJson ldaTopicJson
     * @return list
     */
    private List<String> dealLdaTopicAsList(String ldaTopicJson){
        List<String> ldaTopicList = new ArrayList<>();
        try{
            //json 转码
            List<LdaTopicItem> ldaTopicItems = JsonUtil.json2Object(ldaTopicJson,new TypeToken<List<LdaTopicItem>>() {}.getType());
            if(CollectionUtils.isNotEmpty(ldaTopicItems) && ldaTopicItems.size() > 0){
                //多个 ldaTopic 需要遍历
                for (LdaTopicItem ldaItem : ldaTopicItems) {
                    String ldaTopic = ldaItem.getTopic();
                    if (org.apache.commons.lang3.StringUtils.isNotBlank(ldaTopic)){
                        ldaTopicList.add(ldaTopic);
                    }
                }
            }
        }catch (Exception e){
            log.error("DealLdaTopicAsList Err, ldaTopicJson:{}, error:{}", ldaTopicJson, e.toString(), e);
        }

        return ldaTopicList;
    }


    static class LdaTopicItem{
        public String topic;
        public String weight;

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public String getWeight() {
            return weight;
        }

        public void setWeight(String weight) {
            this.weight = weight;
        }

    }





    public static void main(String[] args) {
        String row_key = new DocClient().getHashedID("11076678");
        System.out.println(row_key);

    }
}