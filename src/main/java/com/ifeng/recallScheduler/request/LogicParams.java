package com.ifeng.recallScheduler.request;


import com.beust.jcommander.internal.Maps;
import com.beust.jcommander.internal.Sets;
import com.fasterxml.jackson.annotation.JsonIgnore;

import com.ifeng.recallScheduler.apolloConf.ApolloConstant;
import com.ifeng.recallScheduler.apolloConf.ApplicationConfig;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.Set;

/**
 * 逻辑参数配置，控制页面新闻数量占比
 */
@Setter
@Getter
public class LogicParams {


    /**
     * 优质置顶，绝对的置顶（从0计数）
     */
    private int preOrderPos = 0;

    /**
     * 新增 channelDistribution 第2位展示 显示 首屏会有强插位 一般在24小时 之前
     */
    private int channelDistributionPos = 1;

    /**
     * 本地 位置坐标（从0计数）
     */
    private int locPosition = 2;

    /**
     * bid投放（从0计数）
     */
    private int bidPosition = 2;

    /**
     * 编辑精选横排位置 (从0计数)
     */
    private int EditorMarqueePos = 3;

    /**
     * 凤凰精选 卫视内容横排位置 (从0计数)
     */
    private int ifengVideoMarqueePos = 3;

    /**
     * 搜索位置 (从0计数)
     */
    private int searchPos = 3;

    /**
     * 订阅文章强插 (从0计数)
     */
    private int userSubInsertPos = 4;


    /**
     * 正能量文章强插 (从0计数)
     */
    private int posiInsertPos = 8;


    /**
     * 媒体精选横排位置 (从0计数)
     */
    private int mediaPos = 5;

    /**
     * 话题位置 (从0计数)
     */
    private int lastTopicPos = 5;

    /**
     * 话题位置 (从0计数)
     */
    private int themePos = 7;


    /**
     * 媒体精选横排位置 (从0计数)
     */
    @Deprecated
    private int mediaPosSub = 1;

    /**
     * 独家位置 (从0计数)
     */
    @Deprecated
    private int ExclusivePos = 4;

    private int EditorMarqueeNum = 0;

    private int IfengVideoMarqueeNum = 0; // 凤凰精选 卫视内容 包框

    /**
     * 设置首屏横排元素个数（这里的首屏有 编辑精选、独家、焦点图热点）
     */
    private int marqueeNum = 0;

    /**
     * deeplink 文章插入个数
     */
    private int deeplinkInsertNum = 0;

    /**
     * 视频帖子召回数量 2个
     */
    private int videoNum = 2;


    /**
     * 小视频视频帖子召回数量 1个
     */
    private int miniVideoNum = 1;

    /**
     * 判断视频数量是否触发降权逻辑
     */
    private boolean videoNumIsLimit = false;

    /**
     * 图集 帖子召回数量 2个
     */
    private int slideNum = 2;


    /**
     * 每次推荐结果 相同分类数量
     */
    private int featuresLimit_C = ApplicationConfig.getIntProperty(ApolloConstant.CateDefault);


    /**
     * 编辑精选相同类别限制的条数
     */
    private int editorMarqueeLimit_C = 2;

    /**
     * 打底通道的每次推荐结果 相同分类数量，为了保证效果，所以设置的较粗
     */
    private int backUp_featuresLimit_C = 3;

    /**
     * 冷启动实验控制，保证多样性，每个大类只出一条
     */
    private int coldStart_featuresLimit_C_1 = 1;
    /**
     * 冷启动实验控制，保证多样性，每个大类只出2条，配置文件指定只出部分的大类
     */
    private int coldStart_featuresLimit_C_2 = 2;

    /**
     * 试探新闻，保证多样性，每个大类只出一条
     */
    private int explore_featuresLimit_C = 1;
    /**
     * 社会C类别，每屏最多出1条
     */
    private int featureLimit_C_society = 1;

    /**
     * 每次结果，推荐recallTag的个数
     */
    private int featuresLimit_recallTag = 5;

    /**
     * 根据用户推荐历史，动态控制对c的限制
     */
    @JsonIgnore
    private Map<String, Integer> recallTag_limit_History = Maps.newHashMap();

    /**
     * 根据画像试探获取的用户不感兴趣的t1 做过滤，如果不感兴趣，则一定几率屏蔽
     * 曝光次数大于10次，认为用户强烈不感兴趣， 80% 几率屏蔽
     */
    private Set<String> c_limit = Sets.newHashSet();

    /**
     * 曝光大于一定次数，点击小于一定次数 一定概率屏蔽 避免刷屏
     */
    private Set<String> cFilteredSet = Sets.newHashSet();

    private Set<String> scFilteredSet = Sets.newHashSet();

    private Set<String> ldaTopicFilteredSet = Sets.newHashSet();



    /**
     * 每次推荐结果 相同实体词数量
     */
    private int featuresLimit_Et = 2;


    /**
     * 每次推荐结果 来源相同数量
     */
    private int sourceLimit = 1;

    /**
     * 同一个simid的itemcf正反馈强插次数的限制
     */
    private int itemcfPositiveFeedCountLimit = 4;
    /**
     * 同一个simid的普通正反馈强插次数的限制
     */
    private int positiveFeedCountLimit = 6;

    /**
     * 同一个simid的普通正反馈强插次数的限制,北京用户限制4条
     */
    private int positiveFeedCountLimit_BeiJin = 3;

