package com.ifeng.recallScheduler.filterAssemble;

import com.google.gson.reflect.TypeToken;
import com.ifeng.recallScheduler.apolloConf.ApolloConstant;
import com.ifeng.recallScheduler.apolloConf.ApplicationConfig;
import com.ifeng.recallScheduler.constant.AbTestConstant;
import com.ifeng.recallScheduler.constant.GyConstant;
import com.ifeng.recallScheduler.constant.RecWhy;
import com.ifeng.recallScheduler.constant.cache.CacheFactory;
import com.ifeng.recallScheduler.constant.cache.SourceInfoDataUtil;
import com.ifeng.recallScheduler.enums.ProidType;
import com.ifeng.recallScheduler.item.Document;
import com.ifeng.recallScheduler.item.EditorInsertItem;
import com.ifeng.recallScheduler.request.RequestInfo;
import com.ifeng.recallScheduler.rule.impl.WeMediaSourceFilterHandler;
import com.ifeng.recallScheduler.utils.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 针对特殊用户群里进行更加严格的过滤
 * Created by jibin on 2018/1/10.
 */
@Service
public class SpecialFilterHandler {

    private final static Logger log = LoggerFactory.getLogger(SpecialFilterHandler.class);

    @Autowired
    private SourceInfoDataUtil sourceInfoDataUtil;

    @Autowired
    private VideoSourceInfoDataUtil videoSourceInfoDataUtil;

    @Autowired
    private SpecialFilterUserUtil specialFilterUserUtil;

    @Autowired
    private DocUtil docUtil;

    @Autowired
    private WeMediaSourceFilterHandler weMediaSourceFilterHandler;

    @Autowired
    private EhCacheUtil ehCacheUtil;

    @Autowired
    private UserActionUtil userActionUtil;

//    public static LoadingCache<String, String> DocIsIfengVideoCache = CacheBuilder.newBuilder()
//            .maximumSize(100000)
//            .expireAfterWrite(5, TimeUnit.HOURS)
//            .build(
//                    new CacheLoader<String, String>() {
//                        public String load(String key) throws Exception {
//                            return "";
//                        }
//                    }
//            );


    /**
     * 判断改用户是否需要过滤小视频
     * @param requestInfo
     * @param doc
     * @return
     */
    public boolean needFilterMiniVideo(RequestInfo requestInfo, Document doc) {
        boolean needFilter = false;
        if (DocUtil.isVideo(doc) && DocUtil.isMiniVideo(doc)) {
            //头条流 推荐流和视频流不出小视频
            if (FlowTypeUtils.isIfengnewsHeadLine(requestInfo)||FlowTypeUtils.isRecom(requestInfo)||FlowTypeUtils.isVideoChannel(requestInfo)) {
                return true;
            }

        }
        return needFilter;
    }


    /**
     * 判断该用户是否需要过滤趣头条视频
     * @param requestInfo
     * @param doc
     * @return
     */
    public boolean needQuTTFilter(RequestInfo requestInfo, Document doc) {
        boolean needFilter = false;
        Map<String, Boolean> userTypeMap = requestInfo.getUserTypeMap();
        Boolean needQttFilter = (userTypeMap.get(GyConstant.needQttFilter));
        if (needQttFilter == null) {
            return true;
        }

        if (needQttFilter && DocUtil.isQuTT(doc)) {
            needFilter = true;
        }
        return needFilter;
    }



    /**
     * 快头条只出SAB或者精品池内容
     * @return
     */
    public boolean needKttFilterSAB(RequestInfo requestInfo, Document doc){
        if(FlowTypeUtils.isIfengnewsGold(requestInfo) && FlowTypeUtils.isIfengnewsHeadLine(requestInfo)){
            String sourceLevel = Optional.ofNullable(getSourceLevel(doc)).orElse(doc.getSourceLevel());
            if(GyConstant.sourceLevel_S.equals(sourceLevel)
                    || GyConstant.sourceLevel_A.equals(sourceLevel)
                    || GyConstant.sourceLevel_B.equals(sourceLevel)
                    || "jppool".equals(doc.getDisType())){
                return false;
            }
            return true;
        }
        return false;
    }


