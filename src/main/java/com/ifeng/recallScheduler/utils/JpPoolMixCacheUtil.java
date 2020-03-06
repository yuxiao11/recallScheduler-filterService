package com.ifeng.recallScheduler.utils;

import com.google.common.collect.Lists;
import com.ifeng.recallScheduler.constant.cache.CacheFactory;
import com.ifeng.recallScheduler.item.Document;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by lilg1 on 2017/11/24.
 */
@Service
public class JpPoolMixCacheUtil {

    private final static Logger logger = LoggerFactory.getLogger(JpPoolMixCacheUtil.class);

    @Autowired
    private CacheManager cacheManager;

    /**
     * 查询mix更新缓存，
     *
     * @param uid
     * @param list
     * @return
     */
    public void updateIndexCache(String uid, List<Document> list) {
        Cache jpMixIndexCache = cacheManager.getCache(CacheFactory.CacheName.JpPoolMixIndex.getValue());

        try {
            List<String> docIds = list.stream().map(x -> x.getDocId()).collect(Collectors.toList());
            Element element = new Element(uid, docIds);
            jpMixIndexCache.put(element);
        } catch (Exception e) {
            logger.error("update Mix JpPersonal {} Error: {}", uid, e);
        }
    }

    /**
     * 获取缓存中的个性化数据
     * @param uid
     * @return
     */
    public List<String> getJpMixPersonalDocuments(String uid) {
        List<String> recomIdList = Lists.newArrayList();

        Cache jpMixIndexCache = cacheManager.getCache(CacheFactory.CacheName.JpPoolMixIndex.getValue());

        try {
            Element jpIndexElement = jpMixIndexCache.get(uid);
            if (jpIndexElement == null) {
                return recomIdList;
            }
            List<String> indexIds = (List<String>) jpIndexElement.getObjectValue();
            recomIdList.addAll(indexIds);
        } catch (Exception e) {
            logger.error("Get JpPool Mix Cache Error: {}", e);
        }

        return recomIdList;
    }
}
