package com.ifeng.recallScheduler.threadUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by jibin on 2017/5/23.
 */
public class BaseThreadPool {
    private static final Logger logger = LoggerFactory.getLogger(BaseThreadPool.class);

    /**
     * 用户个性化推荐的hbase的查询线程池
     */
/*
   public static ExecutorService THREAD_POOL_UpdateIndex = null;

   static {
        THREAD_POOL_UpdateIndex = Executors.newFixedThreadPool(50, new BaseThreadFactory("updateIndex"));
        logger.info("updateIndex pool init succeed! thread nums: " + 10);
    }*/




    public static ExecutorService THREAD_POOL_UpdateIndex = new ThreadPoolExecutor(20, 55, 1000L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(200), new ThreadPoolExecutor.DiscardOldestPolicy());

    public static ExecutorService REALTIMEPROFILE_THREAD_POOL = new ThreadPoolExecutor(20, 55, 1000L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(200), new ThreadPoolExecutor.DiscardOldestPolicy());

    //内容画像定时刷新线程池，线上一般每2s刷一次，固定线程数避免大批量请求
    public static ExecutorService DOCUMENT_UPDATE_THREAD_POOL = new ThreadPoolExecutor(1, 1,1000L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(2), new ThreadPoolExecutor.DiscardOldestPolicy());
}
