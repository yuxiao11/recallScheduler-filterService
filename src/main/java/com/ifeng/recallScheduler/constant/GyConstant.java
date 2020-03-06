package com.ifeng.recallScheduler.constant;

import com.beust.jcommander.internal.Sets;
import com.ifeng.recallScheduler.user.RecordInfo;
import com.ifeng.recallScheduler.utils.DateUtils;
import com.ifeng.recallScheduler.utils.IPUtil;
import com.ifeng.recallScheduler.utils.PathUtils;

import javax.validation.Payload;
import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by jibin on 2017/6/23.
 */
public class GyConstant {


    /**
     * 线上模式会进行正常初始化，
     * 如果是debug模式启动 值为false，则为关闭状态，不会初始化非必要环节
     */
    public static volatile boolean online_switch_init = true;


    /**
     * 画像统计信息
     * 比较器，按照Weight排序
     */
    public static final Comparator<RecordInfo> recordInfoComparator = new Comparator<RecordInfo>() {
        @Override
        public int compare(RecordInfo re1, RecordInfo re2) {


            // 按照rank=Q倒序排序
            final double rank1 = re1.getWeight();
            final double rank2 = re2.getWeight();
            return rank1 == rank2 ? 0 : ((rank1 < rank2) ? 1 : -1);

        }
    };



    /**
     * HotBoostCtr格式，保留5位小数
     */
    public static final String HotBoostCtr_Format = "%.5f";


    public static final double VIdeoQ_ctr_threshold = 0.01;

    /**
     * 环境变量： 开发环境
     */
    public static final String CONFIG_DEVELOP = "develop";

    /**
     * 服务的系统错误返回码
     */
    public static final int SYSTEM_ERROR_EXIT_STATUS = -1;

    /**
     * 曝光率大于10000 转换率低于8%
     */
    public static final int JP_POOL_BLACK_LIST_SIZE = 50;

    /**
     * 项目的绝对路径， 如果配置文件被打到jar包里面，则使用相对路径就可以， 尽量不要使用绝对路径
     */
    public static String basePath = PathUtils.getCurrentPath(GyConstant.class);


    /**
     * 本机ip，用作系统缓存分片使用
     */
    public final static String linuxLocalIp = IPUtil.getLinuxLocalIp();

    /**
     * 符号 空格
     */
    public static final String Symb_blank = " ";

    /**
     * 符号 冒号
     */
    public static final String Symb_Colon = ":";

    /**
     * 符号 小于号
     */
    public static final String Symb_Less = "<";

    /**
     * 符号 大于号
     */
    public static final String Symb_Greater = ">";

    /**
     * 符号 逗号
     */
    public static final String Symb_Comma = ",";

    /**
     * 符号 分号
     */
    public static final String Symb_Semicolon = ";";

    /**
     * 符号  井号
     */
    public static final String Symb_Pound = "#";

    /**
     * 中括号
     */
    public static final String Symb_Bracket_right = "]";

    /**
     * 符号 等号
     */
    public static final String Symb_equal = "=";

    /**
     * 符号 下划线
     */
    public static final String Symb_Underline = "_";

    /**
     * 符号 连字符
     */
    public static final String Symb_Hyphen = "-";

    /**
     * 符号 脱字符号 插入符号
     */
    public static final String Symb_Caret = "^";

    /**
     * 左 花括号
     */
    public static final String Symb_openBrace = "{";

    /**
     * 符号 base
     */
    public static final String Symb_base = "base";
    /**
     * 头条新闻的编辑新闻，（置顶）
     */
    public static final String HEADLINE_EDITOR = "editor";

    /**
     * 编辑横排精选样式
     */
    public static final String EDITOR_MARQUEE_TITLE = "编辑精选,default";

    /**
     * 媒体横排精选样式
     */
    public static final String SOURCE_MARQUEE_TITLE = "大家都关注";

    /**
     * 独家精选样式
     */
    public static final String EXCLUSIVE_TITLE = "凤凰独家";

    /**
     * 媒体来源前缀
     */
    public static final String key_Source = "source=";
    /**
     * 编辑精选横排ID
     */
    public static final String EDITOR_MARQUEE_IDHOLDER = "00000001";

    /**
     * 编辑横排精选位置
     */
    public static final int EDITOR_MARQUEE_POS = 5;

    /**
     * 头条新闻的bidding新闻，固定排在第四位,只有一个
     */
    public static final String HEADLINE_BIDDING = "bidding";
    /**
     * 头条新闻的大图新闻，大图不在首位不在末位，且大图之间不相邻
     */
    public static final String HEADLINE_SpecialView = "specialView";

    public static final String HEADLINE_PUSHDOC = "push";

    /**
     * push 返回 只返回 推荐流 用户标记
     */
    public static final String BACK_FROM_PUSH = "push";

    public static final String REF_PUSH = "push";

    public static final String REF_DEEPLINK = "deeplink";


    /**
     * simId的公共前缀
     */
    public static final String pre_simId = "clusterId_";


    public static final int MaxTimeOut_UpadteMixIndex = 150;


    /**
     * 通道召回熔断，限制最多  250毫秒
     */
    public static final int MaxTimeOut_Channel = 250;

    /**
     * 更新doc的最大耗时  150毫秒
     */
    public static final int MaxTimeOut_UpadteDocOnline = 150;

    /**
     * 查询画像的最大耗时  150毫秒
     */
    public static final int MaxTimeOut_UserModelOnline = 150;

    /**
     * 查询画像的最大耗时  100毫秒
     */
    public static final int MaxTimeOut_UserModelLast = 50;

    /**
     * 查询hbase更新doc的最大耗时  200毫秒
     */
    public static final int MaxTimeOut_QueryHbaseDocOnline = 200;


    /**
     * 布隆回写的耗时
     */
    public static final int MaxTimeOut_AddBf = 100;

    /**
     * 布隆回写的耗时
     */
    public static final int MaxTimeOut_ReAddBf = 200;

    /**
     * 在没有命中缓存的情况下，实时服务最多一次查询100条doc，减少耗时，尽量从cache中查询doc
     */
    public static final int MaxDocQueryNum_Online = 100;


    public static final int timeout_Online = 200;


    /**
     * #杨勇组的bid投放通道的url的key
     */
    public static final String KEY_Bid_Url = "bid_url";

    //TODO:remove
    public static final String bid_test_url = "http://172.30.161.150:9090/tc-supply-api/promote/news";

    /**
     * 编辑透传的编辑帖 视图样式的key
     */
    public static final String EditorOthers_Style_View = "view";
    /**
     * 编辑透传的编辑帖 视图样式为大图 titleimg
     */
    public static final String EditorOthers_Style_View_Titleimg = "titleimg";
    /**
     * 编辑透传的编辑帖 视图样式为大图 bigimg
     */
    public static final String EditorOthers_Style_View_Bigimg = "bigimg";


