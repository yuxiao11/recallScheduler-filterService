package com.ifeng.recallScheduler.support;

import com.ctrip.framework.apollo.Apollo;
import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfig;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;
import com.ifeng.recallScheduler.apolloConf.*;
import com.ifeng.recallScheduler.utils.DebugUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Component
public class ApolloConfiguration {

    private static Logger logger = LoggerFactory.getLogger(Apollo.class);

    //application配置项,公共配置项
    @ApolloConfig("application")
    private Config appConfig;


    @ApolloConfig("stormDataMerge")
    private Config stormDataMergeConfig;

    @ApolloConfig(ApolloConstant.Debug_User_Key)
    Config debugConfig;

    @ApolloConfig(ApolloConstant.TOBE_FILTER_KEYWORD_NAMESPACE)
    private Config toBeFilteredKeywordConfig;

    @ApolloConfig(ApolloConstant.TOBE_FILTER_DOCSOURCE_NAMESPACE)
    private Config docSourceConfig;

    @ApolloConfig("BossApplication")
    private Config BossConfig;

    @ApolloConfig("HotApplication")
    private Config HotConfig;

    @ApolloConfig("SpecialApplication")
    private Config SpecialConfig;

    @ApolloConfig("SafeStrategy")
    private Config safeStrategyApolloConfig;

    @Autowired
    private StormDataMergeConfigUtil stormDataMergeConfigUtil;

    @Autowired
    private ApplicationConfig applicationConfig;

    @Autowired
    private DebugUserConfig debugUserConfig;

    @Autowired
    private DebugUtil debugUtil;

    @Autowired
    private KeywordFilterConfigUtil keywordFilterConfigUtil;

    @Autowired
    private DocSourceFilterConfigUtil docSourceFilterConfigUtil;

    @Autowired
    private BossApplicationConfig bossConfigUtil;

    @Autowired
    private HotApplicationConfig hotConfigUtil;

    @Autowired
    private SpecialApplicationConfig specialConfigUtil;

    @Autowired
    private SafeStrategyConfig safeStrategyConfig;


    @PostConstruct
    public void init() throws InterruptedException, IOException {

         long start=System.currentTimeMillis();

        //初始化application中的配置项
        applicationConfig.init(appConfig);
        stormDataMergeConfigUtil.init(stormDataMergeConfig);

        //初始化debugUser配置项
        debugUserConfig.init(debugConfig);
        debugUtil.init(debugConfig);

        bossConfigUtil.init(BossConfig);
        hotConfigUtil.init(HotConfig);
        specialConfigUtil.init(SpecialConfig);

        //--------初始化doc关键词过滤配置项------
        this.keywordFilterConfigUtil.init(this.toBeFilteredKeywordConfig);

        //--------初始化doc source过滤配置项------
        this.docSourceFilterConfigUtil.init(this.docSourceConfig);

        //加载安全策略配置
        safeStrategyConfig.init(safeStrategyApolloConfig);

        logger.info("ApolloConfiguration init cost:{}", System.currentTimeMillis() - start);

    }





    //监听配置更新事件
    @ApolloConfigChangeListener("application")
    public void onApplicationConfigChange(ConfigChangeEvent configChangeEvent) {
        applicationConfig.onChangeJob(configChangeEvent);
    }

    //监听配置更新事件
    @ApolloConfigChangeListener("BossApplication")
    public void onBossConfigChange(ConfigChangeEvent configChangeEvent) {
        bossConfigUtil.onChangeJob(configChangeEvent);
    }

    //监听配置更新事件
    @ApolloConfigChangeListener("HotApplication")
    public void onHotApplicationChange(ConfigChangeEvent configChangeEvent) {
        hotConfigUtil.onChangeJob(configChangeEvent);
    }

    //监听配置更新事件
    @ApolloConfigChangeListener("SpecialApplication")
    public void onSpecialApplicationChange(ConfigChangeEvent configChangeEvent) {
        specialConfigUtil.onChangeJob(configChangeEvent);
    }


    /**
     * 监听sotrm增量的配置更新事件
     *
     * @param configChangeEvent
     */
    @ApolloConfigChangeListener("stormDataMerge")
    public void onStormDataMergeConfigChange(ConfigChangeEvent configChangeEvent) {
        stormDataMergeConfigUtil.onChangeJob(configChangeEvent);
    }




    //监听debug用户更新事件
    @ApolloConfigChangeListener("DebugUsers")
    public void onDebugUserChange(ConfigChangeEvent configChangeEvent) {
        //监听debug用户更新事件
        debugUserConfig.onChangeJob(configChangeEvent);
        //监听debug用户更新事件
        debugUtil.onChangeJob(configChangeEvent);
    }

    /**
     * 关键词过滤更新事件
     * @param configChangeEvent
     */
    @ApolloConfigChangeListener(ApolloConstant.TOBE_FILTER_KEYWORD_NAMESPACE)
    public void onToBeFilteredKeywordChange(ConfigChangeEvent configChangeEvent){
        this.keywordFilterConfigUtil.onChangeJob(configChangeEvent);
    }

    /**
     * doc source 更新事件
     * @param configChangeEvent
     */
    @ApolloConfigChangeListener(ApolloConstant.TOBE_FILTER_DOCSOURCE_NAMESPACE)
    public void onDocSourceChage(ConfigChangeEvent configChangeEvent){
        this.docSourceFilterConfigUtil.onChangeJob(configChangeEvent);
    }

    @ApolloConfigChangeListener(ApolloConstant.SAFE_STRATEGY_CONFIG)
    public void onSafeStrategyConfig(ConfigChangeEvent configChangeEvent){
        this.safeStrategyConfig.onChangeJob(configChangeEvent);
    }

}

