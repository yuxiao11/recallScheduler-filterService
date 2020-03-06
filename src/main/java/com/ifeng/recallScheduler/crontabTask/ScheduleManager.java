package com.ifeng.recallScheduler.crontabTask;

import com.beust.jcommander.internal.Maps;
import com.google.gson.reflect.TypeToken;
import com.ifeng.recallScheduler.constant.GyConstant;
import com.ifeng.recallScheduler.constant.RecWhy;
import com.ifeng.recallScheduler.constant.cache.CacheFactory;
import com.ifeng.recallScheduler.constant.cache.MysqlSourceInfoDataUtil;
import com.ifeng.recallScheduler.esSearch.EsJpPoolQueryUtil;
import com.ifeng.recallScheduler.filter.MediaFilter;
import com.ifeng.recallScheduler.filter.SansuFilter;
import com.ifeng.recallScheduler.filterController.service.impl.FilterService;
import com.ifeng.recallScheduler.item.Document;
import com.ifeng.recallScheduler.item.EditorBean;
import com.ifeng.recallScheduler.item.EditorInsertItem;
import com.ifeng.recallScheduler.params.RedisParams;
import com.ifeng.recallScheduler.remoteCache.editor.EditorFocusUtil;
import com.ifeng.recallScheduler.rule.DocModuleConvert;
import com.ifeng.recallScheduler.rule.impl.WeMediaSourceFilterHandler;
import com.ifeng.recallScheduler.utils.*;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by wupeng1 on 2017/6/6.
 * 定时任务调度管理器
 */
@Component
public class ScheduleManager {

    private final static Logger log = LoggerFactory.getLogger(ScheduleManager.class);

    @Autowired
    private SanSuDocUtil sanSuDocUtil;

    @Autowired
    private NegCommentDocUtil negCommentDocUtil;

//    @Autowired
//    private HotRecomUtil hotRecomUtil;

    @Autowired
    private EhCacheUtil ehCacheUtil;


    @Autowired
    private BlackDocUtil blackDocUtil;


    @Autowired
    private DocUtil docUtil;

//    @Autowired
//    private YidianVideoHotDataUtil yidianVideoHotDataUtil;
//
//    @Autowired
//    private JpPoolNewsUtil jpPoolNewsUtil;

    @Autowired
    private EditorFocusUtil editorFocusUtil;

    @Autowired
    private SpecialFilterUserUtil specialFilterUserUtil;

//    @Autowired
//    private LvSHotRecomUtil lvSHotRecomUtil;
//
//    @Autowired
//    private ServeCountryHotRecomUtil serveCountryHotRecomUtil;
//
//    @Autowired
//    private WxSilenceSeaHotRecomUtil wxSilenceSeaHotRecomUtil;
//
//    @Autowired
//    private VideoJpPoolUtil videoJpPoolUtil;
//
//    @Autowired
//    private PositiveEnergyHotRecomUtil positiveEnergyHotRecomUtil;
//
//    @Autowired
    private SansuFilter sansuFilter;

    @Autowired
    private MediaFilter mediaFilter;

//    @Autowired
//    private HotTagDataUtil hotTagDataUtil;

    @Autowired
    private JpPoolCacheUtil jpPoolCacheUtil;


    @Autowired
    private MysqlSourceInfoDataUtil mysqlSourceInfoDataUtil;


    @Autowired
    private CacheManager cacheManager;

//
//    @Autowired
//    private VideoNumModelCache videoNumModelCache;

    @Autowired
    private WeMediaSourceFilterHandler weMediaSourceFilterHandler;

    @Autowired
    private EsJpPoolQueryUtil esJpPoolQueryUtil;

//    @Autowired
//    private ExploerSourceInfoDataUtil exploerSourceInfoDataUtil;