    /**
     * 探索版图谱分类过滤
     * @param requestInfo
     * @param doc
     * @return
     */
    public boolean needFilterHealth(RequestInfo requestInfo, Document doc){
        //此处添加过滤条件 调用方可以不用另添加 取消限制也只需取消此处
        if(!isGenericProid(requestInfo,ProidType.ifengnewsdiscovery.getValue())||
                requestInfo.checkAbtestInfo(AbTestConstant.Abtest_Graph_Test, AbTestConstant.Graph_test_rate5)){
            return false;
        }

        String graphFilterColl= org.apache.commons.lang3.StringUtils.isNotBlank(ApplicationConfig.getProperty(ApolloConstant.graphFilterColl))?
                ApplicationConfig.getProperty(ApolloConstant.graphFilterColl):"";
        if(StringUtils.isNotBlank(graphFilterColl)){
            if(doc.getCategory()!=null && !doc.getCategory().isEmpty()) {
                for(String cate:doc.getCategory()){
                    if(graphFilterColl.contains(cate)) {
                        DebugUtil.debugLog(requestInfo.isDebugUser(), "{} docId:{} graph filter C:{}", requestInfo.getUserId(), doc.getDocId(), doc.getCategory());
                        return true;
                    }
                }
            }

            if(StringUtils.isNotBlank(doc.getC()) && graphFilterColl.contains(doc.getC())) {
                DebugUtil.debugLog(requestInfo.isDebugUser(), "{} docId:{} graph filter C:{}", requestInfo.getUserId(), doc.getDocId(), doc.getC());
                return true;
            }
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
            if(requestInfo!=null&& org.apache.commons.lang3.StringUtils.isNotBlank(requestInfo.getProid())&&proid.equals(requestInfo.getProid())){
                return  true;
            }
        } catch (Exception e) {
            log.error("{},check isGenericProid ERROR:{}", requestInfo.getUserId(), e);
        }
        return false;
    }



    /**
     * 探索版图谱试探标签过滤
     * @param requestInfo
     * @param exp
     * @return
     */
    public boolean needFilterHealthTag(RequestInfo requestInfo, String exp){
        //此处添加过滤条件 调用方可以不用另添加 取消限制也只需取消此处
        if(!isGraphFilterProid(requestInfo)|| requestInfo.checkAbtestInfo(AbTestConstant.Abtest_Graph_Test, AbTestConstant.Graph_test_rate5)){
            return false;
        }
        String graphFilterColl= org.apache.commons.lang3.StringUtils.isNotBlank(ApplicationConfig.getProperty(ApolloConstant.graphFilterExpTag))?
                ApplicationConfig.getProperty(ApolloConstant.graphFilterExpTag):"";
        if(StringUtils.isNotBlank(graphFilterColl)){
            if(StringUtils.isNotBlank(exp) && graphFilterColl.contains(exp)) {
                DebugUtil.debugLog(requestInfo.isDebugUser(), "{} graph filter exp:{}", requestInfo.getUserId(), exp);
                return true;
            }
        }
        return false;
    }


    public boolean isGraphFilter(String recomReason){
        if(StringUtils.isNotBlank(recomReason) && recomReason.equals(RecWhy.whyGraphOut)){
            return false;
        }

        if (StringUtils.isNotBlank(recomReason) && (recomReason.contains(RecWhy.ReasonUserSub)||recomReason.contains("bidding"))) {
            return false;
        }
        return true;
    }

