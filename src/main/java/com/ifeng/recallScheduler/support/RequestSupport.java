package com.ifeng.recallScheduler.support;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.ifeng.recallScheduler.constant.*;
import com.ifeng.recallScheduler.enums.ProidType;
import com.ifeng.recallScheduler.logUtil.StackTraceUtil;
import com.ifeng.recallScheduler.request.RequestInfo;
import com.ifeng.recallScheduler.timer.TimerEntity;
import com.ifeng.recallScheduler.timer.TimerEntityUtil;
import com.ifeng.recallScheduler.user.UserNegs;
import com.ifeng.recallScheduler.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.Booleans;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by wupeng1 on 2017/6/27.
 * 请求参数解析辅助类
 * 客户端传参 uid operation city size proid lastDoc publishid nw gv
 */
@Service
public class RequestSupport {

    static Logger logger = LoggerFactory.getLogger(RequestSupport.class);

    @Autowired
    private UserNegs userNegs;

    @Autowired
    private RequestSupport requestSupport;

    @Autowired
    private SpecialFilterUserUtil specialFilterUserUtil;


    /**
     * 预处理，初始化 RequestInfo，并输出访问日志
     *
     * @param params
     * @param ptype
     * @return
     * @throws Exception
     */
    public RequestInfo preRequest(Map<String, String> params, String ptype) throws Exception {
        TimerEntity timer = TimerEntityUtil.getInstance();
        RequestInfo requestInfo;

        timer.addStartTime("init");
        requestInfo = requestSupport.initRequest(params, ptype);
        timer.addEndTime("init");

        timer.addStartTime("updateSpecial");
        specialFilterUserUtil.updateSpecialUserCache(requestInfo);
        timer.addEndTime("updateSpecial");


        return requestInfo;
    }


    public RequestInfo initRequest(Map<String, String> params, String ptype) throws Exception {

        String useridParam = params.get("uid");
        RequestInfo requestInfo = new RequestInfo().setUserId(useridParam); //TODO RecallScheduler
        try {
            // TODO: 2017/6/29 日志打印请求参数
            TimerEntity timer = TimerEntityUtil.getInstance();
            String operation = params.get("operation");
            operation = StringUtils.isBlank(operation) ? "pullDown" : operation;
            checkInput(useridParam, operation);  //TODO RecallScheduler

            boolean isDebugUser = DebugUtil.isDebugUser(useridParam); //TODO RecallScheduler


            /**
             * 直接通过参数传递UserGroup信息
             */

            String userGroup =Strings.nullToEmpty(params.get("userGroup")); //TODO 通过参数传递 用户群信息

            /**
             * 通过参数进行传递 city信息
             */

            String city = Strings.nullToEmpty(params.get("city")); //TODO 通过参数进行传递 city信息

            /**
             * 通过参数传递 常住地
             */
            String permanentLoc = Strings.nullToEmpty(params.get("permanentLoc")); //TODO 通过参数传递 常住地


            /**
             * 添加省份信息
             */
            String province = getDecode(Strings.nullToEmpty(params.get("province")));

            /**
             * 添加渠道标识
             */
            String proid = Strings.nullToEmpty(params.get("proid")); //TODO RecallScheduler

            /**
             * 直接由引擎添加是否为冷启动用户标识
             */
            boolean isColder = Booleans.parseBoolean(params.get("isColder"),false); //TODO RecallScheduler

            /**
             * 直接由引擎添加
             */
            int pullCount = StringUtils.isEmpty(params.get("pullCount")) ? -1 : Integer.parseInt(params.get("pullCount")); //TODO RecallScheduler


            String recomChannel = Strings.nullToEmpty(params.get("recomChannel"));  //TODO RecallScheduler

            int pullNum = StringUtils.isEmpty(params.get("pullNum")) ? -1 : Integer.parseInt(params.get("pullNum"));  //TODO RecallScheduler

            timer.addStartTime("negs");
            Map<String, List<String>> negs = userNegs.getNegMaps(useridParam);  //TODO RecallScheduler
            timer.addEndTime("negs");

            Instant now = Instant.now();
            String sid = RTokenGenerator.uuidWithoutHyphen();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS");
            LocalDateTime localDateTime = LocalDateTime.ofInstant(now, ZoneId.of(ZoneId.SHORT_IDS.get("CTT")));
            String nowStr = formatter.format(localDateTime);

            requestInfo.setSid(sid)
                    .setCity(city)
                    .setProvince(province)
                    .setOperation(operation)
                    .setProid(proid)
                    .setRecTime(nowStr)
                    .setNegMaps(negs)
                    .setPullNum(pullNum)
                    .setRtoken(UUID.randomUUID().toString())
                    .setCurrDate(DateUtils.getCurrDateTimeStr())
                    .setDebugUser(isDebugUser)
                    .setRecomChannel(recomChannel)
                    .setPtype(ptype)
                    .setPermanentLoc(permanentLoc)
                    .setColdUser(isColder)
                    .setPullCount(pullCount)
                    .setPermanentLoc(permanentLoc)
                    .setUserGroup(userGroup);

            requestInfo.setPullCount(pullCount);

            specialFilterUserUtil.updateAndCheckTitleFilterWhiteNotWxb(requestInfo); //TODO RecallScheduler

            DebugUtil.debugLog(requestInfo.isDebugUser(), "{} lastDoc:{} " +
                    "LastDocList:{} initNewLastDoc :{}", requestInfo.getUserId(), JsonUtil.object2jsonWithoutException(requestInfo.getLastDoc()), JsonUtil.object2jsonWithoutException(requestInfo.getLastDocList()), JsonUtil.object2jsonWithoutException(requestInfo.getNewLastDocList()));

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("init ERROR:{}", StackTraceUtil.getStackTrace(e));
        }

        // debug 用户 打印 requestInfo 日志
        DebugUtil.debugLog(requestInfo.isDebugUser(), "{}, initRequest requestInfo result: {}", requestInfo.getUserId(), JsonUtil.object2jsonWithoutException(requestInfo));

        return requestInfo;
    }

//

