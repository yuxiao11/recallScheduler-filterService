package com.ifeng.recallScheduler.apolloConf;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Properties;

/**
 * storm配置以及废弃，这里用作配置头条的视频条数控制
 * 管理增量相关的配置
 */
@Service
public class StormDataMergeConfigUtil {

    private static Logger logger = LoggerFactory.getLogger(StormDataMergeConfigUtil.class);

    public static volatile Properties properties;

    static {
        properties = new Properties();
    }

    public static void setProperty(String key,String value){
        if(StringUtils.isNotBlank(value)) {
            properties.setProperty(key,value);
        }else{
            //apollo new value为空时删除
            properties.remove(key);
        }
    }


    /**
     * 获取缓存中的double 配置
     *
     * @param key
     * @return
     */
    public static double getDoubleProperty(String key) {
        return NumberUtils.toDouble(getProperty(key));
    }

    public static String getProperty(String key){
        return properties.getProperty(key);
    }

    public static Properties getProperties(){
        return properties;
    }


    /**
     * 初始化
     *
     * @param commonConfig
     */
    public void init(Config commonConfig) {
        for (String key : commonConfig.getPropertyNames()) {
            String defaultValue = getProperty(key);
            String configValue = commonConfig.getProperty(key, defaultValue);
            setProperty(key, configValue);
            logger.info(" init configuration apollo config key: {}, config value: {}"
                    , key, configValue);
        }
    }

    /**
     * 更新监听storm的配置信息
     * @param configChangeEvent
     */
    public void onChangeJob(ConfigChangeEvent configChangeEvent) {
        try {
            for (String key : configChangeEvent.changedKeys()) {
                ConfigChange configChange = configChangeEvent.getChange(key);
                String value = configChange.getNewValue();
                setProperty(key, value);
                logger.info(" update StormDataMergeConfigUtil configuration apollo config key: {}, config value: {}"
                        , key, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("onChangeJob ERROR:{}", e);
        }
    }

}
