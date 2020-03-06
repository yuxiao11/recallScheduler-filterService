package com.ifeng.recallScheduler.utils;

import com.beust.jcommander.internal.Maps;
import com.google.common.cache.CacheBuilder;
import com.ifeng.recallScheduler.constant.GyConstant;
import com.ifeng.recallScheduler.params.RedisParams;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;


/**
 * 冷启动的热门推荐缓存数据
 * Created by jibin on 2017/6/23.
 */
@Service
public class BlackDocUtil {


    protected static Logger logger = LoggerFactory.getLogger(BlackDocUtil.class);

    @Autowired
    private SpringConstantUtil springConstantUtil;

    public static com.google.common.cache.Cache<String, Integer> blackDocCache = CacheBuilder
            .newBuilder()
            .recordStats()
            .concurrencyLevel(15)
            .expireAfterWrite(48, TimeUnit.HOURS)
            .initialCapacity(1500000)
            .maximumSize(1500000)
            .build();


    @PostConstruct
    public void initFirst() {

        long start = System.currentTimeMillis();
        try {
            CachePersist.loadToCache(BlackDocUtil.blackDocCache, GyConstant.blackDocCache);
            Map<String, Integer> blackMap = BlackDocUtil.blackDocCache.asMap();
            if (blackMap == null || blackMap.size() < GyConstant.cacheNum_Min) {
                blackMap = getBlackDocMap();
                logger.info("BlackDocUtil initFirst byRedis,size:{}", blackMap.keySet().size());
            } else {
                logger.info("BlackDocUtil initFirst byFile,size:{}", blackMap.keySet().size());
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("BlackDocUtil initFirst ERROR:{}", e);
        }
        logger.info("BlackDocUtil initFirst cost:{}", System.currentTimeMillis() - start);
    }


    public Map<String, Integer> getBlackDocMap() {
        long start = System.currentTimeMillis();
        Map<String, Integer> blackDoc = Maps.newHashMap();
        Set<String> list = RedisUtil.getKeys(RedisParams.getBlackList_IP(), RedisParams.getBlackList_port(), RedisParams.getBlackList_db(), 3000);
        if (CollectionUtils.isEmpty(list)) {
            list = RedisUtil.getKeys(RedisParams.getBlackList_IP(), RedisParams.getBlackList_port(), RedisParams.getBlackList_db(), 3000);
        }

        if (CollectionUtils.isEmpty(list)) {
            logger.error("BLACK LIST EMPTY!!! {},{} ,{}", RedisParams.getBlackList_IP(), RedisParams.getBlackList_port(), RedisParams.getBlackList_db());
        } else {
            for (String id : list) {
                blackDoc.put(id, 1);
            }
            BlackDocUtil.blackDocCache.putAll(blackDoc);
            logger.info("BlackDocUtil init size:{}, cost:{}", blackDoc.size(), System.currentTimeMillis() - start);
        }
        return blackDoc;
    }


    /**
     * 将文章id写入 黑名单
     * @param docId
     */
    public void addToBlackDocRedis(String docId){
        if (StringUtils.isNotBlank(docId)){
            RedisUtil.setex(RedisParams.getBlackList_IP(), RedisParams.getBlackList_port(), RedisParams.getBlackList_db(), docId, "state=-1", 864000);
        }
    }

}