    /**
     * 操作标记：上滑
     */
    public static final String operation_PullUp = "pullUp";
    /**
     * 操作标记：下滑
     */
    public static final String operation_PullDown = "pullDown";
    /**
     * 操作标记：第一次加载，没有滑动行为，默认逻辑
     */
    public static final String operation_Default = "default";

    /**
     * 运维监控的测uid都是以 _test结尾，如果以 _test结尾，则不走布隆过滤器
     */
    public static final String UidEnd_Test = "_test";

    /**
     * ikv的分隔符
     */
    public static final String Symb_Split_IKV = "|!|";

    /**
     * ikv查询的前缀，如果是数字开头，则拼接 cmpp_的前缀
     */
    public static final String IKV_Prefix = "cmpp_";

    /**
     * ikv查询的前缀，ucms新推荐id前缀
     */
    public static final String IKV_Prefix_UCMS = "ucms_";

    /**
     * 需要添加召回原因，氛围3组，每组一条
     */
    public static final int ReasonNum_Group = 3;


    //--------------------召回原因样式相关-----------------------------------------

    /**
     * reason的样式的key
     */
    public static final String Reason_Style = "reasonStyle";

    /**
     * reason的名称的key
     */
    public static final String Reason_Name = "reasonName";

    //披露给客户端的召回原因和样式  展现样式的枚举含义（[可能在搜] 1，  [关注] 8， [精选]  3，[本地] 7，  [其他] 5）
    //披露给客户端的召回原因和样式  展现样式的枚举含义（[可能在搜] 9，  [关注] 7， [热点]  8，[本地] 7，  [其他] 8， [多少人在读] 6 ）
    /**
     * 召回原因 样式 [可能在搜] 1
     */
    public static final String Style_Reason_Knzs = "9";
    public static final String Tag_Reason_Knzs = "可能在搜";

    /**
     * 召回原因 样式 [关注] 2
     */
    public static final String Style_Reason_Ydy = "8";
    public static final String Tag_Reason_Ydy = "关注";

    /**
     * 召回原因 样式 [精选]  3
     */
    public static final String Style_Reason_JrXw = "10";
    public static final String Tag_Reason_JrXw = "精选";

    /**
     * 召回原因 样式 [热点]  3
     */
    public static final String Style_Reason_HotTag = "10";
    public static final String Tag_Reason_HotTag = "热点";


    /**
     * 召回原因 样式 [本地] 4
     */
    public static final String Style_Reason_Bd = "7";
    public static final String Tag_Reason_Bd = "本地";

//    public static final String Tag_Reason_MinVideo = "小视频";
    public static final String Tag_Reason_MinVideo = ""; //miniVideo的“小视频”标签可以不吐了，简直来，直接把原先那个标签常量置成空就可以
    /**
     * 召回原因 样式 [其他理由] 5
     */
    public static final String Style_Reason_Qt = "7";


    /**
     * 热点分发所有人群
     */
    public static final String Hot_Group_All = "all";
    /**
     * 运营人工热点标识
     */
    public static final String OperatorEvents = "OperatorEvents";


    /**
     * 重大事件只从投放出来
     */
    public static final double hot_bigEvent_score = 0.8;

    /**
     * 召回原因 样式 [正能量]
     */
    public static final String Tag_Reason_PositiveEnergyHot = "正能量";




    /**
     * 隐含标签的前缀，不能展现给用户
     */
    public static final String reason_Prefix_LT = "lt_";
    /**
     * 标签最大显示长度 4个字
     */
    public static final int reason_Length_Max = 4;

    //--------------------召回原因样式相关-----------------------------------------

    /**
     * 推荐流量 ab测默认标记
     */
    public static final String abtest_Default = "restruct";

    /**
     * 强插实验路 ab测A组标记
     */
    public static final String ABTEST_FORCEINSERT_A = "_forceInsertA";

    /**
     * 强插实验路 ab测默认标记
     */
    public static final String ABTEST_FORCEINSERT_DEFAULT = "_forceInsertDefault";


    /**
     * 阿波罗中的storm配置的命名空间
     */
    public static final String nameSpace_StormDataMerge = "stormDataMerge";

    /**
     * 阿波罗中的storm配置的命名空间下的增强召回数据配置
     */
    public static final String key_StormDataMerge_increaseRecallNum = "increaseRecallNum";


    /**
     * 推荐流大图ab测标记
     */
    public static final String abtest_BigView_Key = "specialView_key";

    /**
     * 推荐流第一屏强插
     */
    public static final String abtest_FirstScreen = "firstScreen";

    /**
     * 个性化召回原因的默认值
     */
    public static final String personal_Why_Default = "mis#unknow";


    /**
     * 短期新闻，冯晓伟使用这个字段
     */
    public static final String Why_PerfectNew = "perfectNew";
    /**
     * 长效新闻，冯晓伟使用这个字段，进行样式修改
     */
    public static final String Why_PerfectOld = "perfectOld";


    /**
     * 给冯晓伟使用，作为ext中的key
     */
    public static final String displayType = "displayType";

    /**
     * 给冯晓伟的焦点图数据的displayType=0
     */
    public static final String focusDisplayType = "0";

    /**
     * 热点焦点图替换焦点图样式
     */
    public static final String hotFocusDisplayType = "1";

    /**
     * 给冯晓伟使用，作为ext中的key  数据组 外部冷启动kind
     */
    public static final String outKind = "outKind";

    /**
     * 给冯晓伟使用，作为ext中的key  数据组 外部冷启动currentPlay 是否为当前页播放
     */
    public static final String currentPlay = "currentPlay";

    /**
     * 给冯晓伟使用，作为ext中的key  数据组 外部冷启动controlPlaySec 视频预览标签倒计时显示控制
     */
    public static final String controlPlaySec = "controlPlaySec";

    /**
     * 给冯晓伟使用，作为ext中的key  数据组 外部冷启动controlPlaySec 视频预览声音倒计时渐低控制
     */
    public static final String controlVoiceSec = "controlVoiceSec";

    /**
     * 给冯晓伟使用，作为ext中的key  数据组 外部冷启动completePlay 完整版标签显示 1 显示 0 不显示
     */
    public static final String completePlay = "completePlay";

    /**
     * guid的长度 32位 md5值
     */
    public static final int GUID_Length = 32;

    /**
     * 短期内的热门新闻的时间限制，限制在3天以内
     */
    public static final int RecentHotDateLimit = 3;