    /**
     * 对于正反馈强插，如果用户没有点击则开始降权展现
     */
    private int positiveFeedCountLimit_Insert_NoClick = 2;

    /**
     * hot备选集 每次推荐结果 相同特征数量
     */
    private int hotfeaturesLimit = 1;

    /**
     * 长效精品池 多样性过滤参数
     */
    private int jpCxfeaturesLimit = 2;

    /**
     * 召回通道曝光，本地限制数量
     */
    private int localLimit = 1;
    /**
     * 召回通道曝光，热点新闻限制数量
     */
    private int hot_tagLimit = 2;

    /**
     * 召回通道曝光，用户搜索限制数量
     */
    private int userSearchLimit = 2;
    /**
     * 召回通道曝光，用户订阅限制数量
     */
    private int userSubLimit = 2;

    /**
     * 召回strategy曝光，用户（新闻+视频）正反馈多样性限制，最多一屏4条
     */
    private int positiveFeedLimit = 4;

    /**
     * 编辑优质精选置顶条数
     */
    private int jpPreTopNum = 0;


    /**
     * 焦点图数量
     */
    private int focusNum = 0;


    /**
     * 热点新闻强插个数
     */
    private int hotTagForceInsertNum = 1;


    /**
     * 凤凰卫视 原创新闻强插个数
     */
    private int ifengOriginalForceInsertNum = 0;


    /**
     * 热点新闻编辑强插新闻
     */
    private int hotTagEditorAllNum = 1;

    /**
     * 本地新闻强插个数
     */
    private int localForceInsertNum = 0;

    /**
     * 本地新闻正反馈个数
     */
    private int localposiInsertNum = 1;

    /**
     * 正反馈位强插
     */
    private int positiveFeedNum = 1;

    /**
     * 冷启动用户正反馈 加权条数上限
     */
    private int positiveBoostLimitNum = 3;


    /**
     * 大图展示数量，默认设置两张大图样式
     */
    private int specialViewNum=2;

    /**
     * 视频正反馈位强插
     */
    private int videoPositiveFeedNum = 1;

    /**
     * userLast强插
     */
    private int userLastFeedNum = 1;


    /**
     * lastCotag强插数，入口处控制，实际出的条数看recallCate计算分数
     */
    private int LastCotagFeedNum = 0;


    /**
     * 用户粉丝标签推荐数
     */
    private int userFanTagRecomNum = 0;

    /**
     * 安全视频个数
     */
    private int safeVideoNum = 2;

    /**
     *报国
     */
    private int serveCountryHotNum = 0;

    /**
     *网信静海
     */
    private int WxSilenceSeaHotNum = 1;

    /**
     * 正能量强插数量
     */
    private int positiveEnergyHotNum = 0;
    /**
     * 两张大图的间距
     */
    private int specialViewStep = 2;

    /**
     * 试探数量
     */
    private int exploreNewsNum = 0;


    /**
     * 试探数量
     */
    private int coldUserForceInsertExploreNewsNum = 0;


    /**
     * xr试探数量
     */
    private int coldXrExploreNum = 0;


    /**
     * 非冷启动试探数量   视频频道不考虑是否冷启动，为通用兴趣试探逻辑
     */
    private int exploreNum = 0;
    /**
     * 关注频道优先出用户订阅的新闻
     * 在在关注频道有效
     */
    private int userSubNum = 5;

    /**
     * 非关注频道插入用户订阅的新闻
     *
     */
    private int userSubInsertNum = 0;


    /**
     * 非关注频道插入用户订阅的新闻
     *
     */
    private int userSubInsertNumTest = 0;

    /**
     * 需要的精品池的hotTag热点数据的size
     */
    private int jpHotTagNum = 1;



    //小视频限制
    private int smallVideoLimit = 0;

    //视频C限制
    private int video_C_Limit = 2;

    //视频et限制
    private int video_ET_Limit = 2;

    /**
     * 热点焦点图数量
     */
    private int hotFocusNum = 0;

    /**
     * 置顶稿子数量
     */
    private int fixTopDataNum = 0;

    /**
     * 特殊渠道 新增用户 强插新闻 数量
     */
    private int newUserChannelDistributionNum = 0;

    /**
     * 热点专题 需要的条数，在在专题详情页触发
     */
    private int hotFocusTopicNum = 0;

    /**
     *横排媒体试探条数
     */
    private int SourceMarqueeNum = 0;

    /**
     *独家显示条数
     */
    private int ExclusiveNum = 0;

    /**
     * 横排的焦点图样式的热点数量
     */
    private int hotFocusMarqueeNum = 0;

    /**
     * 热点正反馈数量
     */
    private int hotTagPosiNum = 1;

    /**
     * 四级热点数量
     */
    private int hotTag4LevelNum = 1;

    /**
     * 安全文章下控制C数量
     */
    private int safeLimit_C = 3;


    /**
     * 话题数量
     */
    private int themeNum = 0;


    /**
     * 话题下控制的文章数
     */
    private int themeDocNum = 10;

    /**
     * 需要强插的搜索条数
     */
    private int needSearchNum = 1;



    /**
     * jppool 每刷条数控制
     */
    private int jpPoolNum= 0;

    /**
     * jppool 首屏置顶下安全条数 目前只有冷启动单独提出
     */
    private int jpSafeNum= 0;

    private int biddingNum = 1;
}
