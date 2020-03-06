package com.ifeng.recallScheduler.apolloConf;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by liligeng on 2019/9/12.
 */
@Service
@Getter
@Setter
public class SafeStrategyConfig {

    private static Logger logger = LoggerFactory.getLogger(SafeStrategyConfig.class);

    public static volatile Properties properties;

    private static Gson gson = new Gson();

    private static Type typeToken = new TypeToken<List<JpPreTopPhoneTypeConfig>>(){}.getType();

    //限制首屏分类
    private Set<String> restrictCSet = new HashSet<>();

    //限制首屏的二级分类
    private Set<String> restrictScSet = new HashSet<>();

    //限制分发区域
    private Set<String> restrictArea = new HashSet<>();

    //限制首屏关键词
    private Set<String> restrictKeyWord = new HashSet<>();

    //白名单媒体不做限制
    private Set<String> newsWhiteSource = new HashSet<>();

    //策略失效时间
    private long invalidTime = Long.MAX_VALUE;

    private int restrictIndex = 0;

    //pullDown下拉优质精选置顶条数
    private int pullDownJpPreTopSize = 0;

    //default优质精选置顶条数, default已经有一条优质精选
    private int defaultJpPreTopSize = 0;

    //编辑置顶策略时效时间
    private long jpPreTopInvalidTime = Long.MAX_VALUE;

    //编辑优质精选置顶开关
    private String jpPreTopSwitch = "off";

    //一点媒体过滤开关
    private String yidianSourceFilterSwitch = "off";

    //根据mediaId过滤一点账号时效时间
    private long yidianSourceFilterInvalidTime = Long.MAX_VALUE;

    //编辑优化置顶
    private Map<String, JpPreTopPhoneTypeConfig> phoneTypeConfigMap = new HashMap<>();


    static {
        properties = new Properties();
    }

    public static void setProperty(String key, String value) {
        if (StringUtils.isNotBlank(value)) {
            properties.setProperty(key, value);
        } else {
            //apollo new value为空时删除
            properties.remove(key);
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }


    /**
     * 获取缓存中的int 配置
     *
     * @param key
     * @return
     */
    public static int getIntProperty(String key) {
        return NumberUtils.toInt(getProperty(key), 0);
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


    public static Properties getProperties() {
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
        reloadConfig(properties);
    }


    /**
     * 更新监听storm的配置信息
     *
     * @param configChangeEvent
     */
    public void onChangeJob(ConfigChangeEvent configChangeEvent) {
        try {
            for (String key : configChangeEvent.changedKeys()) {
                ConfigChange configChange = configChangeEvent.getChange(key);
                String value = configChange.getNewValue();
                setProperty(key, value);
                logger.info(" update ApplicationConfig configuration apollo config key: {}, config value: {}"
                        , key, value);
            }
            reloadConfig(properties);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("onChangeJob ERROR:{}", e);
        }
    }


    /**
     * 启动或更新时对配置进行重新加载
     * @param properties
     */
    private void reloadConfig(Properties properties){
        //策略生效区域，all代表全部限制
        restrictArea = convertStr2Set(properties.getProperty("restrictArea"));

        //限制出现在首屏中的C分类
        restrictCSet = convertStr2Set(properties.getProperty("restrictCSet"));

        restrictScSet = convertStr2Set(properties.getProperty("restrictScSet"));

        //限制出现在首屏的关键词
        restrictKeyWord = convertStr2Set(properties.getProperty("restrictKeyword"));

        //热点白名单
        newsWhiteSource = convertStr2Set(properties.getProperty("newsWhiteSource"));

        //限制首屏index
        restrictIndex = getIntProperty("restrictIndex");

        try {
            String invalidDateStr = properties.getProperty("invalidTime");
            logger.info("load invalidStr:{}", invalidDateStr);
            invalidDateStr = invalidDateStr.replace("\\","");
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = formatter.parse(invalidDateStr);
            invalidTime = date.getTime();
        } catch (Exception e) {
            logger.error("parse invalidTime err:{}", e);
        }
        logger.info("reload safe strategy area:{}, cSet:{}, scSet:{}, keyword:{}, whiteSource:{}, index:{}, invalidTime:{}", restrictArea, restrictCSet, restrictScSet,
                restrictKeyWord, newsWhiteSource, restrictIndex, invalidTime);


        //---------- 编辑优质精选置顶开关及条数配置
        pullDownJpPreTopSize = getIntProperty("pullDownJpPreTopSize");
        defaultJpPreTopSize = getIntProperty("defaultJpPreTopSize");
        try {
            String invalidDateStr = properties.getProperty("jpPreTopInvalidTime");
            logger.info("load jpPreTop invalidStr:{}", invalidDateStr);
            invalidDateStr = invalidDateStr.replace("\\","");
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = formatter.parse(invalidDateStr);
            jpPreTopInvalidTime = date.getTime();
        } catch (Exception e) {
            logger.error("parse invalidTime err:{}", e);
        }
        jpPreTopSwitch = properties.getProperty("jpPreTopSwitch");

        logger.info("reload safe strategy pullDownJpPreTopSize:{}, defaultJpPreTopSize:{}, jpPreTopInvalidTime:{}, jpPreTopSwitch:{}",
                pullDownJpPreTopSize, defaultJpPreTopSize, jpPreTopInvalidTime, jpPreTopSwitch);


        //加载一点过滤失效时间......好像有这个配置可以不要开关了
        yidianSourceFilterSwitch = properties.getProperty("yidianSourceFilterSwitch");
        try {
            String invalidDateStr = properties.getProperty("yidianSourceFilterInvalidTime");
            logger.info("load yidianSourceFilter invalidStr:{}", invalidDateStr);
            invalidDateStr = invalidDateStr.replace("\\","");
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = formatter.parse(invalidDateStr);
            yidianSourceFilterInvalidTime = date.getTime();
        } catch (Exception e) {
            logger.error("parse invalidTime err:{}", e);
        }
        logger.info("reload safe strategy yidianSourceFilterSwitch:{}, yidianSourceFilterInvalidTime:{}", yidianSourceFilterSwitch, yidianSourceFilterInvalidTime);


        //-------编辑优质精选机型配置，按机型控制default和下拉条数，不在内的机型走默认配置
        String jpPreTopConfig = properties.getProperty("jpPreTopPhoneTypeConfig");
        if(StringUtils.isNotBlank(jpPreTopConfig)){
            List<JpPreTopPhoneTypeConfig> phoneTypeConfigList = gson.fromJson(jpPreTopConfig, typeToken);
            for(JpPreTopPhoneTypeConfig config : phoneTypeConfigList){
                String phoneType = config.getPhoneType();
                phoneTypeConfigMap.put(phoneType, config);
            }
        }
        logger.info("reload safe strategy phoneTypeConfig:{}", gson.toJson(phoneTypeConfigMap));

    }



    private Set<String> convertStr2Set(String value){
        Set<String> set = new HashSet<>();
        if(StringUtils.isNotBlank(value)){
            String[] whiteSourceStr = value.split(",");
            for(String whiteSource : whiteSourceStr){
                set.add(whiteSource);
            }
        }
        return set;
    }

}