    @Autowired
    private FilterService filterService;

//
//    @Autowired
//    private FixTopDataUtil fixTopDataUtil;
//
//    @Autowired
//    private Paradigm4RecomUtils paradigm4RecomUtils;
//
//    @Autowired
//    private ThemeHotCache themeHotCache;
//
//    @Autowired
//    private HotMarqueeCache hotMarqueeCache;
//
//    @Autowired
//    private HotTagDataUtilNew hotTagDataUtilNew;
//
//    @Autowired
//    private HotMarqueeMySqlCache hotMarqueeMySqlCache;
//
//    @Autowired
//    private HotArticleCache hotArticleCache;
//
//    @Autowired
//    private CacheEvictUtil cacheEvictUtil;
//
//    @Autowired
//    private ColdFilterCache coldFilterCache;
//
//    @Autowired
//    private JpPoolPreDataUtil jpPoolPreDataUtil;
//
//    @Autowired
//    private OutColdCacheUtil outColdCacheUtil;

    /**
     * guava Cache 的持久化
     */
    @Scheduled(cron = "29 20 0/1 * * ? ")
    public void guavaCachePersist() {
//        CachePersist.writeToFile(SpecialFilterHandler.DocIsIfengVideoCache, GyConstant.DocIsIfengVideoCacheOfpath);
        CachePersist.writeToFile(WeMediaSourceFilterHandler.cache, GyConstant.WeMediaSourceCacheOfpath);
        CachePersist.writeToFile(SansuFilter.sansuFiltercache, GyConstant.SansuFiltercache);
        CachePersist.writeToFile(BlackDocUtil.blackDocCache, GyConstant.blackDocCache);
    }

    @Scheduled(cron = "23 1 0/1 * * ? ")
    public void updateWeMedia() {
        weMediaSourceFilterHandler.updateWeMedia();
    }


    @Scheduled(cron = "0 1 1 * * ?")
    public void writetolocalfile() {
        try {
            Cache cache = cacheManager.getCache(CacheFactory.CacheName.PersonalRecomDocumentInfo.getValue());
            if (null != cache) {

                int size = cache.getSize();
                log.info("into writetolocalfile cache size:{}", size);
                List list = cache.getKeys();
                String key = "";
                long sizewrite = 0L;
                Map<Integer, Writer> writerMap = Maps.newHashMap();
                Writer writer = null;
                try {
                    long startwrite = System.currentTimeMillis();


                    for (int i = 0; i < GyConstant.doc_txt_Num; i++) {
                        Writer writerTmp = new FileWriter(GyConstant.personalcacheOfpath + GyConstant.Symb_Underline + i, false);  // 写入的文本不附加在原来的后面而是直接覆盖
                        writerMap.put(i, writerTmp);
                    }


                    int mod = 0;
                    Element oneElement = null;
                    Document onedocument = null;
                    String jsonString = "";


                    for (int i = 0; i < list.size(); i++) {
                        try {
                            key = (String) list.get(i);
                            oneElement = cache.get(key);
                            if (oneElement == null) {
                                continue;
                            }

                            onedocument = (Document) oneElement.getValue();
                            jsonString = JsonUtil.object2jsonWithoutException(onedocument);

                            mod = i % GyConstant.doc_txt_Num;


                            //如果文章个数大于baseNum，后续内容按照几率进行更新，从而避免缓存内容媒体刷到磁盘上而不更新
                            if (sizewrite > GyConstant.doc_txt_BaseNum && MathUtil.getNum(100) > 50) {
                                continue;
                            }

                            writer = writerMap.get(mod);
                            writer.write(jsonString + "\n");
                            sizewrite++;
                        } catch (Exception e) {
                            log.error("writetolocalfile error:{}", e);
                        }
                    }
                    long cost = System.currentTimeMillis() - startwrite;
                    log.info("into writetolocalfile size={},sizewrite={},writecost={}", list.size(), sizewrite, cost);
                } catch (Exception e) {
                    log.error("writetolocalfile write to localfile:{}", e);
                } finally {
                    try {
                        for (Map.Entry<Integer, Writer> entry : writerMap.entrySet()) {
                            Writer tmp = entry.getValue();
                            if (tmp != null) {
                                tmp.close();
                                log.info("Writer close");
                            }
                        }
                    } catch (Exception e) {
                        log.error("writetolocalfile close e:{}", e);
                    }
                }


            }
        } catch (Exception e) {
            log.error("writetolocalfile ERROR:{}", e);
        }
    }

