package com.ifeng.recallScheduler.utils;

import com.beust.jcommander.internal.Sets;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.ifeng.recallScheduler.apolloConf.ApolloConstant;
import com.ifeng.recallScheduler.apolloConf.ApplicationConfig;
import com.ifeng.recallScheduler.apolloConf.DebugUserConfig;
import com.ifeng.recallScheduler.apolloConf.SpecialApplicationConfig;
import com.ifeng.recallScheduler.constant.AbTestConstant;
import com.ifeng.recallScheduler.constant.GyConstant;
import com.ifeng.recallScheduler.constant.RecomChannelEnum;
import com.ifeng.recallScheduler.constant.ResultFlagConstant;
import com.ifeng.recallScheduler.enums.ProidType;
import com.ifeng.recallScheduler.expfw.ExpfwUtil;
import com.ifeng.recallScheduler.redis.jedisPool.SpecialFilterUserJedisUtil;
import com.ifeng.recallScheduler.request.RequestInfo;
import com.ifeng.recallScheduler.user.UserModel;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 * 冷启动的热门推荐缓存数据
 * Created by jibin on 2017/6/23.
 */
@Service
public class SpecialFilterUserUtil {


    protected static Logger logger = LoggerFactory.getLogger(SpecialFilterUserUtil.class);


    @Autowired
    private DebugUserConfig debugUserConfig;


    @Autowired
    private ExpfwUtil expfwUtil;


    @Autowired
    private UserActionUtil userActionUtil;


    /**
     * 特殊过滤用户的缓存数据的redis dbNum
     */
    private static final int dbNum_hot = 3;


    private static final String Key_BignessUids = "BignessUids";

    private static final String Key_NewBigness = "Bigness"; //新特殊人群

    private static final String Key_ComProduct = "ComProduct";//竞品公司


    //白名单
    private static final String Key_BignessUids_white = "BignessUids_white";

    //新白名单
    private static final String Key_newWhiteBigness = "whiteBigness";


    /**
     * 网信办用户的uid
     */
    public static volatile Cache<String, String> wxbUsercache;

    public static volatile Cache<String, String> beijinUsercache;


    private static Set<String> whiteUidSet = Sets.newHashSet();


    static {
        initWhiteUidSet();
        initCacheIds();
        loadCache();
    }


    private static void initWhiteUidSet() {
        Set<String> bignessUids_White = null;
        try {
            bignessUids_White = SpecialFilterUserJedisUtil.hKeys(dbNum_hot, Key_newWhiteBigness);
        } catch (Exception e) {
            logger.error("bignessUids_White loadCache ERROR First!!! {}", e);
        }

        if (CollectionUtils.isEmpty(bignessUids_White)) {
            try {
                bignessUids_White = SpecialFilterUserJedisUtil.hKeys(dbNum_hot, Key_newWhiteBigness);
            } catch (Exception e) {
                logger.error("bignessUids_White loadCache ERROR second!!! {}", e);
            }
        }

        if (CollectionUtils.isNotEmpty(bignessUids_White)) {
            whiteUidSet.addAll(bignessUids_White);
            logger.info("bignessUids_White size:{}", whiteUidSet.size());
        }
    }


    static void initCacheIds() {
        wxbUsercache = CacheBuilder
                .newBuilder()
                .recordStats()
                .concurrencyLevel(10)
                .expireAfterWrite(300, TimeUnit.SECONDS)
                .initialCapacity(200000)
                .maximumSize(200000)
                .build();


        beijinUsercache = CacheBuilder
                .newBuilder()
                .recordStats()
                .concurrencyLevel(10)
                .expireAfterWrite(12, TimeUnit.HOURS)
                .initialCapacity(500000)
                .maximumSize(500000)
                .build();
    }


    /**
     * 加载三俗缓存数据
     *
     * @return
     */
    public static boolean loadCache() {
        try {

            Set<String> bignessUids = new HashSet<>();
            Set<String> compareUids = new HashSet<>();
            try {
                bignessUids = SpecialFilterUserJedisUtil.hKeys(dbNum_hot, Key_NewBigness);
                compareUids = SpecialFilterUserJedisUtil.hKeys(dbNum_hot, Key_ComProduct);
            } catch (Exception e) {
                logger.error("bignessUids loadCache ERROR First!!! {}", e);
            }

            if (CollectionUtils.isEmpty(bignessUids)) {
                try {
                    bignessUids = SpecialFilterUserJedisUtil.hKeys(dbNum_hot, Key_NewBigness);
                } catch (Exception e) {
                    logger.error("wxbUsercache loadCache ERROR second!!! {}", e);
                }
            }

            if (CollectionUtils.isEmpty(compareUids)) {
                try {
                    compareUids = SpecialFilterUserJedisUtil.hKeys(dbNum_hot, Key_ComProduct);
                } catch (Exception e) {
                    logger.error("compareUids loadCache ERROR second!!! {}", e);
                }
            }

            bignessUids.addAll(compareUids);

            if (CollectionUtils.isEmpty(bignessUids)) {
                logger.error("wxbUsercache EMPTY!!! ");
                bignessUids = Sets.newHashSet();

                Map<String, String> oldMap = wxbUsercache.asMap();

                //查询内容为空时, 重刷旧cache的ttl
                bignessUids.addAll(oldMap.keySet());
            }

            for (String uid : bignessUids) {

                //白名单
                if (whiteUidSet.contains(uid)) {
                    continue;
                }
                wxbUsercache.put(uid, "1");
            }

            logger.warn("update wxbUsercache from redis allSize:{} compareUids size:{}", bignessUids.size(), compareUids.size());
        } catch (Exception e) {
            logger.error("wxbUsercache ERROR :{}", e);
        }
        return true;
    }

    /**
     * 判断是北京用户，但是排wxb
     *
     * @param requestInfo
     * @return
     */
    public boolean isBeiJingUserNotWxb(RequestInfo requestInfo) {
        //wxb走特殊通道   或者 特殊用户分到非北上广测试组
        if (isWxbUser(requestInfo) || debugUserConfig.getDebugUser(ApolloConstant.NotBSG_User_Key).contains(requestInfo.getUserId())) {
            return false;
        }
        if (requestInfo.isDebugUser()) {
            return true;
        }

        return (GyConstant.Str_true.equals(beijinUsercache.getIfPresent(requestInfo.getUserId())));
    }


