package com.ifeng.recallScheduler.apolloConf;

/**
 * apollo配置中心常量类
 * Created by lilg1 on 2017/11/2.
 */
public class ApolloConstant {


    /**
     * Debug用户配置Apollo配置中心namespace及 properties key
     */
    public static final String Debug_User_Key = "DebugUsers";

    /**
     * DebugLogUsers输出详细日志的配置中心property name
     */
    public static final String Log_DebugUser_Key = "LogDebugUsers";

    /**
     * 精品池编辑debug用户组 yml中的key
     */
    public static final String Editor_Debug_User_Key = "editor_debug_users";

    /**
     *  短session 过滤 debug 用户
     */
    public static final String ShortSessionFilter_Debug_Users_key = "shortSessionFilter_debug_users";

    /**
     * 大图逻辑debug用户组 key
     */
    public static final String Special_View_User_Key = "special_view_users";

    /**
     * 精品池mix个性化测试用户
     */
    public static final String Special_Style_Key = "Special_Style_Key";

    /**
     * 大图逻辑debug用户组 key
     */
    public static final String LongOperation_Backup_User_Key = "longOperation_backup_users";


    /**
     * 对特殊用户的特殊通道
     */
    public static final String boss_User_Key = "boss_users";


    /**
     * 视频数量实验 debug用户组 key
     */
    public static final String VideoNumTest_User_Key = "video_num_test_users";
    public static final String Video_Model_0704 = "model0704";


    /**
     * mix_host地址配置
     */
    public static final String Mix_Host = "Mix_Host";

    /**
     * push debug用户组key
     */
    public static final String Push_Debug_User_key = "push_debug_users";

    /**
     * 媒体试探 debug用户组key
     */
    public static final String Media_debug_users = "media_debug_users";

    /**
     * cdml 测试debug用户key
     */
    public static final String CDML_DEBUG_USER = "cdml_debug_user";

    /**
     * 相关正反馈 测试debug用户key
     */
    public static final String RELATED_POSI_DEBUG_USER = "related_posi_debug_user";

    /**
     * yml配置中debuglog user的key
     */
    public static final String DebugLog_User_Key = "debug_log_users";


    public static final String positiveFeedCountLimit_BeiJin= "positiveFeedCountLimit_BeiJin";

    /**
     * 一屏内的视频最大数量
     */
    public static final String logicParam_VideoMaxNumRate= "logicParam_VideoMaxNumRate";

    /**
     * 视频数量正反馈时候，对曝光视频进行计数，如果大于计数则不会作为视频数正反馈的依据
     */
    public static final String logicParam_EvVideoNumCountLimitForAdd= "logicParam_EvVideoNumCountLimitForAdd";


    /**
     *merge时 缓存进行rank的随机数上限   cacheRankRandomNum4Merge
     */
    public static final String Cache_Rank_Random_Num4Merge = "cacheRankRandomNum4Merge";

    /**
     * 调远程布隆开关key
     */
    public static final String Remote_Bloom_SwitchKey = "remoteBloomSwitch";
    /**
     * 调远程布隆开关key
     */
    public static final String mixFirstSwitchKey = "mixFirstSwitch";
    /**
     * 调远程布隆开关key
     */
    public static final String ReAddBfDataSwitchKey = "ReAddBfDataSwitch";

    /**
     * 调远程布隆开关打开标识
     */
    public static final String Switch_on ="on";

    /**
     * 调远程布隆开关关闭标识
     */
    public static final String Switch_off ="off";

    //----------------关键词、docSource常量---------------------------------------------------------------------

    /**
     * 配置中心关键词过滤 namespace
     */
    public static final String TOBE_FILTER_KEYWORD_NAMESPACE = "KeywordsToFilter";


    /**
     * 配置中心doc source过滤 namespace
     */
    public static final String TOBE_FILTER_DOCSOURCE_NAMESPACE = "DcoSourceToFilter";

    /**
     *
     */
    public static final String SAFE_STRATEGY_CONFIG = "SafeStrategy";

    /**
     * 视频数量计算参数
     */

    public static final String VideoMaxNumRate_KEY = "VideoMaxNumRate";

    public static final String VIDEO_MODEL_CONF_KEY = "VideoModelConf";

    /**
     * 针对wxb的内容安全过滤控制开关，只有必要时候打开，进行严格过滤
     */
    public static final String WxbContentSecuritySwitch = "WxbContentSecuritySwitch";