    public boolean isGraphFilterProid(RequestInfo requestInfo){
        try {
            String graphFilterProid= org.apache.commons.lang3.StringUtils.isNotBlank(ApplicationConfig.getProperty(ApolloConstant.graphFilterProid))?
                    ApplicationConfig.getProperty(ApolloConstant.graphFilterProid):"ifengnewsdiscovery";
            String[] graphArr=graphFilterProid.split(GyConstant.Symb_Comma);
            if(requestInfo!=null&& org.apache.commons.lang3.StringUtils.isNotBlank(requestInfo.getProid())){
                for(String graphProid:graphArr){
                    if(graphProid.equals(requestInfo.getProid())){
                        return true;
                    }
                }
            }
        }catch (Exception e){
            log.error("{} isGraphFilterProid error:{}",requestInfo.getUserId(),e);
        }
        return false;
    }



    /**
     * 只展现媒体评级为机构媒体的
     * 不出自媒体新闻
     *  限定前几刷-可配置
     * @param requestInfo
     * @param doc
     * @return
     */
    public boolean needFilterSourceOnlyShowJiGou(RequestInfo requestInfo, Document doc, String recomReason) {
        int limitJGCount=StringUtils.isNotBlank(ApplicationConfig.getProperty(ApolloConstant.limitJGCount))?
                Integer.parseInt(ApplicationConfig.getProperty(ApolloConstant.limitJGCount)):3;
        //限制前几刷之内出机构
        if(requestInfo.getPullCount()>limitJGCount){
            return false;
        }

        //视频不过滤机构
        if (docUtil.isVideo(doc)) {
            return false;
        }

        //用户订阅内容不用过滤媒体级别，其他通道都需要过滤
        if (RecWhy.ReasonUserSub.equals(recomReason)) {
            return false;
        }

        //精品池帖子 热点帖子 short不过滤机构
        if (docUtil.isJpPool(doc) || RecWhy.ReasonHotTagInsert.equals(recomReason)||docUtil.isShort(doc)) {
            return false;
        }


        Map<String, Boolean> userTypeMap = requestInfo.getUserTypeMap();
        boolean isJiGoutWhite = userTypeMap.get(GyConstant.isJiGoutWhite);
        if (isJiGoutWhite) {
            return false;
        }


        boolean needFilter = false;
        //持续查redis，避免cache为空
        boolean isSpecialUser = specialFilterUserUtil.isWxbUser(requestInfo);



        needFilter = needFilterSource_Jigou(doc);


        if (isSpecialUser || requestInfo.isDebugUser()) {
            String sourceLevel = getSourceLevel(doc);
            log.info("ShowJiGou needFilter :{}, {},isWxbUser:{},pullCount:{}, {},type:{},title:{},source:{},sourceLevel:{},Lv:{}", needFilter, requestInfo.getUserId(),
                    isSpecialUser, requestInfo.getPullCount(), doc.getDocId(), doc.getDocType(), doc.getTitle(), doc.getSource(), sourceLevel, doc.getSourceLevel());
        }
        return needFilter;
    }




    /**
     * 除本地通道出的天气外 其余全部过滤掉天气
     * @param requestInfo
     * @param doc
     * @param recomReason
     * @return
     */
    public boolean needFilterTianqi(RequestInfo requestInfo, Document doc, String recomReason) {
        try{
            if(StringUtils.isBlank(recomReason)){
                return false;
            }
            if(StringUtils.isBlank(doc.getReadableFeatures())){
                return false;
            }
            //图谱不过滤
            if(recomReason.equals(RecWhy.whyGraphOut)){
                return false;
            }
            if(doc.getReadableFeatures().contains("天气")){
                return true;
            }
        }catch (Exception e){
            log.error("uid:{} needFilterTianqi error:{}",requestInfo.getUserId(),e);
        }
        return false;
    }