    public boolean isGdAndShUser(RequestInfo requestInfo) {

        UserModel userModel = requestInfo.getUserModel();
        String generalloc = "";
        if (userModel != null) {
            generalloc = userModel.getGeneralloc();
        }
        boolean isGdSh = true;
        if (StringUtils.isNotBlank(generalloc)) {
            if (generalloc.contains(GyConstant.loc_GuangDong) || generalloc.contains(GyConstant.loc_ShangHai)) {
                isGdSh = true;
            } else {
                isGdSh = false;
            }
        }
        return isGdSh;
    }


    /**
     * 判断是北京用户或者wxb用户
     *
     * @param requestInfo
     * @return
     */
    public boolean isBeiJingUserOrWxb(RequestInfo requestInfo) {
        boolean isBeiJing = true;
        try {
            //wxb走特殊通道   或者 特殊用户分到非北上广测试组
            if (debugUserConfig.getDebugUser(ApolloConstant.NotBSG_User_Key).contains(requestInfo.getUserId())) {
                return false;
            }

            String loc = requestInfo.getPermanentLoc() + requestInfo.getLoc();

            if (StringUtils.isNotBlank(loc)) {
                //北上广用户 && wxb 都为true
                if (loc.contains(GyConstant.loc_BeiJin)
                        || GyConstant.Str_true.equals(beijinUsercache.getIfPresent(requestInfo.getUserId())) || isWxbUser(requestInfo)) {
                    isBeiJing = true;
                } else {
                    isBeiJing = false;
                }
            }
        } catch (Exception e) {
            logger.error("uid:{},isBSGUser error{} ", requestInfo.getUserId(), e);
            return isBeiJing;
        }
        return isBeiJing;
    }


    public boolean isBSGUser(RequestInfo requestInfo) {
        boolean isBSG = true;
        try {
            //wxb走特殊通道   或者 特殊用户分到非北上广测试组
            if (debugUserConfig.getDebugUser(ApolloConstant.NotBSG_User_Key).contains(requestInfo.getUserId())) {
                return false;
            }

            String loc = requestInfo.getPermanentLoc() + requestInfo.getLoc();

            logger.info("test requestInfo locInfo:{}",loc);

            if (StringUtils.isNotBlank(loc)) {
                //北上广用户 && wxb 都为true
                if (loc.contains(GyConstant.loc_GuangDong) || loc.contains(GyConstant.loc_ShangHai) || loc.contains(GyConstant.loc_BeiJin)
                        || GyConstant.Str_true.equals(beijinUsercache.getIfPresent(requestInfo.getUserId())) || isWxbUser(requestInfo)) {
                    isBSG = true;
                } else {
                    isBSG = false;
                }
            }
        } catch (Exception e) {
            logger.error("uid:{},isBSGUser error{} ", requestInfo.getUserId(), e);
            return isBSG;
        }

        return isBSG;
    }

    /**
     * 判断是否特殊用户，并赋值给 requestInfo ，减少重复查询
     *
     * @param requestInfo
     * @return
     */
    public boolean updateAndCheckTitleFilterWhiteNotWxb(RequestInfo requestInfo) {

        int titleThreshold = getTitleThreshold(requestInfo);
        requestInfo.setTitleThreshold(titleThreshold);

        return (titleThreshold >= GyConstant.THRESHOLD_White);
    }

    /**
     * 获取敏感词语过滤的分值控制
     *
     * @param requestInfo
     * @return
     */
    private int getTitleThreshold(RequestInfo requestInfo) {
        //wxb用户都不是标题过滤白名单
        if (isWxbUser(requestInfo)) {
            return SpecialApplicationConfig.getIntProperty(ApolloConstant.Wxb_Threshold);
        }


        String generalloc = null;

        //TODO 这里直接设置从requestInfo获取GeneralLoc
        UserModel userModel = requestInfo.getUserModel();
        if (userModel != null) {
            generalloc = userModel.getGeneralloc();
        }

        if (StringUtils.isBlank(generalloc)) {
            return GyConstant.THRESHOLD_UnKnow;
        } else {
            if (generalloc.contains(GyConstant.loc_BeiJin)) {
                return SpecialApplicationConfig.getIntProperty(ApolloConstant.Beijing_Threshold);
            } else if (generalloc.contains(GyConstant.loc_ShangHai) || generalloc.contains(GyConstant.loc_GuangDong)) {
                return SpecialApplicationConfig.getIntProperty(ApolloConstant.ShangHai_GuangDong_Threshold);
            }

        }
        return GyConstant.THRESHOLD_White;
    }


    /**
     * 老大的账户，不过滤a级媒体
     *
     * @param requestInfo
     * @return
     */
    public boolean isBossUser(RequestInfo requestInfo) {
        if (debugUserConfig.getDebugUser(ApolloConstant.boss_User_Key).contains(requestInfo.getUserId())) {
            return true;
        }
        return false;
    }


    /**
     * 判断是否特殊用户，并赋值给 requestInfo ，减少重复查询
     *
     * @param requestInfo
     * @return
     */
    public boolean isWxbUser(RequestInfo requestInfo) {
        String isSpecialUserStr = requestInfo.getIsSpecialUser();

        try {
            if (ApolloConstant.Switch_on.equals(ApplicationConfig.getProperty(ApolloConstant.ZQSwitch))) {

                //TODO 此处做了修改 注意！
                if (requestInfo.getUserGroup().contains("中轻大厦")) {
                    return false;
                }
            }
        } catch (Exception e) {
            logger.error("{} isZq Error {}", requestInfo.getUserId(), e);
        }

        if (StringUtils.isBlank(isSpecialUserStr)) {
            String value = wxbUsercache.getIfPresent(requestInfo.getUserId());
            boolean checkResult = StringUtils.isNotBlank(value);
            requestInfo.setIsSpecialUser(String.valueOf(checkResult)); //此处将网信办信息赋值给 requestInfo
            return checkResult;
        } else {
            return GyConstant.Str_true.equals(isSpecialUserStr);
        }
    }