    /**
     * 大图精品池的key
     */
    public static final String Key_SlideType = "slideType";

    /**
     * 多通道实验的key
     */
    public static final String Key_Records = "records";


    /**
     * 负反馈使用的 来源
     */
    public static final String UserNegs_Src = "src";


    /**
     * 负反馈使用的数据的key
     */
    public static final String UserNegsKey_End = ":negfb";


    /**
     * 负反馈使用的c 数据的 key
     */
    public static final String UserCNegsKey_End = ":negfb_c";

    /**
     * 字符串 true
     */
    public static final String Str_true = "true";

    /**
     * 字符串 true
     */
    public static final String Str_false = "false";


    /**
     * 开关打开
     */
    public static final String Str_Switch_On = "SwitchOn2";

    /**
     * app客户端传递的lastDoc的默认值，不做处理
     */
    public static final String lastDoc_Default = ",,,";

    /**
     * 本机器ip 127.0.0.1
     */
    public static final String localhost_ip = "127.0.0.1";


    /**
     * 符号 ikv的ReadableFeatures 的分隔符
     */
    public static final String Symb_Split_ReadableFeatures = "\\|!\\|";
    /**
     * 符号 ikv的ReadableFeatures 的c 分类
     */
    public static final String Symb_C_ReadableFeatures = "c";
    /**
     * hbase中 itemf里的 wemediaLevel
     */
    public static final String Symb_wemediaLevel_ItemOther = "wemediaLevel";


    /**
     * 符号 ikv的ReadableFeatures 的et 实体词
     */
    public static final String Symb_ET_ReadableFeatures = "et";

    /**
     * 符号 ikv的ReadableFeatures 的et 实体词
     */
    public static final String Symb_SC_ReadableFeatures = "sc";

    /**
     * 编辑贴中优质或焦点图精选，存在other字段中
     */
    public static final String Flag_Editor_Top = "top_can";

    /**
     * http接口的 状态 正常
     */
    public static final String Key_Status_OK = "ok";

    /**
     * http接口的 状态 异常
     */
    public static final String Key_Status_ERROR = "error";


    /**
     * 编辑帖的页面轮播的游标过期时间,一天
     */
    public static final int EditorPageTimeout = 24 * 60 * 60;


    /**
     * 一天的秒数
     */
    public static final int Second_OneDay = 3600 * 24;


    /**
     * 2天的毫秒秒数
     */
    public static final int MilliSecond_TwoDay = 3600 * 48 * 1000;

    /**
     * 12小时的秒数
     */
    public static final int Second_HalfDay = 3600 * 12;

    /**
     * 用户点击的时效性控制，1小时限制
     */
    public static final int Millisecond_ClickLimit = 1000 * 60 * 60;

    /**
     * 低优先级的召回原因过滤使用
     */
    public static final Set<String> reasonFilter = new HashSet<String>() {
        private static final long serialVersionUID = 1L;

        {
            add(RecWhy.ReasonMix);
        }
    };


    /**
     * 马迪的uid
     */
    public static final String madiCool = "madicool";
    /**
     * 陈梦雪uid
     */
    public static final String smile = "smile";


    //--------------------ctr常用默认值相关-----------------------------------------
    /**
     * ctr得分的默认值 0.01
     */
    public static final String CTRQ_DEFAULT = "0.01";

    /**
     * VideoRatingQ得分的默认值 0.01
     */
    public static final String VideoRatingQ_DEFAULT = "0.0001";

    /**
     * 使用hotBoost来处理ctr的more值，这里用hotBoost直接乘以阈值
     */
    public static final double CTRQ_hotBoost_threshold = 0.01;


    //------------------------ctr分值-----------------------------------------
    /**
     * 置顶帖子
     */
    public static final String CTRQ_Max_FixTop = "0.995";

    /**
     * 置顶 deeplink
     */
    public static final String CTRQ_Max_Deeplink = "0.996";

    /**
     * ctr得分最大值，特殊处理一些强插帖, 正能量放在首位
     */
    public static final String CTRQ_Max_PositiveEnergyHot = "0.99";

    /**
     * ctr得分最大值，特殊处理一些强插帖, 放在首位
     */
    public static final String CTRQ_Max = "0.9";

    /**
     * 图文正反馈排序值
     */
    public static final String CTRQ_Doc_Posi = "0.898";

    /**
     * 视频正反馈排序值
     */
    public static final String CTRQ_Video_Posi = "0.895";

    /**
     * ctr得分中小值，特殊处理一些正能量
     */
    public static final String CTRQ_Normal = "0.05";

//------------------------ctr分值-----------------------------------------


    /**
     * payload字段值
     */
    public static final Field[] payloadFields = Payload.class.getDeclaredFields();
    public static final String title1 = "0";
    public static final String title2 = "1";
    public static final String title_field = "title";

    public static final String pic1 = "0";
    public static final String pic2 = "1";
    public static final String pic_field = "pic";

    /**
     * 热点事件的名称
     */
    public static final String ext_hotTag_eventName = "eventName";

    /**
     * 热点事件更新时间
     */
    public static final String ext_hotTag_eventUpdateTime = "eventUpdateTime";

    /**
     * 热点事件下有效条数
     */
    public static final String ext_hotTag_eventNum = "eventNum";
    /**
     * specialParam字段值
     */
    public static final String specialParam = "specialParam";

    /**
     * specialParam对象格式
     */
    public static final String specialParamObj = "specialParamObj";

    /**
     * 成伟透传给小伟使用 话题文章标签
     */
    public static final String label = "label";

    /**
     * 热点事件是否展示聚合 0：不展示
     */
    public static final String ext_hot_converge = "converge";
    /**
     * debuguid 标记
     */
    public static final String debugUidFlag = "debug";

    /**
     * 缓存用户最近20次的点击记录
     */
    public static final int MaxSize_SessionLastDoc = 20;

    /**
     * 缓存用户最近10次的Ev访问信息
     */
    public static final int MaxSize_SessionEvInfo = 10;

    /**
     * 缓存用户最近2次的访问信息频道信息
     */
    public static final int MaxSize_RecomChannel = 2;


    public static final String KeyClick_Pre = "cli:";


    public static final String uid_Jibin = "867305035296545";


    public static final String uid_FengXiaoWei = "6760a0bc44290cb65e03bb6a1d7f2c7ac2ebe359";


    public static final String uid_pd = "865969031431182";


    public static final String uid_ws = "82ca26d7735059a895926486608619d9366688cc";

    public static final String uid_ws_android = "355500060748050";

    /**
     * 待过滤的时间点
     */
    public static final Date date2Filter = DateUtils.strToDate("2017-07-01 01:00:00");

