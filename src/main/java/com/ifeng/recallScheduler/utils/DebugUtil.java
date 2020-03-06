package com.ifeng.recallScheduler.utils;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.google.common.collect.Sets;

import com.ifeng.recallScheduler.apolloConf.ApolloConstant;
import com.ifeng.recallScheduler.constant.GyConstant;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * 对debug用户将输入详细日志
 * Created by jibin on 2017/7/6.
 */
@Service
public class DebugUtil {

    private static final Logger logger = LoggerFactory.getLogger(DebugUtil.class);

    /**
     * 张阳debug用户redis 地址，配置中心更新后同步
     */
    private static final String host = "10.90.1.56";

    /**
     * 张阳debug用户redis 端口，配置中心更新后同步
     */
    private static final int port  = 6379;

    /**
     * 张阳debug用户redis set key值，配置中心更新后同步
     */
    private static final String debug_User_Key = "app_debugids";

    /**
     * 张阳debug用户redis db值，配置中心更新后同步
     */
    private static final int db = 1;


    /**
     * debug用户的集合
     */
    private static Set<String> debugUserSet = Sets.newHashSet();


    /**
     * 初始化debug用户集合
     */
    public void init(Config config){
        try {
            String yml = config.getProperty(ApolloConstant.Log_DebugUser_Key, "");
            //初始化debug用户
            loadLogDebugUsers(yml);
            write2Redis();
        }catch(Exception e){
            logger.error("init Log Debug User Error: {}",e );
        }
    }

    /**
     * 监听debug用户修改后更新用户集合
     */
    public void onChangeJob(ConfigChangeEvent configChangeEvent){
        //判断property是否更新
        if(!configChangeEvent.changedKeys().contains(ApolloConstant.Log_DebugUser_Key)){
            return;
        }

        ConfigChange configChange = configChangeEvent.getChange(ApolloConstant.Log_DebugUser_Key);
        String yml = configChange.getNewValue();
        if(StringUtils.isBlank(yml)){
            return;
        }
        //更新debug用户
        loadLogDebugUsers(yml);
        write2Redis();
    }


    /**
     * 判断是否是debug用户
     *
     * @param uid
     */
    public static boolean isDebugUser(String uid) {
        boolean isdebugUser = false;
        if (StringUtils.isNotBlank(uid)) {
            isdebugUser = debugUserSet.contains(uid) || uid.startsWith(GyConstant.debugUidFlag);
        }

        return isdebugUser;
    }


    /**
     * 对debug用户将输入详细日志
     * @param isDebugUser
     * @param format
     * @param argArray
     */
    public static void debugLog(boolean isDebugUser, String format, Object... argArray) {
        if (isDebugUser) {
            logger.info(format, argArray);
        }
    }

    public static void debugLazyJsonDesLog(boolean isDebugUser, String format, Object... argArray) {
        debugLazyDesLog(isDebugUser, format, JsonUtil::object2jsonWithoutException, argArray);
    }

    /**
     * 对debug用户将输入详细日志
     * @param isDebugUser
     * @param format
     * @param argArray
     */
    public static void debugLazyDesLog(boolean isDebugUser, String format, Function<Object, String> noBaseTypeMapper, Object... argArray) {
        if (isDebugUser) {
            Object[] arr = new Object[argArray.length];

            for(int i =0 ;i < argArray.length; i++) {
                Object o = argArray[i];
                if (o instanceof Number) {
                    arr[i] = o;
                } else if (o instanceof String || o instanceof StringBuilder || o instanceof StringBuffer) {
                    arr[i] = o;
                } else {
                    arr[i] = noBaseTypeMapper.apply(o);
                }
            }
            logger.info(format, arr);
        }
    }

    /**
     * 对debug用户将输入详细日志
     * @param format
     * @param argArray
     */
    public static void log(String format, Object... argArray) {
        logger.info(format, argArray);
    }

    /**
     * 对debug用户将输入详细日志
     * @param format
     * @param argArray
     */
    public static void logError(String format, Object... argArray) {
        logger.error(format, argArray);
    }


    /**
     * 上报 error 到统计 es 标准格式
     * @param className className
     * @param errorMark errorMark
     * @param uid uid
     * @param e e
     */
    public static void errorLogToEs(String className, String errorMark, String uid, Exception e) {
        LoggerFactory.getLogger(Error.class).error("statisticsError, class: {}, errMark: {}, ip: {}, uid: {}, exceptionCause: {},  exceptionToString: {}", className, errorMark, GyConstant.linuxLocalIp, uid, e.getCause(), e.toString());
    }

    /**
     * 将配置中心debug用户写到 Redis 里
     */
    public static void write2Redis(){

        //将debug用户配置写入redis
        if(debugUserSet!=null && debugUserSet.size()>0){
            RedisUtil.addSet(host,port,db,debug_User_Key,debugUserSet);
        }
    }

    public void loadLogDebugUsers(String yml) {
        Yaml yaml = new Yaml();
        try {
            if (StringUtils.isBlank(yml)) {
                logger.warn("parse debug user configuration empty! ");
            }
            Object obj = yaml.load(yml);
            Map<String, Object> userMap = (Map) obj;

            //获取yml中debug_log_users配置的uid
            List<Object> list = (List<Object>) userMap.get(ApolloConstant.DebugLog_User_Key);

            Set<String> updateUserSet = Sets.newHashSet();
            list.forEach(x -> updateUserSet.add(String.valueOf(x)));
            debugUserSet = updateUserSet;

        } catch (Exception e) {
            logger.error("Parse LogDebug user configuration error: {}", e);
        }
    }
}