    /**
     * 判断是是否是lvs过滤的白名单
     *
     * @param requestInfo
     * @return
     */
    public boolean isJiGouWhite(RequestInfo requestInfo) {
        //wxb都需要过滤
        if (isWxbUser(requestInfo)) {
            return false;
        }


        String generalLoc = null;

        UserModel userModel = requestInfo.getUserModel();
        if (userModel != null) {
            generalLoc = userModel.getGeneralloc();
        }


        //wxb检查时候打开开关，进行严格媒体过滤
        //全北京+地域为空进行机构过滤
        if (ApolloConstant.Switch_on.equals(SpecialApplicationConfig.getProperty(ApolloConstant.WxbContentSecuritySwitch))) {
            if (RecomChannelEnum.headline.getValue().equals(requestInfo.getRecomChannel())) {
                if (StringUtils.isBlank(generalLoc) || generalLoc.contains(GyConstant.loc_BeiJin)) {
                    if (requestInfo.getPullCount() <= GyConstant.pullCountLimit_3) {
                        return false;
                    }
                }
            }
        } else {
            //普通时期
            //地域为空只过滤前三刷
            if (RecomChannelEnum.headline.getValue().equals(requestInfo.getRecomChannel())) {
                if (StringUtils.isBlank(generalLoc)) {
                    if (requestInfo.getPullCount() <= GyConstant.pullCountLimit_3) {
                        return false;
                    }
                }
            }
        }


        return true;
    }


    public void updateAllUserAB(RequestInfo requestInfo) {

        boolean addLastSim = false;
        //lastCotag试验
        if (!requestInfo.isColdUser() || debugUserConfig.getDebugUser(ApolloConstant.CDML_DEBUG_USER).contains(requestInfo.getUserId())) {
            if(requestInfo.getLogicParams()!=null) {
                requestInfo.getLogicParams().setLastCotagFeedNum(1);
            }
            addLastSim = true;
        }else if(UserUtils.isFirstInToday(requestInfo.getUserModel())){
            if(requestInfo.getLogicParams()!=null) {
                requestInfo.getLogicParams().setLastCotagFeedNum(3);
            }
            addLastSim = true;
        } else if (requestInfo.isColdUser()) {
            if(requestInfo.getLogicParams()!=null) {
                requestInfo.getLogicParams().setLastCotagFeedNum(2);
            }
            addLastSim = true;
        }

        if(addLastSim||requestInfo.isColdFullNess()){
            Map<String, String> devMap = requestInfo.getDevMap();
            if (devMap == null) {
                devMap = new HashMap<>();
                requestInfo.setDevMap(devMap);
            }
            devMap.put("LastSim0", "true");
        }



        long checkNumFFM = MathUtil.getNumByUid(requestInfo.getUserId(), AbTestConstant.Abtest_recall_FFMTest);
        if (checkNumFFM < 10||(checkNumFFM>=30&&checkNumFFM<40)) {
            requestInfo.addAbtestInfo(AbTestConstant.Abtest_recall_FFMTest, AbTestConstant.Abtest_FFM_test_rate10);
            Map<String, String> devMap = requestInfo.getDevMap();
            if (devMap == null) {
                devMap = new HashMap<>();
                requestInfo.setDevMap(devMap);
            }
            devMap.put("userFFM", "true");
        } else if (checkNumFFM < 30) {
            requestInfo.addAbtestInfo(AbTestConstant.Abtest_recall_FFMTest, AbTestConstant.Abtest_FFM_base_rate10);
        } else if(checkNumFFM < 60){
            requestInfo.addAbtestInfo(AbTestConstant.Abtest_recall_FFMTest, AbTestConstant.Abtest_FFM_video_rate10);
            Map<String, String> devMap = requestInfo.getDevMap();
            if (devMap == null) {
                devMap = new HashMap<>();
                requestInfo.setDevMap(devMap);
            }
            devMap.put("userFFMV", "true");
        }


        //增加高品质账号试验，复用配置中心里面cdml测试用户配置方便修改配置
        UserModel userModel=requestInfo.getUserModel();
        if(userModel==null||(StringUtils.isNotBlank(userModel.getUa_v())&&"2".equals(userModel.getUa_v()))||debugUserConfig.getDebugUser(ApolloConstant.CDML_DEBUG_USER).contains(requestInfo.getUserId())){
            long highQualityTest = MathUtil.getNumByUid(requestInfo.getUserId(), AbTestConstant.Abtest_HighQualitySource_Test);
            if (highQualityTest < 10 && !debugUserConfig.getDebugUser(ApolloConstant.CDML_DEBUG_USER).contains(requestInfo.getUserId())) {
                requestInfo.addAbtestInfo(AbTestConstant.Abtest_HighQualitySource_Test, AbTestConstant.Abtest_HighQualitySource_base_NoLimit_rate10);
            } else if (highQualityTest < 20 || debugUserConfig.getDebugUser(ApolloConstant.CDML_DEBUG_USER).contains(requestInfo.getUserId())) {
                requestInfo.addAbtestInfo(AbTestConstant.Abtest_HighQualitySource_Test, AbTestConstant.Abtest_HighQualitySource_test_onlyHigh_rate10);
            }
        }

        /**
         * 阶梯流量控制实验 测试流量影响
         */
        long recallCrcNew = MathUtil.getNumByUidNew(requestInfo.getUserId(), AbTestConstant.Abtest_JpPoolReduce_Test);
        if (recallCrcNew < 100) {
            requestInfo.addAbtestInfo(AbTestConstant.CRCFlow_Test, AbTestConstant.Abtest_CRCFlow_test1);
        } else if (recallCrcNew < 600) {
            requestInfo.addAbtestInfo(AbTestConstant.CRCFlow_Test, AbTestConstant.Abtest_CRCFlow_test5);
        } else if (recallCrcNew < 1600) {
            requestInfo.addAbtestInfo(AbTestConstant.CRCFlow_Test, AbTestConstant.Abtest_CRCFlow_test10);
        } else if (recallCrcNew < 3600) {
            requestInfo.addAbtestInfo(AbTestConstant.CRCFlow_Test, AbTestConstant.Abtest_CRCFlow_test20);
        } else if (recallCrcNew < 7600) {
            requestInfo.addAbtestInfo(AbTestConstant.CRCFlow_Test, AbTestConstant.Abtest_CRCFlow_test40);
        } else {
            requestInfo.addAbtestInfo(AbTestConstant.CRCFlow_Test, AbTestConstant.Abtest_CRCFlow_test24);
        }


        /**
         *
         */
        long recallTest = MathUtil.getNumByUidNew(requestInfo.getUserId(), AbTestConstant.Abtest_CateFilter_Test);
        if (recallTest < 2000) {
            requestInfo.addAbtestInfo(AbTestConstant.Abtest_CateFilter_Test, AbTestConstant.CateFilter_test1_rate20);
            Map<String, String> devMap = requestInfo.getDevMap();
            if (devMap == null) {
                devMap = new HashMap<>();
                requestInfo.setDevMap(devMap);
            }
            devMap.put("CateFilter", "test1");
        } else if (recallTest < 4000) {
            requestInfo.addAbtestInfo(AbTestConstant.Abtest_CateFilter_Test, AbTestConstant.CateFilter_base_rate20);
        } else if (recallTest < 10000 && recallTest > 8000) {
            requestInfo.addAbtestInfo(AbTestConstant.Abtest_CateFilter_Test, AbTestConstant.CateFilter_test2_rate20);
            Map<String, String> devMap = requestInfo.getDevMap();
            if (devMap == null) {
                devMap = new HashMap<>();
                requestInfo.setDevMap(devMap);
            }
            devMap.put("CateFilter", "test2");
        }

        //GraphCotagTest 实验修改流量 50%实验 50%base
        long graphCotagTest = MathUtil.getNumByUidNew(requestInfo.getUserId(), AbTestConstant.Abtest_GraphCotag_Test);
        if (graphCotagTest < 5000) {
            requestInfo.addAbtestInfo(AbTestConstant.Abtest_GraphCotag_Test, AbTestConstant.GraphCotag_test_rate50);
            Map<String, String> devMap = requestInfo.getDevMap();
            if (devMap == null) {
                devMap = new HashMap<>();
                requestInfo.setDevMap(devMap);
            }
            devMap.put("GraphCotag", "true");
        } else if (graphCotagTest < 10000) {
            requestInfo.addAbtestInfo(AbTestConstant.Abtest_GraphCotag_Test, AbTestConstant.GraphCotag_base_rate50);
        }


        //于潇实验
        long negativeTest = MathUtil.getNumByUidNew(requestInfo.getUserId(), AbTestConstant.Abtest_FinalNegative_Test);
        if (negativeTest < 2000) {
            requestInfo.addAbtestInfo(AbTestConstant.Abtest_FinalNegative_Test, AbTestConstant.FinalNegative_test_rate20);
            Map<String, String> devMap = requestInfo.getDevMap();
            if (devMap == null) {
                devMap = new HashMap<>();
                requestInfo.setDevMap(devMap);
            }
            devMap.put("FinalNegative", "true");
        } else if (negativeTest < 4000) {
            requestInfo.addAbtestInfo(AbTestConstant.Abtest_FinalNegative_Test, AbTestConstant.FinalNegative_base_rate20);
        }


    }