    /**
     *
     * 判断是否只出高质量账号内容
     * @param requestInfo
     * @param doc
     * @return
     */
    public boolean needFilterHighQualitySource(RequestInfo requestInfo, Document doc) {
            if (StringUtils.isBlank(doc.getSpecialParam())) {
                return true;
            } else {
                try {
                    String specialParamJson = doc.getSpecialParam();
                    Map<String, Object> specialParamMap = JsonUtil.json2Object(specialParamJson, new TypeToken<Map<String, Object>>() {
                    }.getType());
                    if (specialParamMap.containsKey("extData")) {
                        String extDataJson = String.valueOf(Optional.ofNullable(specialParamMap.get("extData")).orElse(""));
                        Map<String, Object> extDataMap = JsonUtil.json2Object(extDataJson, new TypeToken<Map<String, Object>>() {
                        }.getType());

                        //测试组仅推荐 specialParam->extData->highQuality = 1 的文章
                        Object highQualityObj = extDataMap.get("highQuality");
                        if (highQualityObj != null && highQualityObj instanceof Double && highQualityObj.equals(1.0d)) {
                            return false;
                        }
                    }
                } catch (Exception e) {
                    log.error("uid:{} parse docId:{} high quality source err:{}", requestInfo.getUserId(), doc.getDocId(), e);
                }
            }
            return true;
    }
    public boolean needFilterRegular(RequestInfo requestInfo,Document doc) {
        try{
            boolean flag=false;
            int pullNum=requestInfo.getPullNum();
            String action=StringUtils.isNotBlank(requestInfo.getOperation())?requestInfo.getOperation():"";
            String simId= StringUtils.isNotBlank(doc.getSimId())?doc.getSimId():"";
            String title= StringUtils.isNotBlank(doc.getTitle())?doc.getTitle():"";
            String docid=StringUtils.isNotBlank(doc.getDocId())?doc.getDocId():"";
            List<EditorInsertItem> editorRegularList = (List<EditorInsertItem>) ehCacheUtil.getListEditor(CacheFactory.CacheName.EditorRegularPosition.getValue(), CacheFactory.CacheName.EditorRegularNew.getValue());
            editorRegularList = CollectionUtils.isEmpty(editorRegularList) ? Collections.EMPTY_LIST : editorRegularList;
            Set<Integer> regularPullNum = editorRegularList.stream().map(x -> x.getPullnum()).collect(Collectors.toSet()); //第几刷的哪些位置
            Set<String> regularTitle = new HashSet<>();
            Set<String> regularSimids = new HashSet<>();
            Set<String> regularIds = new HashSet<>();
            //判断该刷是否包含固定位
            if(regularPullNum.contains(pullNum)){
                if(pullNum==0&&action.equals(GyConstant.operation_Default)){ //上滑 下滑或者default
                    flag=true;
                }else if(action.equals(GyConstant.operation_PullUp)){
                    flag=true;
                }
            }
            DebugUtil.debugLog(requestInfo.isDebugUser(),"{} needFilterRegular, docid:{} pullNum:{} action:{} flag:{} regularPullNum:{}",requestInfo.getUserId(),docid,pullNum,action,flag,JsonUtil.object2jsonWithoutException(regularPullNum));

            if(flag&&CollectionUtils.isNotEmpty(editorRegularList)){
               for(EditorInsertItem editorInsertItem:editorRegularList){
                   List<EditorInsertItem.HourEditorInsert> hourEditorInserts=new ArrayList<>();
                   try{
                       //判断固定位simid和title和id与文章是否有重复
                       if(editorInsertItem.getPullnum()==pullNum){
                           hourEditorInserts=editorInsertItem.getHourEditorInsert(); //24小时聚合类
                           if(CollectionUtils.isNotEmpty(hourEditorInserts)){
                               regularSimids =hourEditorInserts.stream().map(x -> x.getSimId()).collect(Collectors.toSet());
                               if(regularSimids.contains(simId)){
                                   DebugUtil.debugLog(requestInfo.isDebugUser(),"{} simid needFilterRegular 24h Or topic simid:{}, docid:{},type:{},title:{}",requestInfo.getUserId(),doc.getSimId(),docid,doc.getDocType(),doc.getTitle());
                                   return true;
                               }
                               regularIds = hourEditorInserts.stream().map(x -> x.getId()).collect(Collectors.toSet());
                               if(regularIds.contains(docid)){
                                   DebugUtil.debugLog(requestInfo.isDebugUser(),"{} id needFilterRegular 24h Or topic  simid:{}, docid:{},type:{},title:{}",requestInfo.getUserId(),doc.getSimId(),docid,doc.getDocType(),doc.getTitle());
                                   return true;
                               }
                               regularTitle = hourEditorInserts.stream().map(x -> x.getTitle()).collect(Collectors.toSet());
                               if(regularTitle.contains(title)){
                                   DebugUtil.debugLog(requestInfo.isDebugUser(),"{} title needFilterRegular 24h Or topic  simid:{}, docid:{},type:{},title:{}",requestInfo.getUserId(),doc.getSimId(),docid,doc.getDocType(),doc.getTitle());
                                   return true;
                               }
                           }else{
                               String editorSimId= StringUtils.isNotBlank(editorInsertItem.getSimId())?editorInsertItem.getSimId():"";
                               String editorTitle= StringUtils.isNotBlank(editorInsertItem.getTitle())?editorInsertItem.getTitle():"";
                               String editorDocid=StringUtils.isNotBlank(editorInsertItem.getId())?editorInsertItem.getId():"";
                               if(editorSimId.equals(simId)||editorTitle.equals(title)||editorDocid.equals(docid)){
                                   DebugUtil.debugLog(requestInfo.isDebugUser(),"{} needFilterRegular simid:{}, docid:{},type:{},title:{}",requestInfo.getUserId(),doc.getSimId(),doc.getDocId(),doc.getDocType(),doc.getTitle());
                                   return true;
                               }
                           }
                       }
                   }catch (Exception e){
                       log.error("{} hourEditorInserts:{} editorInsertItem:{} error:{}",requestInfo.getUserId(),JsonUtil.object2jsonWithoutException(hourEditorInserts),JsonUtil.object2jsonWithoutException(editorInsertItem),e);
                   }


                   //判断是否设置上限 如有则判断是否超过上限  TODO 即同一篇文章只能出现在固定位上2次
                   try{
                       if(editorInsertItem.getEvLimit()>0){  //get userSession 获取pullNum
                           int idNum=StringUtils.isNotBlank(userActionUtil.getUserSessionShort(requestInfo,editorInsertItem.getId()))?Integer.parseInt(userActionUtil.getUserSessionShort(requestInfo,editorInsertItem.getId())):0;
                           if(idNum>editorInsertItem.getEvLimit()){
                               DebugUtil.debugLog(requestInfo.isDebugUser(),"{} EvLimit needFilterRegular simid:{}, docid:{},type:{},title:{},evLimit:{}",requestInfo.getUserId(),doc.getSimId(),doc.getDocId(),doc.getDocType(),doc.getTitle(),editorInsertItem.getEvLimit());
                               return true;
                           }else{
                               userActionUtil.updateSessionNoHis(requestInfo,editorInsertItem.getId(),idNum+1+"");
                           }
                       }
                   }catch (Exception e){
                       log.error("{} editorInsertItem.getEvLimit error:{}",requestInfo.getUserId(),e);
                   }
               }
            }
        }catch (Exception e){
            log.error("uid:{} needFilterRegular error:{}",requestInfo.getUserId(),e);
        }
        return false;
    }

