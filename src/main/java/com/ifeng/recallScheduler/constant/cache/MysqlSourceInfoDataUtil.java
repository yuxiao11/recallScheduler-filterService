package com.ifeng.recallScheduler.constant.cache;

import com.ifeng.recallScheduler.dao.SourceInfoDao;
import com.ifeng.recallScheduler.dao.VideoSourceInfoDao;
import com.ifeng.recallScheduler.item.SourceInfoItem;
import com.ifeng.recallScheduler.rule.impl.WeMediaSourceFilterHandler;
import com.ifeng.recallScheduler.utils.SpringConstantUtil;
import com.ifeng.recallScheduler.utils.VideoSourceInfoDataUtil;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * Created by lilg1 on 2018/3/29.
 */
@Service
public class MysqlSourceInfoDataUtil {

    @Autowired
    CacheManager cacheManager;

    @Autowired
    SourceInfoDao sourceInfoDao;

    @Autowired
    VideoSourceInfoDao videoSourceInfoDao;

    @Autowired
    SourceInfoDataUtil sourceInfoDataUtil;

    @Autowired
    VideoSourceInfoDataUtil videoSourceInfoDataUtil;

    @Autowired
    WeMediaSourceFilterHandler weMediaSourceFilterHandler;

    @Autowired
    private SpringConstantUtil springConstantUtil;




    private static final Logger logger = LoggerFactory.getLogger(MysqlSourceInfoDataUtil.class);

    /**
     * 更新媒体评级
     */
    @PostConstruct
    public void updateMediaInfo() {

        long start=System.currentTimeMillis();
        Cache sourceInfo_Cache = cacheManager.getCache(CacheFactory.CacheName.SourceInfo_EvalLevel.getValue());

        long updateStart = System.currentTimeMillis();

        List<SourceInfoItem> sourceInfoList = sourceInfoDao.getSourceInfo();
        int sourceLevelUpdateCount = 0;
        int orgUpdateCount = 0;

        //更新媒体评级cache
        for (SourceInfoItem sourceInfoItem : sourceInfoList) {
            String evalLevelOld = sourceInfoDataUtil.getSourceLevelByCache(sourceInfo_Cache, sourceInfoItem.getManuscriptName());
            if (StringUtils.isNotBlank(sourceInfoItem.getComEvalLevel())) {
                sourceInfo_Cache.put(new Element(sourceInfoItem.getManuscriptName(), sourceInfoItem.getComEvalLevel()));
                logger.warn("updateSourceLevel :{},level:{},  levelOld:{}", sourceInfoItem.getManuscriptName(), sourceInfoItem.getComEvalLevel(), evalLevelOld);
                sourceLevelUpdateCount++;
            }
        }

        //更新机构媒体数据cache
        long updateOrgStart = System.currentTimeMillis();
        List<String> orgSourceList = sourceInfoDao.getOrganizationMedia();
        for (String sourceName : orgSourceList) {
            weMediaSourceFilterHandler.putMediaSource(sourceName);
            orgUpdateCount++;
        }

        long updateEnd = System.currentTimeMillis();
        logger.info("updateSourceInfo total cost:{}, sourceLevel cost:{} count:{}, organizationSource cost:{} count:{}",
                updateEnd - updateStart, updateOrgStart - updateStart, sourceLevelUpdateCount, updateEnd - updateOrgStart, orgUpdateCount);
        logger.info("updateMediaInfo init cost:{}", System.currentTimeMillis() - start);
    }

    /**
     * 更新视频媒体评级信息
     */
    @PostConstruct
    public void updateVideoMediaInfo() {
        if (springConstantUtil.init_dev_close()) {
            return;
        }

        long start=System.currentTimeMillis();
        Cache videoSource_Cache = cacheManager.getCache(CacheFactory.CacheName.VideoSource_EvalLevel.getValue());

        long updateStart = System.currentTimeMillis();

        List<SourceInfoItem> sourceInfoList = videoSourceInfoDao.getVideoSourceInfo();
        int updateCount = 0;

        for (SourceInfoItem sourceInfoItem : sourceInfoList) {
            String evalLevelOld = videoSourceInfoDataUtil.getVideoLevelByCache(videoSource_Cache, sourceInfoItem.getManuscriptName());
            if (StringUtils.isNotBlank(sourceInfoItem.getComEvalLevel())) {
                videoSource_Cache.put(new Element(sourceInfoItem.getManuscriptName(), sourceInfoItem.getComEvalLevel()));
                logger.warn("updateVideo SourceLevel: {},level:{}, levelOld:{}", sourceInfoItem.getManuscriptName(), sourceInfoItem.getComEvalLevel(), evalLevelOld);
                updateCount++;
            }
        }
        long updateEnd = System.currentTimeMillis();
        logger.info("updateVideo SourceInfo total cost:{}, count:{}", updateEnd - updateStart, updateCount);
        logger.info("updateVideoMediaInfo init cost:{}", System.currentTimeMillis() - start);
    }

}
