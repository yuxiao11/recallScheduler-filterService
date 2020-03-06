package com.ifeng.recallScheduler.request;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.ifeng.recallScheduler.bean.LastDocBean;
import com.ifeng.recallScheduler.constant.GyConstant;
import com.ifeng.recallScheduler.ctrrank.EvItem;
import com.ifeng.recallScheduler.enums.PtypeName;
import com.ifeng.recallScheduler.user.EvInfo;
import com.ifeng.recallScheduler.user.UserModel;
import com.ifeng.recallScheduler.utils.DebugUtil;
import com.ifeng.recallScheduler.utils.StringUtil;
import org.apache.commons.lang3.StringUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 用户请求信息的封装对象
 */
@Accessors(chain = true)
@Setter
@Getter
public class RequestInfo {

    private String os;

    /**
     *
     */

    protected  String userGroup;
    /**
     * 网络状态
     **/
    protected String netStatus;
    /**
     * 操作类型：上滑、下滑
     **/
    protected String operation;
    /**
     * 用户id
     **/
    protected String userId;
    /**
     * 城市
     **/
    protected String city;

    /**
     * 实时位置
     */
    protected String loc;
    /**
     * 省份
     */
    protected String province;
    /**
     * 最后一次访问的docid
     **/
    protected String lastDoc;
    /**
     * 调用方请求的新闻总数
     **/
    protected int size;

    /**
     * 调用方请求的新闻总数原始值
     **/
    protected int oldSize;
    /**
     * 调用方请求的焦点图总数
     */
    protected int focussize;

    /**
     * 调用方请求的热点聚焦替代焦点图的总数
     */
    protected int hotsize;

    /**
     * 客户端请求对应的新闻返回数量的逻辑控制
     **/
    protected LogicParams logicParams;
    /**
     * 渠道标识
     */
    protected String proid;
    /**
     * 版本标识
     */
    protected String publishid;

    /**
     * 召回id
     */
    protected String recallid;

    /**
     * 版本号
     */
    protected String gv;


    /**
     *  客户端传 bf 参数 push 代表 点击push文章后 返回列表直接请求推荐 没有请求潘老师的push运营 数据
     *  bf( backfrom )=push
     */
    protected String backFrom;


    //------------服务填充信息----------------------------------
    /**
     * 用户画像
     **/
    protected UserModel userModel;

    /**
     * 负反馈过滤 c -> 娱乐，国际
     */
    protected Map<String, List<String>> negMaps;

    /**
     * abtest 标记
     */
    protected String abtest;

    /**
     * abtest分组map，key为实验类型、value为具体标记
     */
    protected Map<String, String> abTestMap = Maps.newHashMap();

    /**
     * 新的多通道的 abtest 标记
     */
    protected String records;

    /**
     * 是否是当天第一次访问
     */
    protected boolean isFirstAccess = false;

    /**
     * 一通到底rtoken
     */
    protected String rtoken;

    /**
     * 当前sessionId，没有session生成一个
     */
    protected String sessionId;

    /**
     * 请求来源
     */
    protected String from;

    /**
     * default 分页请求
     */
    protected int pullNum;


    /**
     * 引擎内自己记录的用户上下拉次数，有清零逻辑
     */
    private int pullCount;

    /**
     * 详情页的页码计算
     */
    private int pageNum;

    /**
     * 登录id
     */
    protected String loginid;

    /**
     * 和晓伟对接的实验分流字段
     * dataType=1       是lite版本A组，走精品池逻辑
     * dataType=0       是lite版本B组，走个性化逻辑
     */
    protected String dataType;

    /**
     * 客户端的版本测试透传字段
     */
    protected String sptype;

    //----------数据同步使用，与业务无关----------------------------
    protected String linuxLocalIp;

    /**
     * trace开关，debug时候，加入此参数会输出trace信息与业务无关
     */
    protected String devtrace;

    /**
     * 开发着自己使用的临时变量的map，用来传递参数使用
     */
    protected Map<String, String> devMap = Maps.newHashMap();

    /**
     * 判断是否是debug用户
     */
    protected boolean debugUser = false;
    /**
     * 当前要曝光的新闻的c的类别统计
     */
    protected Map<String, Integer> cMap = Maps.newHashMap();

    /**
     * 当前要曝光的新闻的图片指纹的统计
     */
    protected Set<String> picFingerprintSet = Sets.newHashSet();

    /**
     * 对用户的召回结果的recallTag进行统计，多样性过滤使用
     */
    protected Map<String, Integer> recallTagMap = Maps.newHashMap();

    /**
     * 对用户正反馈结果做多样性控制，如果用户一直不点击，则这个原始点击的simid出触发的正反馈会被限制
     */
    protected Set<String> PF_limit_originSimidSet = Sets.newHashSet();


    protected List<String> recallTagList = Lists.newArrayList();
    /**
     * 当前要曝光的新闻的et的实体词统计
     */
    protected Map<String, Integer> etMap = Maps.newHashMap();

    /**
     * 当前要曝光的新闻的source(站内外来源)统计
     */
    protected Map<String, Integer> sourceMap = Maps.newHashMap();

