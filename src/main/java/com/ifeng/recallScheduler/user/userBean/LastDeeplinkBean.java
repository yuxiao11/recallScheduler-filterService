package com.ifeng.recallScheduler.user.userBean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * 用户最近点击的 deeplink
 * Created by jibin on 2017/10/23.
 */
@Getter
@Setter
public class LastDeeplinkBean implements Serializable{
    private String docId;
    private String simId;
    /**
     * 观看时长
     */
    private String time;

    private String title;

    /*
        点击时间  毫秒
     */
    private Long clickTime;


    /**
     *  deeplink 返回流 第几刷
     */
    private int pullNumBackFromDeeplink;

    /**
     *  deeplink 正反馈 开关
     */
    private boolean deeplinkPositiveSwitch;


    /**
     * 客户端上报两次，对于统计日志回传的点击为page的全部不触发正反馈
     * "type":"page"
     */
    private String type;

    /**
     * 当前帖子的c的信息
     */
    @JsonIgnore
    private List<String> cList;

    //外部来源 push deeplink
    private String ref;

    //晓伟透传字段 push 打开 只走推荐流 不走潘老师 push返回运营流的用户
    private String bf;

    //数据组需要投放素材种类
    private String kind;

    public LastDeeplinkBean() {

    }

    public LastDeeplinkBean(String docId, String simId) {
        this.docId = docId;
        this.simId = simId;
    }

    @Override
    public String toString(){

        return docId+":"+simId;
    }
}
