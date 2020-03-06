package com.ifeng.recallScheduler.rule.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import com.ifeng.recallScheduler.constant.GyConstant;
import com.ifeng.recallScheduler.filter.ResourcesIO;
import com.ifeng.recallScheduler.item.Document;
import com.ifeng.recallScheduler.utils.CachePersist;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * 自媒体来源帖子进行过滤
 * Created by jibin on 2018/1/31.
 */
@Service
public class WeMediaSourceFilterHandler {

    private final static Logger log = LoggerFactory.getLogger(WeMediaSourceFilterHandler.class);

    private final ResourcesIO resourcesIO;


    public static Cache<String, Integer> cache = CacheBuilder
            .newBuilder()
            .concurrencyLevel(10)
            .initialCapacity(500000)
            .maximumSize(500000)
            .build();

    /**
     * cache标记
     */
    private static final int checkFlag = 1;


    @Autowired
    public WeMediaSourceFilterHandler(ResourcesIO resourcesIO) {
        this.resourcesIO = resourcesIO;
    }

    @PostConstruct
    public void init() {
        long start=System.currentTimeMillis();

        int num = CachePersist.loadToCache(WeMediaSourceFilterHandler.cache, GyConstant.WeMediaSourceCacheOfpath);
        if(num<GyConstant.WeMediaNum){
            updateWeMedia();
        }
        log.info("WeMediaSourceFilterHandler init cost:{}", System.currentTimeMillis() - start);
    }


    public void updateWeMedia() {
        List<String> weMediaSourceList = resourcesIO.loadWeMediaName();
        for (String source : weMediaSourceList) {
            cache.put(source, checkFlag);
        }
    }


    /**
     * 更新机构媒体数据
     * @param source
     * @return
     */
    public boolean putMediaSource(String source) {
        if (StringUtils.isBlank(source)) {
            return false;
        }
        if (sourceNeedFilter(source)) {
            cache.put(source, checkFlag);
            log.warn("Put Organization Media Source: {}", source);
            return true;
        }
        return false;
    }

    /**
     * 判断是否是自媒体的 source
     *
     * @param document
     * @return
     */
    public boolean sourceNeedFilter(Document document) {
        if (document == null || StringUtils.isBlank(document.getSource())) {
            return true;
        }
        boolean neeFilter = false;
        String source = document.getSource();

        //不在白名单内的则进行过滤
        Integer check = cache.getIfPresent(source);
        if (check == null) {
            neeFilter = true;
        }
        return neeFilter;
    }


    /**
     * 判断是否是自媒体的 source
     *
     * @param source
     * @return
     */
    public boolean sourceNeedFilter(String source) {
        boolean neeFilter = false;
        //不在白名单内的则进行过滤
        Integer check = cache.getIfPresent(source);
        if (check == null) {
            neeFilter = true;
        }
        return neeFilter;
    }


    public static void main(String[] args) {
        cache.put("aa", 1);
        System.out.println(cache.getIfPresent("aa"));
        System.out.println(cache.getIfPresent("bb"));
        System.out.println(cache.getIfPresent("cc"));
    }

}