    /**
     * Wxb过滤阈值
     */
    public static final String Wxb_Threshold = "Wxb_Threshold";

    /**
     * 北京及一线低等级用户过滤阈值
     */
    public static final String Beijing_Threshold = "Beijing_Threshold";

    /**
     * 上海、广州的标题过滤分值
     */
    public static final String ShangHai_GuangDong_Threshold = "ShangHai_GuangDong_Threshold";


    /**
     * 精品池条数控制
     */
    public static final String isJpPoolFlow_Max_PullNum = "isJpPoolFlow_Max_PullNum";

    public static final String PositiveEnergyDoc_Rate_BJ = "PositiveEnergyDoc_Rate_Bj";
    public static final String PositiveEnergyDoc_Rate_Special = "PositiveEnergyDoc_Rate_Special";
    public static final String PositiveEnergyDoc_Rate_Common = "PositiveEnergyDoc_Rate_Common";


    public static final String ExploreNews_Rate = "ExploreNews_Rate";

    /**
     * 	独家和媒体试探开关
     */
    public static final String mediaAndExclusiveSwitch = "mediaAndExclusiveSwitch";


    /**
     * 小视频过滤白名单
     */
    public static final String min_video_filter_white = "min_video_filter_white";

    /**
     * bid分流测试开关
     */
    public static final String bidAbtestSwitch = "bidAbtestSwitch";

    /**
     * 本地新闻运营 加大强插频率 开关
     */
    public static final String localIncreaseSwitch = "localIncreaseSwitch";

    /**
     * 头条安全开关
     */
    public static final String SafeSwitch ="SafeSwitch";


    /**
     * 头条安全video开关，后续可视情况下线
     */
    public static final String VideoSafeSwitch ="VideoSafeSwitch";

    /**
     * kafka迁移开关
     */
    public static final String KafkaSwitch = "KafkaSwitch";


    /**
     * 中轻大厦 白名单开关判断
     */
    public static final String ZQSwitch ="ZQSwitch";

    /**
     * 冷启动 用户 同步加载及点击日志 开关判断
     */
    public static final String SendEngineLoadInfoSwitch = "sendEngineLoadInfoSwitch";

    /**
     * 非北上广测试分组用户
     */
    public static final String NotBSG_User_Key = "NotBSG_User_Key";

    /**
     * 冷启动测试分组用户
     */
    public static final String coldSpecial_white_key = "coldSpecial_white_key";

    /**
     * 固定位置强插的稿子
     */
    public static final String FixTopData_Key = "FixTopData";

    /**
     * 话题分组用户
     */
    public static final String Theme_User_Key = "Theme_User_Key";

    public static final String HotTag_Rate_BSG = "HotTag_Rate_BSG";
    public static final String HotTag_Rate_COMMON= "HotTag_Rate_Common";

    public static final String HotTag_SHOWNUM_SPE = "HotTag_SHOWNUM_SPE";
    /**
     * 需要过滤的热点id，防止热点缓存造成的无法及时下线文章
     */
    public static final String HotFilter_Key = "HotFilter_Key";


    public static final String EXP_UserTest_Key = "EXP_UserTest_Key";

    public static final String Low_Expo_LimitNum = "Low_Expo_LimitNum";
    /**
     * 四五级包框热点缓存开关
     */
    public static final String HotSpot_Cache_Switch = "HotSpot_Cache_Switch";
    /**
     * 四五级包框热点服务开关
     */
    public static final String HotSpot_Impl_Switch = "HotSpot_Impl_Switch";
    /**
     * 四五热点展示频次
     */
    public static final String HotSpot_Freq_Value = "HotSpot_Freq_Value";
    /**
     * bj地区四五热点展示频次
     */
    public static final String HotSpot_Bj_Freq_Value = "HotSpot_Bj_Freq_Value";
    /**
     * 非四五热点，包框展示频次
     */
    public static final String HotSpot_Package_Freq_Value = "HotSpot_Package_Freq_Value";
    /**
     * 四五热点展示位置
     */
    public static final String HotSpot_Position_Value = "HotSpot_Position_Value";


    /**
     * 低试探曝光过滤开关 避免影响性能
     */
    public static final String LowExpoFilterSwitch = "LowExpoFilterSwitch";

    /**
     * 热点试探曝光值
     */
    public static final String HotNewsExpLimit_Value = "HotNewsExpLimit_Value";

