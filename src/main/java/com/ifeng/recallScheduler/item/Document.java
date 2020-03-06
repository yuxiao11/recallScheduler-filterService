package com.ifeng.recallScheduler.item;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 新闻文章对象
 */
public class Document implements Serializable, Cloneable {

    static final Logger log = LoggerFactory.getLogger(Document.class);

    private static final long serialVersionUID = 1L;

    private static final Pattern pattern = Pattern.compile("[^0-9]");

    private String docId; // 文章id(imcp_id或url)

    private String title; // 文章标题

    private Date date; // 文章时间

    private Date importDate; // 文章入精品池时间

    private String hotLevel; // 文章热度

    private String docType; // 文章类型 (slide/video/doc/hdSlide)

    private String docChannel; // 文章所属频道信息

    private String why; // 解释描述(recommend/additional(数目不够，补充)等)

    private String score;//相似度得分

    private String hotBoost;//热度

    private String readableFeatures;//热证词

    private String others;//透传晓伟

    private String simId;//聚类id

    private String groupId; //新的聚合id，用于替换simId

    private String recallid; //recallid供召回使用

    private String mediaId; //媒体id

    private String channels; //召回文章的多通道信息

    private String tags;  //召回recallTags列表信息

    private String rtag;  //文章rtag

    private String revealType;//是否展示大图

    private String recomToken;//随机id

    private Object thumbnail;//王晨透传晓伟

    private String source;//站内外来源

    private String appVersion;//app版本

    private String bs;//王晨透传统计字段

    private String showReason;//用于客户端展示推荐原因

    private String feedbackFeatures;//负反馈使用的标签词

    private Long timestamp = System.currentTimeMillis();  //文章更新时间戳

    private Long updatetime;//文章重编辑更新时间

    private int dataSource;// 0推荐 1编辑(精品池)  2编辑（非精品池） //运营数据来源

    private int statSource;//0推荐 1编辑  //统计来源

    private String payload;//透传字段

    private String style; //h5 展示类型 投放通道透传

    private String loclist; //获取地理位置信息

    private HashMap<String, HashSet<String>> locmap;//获取地理位置信息

    //-------------投放系统字段------------------------------
    private Object marqueeList;//图集地址 投放通道透传
    private String moreType;  // 投放通道透传的BidBean的类型

    /**
     * 这里存储hbase中查询得到的帖子的url（视频就是guid）
     * 也用来存储投放通道透传的url
     */
    private String url;
    private String newstype;//投放透传字段newstype表示类型
    private Integer recomPos;//投放传递强插位置
    private String mark;//投放通道透传mark

    //-----------ctr使用的参数----------------------
    private String ctrQ; //ctr使用的q值
    private String reason; //推荐原因

    //-----------对外提供的拓展字段
    private Map<String, Object> ext;

    private String ctrInfo; //ctr的debug信息原因

    private String recallInfo; //召回的详细信息原因

    /** 一点账号等级 */
    private String wemediaLevel;

    /**图片指纹*/
    private String picFingerprint;

    private List<String> category;

    private String timeSensitive;

    //文章是否在线
    private String onLine;
    /** 曝光量 */
    private Double totalEv;
    /** 点击量 */
    private Double totalPv;
    /** 转化率 */
    private Double convertRatio;

    //-----------开发中自己使用的拓展字段，不对外暴露----------------------------
   // @JsonIgnore
    private Map<String, String> devExt;
    //-----------开发中自己使用的分类c字段，不对外暴露----------------------------
    //@JsonIgnore
    private List<String> devCList;
    //-----------开发中自己使用的实体词字段，不对外暴露----------------------------
   //@JsonIgnore
    private List<String> devEtList;
    //每种类型document都要透传
   // @JsonIgnore
    private String specialParam;

    //标记是否是双标题文章
   //@JsonIgnore
    private boolean isDoubleTitle=false;


