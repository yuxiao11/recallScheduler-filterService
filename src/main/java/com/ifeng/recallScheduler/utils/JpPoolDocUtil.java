package com.ifeng.recallScheduler.utils;


import com.ifeng.recallScheduler.constant.RecWhy;
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

/**
 * Created by jibin on 2017/10/17.
 */
@Service
public class JpPoolDocUtil {


    private final static Logger logger = LoggerFactory.getLogger(JpPoolDocUtil.class);

    @Autowired
    private CacheManager cacheManager;

    /**
     * 更新精品池document cache
     *
     * @param jpList
     * @return
     */
    public boolean updateDocCache(List<Document> jpList) {
        Cache jpdocCache = cacheManager.getCache(CacheFactory.CacheName.JpPoolDocument.getValue());
        for (Document doc : jpList) {
            updateDocCache(jpdocCache, doc);
        }
        return true;
    }


    /**
     * 更新doc的缓存
     *
     * @param docCache
     * @param doc
     * @return
     */
    public boolean updateDocCache(Cache docCache, Document doc) {
        try {
            doc = doc.clone();
            doc.setWhy(RecWhy.WhyJpPoolRecomCache);
            Element element = new Element(doc.getDocId(), doc);
            docCache.put(element);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("update JpDoc {} Error:{}", doc.getDocId(), e);
        }
        return true;
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
     * 从cache中查找Document是否存在
     *
     * @param docId
     * @return
     */
    public boolean checkDocIsNull(Cache docCache, String docId) {
        Element docElement = docCache.get(docId);
        return (docElement == null);
    }

}
