package com.ifeng.recallScheduler.item;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**被打分过滤词滤掉的doc，在陈步伟的管理后台展示
 *
 * Created  on 2018/1/22.
 */
@Getter
@Setter
public class BanItem implements Serializable{

    private static final long serialVersionUID = 1L;


    /**  docid */
    private String id;
    /**  标题*/
    private String title;
    /** 禁推类型   这里是 2 */
    private int banType = 2;
    /** 滤掉的原因 */
    private String banStr;

    /** 业务场景来源 */
    private String bizFrom = "engine";

    public BanItem(String id, String title, String banStr) {
        this.id = id;
        this.title = title;
        this.banStr = banStr;
    }
}
