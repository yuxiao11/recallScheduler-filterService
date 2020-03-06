package com.ifeng.recallScheduler.rule;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.ifeng.recallScheduler.utils.ToolAssit;
import com.ifeng.recallScheduler.constant.GyConstant;
import com.ifeng.recallScheduler.enums.SlideType;
import com.ifeng.recallScheduler.item.Document;
import com.ifeng.recallScheduler.item.EditorBean;
import com.ifeng.recallScheduler.item.EditorInsertItem;
import com.ifeng.recallScheduler.item.item2app;
import com.ifeng.recallScheduler.itemUtil.DocClient;
import com.ifeng.recallScheduler.utils.DocUtil;
import com.ifeng.recallScheduler.utils.JsonUtil;
import com.ifeng.recallScheduler.utils.LogicDateUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.collections.Maps;

import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 * <PRE>
 * 作用 :
 * 不同的Doc模型对象间的转换
 * 使用 :
 * 调用docList2returnList(List<Document> docList)
 * 示例 :
 * <p>
 * 注意 :
 * <p>
 * 历史 :
 * -----------------------------------------------------------------------------
 * VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 * 1.0          2014-5-22        liu_yi          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class DocModuleConvert {
    private static final Logger log = LoggerFactory.getLogger(DocModuleConvert.class);

    /**
     * 编辑帖子的others字段的cache
     */
    private static LoadingCache<String, String> EditorOthersCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(3, TimeUnit.MINUTES)
            .build(
                    new CacheLoader<String, String>() {
                        public String load(String key) throws Exception {
                            return "";
                        }
                    });



    /**
     * 编辑帖子的others字段获取simid的cache
     */
    private static LoadingCache<String, String> EditorOthersSimIdCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build(
                    new CacheLoader<String, String>() {
                        public String load(String key) throws Exception {
                            return "";
                        }
                    });



    /**
     * 编辑帖子的readableFeatures中的c 分类的cache
     */
    private static LoadingCache<String, Map<String, List<String>>> EditorFeaturesCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build(
                    new CacheLoader<String,  Map<String, List<String>>>() {
                        public  Map<String, List<String>> load(String key) throws Exception {
                            return new HashMap<>();
                        }
                    });




    /**
     * 给doc对象补全其他信息
     * @param doc
     */
    private static void fillEditorDoc(Document doc) {
        //晓伟透传字段处理
        dealEditOthers(doc);


        //转换ReadableFeatures，方便解析
        try {
            Map<String, List<String>> features = EditorFeaturesCache.get(doc.getDocId());
            if (MapUtils.isEmpty(features)) {
                features = ToolAssit.getFeatures(doc.getReadableFeatures());
                if (MapUtils.isNotEmpty(features)) {
                    EditorFeaturesCache.put(doc.getDocId(), features);
                }
            }
            ToolAssit.fillFeature(doc, features);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("fillEditorDoc {} ERROR:{}", doc.getDocId(), e);
        }
    }



    /**
     * solr编辑精选池 转换
     */
    public static List<Document> editorPool2Docs(List<item2app> docList) {

        if (CollectionUtils.isEmpty(docList)) return Collections.EMPTY_LIST;

        List<Document> result = Lists.newArrayList();
        Set<String> idSet = new HashSet<String>(docList.size());
        for (item2app tmpDoc : docList) {
            String docId = tmpDoc.getDocID();

            // 去除 "id重复的"和 TODO "有版权问题的"
            if (idSet.contains(docId)) {
                //如果是专题 帖子，则不用去重
                if (!DocUtil.isTopicDocument(tmpDoc.getDocType())) {
                    continue;
                }
            }

            // 转换一下时间格式
            Date docDate = LogicDateUtil.strToDate(tmpDoc.getDate());

            String simidStr = tmpDoc.getSimId();
            String other = tmpDoc.getOthers();

            if (StringUtils.isBlank(simidStr)) {
                if (StringUtils.isNotBlank(other)) {
                    simidStr = getSimId(docId,other);
                }
            }

            if (StringUtils.isBlank(simidStr)) {
                continue;
            }

            Document tmpValue = new Document();
            tmpValue.setDocId(tmpDoc.getDocID());
            tmpValue.setHotLevel(tmpDoc.getHotLevel());
            tmpValue.setDate(docDate);
            tmpValue.setDocChannel(tmpDoc.getDocChannel());
            tmpValue.setDocType(tmpDoc.getDocType());
            tmpValue.setScore(tmpDoc.getScore());
            tmpValue.setSimId(simidStr);

            tmpValue.setTitle(tmpDoc.getTitle());
            tmpValue.setWhy(tmpDoc.getWhy());
            tmpValue.setHotBoost(tmpDoc.getHotBoost());
            tmpValue.setReadableFeatures(tmpDoc.getReadableFeatures());
            tmpValue.setSource(tmpDoc.getSource());
            tmpValue.setUpdatetime(tmpDoc.getUpdatetime());
            tmpValue.setSpecialParam(tmpDoc.getSpecialParam());

            //透传给晓伟specialParam字段放到ext字段里面
            if(StringUtils.isNotBlank(tmpValue.getSpecialParam())){
                Map<String, Object> ext = tmpValue.getExt();
                if(ext == null){
                    ext = Maps.newHashMap();
                    tmpValue.setExt(ext);
                }
                tmpValue.getExt().put(GyConstant.specialParam,tmpValue.getSpecialParam());

                //龙飞统计字段，统计精品池来源于大图池的内容
                if(tmpValue.getSpecialParam().contains(SlideType.BigPicPool.getValue())){
                    Map<String, String> devExt = tmpValue.getDevExt();
                    if (devExt == null) {
                        devExt = new HashMap<>();
                    }
                    devExt.put(GyConstant.Key_SlideType, SlideType.BigPicPool.getValue());
                    tmpValue.setDevExt(devExt);
                }
            }

//判断直播是否过期
            if (("live".equals(tmpDoc.getDocType()) && LogicDateUtil.expire(docDate, 0))) {
//                    log.info(" 直播过期，过滤 " + tmpDoc.getDocID());
                continue;
            }

            // 若遇到直播/专题 则转化 SpecialParam 为other
            if ("topic".equals(tmpDoc.getDocType()) || "live".equals(tmpDoc.getDocType())) {
                tmpValue.setOthers(StringUtils.isNotEmpty(tmpDoc.getSpecialParam()) ? tmpDoc.getSpecialParam() : "");
            } else {
                tmpValue.setOthers(tmpDoc.getOthers());
            }

            // 视频透传缩略图
            if ("video".equals(tmpDoc.getDocType())) {
                if (StringUtils.isNotEmpty(tmpDoc.getSpecialParam())) {
                    Map<String, String> params = JsonUtil.json2ObjectWithoutException(tmpDoc.getSpecialParam(), HashMap.class);
                    if (MapUtils.isNotEmpty(params) && StringUtils.isNotEmpty(params.get("thumbnailpic"))) {
                        tmpValue.setThumbnail(params.get("thumbnailpic"));
                    }
                }
            }

            fillEditorDoc(tmpValue);

            result.add(tmpValue);
            idSet.add(docId);
        }
        return result;
    }

    /**
     * 晓伟透传字段处理
     * 使用缓存，减少字符串处理次数
     * @param doc
     */
    private static void dealEditOthers(Document doc) {
        if (doc == null) {
            return;
        }

        String docid = doc.getDocId();
        try {
            String othersNew = EditorOthersCache.get(docid);
            if (StringUtils.isBlank(othersNew)) {
                othersNew = DocClient.getOthersNew(doc);
                //更新缓存
                if (StringUtils.isNotBlank(othersNew)) {
                    EditorOthersCache.put(docid, othersNew);
                }
            }
            if (StringUtils.isNotBlank(othersNew)) {
                doc.setOthers(othersNew);
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.error("dealEditOthers {}, ERROR:{}", docid);
        }
    }






    /**
     * 从others中获取simid
     * @param others
     * @return
     */
    private static String getOthersSimId(String others) {
        String simidStr = null;
        if (others != null && !others.isEmpty()) {
            if (others.indexOf("simID") > -1) {
                String firstStr = others.split("\\|!\\|")[0];
                simidStr = firstStr.split("=")[1];
            }
        }
        if (simidStr == null) {
            simidStr = "";
        }
        return simidStr;
    }


    /**
     * 结合cache。从others中获取simid
     * @param docId
     * @param others
     * @return
     */
    private static String getSimId(String docId, String others) {
        String simidStr = null;
        try {
            simidStr = EditorOthersSimIdCache.get(docId);
            if (StringUtils.isBlank(simidStr)) {
                simidStr = getOthersSimId(others);
                if (StringUtils.isNotBlank(simidStr)) {
                    EditorOthersSimIdCache.put(docId, simidStr);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("getSimId {},{}, ERROR:{}", docId, others, e);
        }
        return simidStr;
    }

    /**
     * 固定位置帖子从EditorBean 转换为Document对象
     * @param docList
     * @return
     */
    public static List<Document> Editor2DocumentList(List<EditorBean> docList) {
        if (null == docList || 0 == docList.size()) {
            return Collections.EMPTY_LIST;
        }
        List<Document> result = Lists.newArrayList();
        for (EditorBean tmpDoc : docList) {

            // 转换一下时间格式
            Date docDate = LogicDateUtil.strToDate(tmpDoc.getDate());

            Document tmpValue = new Document();
            tmpValue.setDocId(tmpDoc.getDocID());
            tmpValue.setTitle(tmpDoc.getTitle());
            tmpValue.setDate(docDate);
            tmpValue.setHotLevel(tmpDoc.getHotLevel());
            tmpValue.setDocType(tmpDoc.getDocType());
            tmpValue.setDocChannel(tmpDoc.getDocChannel());
            tmpValue.setWhy(tmpDoc.getWhy());
            tmpValue.setScore(tmpDoc.getScore());
            tmpValue.setHotBoost(tmpDoc.getHotBoost());
            tmpValue.setReadableFeatures(tmpDoc.getReadableFeatures());
            tmpValue.setOthers(tmpDoc.getOthers());

            String simidStr = tmpDoc.getSimId();
            if (simidStr != null && !simidStr.isEmpty()) {
                tmpValue.setSimId(simidStr);
            }
            //补全doc的特征信息
            fillEditorDoc(tmpValue);
            result.add(tmpValue);
        }
        return result;
    }


    public static List<EditorInsertItem> DealEditorInsert(List<EditorInsertItem> rawDocList) {
        if (null == rawDocList || 0 == rawDocList.size()) {
            return Collections.EMPTY_LIST;
        }
        List<EditorInsertItem> result = Lists.newArrayList();
        for (EditorInsertItem editorInsertItem : rawDocList) {
            int pos=editorInsertItem.getPos();
            if(pos<=8){
                editorInsertItem.setPullnum(0);
            }else{
                int n=(pos-8)/10+1;
                editorInsertItem.setPullnum(n);
            }
            result.add(editorInsertItem);
        }
        return result;
    }


}
