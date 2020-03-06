package com.ifeng.recallScheduler.filter;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.ifeng.recallScheduler.item.Document;
import com.ifeng.recallScheduler.request.RequestInfo;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import redis.clients.jedis.*;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by liligeng on 2019/2/13.
 */
@Service
public class MediaFilter {

    private static Logger logger = LoggerFactory.getLogger(MediaFilter.class);

    private static Cache<String, Integer> banMediaCache = CacheBuilder
            .newBuilder()
            .recordStats()
            .concurrencyLevel(15)
            .expireAfterWrite(5, TimeUnit.HOURS)
            .initialCapacity(10000)
            .maximumSize(10000)
            .build();


    private static JedisPool jedisPool;

    static {
        if (jedisPool == null) {
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(10);
            config.setMaxIdle(10);
            config.setMaxWaitMillis(10000);
            config.setTestOnBorrow(true);
            config.setTestOnReturn(true);
            config.setBlockWhenExhausted(true);
            jedisPool = new JedisPool(config, "10.90.11.60", 6380, 10000);

        }
    }

    private static int dbNum = 14;

    private static String mediaScanPattern = "media:*";
    private static String mediaFilterPrefix = "media:";

    private static String scoreFilterWordField_level = "sensetiveLevel";
    private static String scoreFilterWordField_expireTs = "expireTs";

    @PostConstruct
    public void loadCache() {
        long start = System.currentTimeMillis();
        try {
            Map<String, Integer> banMediaMap = loadMediaFromRedis();
            banMediaCache.putAll(banMediaMap);
        } catch (Exception e) {
            logger.error("init ban media error: {}", e);
        }
        logger.info("init ban media size:{} cost:{}", banMediaCache.size(), System.currentTimeMillis() - start);
    }

    public static Jedis getJedisClient(int dbNum) {
        Jedis jedis = jedisPool.getResource();
        if (jedis != null) {
            jedis.select(dbNum);
            return jedis;
        } else {
            return null;
        }
    }

    public boolean filterByMedia(RequestInfo requestInfo, Document document) {
        String source = document.getSource();
        if (StringUtils.isBlank(source)) {
            return false;
        }

        try {
            Integer score = banMediaCache.getIfPresent(source);
            if (score != null && score > 0) {
                if (score >= requestInfo.getTitleThreshold()) {
                    return true;
                }
            }
        } catch (Exception e) {
            logger.info("{} mediaFilter err:{}",requestInfo.getUserId(),e);
        }
        return false;
    }

    private Map<String, Integer> loadMediaFromRedis() {
        Map<String, Integer> banMediaMap = new HashMap<>();

        ScanParams scanParams = new ScanParams();
        scanParams.match(mediaScanPattern);
        scanParams.count(1000);

        Jedis jedis = getJedisClient(dbNum);
        try {
            ScanResult<String> scanResult = jedis.scan(ScanParams.SCAN_POINTER_START, scanParams);

            String nextCursor = scanResult.getStringCursor();
            List<String> scanResultMap = scanResult.getResult();
            long now = System.currentTimeMillis();

            while (true) {
                for (String key : scanResultMap) {
                    Map<String, String> mapTemp = jedis.hgetAll(key);
                    if (CollectionUtils.isEmpty(mapTemp)) {
                        continue;
                    }
                    long expireTs = NumberUtils.toLong(mapTemp.get(scoreFilterWordField_expireTs), 0l);
                    if (now <= expireTs) {
                        banMediaMap.put(StringUtils.replaceOnce(key, mediaFilterPrefix, ""),
                                NumberUtils.toInt(mapTemp.get(scoreFilterWordField_level), 1));
                    }
                }
                if (nextCursor == null || ScanParams.SCAN_POINTER_START.equals(nextCursor)) {
                    break;
                }
                scanResult = jedis.scan(nextCursor, scanParams);
                nextCursor = scanResult.getStringCursor();
                scanResultMap = scanResult.getResult();
            }
        } catch (Exception e) {
            logger.error("ScoreFilterCache loadError:{}", e);
        }

        if (jedis != null) {
            jedis.close();
        }
        return banMediaMap;
    }

    public static void main(String[] args) {
        MediaFilter mediaFilter = new MediaFilter();
        Map<String,Integer> map = mediaFilter.loadMediaFromRedis();
        System.out.println(map.size());
    }
}
