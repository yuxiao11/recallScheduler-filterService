package com.ifeng.recallScheduler.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ifeng.recallScheduler.item.Document;
import com.ifeng.recallScheduler.item.EditorInsertItem;
import com.ifeng.recallScheduler.user.RecordInfo;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * Created by wupeng1 on 2017/6/6.
 * ehcache 缓存工具类
 */
@Service
public class EhCacheUtil {

    protected static Logger logger = LoggerFactory.getLogger(EhCacheUtil.class);

    @Autowired
    private CacheManager cacheManager;

    /**
     * 新增缓存记录
     */
    public void put(String cacheName, String key, Object value) {
        Cache cache = cacheManager.getCache(cacheName);
        if (null != cache) {
            Element element = new Element(key, value);
            cache.put(element);
        }
    }


    /**
     * 获取缓存记录
     */
    public Object get(String cacheName, String key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (null == cache) {
            return null;
        }
        Element cacheElement = cache.get(key);
        if (null == cacheElement) {
            return null;
        }
        return cacheElement.getObjectValue();
    }


    public List<String> getListStr(String cacheName, String key) {
        List<String> result = (List<String>) get(cacheName, key);
        if (result == null) {
            result = Lists.newArrayList();
        }
        return result;
    }

    public Set<String> getSetStr(String cacheName, String key) {
        Set<String> result = (Set<String>) get(cacheName, key);
        if (result == null) {
            result = Sets.newHashSet();
        }
        return result;
    }


    /**
     * 获取用户的统计信息
     * @param cacheName
     * @param key
     * @return
     */
    public List<RecordInfo> getRecordInfoList(String cacheName, String key) {
        List<RecordInfo> result = (List<RecordInfo>) get(cacheName, key);
        if (result == null) {
            result = Lists.newArrayList();
        }
        return result;
    }

    public List<Document> getListDoc(String cacheName, String key) {
        List<Document> result = (List<Document>) get(cacheName, key);
        if (result == null) {
            result = Lists.newArrayList();
        }
        return result;
    }

    public List<EditorInsertItem> getListEditor(String cacheName, String key) {
        List<EditorInsertItem> result = (List<EditorInsertItem>) get(cacheName, key);
        if (result == null) {
            result = Lists.newArrayList();
        }
        return result;
    }

    /**
     * 判断cache是否包含该key,返回Element
     *
     * @param cache
     * @param key
     * @return
     */
    public Element getElement(Cache cache, String key) {
        if (null == cache) {
            return null;
        }
        Element cacheElement = cache.get(key);
        return cacheElement;
    }

    /**
     * 判断cache是否包含该key
     *
     * @param cache
     * @param key
     * @return
     */
    public boolean containsElement(Cache cache, String key) {
        if (null == cache) {
            return false;
        }
        Element cacheElement = cache.get(key);
        return (cacheElement != null);
    }


    /**
     * 获取字符串信息
     * @param cache
     * @param key
     * @return
     */
    public String getStrValue(Cache cache, String key) {
        String value = null;
        try {
            Element valueElement = cache.get(key);
            if (valueElement != null) {
                value = (String) valueElement.getObjectValue();
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("cache getStr uid:{} ,ERROR:{}", key, e);
        }
        return value;
    }


}
