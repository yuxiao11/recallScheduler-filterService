package com.ifeng.recallScheduler.constant;


/**
 * 推荐document本地缓存的相关key
 */
public enum LastDocTypeEnum {


    /**
     * 视频联播的标记
     */
    splb("splb"),


    /**
     * 普通点击的标记
     */
    click("click"),

    ;

    private String value;

    private LastDocTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