    //todo 确定datatype
    public static final String JPPOOL_FIRST_SCREEN_8_DATATYPE = "1";

    /**
     * 正反馈需要的lastdoc数量
     */
    public static final int lastDocNum_PositivedNeed = 5;

    /**
     * 增量merge的时候 ，正反馈需要的lastdoc数量
     */
    public static final int lastDocNum_PositivedMergeNeed = 50;

    /**
     * 增量merge的时候 ，正反馈需要的正反馈视频数量
     */
    public static final int Num_PositivedMergeNeed = 50;

    /**
     * merge时候每个lastdoc默认触发10条
     */
    public static final int recallNum_EachLastDoc_Merge_default = 5;

    /**
     * 日志标记，hbase同步的数据
     */
    public static final String logType_IndexHbase = "IndexHbase";

    public static final String logType_FirstRequest_RealTime = "RealTime";

    /**
     * 日志标记，增量同步
     */
    public static final String logType_IndexMerge = "mergePart";

    /**
     * 增反馈merge
     */
    public static final String logType_IndexPsMerge = "psMergePart";

    /**
     * 增反馈merge
     */
    public static final String logType_IndexJpMerge = "jpMergePart";

    /**
     * 调用召回接口的默认返回的结果数量
     */
    public static final int recallNumDefault = 400;

    /**
     * 实时调用召回接口请求数量
     */
    public static final int mixRecallRealTimeNum = 200;


    /**
     * key  request基本信息
     */
    public static final String Key_Session = "session";

    /**
     * key  data，待排序字段
     */
    public static final String Key_Candidate = "candidate";
    public static final String Key_Uid = "uid";
    public static final String Key_PullNum = "pullNum";
    public static final String Key_PullCount = "pullCount";


    /**
     * ctr请求中的key  request基本信息
     */
    public static final String ctrKey_Request = "request";

    /**
     * ctr请求中的key  data，待排序字段
     */
    public static final String ctrKey_Data = "data";

    /**
     * ctr_rerank前的原始ctr
     */
    public  static final String ctrOrigin = "ctrOrigin";

    /**
     *
     */
    public static final String ctrKey_EV = "ev";


    /**
     * ctr模块的模型名
     */
    public static final String FLAG_CTR_MODEL = "FLAG_CTR_MODEL";

    /**
     * ctr模块的特征名
     */
    public static final String FLAG_CTR_FEATURE = "FLAG_CTR_FEATURE";

    /**
     * 日志标记，pullNum
     */
    public static final String LogStr_PullNum = "_pullNum_";

    /**
     * 分流标记
     */
    public static final String LogStr_Size = "_size_";

    /**
     * 正反馈剩余数据补足到cache中
     */
    public static final String flag_Remain = "Remain";
    /**
     * 视频正反馈剩余数据补足到cache中
     */
    public static final String flag_VideoMerge = "VideoMerge";

    /**
     * 新闻正反馈剩余数据补足到cache中
     */
    public static final String flag_DocMerge = "DocMerge";

    /**
     * 大图开始强插的位置
     */
    public static final int specialViewStepStart = 1;

    /**
     * 快头条从第5位开始插入大图
     */
    public static final int specialViewStepStart_Gold = 4;


    public static final String loc_BeiJin = "北京";
    public static final String loc_ShangHai = "上海";
    public static final String loc_GuangDong = "广东";
    public static final String loc_ShenZhen = "深圳";

    /**
     */
    public static final Boolean boolean_true = true;
    public static final Boolean boolean_false = false;

    public static final String string_true = "true";
    public static final String string_false = "false";
    /**
     * 不需要使用session中的 lastDoc
     */
    public static final boolean notNeedSessionLastDoc = false;

    /**
     * 多样性过滤控制使用，最大值50
     */
    public static final int countNew_Max = 50;

    /**
     * 调用ctr，不需要重试
     */
    public static final boolean ctr_needRetry_false = false;

    /**
     * 普通lastdoc 低于5秒的
     */
    public static final int lastDocTimeLimit = 5;
    /**
     * 其他频道点击或者视频联播，10秒以下都过滤
     */
    public static final int lastDocTimeLimit_other = 10;


    /**
     * 凤凰热闻榜数据量限制，目前头条只cache住2万条
     */
    public static final int data_HotRecom_Limit = 10000;

    /**
     * 个性化召回的mix数据的列名
     */
    public static final String mix_ColumnName = "mix";


    /**
     * 在wifi情况下点击视频时，加权初始值为3
     */
    public static final int videoNumWifiClick = 3;

    public static final int MaxTimeOut_log = 50;

    public static final String key_sid = "sid";
    public static final String key_uid = "uid";

    public static final String c_JianKang = "健康";
    public static final String c_LiangXing = "两性";
    public static final String c_QinGan = "情感";
    public static final String c_MeiNv = "美女";
    public static final String c_YuLe = "娱乐";


    /**
     * 一点评级 1到6，  其中1,2级别质量最低，进行过滤
     */
    public static final String wemediaLevel_LV_1 = "1";
    public static final String wemediaLevel_LV_2 = "2";

    public static final String nullStr = "null";

    /**
     * 数据类型：长效等标记
     */
    public static final String cxDisType = "cxDisType";

    /**
     * 统计数据，用户地域信息
     */
    public static final String ext_log_loc = "loc";

    /**
     * 统计数据，editTag信息
     */
    public static final String ext_log_editTag = "editTag";

    /**
     * 统计数据，用户机型
     */
    public static final String ext_log_umt = "umt";

    /**
     * 统计数据，cityLevel信息
     */
    public static final String ext_city_level = "cityLevel";

    /**
     * 媒体评级 A级别
     */
    public static final String sourceLevel_A = "A";

    /**
     * 媒体评级 B级别
     */
    public static final String sourceLevel_B = "B";

    /**
     * 媒体评级 级别
     */
    public static final String sourceLevel_C = "C";

    /**
     * 媒体评级 S级别
     */
    public static final String sourceLevel_S = "S";

    /**
     * 媒体评级 E级别
     */
    public static final String sourceLevel_E = "E";


    /**
     * 打底数据查询hbase时每批的查询条数，容易超时，这个size不能太大
     */
    public static final int doc2QueryLimit = 100;

    /**
     * wxb 正能量 新闻数
     */
    public static final int positiveEnergyNum = 2;

    public static String LOGGER_MARKER = "PersonalIndexUtil";
    public static String HOT_TAG_LOGGER_MARKER = "HotTag";
    public static String LOCAL_LOGGER_MARKER = "Local";


    public static final String log_mix = "mix";
    public static final String log_Hbase = "hbase";
    public static final String log_Empty = "empty";


