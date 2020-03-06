package com.ifeng.recallScheduler.user;

import com.beust.jcommander.internal.Lists;

import com.ifeng.recallScheduler.apolloConf.ApolloConstant;
import com.ifeng.recallScheduler.apolloConf.ApplicationConfig;
import com.ifeng.recallScheduler.bean.LastDocBean;
import com.ifeng.recallScheduler.constant.GyConstant;
import com.ifeng.recallScheduler.constant.RecWhy;
import com.ifeng.recallScheduler.ctrrank.EvItem;
import com.ifeng.recallScheduler.item.Document;
import com.ifeng.recallScheduler.logUtil.StackTraceUtil;
import com.ifeng.recallScheduler.request.RequestInfo;
import com.ifeng.recallScheduler.user.userBean.LastDeeplinkBean;
import com.ifeng.recallScheduler.user.userBean.LastPushBean;
import com.ifeng.recallScheduler.utils.JsonUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;


/**
 * Created by jibin on 2017/11/13.
 */

public class UserSession implements Cloneable {

    private static final Logger logger = LoggerFactory.getLogger(UserSession.class);

    /**
     * 用户最近的点击记录
     */
    private List<LastDocBean> lastDocList = new CopyOnWriteArrayList<>();


    private LastPushBean lastPushBean = new LastPushBean();

    private LastDeeplinkBean lastDeeplinkBean = new LastDeeplinkBean();

    /**
     * 用户最近的ev详细信息
     */
    private List<EvInfo> evInfoList = new LinkedList<>();

    /**
     * 冷启动用户
     */
    private LinkedList<EvPosFeed> evPosFeedList;

    /**
     * 用户最近曝光的item详情,存储最近50个曝光item
     */
    private LinkedList<EvItem> evItemQueue = new LinkedList<>();

    /**
     * 区分头条频道流量和推荐频道流量,记录用户访问频道的历史
     * 头条值为： headline
     * 推荐值为： recom
     */
    private List<String> recomChannelList = new CopyOnWriteArrayList<>();

    /**
     * wifi下的session信息，初始视频数量3条
     */
    private NetStatSession session_wifi = new NetStatSession(GyConstant.videoBaseNum_wifi);
    /**
     * 3g下的session信息，初始视频数量 2条
     */
    private NetStatSession session_3g = new NetStatSession(GyConstant.videoBaseNum_3g);