    /**
     * 热点试探曝光转化率值
     */
    public static final String HotNewsCtrLimit_Value = "HotNewsCtrLimit_Value";

    /**
     * 垂直领域编辑横排测试
     */
    public static final String vertical_marquee_user = "vertical_marquee_user";
    /**
     * 大事件的主题logo名称
     */
    public static final String HotFocus_MARQUEE_TITLE = "HotFocus_MARQUEE_TITLE";


    /**
     * 普通热点展示屏控
     */
    public static final String EditorNewsFreq_Value = "EditorNewsFreq_Value";

    /**
     * 普通热点兴趣匹配t1加权值
     */
    public static final String EditorHotInterestWeight_value = "EditorHotInterestWeight_value";

    /**
     * 普通热点兴趣匹配t2加权值
     */
    public static final String EditorHotInterestWeight_t2_value = "EditorHotInterestWeight_t2_value";

    /**
     * 普通热点规定频次内展示过，降权重
     */
    public static final String EditorHotReduceWeight_value = "EditorHotReduceWeight_value";

    /**
     * 普通热点兴趣匹配t1加权值 new
     */
    public static final String EditorHotInterestWeightNew_value = "EditorHotInterestWeight_value_new";

    /**
     * 普通热点兴趣匹配t2加权值 new
     */
    public static final String EditorHotInterestWeightNew_t2_value = "EditorHotInterestWeight_t2_value_new";

    /**
     * 普通热点规定频次内展示过，降权重 new
     */
    public static final String EditorHotReduceWeightNew_value = "EditorHotReduceWeight_value_new";

    public static final String JpPoolNum_default = "JpPoolNum_default";

    public static final String JpPoolNum_3Pre = "JpPoolNum_3Pre";

    public static final String JpPoolNum_3After = "JpPoolNum_3After";

    public static final String Boss_Hot_Num = "Boss_Hot_Num";
    public static final String Boss_Hot_Num_3After = "Boss_Hot_Num_3After";


    public static final String testOn = "testOn";

    /**
     * 多样性level1
     */
    public static final String CateLevel1 ="CateLevel1";

    /**
     * 多样性level2
     */
    public static final String CateLevel2="CateLevel2";


    /**
     * 多样性level3
     */
    public static final String CateLevel3 ="CateLevel3";

    /**
     * 多样性level4
     */
    public static final String CateLevel4 ="CateLevel4";

    /**
     * 多样性默认level
     */
    public static final String CateDefault ="CateDefault";

    /**
     * 精品池实验组比例
     */
    public static final String JpTest ="JpTest";

    /**
     * 精品池对照组比例
     */
    public static final String JpBase ="JpBase";


    public static final String HOT_TOPIC_Key = "HOT_TOPIC_Key";


    /**
     * 热点mysql开关
     */
    public static final String HotMysqlSwitch ="HotMysqlSwitch";

    /**
     * 实验组三刷以后精品池概率控制
     */
    public static final String Jp_Rate_Common = "Jp_Rate_Common";

    /**
     * 算法抓取热点使用开关
     */
    public static final String Hot_CrawSF_Switch = "Hot_CrawSF_Switch";


    /**
     * videoRatingNew 实验组
     */
    public static final String videoRatingTest= "videoRatingTest";

    /**
     * 新固定位过滤开关
     */
    public static final String regularSwitch= "regularSwitch";

    public static final String coldExpNum= "coldExpNum";
    public static final String expNumA= "expNumA";
    public static final String expNumB= "expNumB";
    public static final String minExpNumA= "minExpNumA";
    public static final String maxExpNumA= "maxExpNumA";
    public static final String minExpNumB= "minExpNumB";
    public static final String maxExpNumB= "maxExpNumB";
    /**
     * 旭冉冷启动兴趣试探
     */
    public static final String coldExpXr= "coldExpXr";


    public static final String evItemQueueNum= "evItemQueueNum";

    public static final String basicFactor= "basicFactor";

    public static final String mulFactor= "mulFactor";

    public static final String fullnessFactor= "fullnessFactor";

    /**
     * 非冷启动兴趣试探
     */
    public static final String commonExp= "commonExp";

    public static final String newColdFullness= "newColdFullness";

    public static final String coldFullness= "coldFullness";

    public static final String newBidSwitch= "newBidSwitch";