    public static final String HaiNan = "海南";
    public static final String HeiLongJiang = "黑龙江";


    /**
     * 地域经纬度格式，保留两位小数
     */
    public static final int Loc_NewScale = 2;
    /**
     * 本地查询的接口
     */
    public static final String GeoUrl = "http://local.segment.nlp.ifengidc.com/LocationSearchServer/locationService";


    public static final String sourceMrMdm = "名人面对面";
    public static final String sourceLyYy = "鲁豫有约";


    /**
     * 长效池标记
     */
    public static final String disType_cxpool = "cxpool";

    /**
     * 时政
     */
    public static final String cateogry_ShiZheng = "时政";

    /**
     * 调用mix接口的key
     */
    public static final String key_requestInfo = "requestInfo";


    /**
     * mix服务的域名
     */
    public static final String mix_url = "http://local.recom.ifeng.com/mixrecom/list?uid=";

    /**
     * mix服务的域名实时
     */
    public static final String mix_url_realtime = "http://local.recom.ifeng.com/realtime/recall/list?uid=";

    /**
     * mix服务的域名
     */
    public static final String mix_first_url = "http://local.recom.ifeng.com/mixrecom/recall?uid=";

    /**
     * 相关  正反馈服务的域名
     */
    public static final String related_positive_url = "http://local.recom.ifeng.com/ifeng_relate/ifeng-relate-service/relatePositiveFeed?deviceId=";


    /**
     * 查询 曝光数 服务的域名 可配置几天内(含今天) 曝光和点击
     */
//    public static final String evpv_url = "http://local.compass.data.ifengidc.com/newsPortrait/recomV2?num=5&ids=";
    public static final String evpv_url = "http://10.80.128.150:5566/item/totalEvPvOfDays?num=5&ids=";

    /**
     * 精品池最低曝光门槛儿
     */
    public static final double jpPoolEvThreshold = 10000;

    /**
     * 精品池最低转化率门槛儿
     */
    public static final double jpPoolConvertRatioThreshold = 0.05;

    /**
     * simid 的前缀
     */
    public static final String clusterId_pre = "clusterId_";

    /**
     * 正反馈前缀
     */
    public static final String itemcf = "itemcf";


    /**
     * 用户的session的key
     */
    public static final String session = "session";


    /**
     */
    public static final String listType_ip = "ip";

    /**
     * 检查布隆过滤记录
     */
    public static final String listType_checkbloom = "checkbloom";

    /**
     * 精品池cache
     */
    public static final String listType_jpPool = "jpPool";

    /**
     * 精品池编辑横排
     */
    public static final String listType_Marquee = "marquee";

    /**
     * a级 s级的媒体白名单限制用户等级  >=4
     */
    public static final double WhiteProvinceUser_Levle = 0.4;




    /**
     * 趣头条抓取 标记
     */
    public static final String qutt = "qutt";


    /**
     * 头条返回结果的格式
     */
    public static final String resultFormat_Map = "map";

    /**
     * 切非北京用户做时效性过滤的实验
     */
    public static final String needTimeFilter = "needTimeFilter";

    /**
     * 小视频过滤规则
     */
    public static final String needMinVideoFilter = "needMinVideoFilter";

    /**
     * 低质量过滤
     */
    public static final String needLowSimidFilter = "needLowSimidFilter";
    public static final String isBeiJingUserNotWxb = "isBeiJingUserNotWxb";
    public static final String isLvsWhite = "isLvsWhite";
    public static final String isJiGoutWhite = "isJiGouWhite";
    public static final String isWxb = "isWxb";

    /**
     * 标题过滤白名单，网信办除外
     */
    public static final String isTitleFilterWhiteNotWxb = "isTitleFilterWhiteNotWxb";

    /**
     * 老板分组
     */
    public static final String isDevTestUser = "isDevTestUser";

    /**
     *
     */
    public static final String isColdNotBj = "isColdNotBj";

    /**
     * 需要过滤掉 凤凰卫视内容 的用户 （网信办 以及 部分城市）
     */
    public static final String isNeedFilterIfengVideoUser = "isNeedFilterIfengVideoUser";


    /**
     *
     */
    public static final String IOS_Mark = "iphone";
    public static final String Android_Mark = "android";


    /**
     * 标题过滤的阈值
     */
    public static final String threadHold_title_filter = "threadHold_title_filter";
    /**
     * 长效过滤标记
     */
    public static final String Long_lowQuality_filter = "long_lowQuality_filter";
    /**
     *
     */
    public static final String Small_Video_Extend = "small_video_extend";

    /**
     * 第四范式合作方ext
     */
    public final static String paradigmKey = "paradigm4Recom";

    public final static String transKey = "trans";

    public static final String needQttFilter = "needQttFilter";

    public static final String isBSGUser = "isBSGUser";



    /**
     * 避免爬虫，限制pullNum上限
     */
    public static final int pullNumLimit = 50;

    /**
     * 过滤模块不需要本地过滤
     */
    public static final boolean needLocalFilter_false = false;

    /**
     * 过滤模块需要本地过滤
     */
    public static final boolean needLocalFilter_true = true;


    /**
     * ios 标记
     */
    public static final String ios = "ios";


    /**
     * ios 标记
     */
    public static final String iphone = "iphone";


    /**
     * ios 标记
     */
    public static final String ipad = "ipad";


    /**
     * 安卓标记
     */
    public static final String android = "android";


    /**
     * 获取用户最近2次的历史记录
     */
    public static final int changeRecomChannelHistory = 2;


    /**
     * 获取用户每屏多样化 每种类别下的条数
     */
    public static final int CDiversifiedFeatureNum = 2;


    /**
     * 媒体类型： 1表示个人媒体；2表示机构媒体
     */
    public static final String mediaType_personal = "1";


    /**
     * 媒体类型： 1表示个人媒体；2表示机构媒体
     */
    public static final String mediaType_organization = "2";


    /**
     * 标题字数小于5则过滤
     */
    public static final int titleLengthLimit = 5;

    /**
     * 小视频标题字数小于3则过滤
     */
    public static final int miniVideoLengthLimit = 3;


    /**
     * wifi下的初始视频条数
     */
    public static final int videoBaseNum_wifi = 3;

    /**
     * 3G下的初始视频条数
     */
    public static final int videoBaseNum_3g = 2;
    /**
     * 3g的非北京用户视频初始化条数为3条
     */
    public static final int videoBaseNum_3g_NotBeiJing = 3;


    /**
     * 用户刷新次数控制媒体过滤
     */
    public static final int pullCountLimit_3 = 3;


    /**
     * 本机缓存的目录
     */
    public static final String localCacheDir = "/data/prod/service/recom-toutiao/cache/";

