package com.ifeng.recallScheduler.filterController.service.impl;

import com.beust.jcommander.internal.Sets;
import com.ifeng.recallScheduler.filterAssemble.SpecialFilterHandler;
import com.ifeng.recallScheduler.apolloConf.ApolloConstant;
import com.ifeng.recallScheduler.apolloConf.DebugUserConfig;
import com.ifeng.recallScheduler.apolloConf.DocSourceFilterConfigUtil;
import com.ifeng.recallScheduler.apolloConf.KeywordFilterConfigUtil;
import com.ifeng.recallScheduler.constant.*;
import com.ifeng.recallScheduler.constant.cache.CacheFactory;
import com.ifeng.recallScheduler.filter.MediaFilter;
import com.ifeng.recallScheduler.filter.SansuFilter;
import com.ifeng.recallScheduler.filter.SourceFilterByMediaId;
import com.ifeng.recallScheduler.filterController.service.FilterServiceImpl;
import com.ifeng.recallScheduler.filterStrategy.WxbFilterService;
import com.ifeng.recallScheduler.item.Document;
import com.ifeng.recallScheduler.logUtil.FilterLog;
import com.ifeng.recallScheduler.logUtil.StackTraceUtil;
import com.ifeng.recallScheduler.request.RequestInfo;
import com.ifeng.recallScheduler.rule.impl.RuleHandler;
import com.ifeng.recallScheduler.utils.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FilterService implements FilterServiceImpl {


    private final static Logger log = LoggerFactory.getLogger(FilterService.class);

    @Autowired
    private EhCacheUtil ehCacheUtil;

    @Autowired
    private DebugUserConfig debugUserConfig;

    @Autowired
    private SpecialFilterHandler specialFilterHandler;

    @Autowired
    private SpecialFilterUserUtil specialFilterUserUtil;

    @Autowired
    private SansuFilter sansuFilter;

    @Autowired
    private MediaFilter mediaFilter;

    @Autowired
    private VideoAppChannelFilterService videoAppChannelFilterService;

    @Autowired
    private SourceFilterByMediaId sourceFilterByMediaId;


    /**
     * @param requestInfo
     * @param doc             备选doc
     * @param focusSimids     焦点图过滤使用
     * @param regularSimids   固定位置强插过滤使用
     * @param needLocalFilter 是否需要本地通道过滤
     * @param recomReason     通道的召回原因，针对特殊通道有特殊的过滤方式
     * @return
     */
    @Override
    public boolean needFilter(RequestInfo requestInfo, Document doc, Set<String> focusSimids, Set<String> regularSimids, Set<String> sansuSimIdSet, boolean needLocalFilter, String recomReason) {



        String uid = requestInfo.getUserId();
        String simId = doc.getSimId();
        boolean isDebugUser = requestInfo.isDebugUser();

        /**
         * 网信办 下线时间过滤 此处只针对网信办处罚时间
         */
        if (WxbFilterService.needTimeFilter(requestInfo, doc)) {
            DebugUtil.debugLog(isDebugUser, "{} needTimeFilter {},simid:{}, docid:{},type:{},title:{},r:{}", uid, doc.getDate(), doc.getSimId(), doc.getDocId(), doc.getDocType(), doc.getTitle(), recomReason);
            return true;
        }


        /**
         * 过滤标题长度 1.小视频标题长度小于3则过滤 2.其他视频标题长度小于5则过滤
         */
        if (needFilterTitleLength(doc)) {
            DebugUtil.debugLog(isDebugUser, "{} needFilter needFilterTitleLength simid:{}, docid:{},type:{},title:{},r:{}", uid, doc.getSimId(), doc.getDocId(), doc.getDocType(), doc.getTitle(), recomReason);
            if (FlowTypeUtils.isIfengnewsDiscovery(requestInfo)){
                FilterLog.toFileBeat(requestInfo, doc, recomReason, FilterLogConstant.TitleLengthLimited);
            }
            return true;
        }


        /**
         * TODO 过滤掉文章online字段为0的 0表示下线
         */
        if (needFilterOnline(doc)) {
            //DebugUtil.debugLog(isDebugUser,"{} needFileterOnlineIsZero simid:{},docid:{},type:{},title:{},onLine:{},r:{}",uid,doc.getSimId(),doc.getDocId(),doc.getDocType(),doc.getTitle(),doc.getOnLine(),recomReason);
            log.info("{} needFilterOnlineIsZero simid:{},docid:{},type:{},title:{},onLine:{},r:{}", uid, doc.getSimId(), doc.getDocId(), doc.getDocType(), doc.getTitle(), doc.getOnLine(), recomReason);
            if (FlowTypeUtils.isIfengnewsDiscovery(requestInfo)){
                FilterLog.toFileBeat(requestInfo, doc, recomReason, FilterLogConstant.OnlineStatusZero);
            }
            return true;
        }

        /**
         * 是否需要过滤小视频
         */
        if (specialFilterHandler.needFilterMiniVideo(requestInfo, doc)) {
            DebugUtil.debugLog(isDebugUser, "{} needFilterMiniVideo {},simid:{}, docid:{},type:{},title:{},r:{}", uid, doc.getPartCategory(), doc.getSimId(), doc.getDocId(), doc.getDocType(), doc.getTitle(), recomReason);
            if (FlowTypeUtils.isIfengnewsDiscovery(requestInfo)){
                FilterLog.toFileBeat(requestInfo, doc, recomReason, FilterLogConstant.MiniVideo);
            }
            return true;
        }

        /**
         * 过滤趣头条视频
         */
        if (specialFilterHandler.needQuTTFilter(requestInfo, doc)) {
            DebugUtil.debugLog(isDebugUser,"{} needQuTTFilter {},simid:{}, docid:{},type:{},title:{},r:{}", uid, doc.getPartCategoryExt(), doc.getSimId(), doc.getDocId(), doc.getDocType(), doc.getTitle(), recomReason);
            if (FlowTypeUtils.isIfengnewsDiscovery(requestInfo)){
                FilterLog.toFileBeat(requestInfo, doc, recomReason, FilterLogConstant.QuTouTiao);
            }
            return true;
        }


        /**
         * 快头条只出SAB或者精品池内容
         * @return
         */
        if(specialFilterHandler.needKttFilterSAB(requestInfo, doc)){
            DebugUtil.debugLog(isDebugUser, "{} needKtt filter:{} SAB:{} disType:{}", requestInfo.getUserId(), doc.getDocId(), doc.getSourceLevel(), doc.getDisType());
            return true;
        }

        /**
         * E级媒体过滤
         */
        if (specialFilterHandler.needFilterSourceLevel_E(requestInfo, doc)) {
            DebugUtil.debugLog(isDebugUser,"{} needFilter LevelE level:{},simid:{}, docid:{},type:{},title:{} ", uid, doc.getWemediaLevel(), doc.getSimId(), doc.getDocId(), doc.getDocType(), doc.getTitle());
            if (FlowTypeUtils.isIfengnewsDiscovery(requestInfo)){
                FilterLog.toFileBeat(requestInfo, doc, recomReason, FilterLogConstant.LevelE);
            }
            return true;
        }

        /**
         * 北上广一线城市按审核标签过滤掉
         */
        if (specialFilterHandler.needFilterByAuditTag(requestInfo, doc)) {
            DebugUtil.debugLog(isDebugUser, "{} needFilter by AuditTag:{},simid:{}, docid:{},type:{},title:{} ", uid, doc.getWemediaLevel(), doc.getSimId(), doc.getDocId(), doc.getDocType(), doc.getTitle());
            if (FlowTypeUtils.isIfengnewsDiscovery(requestInfo)){
                FilterLog.toFileBeat(requestInfo, doc, recomReason, FilterLogConstant.AuditTag);
            }
            return true;
        }

        /**
         *   针对wxb的临时逻辑
         */
        if (StringUtils.isNotBlank(recomReason) && specialFilterUserUtil.isWxbUser(requestInfo)) {
            if (recomReason.contains(RecWhy.ReasonUser_cf) || recomReason.contains(RecWhy.ReasonCotag_crawl_video)) {
                log.info("{} needFilter recomReason docid:{},type:{},title:{},r:{}", uid, doc.getDocId(), doc.getDocType(), doc.getTitle(), recomReason);
                if (FlowTypeUtils.isIfengnewsDiscovery(requestInfo)){
                    FilterLog.toFileBeat(requestInfo, doc, recomReason, FilterLogConstant.SpecialRecomReasonForWXB);
                }
                return true;
            }
        }

        /**
         * 此处需要预加载 注意在启动类上使用@Autowired
         * TODO 焦点图过滤 这里从cache中 存储的是需要过滤的焦点图的simid 针对老版本使用的焦点图过滤 新版本已经没有焦点图了  还有如何从ES获取数据
         */
        if (focusSimids.contains(simId)) {
            DebugUtil.debugLog(isDebugUser, "{} needFilter focusSimids simid:{}, docid:{},type:{},title:{},r:{}", uid, doc.getSimId(), doc.getDocId(), doc.getDocType(), doc.getTitle(), recomReason);
            if (FlowTypeUtils.isIfengnewsDiscovery(requestInfo)){
                FilterLog.toFileBeat(requestInfo, doc, recomReason, FilterLogConstant.FocusSimidDuplicate);
            }
            return true;
        }

        /**
         * 编辑固定位排重  这里的初始化 是由SchedulerManager的定时任务进行的初始化
         */
        if (specialFilterHandler.isRegularTestUser(requestInfo)&&specialFilterHandler.needFilterRegular(requestInfo, doc)) {
            DebugUtil.debugLog(isDebugUser, "{} editorRegular needFilter {},type:{},title:{},r:{}", uid, doc.getDocId(), doc.getDocType(), doc.getTitle(), recomReason);
            if (FlowTypeUtils.isIfengnewsDiscovery(requestInfo)){
                FilterLog.toFileBeat(requestInfo, doc, recomReason, FilterLogConstant.EditorRegular);
            }
            return true;
        }else if (regularSimids.contains(simId)) {  //现在使用的 固定位是否需要过滤
            DebugUtil.debugLog(isDebugUser, "{} needFilter regularSimids simid:{}, docid:{},type:{},title:{},r:{}", uid, doc.getSimId(), doc.getDocId(), doc.getDocType(), doc.getTitle(), recomReason);
            if (FlowTypeUtils.isIfengnewsDiscovery(requestInfo)){
                FilterLog.toFileBeat(requestInfo, doc, recomReason, FilterLogConstant.EditorRegularSimidDuplicate);
            }
            return true;
        }

        /**
         * 低质量文章过滤
         */
        if (requestInfo.getUserTypeMap().getOrDefault(GyConstant.needLowSimidFilter, false)) {
            if (needFilterSansuSimId(requestInfo, doc.getSimId(), sansuSimIdSet)) {
                DebugUtil.debugLog(isDebugUser, "{} needFilter negative comment simid:{}, docid:{},type:{},title:{},r:{}", uid, doc.getSimId(), doc.getDocId(), doc.getDocType(), doc.getTitle(), recomReason);
                if (FlowTypeUtils.isIfengnewsDiscovery(requestInfo)){
                    FilterLog.toFileBeat(requestInfo, doc, recomReason, FilterLogConstant.NegativeComment);
                }
                return true;
            }
        }


        /**
         * Bloom过滤
         */
        if (!RuleHandler.INSTANCE.bloomcheck.filter(doc, uid)) {
            DebugUtil.debugLog(isDebugUser, "{} needFilter bloomcheck simid:{}, docid:{},type:{},title:{},r:{}", uid, doc.getSimId(), doc.getDocId(), doc.getDocType(), doc.getTitle(), recomReason);
            if (FlowTypeUtils.isIfengnewsDiscovery(requestInfo)){
                FilterLog.toFileBeat(requestInfo, doc, recomReason, FilterLogConstant.BloomCheck);
            }
            return true;
        }

        /**
         * 对判断是黑名单的文章进行过滤
         */
        if (RuleHandler.INSTANCE.isBlackDocId(doc.getDocId())) {
            DebugUtil.debugLog(isDebugUser, "{} needFilter blackList {},type:{},title:{},r:{}", uid, doc.getDocId(), doc.getDocType(), doc.getTitle(), recomReason);
            if (FlowTypeUtils.isIfengnewsDiscovery(requestInfo)){
                FilterLog.toFileBeat(requestInfo, doc, recomReason, FilterLogConstant.BlackList);
            }
            return true;
        }

        /**
         * 本地Map进行过滤 通过needLocalFilter判断是否进行本地过滤 这里应该是针对本地频道进行的一种过滤
         */
        if (needLocalFilter&&RuleHandler.INSTANCE.localMapSetWithoutUserSub.filter(doc, requestInfo, recomReason)) {  //判断doc 里 locMap 里的 province 集合 与用户 loc 进行比对
            DebugUtil.debugLog(isDebugUser, "{} needFilter localMapSet {},type:{},title:{},r:{}", uid, doc.getDocId(), doc.getDocType(), doc.getTitle(), recomReason);
            if (FlowTypeUtils.isIfengnewsDiscovery(requestInfo)){
                FilterLog.toFileBeat(requestInfo, doc, recomReason, FilterLogConstant.LocalMapSetNotMatch);
            }
            return true;
        }

        /**
         * 负反馈过滤
         */
        if (!RuleHandler.INSTANCE.feedBack.filter(doc, requestInfo)) {
            DebugUtil.debugLog(isDebugUser, "{} needFilter feedBack {},type:{},title:{},r:{}", uid, doc.getDocId(), doc.getDocType(), doc.getTitle(), recomReason);
            if (FlowTypeUtils.isIfengnewsDiscovery(requestInfo)){
                FilterLog.toFileBeat(requestInfo, doc, recomReason, FilterLogConstant.NegativeFeedback);
            }
            return true;
        }

        /**
         * 特殊词过滤 目前只有av 后期可进行继续添加
         */
        if (!RuleHandler.INSTANCE.keyword.filter(doc, KeywordFilterConfigUtil.toBeFilteredKeywordMap)) {
            log.info("{} needFilter keyword {},type:{},title:{},r:{}", uid, doc.getDocId(), doc.getDocType(), doc.getTitle(), recomReason);
            if (FlowTypeUtils.isIfengnewsDiscovery(requestInfo)){
                FilterLog.toFileBeat(requestInfo, doc, recomReason, FilterLogConstant.TitleKeyword);
            }
            return true;
        }

        /**
         * 文章来源过滤
         */
        if (!RuleHandler.INSTANCE.docSource.filter(doc, DocSourceFilterConfigUtil.docSourceMap)) {
            log.info("{} needFilter doc source {},type:{},source:{},title:{},r:{}", uid, doc.getDocId(), doc.getDocType(), doc.getSource(), doc.getTitle(), recomReason);
            if (FlowTypeUtils.isIfengnewsDiscovery(requestInfo)){
                FilterLog.toFileBeat(requestInfo, doc, recomReason, FilterLogConstant.sourceBlacklist);
            }
            return true;
        }


        /**
         * 从reids获取需要过滤的文章 然后通过前缀树结构对文章title进行匹配 最后根据匹配的分值 和 用户分群的阈值进行比较 若分值超过阈值则过滤
         */
        if (sansuFilter.titleFilter(requestInfo, doc)) {
            DebugUtil.debugLog(isDebugUser, "{} needFilter titleFilter {},type:{},title:{},r:{}", uid, doc.getDocId(), doc.getDocType(), doc.getTitle(), recomReason);
            if (FlowTypeUtils.isIfengnewsDiscovery(requestInfo)){
                FilterLog.toFileBeat(requestInfo, doc, recomReason, FilterLogConstant.TitleKeywordScore);
            }
            return true;
        }


        /**
         * 媒体过滤
         */
        if (mediaFilter.filterByMedia(requestInfo, doc)) {
            DebugUtil.debugLog(isDebugUser,"{} needFilter banned Media {}, type:{}, title:{}, source:{} ", uid, doc.getDocId(), doc.getDocType(), doc.getTitle(), doc.getSource());
            if (FlowTypeUtils.isIfengnewsDiscovery(requestInfo)){
                FilterLog.toFileBeat(requestInfo, doc, recomReason, FilterLogConstant.BannedMedia);
            }
            return true;
        }

        /**
         * 机构过滤
         */
        if (specialFilterHandler.needFilterSourceOnlyShowJiGou(requestInfo, doc, recomReason)) {
            DebugUtil.debugLog(isDebugUser, "{} needFilterSourceOnlyShowJiGou {},type:{},title:{},source:{},r:{}", uid, doc.getDocId(), doc.getDocType(), doc.getTitle(), doc.getSource(), recomReason);
            if (FlowTypeUtils.isIfengnewsDiscovery(requestInfo)){
                FilterLog.toFileBeat(requestInfo, doc, recomReason, FilterLogConstant.ShowAgent);
            }
            return true;
        }


        /**
         * 针对网信办 或 特定城市 过滤掉凤凰卫视数据
         */
        if (!requestInfo.isDebugUser()) {   //过滤掉凤凰卫视数据
            if (specialFilterHandler.needFilterIfengVideo(requestInfo, doc)) {
                log.info("{},{} needFilterIfengVideo {},type:{},title:{},source:{},r:{}", uid, requestInfo.getPermanentLoc(), doc.getDocId(), doc.getDocType(), doc.getTitle(), doc.getSource(), recomReason);
                if (FlowTypeUtils.isIfengnewsDiscovery(requestInfo)){
                    FilterLog.toFileBeat(requestInfo, doc, recomReason, FilterLogConstant.IfengVideo);
                }
                return true;
            }
        }


        /**
         * 针对视频app的内容过滤
         */
        if (videoAppChannelFilterService.needFilter(requestInfo, doc)) {
            return true;
        }

        /**
         * 推荐频道不出热点内容
         */
        if (StringUtils.isNotBlank(requestInfo.getRecomChannel()) && requestInfo.getRecomChannel().equals(RecomChannelEnum.recom.getValue())) {
            String why = doc.getWhy();
            if (StringUtils.isNotBlank(why) && why.contains(RecWhy.ReasonHot_tag)) {
                return true;
            }
        }

        /**
         * 前三刷WXB用户过滤娱乐
         */
        if (needFilterEnt(requestInfo, doc)) {
            DebugUtil.debugLog(isDebugUser, "{} Entertainment Filter {},type:{},title:{},source:{},r:{}", uid, doc.getDocId(), doc.getDocType(), doc.getTitle(), doc.getSource(), recomReason);
            return true;
        }

        /**
         * 除本地通道出的天气外 其余全部过滤掉天气
         */
        if (specialFilterHandler.needFilterTianqi(requestInfo, doc, recomReason)) {
            DebugUtil.debugLog(isDebugUser, "{} Tianqi Filter {},type:{},title:{},r:{}", uid, doc.getDocId(), doc.getDocType(), doc.getTitle(), recomReason);
            if (FlowTypeUtils.isIfengnewsDiscovery(requestInfo)){
                FilterLog.toFileBeat(requestInfo, doc, recomReason, FilterLogConstant.Weather);
            }
            return true;
        }

        /**
         * 召回通道已进行过滤，出口再进行过滤避免缓存问题
         */
        if(filterByTimeSensitive(requestInfo, doc)){
            DebugUtil.debugLog(isDebugUser, "{} docId:{} filter By expireTime:{}", requestInfo.getUserId(), doc.getDocId(), doc.getTimeSensitive());
            if (FlowTypeUtils.isIfengnewsDiscovery(requestInfo)){
                FilterLog.toFileBeat(requestInfo, doc, recomReason, FilterLogConstant.TimeSensitive);
            }
            return true;
        }

        /**
         * 过滤特定mediaId文章
         */
        if(sourceFilterByMediaId.filterDocuments(doc)){
            DebugUtil.debugLog(requestInfo.isDebugUser(), "uid:{} filter doc:{} by mediaId:{}", requestInfo.getUserId(), doc.getDocId(), doc.getMediaId());
            return true;
        }

        /**
         * ab测试，只出优质账号内容，过滤非优质账号内容
         */
        if(requestInfo.checkAbtestInfo(AbTestConstant.Abtest_HighQualitySource_Test, AbTestConstant.Abtest_HighQualitySource_test_onlyHigh_rate10)){
            if(specialFilterHandler.needFilterHighQualitySource(requestInfo, doc)){
                if(MathUtil.getNum(100)<5){
                    log.warn("uid:{} filter not high quality doc:{} mediad:{} recomReason:{}", requestInfo.getUserId(), doc.getDocId(),
                            doc.getMediaId(),recomReason);
                }
                return true;
            }

            if(specialFilterHandler.needFilterSourceLv(doc, GyConstant.levelLimitSetLvSAB)){
                DebugUtil.debugLog(requestInfo.isDebugUser(),"uid:{}  high quality Level filter:{} mediaid:{} recomReason:{}",
                        requestInfo.getUserId(), doc.getDocId(), doc.getMediaId(),recomReason);
                if (FlowTypeUtils.isIfengnewsDiscovery(requestInfo)){
                    FilterLog.toFileBeat(requestInfo, doc, recomReason, FilterLogConstant.HighQualityLevel);
                }
                return true;
            }
        }

        /**
         * 图谱过滤
         */
        if (specialFilterHandler.isGraphFilter(recomReason)&& specialFilterHandler.needFilterHealth(requestInfo, doc)) {
            DebugUtil.debugLog(isDebugUser, "{} graph Filter {},type:{},title:{},r:{}", uid, doc.getDocId(), doc.getDocType(), doc.getTitle(), recomReason);
            if (FlowTypeUtils.isIfengnewsDiscovery(requestInfo)){
                FilterLog.toFileBeat(requestInfo, doc, recomReason, FilterLogConstant.GraphFromOtherChannel);
            }
            return true;
        }



        return false;
    }


    /**
     * 标题字数过滤
     *
     * @param doc
     * @return
     */
    private boolean needFilterTitleLength(Document doc) {
        if (doc == null) {
            return true;
        }
        String title = doc.getTitle();
        if (StringUtils.isBlank(title)) {
            return true;
        }

        int titleLength = title.length();
        //小视频标题小于3 则过滤
        if (DocUtil.isMiniVideo(doc)) {
            if (titleLength < GyConstant.miniVideoLengthLimit) {
                return true;
            }
        }

        if (titleLength < GyConstant.titleLengthLimit) {
            return true;
        }

        return false;
    }

    private boolean needFilterOnline( Document doc) {
        if (doc == null) {
            return true;
        }
        String online = doc.getOnLine();
        //当online字段等于0的时候 需要过滤掉
        if (StringUtils.isNotEmpty(online)) {
            if (online.equals("0")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取焦点图simid集合
     */
    public Set<String> getFocusSimIdSet() {
        Set<String> focusSimids = new HashSet<>();
        try{
            List<Document> editorFocusList = (List<Document>) ehCacheUtil.getListDoc(CacheFactory.CacheName.EditorFocus.getValue(), CacheFactory.CacheName.EditorFocus.getValue());
            editorFocusList = CollectionUtils.isEmpty(editorFocusList) ? Collections.EMPTY_LIST : editorFocusList;
            focusSimids = editorFocusList.stream().map(x -> x.getSimId()).collect(Collectors.toSet());
        }catch (Exception e){
            log.error("getFocusSimIdSet error:{}",e);
            return new HashSet<>();
        }
        return focusSimids;
    }

    /**
     * 判断是否要过来三俗
     */
    public boolean needFilterSansuSimId(RequestInfo requestInfo, String simId, Set<String> sansuSimIdSet) {
        if (StringUtils.isBlank(simId)) {
            return false;
        }
        if (CollectionUtils.isEmpty(sansuSimIdSet)) {
            return false;
        }


        boolean needFilter = sansuSimIdSet.contains(simId);

        if (needFilter) {
            if (requestInfo.isDebugUser()) {
                log.info("{},needFilterSansuSimId,simId:{}", requestInfo.getUserId(), simId);
            }
        }

        return needFilter;
    }

    /**
     * 三俗的过滤simid集合
     */
    public Set<String> getSanSuSimIdSet(RequestInfo requestInfo) {
        Set<String> sansuSimIdSet = (Set<String>) ehCacheUtil.getSetStr(CacheFactory.CacheName.SansuBlack.getValue(), CacheFactory.CacheName.SansuBlack.getValue());
        if (CollectionUtils.isEmpty(sansuSimIdSet)) {
            sansuSimIdSet = Sets.newHashSet();
        }

        //插入 负评 level 5 评级
        Set<String> negCommentLevel5SimIdSet = (Set<String>) ehCacheUtil.getSetStr(CacheFactory.CacheName.NegCommentBlack.getValue(), CacheFactory.CacheName.Level5NegCommentBlack.getValue());
        if (CollectionUtils.isNotEmpty(negCommentLevel5SimIdSet)){
            sansuSimIdSet.addAll(negCommentLevel5SimIdSet);
        }

        // 强过滤负评文章
        //针对boss用户  针对冷启动用户 插入 等级低的 负评 文章 过滤
        if (debugUserConfig.getDebugUser(ApolloConstant.boss_User_Key).contains(requestInfo.getUserId()) || requestInfo.isColdUser()) {
            //插入 负评 level 1-4 评级
            Set<String> negCommentLevel1To4SimIdSet = (Set<String>) ehCacheUtil.getSetStr(CacheFactory.CacheName.NegCommentBlack.getValue(), CacheFactory.CacheName.Level1To4NegCommentBlack.getValue());
            if (CollectionUtils.isNotEmpty(negCommentLevel1To4SimIdSet)){
                sansuSimIdSet.addAll(negCommentLevel1To4SimIdSet);
            }
        }

        return sansuSimIdSet;
    }

    /**
     * 判断是否需要过滤娱乐,wxb用户三刷以内不出娱乐新闻
     *
     * @param requestInfo
     * @param doc
     * @return
     */
    private boolean needFilterEnt(RequestInfo requestInfo, Document doc) {
        if (specialFilterUserUtil.isWxbUser(requestInfo)) {
            if (requestInfo.getPullCount() > 2) {
                return false;
            }


            List<String> category = doc.getCategory();
            if (CollectionUtils.isNotEmpty(category) && category.contains(GyConstant.c_YuLe)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 文章时效性过滤
     *
     * @param requestInfo
     * @param document
     * @return
     */
    public boolean filterByTimeSensitive(RequestInfo requestInfo, Document document) {
        try {
            String dateStr = document.getTimeSensitive();
            if (StringUtils.isBlank(dateStr)||GyConstant.LongTime.equals(dateStr)) {
                return false;
            }
            Date expireDate = DateUtils.strToDate(dateStr);
            Date now = new Date();
            if (now.before(expireDate)) {
                return false;
            } else {
                return true;
            }

        } catch (Exception e) {
            log.error("{} parse doc:{} expireTime:{}", requestInfo.getUserId(), document.getDocId(), document.getTimeSensitive(), StackTraceUtil.getStackTrace(e));
            return true;
        }
    }

    /**
     * 固定位置的过滤simid集合
     */
    public Set<String> getRegularSimIdSet() {
        Set<String> regularSimids=new HashSet<>();
        try{
            List<Document> editorRegularList = (List<Document>) ehCacheUtil.getListDoc(CacheFactory.CacheName.EditorRegularPosition.getValue(), CacheFactory.CacheName.EditorRegularPosition.getValue());
            editorRegularList = CollectionUtils.isEmpty(editorRegularList) ? Collections.EMPTY_LIST : editorRegularList;
            regularSimids = editorRegularList.stream().map(x -> x.getSimId()).collect(Collectors.toSet());
        }catch (Exception e){
            log.error("getRegularSimIdSet error:{}",e);
            return new HashSet<>();
        }
        return regularSimids;
    }


}
