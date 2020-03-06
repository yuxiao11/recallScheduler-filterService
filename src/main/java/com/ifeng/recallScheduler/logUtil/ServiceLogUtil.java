package com.ifeng.recallScheduler.logUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 调用其他服务的耗时日志
 * Created by jibin on 2017/7/6.
 */
public class ServiceLogUtil {

    private static final Logger logger = LoggerFactory.getLogger(ServiceLogUtil.class);





    /**
     * 记录用户数据更新的详细日志
     * @param format
     * @param argArray
     */
    public static void debug(String format, Object... argArray) {
        logger.info(format, argArray);
    }
}
