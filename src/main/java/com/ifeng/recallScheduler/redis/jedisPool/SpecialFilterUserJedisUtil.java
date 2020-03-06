package com.ifeng.recallScheduler.redis.jedisPool;


import com.ifeng.recallScheduler.constant.GyConstant;
import com.ifeng.recallScheduler.redis.jedisPool.bean.JedisClient;
import com.ifeng.recallScheduler.utils.JsonUtil;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 本机的redis连接池，处理本机缓存使用
 */
@Service
public class SpecialFilterUserJedisUtil {
    protected final static Logger log = LoggerFactory.getLogger(SpecialFilterUserJedisUtil.class);

    private static AtomicInteger local = new AtomicInteger(0);

    private static final List<JedisPool> jedisPoolList = new ArrayList<JedisPool>();


    //Redis服务器IP
    private static String addr = "10.90.1.56";

    //Redis的端口号
    private static String port = "6379";


    //可用连接实例的最大数目，默认值为8；
    //如果赋值为-1，则表示不限制；如果pool已经分配了maxActive个jedis实例，则此时pool的状态为exhausted(耗尽)。
    private static int maxActive = 300;

    //控制一个pool最少有多少个状态为idle(空闲的)的jedis实例，默认值也是8。
    private static int minIdle = 8;

    //控制一个pool最多有多少个状态为idle(空闲的)的jedis实例，默认值也是8。
    private static int maxIdle = 100;

    //等待可用连接的最大时间，单位毫秒，默认值为-1，表示永不超时。如果超过等待时间，则直接抛出JedisConnectionException；
    private static int maxWait = 200;

    private static int timeOut = 200;

    //在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；
    private static boolean testOnBorrow = true;


    /**
     * 初始化redis集群
     */
    static {
        initRedis();
    }

    public static void initRedis() {
        log.info("start init localhostJedisUtil.initRedis");
        try {
            JedisPoolConfig config = new JedisPoolConfig();

            config.setMaxTotal(maxActive);
            config.setMaxIdle(maxIdle);
            config.setMinIdle(minIdle);
            config.setMaxWaitMillis(maxWait);
            config.setTestOnBorrow(testOnBorrow);
            String[] redisHost = addr.split(GyConstant.Symb_Comma);
            String[] redisPort = port.split(GyConstant.Symb_Comma);
            for (int i = 0; i < redisHost.length; i++) {
                addJedisPool(new JedisPool(config, redisHost[i], Integer.parseInt(redisPort[i]), timeOut));
            }
        } catch (Exception e) {
            log.error("redisInit SpecialFilterUserJedisUtil ERROR:{}", e);
            e.printStackTrace();
        }
    }


    /**
     * 轮播获取jedis客户端
     *
     * @return
     */
    private static JedisClient getJedisClient() {
        int localValue = local.getAndIncrement();
        int localTmpValue = 0;
        try {
            if (localValue < 0) {
                localValue = 0;
                local.set(0);
            }

            Jedis jedis = null;
            boolean isServerOK = false;
            int serverCount = jedisPoolList.size();
            for (int i = 0; i < serverCount; i++) {
                localTmpValue = localValue % serverCount;
                try {
                    jedis = getRedisClient(localTmpValue);
                    isServerOK = true;
                    break;
                } catch (Exception e) {
                    log.error("Localhost getJedisClient error ! connection:redis {} 失败,现已故障转移,{}",
                            localTmpValue, e);
                    localValue++;  //容错
                }
            }
            if (!isServerOK) {
                log.error("Localhost getJedisClient redis-server-all   均为不可用");
                return null;
            }
            JedisClient jesisReturn = new JedisClient();
            jesisReturn.setServerIndex(localTmpValue);
            jesisReturn.setJedis(jedis);
            return jesisReturn;
        } catch (Exception e) {
            log.error("Localhost getJedisClient {}  error", localTmpValue, e);
        }
        return null;
    }


    private static void returnClient(JedisClient jedis) {
        if (jedis != null)
            returnClient(jedis.getServerIndex(), jedis.getJedis());
    }

    private static Jedis getRedisClient(int serverIndex) throws Exception {
        return jedisPoolList.get(serverIndex).getResource();
    }

    private static void returnClient(int serverIndex, Jedis jedis) {
        jedisPoolList.get(serverIndex).returnResource(jedis);
    }

    private static void addJedisPool(JedisPool jedisPool) {
        jedisPoolList.add(jedisPool);
    }


