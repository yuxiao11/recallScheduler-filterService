package com.ifeng.recallScheduler.constant;


/**
 * 头条频道和推荐频道的流量区分标记
 * recomChannel=recom
 * recomChannel=headline
 */
public enum RecomChannelEnum {


    /**
     * 视频app
     */
    videoapp("videoapp"),

    /**
     *视频频道
     */
    videochannel("videochannel"),

    /**
     * 推荐频道标记
     */
    recom("recom"),

    /**
     * 头条频道标记
     */
    headline("headline"),

    /**
     * 机器push  葛亚鲁
     */
    machinepush("machinepush"),


    /**
     * 关注频道
     */
    momentsnew("momentsnew"),

    /**
     * 相关页中的视频 feed流，用于精彩视频
     */
    videoRelatedFeed("videoRelatedFeed"),


    /**
     * 相关页中的视频 List，无限下拉
     */
    videoRelated("videoRelated"),

    /**
     * 图文相关页
     */
    docRelated("docRelated"),

    /**
     * 详情页中的 竖版焦点图样式的热点流量
     */
    HotFocus("HotFocus"),

    /**
     * 详情页中的  焦点图样式的热点点击跳转 进入热点专题页
     */
    HotFocusTopic("HotFocusTopic"),


    /**
     * 搜索页面下的 热点事件
     */
    HotFocusSearch("HotFocusSearch"),



    /**
     *  话题
     */
    Theme("Theme"),
    ;


    ;

    private String value;

    private RecomChannelEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
