package com.ifeng.recallScheduler.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 记录精品池确实日志
 */
public class TagLackLogUtil {

    private static final Logger logger = LoggerFactory.getLogger(TagLackLogUtil.class);


    /**
     * 记录精品池确实日志
     * @param format
     * @param argArray
     */
    public static void log(String format, Object... argArray) {
        logger.info(format, argArray);
    }
}