    //本地相关配置
    public static final String localEvMin= "localEvMin";
    public static final String localCtrMin= "localCtrMin";
    public static final String totalCtrMax= "totalCtrMax";
    public static final String totalPvMin= "totalPvMin";
    public static final String totalPvMax= "totalPvMax";

    public static final String coldTagNum= "coldTagNum";

    public static final String localUserKey= "localUserKey";


    //热点相关配置
    public static final String Hot_ev_min= "Hot_ev_min";
    public static final String Hot_ev_max= "Hot_ev_max";
    public static final String Total_pv_min= "Total_pv_min";
    public static final String Hot_pv_max= "Hot_pv_max";
    public static final String Hot_ctr_min= "Hot_ctr_min";
    public static final String hot_pv_min= "hot_pv_min";
    public static final String Total_ctr_max= "Total_ctr_max";


    public static final String HotUserReKey= "HotUserReKeyClick2";
    public static final String coldKindColl= "coldKindColl";

    //指定类型文章标题相似度阈值
    public static final String specialTilteDis= "specialTilteDis";

    //特定冷启动实验组不出固定位概率
    public static final String coldNoFocusNum= "coldNoFocusNum";

    //热点特殊处理标记
    public static final String specialLogo= "specialLogo";

    //热点人群匹配C级兴趣获取数量
    public static final String HotTagCTop= "HotTagCTop";

    //热点人群匹配SC级兴趣获取数量
    public static final String HotTagScTop= "HotTagScTop";

    //热点聚焦规则人群
    public static final String hot_boss_users= "hot_boss_users";

    //判断用户 是否属于 特定渠道 逻辑  配置 渠道 对应 投放文章
    public static final String newUserForChannelDistributionConfigJson = "newUserForChannelDistributionConfigJson";

    /**
     * 外部冷启动kind开关
     */
    public static final String OutKindSwitch ="OutKindSwitch";

    /**
     * 页面展现新闻数：下滑10条
     */
    public static final String dispNum_PullDown = "dispNum_PullDown";
    /**
     * 临时修改上滑为8条
     * 页面展现新闻数：上滑8条
     */
    public static final String dispNum_PullUp = "dispNum_PullUp";
    /**
     * 页面展现新闻数：default 10条
     */
    public static final String dispNum_Default = "dispNum_Default";


    public static final String videoTab_VideoNum ="videoTab_VideoNum";

    public static final String dispNum_Default_Dis = "dispNum_Default_Dis";

    /**
     * 头条个性化实时调用开关，后续可视情况下线
     */
    public static final String RealTimeSwitch ="RealTimeSwitch";

    /**
     * 头条个性化实时调用实验组比例，后续可视情况下线
     */
    public static final String RealTimePer ="RealTimePer";

    public static final String realTimeOut = "realTimeOut";
    public static final String ctrTimeOut = "ctrTimeOut";

    public static final String realTimeDebuger = "realTimeDebuger";

    public static final String graphTimeOut = "graphTimeOut";
    public static final String graphSwitch ="graphSwitch";
    public static final String graphFilterColl= "graphFilterColl";
    public static final String graphFilterExpTag= "graphFilterExpTag";
    public static final String graphFilterProid= "graphFilterProid";

    public static final String isGuideNum= "isGuideNum";

    /**
     * sansu 旧表（张阳）开关 如开则不是
     */
    public static final String SansuSwitch ="SansuSwitch";

    /**
     * 热点详情聚合页converge 数据回传开关
     */
    public static final String convergeSwitch ="convergeSwitch";

    /**
     * 固定位白名单 非紧急状态下 不建议添加用户
     */
    public static final String reqularDebuger ="reqularDebuger";

    /**
     * 限制前几刷之内出机构
     */
    public static final String limitJGCount ="limitJGCount";

    /**
     * 数据组冷启动过滤开关
     */
    public static final String OutColdFilterSwitch ="OutColdFilterSwitch";

    /**
     * 热点正反馈 精品池内容配置
     */
    public static final String HotJpSwitch = "HotJpSwitch";
    public static final String lowHotJpNum = "lowHotJpNum";
    public static final String middleHotJpNum = "middleHotJpNum";
    public static final String highHotJpNum = "highHotJpNum";

    /**
     * 非数据组冷启动白名单
     */
    public static final String isNotOutDebuger ="isNotOutDebuger";

    /**
     * 编辑横排开关
     */
    public static final String EditorMarqueeSwitch ="EditorMarqueeSwitch";

}