    public static final String personalcacheOfpath = localCacheDir + "personalDocumentInfo.txt";

    public static final String DocIsIfengVideoCacheOfpath = "DocIsIfengVideoCache";
    public static final String WeMediaSourceCacheOfpath = "WeMediaSourceCache";
    public static final String SansuFiltercache = "SansuFiltercache";
    public static final String blackDocCache = "blackDocCache";
    public static final String coldChannelcache = "coldChannelcache";
    public static final String coldGeneralcache = "coldGeneralcache";
    public static final String coldVideocache = "coldVideocache";
    public static final String sourceCache = "sourceCache";
    /**
     * doc持久化的最大文件数
     */
    public static final int doc_txt_Num = 5;

    /**
     * doc持久化文件的基本size
     */
    public static final int doc_txt_BaseNum = 300000;

    public static final int WeMediaNum = 10000;

    /**
     * cache检测的最小值
     */
    public static final int cacheNum_Min = 5000;


    /**
     * 本地新闻来源：编辑手动添加
     */
    public static final String Local_SourceFrom = "manual";
    public static final String Local_SourceFrom_NT = "manual_NT";


    /**
     * 本地新闻 用户分类
     */
    public static final String Local_UserType_Traveler_Key = "user_type_traveler"; //差旅用户
    public static final String Local_UserType_Resident_Key = "user_type_resident"; //常驻用户
    public static final String Local_UserType_Unknown_Key = "user_type_unknown";   //未知用户

    /**
     * 本地新闻 文章分类
     */
    // 常驻人群: 社会热点 生活服务
    public static final String Local_DocType_Society_Tag = "社会热点";
    public static final String Local_DocType_Living_Tag = "生活服务";

    // 差旅人群: 人文历史 地方美食 休闲旅游
    public static final String Local_DocType_History_Tag = "人文历史";
    public static final String Local_DocType_Delicacy_Tag = "地方美食";
    public static final String Local_DocType_Tourism_Tag = "休闲旅游";





    /**
     * 算分来源：非编辑手动添加
     */
    public static final String Local_SourceFrom_ES_NT = "ES_NT";

    /**
     * 新用户的级别
     */
    public static final String ua_v_2 = "2";
    public static final String ua_v_1 = "1";

    /**
     * 负反馈降权使用，限制最多对几个大类进行降权
     */
    public static final int topic1ExploreDisLike_Limit = 2;
    public static final int topic1ExploreDisLike_Expose_Limit = 5;
    public static final double topic1ExploreDisLike_weight_limit = 0.47;

    /**
     * 负反馈降权的比例 50% 几率不出
     */
    public static final int topic1ExploreDisLike_Rate = 50;


    /**
     * 媒体名称综合，需要替换
     */
    public static final String source_zhonghe = "综合";

    /**
     * 四级热点 0.8
     */
    public static final double hotTag_score_lv4 = 0.8;

    /**
     * 五级热点 0.9
     */
    public static final double hotTag_score_lv5 = 0.9;


    public static final String key_simId="simId";
    public static final String key_docId="docId";
    public static final String key_title="title";
    public static final String key_docType="docType";
    public static final String key_others="others";
    public static final String r="r";
    public static final String rt="rt";
    public static final String strategy="strategy";

    /**
     * 统计使用的日志类型，页面打开，此次不会触发正反馈，但是会入布隆
     */
    public static final String log_Click_Type_Page="page";


    public static final Set<String> levelLimitSetLvSA= Sets.newHashSet();
    public static final Set<String> levelLimitSetLvSAB= Sets.newHashSet();
    public static final Set<String> levelLimitSetLvSABC= Sets.newHashSet();


    static {
        levelLimitSetLvSA.add(sourceLevel_S);
        levelLimitSetLvSA.add(sourceLevel_A);

        levelLimitSetLvSAB.add(sourceLevel_S);
        levelLimitSetLvSAB.add(sourceLevel_A);
        levelLimitSetLvSAB.add(sourceLevel_B);

        levelLimitSetLvSABC.add(sourceLevel_S);
        levelLimitSetLvSABC.add(sourceLevel_A);
        levelLimitSetLvSABC.add(sourceLevel_B);
        levelLimitSetLvSABC.add(sourceLevel_C);
    }
    public static final String[] LocForCold ={"河北","山西","辽宁","吉林","黑龙江","江苏","安徽","江西","山东","河南","湖北","湖南","广东","海南","四川","贵州","云南","陕西","甘肃","青海","台湾","内蒙古","广西","西藏","宁夏","新疆"};
    public static final String[] CityForCold ={"深圳","广州","东莞","汕头","珠海","佛山","潮州","惠州","揭阳","茂名","梅州","清远","韶关","阳江"};

    public static final Set<String> ChannelForColdSet= Sets.newHashSet();

    static{
        ChannelForColdSet.add("2006");
        ChannelForColdSet.add("2024");
        ChannelForColdSet.add("2043");
        ChannelForColdSet.add("2607");
        ChannelForColdSet.add("6001");
        ChannelForColdSet.add("6007");
        ChannelForColdSet.add("6010");
        ChannelForColdSet.add("6101");
        ChannelForColdSet.add("6102");
        ChannelForColdSet.add("6103");
        ChannelForColdSet.add("6104");
        ChannelForColdSet.add("6109");

        ChannelForColdSet.add("8366");
        ChannelForColdSet.add("8367");
        ChannelForColdSet.add("8368");
        ChannelForColdSet.add("8369");
        ChannelForColdSet.add("8381");
        ChannelForColdSet.add("8382");

    }

    public static final String UmtXiaoMi="xiaomi";
    public static final String UmtIPhone="iphone";
    public static final String UmtHuawei="huawei";



    //凤凰卫视内容 地理位置黑名单
    public static final Set<String> blackListOfCityForIfengVideo = new HashSet<String>(){{
        add("北京");
        add("上海");
        add("杭州");
        add("福州");
        add("重庆");
    }};

    public static final String blockedScForNegFeedback = "习近平,李克强,王岐山";


    public static final String isIfengVideo_Yes = "1"; //文章 是凤凰卫视内容


    /**
     * 地域为空的用户默认5分过滤
     */
    public static final Integer THRESHOLD_UnKnow = 5;

    /**
     * 标题过滤白名单 10分，不用过滤
     */
    public static final Integer THRESHOLD_White = 20;


    /**
     * 小视频
     */
    public static final String C_SmallVideo="小视频";


    public static final String SUCCESS = "OK";
    public static final String SET_IF_NOT_EXIST = "NX";
    public static final String SET_WITH_EXPIRE_TIME = "PX";


