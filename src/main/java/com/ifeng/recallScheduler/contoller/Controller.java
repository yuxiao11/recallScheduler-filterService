package com.ifeng.recallScheduler.contoller;
//import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.ifeng.recallScheduler.constant.GyConstant;
import com.ifeng.recallScheduler.constant.cache.CacheFactory;
import com.ifeng.recallScheduler.enums.PtypeName;
import com.ifeng.recallScheduler.filterController.service.impl.FilterService;
import com.ifeng.recallScheduler.item.Document;
import com.ifeng.recallScheduler.request.RequestInfo;
import com.ifeng.recallScheduler.support.RequestSupport;
import com.ifeng.recallScheduler.timer.TimerEntity;
import com.ifeng.recallScheduler.timer.TimerEntityUtil;
import com.ifeng.recallScheduler.utils.DebugUtil;
import com.ifeng.recallScheduler.utils.DocUtil;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ifeng.recallScheduler.preloadDocument2Cache.LoadDocument2cache;

import java.util.*;

/**
 * @Auther: yuxiao
 */

@RestController
public class Controller {
    private final static Logger logger = LoggerFactory.getLogger(Controller.class);

    @Autowired
    private RequestSupport requestSupport;

    @Autowired
    private LoadDocument2cache loadDocument2cache;

    @Autowired
    private FilterService filterService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private DocUtil docUtil;


    /**
     *
     * @param params  引擎或者微服务传递的参数 包括
     *
     * requestInfo.isDebugUser()		是否为Debug用户
     * requestInfo.isColdUser()			是否为冷启动用户
     * requestInfo.getUserTypeMap()		标记当前用户的用户类型
     * requestInfo.getUserModel()		获取用户画像
     * requestInfo.getUserId()			获取uid
     * requestInfo.getTitleThreshold()	标题敏感词过滤阈值，默认值6过滤
     * requestInfo.getRecomChannel()	区分头条频道流量和推荐频道流量
     * requestInfo.getPullNum()			default 分页请求
     * requestInfo.getPullCount()		引擎内自己记录的用户上下拉次数，有清零逻辑
     * requestInfo.getProid()			渠道标识
     * requestInfo.getOperation()		操作类型：上滑、下滑
     * requestInfo.getIsSpecialUser()	判断是否特殊用户
     * requestInfo.getNegMaps()			负反馈map
     *
     * ————————————————————————————————————————————————————
     *
     * @param docId 过滤服务涉及到的 文章信息 包括
     * doc.getWhy()			      解释描述(recommend/additional(数目不够，补充)等  //
     * doc.getTitle()			  标题
     * doc.getTimeSensitive()	  时效性
     * doc.getSpecialParam()	  Json类型特殊参数 每种类型document都要透传
     * doc.getSource()			  站内外来源
     * doc.getSimId()			  聚类id
     * doc.getReadableFeatures()  特征词  //
     * doc.getPartCategoryExt()	  过滤使用的扩展信息，例如世界杯内容
     * doc.getPartCategory()	  分类扩展信息，例如小视频
     * doc.getOnLine()			  文章是否在线        //
     * doc.getMediaId()			  媒体id
     * doc.getLocMap()			  地理位置信息
     * doc.getIsIfengVideo()	  是否是凤凰卫视内容
     * doc.getGroupId()			  新的聚合id，用于替换simId
     * doc.getDocType()			  文章类型 (slide/video/doc/hdSlide)
     * doc.getDocId()			  文章ID      //
     * doc.getCategory()		  文章C类别
     *
     * @return
     */
    @HystrixCommand(fallbackMethod = "getFallback")
    @RequestMapping(value = "/schedulerFilter/list", method = RequestMethod.GET)
    public String resultList(@RequestParam(value = "params", required = true) Map<String, String> params,
                             @RequestParam(value = "docId" , required = false) String docId) {

        TimerEntity timer = TimerEntityUtil.getInstance();
        String uid = null;
        List<Document> result = new ArrayList<>();

        try{

            /**
             * 将文章初始化到本地并进行持久化
             */
            loadDocument2cache.init();

            /**
             * 初始化requestInfo信息
             */
            RequestInfo requestInfo = requestSupport.preRequest(params, PtypeName.HeadLine.getValue());
            uid = requestInfo.getUserId();

            //根据docid 从数据库中获取Document对象并进行
            ArrayList docIds = new ArrayList(Arrays.asList(docId.split(",")));

            /**
             * 对传入参数进行过滤
             */

            //焦点图
            Set<String> focusSimids = filterService.getFocusSimIdSet();
            //固定位置的过滤simid集合
            Set<String> regularSimids = filterService.getRegularSimIdSet();
            //三俗
            Set<String> sansuSimIdSet = filterService.getSanSuSimIdSet(requestInfo);


            //获取文章内容
            Cache docCache = cacheManager.getCache(CacheFactory.CacheName.PersonalRecomDocumentInfo.getValue());

            docUtil.checkUpdateDoc(docIds);

            Document doc;

            for (Object id: docIds) {
                doc = docUtil.getDocByCache(docCache, (String) id);
                if (doc == null) {
                    logger.info("UserLastServiceImpl fillDocCarousel cache doc is null!!!, uid:{},docid:{}", requestInfo.getUserId(), id);
                    continue;
                }

                //过滤 TODO 此处需要根据专家系统添加配置 增加参数Map 对各个通道进行控制
                if (filterService.needFilter(requestInfo, doc, focusSimids, regularSimids, sansuSimIdSet, GyConstant.needLocalFilter_false, null)) {
                    DebugUtil.debugLog(requestInfo.isDebugUser(), "{} UserLastServiceImpl fillDocCarousel common needFilter {},{}", requestInfo.getUserId(), doc.getDocId(), doc.getTitle());
                    continue;
                }
                result.add(doc);

            }


            timer.addTime("EngineRecallSize",1L,result.size()+1);
            TimerEntityUtil.remove();

        } catch (Exception e) {
            logger.error("uid:{} update error:{}", uid, e);
        }
        return "Finish!!!!!!!!!!!";
    }

    public String getFallback(@RequestParam(value = "params", required = true) Map<String, String> params,
                              @RequestParam(value = "docId" , required = false) String docId)  {
        return "Fail!!!!!!!!!!!!!!!";
    }

}
