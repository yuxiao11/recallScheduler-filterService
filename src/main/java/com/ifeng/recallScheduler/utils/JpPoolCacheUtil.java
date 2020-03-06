package com.ifeng.recallScheduler.utils;

import com.google.gson.reflect.TypeToken;

import com.ifeng.recallScheduler.constant.cache.JpPoolTagCache;
import com.ifeng.recallScheduler.item.Document;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jibin on 2017/10/17.
 */
@Service
public class JpPoolCacheUtil {
    private final static Logger logger = LoggerFactory.getLogger(JpPoolDocUtil.class);


    public static final String jpTag_ip = "10.80.82.141";
    public static final int jpTag_port = 6380;
    public static final int jpTag_db = 7;
    public static final String jpTag_pattern = "*";

    @PostConstruct
    public boolean loadCache() {
        Long startTime=System.currentTimeMillis();
        try {
            List<String> keys = RedisUtil.getKeysByScan(jpTag_ip, jpTag_port, jpTag_db, 500, jpTag_pattern);
            if (CollectionUtils.isEmpty(keys)) {
                keys = RedisUtil.getKeysByScan(jpTag_ip, jpTag_port, jpTag_db, 500, jpTag_pattern);
            }
            List<Document> jpDocs = null;
            for (String key : keys) {
                try {
                    if (StringUtils.isNotBlank(key)) {
                        String str = RedisUtil.get(jpTag_ip, jpTag_port, jpTag_db, key);
                        if(StringUtils.isNotBlank(str)){
                            jpDocs =  JsonUtil.json2Object(str, new TypeToken<List<Document>>() {
                            }.getType());

                            if(CollectionUtils.isNotEmpty(jpDocs)){
                                JpPoolTagCache.jpPoolCache.put(key,jpDocs);
                            }
                        }
                    }
                } catch (Exception e) {
                     logger.error("{} getKey error:{}",key,e);
                }
            }
        } catch (Exception e) {
            logger.error("{} JpPoolCacheUtil  error:{}",e);
        }
        logger.info("jpPoolCache size:{},cost:{}",JpPoolTagCache.jpPoolCache.size(),System.currentTimeMillis()-startTime);
        return true;
    }


    public static List<Document> getFromCache(String tag) {
        List<Document> jpDocs = new ArrayList<>();
        try {
            jpDocs = JpPoolTagCache.jpPoolCache.getIfPresent(tag);
            if(CollectionUtils.isEmpty(jpDocs)){
                return new ArrayList<>();
            }
        } catch (Exception e) {
            logger.error("uid:{},JpPoolCacheUtil error:{}", tag, e);
        }
        return jpDocs;
    }


    public static void main(String[] args) {
        JpPoolCacheUtil j=new JpPoolCacheUtil();
        j.loadCache();
        List<Document> results = getFromCache("军事");
        System.out.println(results);
    }

}
