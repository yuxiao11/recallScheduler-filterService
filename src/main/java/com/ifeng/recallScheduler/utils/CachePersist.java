package com.ifeng.recallScheduler.utils;

import com.google.common.cache.Cache;
import com.ifeng.recallScheduler.constant.GyConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 缓存持久化方法
 * Created by jibin on 2018/6/28.
 */
public class CachePersist {


    private final static Logger logger = LoggerFactory.getLogger(CachePersist.class);


    /**
     * 将guava的cache写到磁盘
     * @param cache
     * @param cacheName
     */
    public static void writeToFile(Cache cache, String cacheName) {
        logger.info("start write cache to file, name:{}", cacheName);

        String path = GyConstant.localCacheDir + cacheName;

        try {
            Map map = new HashMap(cache.asMap());
            writeObjToFile(path, map);
            logger.info("write cache to file done, name:{}, size:{}, path:{}", cacheName, map.size(), path);
        } catch (Exception e) {
            logger.error("write cache to file error,name:{}, path:{}", cacheName, path, e.toString(), e);
        }
    }
    public static int loadToCache(Cache cache, String cacheName) {
        logger.info("start load file to cache, name:{}", cacheName);

        String path = GyConstant.localCacheDir  + cacheName;

        if (cache == null) {
            logger.error("load file to cache error, cache is null, name:{} path:{}", cacheName, path);
        }

        Map<Object, Object> map = (Map<Object, Object>) readObjFromFile(path);

        if (map != null) {
            for (Map.Entry<Object, Object> entry : map.entrySet()) {
                try {
                    cache.put(entry.getKey(), entry.getValue());
                } catch (Exception e) {
                    logger.error("doc preload", e);
                }
            }

            logger.info("load file to cache done, name:{} size:{}", cacheName, map.size());
            return map.size();
        }else{
            return 0;
        }
    }

    /**
     * 将cache写到文件中
     * @param path
     * @param fileObj
     */
    private static void writeObjToFile(String path, Object fileObj) {
        File file = new File(path);
        FileOutputStream fileOutputStream = null;
        ObjectOutputStream objectOutputStream = null;

        try {
            File fileParent = file.getParentFile();
            if(!fileParent.exists()){
                fileParent.mkdirs();
            }
            file.createNewFile();
            fileOutputStream = new FileOutputStream(file);
            objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(fileObj);

        } catch (IOException e) {
            logger.error("write obj to file error, path:{}", path, e.toString(), e);
        } finally {
            try {
                if (objectOutputStream != null) {
                    objectOutputStream.flush();
                    objectOutputStream.close();
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                logger.error("write obj to file error, path:{}", path, e.toString(), e);
            }
        }
    }


    /**
     * 成本机读取文件加载到cache中
     * @param path
     * @return
     */
    public static Object readObjFromFile(String path) {
        File file = new File(path);
        if(file==null||!file.exists()){
            return null;
        }

        Object fileObj = null;
        FileInputStream fileInputStream;
        ObjectInputStream objectInputStream = null;

        try {
            fileInputStream = new FileInputStream(file);
            objectInputStream = new ObjectInputStream(fileInputStream);
            fileObj = objectInputStream.readObject();

        } catch (IOException | ClassNotFoundException e) {
            logger.error("path:{} error:{}", path, e);
        } finally {
            try {
                if (objectInputStream != null) {
                    logger.info("load file to cache done,path:{}", path);
                    objectInputStream.close();
                }
            } catch (IOException e) {
                logger.error("", e);
            }
        }

        return fileObj;
    }

    public static void main(String[] args) {
        /**
         * 网信办用户的uid
         */
//         Cache<String, String> testCache = CacheBuilder
//                 .newBuilder()
//                 .recordStats()
//                 .concurrencyLevel(10)
//                 .expireAfterWrite(300, TimeUnit.SECONDS)
//                 .initialCapacity(200000)
//                 .maximumSize(200000)
//                 .build();
//
//        testCache.put("aaa","aaa");
//        testCache.put("bbb","bbb");
//
//        writeToFile(testCache, "test");




//        Map<String, String> doc1 = testCache.asMap();
//        System.out.println(doc1.toString());
    }

}
