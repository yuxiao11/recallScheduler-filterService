package com.ifeng.recallScheduler.enums;

/**
 * 业务线流量标记
 * Created by jibin on 2017/7/6.
 */
public enum PtypeName {
    /**
     * 头条客户端的流量
     */
    HeadLine("headline"),

    /**
     * 视频引擎流量
     */
    VideoEngine("videoEngine"),

    /**
     * 常驻push的流量
     */
    Push("push"),

    /**
     * 相关的流量
     */
    Related("Related")


    ;


    private String value;

    PtypeName(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

}