    /**
     * 视频重排的 质量度（调用周康的服务获取）
     */
    private String videoRatingQ;

    /**
     * 长效标记
     */
    private String disType;

    /**
     * 是否是凤凰卫视 内容
     */
    private String isIfengVideo;

    /**
     * 媒体评级  因对 s级媒体
     */
    private String sourceLevel;

    /**
     * 编辑给打的标签 冷启动用户数据专用
     */
    private String editTag;

    /**
     * 分类扩展信息，例如小视频
     */
    private String partCategory;

    /**
     * 过滤使用的扩展信息，例如世界杯内容
     */
    private String partCategoryExt;

    /**
     * 正能量专用的一个评分值  取决于文章的字数 排版 媒体评级等
     */
    private Double qualityEvalLevel;

    private String performance;

    private String marqueeChannel;

    private String topic1;

    private String c;
    private String sc;

    private List<String> DevScList;

    /**
     * bid专用 为bid返回recallTag
     */
    private String bitTag;

    public String lda_topic;

    private List<String> ldaTopicList;

    private String strategy; //strategy推荐策略

    private String topicLink; //topicLink 热点透传专题id 吐给小伟客户端

    private String hotboostSaveTime; //是否是新版hotboost标识

    /**
     * 是否是热点置顶文章 0 否 1 是
     */
    private Integer isHotTop;

    /**
     * 是否是热点聚合文章
     */
    private Integer converge;

    public Integer getIsHotTop() {
        return isHotTop;
    }

    public void setIsHotTop(Integer isHotTop) {
        this.isHotTop = isHotTop;
    }

    public Integer getConverge() {
        return converge;
    }

    public void setConverge(Integer converge) {
        this.converge = converge;
    }

    public Double getQualityEvalLevel() {
        return qualityEvalLevel;
    }

    public void setQualityEvalLevel(Double qualityEvalLevel) {
        this.qualityEvalLevel = qualityEvalLevel;
    }

    public String getHotboostSaveTime() {
        return hotboostSaveTime;
    }

    public void setHotboostSaveTime(String hotboostSaveTime) {
        this.hotboostSaveTime = hotboostSaveTime;
    }
    public String getTopicLink() {
        return topicLink;
    }

    public void setTopicLink(String topicLink) {
        this.topicLink = topicLink;
    }

    public List<String> getDevScList() {
        return DevScList;
    }

    public void setDevScList(List<String> devScList) {
        DevScList = devScList;
    }

    public String getBitTag() {
        return bitTag;
    }

    public void setBitTag(String bitTag) {
        this.bitTag = bitTag;
    }

    public String getPerformance() {
        return performance;
    }

    public void setPerformance(String performance) {
        this.performance = performance;
    }

    public String getMarqueeChannel() {
        return marqueeChannel;
    }

    public void setMarqueeChannel(String marqueeChannel) {
        this.marqueeChannel = marqueeChannel;
    }



    public String getTopic1() {
        return topic1;
    }

    public void setTopic1(String topic1) {
        this.topic1 = topic1;
    }

    public String getC() {
        return c;
    }

    public void setC(String c) {
        this.c = c;
    }

    public String getSc() {
        return sc;
    }

    public void setSc(String sc) {
        this.sc = sc;
    }

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public String getEditTag() {
        return editTag;
    }

    public void setEditTag(String editTag) {
        this.editTag = editTag;
    }

    public Double subScore;//订阅文章分数 只为订阅逻辑用



    public String getLda_topic() {
        return lda_topic;
    }

    public void setLda_topic(String lda_topic) {
        this.lda_topic = lda_topic;
    }

    public List<String> getLdaTopicList() {
        return ldaTopicList;
    }