    /**
     * 更新用户的频道访问信息
     *
     * @param uid
     * @param recomChannel
     */
    public void addRecomChannel(String uid, String recomChannel) {
        try {
            if (StringUtils.isNotBlank(recomChannel)) {
                this.recomChannelList.add(recomChannel);
                if (this.recomChannelList.size() > GyConstant.MaxSize_RecomChannel) {
                    this.recomChannelList.remove(0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("{} addRecomChannel Error:{}", uid, e);
        }
    }

    /**
     * 获取用户最近几条的访问频道信息
     *
     * @param num
     * @return
     */
    public List<String> getRecomChannel(int num) {
        if(recomChannelList==null){
            return new ArrayList<>();
        }
        int size = this.recomChannelList.size();
        int fromIndex = size - num;
        fromIndex = fromIndex < 0 ? 0 : fromIndex;
        //FIXME sublist modificationException
        List<String> channelList = Lists.newArrayList();
        try {
            for(int i=fromIndex;i<size;i++){
                channelList.add(this.recomChannelList.get(i));
            }
        } catch (Exception e) {
            logger.error("getRecomChannel error:",e);
        }
        return channelList;
    }


    /**
     * 更新用户的Ev访问信息信息，因为ev在requesInfo中，所以可以直接操作堆中的内容而不用重复操作session
     *
     * @param uid
     * @param evInfo
     */
    public void addEvInfo(String uid, EvInfo evInfo) {
        try {
            if (evInfo != null) {
                this.evInfoList.add(evInfo);
                if (this.evInfoList.size() > GyConstant.MaxSize_SessionEvInfo) {
                    this.evInfoList.remove(0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("{} addEvInfo Error:{}", uid, e);
        }
    }

    /**
     * @param requestInfo
     * @param documents
     */
    public void updateEvQueue(RequestInfo requestInfo, List<Document> documents) {
        EvItem item = new EvItem();
        String t = requestInfo.getRecTime();
        long ts;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
        try {
            Date date = sdf.parse(t);
            ts = date.getTime();
        } catch (ParseException e) {
            ts = System.currentTimeMillis();
            e.printStackTrace();
        }
        item.setT(ts);

        try {
            List<EvItem.EvObj> evList = new ArrayList<>();
            for (Document document : documents) {
                if (document.getWhy().equals(RecWhy.WhyJpEditorMarquee)) {
                    for (Document doc : (List<Document>) document.getMarqueeList()) {
                        EvItem.EvObj ev = item.new EvObj(doc.getDocId(), doc.getWhy(), false);
                        evList.add(ev);
                    }
                    continue;
                }

                String reasonStr = document.getReason();
                String recWhy = "";
                if (StringUtils.isNotBlank(reasonStr)) {
                    Reason reason = JsonUtil.json2ObjectWithoutException(reasonStr, Reason.class);
                    if (StringUtils.isNotBlank(reason.getReason())) {
                        recWhy = reason.getReason();
                    }
                }

                if (StringUtils.isBlank(recWhy)) {
                    recWhy = document.getWhy();
                }

                EvItem.EvObj ev = item.new EvObj(document.getDocId(), recWhy, false);
                evList.add(ev);
            }

            item.setEv(evList);

            String evItemQueueStr=StringUtils.isNotBlank(ApplicationConfig.getProperty(ApolloConstant.evItemQueueNum))?ApplicationConfig.getProperty(ApolloConstant.evItemQueueNum):"5";
            int evItemQueueSize=Integer.parseInt(evItemQueueStr);
            if (evItemQueue != null && evItemQueue.size() >= evItemQueueSize) {
                evItemQueue.pop();
            }

            if (evList != null && evList.size() > 0) {
                evItemQueue.offer(item);
            }
        } catch (Exception e) {
            logger.error("updateEvQueue error:{},uid:{}", StackTraceUtil.getStackTrace(e), requestInfo.getUserId());
        }

    }


    /**
     * 更新用户的最近点击记录
     * 先进先出淘汰，只保留最近的点击
     *
     * @param requestInfo
     */
    public void addClick(RequestInfo requestInfo) {
        List<LastDocBean> newLastDocList = requestInfo.getNewLastDocList();
        List<LastDocBean> lastDocList=this.lastDocList;
        if (CollectionUtils.isNotEmpty(newLastDocList)) {
            Set<String> simids=new HashSet<>();
            try {
                if(CollectionUtils.isNotEmpty(lastDocList)){
                    simids=lastDocList.stream().map(x->x.getSimId()).collect(Collectors.toSet());
                }
            } catch (Exception e) {
                logger.error("stream map error lastDocList :{}",lastDocList);
            }

            boolean lastPushFlag = true;
            String coldKindColl=StringUtils.isNotBlank(ApplicationConfig.getProperty(ApolloConstant.coldKindColl))?
                    ApplicationConfig.getProperty(ApolloConstant.coldKindColl):""; //冷启动用户 kind 值 （王一飞实验kind）

            for (LastDocBean lastDocBean : newLastDocList) {
                try {
                    if (lastDocBean != null && StringUtils.isNotBlank(lastDocBean.getSimId())&&!simids.contains(lastDocBean.getSimId())) {
                        //设置 入session时间  正反馈取用的时候过滤掉 很久之前的点击
                        if (null == lastDocBean.getClickTime()){
                            // 补充文章点击 时间
                            lastDocBean.setClickTime(System.currentTimeMillis());
                        }
                        this.lastDocList.add(lastDocBean);
                        if (this.lastDocList.size() > GyConstant.MaxSize_SessionLastDoc) {
                            this.lastDocList.remove(0);
                        }

                        //处理 最近一次 push 点击 记录到 session
                        if((lastPushFlag && StringUtils.isNotBlank(lastDocBean.getBf()) && GyConstant.BACK_FROM_PUSH.equals(lastDocBean.getBf())) || // 晓伟打标记
                                ( "dc07b9f37c51a6fd||82ca26d7735059a895926486608619d9366688cc||debugpushback1||868936035822432".contains(requestInfo.getUserId()) && GyConstant.REF_PUSH.equals(lastDocBean.getRef()))){ // 马迪测试
                            this.lastPushBean.setDocId(lastDocBean.getDocId());
                            this.lastPushBean.setSimId(lastDocBean.getSimId());
                            this.lastPushBean.setClickTime(lastDocBean.getClickTime());
                            this.lastPushBean.setPullNumBackFromPush(0);
                            this.lastPushBean.setPushPositiveSwitch(true);
                            lastPushFlag = false;
                        }

                        //处理 最近一次 deepLink 点击 记录到 session
                        if (StringUtils.isNotBlank(lastDocBean.getRef()) && GyConstant.REF_DEEPLINK.equals(lastDocBean.getRef()) &&
                                (StringUtils.isBlank(lastDocBean.getKind()) || !coldKindColl.contains(lastDocBean.getKind()))){ //躲开一飞冷起实验
                            //记录session 中的 deeplink 如果已有 则 忽略
                            setLastDeeplinkBeanStarting(requestInfo, lastDocBean.getDocId(), lastDocBean.getSimId());
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("{}:lastDocbeanList:{} updateClickSession Error:{}",requestInfo.getUserId(),lastDocList, StackTraceUtil.getStackTrace(e));
                }
            }
        }
    }


    /**
     * 判断是否为 push 返回流  并返回当前 pullNum
     * 不符合条件 返回 -1
     * @param requestInfo requestInfo
     * @return pullNum
     */
    public int checkAndUpdateLastPushBean(RequestInfo requestInfo){
        try{
            // 判断开关 是否打开 如果不可用 开关将关闭
            if ( GyConstant.boolean_true.equals(this.lastPushBean.isPushPositiveSwitch()) ) {
                // 时效性
                if (this.lastPushBean.getClickTime() > System.currentTimeMillis() - 1200000){
                    //获取上次 刷到第多少屏 初始为0
                    int lastPullNum = this.lastPushBean.getPullNumBackFromPush();
                    if ( lastPullNum < 3){ //前3屏 返回当前第几刷 （上次 + 1）
                        this.lastPushBean.setPullNumBackFromPush(lastPullNum + 1);
                        return lastPullNum + 1;
                    }
                }
                // 不符合条件 统一 关闭开关
                this.lastPushBean.setPushPositiveSwitch(false);
            }
        } catch (Exception e) {
            logger.error("{}: checkAndUpdateLastPushBean Error, lastPushBean: {}, Error:{}", requestInfo.getUserId(), JsonUtil.object2jsonWithoutException(this.lastPushBean), e.toString(), e);
        }

        // 不符合条件 pullNum 给 -1
        return -1;
    }

    /**
     * 判断是否为 push 返回流
     * 由于 文章点击接口及列表接口 都有可能写入 不分先后
     * upsert 开始记录列表刷新次数
     * @param requestInfo requestInfo
     */
    public void setLastPushBeanStarting(RequestInfo requestInfo, String docId, String simId){
        try{
            //docId 统一处理
            if (StringUtils.isNotBlank(docId)){
                docId = docId.replace(GyConstant.IKV_Prefix_UCMS, "");
                // 判断当前 push 流程是否已经开始 未开始则新建
                if ( !docId.equals(this.lastPushBean.getDocId() )) {
                    this.lastPushBean.setDocId(docId);
                    this.lastPushBean.setSimId(simId);
                    this.lastPushBean.setClickTime(System.currentTimeMillis());
                    this.lastPushBean.setPullNumBackFromPush(0);
                    this.lastPushBean.setPushPositiveSwitch(true);
                }
            }


        } catch (Exception e) {
            logger.error("{}: setLastPushBeanStarting Error, lastPushBean: {}, Error:{}", requestInfo.getUserId(), JsonUtil.object2jsonWithoutException(this.lastPushBean), e.toString(), e);
        }

    }



    /**
     * 判断是否为 deeplink 返回流  并返回当前 pullNum
     * 不符合条件 返回 -1
     * @param requestInfo requestInfo
     * @return pullNum
     */
    public int checkAndUpdateLastDeeplinkBean(RequestInfo requestInfo){
        try{
            // 判断开关 是否打开 如果不可用 开关将关闭
            if ( GyConstant.boolean_true.equals(this.lastDeeplinkBean.isDeeplinkPositiveSwitch()) ) {
                // 时效性 20 分钟
                if (this.lastDeeplinkBean.getClickTime() > System.currentTimeMillis() - 1200000){
                    //获取上次 刷到第多少屏 初始为0
                    int lastPullNum = this.lastDeeplinkBean.getPullNumBackFromDeeplink();
                    if ( lastPullNum < 3){ //前3屏 返回当前第几刷 （上次 + 1）
                        this.lastDeeplinkBean.setPullNumBackFromDeeplink(lastPullNum + 1);
                        return lastPullNum + 1;
                    }
                }
                // 不符合条件 统一 关闭开关
                this.lastDeeplinkBean.setDeeplinkPositiveSwitch(false);
            }
        } catch (Exception e) {
            logger.error("{}: checkAndUpdateLastDeeplinkBean Error, lastDeeplinkBean: {}, Error:{}", requestInfo.getUserId(), JsonUtil.object2jsonWithoutException(this.lastDeeplinkBean), e.toString(), e);
        }

        // 不符合条件 pullNum 给 -1
        return -1;
    }


    /**
     * 判断是否为 deeplink 返回流
     * 由于 文章点击接口及列表接口 都有可能写入 不分先后
     * upsert 开始记录列表刷新次数
     * @param requestInfo requestInfo
     */
    public void setLastDeeplinkBeanStarting(RequestInfo requestInfo, String docId, String simId){
        try{
            //docId 统一处理
            if (StringUtils.isNotBlank(docId)){
                docId = docId.replace(GyConstant.IKV_Prefix_UCMS, "");
                // 判断当前deeplink流程是否已经开始 未开始则新建
                if ( !docId.equals(this.lastDeeplinkBean.getDocId() )) {
                    this.lastDeeplinkBean.setDocId(docId);
                    this.lastDeeplinkBean.setSimId(simId);
                    this.lastDeeplinkBean.setClickTime(System.currentTimeMillis());
                    this.lastDeeplinkBean.setPullNumBackFromDeeplink(0);
                    this.lastDeeplinkBean.setDeeplinkPositiveSwitch(true);
                }
            }


        } catch (Exception e) {
            logger.error("{}: setLastDeeplinkBeanStarting Error, lastDeeplinkBean: {}, Error:{}", requestInfo.getUserId(), JsonUtil.object2jsonWithoutException(this.lastDeeplinkBean), e.toString(), e);
        }

    }




    /**
     * 获取 deeplink docId
     * 不符合条件 则返回空
     * @param requestInfo requestInfo
     * @return docId
     */
    public String checkAndGetLastDeeplinkDocId(RequestInfo requestInfo){
        String docId = "";
        // 判断开关 是否打开 如果不可用 开关将关闭
        if ( GyConstant.boolean_true.equals(this.lastDeeplinkBean.isDeeplinkPositiveSwitch()) ) {
            docId = this.lastDeeplinkBean.getDocId();
        }
        return docId;
    }

    /**
     * 根据调用方需求，获取指定的最近的lastdoc，0最老， 末尾最新
     *
     * @param num
     * @return
     */
    public List<LastDocBean> getLastDocListByNum(int num) {
        int size = lastDocList.size();
        int fromIndex = (size - num);
        if (fromIndex < 0) {
            fromIndex = 0;
        }
        //深拷贝，因lastDocList会被Modify
        List<LastDocBean> lastDocListByNum = lastDocList.subList(fromIndex, size);
        return lastDocListByNum;
    }


    /**
     * 获取session中冷启动信息
     * @return
     */
    public LinkedList<EvPosFeed> getEvPosFeedList(){
        if(this.evPosFeedList ==null){
            this.evPosFeedList = new LinkedList<>();
        }
        return this.evPosFeedList;
    }

    /**
     * 更新session中的冷启动记录信息
     * @param evPosFeed
     */
    public void addColdStartInfo(EvPosFeed evPosFeed){
        if(this.evPosFeedList ==null){
            this.evPosFeedList = new LinkedList<>();
        }
        this.evPosFeedList.offer(evPosFeed);
    }


    public List<EvInfo> getEvInfoListByNum(int num) {
        int size = this.evInfoList.size();
        int fromIndex = size - num;
        fromIndex = fromIndex < 0 ? 0 : fromIndex;
        List<EvInfo> evInfoList = this.evInfoList.subList(fromIndex, size);
        return evInfoList;
    }

    public void setLastDocList(List<LastDocBean> lastDocList) {
        this.lastDocList = lastDocList;
    }

    public List<EvInfo> getEvInfoList() {
        return evInfoList;
    }

    public List<EvItem> getEvItemList() {
        return this.evItemQueue;
    }

    public void setEvInfoList(List<EvInfo> evInfoList) {
        this.evInfoList = evInfoList;
    }

    @Deprecated
    public List<LastDocBean> getLastDocList() {
        return lastDocList;
    }

    public List<String> getRecomChannelList() {
        return recomChannelList;
    }

    public void setRecomChannelList(List<String> recomChannelList) {
        this.recomChannelList = recomChannelList;
    }

    public NetStatSession getSession_wifi() {
        return session_wifi;
    }

    public void setSession_wifi(NetStatSession session_wifi) {
        this.session_wifi = session_wifi;
    }

    public NetStatSession getSession_3g() {
        return session_3g;
    }

    public void setSession_3g(NetStatSession session_3g) {
        this.session_3g = session_3g;
    }

    public LinkedList<EvItem> getEvItemQueue() {
        return evItemQueue;
    }

    public void setEvItemQueue(LinkedList<EvItem> evItemQueue) {
        this.evItemQueue = evItemQueue;
    }

    public Object clone()  {
        try {
            return super.clone();
        }catch (CloneNotSupportedException exception){
            logger.error("UserSession clone err:{}", exception);
        }
        return null;
    }
}