    public boolean isRegularTestUser(RequestInfo requestInfo){
         if(ApolloConstant.Switch_on.equals(ApplicationConfig.getProperty(ApolloConstant.regularSwitch))){
             return true;
         }
         return false;
    }
        /**
         * 过滤掉媒体评级为E的媒体发文
         *
         * @param requestInfo
         * @param doc
         * @return
         */
    public boolean needFilterSourceLevel_E(RequestInfo requestInfo, Document doc) {

        String sourceLevel = getSourceLevel(doc);

        //媒体级别为空或者媒体级别不等于E，不进行过滤
        if (!GyConstant.sourceLevel_E.equals(sourceLevel)) {
            return false;
        }

        return true;
    }


    /**
     * 北上广一线城市按审核标签过滤掉文章
     * @param requestInfo
     * @param document
     * @return
     */
    public boolean needFilterByAuditTag(RequestInfo requestInfo, Document document){
        boolean isBsgUser = specialFilterUserUtil.isBSGUser(requestInfo); //判断是否为BSG用户
        try{
            if(isBsgUser){
                String specialParam = document.getSpecialParam();
                if(StringUtils.isNotBlank(specialParam)){
                    Map<String,String> jsonMap = JsonUtil.json2ObjectWithoutException(specialParam,Map.class);
                    String auditTag = jsonMap.get("reportMessage");
                    if(StringUtils.isNotBlank(auditTag) && (auditTag.contains("暴力血腥") || auditTag.contains("三俗") ||
                            auditTag.contains("色情") || auditTag.contains("政治敏感"))){
                        return true;
                    }

                    //应视频运营方请求  北京排凤凰cut 自动剪裁视频 内容
                    if(specialFilterUserUtil.isBeiJingUserOrWxb(requestInfo)){
                        String cpName=jsonMap.get("cpName");
                        if(DocUtil.isVideo(document)&&StringUtils.isNotBlank(cpName) &&
                                cpName.equals("凤凰外包")){
                            return true;
                        }
                    }
                }
            }
        }catch (Exception e){
            log.error("{} needFilterByAuditTag error {}",requestInfo.getUserId(),e);
        }

        return false;
    }

//    /**
//     * 编辑特殊标记：“社会负面”、“特殊管控”不上首屏
//     * 注：截止2019-10-12日，仅热点有此需求，其他内容暂不考虑，也即：热点文章包含社会负面、特殊管控的，将统一挪至第三条之后出
//     * 另：理论上只要包含“特殊管控”的就可以覆盖大部分的同类场景，如果后续有其他额外标记处理需求时，可以考虑做成可apollo配置的变量
//     * @param requestInfo
//     * @param document
//     * @return
//     */
//    public boolean needFilterByEditTag(RequestInfo requestInfo, Document document) {
//        try{
//            if (document != null&&StringUtils.isNotBlank(document.getSpecialParam())) {
//                String specialParam = document.getSpecialParam();
//                String specialLogo=HotApplicationConfig.getProperty(ApolloConstant.specialLogo);
//                if(StringUtils.isNotBlank(specialLogo)){
//                    String[] specialLogoArr=specialLogo.split(GyConstant.Symb_Pound);
//                    for(String specialLogoItem:specialLogoArr){
//                        if(specialParam.contains(specialLogoItem)){
//                            return true;
//                        }
//                    }
//                }
//            }
//        } catch (Exception e) {
//            log.error("{} needFilterByEditTag error {}", requestInfo.getUserId(), e);
//        }
//        return false;
//    }

