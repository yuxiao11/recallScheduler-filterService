package com.ifeng.recallScheduler.utils;

import com.beust.jcommander.internal.Sets;
import com.google.common.collect.Lists;
import com.ifeng.recallScheduler.constant.GyConstant;
import com.ifeng.recallScheduler.constant.RecWhy;
import com.ifeng.recallScheduler.constant.cache.CacheFactory;
import com.ifeng.recallScheduler.enums.DocType;
import com.ifeng.recallScheduler.filter.SansuFilter;
import com.ifeng.recallScheduler.item.Document;
import com.ifeng.recallScheduler.item.item2app;
import com.ifeng.recallScheduler.itemUtil.DocClient;
import com.ifeng.recallScheduler.threadUtil.BaseThreadPool;
import com.ifeng.recallScheduler.timer.TimerEntity;
import com.ifeng.recallScheduler.timer.TimerEntityUtil;
import com.ifeng.recallScheduler.user.Index4User;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import net.sf.ehcache.CacheManager;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.hadoop.hbase.client.Table;

/**
 * 通过hbase查询document
 * Created by jibin on 2017/7/3.
 */
@Service
public class DocUtil {

    private static final String columnName = "value";

    protected static Logger logger = LoggerFactory.getLogger(DocUtil.class);

    private final static Pattern HOT_FOCUS_PATTERN = Pattern.compile("v(\\d+)/");

    private static Set<String> needRefreshDocIds = new HashSet<>();

    @Autowired
    private DocClient docClient;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private SansuFilter sansuFilter;