    public void setLdaTopicList(List<String> ldaTopicList) {
        this.ldaTopicList = ldaTopicList;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public static Pattern getPattern() {
        return pattern;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getImportDate() {
        return importDate;
    }

    public void setImportDate(Date date) {
        this.importDate = date;
    }

    public String getHotLevel() {
        return hotLevel;
    }

    public void setHotLevel(String hotLevel) {
        this.hotLevel = hotLevel;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public String getDocChannel() {
        return docChannel;
    }

    public void setDocChannel(String docChannel) {
        this.docChannel = docChannel;
    }

    public String getWhy() {
        return why;
    }

    public void setWhy(String why) {
        this.why = why;
    }

    public String getTimeSensitive() {
        return timeSensitive;
    }

    public void setTimeSensitive(String timeSensitive) {
        this.timeSensitive = timeSensitive;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getHotBoost() {
        return hotBoost;
    }

    public void setHotBoost(String hotBoost) {
        this.hotBoost = hotBoost;
    }

    public String getReadableFeatures() {
        return readableFeatures;
    }

    public String getChannels() {
        return channels;
    }

    public void setChannels(String channels) {
        this.channels = channels;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getRtag() {
        return rtag;
    }

    public void setRtag(String rtag) {
        this.rtag = rtag;
    }

    public void setReadableFeatures(String readableFeatures) {
        this.readableFeatures = readableFeatures;
    }

    public String getLocList() {
        return this.loclist;
    }

    public void setLocList(String loclist) {
        this.loclist = loclist;
    }

    public String getOthers() {
        return others;
    }

    public void setOthers(String others) {
        this.others = others;
    }

    public String getSimId() {
        return simId;
    }

    public void setSimId(String simId) {
        this.simId = simId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getRecallid() {
        return recallid;
    }

    public void setRecallid(String recallid) {
        this.recallid = recallid;
    }

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public String getRevealType() {
        return revealType;
    }

    public void setRevealType(String revealType) {
        this.revealType = revealType;
    }

    public String getRecomToken() {
        return recomToken;
    }

    public void setRecomToken(String recomToken) {
        this.recomToken = recomToken;
    }

    public Object getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(Object thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getBs() {
        return bs;
    }

    public void setBs(String bs) {
        this.bs = bs;
    }

    public String getShowReason() {
        return showReason;
    }

    public void setShowReason(String showReason) {
        this.showReason = showReason;
    }

    public String getFeedbackFeatures() {
        return feedbackFeatures;
    }

    public void setFeedbackFeatures(String feedbackFeatures) {
        this.feedbackFeatures = feedbackFeatures;
    }

    public Long getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(Long updatetime) {
        this.updatetime = updatetime;
    }

    public int getDataSource() {
        return dataSource;
    }

    public void setDataSource(int dataSource) {
        this.dataSource = dataSource;
    }

    public int getStatSource() {
        return statSource;
    }

    public void setStatSource(int statSource) {
        this.statSource = statSource;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public Object getMarqueeList() {
        return marqueeList;
    }

    public void setMarqueeList(Object marqueeList) {
        this.marqueeList = marqueeList;
    }

    public String getMoreType() {
        return moreType;
    }

    public void setMoreType(String moreType) {
        this.moreType = moreType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCtrQ() {
        return ctrQ;
    }

    public void setCtrQ(String ctrQ) {
        this.ctrQ = ctrQ;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Map<String, String> getDevExt() {
        return devExt;
    }

    public void setDevExt(Map<String, String> devExt) {
        this.devExt = devExt;
    }

    public List<String> getDevCList() {
        return devCList;
    }

    public void setDevCList(List<String> devCList) {
        this.devCList = devCList;
    }

    public List<String> getDevEtList() {
        return devEtList;
    }

    public void setDevEtList(List<String> devEtList) {
        this.devEtList = devEtList;
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }

    public String getNewstype() {
        return newstype;
    }

    public void setNewstype(String newstype) {
        this.newstype = newstype;
    }

    public Integer getRecomPos() {
        return recomPos;
    }

    public void setRecomPos(int recomPos) {
        this.recomPos = recomPos;
    }

    public String getCtrInfo() {
        return ctrInfo;
    }

    public void setCtrInfo(String ctrInfo) {
        this.ctrInfo = ctrInfo;
    }

    public String getRecallInfo() {
        return recallInfo;
    }

    public void setRecallInfo(String recallInfo) {
        this.recallInfo = recallInfo;
    }

    public Map<String, Object> getExt() {
        return ext;
    }

    public void setExt(Map<String, Object> ext) {
        this.ext = ext;
    }

    public String getSpecialParam() {
        return specialParam;
    }

    public void setSpecialParam(String specialParam) {
        this.specialParam = specialParam;
    }

    public void setIsDoubleTitle(boolean isDoubleTitle){
        this.isDoubleTitle = isDoubleTitle;
    }

    public boolean getIsDoubleTitle(){
        return this.isDoubleTitle;
    }

    public String getWemediaLevel() {
        return wemediaLevel;
    }

    public void setWemediaLevel(String wemediaLevel) {
        this.wemediaLevel = wemediaLevel;
    }

    public String getPicFingerprint() {
        return picFingerprint;
    }

    public void setPicFingerprint(String picFingerprint) {
        this.picFingerprint = picFingerprint;
    }

    public String getVideoRatingQ() {
        return videoRatingQ;
    }

    public void setVideoRatingQ(String videoRatingQ) {
        this.videoRatingQ = videoRatingQ;
    }

    public String getDisType() {
        return disType;
    }

    public void setDisType(String disType) {
        this.disType = disType;
    }

    public String getSourceLevel() {
        return sourceLevel;
    }

    public void setSourceLevel(String sourceLevel) {
        this.sourceLevel = sourceLevel;
    }

    public String getIsIfengVideo() {
        return isIfengVideo;
    }

    public void setIsIfengVideo(String isIfengVideo) {
        this.isIfengVideo = isIfengVideo;
    }

    public List<String> getCategory() {
        return category;
    }

    public void setCategory(List<String> category) {
        this.category = category;
    }

    // 地域识别的结果
    public HashMap<String, HashSet<String>> getLocMap() { return this.locmap; }

    public void setLocMap(HashMap<String, HashSet<String>> locmap) {
        this.locmap = locmap;
    }

    public Double getTotalEv() {
        return totalEv;
    }

    public void setTotalEv(Double totalEv) {
        this.totalEv = totalEv;
    }

    public Double getTotalPv() {
        return totalPv;
    }

    public void setTotalPv(Double totalPv) {
        this.totalPv = totalPv;
    }

    public Double getConvertRatio() {
        return convertRatio;
    }

    public void setConvertRatio(Double convertRatio) {
        this.convertRatio = convertRatio;
    }

    public String getPartCategory() {
        return partCategory;
    }

    public void setPartCategory(String partCategory) {
        this.partCategory = partCategory;
    }

    public String getPartCategoryExt() {
        return partCategoryExt;
    }

    public void setPartCategoryExt(String partCategoryExt) {
        this.partCategoryExt = partCategoryExt;
    }

    public String getOnLine(){
        return onLine;
    }

    public void setOnLine(String onLine){
        this.onLine = onLine;
    }

    public Double getSubScore() {
        return subScore;
    }

    public void setSubScore(Double subScore) {
        this.subScore = subScore;
    }

    @Override
    public String toString() {
        return "Document [title=" + title + ", docid=" + docId + ", score=" + score + ", totalEv=" + totalEv + ", totalPv=" + totalPv + ", convertRatio=" + convertRatio + ", importDate=" + importDate + "]";
    }

    @Override
    public Document clone() {
        Document clone = null;
        try {
            clone = (Document) super.clone();
        } catch (CloneNotSupportedException e) {
            log.error("CloneNotSupportedException", e);
        }
        return clone;
    }

    public Document() {

    }

    public Document(String docId) {
        this.docId = docId;
    }

}
