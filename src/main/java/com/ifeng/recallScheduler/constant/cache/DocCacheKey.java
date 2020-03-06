package com.ifeng.recallScheduler.constant.cache;


/**
 * 推荐document本地缓存的相关key
 */
public enum DocCacheKey {

    DOC_HOT_COLD("doc_hot_cold"),
    VIDEO_HOT_COLD("video_hot_cold"),
    VIDEO_HOT_NEW("video_hot_new"),
    DOC_HOT_NEW("doc_hot_new"),

    /**
     * 冷启动的高阅读时长文章 暂时弃用
     */
    DOC_READLONG_LIST("doc_readLong_list"),
    /**
     * 冷启动渠道测试文章 暂时弃用
     */
    DOC_CHANNEL_LIST("doc_channel_list"),

    /**
     * 冷启动渠道测试视频
     */
    VIDEO_COLD_LIST("video_cold_list"),

    /**
     * 单个用户个性化  图文 推荐列表
     */
    Doc_PERSONAL_LIST("%s_d"),
    /**
     * 单个用户个性化  图集 推荐列表
     */
    Slide_PERSONAL_LIST("%s_s"),
    /**
     * 单个用户个性化  视频 推荐列表
     */
    Video_PERSONAL_LIST("%s_v"),

    /**
     * 小视频
     */
    MiniVideo_PERSONAL_LIST("%s_m_v"),

    /**
     * 混用文章
     */
    Mix_PERSONAL_LIST("%s_mix"),

    /**
     * 小视频jx
     */
    MiniVideo_Jx_LIST("%s_mj_v"),

    /**
     * 媒体来源
     */
    MEDIA_SOURCE_LIST("media_source_list"),

    /**
     * 临时逻辑安全图文
     */
    DOC_LIST("doc_list"),
    /**
     * 临时逻辑安全视频
     */
    VIDEO_LIST("video_list"),;
    private String value;

    private DocCacheKey(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