    public boolean checkIsCommonExpTest(RequestInfo requestInfo){
        return requestInfo.checkAbtestInfo(AbTestConstant.Abtest_UserInsertExp_Test, AbTestConstant.Abtest_CommonExp_test_rate10);
    }

    public boolean checkIsCommonExpMixTest(RequestInfo requestInfo){
        return requestInfo.checkAbtestInfo(AbTestConstant.Abtest_UserInsertExp_Test, AbTestConstant.Abtest_CommonExp_testMix_rate10);
    }

    public boolean checkIsNewIncreaseTest(RequestInfo requestInfo){
        return requestInfo.checkAbtestInfo(AbTestConstant.Abtest_newIncrease_Test, AbTestConstant.NewIncrease_test_rate30);
    }

    public boolean checkIsNewIncreaseGuide(RequestInfo requestInfo){
        return requestInfo.checkAbtestInfo(AbTestConstant.Abtest_newIncrease_Test, AbTestConstant.NewIncrease_guide_rate30);
    }

    public boolean checkIsOutColdTest(RequestInfo requestInfo){
        return requestInfo.checkAbtestInfo(AbTestConstant.Abtest_OutCold_Test, AbTestConstant.OutCold_test_rate);
    }
    /**
     *  实时调用试验
     */
    public boolean checkIsRealTimeTest(RequestInfo requestInfo){
        if(ApolloConstant.Switch_on.equals(ApplicationConfig.getProperty(ApolloConstant.RealTimeSwitch))){
            int percent= StringUtils.isBlank(ApplicationConfig.getProperty(ApolloConstant.RealTimePer))?5:ApplicationConfig.getIntProperty(ApolloConstant.RealTimePer);
            long recallRealTime = MathUtil.getNumByUid(requestInfo.getUserId(), AbTestConstant.Abtest_RealTimeRecall_Test);
            if (recallRealTime < percent||debugUserConfig.getDebugUser(ApolloConstant.realTimeDebuger).contains(requestInfo.getUserId())) {
                return true;
            }
        }
        return false;
    }

    public int initCommonExpPro(RequestInfo requestInfo){
        double p=0;
        double fullness=0;
        double dailyPull=0;
        String basicFactorStr=ApplicationConfig.getProperty(ApolloConstant.basicFactor);
        String mulFactorStr=ApplicationConfig.getProperty(ApolloConstant.mulFactor);
        String fullnessFactorStr=ApplicationConfig.getProperty(ApolloConstant.fullnessFactor);
        double basicFactor=StringUtils.isNotBlank(basicFactorStr)?Double.parseDouble(basicFactorStr):0.3;
        double mulFactor=StringUtils.isNotBlank(mulFactorStr)?Double.parseDouble(mulFactorStr):0.7;
        double fullnessFactor=StringUtils.isNotBlank(fullnessFactorStr)?Double.parseDouble(fullnessFactorStr):0.4;
        int expNum=0;
        try{
            int pullcount = userActionUtil.getUserSessionPullNum(requestInfo);
            UserModel userModel=requestInfo.getUserModel();
            if(userModel!=null){
                if(StringUtils.isNotBlank(userModel.getFullness())){
                    fullness=Double.parseDouble(userModel.getFullness());
                }
                if(StringUtils.isNotBlank(userModel.getDaily_pullnum())){
                    dailyPull=Double.parseDouble(userModel.getDaily_pullnum());
                }
            }

            if(fullness>0&&dailyPull>0){
                p=Math.sqrt((1-fullness)*Math.min(pullcount,dailyPull)/dailyPull)*mulFactor+basicFactor;
            }
            double yourRandom=Math.random();
            int dailyPullInt=Math.max(9,(int)dailyPull);

            //无论是否命中都要计算expnum
            expNum=initCommonExpNum(requestInfo,fullness,dailyPullInt);
            if(yourRandom>p||(fullness>fullnessFactor&&pullcount<2)){
                expNum=0;
            }
            DebugUtil.debugLog(requestInfo.isDebugUser(),"{} initCommonExpPro:{} yourRandom:{} pullcount:{} dailyPull:{} fullness:{} expNum:{}",requestInfo.getUserId(),p,yourRandom,pullcount,dailyPull,fullness,expNum);
        }catch (Exception e){
            logger.error("{} initCommonExpPro error:{}",requestInfo.getUserId(),e);
        }
        return expNum;
    }

