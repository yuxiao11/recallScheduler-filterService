package com.ifeng.recallScheduler.request;

import com.google.common.collect.Maps;
import com.ifeng.recallScheduler.bean.LastDocBean;
import com.ifeng.recallScheduler.ctrrank.EvItem;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * 用户请求信息的封装对象
 */
@Accessors(chain = true)
@Setter
@Getter
public class MixRequestInfo {
    /**
     * 用户id
     **/
    protected String uid;

    /**
     * 增量调用召回接口时指定 召回的size大小
     */
    protected int size;

    /**
     * 请求来源
     */
    protected String flowType;

    /**
     * 判断是否是debug用户
     */
    private boolean debugUser = false;


    /**
     * 引擎内自己记录的用户上下拉次数，有清零逻辑
     */
    private int pullCount;


    /**
     * 标记当前用户的用户类型
     */
    protected Map<String, Boolean> userTypeMap = Maps.newHashMap();


    /**
     * abtest分组map，key为实验类型、value为具体标记
     */
    protected Map<String, String> abTestMap = Maps.newHashMap();

    /**
     * 开发着自己使用的临时变量的map，用来传递参数使用
     */
    protected Map<String, String> devMap = Maps.newHashMap();
    /**
     * 负反馈map，用来传递参数使用
     */
    protected Map<String, List<String>> negMaps = Maps.newHashMap();
    /**
     * 用户的点击记录
     */
    private List<LastDocBean> lastDocBeans;

    /**
     * 召回使用的lastCotag
     */
    private String lastCotag;

    /**
     * 区分头条频道流量和推荐频道流量
     * 头条值为： headline
     * 推荐值为： recom
     */
    private String recomChannel;


    /**
     * 渠道标识
     */
    protected String proid;

    /**
     * 召回id
     */
    protected String recallid;

    /**
     * 是否压缩
     */
    protected boolean compress;


    /**
     * 用户曝光队列
     */
    private List<EvItem> evItems;

    /**
     * 渠道标识
     */
    protected String publishid;

    /**
     * 添加abtest信息
     *
     * @param abtestGroup
     * @param expFlag
     */
    public void addAbtestInfo(String abtestGroup, String expFlag) {
        this.abTestMap.put(abtestGroup, expFlag);
    }

    /**
     * 检查abtestGroup 下的实验名称是否满足待判断的expFlag 情况
     *
     * @param abtestGroup
     * @param expFlag2Check
     * @return
     */
    public boolean checkAbtestInfo(String abtestGroup, String expFlag2Check) {
        if (StringUtils.isBlank(expFlag2Check)) {
            return false;
        }
        String flagNow = this.abTestMap.get(abtestGroup);
        return expFlag2Check.equals(flagNow);
    }
}
