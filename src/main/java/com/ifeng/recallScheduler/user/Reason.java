package com.ifeng.recallScheduler.user;

/**
 * 推荐原因
 * Created by jibin on 2017/8/3.
 */
public class Reason {
    private String strategy; //推荐策略                                                                                                            暂无
    private String reason;   //推荐原因                                   mix             corec (正反馈)       local(本地)
    private String source;   //推荐通道                                     preload          preload              preload
    private String sensitive;  //时效或长效文章                       true(false)  true相当于 PerfectNew  false相当于preload
    private String tag;     //文章中在通道中被具体tag召回的标签名称           科技                                   CF,DeepChel等                   北京市
    private  Integer tagRank;  //这个tag召回的这篇文章的排名顺序             10               5                     6
    private  Integer tagnum;   //这个tag召回的文章总数量                    100              100                  100
    private Double tagsimScore; //这篇文章和这个tag的相关度                0.6              cf若没有则为null        0.8
    private Double hotBoost;//热度值                                      0.3
    private String recallTag; //多值召回标签
    private String debugInfo; //debug信息  只有debug用户添加
    private Integer type;  //0:普通贴，   1，商品文
    private String recallId; // 召回当前帖子的起源帖子的信息
    private String position; // 召回帖子在当前标签中的位置，只对debug用户生效
    private String engineLog;
    private String ctrOrigin; //原始ctr值



    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSensitive() {
        return sensitive;
    }

    public void setSensitive(String sensitive) {
        this.sensitive = sensitive;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Integer getTagRank() {
        return tagRank;
    }

    public void setTagRank(Integer tagRank) {
        this.tagRank = tagRank;
    }

    public Integer getTagnum() {
        return tagnum;
    }

    public void setTagnum(Integer tagnum) {
        this.tagnum = tagnum;
    }

    public Double getTagsimScore() {
        return tagsimScore;
    }

    public void setTagsimScore(Double tagsimScore) {
        this.tagsimScore = tagsimScore;
    }

    public Double getHotBoost() {
        return hotBoost;
    }

    public void setHotBoost(Double hotBoost) {
        this.hotBoost = hotBoost;
    }

    public String getRecallTag() {
        return recallTag;
    }

    public void setRecallTag(String recallTag) {
        this.recallTag = recallTag;
    }

    public String getDebugInfo() {
        return debugInfo;
    }

    public void setDebugInfo(String debugInfo) {
        this.debugInfo = debugInfo;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getRecallId() {
        return recallId;
    }

    public void setRecallId(String recallId) {
        this.recallId = recallId;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getEngineLog() {
        return engineLog;
    }

    public void setEngineLog(String engineLog) {
        this.engineLog = engineLog;
    }

    public String getCtrOrigin() {
        return ctrOrigin;
    }

    public void setCtrOrigin(String ctrOrigin) {
        this.ctrOrigin = ctrOrigin;
    }
}
