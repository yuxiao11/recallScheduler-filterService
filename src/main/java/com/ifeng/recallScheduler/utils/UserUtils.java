package com.ifeng.recallScheduler.utils;


import com.ifeng.recallScheduler.apolloConf.ApolloConstant;
import com.ifeng.recallScheduler.apolloConf.ApplicationConfig;
import com.ifeng.recallScheduler.apolloConf.HotApplicationConfig;
import com.ifeng.recallScheduler.constant.AbTestConstant;
import com.ifeng.recallScheduler.constant.GyConstant;
import com.ifeng.recallScheduler.constant.RecomChannelEnum;
import com.ifeng.recallScheduler.enums.ProidType;
import com.ifeng.recallScheduler.request.RequestInfo;
import com.ifeng.recallScheduler.user.ChannelCtr;
import com.ifeng.recallScheduler.user.RecordInfo;
import com.ifeng.recallScheduler.user.UserModel;
import com.ifeng.recallScheduler.user.UserModelSearchAPI;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 和用户画像拆分出来的部分画像字段，单独控制过期时间
 * Created by jibin on 2017/11/7.
 */
@Service
public class UserUtils {

    protected static Logger logger = LoggerFactory.getLogger(UserUtils.class);


    @Autowired
    private UserModelSearchAPI userModelSearchAPI;