    /**
     * 设：
     *   试探计数：n,
     *   当前session内已加载屏数：t,
     *   最近1屏是否有过点击（bool）: x,
     *   最近2屏是否有过点击（bool）: y,
     *   最近3屏是否有过点击（bool）: z,
     *   最近k屏是否有过点击（bool）: k,
     * 则，下一屏试探条数=(int) (Math.floor(n/(x==1?2:1))+Math.max(0,z-y-x)+(1-k)*(pullcount>=pullK?1:0));
     * @param requestInfo
     * @return
     */
    public int initCommonExpNum(RequestInfo requestInfo,double fullness,int pullK){
        int expNum=2;
        String expLastNStr=userActionUtil.getUserSessionShort(requestInfo,GyConstant.expLastN);
        if(fullness<=0.7){
            expNum=StringUtils.isNotBlank(expLastNStr)?Integer.parseInt(expLastNStr):ApplicationConfig.getIntProperty(ApolloConstant.expNumA);
        }else {
            expNum=StringUtils.isNotBlank(expLastNStr)?Integer.parseInt(expLastNStr):ApplicationConfig.getIntProperty(ApolloConstant.expNumB);
        }
        try{
            int x=0;
            int y=0;
            int z=0;
            int k=0;
            int pullcount = userActionUtil.getUserSessionPullNum(requestInfo);
            int pullExpCount2=0;
            int pullExpCount4=0;
            int pullExpCount6=0;
            int pullExpCountk=0;
            String pullExpCountStr2=userActionUtil.getUserSessionShort(requestInfo,GyConstant.pullExpCount2);
            String pullExpCountStr4=userActionUtil.getUserSessionShort(requestInfo,GyConstant.pullExpCount4);
            String pullExpCountStr6=userActionUtil.getUserSessionShort(requestInfo,GyConstant.pullExpCount6);
            String pullExpCountStrK=userActionUtil.getUserSessionShort(requestInfo,GyConstant.pullExpCountK);

            if(CollectionUtils.isNotEmpty(requestInfo.getNewLastDocList())&&requestInfo.getNewLastDocList().size()>0){
                //因为是上一屏pullcount点击的所以以下2,4,6屏在此屏应该是+0,1,2
                int pullExpCountNew2=pullcount;  x=1;
                int pullExpCountNew4=pullcount+1;  y=1;
                int pullExpCountNew6=pullcount+2;  z=1;
                int pullExpCountNewk=pullcount+pullK-1;  k=1;
                //因为有点击 所以k清零
                DebugUtil.debugLog(requestInfo.isDebugUser(),"{} initCommonExpNum come in step1 pullExpCountNew1:{} pullExpCountNew2:{} pullExpCountNew3:{} pullExpCountNewk:{} expInitNum:{} x:{},y:{},z:{},k:{}",requestInfo.getUserId(),pullExpCountNew2,pullExpCountNew4,pullExpCountNew6,pullExpCountNewk,expNum,x,y,z,k);
                userActionUtil.updateSessionNoHis(requestInfo,GyConstant.pullExpCount2,pullExpCountNew2+"");
                userActionUtil.updateSessionNoHis(requestInfo,GyConstant.pullExpCount4,pullExpCountNew4+"");
                userActionUtil.updateSessionNoHis(requestInfo,GyConstant.pullExpCount6,pullExpCountNew6+"");
                userActionUtil.updateSessionNoHis(requestInfo,GyConstant.pullExpCountK,pullExpCountNewk+"");
            }else{
                pullExpCount2=StringUtils.isNotBlank(pullExpCountStr2)?Integer.parseInt(pullExpCountStr2):-1;
                if(pullExpCount2>0&&pullcount<=pullExpCount2){
                    x=1;
                }
                pullExpCount4=StringUtils.isNotBlank(pullExpCountStr4)?Integer.parseInt(pullExpCountStr4):-1;
                if(pullExpCount4>0&&pullcount<=pullExpCount4){
                    y=1;
                }
                pullExpCount6=StringUtils.isNotBlank(pullExpCountStr6)?Integer.parseInt(pullExpCountStr6):-1;
                if(pullExpCount6>0&&pullcount<=pullExpCount6){
                    z=1;
                }
                pullExpCountk=StringUtils.isNotBlank(pullExpCountStrK)?Integer.parseInt(pullExpCountStrK):-1;
                if(pullExpCountk>0&&pullcount<=pullExpCountk){
                    k=1;
                }
                DebugUtil.debugLog(requestInfo.isDebugUser(),"{} initCommonExpNum come in step2 pullExpCountNew1:{} pullExpCountNew2:{} pullExpCountNew3:{},pullExpCountNewk:{} expInitNum:{} x:{},y:{},z:{},k:{}",requestInfo.getUserId(),pullExpCount2,pullExpCount4,pullExpCount6,pullExpCountk,expNum,x,y,z,k);
            }
            int pr=x==1?2:1;
            expNum= (int) (Math.floor(expNum/pr)+Math.max(0,z-y-x)+(1-k)*(pullcount>=pullK?1:0));
            if(fullness<=0.7){
                if(expNum<0){
                    expNum=ApplicationConfig.getIntProperty(ApolloConstant.minExpNumA);
                }else if(expNum>ApplicationConfig.getIntProperty(ApolloConstant.maxExpNumA)){
                    expNum=ApplicationConfig.getIntProperty(ApolloConstant.maxExpNumA);
                }
            }else{
                if(expNum<0){
                    expNum=ApplicationConfig.getIntProperty(ApolloConstant.minExpNumB);
                }else if(expNum>ApplicationConfig.getIntProperty(ApolloConstant.maxExpNumB)){
                    expNum=ApplicationConfig.getIntProperty(ApolloConstant.maxExpNumB);
                }
            }
            userActionUtil.updateSessionNoHis(requestInfo,GyConstant.expLastN,expNum+"");
            DebugUtil.debugLog(requestInfo.isDebugUser(),"{} pullcount:{} initCommonExpNum old session pullExpCountNew1:{},pullExpCountNew2:{},pullExpCountNew3:{},pullExpCountNewk:{} expNum:{}",requestInfo.getUserId(),pullcount,pullExpCountStr2,pullExpCountStr4,pullExpCountStr6,pullExpCountStrK,expNum);
            DebugUtil.debugLog(requestInfo.isDebugUser(),"{} pullcount:{} initCommonExpNum new session pullExpCountNew1:{},pullExpCountNew2:{},pullExpCountNew3:{},pullExpCountNewk:{} updateLastExpN:{} expNum:{}",requestInfo.getUserId(),pullcount,userActionUtil.getUserSessionShort(requestInfo,GyConstant.pullExpCount2),userActionUtil.getUserSessionShort(requestInfo,GyConstant.pullExpCount4),userActionUtil.getUserSessionShort(requestInfo,GyConstant.pullExpCount6),userActionUtil.getUserSessionShort(requestInfo,GyConstant.pullExpCountK),userActionUtil.getUserSessionShort(requestInfo,GyConstant.expLastN),expNum);
        }catch (Exception e){
            logger.error("{} initCommonExpNum error:{}",requestInfo.getUserId(),e);
            expNum=0;
        }

        return Math.max(expNum,0);
    }

