package com.ifeng.recallScheduler.constant;

/**
 * 待修改，简化统一
 * Created by wupeng1 on 2016/12/15.
 */
public class RecWhy {

    public final static String SourcePerfectOld = "perfectOld";
    public final static String SourceMerge = "merge";
    public final static String SourceCustomize = "customize";  //精选

    public final static String SourcePositiveFeed = "posi";     //正反馈强插的前缀source
    public final static String SourceCold = "cold";     //冷启动的前缀source

    public final static String SourceSafe= "safe";     //临时安全文章的前缀source

    public final static String SourceFix = "Fix";     //固定位置的稿子

    public final static String ReasonPositiveFeed_Start = "pos_";     //正反馈强插的前缀reason

    public final static String StrategyRegular = "Regular";    //编辑固定位推荐
    public final static String ReasonEditorList = "editorList";  //编辑固定位post editorList


    public final static String ReasonFixTop = "FixTop";    //置顶稿子

    public final static String ReasonLvS_Hot = "LvS_Hot";    //等级为S级别的hot新闻
    public final static String ReasonYDHot = "ydHot";    //一点资讯的hot视频
    public final static String ReasonIfengHot = "IfengHotVideo";    //凤凰热闻榜视频的hot视频
    public final static String ReasonMixIfengHot = "MixIfengHot";    //凤凰一点热闻榜视频的hot视频
    public final static String ReasonJpPool = "jpPool";    //精品池
    public final static String ReasonMix = "MultiAlgMixRank";
    public final static String ReasonLocal = "Local";
    public final static String ReasonLocalFirst = "LocalFirst";
    public final static String ReasonLocalFirstNew = "LocalFirstNew";
    public final static String ReasonLocalFirstPosi = "LocalFirstPosi";
    public final static String ReasonHot_tag = "hot_tag"; //热点新闻
    public final static String ReasonCotag = "cotag"; //召回新闻
    public final static String ReasonUserSub = "UserSub";  //订阅
    public final static String ReasonUserSubForceInsert = "UserSubForceInsert";  //强插订阅
    public final static String ReasonUserSearch = "UserSearch";  //搜索
    public final static String ReasonIfengHotDoc = "IfengHotDoc";  //凤凰热闻榜 兜底数据

    public final static String ReasonOperationLong = "operationLong";  //运营维护的长效数据 兜底数据
    public final static String ReasonJpMerge = "jpMerge";  //精品池merge
    public final static String ReasonUser_cf = "user_cf";
    public final static String ReasonCotag_crawl_video = "cotag_crawl_video";


    public final static String WhyJpPoolRecomCache = "jpPool#recomCache"; //精品池solr个性化查询的数据，缓存数据
    public final static String WhyEditorInsert = "EditorInsert"; //精品池强插
    public final static String WhyJpEditorH5 = "EditorHot#EditorH5"; //手凤精彩推荐
    public final static String WhyJpEditorSupply = "EditorHot#EditorSupply"; //精品池强插
    public final static String WhyBidHotspot = "bidding#bidHotspot"; //热点投放
    public final static String WhyJpEditorMarquee = "editorPool#Marquee"; //精品池编辑精选横排混排
    public final static String WhyJpEditorVertical = "editorPool#Vertical"; //精品池编辑精选横排垂直领域

    public final static String WhyHotFocusEditorMarquee = "editorPool#HotFocusMarquee"; //横排的焦点图样式的热点精选
    public final static String WhyHotTheme= "theme#Hot"; //话题

    public final static String WhyIfengVideoMarquee = "ifengVideo#Marquee"; //卫视内容 凤凰精选 横排混排

    public final static String WhyIfengOriginalMarquee = "ifengOriginal#Marquee"; //卫视内容 凤凰原创 包框样式

    public final static String WhyMediaExcellent = "media#Media_Ex"; //媒体精选横排
    public final static String WhyExclusive = "exclusive#Featured"; //独家精选

    public final static String WhyJpPoolHot = "jpPool#hot";     //精品池solr实时个性化数据不足，轮播历史数据的兜底数据
    public final static String WhyJpPoolEvLessRecom = "jpPool#evLessRecom";       //精品池曝光量小推荐
    public final static String WhyJpPoolPre = "jpPool#pre";     //精品池中的优质精选
    public final static String WhyJpPoolPrePostfix = "pre";
    public final static String WhyJpPoolFocus = "jpPool#focus";     //精品池中的焦点图数据

    public final static String WhyJpPoolBasic = "jpPool#basic";     //精品池中出的帖子，可能没有经过个性化，这里统一设置为basic

