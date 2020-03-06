package com.ifeng.recallScheduler.enums;

/**
 * 大图的特殊数据类型，jpBigPicPool和cxBigPicPool
 * Created by jibin on 2017/9/6.
 */
public enum SlideType {

    /**
     * 大图精品池
     */
    JpBigPicPool("jpBigPicPool"),

    /**
     * 长效大图精品池
     */
    CxBigPicPool("cxBigPicPool"),

    /**
     * 大图池
     */
    BigPicPool("bigpicPool");

    private String value;

    SlideType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
