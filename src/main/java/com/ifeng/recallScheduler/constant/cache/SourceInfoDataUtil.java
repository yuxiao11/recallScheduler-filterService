package com.ifeng.recallScheduler.constant.cache;

import com.ifeng.recallScheduler.constant.GyConstant;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 获取媒体源信息
 * Created by jibin on 2018/1/10.
 */
@Service
public class SourceInfoDataUtil {

    protected static Logger logger = LoggerFactory.getLogger(SourceInfoDataUtil.class);

    @Autowired
    private CacheManager cacheManager;

    /**
     * 获取source的等级
     *
     * @param source
     * @return
     */
    public String getSourceLevel(String source) {
        Cache sourceInfo_EvalLevelCache = cacheManager.getCache(CacheFactory.CacheName.SourceInfo_EvalLevel.getValue());
        return getSourceLevelByCache(sourceInfo_EvalLevelCache, source);
    }


    /**
     * 从cache中获取sourceInfo
     *
     * @param SourceInfo_EvalLevelCache
     * @param source
     * @return
     */
    public String getSourceLevelByCache(Cache SourceInfo_EvalLevelCache, String source) {
        if (org.apache.commons.lang.StringUtils.isBlank(source)) {
            return null;
        }

        String result = null;
        try {
            Element SourceInfoElement = SourceInfo_EvalLevelCache.get(source);
            if (SourceInfoElement == null && source.endsWith(GyConstant.source_zhonghe)) {
                String sourceTmp = source.substring(0, source.indexOf(GyConstant.source_zhonghe));
                SourceInfoElement = SourceInfo_EvalLevelCache.get(sourceTmp);
                if (SourceInfoElement != null) {
                    result = (String) SourceInfoElement.getObjectValue();
                    SourceInfo_EvalLevelCache.put(new Element(source, result));
                    logger.info("{},{},doc update Levle:{}", source, sourceTmp, result);
                }
            } else if (SourceInfoElement != null) {
                result = (String) SourceInfoElement.getObjectValue();
            }




        } catch (Exception e) {
            e.printStackTrace();
            logger.error("getSourceLevelByCache source:{} ERROR:{}", source, e);
        }
        return result;
    }


}
