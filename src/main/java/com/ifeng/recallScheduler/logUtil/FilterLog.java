package com.ifeng.recallScheduler.logUtil;

import com.ifeng.recallScheduler.item.Document;
import com.ifeng.recallScheduler.request.RequestInfo;
import com.ifeng.recallScheduler.utils.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * Created by wupeng1 on 2017/7/13.
 * 推荐响应日志
 * 吐给客户端，本地备案自查
 */
public class FilterLog {

    private static final Logger logger = LoggerFactory.getLogger(FilterLog.class);

    public static void info(String format, Object... argArray) {
        logger.info(format, argArray);
    }


    /**
     * 打过滤日志 写到 filterLog 文件夹下  最终通过 filebeat 同步到 日志中心
     * @param requestInfo requestInfo
     * @param document document
     * @param recomReason recomReason
     * @param filterReason filterReason
     */
    public static void toFileBeat(RequestInfo requestInfo, Document document, String recomReason, String filterReason) {

        try{
            HashMap<String, Object> logMap = new HashMap<>();
            //用户相关
            logMap.put("uid", requestInfo.getUserId());
            logMap.put("proid", requestInfo.getProid());
            //文章相关
            logMap.put("docId", document.getDocId());
            logMap.put("simId", document.getSimId());
            logMap.put("title", document.getTitle());
            logMap.put("docType", document.getDocType());
            logMap.put("source", document.getSource());
            //过滤相关
            logMap.put("recomReason", recomReason);
            logMap.put("filterReason", filterReason);

            logger.info("{} needFilter, detailedJson: {}", requestInfo.getUserId(), JsonUtil.object2jsonWithoutException(logMap));
        }catch (Exception e){
            logger.error("toFileBeat err:{}", e.toString(), e);
        }

    }


}
