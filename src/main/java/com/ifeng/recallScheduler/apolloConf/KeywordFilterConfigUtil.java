package com.ifeng.recallScheduler.apolloConf;

import com.ctrip.framework.apollo.Apollo;
import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.testng.collections.Maps;

import java.util.Map;
import java.util.Set;

/**
 * @author liangky
 * @Date 2017/11/20
 */

@Service
public class KeywordFilterConfigUtil {

    private static Logger logger = LoggerFactory.getLogger(Apollo.class);

    public static Map<String, Set<String>> toBeFilteredKeywordMap;

    static {
        toBeFilteredKeywordMap = Maps.newHashMap();
    }

    //----------------public 方法---------------------------------------------------------------------

    /**
     * @param keywordSetKey
     * @return
     */
    public Set<String> getToBeFilteredKeywordSet(String keywordSetKey) {
        Set<String> keywordSet = toBeFilteredKeywordMap.get(keywordSetKey);
        if (keywordSet != null) {
            return keywordSet;
        }
        return Sets.newHashSet();
    }

    public void onChangeJob(ConfigChangeEvent configChangeEvent) {
        Set<String> changedKeys = configChangeEvent.changedKeys();
        for (String changedKey : changedKeys) {
            ConfigChange configChange = configChangeEvent.getChange(changedKey);
            String value = configChange.getNewValue();
            loadToBeFilteredKeywordConfig(changedKey,value);
            logger.info("update [to be filtered keyword] configuration, changeKey:[{}] ,new value:[{}]" ,changedKey,value);
        }
    }

    /**
     * @param toBeFilteredKeywordConfig
     */
    public void init(Config toBeFilteredKeywordConfig) {
        Set<String> propertyNames = toBeFilteredKeywordConfig.getPropertyNames();
        for (String propertyName : propertyNames) {
            String value = toBeFilteredKeywordConfig.getProperty(propertyName,"");
            loadToBeFilteredKeywordConfig(propertyName,value);
        }
    }

    //----------------private 方法---------------------------------------------------------------------

    /**
     *
     * @param propertyName
     * @param keyWordStringSplitByComma
     */
    private void loadToBeFilteredKeywordConfig(String propertyName,String keyWordStringSplitByComma) {
        try {
            Set<String> keywordSet = Sets.newHashSet();
            String[] keywordArray = keyWordStringSplitByComma.split(",");
            for (String keyword : keywordArray) {
                keywordSet.add(keyword);
            }
            this.putKeywordSet(propertyName, keywordSet);
        } catch (Exception e) {
            logger.error("parse [to be filtered keyword] configuration error: {} ", e);
        }
    }

    /**
     * @param key
     * @param keywordSet
     */
    private void putKeywordSet(String key, Set<String> keywordSet) {
        if (toBeFilteredKeywordMap != null) {
            toBeFilteredKeywordMap.put(key, keywordSet);
        }
    }

}
