package com.ifeng.recallScheduler.constant.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 缓存打分过滤的文章id 的分值
 */
public class DocSansuScoreCache {
    private static final Logger logger = LoggerFactory.getLogger(DocSansuScoreCache.class);
    public static volatile Cache<String, Integer> docScore;


    static {
        initCacheIds();
    }

    private static void initCacheIds() {
        docScore = CacheBuilder
                .newBuilder()
                .recordStats()
                .concurrencyLevel(10)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .initialCapacity(1000000)
                .maximumSize(1000000)
                .build();
    }

    public static Map<String, Integer> checkFilteredId(Set<String> ids) {
        return docScore.getAllPresent(ids);
    }


    public static Integer getDocScore(String docId) {
        return docScore.getIfPresent(docId);
    }

    public static void putFilteredId(String id, int score) {
        docScore.put(id, score);
    }


    public static void checkStatus() {
        logger.info("docScore hit_count:{} hit_rate:{} load_count:{} cache_size:{}", docScore.stats().hitCount(), docScore.stats().hitRate(), docScore.stats().loadCount(), docScore.size());
    }


    public static void main(String[] args) {
        System.out.println(getDocScore("111"));
        putFilteredId("111", 10);
        System.out.println(getDocScore("111"));
    }
}