    /**
     * 更新docid对应的doc，如果docid太多，这里先检查cache中是否存在，然后再切分成小段，多次请求
     *
     * @param ids
     * @param docCache
     */
    public void updateDocCacheBigList(List<String> ids, Cache docCache) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }

        List<String> ids2Query = Lists.newArrayList();
        int count = 0;
        for (String docid : ids) {
            Element docElement = docCache.get(docid);
            if (docElement == null) {
                ids2Query.add(docid);
                count++;
                if (count >= GyConstant.MaxDocQueryNum_Online) {
                    updateDocCacheBatch(ids2Query);
                    count = 0;
                    ids2Query.clear();
                }
            }
        }
        updateDocCacheBatch(ids2Query);
    }


    /**
     * 指定最大超时时间更新doc的缓存
     *
     * @param docIdList
     * @return
     */
    public boolean updateDocCacheBatchLimit(List<String> docIdList) {
        Future<Boolean> future = BaseThreadPool.THREAD_POOL_UpdateIndex.submit(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                updateDocCacheBatch(docIdList);
                return true;
            }
        });

        boolean finish = false;
        try {
            finish = future.get(GyConstant.MaxTimeOut_UpadteDocOnline, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            logger.error("updateDocCacheBatch docid:{} is error!:{}", JsonUtil.object2jsonWithoutException(docIdList), e);
            logger.error("THREAD_POOL_LOG:{}", BaseThreadPool.THREAD_POOL_UpdateIndex.toString());
            future.cancel(true);
        }
        return finish;
    }




    /**
     * doc缓存
     *
     * @param docIds
     */
    public void checkUpdateDoc(List<String> docIds) {
        try {
            if (CollectionUtils.isEmpty(docIds)){
                return;
            }

            TimerEntity timer = TimerEntityUtil.getInstance();
            Cache docCache = cacheManager.getCache(CacheFactory.CacheName.PersonalRecomDocumentInfo.getValue());
            Set<String> idSet2Query = Sets.newHashSet();
            for (String docid : docIds) {
                Element docElement = docCache.get(docid);
                if (docElement == null) {
                    //尽量从cache中获取，而不是实时查询hbase
                    idSet2Query.add(docid);
                }
            }

            if (idSet2Query.size() > 0) {
                List<String> docIdList = Lists.newArrayList();
                docIdList.addAll(idSet2Query);

                //查询hbase,并更新cache
                timer.addStartTime("checkUpdateDoc");
                updateDocCacheBatchLimit(docIdList);
                timer.addEndTime("checkUpdateDoc");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("checkUpdateDocCache ids:{},Error:{}", JsonUtil.object2jsonWithoutException(docIds), e);
        }
    }


    public void checkRefreshDocument(List<Object> inputList){
        long recentUpdate = System.currentTimeMillis() - 3600000; //每小时刷新一遍
        for(Object object : inputList){
            if(object instanceof Document) {
                Document doc = (Document) object;
                if (doc.getTimestamp() < recentUpdate) {
                    needRefreshDocIds.add(doc.getDocId());
                }
            }
        }
        if(needRefreshDocIds.size()>100) {
            List<String> checkIds = new ArrayList<>(needRefreshDocIds);
            needRefreshDocIds.clear();
            BaseThreadPool.DOCUMENT_UPDATE_THREAD_POOL.execute(
                    ()-> updateDocCacheBatch(checkIds)
            );

            logger.info("refresh docIds:{} size:{}", checkIds.get(0), checkIds.size());
        }
    }

    /**
     * 添加需要异步更新文章缓存的docId
     * @param docIds
     */
    public void addneedRefreshDocIds(String... docIds){
        for(String docId : docIds){
            needRefreshDocIds.add(docId);
        }
    }


    /**
     * doc缓存
     *
     * @param index4UserList
     */
    public void checkUpdateDocCache(List<Index4User> index4UserList) {
        if (CollectionUtils.isEmpty(index4UserList)) {
            return;
        }

        try {
            TimerEntity timer = TimerEntityUtil.getInstance();
            Cache docCache = cacheManager.getCache(CacheFactory.CacheName.PersonalRecomDocumentInfo.getValue());
            Set<String> idSet2Query = Sets.newHashSet();
            for (Index4User index4User : index4UserList) {
                String docid = index4User.getI();
                Element docElement = docCache.get(docid);
                if (docElement == null) {
                    //尽量从cache中获取，而不是实时查询hbase
                    idSet2Query.add(docid);
                }
            }

            if (idSet2Query.size() > 0) {
                List<String> docIdList = Lists.newArrayList();
                docIdList.addAll(idSet2Query);

                //查询hbase,并更新cache
                timer.addStartTime("checkUpdateDocCache");
                updateDocCacheBatchLimit(docIdList);
                timer.addEndTime("checkUpdateDocCache");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("checkUpdateDocCache ids:{},Error:{}", JsonUtil.object2jsonWithoutException(index4UserList), e);
        }
    }


    /**
     * 根据docid批量查询hbase 更新缓存
     *
     * @param docIdList
     */
    public void updateDocCacheBatch(List<String> docIdList) {

        if (docIdList == null || docIdList.size() == 0) {
            return;
        }

        Table cntTable = docClient.getDocTable(DocClient.CONTENT_TableName);
        Table indexTable = docClient.getDocTable(DocClient.INDEX_TableName);

        try {

            Map<String, Document> reMap = docClient.getDocBatch(docIdList, cntTable, indexTable);
            Cache recomDocumentInfo = cacheManager.getCache(CacheFactory.CacheName.PersonalRecomDocumentInfo.getValue());

            for (String docId : docIdList) {
                Document doc = reMap.get(docId);

                if (doc == null) {
                    continue;
                }
                recomDocumentInfo.put(new Element(docId, doc));
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("updateDocCacheBatch ERROR:{}", e);
        } finally {

            try {
                if (indexTable != null)
                    indexTable.close();
            } catch (IOException e) {
                logger.error("[HBase] Error while closing table {} {} ", DocClient.INDEX_TableName
                        , e.getMessage());
            }

            try {
                if (cntTable != null)
                    cntTable.close();
            } catch (IOException e) {
                logger.error("[HBase] Error while closing table {} {} ", DocClient.CONTENT_TableName
                        , e);
            }
        }
    }


    /**
     * 从cache中查找Document
     *
     * @param docId
     * @return
     */
    public Document getDocByCache(Cache docCache, String docId) {
        Document doc = null;
        Element docElement = docCache.get(docId);
        if (docElement != null) {
            doc = (Document) docElement.getObjectValue();
        }
        return doc;
    }





    /**
     * 判断doc是编辑帖
     *
     * @param doc
     * @return
     */
    public boolean isEditor(Document doc) {
        return (doc.getWhy() != null && doc.getWhy().contains(GyConstant.HEADLINE_EDITOR));
    }




    /**
     * 判断doc是编辑帖强插
     *
     * @param doc
     * @return
     */
    public boolean isEditorInsert(Document doc) {
        return (doc.getWhy() != null && doc.getWhy().contains(RecWhy.WhyEditorInsert));
    }

    public boolean isEditorMarquee(Document doc) {
        return (doc.getWhy() != null && (doc.getWhy().equals(RecWhy.WhyJpEditorMarquee) || doc.getWhy().equals(RecWhy.WhyJpEditorVertical)));
    }

    public boolean isMediaEx(Document doc) {
        return (doc.getWhy() != null && doc.getWhy().equals(RecWhy.WhyMediaExcellent));
    }

    public boolean isExclusive(Document doc) {
        return (doc.getWhy() != null && doc.getWhy().equals(RecWhy.WhyExclusive));
    }


    /**
     * 横排焦点图
     *
     * @param doc
     * @return
     */
    public boolean isHotFocusEditorMarquee(Document doc) {
        return (doc.getWhy() != null && doc.getWhy().equals(RecWhy.WhyHotFocusEditorMarquee));
    }

    /**
     * 凤凰精选 卫视视频 横排焦点图
     *
     * @param doc
     * @return
     */
    public boolean isIfengVideoMarquee(Document doc) {
        return (doc.getWhy() != null && doc.getWhy().equals(RecWhy.WhyIfengVideoMarquee));
    }

    /**
     * 订阅强插
     *
     * @param doc
     * @return
     */
    public boolean isUserSubInsert(Document doc) {
        return (doc.getWhy() != null && doc.getWhy().contains(RecWhy.ReasonUserSubForceInsert));
    }


    /**
     *
     * @param object
     * @return
     */
    public static boolean isFocus(Object object){
        if(object instanceof Document){
            Document doc = (Document) object;
            Map<String,Object> ext = doc.getExt();
            if(ext!=null && ext.containsKey(GyConstant.displayType)){
                String value = (String) ext.get(GyConstant.displayType);
                if(value!=null && (value.equals(GyConstant.focusDisplayType) || value.equals(GyConstant.hotFocusDisplayType))){
                    return true;
                }
            }
            return false;
        }
        return false;
    }


    /**
     * 正能量
     *
     * @param doc
     * @return
     */
    public boolean isPosiInsert(Document doc) {
        return (doc.getWhy() != null && doc.getWhy().equals(RecWhy.WhyPositiveEnergyHot));
    }

    /**
     * 判断热点事件是编辑强插的全局热点
     *
     * @param recallTag
     * @return
     */
    public static boolean isEditorHottagInsertAll(String recallTag) {
        if (StringUtils.isNotBlank(recallTag) && recallTag.contains(GyConstant.OperatorEvents) && recallTag.contains(GyConstant.Hot_Group_All)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断热点事件是编辑强插的全局热点 包含带标签的热点
     *
     * @param recallTag
     * @return
     */
    public static boolean isEditorHottagInsert(String recallTag) {
        if (StringUtils.isNotBlank(recallTag) && recallTag.contains(GyConstant.OperatorEvents)) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * 判断热点事件是编辑强插的热点
     *
     * @param recallTag
     * @return
     */
    public static boolean isEditorHottag(String recallTag) {
        if (StringUtils.isNotBlank(recallTag) && recallTag.contains(GyConstant.OperatorEvents)) {
            return true;
        } else {
            return false;
        }
    }


//    /**
//     * 判断是否编辑强制设置为大图样式
//     *
//     * @param doc
//     * @return
//     */
//    public boolean isEditorTitleimg(Document doc) {
//        boolean result = false;
//        if (isEditor(doc)) {
//            String others = doc.getOthers();
//            if (StringUtils.isNotBlank(others)) {
//                try {
//                    EditorOthers editorOthers = JsonUtil.json2ObjectWithoutException(others, EditorOthers.class);
//                    Map<String, Object> style = editorOthers.getStyle();
//                    if (style != null) {
//                        String view = String.valueOf(style.get(GyConstant.EditorOthers_Style_View));
//                        if (GyConstant.EditorOthers_Style_View_Titleimg.equals(view) || GyConstant.EditorOthers_Style_View_Bigimg.equals(view)) {
//                            result = true;
//                        }
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    logger.error("editorOthers {} ERROR:{}", others, e);
//                }
//            }
//        }
//        return result;
//    }


    public boolean isEditorPreOrder(Document x) {
        return StringUtils.isNotBlank(x.getOthers()) && x.getOthers().contains(GyConstant.Flag_Editor_Top);
    }

    /**
     * 判断是直播
     *
     * @param doc
     * @return
     */
    public boolean isLiveDocument(Document doc) {
        return DocType.Live.getValue().equals(doc.getDocType());
    }

    /**
     * 判断是精品池专题
     *
     * @param doc
     * @return
     */
    public static boolean isTopicDocument(Document doc) {
        return DocType.Topic.getValue().equals(doc.getDocType());
    }

    /**
     * 判断是精品池专题
     *
     * @param docType
     * @return
     */
    public static boolean isTopicDocument(String docType) {
        return DocType.Topic.getValue().equals(docType);
    }


    /**
     * 判断doc是图集
     *
     * @param doc
     * @return
     */
    public boolean isSlide(Document doc) {
        if (doc == null||StringUtils.isBlank(doc.getDocType())) {
            return false;
        }

        return (DocType.Slide.getValue().equals(doc.getDocType()));
    }

    public boolean isShort(Document doc) {
        if (doc == null) {
            return false;
        }

        return (DocType.Short.getValue().equals(doc.getDocType()));
    }

    /**
     * 判断doc是视频
     *
     * @param doc
     * @return
     */
    public static boolean isVideo(Document doc) {
        if (doc == null||StringUtils.isBlank(doc.getDocType())) {
            return false;
        }
        return (DocType.Video.getValue().equals(doc.getDocType()));
    }


    /**
     * 判断doc是小视频
     *
     * @param doc
     * @return
     */
    public static boolean isMiniVideo(Document doc) {
        if (doc == null || StringUtils.isBlank(doc.getPartCategory())) {
            return false;
        }
        return (GyConstant.miniVideo.equals(doc.getPartCategory()));
    }


    /**
     * 判断doc是世界杯无版权内容
     *
     * @param doc
     * @return
     */
    public static boolean isWorldCup(Document doc) {
        if (doc == null) {
            return false;
        }
        return (GyConstant.worldCup.equals(doc.getPartCategoryExt()));
    }


    /**
     * 判断doc是趣头条抓取内容
     *
     * @param doc
     * @return
     */
    public static boolean isQuTT(Document doc) {
        if (doc == null) {
            return false;
        }
        return (GyConstant.qutt.equals(doc.getPartCategoryExt()));
    }


    /**
     * 判断doc是视频
     *
     * @param docId
     * @return
     */
    public boolean isVideoGuid(String docId) {
        if (StringUtils.isBlank(docId)) {
            return false;
        }
        return (docId.length() >= GyConstant.GUID_Length);
    }


    /**
     *
     */
    public boolean isDocpic(Document doc) {
        return (DocType.Docpic.getValue().equals(doc.getDocType()));
    }

    /**
     * 判断是否是marquee
     *
     * @param doc
     * @return
     */
    public boolean isMarquee(Document doc) {
        return DocType.Marquee.getValue().equals(doc.getDocType());
    }

    /**
     * 判断是否是投放热点
     *
     * @param doc
     * @return
     */
    public boolean isBidHotspot(Document doc) {
        return DocType.hotspot.getValue().equals(doc.getDocType());
    }


//    /**
//     * 判断是否是新增的非北京的冷启动投放
//     *
//     * @param resultBidBean
//     * @return
//     */
//    public static boolean isColdUserNotBjBid(ResultBidBean resultBidBean) {
//        if (StringUtils.isBlank(resultBidBean.getBidType())) {
//            return false;
//        } else {
//            return (resultBidBean.getBidType().startsWith(DocType.ColdUserNotBjBid.getValue())
//                    || resultBidBean.getBidType().startsWith(DocType.BidNew.getValue()));
//        }
//    }


    /**
     * 判断doc是视频
     *
     * @param doc
     * @return
     */
    public boolean isBidding(Document doc) {
        return (doc.getWhy() != null && doc.getWhy().contains(GyConstant.HEADLINE_BIDDING));
    }

    /**
     * 判断why是正反馈强插
     *
     * @param why
     * @return
     */
    public boolean isPositiveFeedInsert(String why) {
        return (why != null && why.startsWith(RecWhy.SourcePositiveFeed));
    }


    public boolean isHotTagInsert(Document doc) {
        return (StringUtils.isNotBlank(doc.getWhy()) && doc.getWhy().contains(RecWhy.ReasonHotTagInsert));
    }

    public boolean isSearchInsert(Document doc) {
        return (StringUtils.isNotBlank(doc.getWhy()) && doc.getWhy().contains(RecWhy.ReasonUserSearch));
    }

    /**
     * 判断是否为 deeplink 强插文章
     * @param doc doc
     * @return boolean
     */
    public boolean isDeeplinkInsert(Document doc) {
        return (StringUtils.isNotBlank(doc.getWhy()) && doc.getWhy().contains(RecWhy.whyDeeplinkInsert));
    }

    public boolean isChannelDistributionInsert(Document doc) {
        return (StringUtils.isNotBlank(doc.getWhy()) && doc.getWhy().contains(RecWhy.whyChannelDistributionInsert));
    }

    public boolean isHotDoc(Document doc) {
        return (isHotTagInsert(doc) || isHotTagPosiInsert(doc) || isBiddingHotInsert(doc));
    }

    public boolean isBiddingHotInsert(Document doc) {
        return (StringUtils.isNotBlank(doc.getWhy()) && doc.getWhy().contains("biddingHot"));
    }

    //外部冷启动特殊展示形式
    public boolean isOutSpecialViewInsert(Document doc) {
        return (StringUtils.isNotBlank(doc.getRevealType()) && GyConstant.outSpecialView.contains(doc.getRevealType()));
    }
    /**
     * 热点正反馈强插
     *
     * @param doc
     * @return
     */
    public boolean isHotTagPosiInsert(Document doc) {
        return (StringUtils.isNotBlank(doc.getWhy()) && doc.getWhy().contains(RecWhy.PosiHotTagPosiInsert));
    }

    public boolean isLocalInsert(Document doc) {
        return (StringUtils.isNotBlank(doc.getWhy()) && doc.getWhy().contains(RecWhy.ReasonLocalFirst));
    }

    public boolean isLocal(Document doc) {
        return (StringUtils.isNotBlank(doc.getWhy()) && doc.getWhy().contains(RecWhy.ReasonLocal));
    }

    public boolean isUserSub(Document doc) {
        return (StringUtils.isNotBlank(doc.getWhy()) && doc.getWhy().contains(RecWhy.ReasonUserSub));
    }


    /**
     * 判断doc是大图
     * 这里的revealType 是我们自己添加的
     *
     * @param doc
     * @return
     */
    public static boolean isSpecialView(Document doc) {
        if (doc == null) {
            return false;
        }
        return (GyConstant.HEADLINE_SpecialView.equals(doc.getRevealType()));
    }


    /**
     * 判断是否是强插大图
     *
     * @param doc
     * @return
     */
    public static boolean isSpecialViewInsert(Document doc) {
        if (doc == null || doc.getWhy() == null) {
            return false;
        }
        if (doc.getWhy().equals(RecWhy.WhyHotFocusEditorMarquee) || doc.getWhy().equals(RecWhy.WhyMediaExcellent)
                || doc.getWhy().equals(RecWhy.WhyJpEditorMarquee)) {
            return true;
        }
        return false;
    }


    /**
     * 判断recallTag是simId
     *
     * @param recallTag
     * @return
     */
    public static boolean isSimId(String recallTag) {
        if (StringUtils.isNotBlank(recallTag)) {
            if (recallTag.startsWith(GyConstant.pre_simId)) {
                return true;
            }
        }
        return false;
    }


    /**
     * 12小时以内的优质精选需要优先展现
     *
     * @param doc
     * @return
     */
    public boolean isJpPoolPre(Document doc) {
        if (doc == null) {
            return false;
        }

        boolean result = isEditorPreOrder(doc);
        return result;
    }


    /**
     * 判断是热点数据
     *
     * @param doc
     * @return
     */
    public static boolean isHotTag(Document doc) {
        if (doc == null) {
            return false;
        }
        if (StringUtils.isNotBlank(doc.getWhy()) && doc.getWhy().contains(RecWhy.whyHotTag)) {
            return true;
        }
        return false;
    }


    /**
     * 判断是精品池的帖子
     *
     * @param doc
     * @return
     */
    public boolean isJpPool(Document doc) {
        if (doc == null || StringUtils.isBlank(doc.getWhy())) {
            return false;
        }
        return (doc.getWhy().startsWith(RecWhy.ReasonJpPool));
    }


    /**
     * @param docList
     * @return
     * @Title: rawdocList2DocumentList
     * @Description: 从redis中取出的文档对象转为内部业务逻辑处理的Document对象
     */
    public static List<Document> itemappList2DocumentList(List<item2app> docList) {
        if (null == docList || 0 == docList.size()) {
            return null;
        }
        List<Document> result = new ArrayList<Document>();
        Set<String> idSet = new HashSet<String>(docList.size());
        for (item2app tmpDoc : docList) {
            String docId = tmpDoc.getDocID();

            // 去除 "id重复的"和 TODO "有版权问题的"
            if (idSet.contains(docId)/*||others.indexOf("illegal")>=0*/) {
                continue;
            }

            // 转换一下时间格式
            Date docDate = DateUtils.strToDate(tmpDoc.getDate());

            String simidStr = tmpDoc.getSimId();
            String other = tmpDoc.getOthers();
            if (org.apache.commons.lang.StringUtils.isBlank(simidStr)) {
                if (org.apache.commons.lang.StringUtils.isNotBlank(other)) {
                    simidStr = getSimId(other);
                }
            }

            String urlStr = tmpDoc.getUrl();
            if (org.apache.commons.lang.StringUtils.isBlank(urlStr)) {
                if (org.apache.commons.lang.StringUtils.isNotBlank(other)) {
                    urlStr = getUrl(other);
                }
            }


            if (org.apache.commons.lang.StringUtils.isBlank(simidStr)) {
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
            tmpValue.setUrl(urlStr);
            tmpValue.setTitle(tmpDoc.getTitle());
            if (StringUtils.isNotBlank(tmpDoc.getWhy())) {
                tmpValue.setWhy(tmpDoc.getWhy());
            }
            tmpValue.setHotBoost(tmpDoc.getHotBoost());
            tmpValue.setReadableFeatures(tmpDoc.getReadableFeatures());
            tmpValue.setOthers(other);
            tmpValue.setSource(tmpDoc.getSource());
            tmpValue.setPartCategory(tmpDoc.getPartCategory());
            tmpValue.setPartCategoryExt(tmpDoc.getPartCategoryExt());


            //晓伟透传字段处理
            DocClient.dealOtherStr(tmpValue);
            DocClient.dealFeatures(tmpValue);

            result.add(tmpValue);
            idSet.add(docId);
        }
        return result;
    }


    /**
     * 从others字段中获取simid
     *
     * @param others
     * @return
     */
    private static String getSimId(String others) {
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
     * 从others字段中获取url
     *
     * @param others
     * @return
     */
    private static String getUrl(String others) {
        String urlStr = null;
        if (others != null && !others.isEmpty()) {
            if (others.contains("url=")) {
                String[] firstArr = others.split("\\|!\\|");
                for (String firstStr : firstArr) {
                    if (firstStr.contains("url=")) {
                        urlStr = firstStr.replaceFirst("url=", "");
                        break;
                    }
                }
            }
        }
        if (urlStr == null) {
            urlStr = "";
        }
        return urlStr;
    }


    /**
     * 去除documents中simId重复的Document
     *
     * @param list
     */
    public List<Document> removeDuplicateDoc(List<Document> list) {
        Set<String> simIds = new HashSet<>();
        List<Document> result = Lists.newArrayList();

        list.forEach(document -> {
            if (simIds.add(document.getSimId()) || DocUtil.isTopicDocument(document)) {
                result.add(document);
            }
        });
        return result;
    }


    /**
     * 用target中的数据过滤source中的数据
     *
     * @param source
     * @param target
     * @return
     */
    public List<Document> filterDuplicateDoc(List<Document> source, List<Document> target) {
        if ((CollectionUtils.isEmpty(target))) {
            return source;
        }

        Set<String> simIds = new HashSet<>();
        for (Document doc : target) {
            simIds.add(doc.getSimId());
        }

        List<Document> result = new ArrayList<>(source.size());
        for (Document doc : source) {
            if (simIds.contains(doc.getSimId())) {
                continue;
            }
            result.add(doc);
        }
        return result;
    }


    /**
     * 比较器，按照时间排序
     */
    public static final Comparator<Document> dateComparator = new Comparator<Document>() {
        @Override
        public int compare(Document doc1, Document doc2) {

            Date date1 = doc1.getDate();
            if (date1 == null) {
                date1 = new Date();
            }

            Date date2 = doc2.getDate();
            if (date2 == null) {
                date2 = new Date();
            }
            return ((date1.getTime() < date2.getTime()) ? 1 : -1);
        }
    };

    /**
     * 比较器，按照HotBoost排序
     */
    public static final Comparator<Document> hotBoostComparator = new Comparator<Document>() {
        @Override
        public int compare(Document doc1, Document doc2) {

            String hotBoost1 = StringUtils.defaultString(doc1.getHotBoost(), GyConstant.CTRQ_DEFAULT);
            String hotBoost2 = StringUtils.defaultString(doc2.getHotBoost(), GyConstant.CTRQ_DEFAULT);

            // 按照rank=Q倒序排序
            final double rank1 = Double.parseDouble(hotBoost1);
            final double rank2 = Double.parseDouble(hotBoost2);
            return rank1 == rank2 ? 0 : ((rank1 < rank2) ? 1 : -1);

        }
    };


//    /**
//     * 将mixBean 转换为 Index4User
//     *
//     * @param mixBean
//     * @return
//     */
//    public Index4User getIndex4UserByMixBean(MixBean mixBean) {
//        Index4User index4User = new Index4User();
//        index4User.setI(mixBean.getDocId());
//        index4User.setP(mixBean.getRecallInfo());
//        return index4User;
//    }


    /**
     * 根据三大基本类型
     * 将备选结果集合分类填充
     *
     * @param doc
     * @param docPicList
     * @param slideList
     * @param videoList
     */
    public void classifyBaseType(Document doc, List<Document> docPicList, List<Document> slideList, List<Document> videoList) {
        if (isVideo(doc)) {
            videoList.add(doc);
        } else if (isSlide(doc)) {
            slideList.add(doc);
        } else {
            docPicList.add(doc);
        }
    }

//    /**
//     * 把document转换为视频引擎的VideoTab
//     * 新闻app的视频频道使用
//     *
//     * @param doc
//     * @return
//     */
//    public VideoTab convertDoc2VideoTab(Document doc) {
//        VideoTab videoTab = new VideoTab();
//        videoTab.setDocId(doc.getDocId());
//        videoTab.setDocType(doc.getDocType());
//        videoTab.setOthers("url=" + doc.getUrl());
//        videoTab.setSimId(doc.getSimId());
//        videoTab.setRecomToken(doc.getRecomToken());
//        videoTab.setTitle(doc.getTitle());
//        videoTab.setCategoryName(doc.getWhy());
//        videoTab.setRecomMethod(doc.getReason());
//        videoTab.setDataSource(doc.getDataSource());
//        videoTab.setStatSource(doc.getStatSource());
//        videoTab.setRevealType(GyConstant.HEADLINE_SpecialView);
//        videoTab.setExt(doc.getExt());
//        videoTab.setPartCategory(doc.getPartCategory());
//        videoTab.setPartCategoryExt(doc.getPartCategoryExt());
//        videoTab.setFeedbackFeatures(doc.getFeedbackFeatures());
//        return videoTab;
//    }
//
//
//    /**
//     * 把document转换为视频引擎的Video
//     * 视频app使用
//     *
//     * @param doc
//     * @return
//     */
//    public Video convertDoc2Video(Document doc) {
//        Video video = new Video();
//        video.setTitle(doc.getTitle());
//        video.setDocId(doc.getDocId());
//        video.setGuid(doc.getUrl());
//        video.setSimId(doc.getSimId());
//        video.setRecomToken(doc.getRecomToken());
//        video.setCtrQ(doc.getCtrQ());
//        video.setSource(doc.getSource());
//        video.setCategoryName(doc.getWhy());
//        video.setRecomMethod(doc.getReason());
//        video.setDataSource(doc.getDataSource());
//        video.setStatSource(doc.getStatSource());
//        video.setPartCategory(doc.getPartCategory());
//        video.setFeedbackFeatures(doc.getFeedbackFeatures());
//        video.setPartCategoryExt(doc.getPartCategoryExt());
//        return video;
//    }


    /**
     * 根据index获取指定doc
     *
     * @param docList
     * @param index
     * @return
     */
    public Document getDocByIndex(List<Document> docList, int index) {
        Document doc = null;
        if (CollectionUtils.isNotEmpty(docList) && index < docList.size()) {
            doc = docList.get(index);
        }
        return doc;
    }


    /**
     * 添加要闻的召回原因
     *
     * @param doc
     */
    public void addYwShowReason(Document doc) {
        Map<String, String> result = new HashMap<>();
        result.put(GyConstant.Reason_Style, GyConstant.Style_Reason_JrXw);
        result.put(GyConstant.Reason_Name, GyConstant.Tag_Reason_JrXw);

        String showReason = JsonUtil.object2jsonWithoutException(result);
        doc.setShowReason(showReason);
    }

    /**
     * 添加其他的召回原因（不占用普通召回原因位置）
     *
     * @param doc
     */
    public void addOtherShowReason(Document doc, String showName, String style) {
        Map<String, String> result = new HashMap<>();
        result.put(GyConstant.Reason_Style, style);
        result.put(GyConstant.Reason_Name, showName);
        String showReason = JsonUtil.object2jsonWithoutException(result);
        doc.setShowReason(showReason);
    }


    /**
     * 把打底的热数据翻译成通用的document对象（打底数据字段不足）
     *
     * @param docIdList2Query
     * @return
     */
    public List<Document> hotIdList2DocumentList(List<String> docIdList2Query) {
        List<Document> result = Lists.newArrayList();
        List<String> id2query = Lists.newArrayList();
        for (String docId : docIdList2Query) {
            id2query.add(docId);
            if (id2query.size() >= GyConstant.doc2QueryLimit) {
                checkUpdateDoc(id2query);
                id2query.clear();
            }
        }
        if (CollectionUtils.isNotEmpty(id2query)) {
            checkUpdateDoc(id2query);
            id2query.clear();
        }

        Cache docCache = cacheManager.getCache(CacheFactory.CacheName.PersonalRecomDocumentInfo.getValue());

        Document doc = null;
        for (String docId : docIdList2Query) {
            doc = getDocByCache(docCache, docId);
            if (doc == null) {
                continue;
            }
            doc = doc.clone();
            result.add(doc);
        }
        logger.info("hotIdList2DocumentList size:{}", result.size());
        return result;
    }

    public boolean isTheme(Document doc) {
        return (doc.getWhy() != null && doc.getWhy().equals(RecWhy.WhyHotTheme));
    }


    public boolean isPreOrder(Document doc) {
        return (doc.getWhy() != null && doc.getWhy().contains(RecWhy.WhyJpPoolPre));
    }

    public boolean isJpOrder(Document doc) {
        return (doc.getWhy() != null && doc.getWhy().contains(RecWhy.WhyRecomEs));
    }

    //非个性化精品池
    public boolean isJpBasic(Document doc) {
        return (doc.getWhy() != null && doc.getWhy().contains(RecWhy.WhyJpPoolBasicEs));
    }
    public boolean isHotFocus(Document doc) {
        return (doc.getWhy() != null && doc.getWhy().contains(RecWhy.ReasonHotTagInsertFocus));
    }


//    /**
//     * 根据精品池的solr的other原因拼接精品池的why字段
//     *
//     * @param jpOther
//     * @return
//     */
//    public static String getJpWhy(String jpOther) {
//        String why = RecWhy.WhyJpPoolBasic;
//        if (StringUtils.isNotBlank(jpOther)) {
//            if (JpOtherEnum.jpPoolHot.getValue().equals(jpOther.trim())) {
//                why = RecWhy.WhyJpPoolHotTagbasic;
//            }
//        }
//
//        return why;
//    }


    /**
     * 更新bid的simId
     *
     * @param doc
     */
    public void updateBidSimId(Document doc) {
        if (doc == null) {
            return;
        }

        List<String> ids2Query = Lists.newArrayList();

        //热点事件不用更新doc本身的simId
        if (isBidHotspot(doc)) {
            return;
        }

        if (StringUtils.isBlank(doc.getSimId())) {
            ids2Query.add(doc.getDocId());
        }

        if (ids2Query.size() > 0) {
            Cache docCache = cacheManager.getCache(CacheFactory.CacheName.PersonalRecomDocumentInfo.getValue());
            TimerEntity timer = TimerEntityUtil.getInstance();
            //查询hbase,并更新cache
            timer.addStartTime("updateBidDoc");
            updateDocCacheBatchLimit(ids2Query);
            timer.addEndTime("updateBidDoc");


            //查询habse，更新缓存后再次更新simid
            Document cachedoc = getDocByCache(docCache, doc.getDocId());
            //simid为空则补足
            if (cachedoc != null) {
                if (StringUtils.isBlank(doc.getSimId())) {
                    doc.setSimId(cachedoc.getSimId());
                }

                if (StringUtils.isBlank(doc.getGroupId())) {
                    doc.setGroupId(cachedoc.getGroupId());
                }
            }
        }
    }


//    public static List<Index4User> convertToIndex(List<Document> documents) {
//        if (documents == null || documents.size() == 0) {
//            return new ArrayList<>();
//        }
//        List<Index4User> results = new ArrayList<>();
//        String docId = "";
//        try {
//            for (Document doc : documents) {
//                Index4User index4User = new Index4User();
//                String rt = doc.getC() == null ? "" : doc.getC();
//                String why = StringUtils.isBlank(doc.getWhy()) ? "" : doc.getWhy();
//                docId = doc.getDocId();
//                index4User.setI(docId);
//                index4User.setRT(rt);
//                index4User.setC(doc.getCtrQ());
//                index4User.setS(RecWhy.ReasonJpPool);
//                if(StringUtils.isNotBlank(doc.getHotBoost())){
//                    index4User.setH(Double.parseDouble(doc.getHotBoost()));
//                }
//                String whyOld=why;
//                if (why.contains(RecWhy.WhyRecomEs)) {
//                    why = RecWhy.WhyRecomEs;
//                } else if(why.contains(RecWhy.WhyJpPoolPre)){
//                    why = "pre";
//                } else{
//                    why = "basicEs";
//                }
//                index4User.setR(why);
//                results.add(index4User);
//            }
//        } catch (Exception e) {
//            logger.error("docid:{} convertToIndex error:{}", docId, e);
//        }
//        return results;
//    }

    public Map<String, List<Document>> pilesByDoctype(List<Document> sourceList) {
        Map<String, List<Document>> resultMap = new HashMap<>();
        List<Document> docList = Lists.newArrayList();
        List<Document> videoList = Lists.newArrayList();
        List<Document> slideList = Lists.newArrayList();
        List<Document> shortList = Lists.newArrayList();

        for (Document doc : sourceList) {
            //图文
            if (isVideo(doc)) {
                videoList.add(doc);
                //视频
            } else if (isDocpic(doc)) {
                docList.add(doc);
                //滑动图集
            } else if (isSlide(doc)) {
                slideList.add(doc);
            } else if(isShort(doc)) {
                shortList.add(doc);
            }
        }
        resultMap.put("v", videoList);
        resultMap.put("d", docList);
        resultMap.put("s", slideList);
        resultMap.put("short", shortList);
        return resultMap;
    }

//    public static List<Index4User> convertToIndex(List<String> docIds, String text, String type) {
//        if (docIds == null || docIds.size() == 0) {
//            return new ArrayList<>();
//        }
//        List<Index4User> results = new ArrayList<>();
//        try {
//            for (String docid : docIds) {
//                Index4User index4User = new Index4User();
//                index4User.setI(docid);
//                index4User.setRT(text);
//                if (type.equals("search")) {
//                    index4User.setR(RecWhy.ReasonUserSearch);
//                } else if (type.equals("sub")) {
//                    index4User.setR(RecWhy.ReasonUserSubForceInsert);
//                }
//                index4User.setS(RecWhy.whyInsertRecom);
//                results.add(index4User);
//            }
//        } catch (Exception e) {
//            logger.error("docIds convertToIndex error:{}", e);
//        }
//        return results;
//    }

}
