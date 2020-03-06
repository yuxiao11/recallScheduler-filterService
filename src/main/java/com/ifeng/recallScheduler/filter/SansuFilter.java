package com.ifeng.recallScheduler.filter;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.ifeng.recallScheduler.utils.TrieTree;
import com.ifeng.recallScheduler.constant.GyConstant;
import com.ifeng.recallScheduler.constant.cache.DocSansuScoreCache;
import com.ifeng.recallScheduler.constant.cache.ScoreFilterCache;
import com.ifeng.recallScheduler.item.Document;
import com.ifeng.recallScheduler.request.RequestInfo;
import com.ifeng.recallScheduler.utils.CachePersist;
import com.ifeng.recallScheduler.utils.EhCacheUtil;
import com.ifeng.recallScheduler.utils.SpecialFilterUserUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * Created by geyl on 2017/12/14.
 */
@Service
public class SansuFilter {
    private static final Logger logger = LoggerFactory.getLogger(SansuFilter.class);

    public static final Integer THRESHOLD = 6;

    /**
     * 标题过滤白名单 10分，不用过滤
     */
    public static final Integer THRESHOLD_White = 20;


    //    private ScoreFilterCache scoreFilterCache;
    //关键词词表过滤字典树
    private TrieTree trieTree;

    @Autowired
    private EhCacheUtil ehCacheUtil;

    @Autowired
    private SpecialFilterUserUtil specialFilterUserUtil;


    public static Cache<String, Integer> sansuFiltercache = CacheBuilder
            .newBuilder()
            .recordStats()
            .concurrencyLevel(15)
            .expireAfterWrite(20, TimeUnit.HOURS)
            .initialCapacity(400000)
            .maximumSize(400000)
            .build();


    /**
     * 全量更新
     */
    public void loadCacheAll() {
        long start = System.currentTimeMillis();
        Map<String, Integer> filterResult = ScoreFilterCache.queryScoreFilterWordsFromRedis();
        if (MapUtils.isEmpty(filterResult)) {
            logger.error("scoreFilter,queryScoreFilterWordsFromRedis=null");
        } else {
            logger.info("scoreFilter load cache,size:{}", filterResult.keySet().size());
            trieTree = new TrieTree(filterResult.keySet(), filterResult);
            sansuFiltercache.putAll(filterResult);
        }
        logger.info("sansu loadCacheAll init cost:{}", System.currentTimeMillis() - start);
    }

    @PostConstruct
    public void initFirst() {
        long start = System.currentTimeMillis();
        try {
            CachePersist.loadToCache(SansuFilter.sansuFiltercache, GyConstant.SansuFiltercache);
            Map<String, Integer> filterResult = SansuFilter.sansuFiltercache.asMap();
            if (filterResult == null || filterResult.size() < GyConstant.cacheNum_Min) {
                filterResult = ScoreFilterCache.queryScoreFilterWordsFromRedis();
                logger.info("scoreFilter initFirst byRedis,size:{}", filterResult.keySet().size());
            } else {
                logger.info("scoreFilter initFirst byFile,size:{}", filterResult.keySet().size());
            }

            if (MapUtils.isEmpty(filterResult)) {
                logger.error("scoreFilter initFirst byFile,size:0");
            } else {
                trieTree = new TrieTree(filterResult.keySet(), filterResult);
                sansuFiltercache.putAll(filterResult);
            }
            logger.info("sansu trieTree init cost:{}", System.currentTimeMillis() - start);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("sansu initFirst ERROR:{}", e);
        }
        logger.info("sansu initFirst cost:{}", System.currentTimeMillis() - start);
    }


    private boolean textFilter(Document document, int THRESHOLD) {  //THreshold通过requestinfo获得


        String title = document.getTitle();
        title = removeStringMark(title);

        int point = 0;

        Integer cachePoint = DocSansuScoreCache.getDocScore(document.getDocId());
        if (cachePoint == null) {
            point = trieTree.matchesPoints(title.toLowerCase(), THRESHOLD, document);
            DocSansuScoreCache.putFilteredId(document.getDocId(), point);
//            logger.info("scoreFilter load cachePoint,cachePoint=null,point={},docId={},title={}", point, document.getDocId(),title);
        } else {
            point = cachePoint;
//            logger.info("scoreFilter load cachePoint,cachePoint not null,point={},docId={},title={}", point, document.getDocId(),title);
        }

        if (point >= THRESHOLD) {
//                filterLogger.info("filter points:{} title:{}", point, title);
            return true;
        }
        return false;

    }

