package com.ifeng.recallScheduler.apolloConf;

import com.ctrip.framework.apollo.Apollo;
import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by lilg1 on 2017/11/2.
 * 从配置中心获取debug用户配置
 */
@Service
public class DebugUserConfig {

    private static Map<String,Set<String>> debugUserMap;

    private static Logger logger = LoggerFactory.getLogger(Apollo.class);

    static {
        debugUserMap = Maps.newHashMap();
    }

    public void init(Config debugConfig){
        String yml = debugConfig.getProperty(ApolloConstant.Debug_User_Key, "");
        loadDeubgUserConfig(yml);
    }

    public void onChangeJob(ConfigChangeEvent configChangeEvent){
        //property未更新，不执行修改
        if(!configChangeEvent.changedKeys().contains(ApolloConstant.Debug_User_Key)){
            return;
        }
        ConfigChange configChange = configChangeEvent.getChange(ApolloConstant.Debug_User_Key);
        String yml = configChange.getNewValue();
        logger.info("update debug user configuration:  {}", yml);
        loadDeubgUserConfig(yml);
    }

    /**
     *
     * @param updateDebugUserMap
     */
    public void updateDebugUsers(Map<String,Set<String>> updateDebugUserMap){
        if(updateDebugUserMap !=null){
            debugUserMap.putAll(updateDebugUserMap);
        }
    }

    /**
     *
     * @param key
     * @param userSet
     */
    public void putUserSet(String key,Set<String> userSet){
        if(debugUserMap !=null ){
            debugUserMap.put(key,userSet);
        }
    }

    /**
     *
     * @param debugerSetKey
     * @return
     */
    public Set<String> getDebugUser(String debugerSetKey){
        Set<String> debugUsers = debugUserMap.get(debugerSetKey);
        if(debugUsers != null){
            return debugUsers;
        }
        return Sets.newHashSet();
    }

    /**
     * 解析yml配置到debug用户组
     */
    public void loadDeubgUserConfig(String yml) {
        Yaml yaml = new Yaml();
        try {
            if (StringUtils.isBlank(yml)) {
                logger.warn("parse debug user configuration empty! ");
            }
            Object obj = yaml.load(yml);
            Map<String, Object> userMap = (Map) obj;
            for (String key : userMap.keySet()) {
                List<Object> list = (List<Object>) userMap.get(key);
                Set<String> userSet = Sets.newHashSet();
                list.forEach(x -> userSet.add(String.valueOf(x)));
                this.putUserSet(key, userSet);
            }
        } catch (Exception e) {
            logger.error("parse debug user configuration error: {}", e);
        }
    }
}
