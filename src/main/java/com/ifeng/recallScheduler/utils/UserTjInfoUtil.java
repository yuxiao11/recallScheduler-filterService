package com.ifeng.recallScheduler.utils;

import com.beust.jcommander.internal.Maps;
import com.ifeng.recallScheduler.apolloConf.ApolloConstant;
import com.ifeng.recallScheduler.apolloConf.DebugUserConfig;
import com.ifeng.recallScheduler.constant.GyConstant;
import com.ifeng.recallScheduler.constant.cache.CacheFactory;
import com.ifeng.recallScheduler.request.RequestInfo;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 用户统计信息的工具类
 * Created by jibin on 2017/12/20.
 */
@Service
public class UserTjInfoUtil {

    @Autowired
    private CacheManager cacheManager;


    @Autowired
    private DebugUserConfig debugUserConfig;


    /**
     * 记录耗时的日志
     */
    private static final Logger logger = LoggerFactory.getLogger(UserTjInfoUtil.class);


    public Map<String, Integer> getRecallTagCount(RequestInfo requestInfo) {

        Cache userEv_RecallTag_Cache = null;

        //boss用户的recallTag的多样性控制时间更长，为6小时
        if (debugUserConfig.getDebugUser(ApolloConstant.boss_User_Key).contains(requestInfo.getUserId())) {
            userEv_RecallTag_Cache = cacheManager.getCache(CacheFactory.CacheName.UserEv_RecallTag_Info_longTime.getValue());
        } else {
            userEv_RecallTag_Cache = cacheManager.getCache(CacheFactory.CacheName.UserEv_RecallTag_Info.getValue());
        }



        String uid = requestInfo.getUserId();
        Element userEv_RecallTag_Element = userEv_RecallTag_Cache.get(uid);
        Map<String, Integer> userEv_RecallTagCount = null;
        if (userEv_RecallTag_Element != null) {
            userEv_RecallTagCount = (Map<String, Integer>) userEv_RecallTag_Element.getObjectValue();
        } else {
            userEv_RecallTagCount = Maps.newHashMap();
            userEv_RecallTag_Cache.put(new Element(uid, userEv_RecallTagCount));
        }
        return userEv_RecallTagCount;
    }

    /**
     * 更新用户看过的RecallTag的信息
     * @param requestInfo
     */
    public void updateRecallTagCount(RequestInfo requestInfo) {
        Map<String,Integer> recallTagMap = requestInfo.getRecallTagMap();
        if (MapUtils.isEmpty(recallTagMap)) {
            return;
        }

        Map<String, Integer> userRecallTagMap = getRecallTagCount(requestInfo);

        int recallTagSizeLimit = 1000;
        //boss用户的recallTag的多样性控制时间更长，为6小时,对多记录1万个recallTag
        if (debugUserConfig.getDebugUser(ApolloConstant.boss_User_Key).contains(requestInfo.getUserId())) {
            recallTagSizeLimit = 5000;
        }

        if (userRecallTagMap.size() > recallTagSizeLimit) {
            userRecallTagMap.clear();
        }


        for (Map.Entry<String, Integer> entry : recallTagMap.entrySet()) {
            String recallTag = entry.getKey();
            int recallTagCount = entry.getValue();

            int baseCount = userRecallTagMap.getOrDefault(recallTag, 0);
            int countNew = baseCount + recallTagCount;
            if (countNew > GyConstant.countNew_Max) {
                countNew = GyConstant.countNew_Max;
            }
            userRecallTagMap.put(recallTag, countNew);
        }


    }

}