    /**
     * 冷启动试验分组
     *
     * @param requestInfo
     */
    public void updateColdTestUser(RequestInfo requestInfo) {
        try {
            if ((!requestInfo.isColdUser() && !debugUserConfig.getDebugUser(ApolloConstant.coldSpecial_white_key).contains(requestInfo.getUserId()))
                    || isBeiJingUserOrWxb(requestInfo)) {
                return;
            }

            //为mix透传 冷启动标识
            Map<String, String> devMap = requestInfo.getDevMap();
            if (devMap == null) {
                devMap = new HashMap<>();
                requestInfo.setDevMap(devMap);
            }
            devMap.put("isColder", "Yes");

            //TODO 节后将视频自动播放试验恢复

//            if(debugUserConfig.getDebugUser(ApolloConstant.coldSpecial_white_key).contains(requestInfo.getUserId())){
//                requestInfo.addAbtestInfo(AbTestConstant.Abtest_Cold_VideoIsAuto_Test, AbTestConstant.Abtest_Cold_VideoIsAuto_test_rate5);
//            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("{} updateColdTestUser ERROR:{}", requestInfo.getUserId(), e);
        }
    }



//    public boolean checkColdVideoIsAuto(RequestInfo requestInfo) {
//        if (requestInfo.checkAbtestInfo(AbTestConstant.Abtest_Cold_VideoIsAuto_Test, AbTestConstant.Abtest_Cold_VideoIsAuto_test_rate5)) {
//            return true;
//        }
//        return false;
//    }


    /**
     * 更新用户的相关白名单缓存
     *
     * @param requestInfo
     */
    public void updateSpecialUserCache(RequestInfo requestInfo) {
        String generalloc = "";
        String umos = "";

        if (requestInfo != null) {
            UserModel userModel = requestInfo.getUserModel();

            if (userModel != null) {
                generalloc = userModel.getGeneralloc();
                umos = userModel.getUmos();
            }

            //此处更新常驻地是否为北京 及北京ios用户标识
            updateBeiJingCache(requestInfo, generalloc, umos);

            Map<String, Boolean> userTypeMap = requestInfo.getUserTypeMap();
            userTypeMap.put(GyConstant.isWxb, isWxbUser(requestInfo));
            userTypeMap.put(GyConstant.isBeiJingUserNotWxb, isBeiJingUserNotWxb(requestInfo));
            userTypeMap.put(GyConstant.isJiGoutWhite, isJiGouWhite(requestInfo));
            userTypeMap.put(GyConstant.isTitleFilterWhiteNotWxb, updateAndCheckTitleFilterWhiteNotWxb(requestInfo));
            userTypeMap.put(GyConstant.isDevTestUser, isBossUser(requestInfo));
            userTypeMap.put(GyConstant.isColdNotBj, isColderUserNotBeijing(requestInfo));
            userTypeMap.put(GyConstant.isNeedFilterIfengVideoUser, isNeedFilterIfengVideoUser(requestInfo)); //排除凤凰卫视内容用户
            userTypeMap.put(GyConstant.needLowSimidFilter, needLowSimidFilter(requestInfo));
            userTypeMap.put(GyConstant.needQttFilter, needQttFilter(requestInfo));
            userTypeMap.put(GyConstant.isBSGUser, isBSGUser(requestInfo));//判断是否是北上广用户

            //更新冷启动用户标记
            updateColdUserFlag(requestInfo);

            //冷启动ab试验分组
            updateColdTestUser(requestInfo);

            //全量用户ab测
            updateAllUserAB(requestInfo);

            //更新dev信息，透传给mix使用或者头条自己使用
            updateDevMap(requestInfo);

            //个性化帖子都会打上通用的abtest标记
            expfwUtil.doCommonAbtest(requestInfo);


            updateResultFlag(requestInfo);
        }
    }



    /**
     * 更新吐给调用方的 实验标记
     *
     * @param requestInfo
     */
    private void updateResultFlag(RequestInfo requestInfo) {
        String proid = requestInfo.getProid();
        if (ProidType.ifengnewslite.getValue().equals(proid)
                || ProidType.ifengnewssdk.getValue().equals(proid)
                || ProidType.ifengnewsdiscovery.getValue().equals(proid)
                || ProidType.ifengnewsgold.getValue().equals(proid)) {

            if (FlowTypeUtils.isIfengnewsHeadLine(requestInfo)) {
                Map<String, String> flagMap = requestInfo.getFlagMap();
                flagMap.put(ResultFlagConstant.flag_jpType, ResultFlagConstant.flag_value_jpFilter);

                //兼容晓伟的逻辑，首屏设置size为10
                if (requestInfo.getPullNum() == 0) {
                    requestInfo.setSize(10);
                }
            }
        }
    }

    public void updateIsGuideFlag(RequestInfo requestInfo) {
        try{
            if((checkIsNewIncreaseGuide(requestInfo)&&!checkIsOutColdTest(requestInfo)&&requestInfo.isFirstAccess()&&UserUtils.isFirstScreen(requestInfo))||(debugUserConfig.getDebugUser(ApolloConstant.Special_Style_Key).contains(requestInfo.getUserId())&&UserUtils.isFirstScreen(requestInfo))){
                Map<String, String> flagMap = requestInfo.getFlagMap();
                String isGuideNum= StringUtils.isBlank(ApplicationConfig.getProperty(ApolloConstant.isGuideNum))?"3.5":ApplicationConfig.getProperty(ApolloConstant.isGuideNum);
                flagMap.put("isGuide", isGuideNum);
            }
        }catch (Exception e){
            logger.error("{} updateIsGuideFlag error:{}",requestInfo.getUserId(),e);
        }

    }

