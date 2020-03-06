package com.ifeng.recallScheduler.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by liligeng on 2018/11/27.
 * 冷启动用户正反馈点击情况
 */
@Getter
@Setter
public class EvPosFeed {

    //正反馈曝光的视频
    private List<EvPosItem> videoEv;

    //正反馈曝光的非视频
    private List<EvPosItem> docEv;

    //请求返回的时间戳
    private long timestamp;

    @Getter
    @Setter
    @AllArgsConstructor
    public class EvPosItem {
        private boolean click;
        private String simID;
        private String fromSimId;
    }

}