    private String getDecode(String input) throws UnsupportedEncodingException {
        if (input == null || input.isEmpty()) return null;
        String outPut = URLDecoder.decode(input, "UTF-8");
        return outPut;
    }


    /**
     * 验证输入参数合法性
     *
     * @param useridParam
     * @param operation
     * @throws Exception
     */
    private void checkInput(String useridParam, String operation) throws Exception {
        Preconditions.checkArgument(StringUtils.isNotEmpty(useridParam), String.format("uid %s is invalid", useridParam));

        Preconditions.checkArgument(isOperation(operation), String.format("operation %s is invalid", operation));
    }

    private boolean isOperation(String operation) {
        return "pullUp".equals(operation) || "pullDown".equals(operation) || "default".equals(operation);
    }

    public boolean isGv6_1_0(RequestInfo requestInfo) {
        try {
            String verStr = requestInfo.getGv().replace(".", "");
            int number = Integer.parseInt(verStr);
            if(StringUtils.isEmpty(verStr)){
                return false;
            }
            if (number >= 610) {
                return true;
            }
        } catch (Exception e) {
            logger.error("parse uid:{} version gv:{} error:{}", requestInfo.getUserId(), requestInfo.getGv(), StackTraceUtil.getStackTrace(e));
        }
        return false;
    }

    public boolean isGv6_3_0(RequestInfo requestInfo) {
        try {
            String verStr = requestInfo.getGv().replace(".", "");
            int number = Integer.parseInt(verStr);
            if(StringUtils.isEmpty(verStr)){
                return false;
            }
            if (number >= 630) {
                return true;
            }
        } catch (Exception e) {
            logger.error("parse uid:{} version gv:{} error:{}", requestInfo.getUserId(), requestInfo.getGv(), StackTraceUtil.getStackTrace(e));
        }
        return false;
    }

    public boolean isGv6_5_2(RequestInfo requestInfo) {
        try {
            String verStr = requestInfo.getGv().replace(".", "");
            if(StringUtils.isEmpty(verStr)){
                return false;
            }
            int number = Integer.parseInt(verStr);
            if (number >= 652) {
                return true;
            }
        } catch (Exception e) {
            logger.error("parse uid:{} version gv:{} error:{}", requestInfo.getUserId(), requestInfo.getGv(), StackTraceUtil.getStackTrace(e));
        }
        return false;
    }

    /**
     * 判断是否是ios
     *
     * @param requestInfo
     * @return
     */
    public static boolean isIos(RequestInfo requestInfo) {
        String os = requestInfo.getOs();
        boolean isIos = false;
        if (StringUtils.isBlank(os)) {
            return isIos;
        }

        try {
            isIos = requestInfo.getOs().toLowerCase().contains(GyConstant.ios);
        } catch (Exception e) {
            logger.error("parse uid:{},{} isIos error:{}", requestInfo.getUserId(), requestInfo.getOs(), StackTraceUtil.getStackTrace(e));
        }
        return isIos;
    }


