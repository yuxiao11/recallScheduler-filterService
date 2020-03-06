package com.ifeng.recallScheduler.user;

import lombok.Getter;
import lombok.Setter;

/**
 * 用户每一次访问头条的曝光情况详情
 * Created by jibin on 2017/11/16.
 */
@Getter
@Setter
public class EvInfo {

    /**
     * 本次ev 曝光的视频数量
     */
    private int videoNum;

    /**
     * 本次请求中lastDoc带有的视频数量
     */
    private int videoLastDocCount;

    /**
     * 通过kafka回写的点击视频数量
     */
    private int videoClickCountOther;

    /**
     * 时间戳
     */
    private long timestamp;

    /**
     * 是否为预载请求
     */
    private boolean preloadRequest;

    /**
     * 用户的网络状态
     */
    private String nw;
}
