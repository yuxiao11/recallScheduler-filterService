package com.ifeng.recallScheduler.constant.cache;

/**
 * Created by jibin on 2017/6/23.
 */
public class CacheFactory {


    public static enum CacheName {

        /**
         * 精品池的index的缓存，过期时间10分钟，数据量最大50000
         */
        JpPoolIndex("jpPoolIndex"),

        /**
         * mix精品池的index缓存，过期时间10分钟，数据量最大50000
         */
        JpPoolMixIndex("jpPoolMixIndex"),

        /**
         * 精品池的Document的缓存，过期时间2小时，数据量最大5000
         */
        JpPoolDocument("jpPoolDocument"),

        /**
         * 编辑精品池优质精选缓存
         */
        JpPoolPreDocument("jpPoolPreDocument"),

        /**
         * 编辑精品池长效优质精选(七天内)
         */
        JpPoolPreLongDocument("jpPoolPreLongDocument"),

        /**
         * 疑似异常用户黑名单缓存，对疑似uid进行计数，时效2小时
         */
        seemBlackUserList("seemBlackUserList"),


        /**
         * 用户级别缓存
         */
        UserModel_ua_v("UserModel_ua_v"),

        /**
         * 用户级别为空的缓存。避免重复查询
         */
        UserModel_ua_v_null("UserModel_ua_v_null"),



        /**
         * 用户维度的推荐缓存数据用户id到docid   时效10小时
         */
        PersonalIndex("personalIndex"),


        /**
         * 推荐的document数据   时效10天
         */
        PersonalRecomDocumentInfo("personalDocumentInfo"),


        /**
         * 推荐缓存热门数据
         */
        HotRecomDocument("hotRecomDocument"),

        /**
         * 冷启动小视频数据
         */
        HotRecomMini("hotRecomMini"),

        /**
         * 根据地域、机型控制冷启动的热门推荐数据  时效100小时
         */
        ColdStartRecomHot("ColdStartRecomHot"),

        /**
         * 视频的冷启动的推荐缓存热门数据 时效8小时
         */
        ifengHotVideoDocument("ifengHotVideoDocument"),


        /**
         * 混合的视频热数据缓存，过期时间100小时
         */
        ifengMixedVideoDocument("ifengMixedVideoDocument"),
        /**
         * sabc级媒体的打底池子
         */
        LvS_hotRecomDocument("LvS_hotRecomDocument"),

        /**
         *报国文章的打底池子
         */
        ServeCountryDocument("ServeCountryDocument"),

        /**
         *网信静海的打底池子
         */
        WxSilenceSeaDocument("WxSilenceSeaDocument"),

        /**
         *凤凰视频精品池子
         */
        VideoJpPoolDocument("VideoJpPoolDocument"),

        /**
         * 正能量的打底池子
         */
        PositiveEnergyDocument("PositiveEnergyDocument"),

        /**
         * 运营维护的冷启动的长效热闻的数据 时效48小时
         */
        LongOperationBackup("LongOperationBackup"),



        /**
         * 一点资讯的视频的冷启动的推荐缓存热门数据 时效8小时
         */
        YidianVideoHot("YidianVideoHot"),


        /**
         * 媒体评级
         */
        SourceInfo_EvalLevel("SourceInfo_EvalLevel"),

        /**
         * 视屏媒体评级
         */
        VideoSource_EvalLevel("VideoSource_EvalLevel"),


        /**
         * 媒体评级 为Null
         */
        NullSourceInfo_EvalLevel("NullSourceInfo_EvalLevel"),

        /**
         * 黑名单缓存
         */
        SansuBlack("sansuBlack"),

        /**
         * 负评黑名单缓存
         */
        NegCommentBlack("negCommentBlack"),

        /**
         * 五级负评黑名单缓存
         */
        Level5NegCommentBlack("level5NegCommentBlack"),

        /**
         * 一到四级负评黑名单缓存
         */
        Level1To4NegCommentBlack("level1To4NegCommentBlack"),

        /**
         * 编辑焦点图
         */
        EditorFocus("EditorFocus"),


        /**
         * 编辑固定位置的强插帖
         */
        EditorRegularPosition("EditorRegularPosition"),

        /**
         * 新编辑固定位置的强插帖
         */
        EditorRegularNew("EditorRegularNew"),
        /**
         * 编辑精品池
         */
        EditorJpPool("EditorJpPool"),

        /**
         * 编辑精品池数据 从ES查询
         */
        EditorEsJpPool("EditorEsJpPool"),

        /**
         * 从ES中获取的个性化数据
         */
        EditorEsJpIndex("EditorEsJpIndex"),

        /**
         * 从ES中获取长效精品池数据
         */
        EditorEsJpLongTerm("EditorEsJpLongterm"),

        /**
         * 编辑精品池带曝光数据
         */
        EditorJpPoolWithEv("EditorJpPoolWithEv"),

        /**
         * 编辑精品池 大于10000曝光量 且转换率低于8%的 前50 或 全部
         */
        EditorJpPoolWithHighEvLowPv("EditorJpPoolWithHighEvLowPv"),

        /**
         * 编辑精品池
         */
        AbTest("AbTest"),