    private boolean isFromHeadLine(String from) {
        return !Strings.isNullOrEmpty(from) && from.contains("ifengnews");
    }

    /**
     * 判断是否需要获得焦点图
     *
     * @param requestInfo
     * @return
     */
    public boolean needFocus(RequestInfo requestInfo) {
        //return requestInfo.getPullNum() == 0; TODO:焦点图需求
        if (GyConstant.operation_Default.equals(requestInfo.getOperation()) && requestInfo.getPullNum() == 0) {
            //专业版 冷启动用户 ab 测试
            if (ProidType.ifengnewsvip.getValue().equals(requestInfo.getProid()) && requestInfo.isColdUser()) {
                long checkNum = MathUtil.getNumByUid(requestInfo.getUserId(), AbTestConstant.Abtest_Group_ColdUserVipFocus);
                if (checkNum < 50) {
                    requestInfo.addAbtestInfo(AbTestConstant.Abtest_Group_ColdUserVipFocus, AbTestConstant.Abtest_ExpFlag_ColdUserVipFocus_test_rate50); //测试 50% 删除 焦点图
                    return false;
                } else {
                    requestInfo.addAbtestInfo(AbTestConstant.Abtest_Group_ColdUserVipFocus, AbTestConstant.Abtest_ExpFlag_ColdUserVipFocus_base_rate50);
                    return true;
                }
            } else if (ProidType.ifengnewsdiscovery.getValue().equals(requestInfo.getProid()) && requestInfo.isColdUser()) {  //探索版 冷启动用户 ab 测试
                long checkNum = MathUtil.getNumByUid(requestInfo.getUserId(), AbTestConstant.Abtest_Group_ColdUserDiscoveryFocus);
                if (checkNum < 50) {
                    requestInfo.addAbtestInfo(AbTestConstant.Abtest_Group_ColdUserDiscoveryFocus, AbTestConstant.Abtest_ExpFlag_ColdUserDiscoveryFocus_test_rate50); //测试 50% 删除 焦点图
                    return false;
                } else {
                    requestInfo.addAbtestInfo(AbTestConstant.Abtest_Group_ColdUserDiscoveryFocus, AbTestConstant.Abtest_ExpFlag_ColdUserDiscoveryFocus_base_rate50);
                    return true;
                }
            } else {
                return true; //其他客户端安原有逻辑 return true
            }
        } else {
            return false;
        }

    }

//    /**
//     * 解析编辑固定位信息
//     * @param requestInfo
//     */
//    public void parseEditorList(RequestInfo requestInfo, Map<String, String> params){
//        String editorStr = params.get(BizRequestKey.EditorList.getValue());
//        if(StringUtils.isNotBlank(editorStr)){
//            //固定位去掉效果ab测试
//            UserModel userModel = requestInfo.getUserModel();
//            if(userModel!=null && StringUtils.isNotBlank(userModel.getUserGroup())){
//                if(userModel.getUserGroup().contains("三线城市") || userModel.getUserGroup().contains("四线城市") || userModel.getUserGroup().contains("五线城市")){
//                    long checkNum = MathUtil.getNumByUid(requestInfo.getUserId(), AbTestConstant.Abtest_RegularDisplay_Test);
//                    if(checkNum < 50){
//                        requestInfo.addAbtestInfo(AbTestConstant.Abtest_RegularDisplay_Test, AbTestConstant.Abtest_RegularDisplay_rate50_base);
//                    }else {
//                        requestInfo.addAbtestInfo(AbTestConstant.Abtest_RegularDisplay_Test, AbTestConstant.Abtest_RegularUnDisplay_rate50_test);
//                    }
//                }
//            }
//
//            //因为固定位比较特殊 留一个活口 方便处理若想要恢复固定位 可即刻恢复
//            if (requestInfo.isColdUser() || debugUserConfig.getDebugUser(ApolloConstant.coldSpecial_white_key).contains(requestInfo.getUserId())) {
//                if(UserUtils.isFirstInToday(requestInfo.getUserModel())&&requestInfo.isFirstAccess()&&!specialFilterUserUtil.isBeiJingUserOrWxb(requestInfo)){
//                    requestInfo.setColdRandom(0);
//                }
//                //debug 用户直接走不出固定位逻辑
//                if(debugUserConfig.getDebugUser(ApolloConstant.coldSpecial_white_key).contains(requestInfo.getUserId())){
//                    requestInfo.setColdRandom(0);
//                }
//            }
//
//            //push 及 deeplink 返回流 不出编辑 固定位逻辑
//            if (GyConstant.REF_PUSH.equals(requestInfo.getRef()) || GyConstant.REF_DEEPLINK.equals(requestInfo.getRef())){
//                requestInfo.setColdRandom(0);
//            }
//
//            //攒 固定位 数据
//            List<Map<String,Object>> editorList = JsonUtil.json2Object(editorStr, new TypeToken<List<Map<String,Object>>>(){}.getType());
//            requestInfo.setEditorList(editorList);
//            int supplySize = editorListSupport.getSupplySize(requestInfo);
//            if(supplySize>0){
//                int size = supplySize + requestInfo.getSize();
//                requestInfo.setSize(size);
//            }
//        }
//    }