    /**
     * 更新媒体信息
     */
    @Scheduled(cron = "0 30 0/1 * * ? ")
    public void updateSourceInfo() {
        try {
            mysqlSourceInfoDataUtil.updateMediaInfo();
            mysqlSourceInfoDataUtil.updateVideoMediaInfo();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("updateSourceInfo ERROR:{}", e);
        }
    }

//
//    /**
//     * 精品池数据更新  一分钟
//     */
//    @Scheduled(cron = "15 0/1 * * * ? ")
//    public void updateJpPool() {
//        try {
//            jpPoolNewsUtil.loadCache();
//            jpPoolPreDataUtil.loadCache();
//        } catch (Exception e) {
//            e.printStackTrace();
//            log.error("updateJpPool ERROR:{}", e);
//        }
//    }


//    /**
//     * 精品池带曝光 数据更新  每二十八分钟  调用一次
//     */
//    @Scheduled(cron = "40 */28 * * * ? ")
//    public void updateJpPoolWithEv() {
//        try {
//            jpPoolNewsUtil.loadEvCache();
//        } catch (Exception e) {
//            e.printStackTrace();
//            log.error("updateJpPoolWithEv ERROR:{}", e);
//        }
//    }


    /**
     * 编辑焦点图  30s
     */
    @Scheduled(cron = "0/30 * * * * ? ")
    public void updateEditorFocus() {
        try {
            editorFocusUtil.loadCache();
        } catch (Exception e) {
            log.error("updateEditorFocus ERROR :{}", e);
        }
    }



    /**
     * 张阳提供，目前基本不更新
     * 每1小时更新三俗黑名单
     */
    @Scheduled(cron = "37 38 0/2 * * ? ")
    public void updateSansuBlack() {
        try {
            sanSuDocUtil.loadCacheFromSql();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("updateSansuBlack ERROR:{}", e);
        }
    }


    /**
     * 张鹏程提供 负向评论 文章 分等级缓存 用于过滤
     * 每 半小时 更新  负评论黑名单
     */
    @Scheduled(cron = "0 0/30 * * * ? ")
    public void updateNegCommentBlack() {
        try {
            negCommentDocUtil.loadCacheFromSql();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("updateNegCommentBlack ERROR:{}", e.toString());
        }
    }


