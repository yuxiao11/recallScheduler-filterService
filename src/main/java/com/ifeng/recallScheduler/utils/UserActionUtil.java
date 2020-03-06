package com.ifeng.recallScheduler.utils;

import com.beust.jcommander.internal.Lists;
import com.ifeng.recallScheduler.bean.LastDocBean;
import com.ifeng.recallScheduler.constant.GyConstant;
import com.ifeng.recallScheduler.constant.cache.CacheFactory;
import com.ifeng.recallScheduler.enums.ProidType;
import com.ifeng.recallScheduler.filter.SansuFilter;
import com.ifeng.recallScheduler.item.Document;
import com.ifeng.recallScheduler.logUtil.StackTraceUtil;
import com.ifeng.recallScheduler.redis.jedisPool.LocalhostJedisUtil;
import com.ifeng.recallScheduler.request.RequestInfo;
import com.ifeng.recallScheduler.timer.TimerEntity;
import com.ifeng.recallScheduler.timer.TimerEntityUtil;
import com.ifeng.recallScheduler.user.EvInfo;
import com.ifeng.recallScheduler.user.UserModel;
import com.ifeng.recallScheduler.user.UserSession;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by jibin on 2017/10/24.
 */
@Service
public class UserActionUtil {


    /**
     * 记录耗时的日志
     */
    private static final Logger logger = LoggerFactory.getLogger(UserActionUtil.class);

    @Autowired
    private LocalhostJedisUtil localhostJedisUtil;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private DocUtil docUtil;

    @Autowired
    private UserTjInfoUtil userTjInfoUtil;



    @Autowired
    private SansuFilter sansuFilter;


    /**
     * 视频正向递增，最大上限
     */
    private final static int maxNumAdd = 8;


    /**
     * 获取redis中存储的lastdocId的key
     *
     * @param requestInfo
     * @param lastDocId
     * @return
     */
    private String getlastDocKey(RequestInfo requestInfo, String lastDocId) {
        String uid = requestInfo.getUserId();
        String key = GyConstant.KeyClick_Pre + uid + lastDocId;
        return key;
    }