    /**
     * 解析用户请求中的lastdoc
     * <p>
     * 不要用这样的存储结构！！！
     * 当前频道内最近的5个点击，前三位图文，后两位，先进先出，最左边的是最开始点击，后面逐个追加，安卓用户退出客户端情况，ios过期时间1天
     * 格式：（<simid,docid,time,类别，来源>|<simid,docid,time,类别，来源>|<simid,docid,time,类别，来源>|<simid,docid,time,类别，来源>|<simid,docid,time,类别，来源>）
     *
     * @param requestInfo
     */
//    public void parseLastDoc(RequestInfo requestInfo) {
//        TimerEntity timer = TimerEntityUtil.getInstance();
//        String lastDocStr = requestInfo.getLastDoc();
//        if (StringUtils.isBlank(lastDocStr) || GyConstant.lastDoc_Default.equals(lastDocStr)) {
//            return;
//        }
//        Cache docCache = cacheManager.getCache(CacheFactory.CacheName.PersonalRecomDocumentInfo.getValue());
//        List<String> ids2Query = Lists.newArrayList();
//
//        //不区分类型存储
//        List<LastDocBean> lastDocList = Lists.newArrayList();
//        try {
//
//            lastDocStr = lastDocStr.replace(GyConstant.Symb_Less, "").replace(GyConstant.Symb_Greater, "");
//            String[] lastdocArray = lastDocStr.split("\\|");
//
//            for (String tmp : lastdocArray) {
//                String[] docArray = tmp.split(GyConstant.Symb_Comma);
//
//                int length = docArray.length;
//                if (length < 2) {
//                    continue;
//                }
//
//                String simId = docArray[0];
//                String docid = docArray[1];
//
//                String time = "";
//                String clickType = "";
//                String channle = "";
//                String clickTimeStr = "";
//
//                if (length >= 3) {
//                    time = docArray[2];
//                }
//                if (length >= 4) {
//                    clickType = docArray[3];
//                }
//
//                if (length >= 6){
//                    channle = docArray[4]; // 点击文章 所在频道
//                    clickTimeStr = docArray[5]; // 点击文章时间
//                }
//
//                //时长较少认为是误点，不记录
//                int time_view = 0;
//                if (StringUtils.isNotBlank(time)) {
//                    try {
//                        time_view = Integer.valueOf(time);
//                        if (time_view < GyConstant.lastDocTimeLimit) {
//                            DebugUtil.debugLog(requestInfo.isDebugUser(), "{},LastDocFilter time:{}", requestInfo.getUserId(), tmp);
//                            continue;
//                        }
//
//                    } catch (Exception e) {
//                        logger.error("{} lastDoc:{} Parse time:{} ERROR:,{}", requestInfo.getUserId(), tmp, time, StackTraceUtil.getStackTrace(e));
//                    }
//                }
//
//                //时长较少认为是误点，不记录，视频联播单独处理
//                if (LastDocTypeEnum.splb.getValue().equals(clickType)) {
//                    if (time_view < GyConstant.lastDocTimeLimit_other) {
//                        DebugUtil.debugLog(requestInfo.isDebugUser(), "{},LastDocFilter splb:{}", requestInfo.getUserId(), tmp);
//                        continue;
//                    }
//                }
//
//
//                if (StringUtils.isBlank(docid)) {
//                    continue;
//                }
//                docid = docid.replace(GyConstant.IKV_Prefix, "");
//                docid = docid.replace(GyConstant.IKV_Prefix_UCMS, "");
//
//                Document doc = docUtil.getDocByCache(docCache, docid);
//
//
//                if (StringUtils.isBlank(simId)) {
//                    if (doc != null) {
//                        simId = doc.getSimId();
//                        //视频传了guid和simid，不用补足，但是第一次doc查询可能查不到，后面推晓伟改成docid
//                    } else {
//                        ids2Query.add(docid);
//                        //客户端回传的视频docid不是我们的docid，造成后面的正反馈查询不到，所以需要我们自己查一下更新缓存
//                    }
//                }
//
//
//                //下掉 将正反馈过滤逻辑交给 通道内部判断 不前置 避免效果出不来
////                if (StringUtils.isNotBlank(simId)) {
////                    if (filterService.needFilterSansuSimId(requestInfo, simId, sansuSimIdSet) &&
////                            StringUtils.isBlank(DocPosiHotCache.getDocEvent(simId))) {
////                        logger.info("{} parseLastDoc filter sansu:{}", requestInfo.getUserId(), simId);
////                        continue;
////                    }
////                }
////
////                if (doc != null) {
////                    if (sansuFilter.titleFilter(requestInfo, doc) && StringUtils.isBlank(DocPosiHotCache.getDocEvent(simId))) {
////                        logger.info("{} {} parseLastDoc filter titleFilter:{}, title:{}", requestInfo.getUserId(), simId, doc.getDocId(), doc.getTitle());
////                        continue;
////                    }
////                }
//
//
//                LastDocBean tmpDoc = new LastDocBean();
//                tmpDoc.setDocId(docid);
//                tmpDoc.setSimId(simId);
//                tmpDoc.setTime(time);
//
//                // 判断点击文章 时间 是否合法
//                if ( Long.parseLong(clickTimeStr + "000") <  System.currentTimeMillis()){
//                    tmpDoc.setClickTime(Long.parseLong(clickTimeStr + "000")); // set 点击时间
//                }
//
//                if (doc != null) {
//                    //更新lastdoc中的c的信息
//                    tmpDoc.setCList(doc.getDevCList());
//                    tmpDoc.setScList(doc.getDevScList());
//                    tmpDoc.setLdaTopicList(doc.getLdaTopicList());
//                }
//
//                lastDocList.add(tmpDoc);
//            }
//
//
//            //ios lastDoc 左侧最新，需要反转统一顺序
//            if (isIosUser(requestInfo)) {
//                Collections.reverse(lastDocList);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            logger.error("parseLastDoc {} ERROR:{}", lastDocStr, StackTraceUtil.getStackTrace(e));
//
//        }
//
//        if (ids2Query.size() > 0) {
//            //查询hbase,并更新cache
//            timer.addStartTime("updateLast1");
//            docUtil.updateDocCacheBatchLimit(ids2Query);
//            timer.addEndTime("updateLast1");
//            DebugUtil.debugLog(requestInfo.isDebugUser(), "{},lastdoc ids2Query size:{}", requestInfo.getUserId(), ids2Query.size());
//
//            //查询habse，更新缓存后再次更新simid
//            Document doc;
//            for (LastDocBean tmpDoc : lastDocList) {
//                doc = docUtil.getDocByCache(docCache, tmpDoc.getDocId());
//                DebugUtil.debugLog(requestInfo.isDebugUser(), "{},lastdoc ids2Query doc:{}", requestInfo.getUserId(), JsonUtil.object2jsonWithoutException(doc));
//                //simid为空则补足
//                if (doc != null) {
//                    if (StringUtils.isBlank(tmpDoc.getSimId())) {
//                        tmpDoc.setSimId(doc.getSimId());
//                    }
//
//                    //更新lastdoc中的c的信息
//                    tmpDoc.setCList(doc.getDevCList());
//                    tmpDoc.setScList(doc.getDevScList());
//                    tmpDoc.setLdaTopicList(doc.getLdaTopicList());
//                }
//            }
//        }
//
//        requestInfo.setLastDocList(lastDocList);
//
//        if (requestInfo.isDebugUser()) {
//            DebugUtil.log("{} lastdoc is:{}", requestInfo.getUserId(), JsonUtil.object2jsonWithoutException(lastDocList));
//        }
//    }


    /**
     * 判断是ios用户
     *
     * @param requestInfo
     * @return
     */
    public boolean isIosUser(RequestInfo requestInfo) {
        //ios用户需要反转lastDoc，ios队列时最新的在最左边
        if (requestInfo == null) {
            return false;
        }
        //判断接口 传送 os 参数
        String os = Strings.nullToEmpty(requestInfo.getOs()).toLowerCase();
        if (os.contains(GyConstant.ios)) {
            return true;
        }

        if ( requestInfo.getUserModel() == null ){
            return false;
        }

        //判断 画像 字段
        String umt = Strings.nullToEmpty(requestInfo.getUserModel().getUmt()).toLowerCase();
        if (umt.contains(GyConstant.iphone) || umt.contains(GyConstant.ipad)) {
            return true;
        }

        return false;
    }

}