    public final static String WhyJpPoolBasicEs = "basicEs"; //精品池ES打底数据
    public final static String WhyJpPoolRecomEs = "jpPool#recomEs"; //精品池ES个性化数据
    public final static String WhyRecomEs = "recomEs"; //精品池ES个性化数据


    public final static String WhyJpPoolHotTagbasic = "jpPool#HotTagbasic";     //精品池中出的帖子，热点打底

    public final static String WhyInsertIfengOriginal = "ifengOriginal#insert"; //强插 凤凰卫视

    public final static String WhyInsertParadigm4 = "paradigm4#Video";  //第四范式视频推荐
    public final static String WhyParadigm4Personal = "p4Compare#Personal";  //第四范式视频推荐

    public final static String whyInsertRecom = "insertRecom";
    public final static String ReasonHotTagInsert = "HotTagInsert";  //首位强插的热点
    public final static String PosiHotTagPosiInsert = "HotTagPosiInsert";  //正反馈热点强插

    public final static String ReasonHotTagInsertFocus = "HotFocus"; //焦点图形式的热点
    public final static String ReasonHotTagInsertFocusTopic = "HotFocusTopic"; //焦点图形式的热点专题
    public final static String ReasonCtr = "Ctr";  //热点召回原因拼接使用，添加ctr标记


    public final static String whyHotTag = "HotTag"; //热点数据


    public final static String WhyPositiveEnergyHot = "perfectOld#PositiveEnergyHot"; // 正能量热榜

    public final static String WhyHotRandomDoc = "perfectOld#RandomDoc"; // 打底图文
    public final static String WhyHotRandomVideo = "perfectOld#RandomVideo"; // 打底视频
    public final static String WhyFanTagRecom = "perfectOld#FanTag";

    public final static String WhyBiddingBidding = "bidding#Bidding";   //投放bidding渠道 bidding类型

    public final static String WhyColdTag = "coldTag"; //冷启动用户数据
    public final static String WhyColdTagChannel = "coldTagChannel"; //冷启动渠道用户数据
    public final static String WhyColdTagVideo = "coldTagVideo"; //冷启动视频数据


    public final static String WhyServeCountry = "perfectOld#ServeCountry";//报国故事

    public final static String WhyWxSilenceSea = "perfectOld#WxSilenceSea";//网信静海

    public final static String WhyVideoJpPoolRecom = "videoEnginePool#Editor"; //视频精品池 es查询 recommendLevel = 2 的数据

    public final static String WhyQuickColdTag = "quickColdTag"; //快头条冷启动用户图文数据
    public final static String WhyQuickColdTagVideo = "quickColdTagVideo"; //快头条冷启动r视频数据

    public final static String whyGraphOut = "graph#Out";  // 外部知识图谱召回
    public final static String whyGraph = "graph";  // 图谱健康类召回

    public final static String WhySafeDoc = "safeDocTag"; //新doc
    public final static String WhySafeVideo = "safeVideoTag"; //新视频

    public final static String ReasonExpForceInsert = "UserExpForceInsert";  //冷启动强插兴趣试探

    public final static String UserExpPreInsert = "UserExpPreInsert";  //冷启动强插兴趣试探 旭冉组

    public final static String UserExpOutVideoInsert = "UserExpOutVideoInsert";  //冷启动强插兴趣试探 内容为数据组内容
    public final static String UserExpOutDocInsert = "UserExpOutDocInsert";  //冷启动强插兴趣试探 内容为数据组内容

    public final static String ReasonCommonExpForceInsert = "CommonExpForceInsert";  //普通用户强插兴趣试探

    public final static String ReasonVideoExpForceInsert = "VideoExpForceInsert";  //通用 新闻客户端 视频频道 用户 强插兴趣试探

    public final static String ReasonLowExpoDExpInsert = "ex_cd";  //低曝光图文强插

    public final static String ReasonLowExpoVExpInsert = "ex_cv";  //低曝光视频强插

    public final static String whyColdOutColdVideo = "insertRecom#ColdOutVideo";//数据组提供数据 视频
    public final static String whyColdOutColdDoc = "insertRecom#ColdOutDoc";//数据组提供数据 非视频
    public final static String whyColdOutEditorMarquee = "insertRecom#ColdOutEditorMarquee";//数据组提供编辑横排
    public final static String whyColdOutHotMarqeue = "insertRecom#ColdOutHotMarquee";//数据组提供热点包框

    public final static String whyDeeplinkInsert = "insertRecom#Deeplink";//数据组提供数据

    public final static String whyChannelDistributionInsert = "insertRecom#ChannelDistribution";//数据组提供数据

    public final static String whyHotPosiJp = "HotPosiJp";

}
