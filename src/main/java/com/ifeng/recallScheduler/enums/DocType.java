package com.ifeng.recallScheduler.enums;

/**
 * 推荐结果的doc 类型，目前氛围 图文（配图新闻）、视频、图集
 * Created by jibin on 2017/7/7.
 */
public enum DocType {
    /**
     * 无图文章
     */
    Doc("doc"),

    /**
     * 图文（配图新闻）
     */
    Docpic("docpic"),

    /**
     * 视频
     */
    Video("video"),

    /**
     * 图集
     */
    Slide("slide"),

    /**
     * 图集
     */
    Short("short"),

    /**
     * 精品池专题
     */
    Topic("topic"),

    /**
     * 24小时
     */
    FastMessageScroll("fastmessagescroll"),

    /**
     * 投放热点
     */
    hotspot("hotspot"),
    /**
     * 直播
     */
    Live("live"),

    /**
     * 横排文章
     */
    Marquee("marquee"),

    /**
     * 横排焦点图样式的热点
     */
    HotFocusMarquee("hotFocusMarquee"),

    /**
     * 凤凰精选 卫视内容 横排文章
     */
    IfengVideoMarquee("ifengVideoMarquee"),

    /**
     * 凤凰原创 独家内容 横排文章
     */
    IfengOriginalMarquee("ifengOriginalMarquee"),

    /**
     * 话题类型
     */
    ThemeType("themeType"),
    /**
     * 横排热点（焦点图形式）
     */
    HotFocus("HotFocus"),

    /**
     * 横排媒体
     */
    mediaExcellent("mediaExcellent"),

    /**
     * 独家
     */
    exclusive("exclusive"),



    /**
     * 非北京的冷启动bid
     */
    ColdUserNotBjBid("ColdUserNotBjBid"),

    /**
     * bid新数据
     */
    BidNew("BidNew"),

    /**
     * 虚拟的小视频类型， 小视频的doctype是video
     */
    MiniVideo("MiniVideo"),

    /**
     * 精品池小视频
     */
    MiniVideoJx("MiniVideoJx"),

    /**
     * 数据组编辑横排
     */
    OutEditorMarquee("OutEditorMarquee"),

    /**
     * 数据组热点包框
     */
    OutHotMarquee("OutHotMarquee"),

    /**
     * 混用类型
     */
    Mix("mix");



    private String value;

    DocType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

}
