package com.ifeng.recallScheduler.utils;


import com.ifeng.recallScheduler.constant.GyConstant;
import com.ifeng.recallScheduler.constant.cache.CacheFactory;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.annotations.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by lilg1 on 2018/3/29.
 */
@Service
public class VideoSourceInfoDataUtil {

    @Autowired
    CacheManager cacheManager;

    private final static Logger logger = LoggerFactory.getLogger(VideoSourceInfoDataUtil.class);

    public String getVideoSourceLevel(String source) {
        Cache videoSource_Cache = cacheManager.getCache(CacheFactory.CacheName.VideoSource_EvalLevel.getValue());

        String videoSourceLevel = null;
        videoSourceLevel = getVideoLevelByCache(videoSource_Cache, source);
        return videoSourceLevel;
    }

    public String getVideoLevelByCache(Cache videoSource_Cache, String source) {
        if (StringUtils.isBlank(source)) {
            return null;
        }

        String result = null;
        try {
            Element videoSourceElement = videoSource_Cache.get(source);
            if (videoSourceElement == null && source.endsWith(GyConstant.source_zhonghe)) {
                String sourceTmp = source.substring(0, source.indexOf(GyConstant.source_zhonghe));
                videoSourceElement = videoSource_Cache.get(sourceTmp);
                if (videoSourceElement != null) {
                    result = (String) videoSourceElement.getObjectValue();
                    videoSource_Cache.put(new Element(source, result));
                    logger.info("{},{},video update Levle:{}", source, sourceTmp, result);
                }
            } else  if (videoSourceElement != null) {
                result = (String) videoSourceElement.getObjectValue();
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("get VideoSourceLevel source:{} ERROR:{}", source, e);
        }

        return result;
    }


    public static void main(String[] args) {
        String str = "直播港澳台综合综合";

        if (str.endsWith("综合")) {
            str = str.substring(0,str.indexOf("综合"));
            System.out.println(str);
        }
    }
}
