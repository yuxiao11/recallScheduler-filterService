package com.ifeng.recallScheduler.user;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ifeng.recallScheduler.constant.GyConstant;
import com.ifeng.recallScheduler.timer.TimerEntity;
import com.ifeng.recallScheduler.timer.TimerEntityUtil;
import com.ifeng.recallScheduler.utils.UserNegsJedisUtil;
import net.sf.ehcache.CacheManager;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 获取用户负反馈信息
 * Created by jibin on 2017/9/6.
 */
@Service
public class UserNegs {

    private static Logger logger = LoggerFactory.getLogger(UserNegs.class);

    @Autowired
    private UserNegsJedisUtil userNegsJedisUtil;


    /**
     * 负反馈截取 0～20
     */
    private final int start = 0;
    private final int end = 20;

    /**
     * 取实时不感兴趣特征分类->特征词
     *
     * @param userId
     * @return
     */
    public Map<String, List<String>> getNegMaps(String userId) {
        TimerEntity timer = TimerEntityUtil.getInstance();

        //全量不感兴趣特征
        timer.addStartTime("negByZrevrange");
        Set<String> NegsNFeatures = userNegsJedisUtil.getSetByZrevrange(userId, start, end);
        timer.addEndTime("negByZrevrange");

        //C类不感兴趣特征
        timer.addStartTime("negGetAllCSet");
        Set<String> negsCFeatures = userNegsJedisUtil.getAllCSet(userId);
        timer.addEndTime("negGetAllCSet");

        Set<String> negsFeatures = new HashSet<String>();

        if (CollectionUtils.isNotEmpty(NegsNFeatures)){
            negsFeatures.addAll(NegsNFeatures);//全量不感兴趣特征
        }
        if (CollectionUtils.isNotEmpty(negsCFeatures)){
            negsFeatures.addAll(negsCFeatures);//C类不感兴趣特征
        }

        if (CollectionUtils.isEmpty(negsFeatures)){
            return Collections.EMPTY_MAP; //C类不管兴趣特征
        }

        Map<String, List<String>> featruesMap = Maps.newHashMap();
        for (String feature : negsFeatures) {
            String[] item = feature.split(GyConstant.Symb_equal);
            if (item.length != 2)
                continue;
            String featureType = item[0];
            String featureValue = item[1];
            List<String> type = featruesMap.getOrDefault(featureType, Lists.newArrayList());
            type.add(featureValue);
            featruesMap.put(featureType, type);
        }
        return featruesMap;
    }
}