    /**
     * 更新冷启动标记
     *
     * @param requestInfo
     */
    public void updateColdUserFlag(RequestInfo requestInfo) {
        try {

            Map<String, Boolean> userTypeMap = requestInfo.getUserTypeMap();
            boolean isWxb = userTypeMap.getOrDefault(GyConstant.isWxb, true);
            if (isWxb) {
                return;
            }
            UserModel userModel = requestInfo.getUserModel();
            boolean isColdNotBeiJing = false;
            int dayCount = -2;
            if (userModel != null && StringUtils.isNotBlank(userModel.getGeneralloc())) {
                if (userModel.getGeneralloc().contains(GyConstant.loc_BeiJin)) {
                    return;
                } else {
                    dayCount = userModel.getDayCount();
                    if (dayCount < GyConstant.dayCount_ColdUser && dayCount >= 0) {
                        isColdNotBeiJing = true;
                    }
                }
            } else {
                String loc = requestInfo.getLoc();
                if (StringUtils.isNotBlank(loc)) {
                    if (loc.contains(GyConstant.loc_BeiJin)) {
                        return;
                    } else {
                        isColdNotBeiJing = true;
                    }
                }
            }


            requestInfo.setColdUserNotBj(isColdNotBeiJing);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("{} Abtest_ColdJpTest ERROR:{}", requestInfo.getUserId(), e);
        }
    }


    /**
     * 更新dev信息，透传给mix使用或者头条自己使用
     *
     * @param requestInfo
     */
    private void updateDevMap(RequestInfo requestInfo) {
        Map<String, String> devMap = requestInfo.getDevMap();

        //更新敏感词过滤分值控制
        int threadHold = requestInfo.getTitleThreshold();
        devMap.put(GyConstant.threadHold_title_filter, String.valueOf(threadHold));
        Map<String, Boolean> userTypeMap = requestInfo.getUserTypeMap();
        boolean needBeijingFilter = userTypeMap.getOrDefault(GyConstant.isBeiJingUserNotWxb, false);

        //对非北京用户进行小视频放量
        if (!needBeijingFilter) {
            devMap.put(GyConstant.Small_Video_Extend, GyConstant.Small_Video_Extend);
        }
    }

    /**
     * 处理张阳的simid垃圾过滤
     *
     * @param requestInfo
     * @return
     */
    private boolean needLowSimidFilter(RequestInfo requestInfo) {
        Map<String, Boolean> userTypeMap = requestInfo.getUserTypeMap();
        long abTestLowDoc = MathUtil.getNumByUid(requestInfo.getUserId(),"LowSimidFilterTest");
        boolean needLowSimIdFilter = true;
        if (userTypeMap.getOrDefault(GyConstant.isTitleFilterWhiteNotWxb, false)) {
            //非北上广不用过滤，留下5%流量做对比
            if (abTestLowDoc < 5) {
                needLowSimIdFilter = true;
            } else if (abTestLowDoc < 10) {
                needLowSimIdFilter = false;
            } else {
                needLowSimIdFilter = false;
            }
        } else {
            needLowSimIdFilter = true;
        }
        return needLowSimIdFilter;
    }




    /**
     * 过滤最后一次曝光的热点，防止两个热点相连
     * @param eventTitle
     * @return
     */
    public boolean filterHotLastExpo(RequestInfo requestInfo,String eventTitle) {
        String lastExpo = userActionUtil.getUserSessionShort(requestInfo,GyConstant.hotLastExpo);
        if (StringUtils.isNotBlank(lastExpo)&&lastExpo.contains(eventTitle)) {
            return true;
        }
        return false;
    }

    /**
     * 处理趣头条过滤，对常驻地为北京上海的用户进行过滤
     *
     * @param requestInfo
     * @return
     */
    private boolean needQttFilter(RequestInfo requestInfo) {
        boolean needQttFilter = true;
        UserModel userModel = requestInfo.getUserModel();
        if (userModel != null) {
            String locString = userModel.getGeneralloc();
            if (FlowTypeUtils.isIfengnewsGold(requestInfo)) {
                needQttFilter = false;
            } else if (StringUtils.isNotBlank(locString) && FlowTypeUtils.isIfengnewsHeadLine(requestInfo)) {
                if (locString.contains(GyConstant.loc_BeiJin) || locString.contains(GyConstant.loc_ShangHai)) {
                    needQttFilter = true;
                } else {
                    needQttFilter = false;
                }
            }
        }
        return needQttFilter;
    }


    /**
     * 判断是否是排北京的冷启动用户
     *
     * @param requestInfo
     * @return
     */
    private boolean isColderUserNotBeijing(RequestInfo requestInfo) {
        //排北京
        if (GyConstant.Str_true.equals(beijinUsercache.getIfPresent(requestInfo.getUserId()))) {
            return false;
        }
        String loc = requestInfo.getLoc();
        if (StringUtils.isBlank(loc)) {
            return false;
        } else if (loc.contains(GyConstant.loc_BeiJin)) {
            return false;
        }
        return requestInfo.isColdUser();
    }


    /**
     * 判断是否是排 凤凰卫视 内容的用户
     *
     * @param requestInfo
     * @return
     */
    public boolean isNeedFilterIfengVideoUser(RequestInfo requestInfo) {
        //排网信办
        if (isWxbUser(requestInfo)) {
            return true;
        } else if (requestInfo.isDebugUser()) {
            return false; //debug 用户 不排除 卫视内容
        }
//        //获取 常住地址字符串  排除 配置项的 一些城市
//        String locString = requestInfo.getPermanentLoc();
//        //增量过来 不查询用户画像 所以字符串为空 所以需要查询缓存获取
//        if (StringUtils.isBlank(locString)) {
//            UserModel userModel = requestInfo.getUserModel();
//            if (userModel != null) {
//                locString = userModel.getGeneralloc();
//                if (StringUtils.isNotBlank(locString)) {
//                    requestInfo.setPermanentLoc(locString); //反写入requestInfo
//                }
//            }
//        }
//
//        if (StringUtils.isBlank(locString)) {
//            return true; //常驻地 丢失 则默认排除
//        }
//        for (String CityStr : GyConstant.blackListOfCityForIfengVideo) {
//            if (locString.contains(CityStr)) {
//                return true; //常驻地 字符串 包含黑名单 则过滤
//            }
//        }
        //其余用户 不需要过滤 凤凰卫视内容
        return false;
    }