    private int getLastVideoCount(RequestInfo requestInfo) {
        int lastVideoCount = 0;
        List<LastDocBean> lastDocs = requestInfo.getNewLastDocList();
        if (CollectionUtils.isEmpty(lastDocs)) {
            return lastVideoCount;
        }
        Cache docCache = cacheManager.getCache(CacheFactory.CacheName.PersonalRecomDocumentInfo.getValue());
        for (LastDocBean lastDocBean : lastDocs) {
            try {
                String docId = lastDocBean.getDocId();
                String key = getlastDocKey(requestInfo, docId);
                String value = localhostJedisUtil.get(LocalhostJedisUtil.db_UserClick, key);
                if (StringUtils.isBlank(value)) {
                    if (docUtil.isVideoGuid(docId)) {
                        DebugUtil.debugLog(requestInfo.isDebugUser(),"{} initNewLastDoc videoGuid {}",requestInfo.getUserId(),key);
                        lastVideoCount++;
                    } else {
                        Document doc = docUtil.getDocByCache(docCache, docId);
                        //如果客户端传递的不是docid，而是guid，根据长度判断，如果是guid，则是视频
                        if (doc != null) {
                            if (docUtil.isVideo(doc)) {
                                DebugUtil.debugLog(requestInfo.isDebugUser(),"{} initNewLastDoc video {}",requestInfo.getUserId(),key);
                                lastVideoCount++;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("initNewLastDoc Error:{}", e);
            }
        }
        return lastVideoCount;
    }



    /**
     * cMapCount是用户曝光过的帖子的大类计数，用作下一次曝光的限制使用， 如果用户有点击，则立刻把这个限制计数清0
     *
     * @param requestInfo
     */
    private void updateRecallTagLimit(RequestInfo requestInfo) {
        List<LastDocBean> newLastDocs = requestInfo.getNewLastDocList();
        if (CollectionUtils.isEmpty(newLastDocs)) {
            return;
        }

        Cache reason_RecallTag_Cache = cacheManager.getCache(CacheFactory.CacheName.Reason_recallTag.getValue());
        Map<String, Integer> userEv_RecallTagCount = userTjInfoUtil.getRecallTagCount(requestInfo);
        Element userEv_RecallTag_Element;
        for (LastDocBean lastDocBean : newLastDocs) {

            userEv_RecallTag_Element = reason_RecallTag_Cache.get(lastDocBean.getSimId());
            if (userEv_RecallTag_Element != null) {
                String recallTag = (String) userEv_RecallTag_Element.getObjectValue();
                if (StringUtils.isNotBlank(recallTag)) {
                    userEv_RecallTagCount.put(recallTag, 0);
                }
            }
        }
    }


    /**
     * 更新本次ev详情，待扩展
     *
     * @param requestInfo
     * @param result
     */
    public void updateEvInfo(RequestInfo requestInfo, List<Document> result) {
        try {
            UserSession userSession = getUserSession(requestInfo);
            if (CollectionUtils.isEmpty(result)) {
                return;
            }

            int videoCount = 0;
            for (Document doc : result) {
                if (docUtil.isVideo(doc)) {
                    videoCount++;
                }
            }

            List<EvInfo> evInfoList = userSession.getEvInfoList();
            boolean isPreloadRequest = false;
            long timestamp = System.currentTimeMillis();
            if (CollectionUtils.isNotEmpty(evInfoList)) {
                EvInfo lastEvInfo = evInfoList.get(evInfoList.size() - 1);
                long interval = timestamp - lastEvInfo.getTimestamp();
                if (interval < 1000L) {
                    isPreloadRequest = true;
                }
            }

            EvInfo evInfo = requestInfo.getEvInfo();
            evInfo.setTimestamp(timestamp);
            evInfo.setPreloadRequest(isPreloadRequest);
            evInfo.setVideoNum(videoCount);
            evInfo.setNw(requestInfo.getNetStatus());
            userSession.addEvInfo(requestInfo.getUserId(), evInfo);

            requestInfo.setEvInfo(null);  //情况requestInfo中的EvInfo ，EvInfo已经存在与Session中了，避免requestInfo对象回收问题


            userTjInfoUtil.updateRecallTagCount(requestInfo);
            if (requestInfo.isDebugUser()) {
                DebugUtil.log("{} userSession:{}", JsonUtil.object2jsonWithoutException(getUserSession(requestInfo)));
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("{} updateEvInfo Error:{}", requestInfo.getUserId(), StackTraceUtil.getStackTrace(e));
        }
    }

    /**
     * 更新曝光，更新用户 session 中的曝光item
     *
     * @param requestInfo
     * @param result
     */
    public void updateEvItem(RequestInfo requestInfo, List<Document> result) {
        try{
            UserSession userSession = getUserSession(requestInfo);

            userSession.updateEvQueue(requestInfo, result);
        }catch (Exception e){
            e.printStackTrace();
            logger.error("{} updateEvItem Error:{}", requestInfo.getUserId(), e);
        }


    }


    /**
     * push 返回后 请求列表次数
     * 返回 当前 pull num 并更新 缓存
     * 不符合条件 返回 -1
     * @param requestInfo requestInfo
     * @return pullNum
     */
    public int getPullNumBackFromPushAndUpdateSession(RequestInfo requestInfo){
        try{
            // 获取session 类
            UserSession userSession = getUserSession(requestInfo);
            // 返回当前 pullNum
            return userSession.checkAndUpdateLastPushBean(requestInfo);

        }catch (Exception e){
            logger.error("{} getPullNumBackFromPushAndUpdateSession Error:{}", requestInfo.getUserId(), e.toString(), e);
        }

        return -1;
    }

    /**
     * push 返回后 请求列表 开始计数 （刷新列表次数）
     * 已经开始后 不重复计数
     * @param requestInfo requestInfo
     */
    public void setBackFromPushStartingAndUpdateSession(RequestInfo requestInfo, String docId){
        try{
            // 获取session 类
            UserSession userSession = getUserSession(requestInfo);
            // 开始计数
            userSession.setLastPushBeanStarting(requestInfo, docId, "");

        }catch (Exception e){
            logger.error("{} setBackFromPushStartingAndUpdateSession docId:{}, Error:{}", requestInfo.getUserId(), docId, e.toString(), e);
        }
    }


    /**
     * deeplink 返回后 请求列表次数
     * 返回 当前 pull num 并更新 缓存
     * 不符合条件 返回 -1
     * @param requestInfo requestInfo
     * @return pullNum
     */
    public int getPullNumBackFromDeeplinkAndUpdateSession(RequestInfo requestInfo){
        try{
            // 获取session 类
            UserSession userSession = getUserSession(requestInfo);
            // 返回当前 pullNum
            return userSession.checkAndUpdateLastDeeplinkBean(requestInfo);

        }catch (Exception e){
            logger.error("{} getPullNumBackFromDeeplinkAndUpdateSession Error:{}", requestInfo.getUserId(), e.toString(), e);
        }

        return -1;
    }

    /**
     * deeplink 返回后 请求列表 开始计数 （刷新列表次数）
     * 已经开始后 不重复计数
     * @param requestInfo requestInfo
     */
    public void setBackFromDeeplinkStartingAndUpdateSession(RequestInfo requestInfo, String docId){
        try{
            // 获取session 类
            UserSession userSession = getUserSession(requestInfo);
            // 开始计数
            userSession.setLastDeeplinkBeanStarting(requestInfo, docId, "");

        }catch (Exception e){
            logger.error("{} setBackFromDeeplinkStartingAndUpdateSession docId:{}, Error:{}", requestInfo.getUserId(), docId, e.toString(), e);
        }
    }




    /**
     * 更新lastdoc的redis时间戳
     *
     * @param requestInfo
     */
    public boolean updateLastDocTimeRedisCache(RequestInfo requestInfo) {
        String uid = requestInfo.getUserId();
        List<LastDocBean> lastDocs = requestInfo.getLastDocList();

        if (CollectionUtils.isEmpty(lastDocs)) {
            return true;
        }

        boolean isDebug = requestInfo.isDebugUser();


        long timeNow = System.currentTimeMillis();

        for (LastDocBean lastDocBean : lastDocs) {
            try {
                String key = getlastDocKey(requestInfo, lastDocBean.getDocId());

                //先更新redis中的点击时间戳，如果已经存在则不更新
                boolean check = localhostJedisUtil.setNxAndExpire(LocalhostJedisUtil.db_UserClick, key, String.valueOf(timeNow), GyConstant.MilliSecond_TwoDay);

                if (check) {//更新成功表示redis中没有数据，设置过期时间
                    DebugUtil.debugLog(isDebug, "{} initNewLastDoc,write2redis key:{},timeNow:{}", uid, key, timeNow);
                }
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("updateLastDocTimeRedisCache Error:{}", e);
                return false;
            }
        }
        return true;
    }

    /**
     * 更新用户session中的pullNum
     *
     * @param requestInfo
     */
    public void updateUserSessionPullNum(RequestInfo requestInfo) {
        Cache userPullNumCache = cacheManager.getCache(CacheFactory.CacheName.UserPullNum.getValue());
        String uid = requestInfo.getUserId();

        int pullCount = getUserSessionPullNum(requestInfo);

        String operation = requestInfo.getOperation();
        //处理重置逻辑

        //如果用户切换频道，则清0
        if (changeRecomChannel(requestInfo)) {
            if (GyConstant.operation_Default.equals(operation)) {
                pullCount = 0;
            } else {
                pullCount = 1;
            }
            //快头条清0逻辑
        } else if (FlowTypeUtils.isIfengnewsGold(requestInfo)) {
            if (GyConstant.operation_Default.equals(operation) && requestInfo.getPullNum() == 0) {
                pullCount = 1;
            } else if (GyConstant.operation_PullDown.equals(operation) && requestInfo.getPullNum() == 1) {
                pullCount = 1;
            } else if (pullCount < GyConstant.pullNumLimit) {
                ++pullCount;
            }
            //头条清理逻辑
        } else {
            if (requestInfo.getPullNum() == 0) {
                //首屏精品池不走个性化，这里算作第0 屏
                if (GyConstant.operation_Default.equals(operation)) {
                    pullCount = 0;
                } else {
                    pullCount = 1;
                }
            }else {
                if (pullCount < GyConstant.pullNumLimit) {
                    ++pullCount;
                }else {
                    pullCount=1;
                }
            }
        }

        Map<String,String> userMap=null;
        Element userPullNumElement = userPullNumCache.get(uid);
        if (userPullNumElement != null) {
            userMap = ( Map<String,String>) userPullNumElement.getObjectValue();
        }

        if(userMap==null) {
            userMap = new HashMap();
        }

        if(requestInfo.isDebugUser()&&StringUtils.isBlank(userMap.get("timestamp"))){
            userMap.put("timestamp",System.currentTimeMillis()+"");
        }
        userMap.put("pullCount",pullCount+"");
        userPullNumCache.put(new Element(uid, userMap));
        DebugUtil.debugLog(requestInfo.isDebugUser(), "{} {} updateUserSession updatePullNum:{}", requestInfo.getUserId(),userMap.get("timestamp"), pullCount);
    }


    public void updateUserSessionEditorTag(RequestInfo requestInfo,String tag) {
        Cache userPullNumCache = cacheManager.getCache(CacheFactory.CacheName.UserPullNum.getValue());
        String uid = requestInfo.getUserId();

        Map<String,String> userMap=null;
        Element userPullNumElement = userPullNumCache.get(uid);
        if (userPullNumElement != null) {
            userMap = ( Map<String,String>) userPullNumElement.getObjectValue();
        }

        String tagHis="";
        if(StringUtils.isNotBlank(tag)){
            tagHis=userMap.get("marqueeHis")==null?"":userMap.get("marqueeHis");
            userMap.put("marqueeHis",tagHis+","+tag);
        }
        userPullNumCache.put(new Element(uid, userMap));
        DebugUtil.debugLog(requestInfo.isDebugUser(), "{}  updateUserSession updateSessionEdTag:{}", requestInfo.getUserId(),tagHis);
    }


    public void updateSessionShort(RequestInfo requestInfo,String key,String tag) {
        Cache userPullNumCache = cacheManager.getCache(CacheFactory.CacheName.UserPullNum.getValue());
        String uid = requestInfo.getUserId();

        Map<String,String> userMap=null;
        Element userPullNumElement = userPullNumCache.get(uid);
        if (userPullNumElement != null) {
            userMap = ( Map<String,String>) userPullNumElement.getObjectValue();
        }
        if(userMap==null){
            userMap=new HashMap();
        }
        String tagHis="";
        if(StringUtils.isNotBlank(tag)){
            tagHis=userMap.get(key)==null?"":userMap.get(key);
            if(key.equals(GyConstant.specialTitle)){
                userMap.put(key,tagHis+"##"+tag);
            }else{
                userMap.put(key,tagHis+","+tag);
            }

        }
        userPullNumCache.put(new Element(uid, userMap));
        DebugUtil.debugLog(requestInfo.isDebugUser(), "{} {} updateSessionShort key:{} tag:{}", requestInfo.getUserId(),System.currentTimeMillis(),key,tagHis);
    }

    public void updateSessionNoHis(RequestInfo requestInfo,String key,String tag) {
        Cache userPullNumCache = cacheManager.getCache(CacheFactory.CacheName.UserPullNum.getValue());
        String uid = requestInfo.getUserId();

        Map<String,String> userMap=null;
        Element userPullNumElement = userPullNumCache.get(uid);
        if (userPullNumElement != null) {
            userMap = ( Map<String,String>) userPullNumElement.getObjectValue();
        }
        if(userMap==null){
            userMap=new HashMap();
        }
        if(StringUtils.isNotBlank(tag)){
            userMap.put(key,tag);
        }
        userPullNumCache.put(new Element(uid, userMap));
        DebugUtil.debugLog(requestInfo.isDebugUser(), "{} {} updateSessionShort key:{} tag:{}", requestInfo.getUserId(),System.currentTimeMillis(),key,tag);
    }


    /**
     * @param requestInfo
     * @return
     */
    public int getUserSessionPullNum(RequestInfo requestInfo) {
        Map<String,String> userPullNum =null;
        int pullCount=0;
        try {
            Cache userPullNumCache = cacheManager.getCache(CacheFactory.CacheName.UserPullNum.getValue());
            String uid = requestInfo.getUserId();
            Element userPullNumElement = userPullNumCache.get(uid);
            if (userPullNumElement != null) {
                userPullNum = ( Map<String,String>) userPullNumElement.getObjectValue();
                pullCount=userPullNum.get("pullCount")==null?0:Integer.parseInt(userPullNum.get("pullCount"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("{} getUserSessionPullNum ERROR:{}", requestInfo.getUserId(), e);
            return 0;
        }
        return pullCount;
    }



    public String getUserSessionShort(RequestInfo requestInfo,String key) {
        Map<String,String> userMap =null;
        String tagHis="";
        try {
            Cache userPullNumCache = cacheManager.getCache(CacheFactory.CacheName.UserPullNum.getValue());
            String uid = requestInfo.getUserId();
            Element userPullNumElement = userPullNumCache.get(uid);
            if (userPullNumElement != null) {
                userMap = ( Map<String,String>) userPullNumElement.getObjectValue();
                tagHis=userMap.get(key)==null?"":userMap.get(key);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("{} getUserSessionShort key:{} ERROR:{}", requestInfo.getUserId(),key, e);
        }
        return tagHis;
    }

    /**
     * 获取用户sessionId
     * @param uid
     * @return
     */
    public String getUseSessionId(String uid) {
        if(StringUtils.isBlank(uid)){
            return RTokenGenerator.sessionIdBuilder();
        }
        Map<String, String> userSessionMap = null;
        String sessionId = null;
        try{
            Cache userSessionCache = cacheManager.getCache(CacheFactory.CacheName.UserPullNum.getValue());
            Element userSessionElement = userSessionCache.get(uid);
            if(userSessionElement!=null){
                userSessionMap = ( Map<String, String>) userSessionElement.getObjectValue();
                sessionId =userSessionMap.get("sessionId");
                if(StringUtils.isBlank(sessionId)){
                    sessionId = RTokenGenerator.sessionIdBuilder();
                    userSessionMap.put("sessionId", sessionId);
                }
            }else{
                userSessionMap = new HashMap<>();
                sessionId = RTokenGenerator.sessionIdBuilder();
                userSessionMap.put("sessionId", sessionId);
                userSessionCache.put(new Element(uid, userSessionMap));
            }
        }catch (Exception e){
            logger.error("uid:{} get sessionId err:{}", e);
        }
        return sessionId;
    }


    /**
     * 通过redis检查是否是第一次访问
     *
     * @param requestInfo
     * @return
     */
    public boolean checkIsFirstAccessByRedis(RequestInfo requestInfo) {
        String uid = requestInfo.getUserId();
        long timeNow = System.currentTimeMillis();
        boolean check = localhostJedisUtil.setnx(LocalhostJedisUtil.db_UserSessionData, uid, String.valueOf(timeNow));
        if (check) {//更新成功表示redis中没有数据，设置过期时间
            localhostJedisUtil.expire(LocalhostJedisUtil.db_UserSessionData, uid, GyConstant.Second_HalfDay);
            logger.info("{} first", requestInfo.getUserId());
        }
        return check;
    }

    /**
     * 获取用户的session
     *
     * @param requestInfo
     * @return
     */
    public UserSession getUserSession(RequestInfo requestInfo) {
        UserSession userSession = null;
        Element userActionSessionElement=null;
        try{
            Cache userActionSessionCache = cacheManager.getCache(CacheFactory.CacheName.UserSession.getValue());
            String uid = requestInfo.getUserId();
            userActionSessionElement = userActionSessionCache.get(uid);

            if (userActionSessionElement != null) {
                userSession = (UserSession) userActionSessionElement.getObjectValue();
            }
        }catch (Exception e){
            e.printStackTrace();
            logger.error("{} {},getUserSession error:{}",requestInfo.getUserId(),JsonUtil.object2jsonWithoutException((UserSession) userActionSessionElement.getObjectValue()),e);
        }

        return userSession;
    }




    /**
     * 过滤lastdoc中的三俗信息，并补全simid
     *
     * @param requestInfo
     * @param lastDocBeansBase
     */
    public void dealLastDoc(RequestInfo requestInfo, List<LastDocBean> lastDocBeansBase) {

        List<String> ids2Query = Lists.newArrayList();
        Cache docCache = cacheManager.getCache(CacheFactory.CacheName.PersonalRecomDocumentInfo.getValue());
        //不区分类型存储
        List<LastDocBean> lastDocList = Lists.newArrayList();
        try {
            for (LastDocBean lastDocBean : lastDocBeansBase) {
                String simId = lastDocBean.getSimId();
                String docid = lastDocBean.getDocId();
                String time = lastDocBean.getTime();

                if (StringUtils.isBlank(docid)) {
                    continue;
                }

                if (StringUtils.isBlank(time)) {
                    continue;
                }
                if (GyConstant.log_Click_Type_Page.equals(lastDocBean.getType())) {
                    continue;
                }


                try {
                    double timeDouble = Double.valueOf(time);
                    if (timeDouble < GyConstant.lastDocTimeLimit_other) {
                        DebugUtil.debugLog(requestInfo.isDebugUser(), "{} lastDocTimeLimit click {},{},{}", requestInfo.getUserId(), lastDocBean.getDocId(), lastDocBean.getTime(), lastDocBean.getSimId());
                        continue;
                    }

                } catch (Exception e) {
                    logger.error("{} dealLastDoc time:{}, ERROR:{}", requestInfo.getUserId(), time, e);
                    continue;
                }


                docid = docid.replace(GyConstant.IKV_Prefix, "");
                docid = docid.replace(GyConstant.IKV_Prefix_UCMS, "");

                lastDocBean.setDocId(docid);

                Document doc = docUtil.getDocByCache(docCache, docid);

                if (doc == null && StringUtils.isBlank(simId)) {
                    //客户端回传的视频docid不是我们的docid，造成后面的正反馈查询不到，所以需要我们自己查一下更新缓存
                    ids2Query.add(docid);
                }

                if (sansuFilter.titleFilter(doc)) {
                    logger.info("{} dealLastDoc filter sansu:{}, title:{}", simId, docid, doc.getTitle());
                    continue;
                }


                if (doc != null) {
                    //更新lastdoc中的c的信息
                    lastDocBean.setCList(doc.getDevCList());
                    lastDocBean.setScList(doc.getDevScList());
                    lastDocBean.setLdaTopicList(doc.getLdaTopicList());

                    if (StringUtils.isBlank(simId)) {
                        lastDocBean.setSimId(doc.getSimId());
                    }
                }
                lastDocList.add(lastDocBean);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("dealLastDoc {} ERROR:{}", requestInfo.getUserId(), e);
        }


        if (ids2Query.size() > 0) {
            TimerEntity timer = TimerEntityUtil.getInstance();
            //查询hbase,并更新cache
            timer.addStartTime("updateLastDoc");
            docUtil.updateDocCacheBatchLimit(ids2Query);
            timer.addEndTime("updateLastDoc");


            //查询habse，更新缓存后再次更新simid
            Document doc;
            for (LastDocBean lastDocBean : lastDocList) {
                String docid = lastDocBean.getDocId();
                docid = docid.replace(GyConstant.IKV_Prefix, "");
                docid = docid.replace(GyConstant.IKV_Prefix_UCMS, "");
                doc = docUtil.getDocByCache(docCache, docid);
                //simid为空则补足
                if (doc != null) {
                    if (StringUtils.isBlank(lastDocBean.getSimId())) {
                        lastDocBean.setSimId(doc.getSimId());
                    }
                    //更新lastdoc中的c的信息
                    lastDocBean.setCList(doc.getDevCList());
                    lastDocBean.setScList(doc.getDevScList());
                    lastDocBean.setLdaTopicList(doc.getLdaTopicList());
                }
            }

        }
        requestInfo.setLastDocList(lastDocList);
        if (requestInfo.isDebugUser()) {
            DebugUtil.log("{} dealLastDoc lastdoc is:{}", requestInfo.getUserId(), JsonUtil.object2jsonWithoutException(lastDocList));
        }
    }


    /**
     * 判断用户是否刚刚切换了频道
     *
     * @param requestInfo
     * @return
     */
    public boolean changeRecomChannel(RequestInfo requestInfo) {
        boolean changeRecom = false;
        List<String> recomChannelList=null;
        UserSession userSession = null;
        try{
            userSession = getUserSession(requestInfo);
            if (userSession == null) {
                changeRecom = true;
            } else {
                recomChannelList = userSession.getRecomChannel(GyConstant.changeRecomChannelHistory);
                if (CollectionUtils.isNotEmpty(recomChannelList) && recomChannelList.size() == GyConstant.changeRecomChannelHistory) {
                    String recomChannelBefore = recomChannelList.get(0);
                    String recomChannelNow = recomChannelList.get(1);

                    if (StringUtils.isNotBlank(recomChannelNow) && !recomChannelNow.equals(recomChannelBefore)) {
                        changeRecom = true;
                    } else {
                        changeRecom = false;
                    }
                } else {
                    changeRecom = true;
                }
            }
        }catch (Exception e){
            logger.error("uid:{},userSession:{} recomChannelList:{} changeRecomChannel error:{}",requestInfo.getUserId(),JsonUtil.object2jsonWithoutException(userSession),JsonUtil.object2jsonWithoutException(recomChannelList),StackTraceUtil.getStackTrace(e));
            return  false;
        }

        return changeRecom;
    }

    public boolean isUsedProid(String proid){
        try{
            if(org.apache.commons.lang3.StringUtils.isBlank(proid)){
                return false;
            }
            if (ProidType.ifengnews.getValue().equals(proid)
                    ||ProidType.ifengnewslite.getValue().equals(proid)
                    || ProidType.ifengnewssdk.getValue().equals(proid)
                    || ProidType.ifengnewsdiscovery.getValue().equals(proid)
                    || ProidType.ifengnewsgold.getValue().equals(proid)
                    || ProidType.ifengnewsvip.getValue().equals(proid)){
                return true;
            }
        }catch (Exception e){
            logger.error("isUsedProid error:{}",e);
        }
        return false;
    }

    /**
     * 最终吐出列表数据之前 按照曝光内容 更新短session 中 sc过滤概率映射
     * @param requestInfo requestInfo
     * @param documentList documentList
     */
    public void updateShortSessionFilterProbabilityMapEV(RequestInfo requestInfo, List<Document> documentList) {
        try{
            String uid = requestInfo.getUserId();
            Cache userShortSessionCache = cacheManager.getCache(CacheFactory.CacheName.UserShortSession.getValue());

            Map<String, Object> userSessionMap = null;
            //获取 短session 缓存
            Element useSessionShortElement = userShortSessionCache.get(uid);
            if (useSessionShortElement != null) {
                userSessionMap = ( Map<String, Object>) useSessionShortElement.getObjectValue();
            }
            if(MapUtils.isEmpty(userSessionMap)){
                userSessionMap = new HashMap<>();
            }
            Map<String, Double> cFilterProbabilityMap = new HashMap<>();
            Map<String, Double> scFilterProbabilityMap = new HashMap<>();
            Map<String, Double> ldaTopicFilterProbabilityMap = new HashMap<>();

            //拼装 新的屏蔽 概率
            if(CollectionUtils.isNotEmpty(documentList)){

                //初始化 增长的概率
                Map<String, Double> cIncrProbMap = new HashMap<>();
                Map<String, Double> scIncrProbMap = new HashMap<>();
                Map<String, Double> ldaTopicIncrProbMap = new HashMap<>();

                //从 docList 获取 sc 要增加的概率
                updateIncreaseProbabilityMapsFromDocList(documentList, cIncrProbMap, scIncrProbMap, ldaTopicIncrProbMap);

                //处理c分类的叠加
                if (MapUtils.isNotEmpty(cIncrProbMap)) { //当前 需增加的概率
                    cFilterProbabilityMap = (Map<String, Double>) userSessionMap.get(GyConstant.session_key_cFilterProbabilityMap);
                    cFilterProbabilityMap = mergeTagFilterProbabilityMap(cFilterProbabilityMap, cIncrProbMap);
                    //处理完 回放入map
                    userSessionMap.put(GyConstant.session_key_cFilterProbabilityMap, cFilterProbabilityMap);
                }

                //处理sc分类的叠加
                if (MapUtils.isNotEmpty(scIncrProbMap)) { //当前 需增加的概率
                    scFilterProbabilityMap = (Map<String, Double>) userSessionMap.get(GyConstant.session_key_scFilterProbabilityMap);
                    scFilterProbabilityMap = mergeTagFilterProbabilityMap(scFilterProbabilityMap, scIncrProbMap);
                    //处理完 回放入map
                    userSessionMap.put(GyConstant.session_key_scFilterProbabilityMap, scFilterProbabilityMap);
                }

                //处理lda_topic分类的叠加
                if (MapUtils.isNotEmpty(ldaTopicIncrProbMap)) { //当前 需增加的概率
                    ldaTopicFilterProbabilityMap = (Map<String, Double>) userSessionMap.get(GyConstant.session_key_ldaTopicFilterProbabilityMap);
                    ldaTopicFilterProbabilityMap = mergeTagFilterProbabilityMap(ldaTopicFilterProbabilityMap, ldaTopicIncrProbMap);
                    //处理完 回放入map
                    userSessionMap.put(GyConstant.session_key_ldaTopicFilterProbabilityMap, ldaTopicFilterProbabilityMap);
                }
            }
            //更新 缓存
            userShortSessionCache.put(new Element(uid, userSessionMap));
            DebugUtil.debugLog(requestInfo.isDebugUser(), "{} updateShortSessionFilterProbabilityMapEV, cFilterProbabilityMap:{}, scFilterProbabilityMap:{}, ldaTopicFilterProbabilityMap:{}",
                    requestInfo.getUserId(), JsonUtil.object2jsonWithoutException(cFilterProbabilityMap), JsonUtil.object2jsonWithoutException(scFilterProbabilityMap), JsonUtil.object2jsonWithoutException(ldaTopicFilterProbabilityMap));
        } catch (Exception e) {
            logger.error("{} updateShortSessionFilterProbabilityMapEV Error:{}", requestInfo.getUserId(), e.toString(), e);
        }
    }


    /**
     * 通用方法 合并 历史概率map 与 新增长概率map 并返回
     * 历史概率第一步先衰减 第二步再叠加
     * @param originProbabilityMap originProbabilityMap
     * @param increaseProbabilityMap increaseProbabilityMap
     * @return Map
     */
    private Map<String, Double> mergeTagFilterProbabilityMap(Map<String, Double> originProbabilityMap, Map<String, Double> increaseProbabilityMap){
        //初始化 结果
        Map<String, Double> finalTagFilterProbabilityMap = new HashMap<>();

        //更新 概率表
        if (originProbabilityMap == null || originProbabilityMap.size() == 0){
            if (increaseProbabilityMap == null || increaseProbabilityMap.size() == 0){
                return finalTagFilterProbabilityMap; //原始与增量都为空 直接返回空map
            }
            finalTagFilterProbabilityMap = increaseProbabilityMap; //历史为空 直接用 增加的概率映射 即可
        }else{
            finalTagFilterProbabilityMap = originProbabilityMap;
            if (increaseProbabilityMap == null || increaseProbabilityMap.size() == 0){
                return finalTagFilterProbabilityMap; //增量为空 直接返回 原始map
            }
            //第一步 历史不为空 先衰减之前的概率
            for(Map.Entry<String, Double> entry : finalTagFilterProbabilityMap.entrySet()){
                String tagString = entry.getKey(); //获取 tag 分类
                double preProbability = entry.getValue(); //获取对应增加的概率
                if (!increaseProbabilityMap.containsKey(tagString)){ //只针对当前页面没有曝光的tag进行衰减   当前页面曝光 不衰减
                    double newProbability = preProbability - 0.1; //百分之十 概率衰减
                    if (newProbability < 0){
                        newProbability = 0;
                    }
                    if (newProbability > 1.2){
                        newProbability = 1.2; //概率超过 1.2 设置边界
                    }
                    newProbability = (double) Math.round(newProbability * 100) / 100; //取小数点后2位
                    finalTagFilterProbabilityMap.put(tagString, newProbability);
                }
            }

            //第二步 增加概率 与 历史叠加
            for(Map.Entry<String, Double> entry : increaseProbabilityMap.entrySet()){
                String tagString = entry.getKey(); //获取sc分类
                Double increaseProbability = entry.getValue(); //获取对应增加的概率
                //叠加概率  double 数值计算 精度容易不准确  做四舍五入 处理 保留小数点后2位
                finalTagFilterProbabilityMap.put(tagString, finalTagFilterProbabilityMap.get(tagString) == null ? increaseProbability : (double) Math.round( (finalTagFilterProbabilityMap.get(tagString) + increaseProbability) * 100) / 100);
            }
        }
        //直接返回结果
        return finalTagFilterProbabilityMap;
    }




    /**
     * 判断用户短session 中曝光的item是否有点击行为，更新缓存
     *
     * @param requestInfo requestInfo
     */
    private void updateShortSessionFilterProbabilityMapClick(RequestInfo requestInfo, List<LastDocBean> newLastDocList) {
        try{

            Cache userShortSessionCache = cacheManager.getCache(CacheFactory.CacheName.UserShortSession.getValue());
            Map<String, Object> userSessionMap = null;
            //获取 短session 缓存
            Element useSessionShortElement = userShortSessionCache.get(requestInfo.getUserId());
            if (useSessionShortElement != null) {
                userSessionMap = ( Map<String, Object>) useSessionShortElement.getObjectValue();
            }
            if(MapUtils.isEmpty(userSessionMap)){
                return;
            }

            //获取 之前历史的 概率表
            Map<String, Double> cFilterProbabilityMap = (Map<String, Double>) userSessionMap.get(GyConstant.session_key_cFilterProbabilityMap);
            Map<String, Double> scFilterProbabilityMap = (Map<String, Double>) userSessionMap.get(GyConstant.session_key_scFilterProbabilityMap);
            Map<String, Double> ldaTopicFilterProbabilityMap = (Map<String, Double>) userSessionMap.get(GyConstant.session_key_ldaTopicFilterProbabilityMap);
            //历史为空 return


            //新点击的 文章 c/sc/lda_topic 集合
            Set<String> newClickCSet = new HashSet<>();
            Set<String> newClickScSet = new HashSet<>();
            Set<String> newClickLdaTopicSet = new HashSet<>();


            for (LastDocBean newLastDoc : newLastDocList) {

                //处理 c 分类
                List<String> devCList = new ArrayList<>();
                if (CollectionUtils.isEmpty(newLastDoc.getCList())){ //针对c分类丢失的情况 特殊处理
                    devCList.add("其他");
                }else{
                    devCList = newLastDoc.getCList();
                }

                //处理 sc 分类
                List<String> devScList = new ArrayList<>();
                if (CollectionUtils.isEmpty(newLastDoc.getScList())){ //针对sc分类丢失的情况 特殊处理
                    for ( String cString : devCList ) {
                        devScList.add( cString + "-其他" );
                    }
                }else{
                    devScList = newLastDoc.getScList();
                }

                //处理 lda_topic 分类
                List<String> ldaTopicList = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(newLastDoc.getLdaTopicList())) {
                    ldaTopicList = newLastDoc.getLdaTopicList();
                }


                //拼装 已点击文章的 c/sc/lda_topic 集合
                newClickCSet.addAll(devCList);
                newClickScSet.addAll(devScList);
                newClickLdaTopicSet.addAll(ldaTopicList);
            }

            //遍历 已点击文章的 c 集合 将 cFilterProbabilityMap 对应的 key  (概率 降为 0) 直接删除 会有并发问题报错
            if (cFilterProbabilityMap != null) {
                for ( String cString : newClickCSet ) {
                    cFilterProbabilityMap.put(cString, 0.0001D);
                }
            }

            //遍历 已点击文章的 sc 集合 将 scFilterProbabilityMap 对应的 key  (概率 降为 0) 直接删除 会有并发问题报错
            if (scFilterProbabilityMap != null) {
                for ( String scString : newClickScSet ) {
                    scFilterProbabilityMap.put(scString, 0.0001D);
                }
            }

            //遍历 已点击文章的 lda_topic 集合 将 ldaTopicFilterProbabilityMap 对应的 key  (概率 降为 0) 直接删除 会有并发问题报错
            if (ldaTopicFilterProbabilityMap != null) {
                for ( String ldaTopicString : newClickLdaTopicSet ) {
                    ldaTopicFilterProbabilityMap.put(ldaTopicString, 0.0001D);
                }
            }


            DebugUtil.debugLog(requestInfo.isDebugUser(), "{} updateShortSessionFilterProbabilityMapClick hasRemoved newClickCSet:{}, cFilterProbabilityMap:{}",
                    requestInfo.getUserId(), JsonUtil.object2jsonWithoutException(newClickCSet), JsonUtil.object2jsonWithoutException(cFilterProbabilityMap));
            DebugUtil.debugLog(requestInfo.isDebugUser(), "{} updateShortSessionFilterProbabilityMapClick hasRemoved newClickScSet:{}, scFilterProbabilityMap:{}",
                    requestInfo.getUserId(), JsonUtil.object2jsonWithoutException(newClickScSet), JsonUtil.object2jsonWithoutException(scFilterProbabilityMap));
            DebugUtil.debugLog(requestInfo.isDebugUser(), "{} updateShortSessionFilterProbabilityMapClick hasRemoved newClickLdaTopicSet:{}, ldaTopicFilterProbabilityMap:{}",
                    requestInfo.getUserId(), JsonUtil.object2jsonWithoutException(newClickLdaTopicSet), JsonUtil.object2jsonWithoutException(ldaTopicFilterProbabilityMap));

        }catch (Exception e){
            logger.error("uid:{} updateShortSessionFilterProbabilityMapClick err:{}", requestInfo.getUserId(), e.toString(), e);
        }
    }




    /**
     * 从短 session 获取曝光点击信息 判断是否 需要有 tag 分类需要屏蔽 过滤
     * @param requestInfo requestInfo
     * @param filteredCSet filteredCSet
     * @param filteredScSet filteredScSet
     * @param filteredLdaTopicSet filteredLdaTopicSet
     */
    public void updateFilteredTagSetsFromShortSessionFilterProbabilityMapChecking(RequestInfo requestInfo, Set<String> filteredCSet, Set<String> filteredScSet, Set<String> filteredLdaTopicSet){
        try{
            Cache userShortSessionCache = cacheManager.getCache(CacheFactory.CacheName.UserShortSession.getValue());
            Map<String, Object> userSessionMap = null;
            //获取 短session 缓存
            Element useSessionShortElement = userShortSessionCache.get(requestInfo.getUserId());
            if (useSessionShortElement != null) {
                userSessionMap = ( Map<String, Object>) useSessionShortElement.getObjectValue();
            }
            // session 为空 return
            if(MapUtils.isEmpty(userSessionMap)){
                return;
            }
            //获取 之前历史的 概率表
            Map<String, Double> cFilterProbabilityMap = (Map<String, Double>) userSessionMap.get(GyConstant.session_key_cFilterProbabilityMap);
            Map<String, Double> scFilterProbabilityMap = (Map<String, Double>) userSessionMap.get(GyConstant.session_key_scFilterProbabilityMap);
            Map<String, Double> ldaTopicFilterProbabilityMap = (Map<String, Double>) userSessionMap.get(GyConstant.session_key_ldaTopicFilterProbabilityMap);
            // 处理 c 分类 历史为空 不处理
            if (cFilterProbabilityMap != null) {
                Map<String, Double> newCFilterProbabilityMap = new HashMap<>(cFilterProbabilityMap); //避免数组越界问题
                //按概率 降序排序
                Map<String, Double> cFilterProbabilityMapSorted = new LinkedHashMap<>();
                newCFilterProbabilityMap.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).forEachOrdered(e -> cFilterProbabilityMapSorted.put(e.getKey(), e.getValue()));

                //遍历 概率 映射
                cFilterProbabilityMapSorted.forEach((cString, probability) -> {
                    if ( filteredCSet.size() < 4 && !GyConstant.cateogry_ShiZheng.equals(cString) ){ // filteredCSet  最多可过滤4个c  且不能过滤时政新闻
                        if (Math.random() < probability){ //按 记录概率 加入过滤 集合
                            filteredCSet.add(cString);
                        }
                    }
                });

                DebugUtil.debugLog(requestInfo.isDebugUser(), "{}, updateFilteredTagSetsFromShortSessionFilterProbabilityMapChecking " +
                                "cFilterProbabilityMapSorted:{}, --> filteredCSet:{}",
                        requestInfo.getUserId(), cFilterProbabilityMapSorted, filteredCSet);
            }

            // 处理 sc 分类 历史为空 不处理
            if (scFilterProbabilityMap != null) {
                Map<String, Double> newScFilterProbabilityMap = new HashMap<>(scFilterProbabilityMap); //避免数组越界问题
                //按概率 降序排序
                Map<String, Double> scFilterProbabilityMapSorted = new LinkedHashMap<>();
                newScFilterProbabilityMap.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).forEachOrdered(e -> scFilterProbabilityMapSorted.put(e.getKey(), e.getValue()));

                //遍历 概率 映射
                scFilterProbabilityMapSorted.forEach((scString, probability) -> {
                    if ( filteredScSet.size() < 10 ){ // filteredScSet  最多可过滤10个sc
                        if (Math.random() < probability){ //按 记录概率 加入过滤 集合
                            filteredScSet.add(scString);
                        }
                    }
                });

                DebugUtil.debugLog(requestInfo.isDebugUser(), "{}, updateFilteredTagSetsFromShortSessionFilterProbabilityMapChecking " +
                                "scFilterProbabilityMapSorted:{}, --> filteredScSet:{}",
                        requestInfo.getUserId(), scFilterProbabilityMapSorted, filteredScSet);
            }

            // 处理 lda_topic 分类 历史为空 不处理
            if (ldaTopicFilterProbabilityMap != null) {
                Map<String, Double> newLdaTopicFilterProbabilityMap = new HashMap<>(ldaTopicFilterProbabilityMap); //避免数组越界问题
                //按概率 降序排序
                Map<String, Double> ldaTopicFilterProbabilityMapSorted = new LinkedHashMap<>();
                newLdaTopicFilterProbabilityMap.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).forEachOrdered(e -> ldaTopicFilterProbabilityMapSorted.put(e.getKey(), e.getValue()));

                //遍历 概率 映射
                ldaTopicFilterProbabilityMapSorted.forEach((ldaTopicString, probability) -> {
                    if ( filteredLdaTopicSet.size() < 30 ){ // filteredLdaTopicSet  最多可过滤30个lda_topic
                        if (Math.random() < probability){ //按 记录概率 加入过滤 集合
                            filteredLdaTopicSet.add(ldaTopicString);
                        }
                    }
                });

                DebugUtil.debugLog(requestInfo.isDebugUser(), "{}, updateFilteredTagSetsFromShortSessionFilterProbabilityMapChecking " +
                                "ldaTopicFilterProbabilityMapSorted:{}, --> filteredLdaTopicSet:{}",
                        requestInfo.getUserId(), ldaTopicFilterProbabilityMapSorted, filteredLdaTopicSet);
            }


        }catch (Exception e){
            logger.error("uid:{} updateFilteredTagSetsFromShortSessionFilterProbabilityMapChecking err:{}", requestInfo.getUserId(), e.toString(), e);
        }


    }




    /**
     * 根据 docList 曝光信息 分别更新 c sc lda_topic 所提升概率的 map 结构 私有方法
     * @param documentList documentList
     * @param cIncrProbMap cIncrProbMap
     * @param scIncrProbMap scIncrProbMap
     * @param ldaTopicIncrProbMap ldaTopicIncrProbMap
     */
    private void updateIncreaseProbabilityMapsFromDocList(List<Document> documentList, Map<String, Double> cIncrProbMap, Map<String, Double> scIncrProbMap, Map<String, Double> ldaTopicIncrProbMap){
        if (CollectionUtils.isNotEmpty(documentList)){
            //遍历docList
            for (Document document : documentList) {
                //判断doc 是否合法
                if (document.getDocId() != null){
                    //处理 c 分类
                    List<String> devCList = new ArrayList<>();
                    if (CollectionUtils.isEmpty((document.getDevCList()))){ //针对c分类丢失的情况 特殊处理
                        devCList.add("其他");
                    }else{
                        devCList = document.getDevCList();
                    }

                    //处理 sc 分类
                    List<String> devScList = new ArrayList<>();
                    if (CollectionUtils.isEmpty(document.getDevScList())){ //针对sc分类丢失的情况 特殊处理
                        for ( String cString : devCList ) {
                            devScList.add( cString + "-其他" );
                        }
                    }else{
                        devScList = document.getDevScList();
                    }

                    //处理 lda_topic 分类
                    List<String> ldaTopicList = new ArrayList<>();
                    if (CollectionUtils.isNotEmpty(document.getLdaTopicList())) {
                        ldaTopicList = document.getLdaTopicList();
                    }


                    //拼装更新 完成 加入 c map 结构
                    for ( String cString : devCList ) {
                        cIncrProbMap.put(cString, cIncrProbMap.get(cString) == null ? 0.2 : cIncrProbMap.get(cString) + 0.1);
                    }

                    //拼装更新 完成 加入 sc map 结构
                    for ( String scString : devScList ) {
                        scIncrProbMap.put(scString, scIncrProbMap.get(scString) == null ? 0.3 : scIncrProbMap.get(scString) + 0.2);
                    }

                    //拼装更新 完成 加入 ldaTopic map 结构
                    for ( String ldaTopic : ldaTopicList ) {
                        ldaTopicIncrProbMap.put(ldaTopic, ldaTopicIncrProbMap.get(ldaTopic) == null ? 0.5 : ldaTopicIncrProbMap.get(ldaTopic) + 0.3);
                    }
                }
            }
        }
    }




}
