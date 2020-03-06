package com.ifeng.recallScheduler.constant.cache;

import com.google.common.cache.CacheBuilder;
import com.ifeng.recallScheduler.item.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 *
 * Created by jibin on 2017/6/23.
 */
@Service
public class JpPoolTagCache {


    protected static Logger logger = LoggerFactory.getLogger(JpPoolTagCache.class);



    public static com.google.common.cache.Cache<String, List<Document>> jpPoolTagCache = CacheBuilder
            .newBuilder()
            .recordStats()
            .concurrencyLevel(10)
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .initialCapacity(100000)
            .maximumSize(100000)
            .build();

    public static com.google.common.cache.Cache<String, List<Document>> jpPoolCache = CacheBuilder
            .newBuilder()
            .recordStats()
            .concurrencyLevel(10)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .initialCapacity(500000)
            .maximumSize(500000)
            .build();


}