    /**
     * 更新北京用户的cache
     *
     * @param requestInfo
     * @param generalloc
     */
    private void updateBeiJingCache(RequestInfo requestInfo, String generalloc, String umos) {
        //避免重复判断
        if (StringUtils.isNotBlank(beijinUsercache.getIfPresent(requestInfo.getUserId()))) {
            return;
        }

        String isBeiJing = GyConstant.Str_false;
        if (StringUtils.isNotBlank(generalloc)) {
            if (generalloc.contains(GyConstant.loc_BeiJin)) {
                isBeiJing = GyConstant.Str_true;
            }
        }
        logger.info("put isBeiJing {}, {}, generalloc：{}, umos:{}", requestInfo.getUserId(), isBeiJing, generalloc, umos);
        beijinUsercache.put(requestInfo.getUserId(), isBeiJing);
    }

    /**
     * 测试分组当天新增用户 是否用xr提供的新标签
     * @param requestInfo
     * @return
     */
    public boolean isNewFirstInTag(RequestInfo requestInfo){
        if(UserUtils.isFirstScreen(requestInfo)&&UserUtils.isFirstInToday(requestInfo.getUserModel())&&requestInfo.isFirstAccess()){
            return true;
        }
        return false;
    }

    public boolean isNewDefaultTag(RequestInfo requestInfo){
        if((UserUtils.isFirstScreen(requestInfo)&&UserUtils.isFirstInToday(requestInfo.getUserModel())&&requestInfo.isFirstAccess()) ||(debugUserConfig.getDebugUser(ApolloConstant.coldSpecial_white_key).contains(requestInfo.getUserId())&&UserUtils.isFirstScreen(requestInfo))){
            return true;
        }
        return false;
    }

//    /**
//     * 判断是否是外部素材拉新的用户
//     * @param requestInfo
//     * @return
//     */
//    public String isOutColdUser(RequestInfo requestInfo){
//        String kind="";
//        try{
//            UserModel userModel=requestInfo.getUserModel();
//            String firstIn=StringUtils.isNotBlank(userModel.getFirst_in())?userModel.getFirst_in():"";
//            SimpleDateFormat format=new SimpleDateFormat("yyyyMMdd");
//            //说明新增用户已经使用超过7个自然日了
//            if(StringUtils.isNotBlank(firstIn)&&DateUtils.getNatureDayCount(format.parse(firstIn),new Date())>7){
//                DebugUtil.debugLog(requestInfo.isDebugUser(),"{} isOutColdUser DayCount:{}",requestInfo.getUserId(),DateUtils.getNatureDayCount(format.parse(firstIn),new Date()));
//                return kind;
//            }else{
//                kind = OutColdCacheUtil.getFromCache(requestInfo.getUserId());
//            }
//            DebugUtil.debugLog(requestInfo.isDebugUser(),"{} isOutColdUser kind:{}",requestInfo.getUserId(),kind);
//        }catch (Exception e){
//            logger.error("{} isOutColdUser error:{}",requestInfo.getUserId(),e);
//        }
//        return kind;
//    }

//    /**
//     * 判断是否请求数据组冷启动，满足kind比例限制的才需要，默认不需要
//     * @param requestInfo
//     * @param outInfo
//     * @return
//     */
//    public boolean isOutKind(RequestInfo requestInfo,String outInfo){
//        try{
//            if(StringUtils.isBlank(outInfo)||debugUserConfig.getDebugUser(ApolloConstant.isNotOutDebuger).contains(requestInfo.getUserId())){
//                return false;
//            }
//            String kind=outInfo.split(GyConstant.Symb_Comma)[0];
//            String perStr=OutColdCacheUtil.getOutKindCache(kind);
//            DebugUtil.debugLog(requestInfo.isDebugUser(),"{} isOutKind kind:{} percent:{}",requestInfo.getUserId(),kind,perStr);
//            if(StringUtils.isNotBlank(perStr)){
//                double percent=Double.parseDouble(perStr)*10000;
//                percent=Math.min(5000,percent);
//                double percentDouble=percent*2;
//                long checkNum = MathUtil.getNumByUidNew(requestInfo.getUserId(), AbTestConstant.Abtest_OutCold_Test);
//                //若uid以debug开头，则不走比例限制
//                if (checkNum < percent || StringUtils.startsWith(requestInfo.getUserId(), GyConstant.debugUidFlag)) {
//                    requestInfo.addAbtestInfo(AbTestConstant.Abtest_OutCold_Test, AbTestConstant.OutCold_test_rate);
//                    return true;
//                } else if (checkNum < percentDouble) {
//                    requestInfo.addAbtestInfo(AbTestConstant.Abtest_OutCold_Test, AbTestConstant.OutCold_base_rate);
//                    return false;
//                }
//            }
//        }catch (Exception e){
//            logger.error("{} isOutKind error:{}",requestInfo.getUserId(),e);
//            return false;
//        }
//        return false;
//    }
//
//    public boolean isOutColdUserTest(RequestInfo requestInfo){
//        try{
//            //对外冷启动不出固定位
//            if(UserUtils.isIfengNewHead(requestInfo)) {
//                String outInfo = isOutColdUser(requestInfo);
//                boolean isOutKind = isOutKind(requestInfo, outInfo);
//                if(StringUtils.isNotBlank(outInfo)&&isOutKind){
//                    return true;
//                }
//            }
//        }catch (Exception e){
//            logger.error("{} isOutColdUserTest error:{}",requestInfo.getUserId(),e);
//        }
//        return false;
//    }


    public static void main(String[] args) {

        long start = System.currentTimeMillis();
        loadCache();
        System.out.println("cost:" + (System.currentTimeMillis() - start));

        System.out.println(wxbUsercache.getIfPresent("debug779"));
        System.out.println(wxbUsercache.getIfPresent("955febc69e224dcd9474f36b37b6d029"));
        System.out.println(wxbUsercache.getIfPresent("04e0ee334ef24c1b8b28255ae4179cc9"));
        System.out.println(wxbUsercache.getIfPresent("866090030643625"));
        System.out.println(wxbUsercache.getIfPresent("8660900306436251"));
        System.out.println(wxbUsercache.getIfPresent("8660900306436252"));
        System.out.println(wxbUsercache.getIfPresent("8660900306436253"));

    }

}