    /**
     * 小视频的标记
     */
    public static final String miniVideo = "miniVideo";

    /**
     * 世界杯标记
     */
    public static final String worldCup = "worldCup";

    /**
     * 热点事件的多样性控制，2小时内8条上限不重复
     */
    public static final Integer eventHistory_limit = 8;

    /**
     * 精品池 ab 测试 截取 t1 长度
     */
    public static final Integer jpPool_t1_limit = 15;

    public static final String ERROR = "error";



    public static final Set<String> editorType = Sets.newHashSet();

    static {
        editorType.add("财经");
        editorType.add("房产");
        editorType.add("科技");
        editorType.add("科学探索");
        editorType.add("时政");
        editorType.add("国际");
        editorType.add("社会");
        editorType.add("娱乐");
        editorType.add("体育");
    }

    public static final Set<String> coldCityA= Sets.newHashSet();
    public static final Set<String> coldCityB= Sets.newHashSet();

    public static final Set<String> miniTag= Sets.newHashSet();
    public static final Set<String> firstSwapTag= Sets.newHashSet();

    static {
        firstSwapTag.add("美女");
        firstSwapTag.add("女星");
        firstSwapTag.add("两性");
        firstSwapTag.add("健康");
        firstSwapTag.add("娱乐");
    }

    static {
        miniTag.add("生活");
        miniTag.add("搞笑");
        miniTag.add("美女");
        miniTag.add("萌宠");
        miniTag.add("萌宠萌娃");
        miniTag.add("娱乐");
        miniTag.add("社会");
        miniTag.add("音乐");
        miniTag.add("美食");
        miniTag.add("舞蹈");
        miniTag.add("旅游");
        miniTag.add("时尚");
        miniTag.add("汽车");
        miniTag.add("体育");
        miniTag.add("高颜值");
        miniTag.add("电影");
        miniTag.add("萌娃");
        miniTag.add("健身");
        miniTag.add("电视娱乐");
        miniTag.add("综艺");

    }

    static {
        coldCityA.add("北京");
        coldCityA.add("上海");
        coldCityA.add("广州");
        coldCityA.add("深圳");
        coldCityA.add("成都");
        coldCityA.add("杭州");
        coldCityA.add("武汉");
        coldCityA.add("重庆");
        coldCityA.add("南京");
        coldCityA.add("天津");
        coldCityA.add("苏州");
        coldCityA.add("西安");
        coldCityA.add("长沙");
        coldCityA.add("沈阳");
        coldCityA.add("青岛");
        coldCityA.add("郑州");
        coldCityA.add("大连");
        coldCityA.add("东莞");
        coldCityA.add("宁波");

        coldCityB.add("厦门");
        coldCityB.add("福州");
        coldCityB.add("无锡");
        coldCityB.add("合肥");
        coldCityB.add("昆明");
        coldCityB.add("哈尔滨");
        coldCityB.add("济南");
        coldCityB.add("佛山");
        coldCityB.add("长春");
        coldCityB.add("温州");
        coldCityB.add("石家庄");
        coldCityB.add("南宁");
        coldCityB.add("常州");
        coldCityB.add("泉州");
        coldCityB.add("南昌");
        coldCityB.add("贵阳");
        coldCityB.add("太原");
        coldCityB.add("烟台");
        coldCityB.add("嘉兴");
        coldCityB.add("南通");
        coldCityB.add("金华");
        coldCityB.add("珠海");
        coldCityB.add("惠州");
        coldCityB.add("徐州");
        coldCityB.add("海口");
        coldCityB.add("乌鲁木齐");
        coldCityB.add("绍兴");
        coldCityB.add("中山");
        coldCityB.add("台州");
        coldCityB.add("兰州");
        coldCityB.add("潍坊");
        coldCityB.add("保定");
        coldCityB.add("镇江");
        coldCityB.add("扬州");
        coldCityB.add("桂林");
        coldCityB.add("唐山");
        coldCityB.add("三亚");
        coldCityB.add("湖州");
        coldCityB.add("呼和浩特");
        coldCityB.add("廊坊");
        coldCityB.add("洛阳");
        coldCityB.add("威海");
        coldCityB.add("盐城");
        coldCityB.add("临沂");
        coldCityB.add("江门");
        coldCityB.add("汕头");
        coldCityB.add("泰州");
        coldCityB.add("漳州");
        coldCityB.add("邯郸");
        coldCityB.add("济宁");
        coldCityB.add("芜湖");
        coldCityB.add("淄博");
        coldCityB.add("银川");
        coldCityB.add("柳州");
        coldCityB.add("绵阳");
        coldCityB.add("湛江");
        coldCityB.add("鞍山");
        coldCityB.add("赣州");
        coldCityB.add("大庆");
        coldCityB.add("宜昌");
        coldCityB.add("包头");
        coldCityB.add("咸阳");
        coldCityB.add("秦皇岛");
        coldCityB.add("株洲");
        coldCityB.add("莆田");
        coldCityB.add("吉林");
        coldCityB.add("淮安");
        coldCityB.add("肇庆");
        coldCityB.add("宁德");
        coldCityB.add("衡阳");
        coldCityB.add("南平");
        coldCityB.add("连云港");
        coldCityB.add("丹东");
        coldCityB.add("丽江");
        coldCityB.add("揭阳");
        coldCityB.add("延边");
        coldCityB.add("舟山");
        coldCityB.add("九江");
        coldCityB.add("龙岩");
        coldCityB.add("沧州");
        coldCityB.add("抚顺");
        coldCityB.add("襄阳");
        coldCityB.add("上饶");
        coldCityB.add("营口");
        coldCityB.add("三明");
        coldCityB.add("蚌埠");
        coldCityB.add("丽水");
        coldCityB.add("岳阳");
        coldCityB.add("清远");
        coldCityB.add("荆州");
        coldCityB.add("泰安");
        coldCityB.add("衢州");
        coldCityB.add("盘锦");
        coldCityB.add("东营");
        coldCityB.add("南阳");
        coldCityB.add("马鞍山");
        coldCityB.add("南充");
        coldCityB.add("西宁");
        coldCityB.add("孝感");
        coldCityB.add("齐齐哈尔");
    }
    /**
     * 冷启动城市评级 A级别
     */
    public static final String coldCityLevel_A = "city_A";

    /**
     * 冷启动城市评级 B级别
     */
    public static final String coldCityLevel_B = "city_B";

    /**
     * 冷启动城市评级 C级别
     */
    public static final String coldCityLevel_C = "city_C";

   //为试探新闻加的key
    public static final String key_mixType="mixType";

    //冷启动渠道测试
    public static final String channelTest="渠道测试";