    /**
     * setnx，如果已存在则不更新
     * 更新成功返回true
     *
     * @param key
     * @param value
     * @return
     */
    public static boolean setnx(int db, String key, String value) {
        boolean check = false;
        JedisClient jedisClient = null;
        try {
            jedisClient = getJedisClient();
            Jedis jedis = jedisClient.getJedis();

            jedis.select(db);
            Long result = jedis.setnx(key, value);

            check = (1 == result);  //更新成功setnx返回1
        } catch (Exception e) {
            e.printStackTrace();
            log.error("setnx Error:{}, key:{},value:{}", e, key, value);
        } finally {
            if (jedisClient != null) {
                returnClient(jedisClient);
            }
        }
        return check;
    }


    public static boolean set(int db, String key, String value) {
        boolean check = false;
        JedisClient jedisClient = null;
        try {
            jedisClient = getJedisClient();
            Jedis jedis = jedisClient.getJedis();

            jedis.select(db);
            jedis.set(key, value);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("set Error:{}, key:{},value:{}", e, key, value);
        } finally {
            if (jedisClient != null) {
                returnClient(jedisClient);
            }
        }
        return check;
    }


    /**
     * 设置过期时间
     *
     * @param db
     * @param key
     * @param second
     * @return
     */
    public static void expire(int db, String key, int second) {
        JedisClient jedisClient = null;
        Jedis jedis = null;
        try {
            jedisClient = getJedisClient();
            jedis = jedisClient.getJedis();

            jedis.select(db);
            jedis.expire(key, second);
        } catch (Exception e) {
            //如果设置过期失败则再设置一次，以免给打满redis留下隐患
            try {
                jedis.select(db);
                jedis.expire(key, second);
            } catch (Exception e1) {
                e1.printStackTrace();
                log.error("setnx Error:{}, key:{},second:{}", e1, key, second);
            }

        } finally {
            if (jedisClient != null) {
                returnClient(jedisClient);
            }
        }
    }


    /**
     * 批量获取
     *
     * @param db
     * @param keys
     * @return
     */
    public static List<String> mget(int db, String[] keys) {
        List<String> result = null;
        JedisClient jedisClient = null;
        try {
            jedisClient = getJedisClient();
            Jedis jedis = jedisClient.getJedis();

            jedis.select(db);
            result = jedis.mget(keys);
        } catch (Exception e) {
            e.printStackTrace();
            String keyStr = JsonUtil.object2jsonWithoutException(keys);
            log.error("mget Error:{}, key:{}", e, keyStr);
        } finally {
            if (jedisClient != null) {
                returnClient(jedisClient);
            }
        }
        return result;
    }


    /**
     * 批量获取
     *
     * @param db
     * @param key
     * @return
     */
    public static String get(int db, String key) {
        String result = null;
        JedisClient jedisClient = null;
        try {
            jedisClient = getJedisClient();
            Jedis jedis = jedisClient.getJedis();

            jedis.select(db);
            result = jedis.get(key);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("mget Error:{}, key:{}", e, key);
        } finally {
            if (jedisClient != null) {
                returnClient(jedisClient);
            }
        }
        return result;
    }


    /**
     * 尝试获取分布式锁 setNxAndExpire
     *
     * @param key        锁
     * @param value      请求标识
     * @param expireTime 超期时间 单位毫秒
     * @return 是否获取成功
     */
    public static boolean setNxAndExpire(int db, String key, String value, int expireTime) {
        JedisClient jedisClient = null;
        try {
            jedisClient = getJedisClient();
            Jedis jedis = jedisClient.getJedis();

            jedis.select(db);
            String result = jedis.set(key, value, GyConstant.SET_IF_NOT_EXIST, GyConstant.SET_WITH_EXPIRE_TIME, expireTime);
            if (GyConstant.SUCCESS.equals(result)) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("setNxAndExpire Error:{}, key:{}", e, key);
        } finally {
            if (jedisClient != null) {
                returnClient(jedisClient);
            }
        }
        return false;
    }


    /**
     * @Description: 返回 key所关联的字符串值 ,如果 key不存在那么返回 null
     */
    public static Set<String> hKeys(int dbNum, String key) {
        JedisClient jedisClient = null;
        try {
            jedisClient = getJedisClient();
            Jedis jedis = jedisClient.getJedis();
            jedis.select(dbNum);
            Set<String> result = jedis.hkeys(key);

            if (CollectionUtils.isEmpty(result)) {
                log.error("hKeys value of " + key + " in redis is null or empty");
            }
            return result;
        } catch (Exception e) {
            log.error("get-string-failed: keys:{}, error:{}", key, e);
            return null;
        } finally {
            if (jedisClient != null) {
                returnClient(jedisClient);
            }
        }
    }
}
