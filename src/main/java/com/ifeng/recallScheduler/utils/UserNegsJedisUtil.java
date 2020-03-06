package com.ifeng.recallScheduler.utils;

import com.ifeng.recallScheduler.constant.GyConstant;
import com.ifeng.recallScheduler.redis.jedisPool.bean.JedisClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class UserNegsJedisUtil {
    protected final static Logger log = LoggerFactory.getLogger(UserNegsJedisUtil.class);

    private static AtomicInteger local = new AtomicInteger(0);

    private static final List<JedisPool> jedisPoolList = new ArrayList<JedisPool>();


    //Redis服务器IP
    @Value("${userNegs.redis.addr}")
    private String addr;

    //Redis的端口号
    @Value("${userNegs.redis.port}")
    private String port;

    //Redis的端口号
    @Value("${userNegs.redis.db}")
    private int db;

    //可用连接实例的最大数目，默认值为8；
    //如果赋值为-1，则表示不限制；如果pool已经分配了maxActive个jedis实例，则此时pool的状态为exhausted(耗尽)。
    @Value("${userNegs.redis.maxActive}")
    private int maxActive;

    //控制一个pool最少有多少个状态为idle(空闲的)的jedis实例，默认值也是8。
    @Value("${userNegs.redis.minIdle}")
    private int minIdle;

    //控制一个pool最多有多少个状态为idle(空闲的)的jedis实例，默认值也是8。
    @Value("${userNegs.redis.maxIdle}")
    private int maxIdle;

    //等待可用连接的最大时间，单位毫秒，默认值为-1，表示永不超时。如果超过等待时间，则直接抛出JedisConnectionException；
    @Value("${userNegs.redis.maxWait}")
    private int maxWait;

    @Value("${userNegs.redis.timeOut}")
    private int timeOut;

    //在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；
    @Value("${userNegs.redis.testOnBorrow}")
    private boolean testOnBorrow;


    /**
     * 初始化redis集群
     */
    @PostConstruct
    public void initRedis() {
        long start=System.currentTimeMillis();
        log.info("start init userNegsJedisUtil.initRedis");
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
            log.error("redisInit ERROR:{}", e);
            e.printStackTrace();
        }
        log.info("userNegsJedisUtil init cost:{}", System.currentTimeMillis() - start);
    }


    /**
     * 轮播获取jedis客户端
     *
     * @return
     */
    public JedisClient getJedisClient() {
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
                    log.error("getJedisClient error ! connection:redis {} 失败,现已故障转移,{}",
                            localTmpValue, e);
                    localValue++;  //容错
                }
            }
            if (!isServerOK) {
                log.error("getJedisClient redis-server-all   均为不可用");
                return null;
            }
            JedisClient jesisReturn = new JedisClient();
            jesisReturn.setServerIndex(localTmpValue);
            jesisReturn.setJedis(jedis);
            return jesisReturn;
        } catch (Exception e) {
            log.error("getJedisClient {}  error", localTmpValue, e);
        }
        return null;
    }


    public  void returnClient(JedisClient jedis) {
        if (jedis != null)
            returnClient(jedis.getServerIndex(), jedis.getJedis());
    }

    private  Jedis getRedisClient(int serverIndex) throws Exception {
        return jedisPoolList.get(serverIndex).getResource();
    }

    private  void returnClient(int serverIndex, Jedis jedis) {
        jedisPoolList.get(serverIndex).returnResource(jedis);
    }

    private void addJedisPool(JedisPool jedisPool) {
        jedisPoolList.add(jedisPool);
    }



    /**
     * 可以拿到最近的
     * @param key
     * @param start
     * @param end
     * @return
     */
    public Set<String> getSetByZrevrange(String key, int start, int end) {

        JedisClient jedisClient = null;
        try {
            jedisClient = getJedisClient();
            Jedis jedis = jedisClient.getJedis();
            jedis.select(db);
            Set<String> values = jedis.zrevrange(key + GyConstant.UserNegsKey_End, start, end);
            return values;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("getSetByZrange Error:{}, key:{}", key, e);
        } finally {
            if (jedisClient != null) {
                returnClient(jedisClient);
            }
        }
        return null;
    }

    /**
     * 可以拿到最近的C级 负反馈
     * @param key
     * @return
     */
    public Set<String> getAllCSet(String key) {

        JedisClient jedisClient = null;
        try {
            jedisClient = getJedisClient();
            Jedis jedis = jedisClient.getJedis();
            jedis.select(db);
            Set<String> values = jedis.zrange(key + GyConstant.UserCNegsKey_End, 0, -1);
            return values;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("getAllCSet Error:{}, key:{}", key, e);
        } finally {
            if (jedisClient != null) {
                returnClient(jedisClient);
            }
        }
        return null;
    }

}