    /**
     * 判断是否普通用户，普通用户3分钟触发一次增量
     * 冷启动用户每次上下拉都触发增量召回消息
     * 判断依据：存在画像，但是 Recent_t1 字段为空（召回使用试探进行召回）
     *
     * @param requestInfo
     * @return
     */
    public boolean isColdUser(RequestInfo requestInfo) {
        boolean isColdUser = false;
        try {
            UserModel userModel = requestInfo.getUserModel();
            String ua_v = userModel.getUa_v();
            //理论上画像为空也应该是冷启动 但是由于怕画像为空是由服务超时导致的 所以暂时不加 判断为冷启动用户，ua_v<=2
            if (((StringUtils.isBlank(ua_v))||isNewColdFullness(requestInfo))
                    && !userModel.isTimeOut()) {
                requestInfo.addAbtestInfo(AbTestConstant.Cold_Count_test, AbTestConstant.Cold_Count_Num);
                logger.warn("{} ua_v is:{},isTimeOut:{}", requestInfo.getUserId(), ua_v,userModel.isTimeOut());
                isColdUser = true;
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("{},check isColdUser ERROR:{}", requestInfo.getUserId(), e);
        }
        return isColdUser;
    }


    public boolean isColdFullNess(RequestInfo requestInfo) {
        boolean isColdFullNess = false;
        try {
            UserModel userModel = requestInfo.getUserModel();
            String fullNess = userModel.getFullness();
            double fullNessNum=StringUtils.isNotBlank(fullNess)?Double.parseDouble(fullNess):0;
            String coldFullnessStr=ApplicationConfig.getProperty(ApolloConstant.coldFullness);
            double fullnessFactor=StringUtils.isNotBlank(coldFullnessStr)?Double.parseDouble(coldFullnessStr):0.35;
            //理论上画像丰满度为空也应该是不丰满用户 但如果画像为空是由服务超时导致的  则不算 其余丰满度小于阈值 则算为不丰满
            if ((StringUtils.isBlank(fullNess) || fullNessNum<fullnessFactor) && !userModel.isTimeOut()) {
                logger.warn("{} fullNess is:{},isTimeOut:{}", requestInfo.getUserId(), fullNessNum,userModel.isTimeOut());
                isColdFullNess = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("{},check isColdFullNess ERROR:{}", requestInfo.getUserId(), e);
        }
        return isColdFullNess;
    }

    public boolean isNewColdFullness(RequestInfo requestInfo) {
        boolean isColdFullNess = false;
        try {
            UserModel userModel = requestInfo.getUserModel();
            String fullNess = userModel.getFullness();
            double fullNessNum=StringUtils.isNotBlank(fullNess)?Double.parseDouble(fullNess):0;
            String coldFullnessStr=ApplicationConfig.getProperty(ApolloConstant.newColdFullness);
            double fullnessFactor=StringUtils.isNotBlank(coldFullnessStr)?Double.parseDouble(coldFullnessStr):0.25;
            //理论上画像丰满度为空也应该是不丰满用户 但如果画像为空是由服务超时导致的  则不算 其余丰满度小于阈值 则算为不丰满
            if ((StringUtils.isBlank(fullNess) || fullNessNum<=fullnessFactor) && !userModel.isTimeOut()) {
                logger.warn("{} newColdfullNess is:{},isTimeOut:{}", requestInfo.getUserId(), fullNessNum,userModel.isTimeOut());
                isColdFullNess = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("{},check isColdFullNess ERROR:{}", requestInfo.getUserId(), e);
        }
        return isColdFullNess;
    }

    /**
     * 为cotag降权做的试验
     * @param requestInfo
     * @param limit
     * @return
     */
    public boolean isFullNessLimit(RequestInfo requestInfo,double limit) {
        boolean isFullNess = false;
        try {
            UserModel userModel = requestInfo.getUserModel();
            if(userModel==null){
                return isFullNess;
            }
            String fullNess = userModel.getFullness();
            double fullNessNum=StringUtils.isNotBlank(fullNess)?Double.parseDouble(fullNess):0;
            if (fullNessNum>=limit) {
                isFullNess = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("{},check isFullNessLimit ERROR:{}", requestInfo.getUserId(), e);
        }
        return isFullNess;
    }

    /**
     * 为cotag降权做的试验（新）
     * @param requestInfo
     * @return
     */
    public boolean isFullNessLimitNew(RequestInfo requestInfo) {
        boolean isFullNess = false;
        try {
            UserModel userModel = requestInfo.getUserModel();
            if(userModel==null){
                return isFullNess;
            }
            String fullNess = userModel.getFullness();
            double fullNessNum=StringUtils.isNotBlank(fullNess)?Double.parseDouble(fullNess):0;
            if (fullNessNum>=0.7&&fullNessNum<0.75) {
                isFullNess = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("{},check isFullNessLimitNew ERROR:{}", requestInfo.getUserId(), e);
        }
        return isFullNess;
    }

    public boolean isChannelLocalMax(RequestInfo requestInfo) {
        boolean isChannelLocalMax = false;
        try {
            UserModel userModel = requestInfo.getUserModel();
            if(userModel==null){
                return false;
            }
            ChannelCtr channelCtr=userModel.getChannelCtrM();
            if(channelCtr==null){
                return isChannelLocalMax;
            }
            int localEv=channelCtr.getLocal_ev();
            double localCtr=channelCtr.getLocal_ctr();
            double totalCtr=channelCtr.getTotal_ctr();
            int totalPv=channelCtr.getTotal_pv();
            DebugUtil.debugLog(requestInfo.isDebugUser(),"{} isChannelLocalMax localEv:{},localCtr:{},totalPv:{}",requestInfo.getUserId(),localEv,localCtr,totalPv);
            int localEvApollo=StringUtils.isNotBlank(ApplicationConfig.getProperty(ApolloConstant.localEvMin))?
                    Integer.parseInt(ApplicationConfig.getProperty(ApolloConstant.localEvMin)):15;
            double localCtrApollo=StringUtils.isNotBlank(ApplicationConfig.getProperty(ApolloConstant.localCtrMin))?
                    Double.parseDouble(ApplicationConfig.getProperty(ApolloConstant.localCtrMin)):0.155;
            int totalPvApollo=StringUtils.isNotBlank(ApplicationConfig.getProperty(ApolloConstant.totalPvMax))?
                    Integer.parseInt(ApplicationConfig.getProperty(ApolloConstant.totalPvMax)):250;
            if (localEv>=localEvApollo&&localCtr>=localCtrApollo&&totalPv<=totalPvApollo) {
                isChannelLocalMax = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("{},check isChannelLocalMax ERROR:{}", requestInfo.getUserId(), e);
        }
        return isChannelLocalMax;
    }

    public boolean isChannelLocalMin(RequestInfo requestInfo) {
        boolean isChannelLocalMin = false;
        try {
            UserModel userModel = requestInfo.getUserModel();
            ChannelCtr channelCtr=userModel.getChannelCtrM();
            if(channelCtr==null){
                return isChannelLocalMin;
            }
            int localEv=channelCtr.getLocal_ev();
            int localPv=channelCtr.getLocal_pv();
            int totalPv=channelCtr.getTotal_pv();
            DebugUtil.debugLog(requestInfo.isDebugUser(),"{} isChannelLocalMin localEv:{},localPv:{},totalPv:{}",requestInfo.getUserId(),localEv,localPv,totalPv);
            int localEvApollo=StringUtils.isNotBlank(ApplicationConfig.getProperty(ApolloConstant.localEvMin))?
                    Integer.parseInt(ApplicationConfig.getProperty(ApolloConstant.localEvMin)):15;
            int totalPvApollo=StringUtils.isNotBlank(ApplicationConfig.getProperty(ApolloConstant.totalPvMin))?
                    Integer.parseInt(ApplicationConfig.getProperty(ApolloConstant.totalPvMin)):5;
            if (localEv>=localEvApollo&&localPv<=1&&totalPv>=totalPvApollo) {
                isChannelLocalMin = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("{},check isChannelLocalMin ERROR:{}", requestInfo.getUserId(), e);
        }
        return isChannelLocalMin;
    }


    public boolean isChannelLocalNew(RequestInfo requestInfo) {
        boolean isChannelLocalNew = false;
        try {
            UserModel userModel = requestInfo.getUserModel();
            ChannelCtr channelCtr=userModel.getChannelCtrM();
            if(channelCtr==null){
                return isChannelLocalNew;
            }
            int localEv=channelCtr.getLocal_ev();
            int localPv=channelCtr.getLocal_pv();
            int totalPv=channelCtr.getTotal_pv();
            DebugUtil.debugLog(requestInfo.isDebugUser(),"{} isChannelLocalNew localEv:{},localPv:{},totalPv:{}",requestInfo.getUserId(),localEv,localPv,totalPv);
            if (localEv>=15&&localPv<=1&&totalPv>=5) {
                isChannelLocalNew = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("{},check isChannelLocalNew ERROR:{}", requestInfo.getUserId(), e);
        }
        return isChannelLocalNew;
    }

    public boolean isChannelHotMax(RequestInfo requestInfo) {
        boolean isChannelHotMax = false;
        try {
            UserModel userModel = requestInfo.getUserModel();
            ChannelCtr channelCtr=userModel.getChannelCtrM();
            if(channelCtr==null){
                return isChannelHotMax;
            }
            int hotEv=channelCtr.getHot_ev();
            double hotCtr=channelCtr.getHot_ctr();
            double totalCtr=channelCtr.getTotal_ctr();
            int hotPv=channelCtr.getHot_pv();
            DebugUtil.debugLog(requestInfo.isDebugUser(),"{} isChannelHotMax hotEv:{},hotCtr:{},totalCtr:{},hotPv:{}",requestInfo.getUserId(),hotEv,hotCtr,totalCtr,hotPv);
            int hotEvMinApollo=StringUtils.isNotBlank(HotApplicationConfig.getProperty(ApolloConstant.Hot_ev_min))?
                    Integer.parseInt(HotApplicationConfig.getProperty(ApolloConstant.Hot_ev_min)):50;
            int hotEvMaxApollo=StringUtils.isNotBlank(HotApplicationConfig.getProperty(ApolloConstant.Hot_ev_max))?
                    Integer.parseInt(HotApplicationConfig.getProperty(ApolloConstant.Hot_ev_max)):350;
            int hotPvMinApollo=StringUtils.isNotBlank(HotApplicationConfig.getProperty(ApolloConstant.hot_pv_min))?
                    Integer.parseInt(HotApplicationConfig.getProperty(ApolloConstant.hot_pv_min)):10;
            double hotCtrMinApollo=StringUtils.isNotBlank(HotApplicationConfig.getProperty(ApolloConstant.Hot_ctr_min))?
                    Double.parseDouble(HotApplicationConfig.getProperty(ApolloConstant.Hot_ctr_min)):0.133;
            double totalCtrMaxApollo=StringUtils.isNotBlank(HotApplicationConfig.getProperty(ApolloConstant.Total_ctr_max))?
                    Double.parseDouble(HotApplicationConfig.getProperty(ApolloConstant.Total_ctr_max)):0.35;

            if (hotEv>=hotEvMinApollo&&hotEv<=hotEvMaxApollo&&hotCtr>=hotCtrMinApollo
                    &&hotPv>=hotPvMinApollo&&totalCtr<=totalCtrMaxApollo) {
                isChannelHotMax = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("{},check isChannelHotMax ERROR:{}", requestInfo.getUserId(), e);
        }
        return isChannelHotMax;
    }

    public boolean isChannelHotMin(RequestInfo requestInfo) {
        boolean isChannelHotMin = false;
        try {
            UserModel userModel = requestInfo.getUserModel();
            ChannelCtr channelCtr=userModel.getChannelCtrM();
            if(channelCtr==null){
                return isChannelHotMin;
            }
            int hotEv=channelCtr.getHot_ev();
            int totalPv=channelCtr.getTotal_pv();
            int hotPv=channelCtr.getHot_pv();
            DebugUtil.debugLog(requestInfo.isDebugUser(),"{} isChannelHotMin hotEv:{},totalPv:{},hotPv:{}",requestInfo.getUserId(),hotEv,totalPv,hotPv);
            int hotEvMinApollo=StringUtils.isNotBlank(HotApplicationConfig.getProperty(ApolloConstant.Hot_ev_min))?
                    Integer.parseInt(HotApplicationConfig.getProperty(ApolloConstant.Hot_ev_min)):50;
            int hotEvMaxApollo=StringUtils.isNotBlank(HotApplicationConfig.getProperty(ApolloConstant.Hot_ev_max))?
                    Integer.parseInt(HotApplicationConfig.getProperty(ApolloConstant.Hot_ev_max)):350;
            int hotPvMaxApollo=StringUtils.isNotBlank(HotApplicationConfig.getProperty(ApolloConstant.Hot_pv_max))?
                    Integer.parseInt(HotApplicationConfig.getProperty(ApolloConstant.Hot_pv_max)):5;
            int totalPvMinApollo=StringUtils.isNotBlank(HotApplicationConfig.getProperty(ApolloConstant.Total_pv_min))?
                    Integer.parseInt(HotApplicationConfig.getProperty(ApolloConstant.Total_pv_min)):12;

            if (hotEv>=hotEvMinApollo && hotEv<=hotEvMaxApollo && hotPv<=hotPvMaxApollo && totalPv>=totalPvMinApollo) {
//            if (hotEv>=hotEvMinApollo) {
                isChannelHotMin = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("{},check isChannelHotMin ERROR:{}", requestInfo.getUserId(), e);
        }
        return isChannelHotMin;
    }


    public boolean isChannelHotInterest(RequestInfo requestInfo) {
        boolean isChannelHotInterest = false;
        try {
            UserModel userModel = requestInfo.getUserModel();
            ChannelCtr channelCtr=userModel.getChannelCtrM();
            if(channelCtr==null){
                return isChannelHotInterest;
            }
            int hotEv=channelCtr.getHot_ev();
            DebugUtil.debugLog(requestInfo.isDebugUser(),"{} isChannelHotInterest hotEv:{}",requestInfo.getUserId(),hotEv);

            if ( hotEv >= 13 && hotEv < 27 ) {
                isChannelHotInterest = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("{},check isChannelHotInterest ERROR:{}", requestInfo.getUserId(), e);
        }
        return isChannelHotInterest;
    }


    /**
     * 精品池 文章 内容 需要 减少 （用户对精品池文章无感）
     * 实验人群：jppool_ev∈[40,400] && jppool_ctr∈[0,0.07] && total_pv∈[14,+∞)
     * @param requestInfo requestInfo
     * @return int 概率数  [0, 100] 代表出 精品池的概率数
     */
    public int getProbabilityForJpPoolToReduce(RequestInfo requestInfo) {
        int probabilityNum = 100; //初始化 100 代表 不减少精品池文章出的 概率
        try {
            UserModel userModel = requestInfo.getUserModel();
            ChannelCtr channelCtr = userModel.getChannelCtrM();
            if(channelCtr == null){
                return probabilityNum; //判断字段 为空 不降权
            }
            int jppool_ev = channelCtr.getJppool_ev();
            int totalPv = channelCtr.getTotal_pv();
            double jppool_ctr = channelCtr.getJppool_ctr();

            if (jppool_ev >= 40 && jppool_ev <= 400 && jppool_ctr <= 0.07 && totalPv >= 14) {
                if (jppool_ev < 90 && jppool_ctr < 0.025){
                    probabilityNum = 25;    //jppool_ev∈[40,90) && jppool_ctr∈[0,0.025) && total_pv∈[14,+∞)
                }else if (jppool_ev >= 90 && jppool_ctr < 0.025){
                    probabilityNum = 10;    //jppool_ev∈[90,400] && jppool_ctr∈[0,0.025) && total_pv∈[14,+∞)
                }else if (jppool_ev < 90 && jppool_ctr >= 0.025){
                    probabilityNum = 70;    //jppool_ev∈[40,90) && jppool_ctr∈[0.025,0.07] && total_pv∈[14,+∞)
                }else if (jppool_ev >= 90 && jppool_ctr >= 0.025){
                    probabilityNum = 60;    //jppool_ev∈[90,400] && jppool_ctr∈[0.025,0.07] && total_pv∈[14,+∞)
                }else{
                    probabilityNum = 99; //万一漏掉的 给 99 方便排查
                }
            }

            DebugUtil.debugLog(requestInfo.isDebugUser(),"{} getProbabilityForJpPoolToReduce, probabilityNum:{}, jppool_ev:{}, totalPv:{}, jppool_ctr:{}", requestInfo.getUserId(), probabilityNum, jppool_ev, totalPv, jppool_ctr);

        } catch (Exception e) {
            logger.error("{}, check getProbabilityForJpPoolToReduce ERROR: {}", requestInfo.getUserId(), e.toString(), e);
        }
        return probabilityNum;
    }

    public boolean isShareUser(RequestInfo requestInfo){
        boolean isflag=false;
        try{
            String uid=requestInfo.getUserId();
            UserModel userModel=requestInfo.getUserModel();
            if((userModel==null||StringUtils.isBlank(userModel.getLastUtime()))&&StringUtil.is_alpha(uid)){
                isflag=true;
            }
        }catch (Exception e){
            logger.error("{} isShareUser error:{}",requestInfo.getUserId(),e);
        }
        return isflag;
    }

    public boolean isH5User(RequestInfo requestInfo){
        boolean isflag=false;
        try{
            String proid=requestInfo.getProid();
            if(StringUtils.isNotBlank(proid)&&proid.equals("ifengnewsh5")){
                isflag=true;
            }
        }catch (Exception e){
            logger.error("{} isShareUser error:{}",requestInfo.getUserId(),e);
        }
        return isflag;
    }

    public boolean isNoModelLiteUser(RequestInfo requestInfo){
        boolean isflag=false;
        try{
            UserModel userModel=requestInfo.getUserModel();
            if((userModel==null||StringUtils.isBlank(userModel.getLastUtime()))&&requestInfo.getProid().equals(ProidType.ifengnewslite.getValue())){
                isflag=true;
            }
        }catch (Exception e){
            logger.error("{} isNoModelLiteUser error:{}",requestInfo.getUserId(),e);
        }
        return isflag;
    }

    public static boolean isFirstScreen(RequestInfo requestInfo){
        try{
            String operation=requestInfo.getOperation();
            if(org.apache.commons.lang.StringUtils.isNotBlank(operation) && GyConstant.operation_Default.equals(operation) && requestInfo.getPullNum() == 0){
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
            logger.error("{} isFirstScreen error:{}",requestInfo.getUserId(),e);
        }

        return false;
    }

    public static boolean isPullDownOpr(RequestInfo requestInfo){
        try{
            String operation=requestInfo.getOperation();
            if(org.apache.commons.lang.StringUtils.isNotBlank(operation) && GyConstant.operation_PullDown.equals(operation)){
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
            logger.error("{} isPullDownOpr error:{}",requestInfo.getUserId(),e);
        }

        return false;
    }


    /**
     * 判断是否是 第一次上拉 或 下刷
     * @param requestInfo requestInfo
     * @return boolean
     */
    public static boolean isFirstPullPage(RequestInfo requestInfo){
        String operation=requestInfo.getOperation();
        return RecomChannelEnum.headline.getValue().equals(requestInfo.getRecomChannel()) && 1 == requestInfo.getPullNum() && requestInfo.getPullCount() <= 1;
    }

    public static List<RecordInfo> weightFilter(List<RecordInfo> recordInfoList){
        if(CollectionUtils.isEmpty(recordInfoList)){
            return new ArrayList<>();
        }
        List<RecordInfo> results=new ArrayList<>();
        for(RecordInfo recordInfo:recordInfoList){
            if(recordInfo.getWeight()>0.5){
                results.add(recordInfo);
            }
        }
        return results;
    }

    /**
     * 是否是当天的进入的用户
     * @return
     */
    public static boolean isFirstInToday(UserModel userModel) {
        boolean isFirstIn = false;
        try {
            String firstIn = "";
            if(userModel!=null){
                if(StringUtils.isNotBlank(userModel.getFirst_in())){
                    firstIn = userModel.getFirst_in();
                }
            }
            //画像为空 或者  画像不为空  但 firstIn是当天 或者 为空则认为是新增用户
            if(userModel==null||
                    (userModel!=null&&!userModel.isTimeOut()&&(firstIn.equals(new SimpleDateFormat("yyyyMMdd").format(new Date()))||StringUtils.isBlank(firstIn)))){
                logger.warn("{} firstIn is:{},isTimeOut:{}", userModel.getUserId(), firstIn,userModel.isTimeOut());
                isFirstIn = true;
            }

        } catch (Exception e) {
            logger.error("{},check isFirstInToday ERROR:{}", userModel.getUserId(), e);
        }
        return isFirstIn;
    }


    /**
     * 是否是主版本头条频道
     * @param requestInfo
     * @return
     */
    public static boolean isIfengNewHead(RequestInfo requestInfo){
        if(StringUtils.isNotBlank(requestInfo.getProid())&&StringUtils.isNotBlank(requestInfo.getRecomChannel())&&ProidType.ifengnews.getValue().equals(requestInfo.getProid()) && RecomChannelEnum.headline.getValue().equals(requestInfo.getRecomChannel())){
           return true;
        }
        return false;
    }

    /**
     * 校验是否是某个版本
     * @param requestInfo
     * @param proid
     * @return
     */
    public static boolean isGenericProid(RequestInfo requestInfo,String proid) {
        try {
            if(requestInfo!=null&&StringUtils.isNotBlank(requestInfo.getProid())&&proid.equals(requestInfo.getProid())){
                return  true;
            }
        } catch (Exception e) {
            logger.error("{},check isGenericProid ERROR:{}", requestInfo.getUserId(), e);
        }
        return false;
    }

    public static void main(String[] args) {
    }
}