package com.ifeng.recallScheduler.filter;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.ifeng.recallScheduler.apolloConf.ApolloConstant;
import com.ifeng.recallScheduler.apolloConf.SafeStrategyConfig;
import com.ifeng.recallScheduler.item.Document;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by liligeng on 2019/9/18.
 */
@Service
public class SourceFilterByMediaId {

    private final static Logger logger = LoggerFactory.getLogger(SourceFilterByMediaId.class);

    //需要过滤的媒体id缓存
    private static Cache<String, Integer> mediaIdCache = CacheBuilder.newBuilder()
            .initialCapacity(20000)
            .maximumSize(20000)
            .build();

    @Autowired
    private SafeStrategyConfig safeStrategyConfig;


    @PostConstruct
    public void loadNeedFilteredSourceId() {
        try {
            InputStream in = ClassLoader.getSystemResourceAsStream("filterYidianMediaId.txt");
            BufferedReader reader;
            reader = new BufferedReader(new InputStreamReader(in));
            String text;
            while ((text = reader.readLine()) != null) {
                try {
                    text = text.trim().replace("\n", "").replace("\r", "");
                    mediaIdCache.put(text, 1);
                } catch (Exception e) {
                    logger.error("put need filter yidian mediaId to map error:{}, text:{}", e, text);
                }

            }
            logger.info("load need filter yidian mediaId size:{}", mediaIdCache.size());

        } catch (Exception e) {
            logger.info("load need filter yidian mediaId list err:{}", e);
        }
    }

    /**
     *
     * 过滤文章  即一点的文章不可用 1.apollo开关是否开启 2.并且当前时间小于一点文章的过期时间
     * @param document
     * @return
     */
    public boolean filterDocuments(Document document) {
        if(ApolloConstant.Switch_on.equals(safeStrategyConfig.getYidianSourceFilterSwitch()) && System.currentTimeMillis() < safeStrategyConfig.getYidianSourceFilterInvalidTime()) {
            String mediaId = document.getMediaId();
            if(StringUtils.isNotBlank(mediaId) && mediaIdCache.getIfPresent(mediaId)!=null){
                return true;
            }
        }

        return false;
    }
}