    /**
     * 每小时输出一次精品池数据更新结果
     */
    @Scheduled(cron = "0 40 0/1 * * ? ")
    public void printJpPoolLog() {
        try {
            List<Document> list = esJpPoolQueryUtil.getAllJpPoolDocuments(5000, 24);;
            if (CollectionUtils.isEmpty(list)) {
                log.info("printJpPoolLog SCHEDULE : GET SOLR ERROR");
            } else {
                for (Document doc : list) {
                    log.info("printJpPoolLog:{}", JsonUtil.object2jsonWithoutException(doc));
                    if (docUtil.isJpPoolPre(doc)) {
                        log.info("printJpPoolLog JpPoolPreLog:{}", JsonUtil.object2jsonWithoutException(doc));
                    }

                    if (DocUtil.isHotTag(doc)) {
                        log.info("printJphotListLog:{}", JsonUtil.object2jsonWithoutException(doc));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("printJpPoolLog ERROR:{}", e);
        }
    }


    @Scheduled(cron = "0 0/10 * * * ? ")
    public void printFocusLog() {
        try {
            List<Document> allFocus = ehCacheUtil.getListDoc(CacheFactory.CacheName.EditorFocus.getValue(), CacheFactory.CacheName.EditorFocus.getValue());
            if (CollectionUtils.isEmpty(allFocus)) {
                log.info("printFocusLog SCHEDULE : GET ERROR");
            } else {
                for (Document doc : allFocus) {
                    log.info("printBaseFocusLog:{}", JsonUtil.object2jsonWithoutException(doc));
                }

                //bug fix，滤掉固定位置(redis)中的焦点图，因缓存要用于排重，不能直接在缓存操作
                //根据docid区分数据源为solr、redis(固定位置)，solr中需排掉redis这部分，redis中焦点图以cmpp开头
                Set<String> redisFocusSimIds = allFocus.stream().filter(x -> {
                    if (!StringUtil.startWithNum(x.getDocId())) {
                        return true;
                    }
                    return false;
                }).map(x -> x.getSimId()).collect(Collectors.toSet());

                allFocus = allFocus.stream().filter(x -> !redisFocusSimIds.contains(x.getSimId())).collect(Collectors.toList());

                //固定位置过滤
                Set<String> simIdSet = filterService.getRegularSimIdSet();
                allFocus = allFocus.stream().filter(x -> !simIdSet.contains(x.getSimId())).collect(Collectors.toList());

                for (Document doc : allFocus) {
                    log.info("printAfterFilFocusLog:{}", JsonUtil.object2jsonWithoutException(doc));
                }


            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("printFocusLog ERROR:{}", e);
        }
    }
    /**
     * 黑名单数据更新  3分钟
     */
    @Scheduled(cron = "19 1/3 * * * ? ")
    public void updateBlackList() {
        blackDocUtil.getBlackDocMap();
    }

    /**
     * 编辑固定位置的强插逻辑数据  30s
     */
    @Scheduled(cron = "0/30 * * * * ? ")
    public void updateEditorRegularPosition() {
        try {
            String value = RedisUtil.get(RedisParams.getEditorRegularPositionDataIP(), RedisParams.getEditorRegularPositionDataPort(), RedisParams.getEditorRegularPositionDataDB(), RedisParams.getEditorRegularPositionDatakey());
            if (StringUtils.isBlank(value)) {
                return;
            }
            List<EditorBean> rawDocList = JsonUtil.json2Object(value, new TypeToken<List<EditorBean>>() {
            }.getType());
            List<Document> list = DocModuleConvert.Editor2DocumentList(rawDocList);
            if (CollectionUtils.isEmpty(list)) return;
            list.forEach(x -> x.setWhy(RecWhy.WhyJpPoolFocus));
            ehCacheUtil.put(CacheFactory.CacheName.EditorRegularPosition.getValue(), CacheFactory.CacheName.EditorRegularPosition.getValue(), list);
            log.warn("update updateEditorRegularPosition from redis {}", list.size());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("updateEditorRegularPosition ERROR :{}", e);
        }
    }

    @Scheduled(cron = "0/30 * * * * ? ")
    public void updateEditorRegularPositionNew() {
        try {
            String value = RedisUtil.get(RedisParams.getEditorRegularPositionDataIP(), RedisParams.getEditorRegularPositionDataPort(), RedisParams.getEditorRegularPositionDataDB(), RedisParams.getEditorRegularPositionDatakeyNew());
            if (StringUtils.isBlank(value)) {
                log.error("emptySource of updateEditorRegularPositionNew from redis ");
                return;
            }
            List<EditorInsertItem> rawDocList = JsonUtil.json2Object(value, new TypeToken<List<EditorInsertItem>>() {
            }.getType());
            List<EditorInsertItem> list = DocModuleConvert.DealEditorInsert(rawDocList);
            if (CollectionUtils.isEmpty(list)) return;
            ehCacheUtil.put(CacheFactory.CacheName.EditorRegularPosition.getValue(), CacheFactory.CacheName.EditorRegularNew.getValue(), list);
            log.warn("update updateEditorRegularPositionNew from redis {}", list.size());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("updateEditorRegularPositionNew ERROR :{}", e);
        }
    }

//    /**
//     * 每2小时更新一次凤凰热闻榜冷启动数据的缓存
//     * 包含原热榜和新的时间段的热榜
//     */
//    @Scheduled(cron = "49 2 0/2 * * ?")
//    public void updateHotRecomData() {
//        try {
//            hotRecomUtil.checkHotRegion();
//        } catch (Exception e) {
//            e.printStackTrace();
//            log.error("updateHotRecomData ERROR :{}", e);
//        }
//    }
//

//
//    /**
//     * 每小时更新一次lvSHotRecomUtil
//     */
//    @Scheduled(cron = "0 3 0/1 * * ?")
//    public void updateLvSHotRecomData() {
//        try {
//            lvSHotRecomUtil.loadCacheByHbase();
//        } catch (Exception e) {
//            e.printStackTrace();
//            log.error("updateLvSHotRecomData ERROR :{}", e);
//        }
//    }

//    /**
//     * 每5分钟检查一下lvSHotRecomUtil
//     */
//    @Scheduled(cron = "0 1/5 * * * ?")
//    public void checkUpdateLvSHotRecomData() {
//        try {
//            lvSHotRecomUtil.check();
//        } catch (Exception e) {
//            e.printStackTrace();
//            log.error("updateLvSHotRecomData  check ERROR :{}", e);
//        }
//    }
//
//
//
//    /**
//     * 每2分钟更新一次热点数据的缓存
//     */
//    @Scheduled(cron = "0 0/2 * * * ?")
//    public void updateHotTagDataUtilData() {
//        try {
//            if (ApolloConstant.Switch_on.equals(HotApplicationConfig.getProperty(ApolloConstant.HotMysqlSwitch))) {
//                hotTagDataUtilNew.loadCache();
//            }else{
//                hotTagDataUtil.loadCache();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            log.error("updateHotTagDataUtilData ERROR :{}", e);
//        }
//    }
//
//    @Scheduled(cron = "15 0/1 * * * ?")
//    public void updateHotEventsUtilData() {
//        try {
//            if (ApolloConstant.Switch_on.equals(HotApplicationConfig.getProperty(ApolloConstant.HotMysqlSwitch))) {
//                hotMarqueeMySqlCache.loadCache();
//            }else{
//                hotMarqueeCache.checkCache();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            log.error("updateHotEventsUtilData ERROR :{}", e);
//        }
//    }
//
//    @Scheduled(cron = "30 0/1 * * * ?")
//    public void updateHotArticleUtilData() {
//        try {
//            if (ApolloConstant.Switch_on.equals(HotApplicationConfig.getProperty(ApolloConstant.HotMysqlSwitch))) {
//                hotArticleCache.loadCache();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            log.error("updateHotEventsUtilData ERROR :{}", e);
//        }
//    }





    /**
     * 每5分钟更新一次精品池数据的缓存
     */
    @Scheduled(cron = "0 0/5 * * * ?")
    public void updateJpTagDataUtilData() {
        try {
            jpPoolCacheUtil.loadCache();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("updateJpTagDataUtilData ERROR :{}", e);
        }
    }

//    /**
//     * 上午11点和凌晨2点
//     */
//    @Scheduled(cron = "0 0 2,11 * * ?")
//    public void updateServeCountryHotRecomUtilData() {
//        try {
//            serveCountryHotRecomUtil.loadCache();
//        } catch (Exception e) {
//            e.printStackTrace();
//            log.error("updateServeCountryData ERROR :{}", e);
//        }
//    }
//
//    /**
//     * 上午10点和凌晨1点
//     */
//    @Scheduled(cron = "0 0 1,10 * * ?")
//    public void updateWxSilenceSeaHotRecomUtilData() {
//        try {
//            wxSilenceSeaHotRecomUtil.loadCache();
//        } catch (Exception e) {
//            e.printStackTrace();
//            log.error("updateWxSilenceSeaData ERROR :{}", e);
//        }
//    }
//
//    /**
//     * 每一小时更新一次凤凰视频精品池内容的缓存
//     */
//    @Scheduled(cron = "0 35 0/1 * * ?")
//    public void updateVideoJpPoolData() {
//        try {
//            videoJpPoolUtil.loadCache();
//        } catch (Exception e) {
//            e.printStackTrace();
//            log.error("updateVideoJpPoolData ERROR :{}", e);
//        }
//    }
//
//
//
//    /**
//     * 每2小时更新一次正能量数据的缓存
//     */
//    @Scheduled(cron = "0 0 3,16 * * ?")
//    public void updatepositiveEnergyHotRecomData() {
//        try {
//            positiveEnergyHotRecomUtil.loadCache();
//        } catch (Exception e) {
//            e.printStackTrace();
//            log.error("updatepositiveEnergyHotRecomData ERROR :{}", e);
//        }
//    }
//
//
//    /**
//     * 每隔1小时更新以下一点资讯的视频冷启动
//     */
//    @Scheduled(cron = "0 7 0/1 * * ?")
//    public void updateYidianVideoHot() {
//        try {
//            int count = yidianVideoHotDataUtil.loadCacheScheduled(YidianVideoHotDataUtil.cacheSize);
//            if (count == 0) {
//                yidianVideoHotDataUtil.loadCacheScheduled(YidianVideoHotDataUtil.cacheSize);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            log.error("updateYidianVideoHot ERROR :{}", e);
//        }
//    }


    /**
     * 每隔15秒更新以下特殊用户名单
     */
    @Scheduled(cron = "0/15 * * * * ?")
    public void updateSpecialFilterUser() {
        try {
            specialFilterUserUtil.loadCache();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("updateSpecialFilterUser ERROR :{}", e);
        }
    }

    /**
     * 每隔 15 min加载
     */
    @Scheduled(cron = "40 0/15 * * * ?")
    public void updateScoreFilter() {
        try {
            sansuFilter.loadCacheAll();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("updateScoreFilter ERROR :{}", e);
        }
    }


    /**
     * 每隔 15 min加载
     */
    @Scheduled(cron = "20 0/15 * * * ?")
    public void updateMediaFilter() {
        try {
            mediaFilter.loadCache();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("updateMediaFilter ERROR :{}", e);
        }
    }

//
//    @Scheduled(cron = "0 3 0/2 * * ?")
//    public void updateSourceNews() {
//        try {
//            exploerSourceInfoDataUtil.getSourceDocMap();
//        } catch (Exception e) {
//            e.printStackTrace();
//            log.error("update updateSourceNews news err:{}", e);
//        }
//    }
//
//    @Scheduled(cron = "0 2/10 * * * ?")
//    public void updateParadigm4Recom() {
//        try {
//            paradigm4RecomUtils.loadCache();
//        } catch (Exception e) {
//            e.printStackTrace();
//            log.error("update updateParadigm4Recom news err:{}", e);
//        }
//    }
//
//    @Scheduled(cron = "0 2/5 * * * ?")
//    public void updateThemeNews() {
//        try {
//            themeHotCache.loadCache();
//        } catch (Exception e) {
//            e.printStackTrace();
//            log.error("update updateThemeNews news err:{}", e);
//        }
//    }
//
//    @Scheduled(cron = "0 55 0/2 * * ?")
//    public void evictExpireCacheElements(){
//        try{
//            log.warn("begin to evict expire element");
//            cacheEvictUtil.evictExpireElement();
//        }catch (Exception e){
//            log.error("cache evict expire element err:{}", e);
//        }
//    }
//
//    /**
//     * 每隔3个小时
//     */
//    @Scheduled(cron = "0 6 0/3 * * ?")
//    public void updateColdFilter(){
//        try{
//            coldFilterCache.initCache();
//        }catch (Exception e){
//            log.error("updateColdFilter Data err:{}", e);
//        }
//    }
//
//    /**
//     * 每隔1个小时
//     */
//    @Scheduled(cron = "0 5 0/1 * * ?")
//    public void updateOutColdkind(){
//        try{
//            outColdCacheUtil.loadCache();
//        }catch (Exception e){
//            log.error("updateOutColdkind Data err:{}", e);
//        }
//    }
}