        /**
         * debug用户的log，落到凤凰罗盘中
         */
        debugUserLog("debugUserLog"),

        /**
         * 正反馈新闻强插
         */
        PositiveFeedIndex("PositiveFeedIndex"),

        /**
         * 正反馈新闻结果为空的缓存，1分钟内不再尝试查询
         */
        NullPositiveFeedIndex("NullPositiveFeedIndex"),


        /**
         * 视频馈新闻结果为空的缓存，1分钟内不再尝试查询
         */
        NullVideoPositiveFeedIndex("NullVideoPositiveFeedIndex"),

        /**
         * 视频正反馈强插
         */
        VideoPositiveFeedIndex("VideoPositiveFeedIndex"),

        /**
         * 视频cdml计算的正反馈
         */
        CdmlVideoPositiveFeedIndex("CdmlVideoPositiveFeedIndex"),

        /**
         * 视频cdml正反馈为空的缓存，1分钟内不再尝试
         */
        NullCdmlVideoPositiveFeedIndex("NullCdmlVideoPositiveFeedIndex"),

        /**
         * 视频related计算的正反馈
         */
        RelatedVideoPositiveFeedIndex("RelatedVideoPositiveFeedIndex"),

        /**
         * 视频related正反馈为空的缓存，1分钟内不再尝试
         */
        NullRelatedVideoPositiveFeedIndex("NullRelatedVideoPositiveFeedIndex"),

        /**
         * 同一个数据来源的simid对用户的正反馈强插的计数的缓存，避免推荐次数较多，过期时间10小时，数据量最大100万
         */
        PositiveFeedSimIdCount("PositiveFeedSimIdCount"),

        /**
         * 用户行为信息的缓存
         */
        UserSession("UserSession"),

        /**
         * 用户行为信息的 短session缓存
         */
        UserShortSession("UserShortSession"),

        /**
         * 用户行为上下拉计数统计
         */
        UserPullNum("UserPullNum"),

        /**
         * 用户行为搜索，订阅缓存
         */
        UserAction("UserAction"),
        /**
         * 用户曝光的recallTag的统计信息，多样性过滤使用，过期时间30分钟
         */
        UserEv_RecallTag_Info("UserEv_RecallTag_Info"),

        /**
         * 用户曝光的recallTag的统计信息，多样性过滤使用，过期时间6小时，只对少部分用户应用
         */
        UserEv_RecallTag_Info_longTime("UserEv_RecallTag_Info_longTime"),


        /**
         * 多样性控制的recall tag
         */
        Reason_recallTag("Reason_recallTag"),

        /**
         * 本地新闻
         */
        Local_IndexLngLat("Local_IndexLngLat"),


        /**
         * simId到docId 的缓存映射
         */
        SimIdMapping("simIdMapping"),


        /**
         * 热点新闻打底数据(过滤媒体评级后的)
         */
        hotTagIndexBackupLvA("hotTagIndexBackupLvA"),

        /**
         * 热点新闻打底数据（不过滤媒体）
         */
        hotTagIndexBackupNorMal("hotTagIndexBackupNorMal"),

        /**
         * 热点新闻最新更新时间，用在热点焦点图展示时间上
         */
        hotTagIndexUpdateTime("hotTagIndexUpdateTime"),

        /**
         * 热点新闻个性化数据 (调过ctr排序)
         */
        hotTagPersonalIndex("hotTagPersonalIndex"),

        /**
         * 已推热点事件用户session
         */
        hotTagUserSession("hotTagUserSession"),


        /**
         * 已推热点焦点图事件用户session
         */
        hotFocusUserSession("hotFocusUserSession"),

        /**
         * 一个session内用户的热点展示次数
         */
        hotUserSessionCount("hotUserSessionCount"),

        /**
         * 热点新闻个性化数据 (调过ctr排序)
         */
        jpTagPersonalIndex("jpTagPersonalIndex"),


        /**
         * 热点新闻个性化数据 (调过ctr排序)
         */
        lastTopicCtrIndex("lastTopicCtrIndex"),

        /**
         * 用户近几刷曝光的lastCotag,
         */
        lastCotagSession("lastCotagSession"),


        /**
         * 用户维度的试探新闻存储，最多20条，时效15min
         */
        explorePersonalIndex("explorePersonalIndex"),

        /**
         * 用户维度的订阅新闻存储，最多20条，时效15min
         */
        userSubPersonalIndex("userSubPersonalIndex"),

        /**
         * 用户维度的用户ucb试探，时效15min
         */
        ucbPersonalIndex("ucbPersonalIndex"),

        /**
         * 上报的用户粉丝标签，目前主要用于张艺兴粉丝渠道拉过来的新用户
         */
        userFansTag("userFansTag"),

        /**
         * 用户粉丝标签推荐session缓存
         */
        userFansTagSession("userFansTagSession"),

        /**
         * 四五级热点缓存
         */
        hotSpotNewsCache("hotSpotNewsCache"),

        /**
         * 非四五级，但需要包框热点缓存
         */
        packageHotSpotNewsCache("packageHotSpotNewsCache")
        ;

        private String value;

        CacheName(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    }


}