    /**
     * 去除String中所有中英文标点
     *
     * @param text
     * @return
     */
    private String removeStringMark(String text) {
        return text.replaceAll("[\\pP+~$`^=|<>～｀＄＾＋＝｜＜＞￥×]", "");
    }


    public boolean titleFilter(Document document) {
        boolean neeFilter = false;
        if (document != null && StringUtils.isNotBlank(document.getTitle())) {
            try {
                if (textFilter(document, THRESHOLD)) {
                    neeFilter = true;
                    logger.info("sansu filter id:{},title:{}", document.getDocId(), document.getTitle());
                }
            } catch (Exception e) {
                logger.error("title filter error:{} doc:{}", e, document.toString());
            }
        }


        return neeFilter;
    }


    /**
     * 根据白名单控制是否需要进行标题的三俗过滤
     *
     * @param requestInfo
     * @param document
     * @return
     */
    public boolean titleFilter(RequestInfo requestInfo, Document document) {
        boolean neeFilter = false;

        //根据人群获取过滤阈值
        int threshold = requestInfo.getTitleThreshold();

        // 白名单  不用过滤
        if (threshold >= GyConstant.THRESHOLD_White) {
            return false;
        }


        if (document != null && StringUtils.isNotBlank(document.getTitle())) {
            try {
                if (textFilter(document, threshold)) {
                    neeFilter = true;
                }
            } catch (Exception e) {
                logger.error("{} titlefilter error:{} doc:{}", requestInfo.getUserId(), e, document.toString());
            }
        }
        return neeFilter;
    }


    /**
     * 临时需求，过滤两性
     *
     * @param document
     * @return
     */
    @Deprecated
    public boolean liangXing_Filter(Document document) {
        boolean neeFilter = false;
        if (document != null) {
            try {
                List<String> cList = document.getDevCList();

                if (CollectionUtils.isNotEmpty(cList)) {
                    for (String c : cList) {
                        if (GyConstant.c_LiangXing.equals(c) || GyConstant.c_QinGan.equals(c) || GyConstant.c_MeiNv.equals(c)) {
                            return true;
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("title filter error:{} doc:{}", e, document.toString());
            }
        }
        return neeFilter;
    }

    /**
     * 临时需求，过滤两性情感
     *
     * @param document
     * @return
     */
    @Deprecated
    public boolean liangXing_QingGan_Filter(Document document) {
        boolean neeFilter = false;
        if (document != null) {
            try {
                List<String> cList = document.getDevCList();

                if (CollectionUtils.isNotEmpty(cList)) {
                    for (String c : cList) {
                        if (GyConstant.c_LiangXing.equals(c) || GyConstant.c_QinGan.equals(c) || GyConstant.c_MeiNv.equals(c)) {
                            return true;
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("title filter error:{} doc:{}", e, document.toString());
            }
        }
        return neeFilter;
    }


    /**
     * 临时需求，过滤两性情感
     *
     * @param document
     * @return
     */
    @Deprecated
    public boolean liangXing_QingGan_Filter(RequestInfo requestInfo, Document document) {
        boolean neeFilter = false;
        if (specialFilterUserUtil.isWxbUser(requestInfo)) {

            if (document != null) {
                try {
                    List<String> cList = document.getDevCList();

                    if (CollectionUtils.isNotEmpty(cList)) {
                        for (String c : cList) {
                            if (GyConstant.c_LiangXing.equals(c) || GyConstant.c_QinGan.equals(c) || GyConstant.c_MeiNv.equals(c)) {
                                return true;
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error("title filter error:{} doc:{}", e, document.toString());
                }
            }
        }
        return neeFilter;
    }

}