    //记录日志使用
    public static final String log_type_a = "a";
    public static final String log_type_b = "b";
    public static final String log_type_c = "c";


    public static final Set<String> isNotNeedLocFilter = new HashSet<String>(){{
        add("UserSub");
    }};


    /**
     * 登录7天以内的用户认为是 新用户
     */
    public static final int dayCount_ColdUser = 7;


    /**
     * 返回给冯小伟组使用的媒体列表，针对我的关注没有关注列表的用户
     */
    public static final int subMediaSize = 20;

    /**
     * 个性化不同维度的数据
     */
    public static final String exploreUcb = "exploreUcb";
    public static final String userSearch = "userSearch";
    public static final String lowExplore = "lowExplore";
    public static final String userSub = "userSub";

    public static final String HotFocusTag = "HotFocusTag";

    /**
     * 每次查询统计redis限制条数
     */
    public static final int TjInfo_Query_Limit = 100;
    /**
     * 是否包框标识字段
     */
    public static final String PACKAGE = "Package";

    /**
     * 低试探曝光次数上限 超过则取消其资格
     */
    public static final int Low_Expo_Limit = 100;


    /**
     * 符号
     */
    public static final String Symb_Point = ".";

    public static final String HotConverge="hot_converge";
    public static final String HotFoucing="hot_foucing";
    public static final String HotThum="hot_thumbnailpic";
    public static final String HotIp="10.80.80.146";

    public static final String ChannelHot="热点通道";
    public static final String ChannelPosi="正反馈通道";
    public static final String ChannelSub="订阅通道";
    public static final String ChannelMarquee="editorJx";

    public static final String LongTime="nt";


    public static final String listType_Local = "Local";


    public static final String search = "_search";

    public static final String sub = "_ub";

    /**
     * 10min 短session map的key
     */
    public static final String picFingerprintSet = "picFingerprintSet";
    public static final String topicList = "topicList";
    public static final String marqueeHis = "marqueeHis";
    public static final String pullExpCount = "pullExpCount";
    public static final String expXrList = "expXrList";
    public static final String expHqList = "expHqList";
    public static final String expHqFreList = "expHqFreList";
    public static final String expComPlexHqCount = "expComPlexHqCount";
    public static final String topicListNew = "topicListNew";
    public static final String pullExpCount2 = "pullExpCount2";
    public static final String pullExpCount4 = "pullExpCount4";
    public static final String pullExpCount6 = "pullExpCount6";
    public static final String pullExpCountK = "pullExpCountK";
    public static final String expLastN = "expLastN";
    public static final String expLowExpo = "expLowExpo";
    public static final String localClick = "localClick";
    public static final String localExpo = "localExpo";
    public static final String localExpoNum = "localExpoNum";
    public static final String localExpoPro = "localExpoPro";
    public static final String hotSpotNum = "hotSpotNum";
    public static final String hotLastExpo = "hotLastExpo";
    public static final String hotExpoNum = "hotExpoNum";
    public static final String hotClick = "hotClick";
    public static final String hotBasicNum = "hotBasicNum";
    public static final String hotReduceDown = "hotReduceDown";
    public static final String specialTitle = "specialTitle";//特定类型标题记录

    public static final String hotJpMark = "hotJpMark";
    public static final String hotJpClickNum = "hotJpClickNum";
    public static final String hotJpExpoDoc = "hotJpExpoDoc";
    /**
     *  15 min 短session map 的 key
     */
    public static final String session_key_evPvItemList = "evPvItemList";  //曝光及点击列表 用于多样性控制
    public static final String session_key_cFilterProbabilityMap = "cFilterProbabilityMap";  // c 过滤概率映射 用于多样性控制
    public static final String session_key_scFilterProbabilityMap = "scFilterProbabilityMap";  // sc 过滤概率映射 用于多样性控制
    public static final String session_key_ldaTopicFilterProbabilityMap = "ldaTopicFilterProbabilityMap";  // ldaTopic 过滤概率映射 用于多样性控制


    public static final String provinceHead = "provinceHead";
    public static final String cityHead = "cityHead";

    public static final String province_unknown = "未知地理位置";

    //预加载 c 分类 前缀
    public static final String preload_c_prefix = "c-";
    //预加载 sc 分类 前缀
    public static final String preload_sc_prefix = "sc-";
    //预加载 topic 分类 前缀
    public static final String preload_topic_prefix = "cc_";

    public static final Set<String> ctrTestSet= Sets.newHashSet();
    static {
        ctrTestSet.add("10.80.94.146");
    }

    /**
     * 给旭冉试探加的文章统计指标过滤
     */
    public static final String performanceStr = "tp1,tp2,tp3,tp4,tp5,tp6,tp7,tp8,tp9,tp10,tp11,tp12,tp13,tp14,tp15,tp16,tp17,tp18,tp19,tp20,tp21,tp22,tp23,tp24,tp25,tp61,tp62,tp63,tp64,tp65,tp81,tp82,tp83,tp84,tp85,tp66,tp67,tp68,tp69,tp70";
    /**
     * 给旭冉试探加的文章统计指标过滤(new)
     */
    public static final String performanceStrNew = "tp1,tp2,tp3,tp4,tp5,tp6,tp7,tp8,tp9,tp10,tp11,tp12,tp13,tp14,tp15,tp16,tp17,tp18,tp19,tp20,tp21,tp22,tp23,tp24,tp25tp26,tp27,tp28,tp29,tp30,tp41,tp42,tp43,tp44,tp45,tp61,tp62,tp63,tp64,tp65tp66,tp67,tp68,tp69,tp70,tp81,tp82,tp83,tp84,tp85tp86,tp87,tp88,tp89,tp90";

    /**
     * lastCotag过滤
     */
    public static final String lastCotagPerform = "tp1,tp2,tp3,tp4,tp5,tp6,tp7,tp8,tp9,tp10,tp11,tp12,tp13,tp14,tp15,tp18,tp20,tp21,tp22,tp23,tp24,tp25,tp41,tp42,tp43,tp44,tp45,tp61,tp62,tp63,tp64,tp65,tp66,tp67,tp68,tp69,tp70,tp71,tp72,tp73,tp74,tp75,tp81,tp82,tp83,tp84,tp85,tp91";

    /**
     * 用户主动行为（客户端传入）
     */
    public static final String clickreload="clickreload";//回到编辑流

    /**
     * 命中热点垂类人群类型
     */
    public static final String isSCVertical="isSCVertical";//SC二级分类命中
    public static final String isCVertical="isCVertical";//C一级分类命中

    public static final int realTimePerNum = 500;

    public static final String outSpecialView = "bigimgpreview";

}
