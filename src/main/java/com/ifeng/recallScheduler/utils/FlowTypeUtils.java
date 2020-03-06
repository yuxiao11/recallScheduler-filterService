package com.ifeng.recallScheduler.utils;


import com.ifeng.recallScheduler.constant.GyConstant;
import com.ifeng.recallScheduler.constant.RecomChannelEnum;
import com.ifeng.recallScheduler.enums.ProidType;
import com.ifeng.recallScheduler.enums.PtypeName;
import com.ifeng.recallScheduler.request.RequestInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by jibin on 2018/2/6.
 */
public class FlowTypeUtils {

    private static final Logger logger = LoggerFactory.getLogger(FlowTypeUtils.class);


    /**
     * 相关页中的视频 feed流，用于精彩视频
     *
     * @param requestInfo
     * @return
     */
    public static boolean isVideoRelatedFeed(RequestInfo requestInfo) {
        if (RecomChannelEnum.videoRelatedFeed.getValue().equals(requestInfo.getRecomChannel())) {
            return true;
        }
        return false;
    }
    /**
     * 相关中的话题判断
     *
     * @param requestInfo
     * @return
     */
    public static boolean isThemeRelatedFeed(RequestInfo requestInfo) {
        if (RecomChannelEnum.Theme.getValue().equals(requestInfo.getRecomChannel())) {
            return true;
        }
        return false;
    }

    /**
     * 相关页中的相关视频
     *
     * @param requestInfo
     * @return
     */
    public static boolean isVideoRelated(RequestInfo requestInfo) {
        if (RecomChannelEnum.videoRelated.getValue().equals(requestInfo.getRecomChannel())) {
            return true;
        }
        return false;
    }


    /**
     * 相关页中的竖版焦点图热点专题页
     *
     * @param requestInfo
     * @return
     */
    public static boolean isHotFocus(RequestInfo requestInfo) {
        if (RecomChannelEnum.HotFocus.getValue().equals(requestInfo.getRecomChannel())) {
            return true;
        }
        return false;
    }
    /**
     * 搜索页面下的 热点事件
     *
     * @param requestInfo
     * @return
     */
    public static boolean isHotFocusSearch(RequestInfo requestInfo) {
        if (RecomChannelEnum.HotFocusSearch.getValue().equals(requestInfo.getRecomChannel())) {
            return true;
        }
        return false;
    }


    /**
     * 相关页中的竖版焦点图热点详情页
     *
     * @param requestInfo
     * @return
     */
    public static boolean isHotFocusTopic(RequestInfo requestInfo) {
        if (RecomChannelEnum.HotFocusTopic.getValue().equals(requestInfo.getRecomChannel())) {
            return true;
        }
        return false;
    }



    /**
     * 判断是详情页中的流量，目前详情页和列表页逻辑不一样
     *
     * @param requestInfo
     * @return
     */
    public static boolean isRelatedFlow(RequestInfo requestInfo) {
        if (PtypeName.Related.getValue().equals(requestInfo.getPtype())) {
            return true;
        }
        return false;
    }

    public static boolean isDocRelatedFlow(RequestInfo requestInfo) {
        try{
            if (isRelatedFlow(requestInfo)&&requestInfo.getRecomChannel()!=null&&RecomChannelEnum.docRelated.getValue().equals(requestInfo.getRecomChannel())) {
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
            logger.error("{} isDocRelatedFlow error:{}",requestInfo.getUserId(),e);
        }
        return false;
    }

    /**
     * 判断是关注频道 ，这部分流量要单独处理
     *
     * @param requestInfo
     * @return
     */
    public static boolean isMomentsnew(RequestInfo requestInfo) {
        if (RecomChannelEnum.momentsnew.getValue().equals(requestInfo.getRecomChannel())) {
            return true;
        }
        return false;
    }

    /**
     * 判断是视频频道
     *
     * @param requestInfo
     * @return
     */
    public static boolean isVideoChannel(RequestInfo requestInfo) {
        if (RecomChannelEnum.videochannel.getValue().equals(requestInfo.getRecomChannel())) {
            return true;
        }
        return false;
    }


    /**
     * 判断是推荐频道
     *
     * @param requestInfo
     * @return
     */
    public static boolean isRecom(RequestInfo requestInfo) {
        if (RecomChannelEnum.recom.getValue().equals(requestInfo.getRecomChannel())) {
            return true;
        }
        return false;
    }


    /**
     * 判断是普通头条流量（包括快头条）
     *
     * @param requestInfo
     * @return
     */
    public static boolean isIfengnewsHeadLine(RequestInfo requestInfo) {
        String recomChannel = requestInfo.getRecomChannel();
        return RecomChannelEnum.headline.getValue().equals(recomChannel);
    }


    /**
     * 判断是视频app
     *
     * @param requestInfo
     * @return
     */
    public static boolean isVideoApps(RequestInfo requestInfo) {
        if (RecomChannelEnum.videoapp.getValue().equals(requestInfo.getRecomChannel())) {
            return true;
        }
        return false;
    }

    /**
     * 判断是快头条流量
     *
     * @param requestInfo
     * @return
     */
    public static boolean isIfengnewsGold(RequestInfo requestInfo) {
        if (ProidType.ifengnewsgold.getValue().equals(requestInfo.getProid())) {
            return true;
        }
        return false;
    }

    /**
     * 判断是探索版流量
     *
     * @param requestInfo
     * @return
     */
    public static boolean isIfengnewsDiscovery(RequestInfo requestInfo) {
        return  ProidType.ifengnewsdiscovery.getValue().equals(requestInfo.getProid());
    }


    /**
     * 判断是普通头条流量
     *
     * @param requestInfo
     * @return
     */
    public static boolean isIfengnewsHeadLineNotGold(RequestInfo requestInfo) {
        String recomChannel = requestInfo.getRecomChannel();
        if (ProidType.ifengnewsgold.getValue().equals(requestInfo.getProid())) {
            return false;
        }
        return RecomChannelEnum.headline.getValue().equals(recomChannel);
    }


    /**
     * 判断是否是手凤的流量
     * @param requestInfo
     * @return
     */
    public static boolean isIfengnewsH5(RequestInfo requestInfo){
        if(ProidType.ifengnewsh5.getValue().equals(requestInfo.getFrom())||ProidType.ifengnewsh5.getValue().equals(requestInfo.getProid())){
            return true;
        }
        return false;
    }


    /**
     * default是首屏
     * recomChannel=headline   是头条频道流量
     * 只有头条频道的default 第0 屏 走精品池， 推荐频道直接走个性化
     *
     * @param requestInfo
     * @return
     */
    public static boolean isJpPoolFlow(RequestInfo requestInfo) {
        boolean check = false;

        String recomChannel = requestInfo.getRecomChannel();
        int pullNum = requestInfo.getPullNum();

        if (GyConstant.operation_Default.equals(requestInfo.getOperation())) {
            //因为担心客户端传错参数，这里特意加了一个参数判空
            if (StringUtils.isBlank(recomChannel)) {
                logger.error("default recomChannel is null!!!!");
            }
            if (RecomChannelEnum.headline.getValue().equals(recomChannel) && (pullNum == 0)) {
                check = true;
            }
        }
        return check;
    }

}
