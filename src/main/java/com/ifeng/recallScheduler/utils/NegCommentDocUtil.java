package com.ifeng.recallScheduler.utils;

import com.beust.jcommander.internal.Sets;
import com.ifeng.recallScheduler.constant.cache.CacheFactory;
import com.ifeng.recallScheduler.dao.NegCommentDocDao;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Set;


/**
 * 冷启动的热门推荐缓存数据
 * Created by jibin on 2017/6/23.
 */
@Service
public class NegCommentDocUtil {


    protected static Logger logger = LoggerFactory.getLogger(NegCommentDocUtil.class);

    @Autowired
    private EhCacheUtil ehCacheUtil;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private NegCommentDocDao negCommentDocDao;


    @Autowired
    private SpringConstantUtil springConstantUtil;



    /**
     * 加载负评论缓存数据
     *
     * @return true
     */
    @PostConstruct
    public boolean loadCacheFromSql() {
        if (springConstantUtil.init_dev_close()) {
            return true;
        }

        long start = System.currentTimeMillis();
        try {

            Set<String>  negCommentLevel5DocIdSet = null;
            Set<String>  negCommentLevel1To4DocIdSet = null;
            //初始化 缓存
            Cache negCommentBlackCache = cacheManager.getCache(CacheFactory.CacheName.NegCommentBlack.getValue());

            //获取 level 5 负评文章 针对 全端用户屏蔽
            try {
                negCommentLevel5DocIdSet = negCommentDocDao.getNegCommentDocsLevel5Set();
            } catch (Exception e) {
                logger.error("getNegCommentDocsLevel5Set loadCacheFromSql ERROR !!! {}", e.toString());
            }
            //判断是否为空
            if (CollectionUtils.isEmpty(negCommentLevel5DocIdSet)) {
                logger.error("negCommentLevel5DocIdSet EMPTY!!! ");
                negCommentLevel5DocIdSet = Sets.newHashSet();
            }else{
                //代码优化，这里只向cache put一次，后面的都是直接在堆中操作
                Element element = new Element(CacheFactory.CacheName.Level5NegCommentBlack.getValue(), negCommentLevel5DocIdSet);
                negCommentBlackCache.put(element);
            }
            logger.warn("update negCommentLevel5 docs from mysql {} ", negCommentLevel5DocIdSet.size());


            //获取 1-4 level 负评文章 针对boss用户屏蔽
            try {
                negCommentLevel1To4DocIdSet = negCommentDocDao.getNegCommentDocsLevel1To4Set();
            } catch (Exception e) {
                logger.error("getNegCommentDocsLevel1To4Set loadCacheFromSql ERROR !!! {}", e.toString());
            }
            //判断是否为空
            if (CollectionUtils.isEmpty(negCommentLevel1To4DocIdSet)) {
                logger.error("negCommentLevel1To4DocIdSet EMPTY!!! ");
                negCommentLevel1To4DocIdSet = Sets.newHashSet();
            }else{
                //这里只向cache put一次
                Element element = new Element(CacheFactory.CacheName.Level1To4NegCommentBlack.getValue(), negCommentLevel1To4DocIdSet);
                negCommentBlackCache.put(element);
            }
            logger.warn("update negCommentLevel1To4 docs from mysql {} ", negCommentLevel1To4DocIdSet.size());



        } catch (Exception e) {
            logger.error("negComment ERROR :{}", e.toString());
        }
        logger.info("negComment black docs init cost:{}", System.currentTimeMillis() - start);

        return true;
    }

}