    /**
     * 当前要曝光的新闻的召回通道(recallAisle)统计
     */
    protected Map<String, Integer> recallAisleMap = Maps.newHashMap();

    /**
     * 当前要曝光的新闻的召回策略统计
     */
    protected Map<String, Integer> recallStrategyMap = Maps.newHashMap();

    /**
     * 当前系统时间
     */
    protected String currDate;

    /**
     * 当前系统时间
     */
    private long currTime = System.currentTimeMillis();

    /**
     * 文章推荐时间
     */
    protected String recTime;


    /**
     * 增量发送时间
     */
    private String sendTime;


    /**
     * 用户最近点击的新闻，不区分类型
     */
    protected List<LastDocBean> lastDocList;

    /**
     * 用户最新的点击的新闻，不区分类型，使用redis缓存判断是否是新点击
     */
    protected List<LastDocBean> newLastDocList;

    /**
     *
     */
    protected List<Map<String,Object>> editorList;

    /**
     * 头条自己使用的流量标记，主要用来区分push
     */
    private String ptype = PtypeName.HeadLine.getValue();

    /**
     * 增量调用召回接口时指定 召回的size大小
     */
    protected String increaseRecallNum = "400";

    /**
     * 区分头条频道流量和推荐频道流量
     * 头条值为： headline
     * 推荐值为： recom
     */
    private String recomChannel;

    /**
     * 用户当前此次访问的 ev详细信息
     */
    private EvInfo evInfo = new EvInfo();

    /**
     * 视频数量被限制
     */
    private boolean videoIsLimit = false;

    /**
     * 判断是否是ctr debug 请求
     */
    @Deprecated
    private boolean ctrDebug = false;

    /**
     * debug时候使用，指定走哪一路的ctr配置
     */
    @Deprecated
    private Map<String, String> debugCtrExpContext;

    /**
     * 累计计数，用户最近几屏没有点击视频
     */
    private int noVideoClickEvCount = 0;


    /**
     * 区分不同业务场景的请求来源标识
     */
    private String requestSource;

    /**
     * 推荐批次id  uid+时间戳
     */
    String sid;

    /**
     * 判断是否特殊用户
     */
    private String isSpecialUser;


    private String themeId;

    private String themeName;

    /**
     * 标记当前用户的用户类型
     */
    protected Map<String, Boolean> userTypeMap = Maps.newHashMap();


    private String isWhiteProvinceUser;


    private boolean isColdUser = false;

    /**
     * 非北京的冷启动用户
     */
    private boolean isColdUserNotBj = false;


    //常驻地
    private String  permanentLoc;

    //常驻省（目前只会在冷启动用户处理时候set值，其他暂无值）
    private String  permanentProvince;

    private String eventName;

    /**
     * 标记返回结果的json格式
     */
    private String resultFormat;


    /**
     * 标题敏感词过滤阈值，默认值6 过滤
     */
    int titleThreshold = GyConstant.THRESHOLD_UnKnow;

    private List<EvItem> evItems;

    /**
     * 小视频个数限制
     */
    protected Map<String, Integer> videoNumMap = Maps.newHashMap();

    /**
     * 小视频个数限制
     */
    protected Map<String, Integer> scLimitMap = Maps.newHashMap();

    /**
     * 给调用方使用的标记
     */
    private Map<String, String> flagMap = Maps.newHashMap();

    /**
     * 是否是不丰满画像
     */
    private boolean isColdFullNess=false;

    //为冷启动是否出固定位写的数
    private  int  coldRandom=100;

    //返回编辑流操作(clickreload) 将来可在此字段上配置更多其他用户行为
    private String reason;

    /**
     * 以下 三个字段为外部素材属性
     */
    private String ref;
    private String kind;
    private String refDocId;

    protected boolean compress = false;

    protected Set<String> graphCateSet;

    public RequestInfo() {

    }


    public RequestInfo(String uid) {
        this.userId = uid;
        this.rtoken = StringUtil.uuid();
    }

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

    /**
     * 检查abtestGroup 下的是否包含改实验
     *
     * @param abtestGroup
     * @return
     */
    public boolean containsAbtestInfo(String abtestGroup) {
        if (StringUtils.isBlank(abtestGroup)) {
            return false;
        }
        return this.abTestMap.containsKey(abtestGroup);
    }

    /**
     * 根据总耗时，对非必要模块进行熔断
     * @return
     */
    public boolean needChannel() {
        long cost = System.currentTimeMillis() - currTime;
        return (cost < GyConstant.MaxTimeOut_Channel);
    }



    /**
     * 根据总耗时，对非必要模块进行熔断
     * @return
     */
    public boolean needChannel( String channel) {
        long cost = System.currentTimeMillis() - currTime;
        boolean isNeed = cost < GyConstant.MaxTimeOut_Channel;

        // 记录是否 通过 熔断 校验
        DebugUtil.debugLog(DebugUtil.isDebugUser(this.userId), "uid: {}, needChannel check, cost: {}, [{}] is Needed result : {}", this.userId, cost, channel, isNeed );

        return isNeed;
    }


}
