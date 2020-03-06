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
public class DocSourceFilterConfigUtil {

    public static Map<String,Set<String>> docSourceMap;

    private static Logger logger = LoggerFactory.getLogger(Apollo.class);

    static{
        docSourceMap = Maps.newHashMap();
    }

    //----------------public 方法---------------------------------------------------------------------
    /**
     *
     * @param docSourceSetKey
     * @return
     */
    public Set<String> getDocSourceSet(String docSourceSetKey){
        Set<String> docSourceSet = docSourceMap.get(docSourceSetKey);
        if(docSourceSet!=null){
            return docSourceSet;
        }
        return Sets.newHashSet();
    }

    public void onChangeJob(ConfigChangeEvent configChangeEvent){

        Set<String> changedKeys = configChangeEvent.changedKeys();
        for (String changedKey : changedKeys) {
            ConfigChange configChange = configChangeEvent.getChange(changedKey);
            String value = configChange.getNewValue();
            loadDocSourceConfig(changedKey,value);
            logger.info("update [to be filter doc source] configuration, changeKey:[{}] ,new value:[{}]" ,changedKey,value);
        }
    }

    /**
     *
     * @param docSourceConfig
     */
    public void init(Config docSourceConfig){  //  DcoSourceToFilter  DcoSourceToFilter
        Set<String> propertyNames = docSourceConfig.getPropertyNames();  //  DcoSourceToFilter
        for (String propertyName : propertyNames) { //中青在线,淺笑嫣然,中国山东网,环球网台湾,人人视频
            String value = docSourceConfig.getProperty(propertyName,"");
            loadDocSourceConfig(propertyName,value);
        }
    }

    //----------------private 方法---------------------------------------------------------------------

    /**
     *
     * @param propertyName
     * @param docSourceStringSplitByComma
     */
    private void loadDocSourceConfig(String propertyName,String docSourceStringSplitByComma){
        try {
            Set<String> docSourceSet = Sets.newHashSet();
            String[] docSourceArray = docSourceStringSplitByComma.split(",");
            for (String docSource : docSourceArray) {
                docSourceSet.add(docSource);
            }
            this.putDocSourceSet(propertyName, docSourceSet);
        } catch (Exception e) {
            logger.error("parse [to be filter doc source] configuration error: {} ", e);
        }
    }

    /**
     *
     * @param key
     * @param docSourceSet
     */
    private void putDocSourceSet(String key, Set<String> docSourceSet){
        if(docSourceMap !=null ){
            docSourceMap.put(key,docSourceSet);
        }
    }
}