    /**
     * 针对网信办 及特定城市用户 屏蔽凤凰卫视内容
     * @param requestInfo
     * @param doc
     * @return boolean
     */
    public boolean needFilterIfengVideo(RequestInfo requestInfo, Document doc) {

        boolean needFilter = false;
        //判断是否为 网信办或 特定用户
        Map<String, Boolean> userTypeMap = requestInfo.getUserTypeMap();
        if (userTypeMap.get(GyConstant.isNeedFilterIfengVideoUser) != null && userTypeMap.get(GyConstant.isNeedFilterIfengVideoUser)){ //2019年1月30日 恢复对几个城市屏蔽
//        if (GyConstant.Str_true.equals(requestInfo.getIsSpecialUser())){ //20180706改为只针对网信办用户屏蔽
            //判断文章是否为 凤凰卫视内容
            String docId = doc.getDocId();
            try{
                //查询guava缓存
//                String result = DocIsIfengVideoCache.get(docId);
//                if (GyConstant.Str_true.equals(result)){ //缓存存在 则
//                    needFilter = true;
//                }else{
                    //判断 文章 是否为 卫视内容
                if (GyConstant.isIfengVideo_Yes.equals(doc.getIsIfengVideo())){
                    needFilter = true;
                    //加入缓存
    //                        DocIsIfengVideoCache.put(docId, GyConstant.Str_true);
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("IfengVideoFilterCache {}, ERROR:{}", docId, e);
            }
        }

        return needFilter;
    }


    /**
     * 三刷以内只出机构媒体的 SABC
     *
     * @param doc
     * @return
     */
    private boolean needFilterSource_Jigou(Document doc) {
        //A/S 集合数据媒体屏蔽自媒体新闻 ,直接过滤
        if (weMediaSourceFilterHandler.sourceNeedFilter(doc)) {
            return true;
        }
        return false;
    }


    /**
     * 三刷以内只出机构媒体的 SABC
     *
     * @param doc
     * @param levelLimitSet
     * @return
     */
    private boolean needFilterSourceLv_Jigou(Document doc, Set<String> levelLimitSet) {
        boolean needFilter = false;
        //A/S 集合数据媒体屏蔽自媒体新闻 ,直接过滤
        if (weMediaSourceFilterHandler.sourceNeedFilter(doc)) {
            return true;
        }

        String sourceLevel = getSourceLevel(doc);
        //海云提供的s级稿源
        if (levelLimitSet.contains(sourceLevel)) {
            needFilter = false;
        } else {
            needFilter = true;
        }
        return needFilter;
    }


    /**
     * 三刷以后只出 SAB  不限制机构
     *
     * @param doc
     * @return
     */
    public boolean needFilterSourceLv(Document doc, Set<String> levelLimitSet) {
        boolean needFilter = false;

        String sourceLevel = getSourceLevel(doc);
        //海云提供的s级稿源
        if (levelLimitSet.contains(sourceLevel)) {
            needFilter = false;
        } else {
            needFilter = true;
        }
        return needFilter;
    }


    /**
     * 三刷以后只出 SABC  不限制机构
     *
     * @param doc
     * @return
     */
    private boolean needFilterSourceLvSABC(Document doc) {
        boolean needFilter = false;

        String sourceLevel = getSourceLevel(doc);
        //海云提供的s级稿源
        if (GyConstant.sourceLevel_S.equals(doc.getSourceLevel())
                //查询redis获取的真正媒体评级
                || GyConstant.sourceLevel_S.equals(sourceLevel)
                || GyConstant.sourceLevel_A.equals(sourceLevel)
                || GyConstant.sourceLevel_B.equals(sourceLevel)
                || GyConstant.sourceLevel_C.equals(sourceLevel)) {
            needFilter = false;
        } else {
            needFilter = true;
        }
        return needFilter;
    }

    /**
     * 不考虑用户直接过滤
     *
     * @param doc
     * @return
     */
    public boolean needFilterSourceOnlyShowLvA(Document doc) {
        boolean needFilter = needFilterSourceLvSABC(doc);

        String sourceLevel = getSourceLevel(doc);
        log.info(" needFilter LvABC :{}, {},type:{},title:{},source:{},sourceLevel:{},Lv:{}", needFilter, doc.getDocId(), doc.getDocType(), doc.getTitle(), doc.getSource(), sourceLevel, doc.getSourceLevel());
        return needFilter;
    }

    /**
     * 查询媒体级别
     *
     * @param doc
     * @return
     */
    private String getSourceLevel(Document doc) {
        String sourceLevel = null;

        if(doc==null||StringUtils.isBlank(doc.getSource())){
            return sourceLevel;
        }

        if (docUtil.isVideo(doc)) {
            sourceLevel = videoSourceInfoDataUtil.getVideoSourceLevel(doc.getSource());
            if (StringUtils.isNotBlank(sourceLevel)) {
                doc.setSourceLevel(sourceLevel);
                return sourceLevel;
            }
        }

        sourceLevel = sourceInfoDataUtil.getSourceLevel(doc.getSource());
        if(StringUtils.isNotBlank(sourceLevel)){
            doc.setSourceLevel(sourceLevel);
        }
        return sourceLevel;
    }
}
