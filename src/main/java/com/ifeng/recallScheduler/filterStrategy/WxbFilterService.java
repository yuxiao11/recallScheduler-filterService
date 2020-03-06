package com.ifeng.recallScheduler.filterStrategy;


import com.ifeng.recallScheduler.item.Document;
import com.ifeng.recallScheduler.request.RequestInfo;
import com.ifeng.recallScheduler.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Created by jibin on 2018/9/26.
 */
public class WxbFilterService {
    private static final Logger logger = LoggerFactory.getLogger(WxbFilterService.class);


    private static Date dateLimit = DateUtils.strToDate("2018-09-26 0:0:01");


    /**
     * 下线时间过滤，只针对wxb处罚期间使用
     *
     * @param requestInfo
     * @param doc
     * @return
     */
    public static boolean needTimeFilter(RequestInfo requestInfo, Document doc) {
        return false;

      /*  boolean needTimeFilter = true;

        try {
            Date docDate = doc.getDate();
            needTimeFilter = dateLimit.before(docDate);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("{} needFilterTime {},{}, ERROR:{}", requestInfo.getUserId(), doc.getDocId(), doc.getDate(), e);
        }
        return needTimeFilter;*/
    }

    public static void main(String[] args) {
        Document doc = new Document();
        doc.setDocId("111");
        doc.setDate(DateUtils.strToDate("2018-09-27 0:0:01"));
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setUserId("aaaaa");
        System.out.println(needTimeFilter(requestInfo, doc));
    }

}
