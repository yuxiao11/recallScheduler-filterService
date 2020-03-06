package com.ifeng.recallScheduler.utils;

import com.beust.jcommander.internal.Sets;
import com.ifeng.recallScheduler.apolloConf.ApolloConstant;
import com.ifeng.recallScheduler.apolloConf.ApplicationConfig;
import com.ifeng.recallScheduler.constant.cache.CacheFactory;
import com.ifeng.recallScheduler.dao.SansuDocDao;
import com.ifeng.recallScheduler.params.RedisParams;
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
public class SanSuDocUtil {


    protected static Logger logger = LoggerFactory.getLogger(SanSuDocUtil.class);

    @Autowired
    private EhCacheUtil ehCacheUtil;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private SansuDocDao sansuDocDao;


    /**
     * 运营维护的长效的冷启动的缓存数据的redis ip
     */
    private static final String ip_hot = RedisParams.getSansuDocFilter_IP();
    /**
     * 运营维护的长效的冷启动的缓存数据的redis端口
     */
    private static final int port_hot = RedisParams.getSansuDocFilter_port();

    /**
     * 运营维护的长效的冷启动的缓存数据的redis dbNum
     */
    private static final int dbNum_hot = RedisParams.getSansuDocFilter_db();



    @Autowired
    private SpringConstantUtil springConstantUtil;





    /**
     * 加载三俗缓存数据
     *
     * @return
     */
    @PostConstruct
    public boolean loadCacheFromSql() {
        if (springConstantUtil.init_dev_close()||ApolloConstant.Switch_off.equals(ApplicationConfig.getProperty(ApolloConstant.SansuSwitch))) {
            logger.warn("update SansuBlack from mysql {} is off");
            return true;
        }

        long start = System.currentTimeMillis();
        try {

            Set<String>  sansuIdSet=null;
            try {
                sansuIdSet = sansuDocDao.getSansuDocSet();
            } catch (Exception e) {
                logger.error("SansuBlack loadCacheFromSql ERROR First!!! {}", e);
            }

            if (CollectionUtils.isEmpty(sansuIdSet)) {
                try {
                    sansuIdSet = sansuDocDao.getSansuDocSet();
                } catch (Exception e) {
                    logger.error("SansuBlack loadCacheFromSql ERROR second!!! {}", e);
                }
            }


            if (CollectionUtils.isEmpty(sansuIdSet)) {
                logger.error("SansuBlack EMPTY!!! ");
                sansuIdSet= Sets.newHashSet();
            }

            Cache sansuBlackCache = cacheManager.getCache(CacheFactory.CacheName.SansuBlack.getValue());
            int count = 0;
            //代码优化，这里只向cacheput一次，后面的都是直接在堆中操作
            if (CollectionUtils.isNotEmpty(sansuIdSet)) {
                Element element = new Element(CacheFactory.CacheName.SansuBlack.getValue(), sansuIdSet);
                sansuBlackCache.put(element);
            }
            logger.warn("update SansuBlack from mysql {} ", sansuIdSet.size());
        } catch (Exception e) {
            logger.error("SansuBlack ERROR :{}", e);
        }
        logger.info("SansuBlack init cost:{}", System.currentTimeMillis() - start);
        return true;
    }

}
