package com.ifeng.recallScheduler.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;


import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * 先将头条工程迁移过来，后期全部要替换为连接池的方式查询redis
 */
public class RedisUtil {

    private static final Logger log = LoggerFactory.getLogger(RedisUtil.class);

    private static AtomicInteger counter = new AtomicInteger(0);

    /**
     * @return
     * @throws
     * @Title: getJedisClient
     * @Description: 获得jedis客户端
     */
    private static Jedis getJedisClient(String host, int port) {
        // 配置发生变化的时候自动载入
        Jedis jedis = null;
        try {
            jedis = new Jedis(host, port, 300);
        } catch (Exception ex) {
            log.error("Get jedis instance failed:" + ex.getMessage());
        }
        return jedis;
    }

    private static Jedis getJedisClient(String host, int port, int timeout) {
        // 配置发生变化的时候自动载入
        Jedis jedis = null;
        try {
            jedis = new Jedis(host, port, timeout);
        } catch (Exception ex) {
            log.error("Get jedis instance failed:" + ex.getMessage());
        }
        return jedis;
    }

    /**
     * @param jedis
     * @throws
     * @Title: returnResource
     * @Description: 释放jedis连接资源
     */
    public static void returnResource(Jedis jedis) {
        if (jedis != null) {
            jedis.disconnect();
            jedis = null;
        }
    }


    /**
     * @param userId
     * @param dbNum
     * @param key
     * @return
     * @Title: get
     * @Description: 返回 key所关联的字符串值 ,如果 key不存在那么返回 null
     */
    public static String get(String userId, String host, int port, int dbNum, String key) {
        Jedis jedis = getJedisClient(host, port);

        try {
            jedis.select(dbNum);
            String value = jedis.get(key);
            if (value == null || value.isEmpty()) {
                log.info("value of " + key + " in redis is null or empty," + userId);
            }
            return value;
        } catch (Exception e) {
            log.error(userId + " get-string-failed:" + key, e);
            return null;
        } finally {
            returnResource(jedis);
        }
    }


    // 批量获取 key
    public static List<String> mget(String host, int port, int dbNum, List<String> keys) {
        String[] redisUrl = host.split(",");
        int co = counter.getAndIncrement() & Integer.MAX_VALUE;
        int index = co % redisUrl.length;
        host = redisUrl[index];
        Jedis jedis = getJedisClient(host, port);
        if (keys == null || keys.isEmpty()) return null;
        try {
            jedis.select(dbNum);
            String[] tempString = new String[keys.size()];
            List<String> values = jedis.mget((String[]) keys.toArray(tempString));
            return values;
        } catch (Exception e) {
            log.error(" get-keys-values Error:" + keys);
            return null;
        } finally {
            returnResource(jedis);
        }
    }

    public static Set<String> getSet(String host, int port, int dbNum, String key) {
        String[] redisUrl = host.split(",");
        int co = counter.getAndIncrement() & Integer.MAX_VALUE;
        int index = co % redisUrl.length;
        host = redisUrl[index];
        Jedis jedis = getJedisClient(host, port);

        try {
            jedis.select(dbNum);
            Set<String> values = jedis.smembers(key);
            return values;
        } catch (Exception e) {
            log.error(" get-keys-values Error:{},{},{},  key:{}", host, port, dbNum, key);
            return null;
        } finally {
            returnResource(jedis);
        }
    }

    /**
     * @param host
     * @param port
     * @param dbNum
     * @param key
     * @param members
     */
    public static void addSet(String host, int port, int dbNum, String key, Set<String> members) {
        String[] redisUrl = host.split(",");
        int co = counter.getAndIncrement() & Integer.MAX_VALUE;
        int index = co % redisUrl.length;
        host = redisUrl[index];
        Jedis jedis = getJedisClient(host, port);

        try {
            jedis.select(dbNum);
            String[] array = members.toArray(new String[]{});
            jedis.sadd(key, array);
        } catch (Exception e) {
            log.error("S-Member set-key-value Error:{}:{}, db{},  key:{}, Exception:{}", host, port, dbNum, key, e);
        } finally {
            returnResource(jedis);
        }

    }


    /**
     * @Description: 返回 key所关联的字符串值 ,如果 key不存在那么返回 null
     */
    public static String get(String host, int port, int dbNum, String key) {

        if (host.indexOf(",") > 0) {
            //轮询
            String[] redisUrl = host.split(",");
            int co = counter.getAndIncrement() & Integer.MAX_VALUE;
            int index = co % redisUrl.length;
            host = redisUrl[index];
        }

        Jedis jedis = getJedisClient(host, port);

        try {
            jedis.select(dbNum);
            String value = jedis.get(key);

            if (value == null || value.isEmpty()) {
                log.info("value of " + key + " in redis is null or empty");
            }
            return value;
        } catch (Exception e) {
            log.error("get-string-failed: keys:{}, error:{}", key, e);
            return null;
        } finally {
            returnResource(jedis);
        }
    }



    /**
     * @Description: 返回 key所关联的字符串值 ,如果 key不存在那么返回 null
     */
    public static Set<String> hKeys(String host, int port, int dbNum, String key) {

        if (host.indexOf(",") > 0) {
            //轮询
            String[] redisUrl = host.split(",");
            int co = counter.getAndIncrement() & Integer.MAX_VALUE;
            int index = co % redisUrl.length;
            host = redisUrl[index];
        }

        Jedis jedis = getJedisClient(host, port);

        try {
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
            returnResource(jedis);
        }
    }

    /**
     * @return
     * @Description: redis服务调用 负载均衡(for 57 now)
     */
    public static String getLoadBanlance(String host, int port, int dbNum, String key) {
        //轮询
        String[] redisUrl = host.split(",");
        int co = counter.getAndIncrement() & Integer.MAX_VALUE;
        int index = co % redisUrl.length;
        host = redisUrl[index];

        Jedis jedis = getJedisClient(host, port);

        try {
            jedis.select(dbNum);
            String value = jedis.get(key);

//			if(value==null||value.isEmpty()){
//				log.info("value of "+key+" in redis is null or empty");
//			}else{
//				log.info("Get value of "+key+" success");
//			}
            return value;
        } catch (Exception e) {
            log.error(" get-string-failed:" + key, e);
            return null;
        } finally {
            returnResource(jedis);
        }
    }


    public static Map<String, String> hgetAll(String host, int port, int dbNum, String key) {
        Jedis jedis = getJedisClient(host, port);

        try {
            jedis.select(dbNum);

            Map<String, String> map = jedis.hgetAll(key);

            if (map == null || map.isEmpty()) {
                log.info("value of " + key + " in redis is null or empty");
            } else {
                log.info("Get value of " + key + " success,size=" + map.size());
            }
            return map;
        } catch (Exception e) {
            log.error("hgetAll failed:" + key, e);
            return null;
        } finally {
            returnResource(jedis);
        }
    }

    public static List<String> getSourceA(String host, int port, int dbNum) {
        Jedis jedis = getJedisClient(host, port);

        try {
            List<String> sourceA = Lists.newArrayList();
            jedis.select(dbNum);
            Set<String> keys = jedis.keys("*");
            if (keys != null && !keys.isEmpty()) {
                for (String key : keys) {
                    Set<String> keySet = jedis.smembers(key);
                    if (keySet == null || keySet.isEmpty()) continue;
                    for (String strInSet : keySet) {
                        if (StringUtils.isBlank(strInSet)) continue;
                        if (strInSet.indexOf("#") > 0) {
                            String[] source = strInSet.split("#");
                            if (source[2].equals("A"))
                                sourceA.add(source[0].trim());
                        }
                    }
                }
            }
            return sourceA;
        } catch (Exception e) {
            log.error("get good source error", e);
            return null;
        } finally {
            returnResource(jedis);
        }
    }


    public static Map<String, String> hgetAllLoadBanlance(String host, int port, int dbNum, String key) {

        String[] redisUrl = host.split(",");
        int co = counter.getAndIncrement() & Integer.MAX_VALUE;
        int index = co % redisUrl.length;
        host = redisUrl[index];
        Jedis jedis = getJedisClient(host, port);

        try {
            jedis.select(dbNum);

            Map<String, String> map = jedis.hgetAll(key);
            if (map == null || map.isEmpty()) {
                log.info("value of {} in redis is null or empty", key);
            }
            return map;
        } catch (Exception e) {
            log.error("hgetAll key:{} failed:{}", key, e);
            return null;
        } finally {
            returnResource(jedis);
        }
    }

    public static Map<String, String> hgetAllInt(String host, int port, int dbNum, String key) {

        String[] redisUrl = host.split(",");
        int co = counter.getAndIncrement() & Integer.MAX_VALUE;
        int index = co % redisUrl.length;
        host = redisUrl[index];
        Jedis jedis = getJedisClient(host, port);

        try {
            jedis.select(dbNum);

            Map<String, String> map = jedis.hgetAll(key);

            if (map == null || map.isEmpty()) {
                log.info("value of {} in redis is null or empty", key);
            }
            return map;
        } catch (Exception e) {
            log.error("hgetAll {} failed:{}", key, e);
            return null;
        } finally {
            returnResource(jedis);
        }
    }

    public static String hget(String host, int port, int dbNum, String key, String field) {
        Jedis jedis = getJedisClient(host, port);

        try {
            jedis.select(dbNum);

            String value = jedis.hget(key, field);
            return value;
        } catch (Exception e) {
            log.error("hget {} failed:{}", key, e);
            return null;
        } finally {
            returnResource(jedis);
        }
    }

    /**
     * @param dbNum
     * @param key
     * @param value
     * @param seconds
     * @throws
     * @Title: setex
     * @Description: 将值 value 关联到 key ，并将 key 的生存时间设为 seconds (以秒为单位) 如果 key
     * 已经存在， SETEX 命令将覆写旧值
     */
    public static void setex(String host, int port, int dbNum, String key, String value, int seconds) {
        Jedis jedis = getJedisClient(host, port);
        try {
            jedis.select(dbNum);
            jedis.setex(key, seconds, value);
        } catch (Exception e) {
            log.error("setex-string {}  failed:{}", key, e);
        } finally {
            returnResource(jedis);
        }
    }

    public static void set(String host, int port, int dbNum, String key, String value) {
        Jedis jedis = getJedisClient(host, port);
        try {
            jedis.select(dbNum);
            jedis.set(key, value);
        } catch (Exception e) {
            log.error("set-string {} failed:{}", key, e);
        } finally {
            returnResource(jedis);
        }
    }

    /**
     * 统计计数 小于5个时 加入 过期时间
     * @param host
     * @param port
     * @param dbNum
     * @param key
     * @param seconds
     */
    public static void incrEx(String host, int port, int dbNum, String key, int seconds) {
        Jedis jedis = getJedisClient(host, port);
        try {
            jedis.select(dbNum);
            Long num = jedis.incr(key);
            if (num < 5){
                jedis.expire(key, seconds);
            }
        } catch (Exception e) {
            log.error("incrEx-string {}  failed:{}", key, e);
        } finally {
            returnResource(jedis);
        }
    }

    public static void zadd(String host, int port, int dbNum, String key, String value, Double score) {
        Jedis jedis = getJedisClient(host, port);
        try {
            jedis.select(dbNum);
            jedis.zadd(key, score, value);
        } catch (Exception e) {
            log.error("zadd-string {} failed:{}", key, e);
        } finally {
            returnResource(jedis);
        }
    }

    public static void del(String host, int port, int dbNum, String key ) {
        Jedis jedis = getJedisClient(host, port);
        try {
            jedis.select( dbNum );
            jedis.del( key );
        } catch (Exception e) {
            log.error("del-string {} failed:{}", key, e);
        } finally {
            returnResource(jedis);
        }
    }


    public static void lsetex(String host, int port, int dbNum, String key, String value) {
        Jedis jedis = getJedisClient(host, port);
        try {
            jedis.select(dbNum);
            jedis.lpush(key, value);
        } catch (Exception e) {
            log.error("setex-string {} failed:{}", key, e);
        } finally {
            returnResource(jedis);
        }
    }

    public static List<String> lgetAll(String host, int port, int dbNum, String key, String value) {
        Jedis jedis = getJedisClient(host, port);
        try {
            jedis.select(dbNum);
            return jedis.lrange(key, 1, -1);
        } catch (Exception e) {
            log.error("lgetAll {} failed:{}", key, e);
            return null;
        } finally {
            returnResource(jedis);
        }
    }


    public static List<String> lrange(String host, int port, int dbNum, String key, String value, int limit) {
        Jedis jedis = getJedisClient(host, port);
        try {
            jedis.select(dbNum);
            return jedis.lrange(key, 1, limit);
        } catch (Exception e) {
            log.error("lrange {} failed:{}", key, e);
            return null;
        } finally {
            returnResource(jedis);
        }
    }

    /**
     * @param dbNum
     * @param key
     * @param value
     * @throws
     * @Title: setex
     * @Description: 将值 value 关联到 key ，并将 key 的生存时间设为 seconds (以秒为单位) 如果 key
     * 已经存在， SETEX 命令将覆写旧值
     */
    public static void hset(String host, int port, int dbNum, String key, String field, String value) {
        Jedis jedis = getJedisClient(host, port);
        try {
            jedis.select(dbNum);
            jedis.hset(key, field, value);
        } catch (Exception e) {
            log.error("hset {} {} error:{}", key, field, e);
        } finally {
            returnResource(jedis);
        }
    }

    public static Set<String> getKeys(String host, int port, int dbNum, int timeout) {
        Jedis jedis = getJedisClient(host, port, timeout);
        try {
            jedis.select(dbNum);

            Set<String> result = Sets.newHashSet();
            ScanParams scanParameters = new ScanParams();
            scanParameters.match("*");
            scanParameters.count(1000);

            ScanResult<String> scanResult = jedis.scan(0, scanParameters);
            String nextCursor = scanResult.getStringCursor();
            List<String> scanResultList = scanResult.getResult();

            while (true) {
                result.addAll(scanResultList);

                if (nextCursor == null || nextCursor.equals("0")) {
                    break;
                }

                scanResult = jedis.scan(nextCursor, scanParameters);
                nextCursor = scanResult.getStringCursor();
                scanResultList = scanResult.getResult();
            }
            return result;

        } catch (Exception e) {
            log.error("get black list error {}", e);
            return Collections.emptySet();
        } finally {
            returnResource(jedis);
        }
    }


    /**
     * 正则匹配，获取所有的key
     *
     * @param host
     * @param port
     * @param dbNum
     * @param timeout
     * @param pattern
     * @return
     */
    public static Set<String> getKeys(String host, int port, int dbNum, int timeout, String pattern) {
        Jedis jedis = getJedisClient(host, port, timeout);
        try {
            jedis.select(dbNum);

            Set<String> result = Sets.newHashSet();
            ScanParams scanParameters = new ScanParams();
            scanParameters.match(pattern);
            scanParameters.count(1000);

            ScanResult<String> scanResult = jedis.scan(0, scanParameters);
            String nextCursor = scanResult.getStringCursor();
            List<String> scanResultList = scanResult.getResult();

            while (true) {
                result.addAll(scanResultList);

                if (nextCursor == null || nextCursor.equals("0")) {
                    break;
                }

                scanResult = jedis.scan(nextCursor, scanParameters);
                nextCursor = scanResult.getStringCursor();
                scanResultList = scanResult.getResult();
            }
            return result;

        } catch (Exception e) {
            log.error("get black list error {}", e);
            return Collections.emptySet();
        } finally {
            returnResource(jedis);
        }
    }

    public static void main(String[] args) {
        //test
    }

    public static List<String> getKeysByScan(String host, int port, int dbNum, int timeout, String pattern) {
        Jedis jedis = getJedisClient(host, port, timeout);
        try {
            jedis.select(dbNum);
            List<String> result = scanKeys(jedis, pattern);
            return result;
        } catch (Exception e) {
            log.error("get black list error {}", e);
            return Collections.EMPTY_LIST;
        } finally {
            returnResource(jedis);
        }
    }

    public static List<String> scanKeys(Jedis jedis, String pattern) {
        List<String> result = Lists.newArrayList();
        ScanParams scanParameters = new ScanParams();
        scanParameters.match(pattern);
        scanParameters.count(100);

        ScanResult<String> scanResult = jedis.scan(0, scanParameters);
        String nextCursor = scanResult.getStringCursor();
        List<String> scanResultList = scanResult.getResult();

        while (true) {
            result.addAll(scanResultList); //每次scan到的结果进行操作

            if (nextCursor == null || nextCursor.equals("0")) {
                break;
            }

            scanResult = jedis.scan(nextCursor, scanParameters);
            nextCursor = scanResult.getStringCursor();
            scanResultList = scanResult.getResult();
        }
        return result;
    }


    public static void writeToRedis(String key, int ttl, String content, String host, int port, int db) {
        Jedis jedis = null;
        try {
            jedis =getJedisClient(host, port);
            jedis.select(db);

            String status = jedis.setex(key, ttl, content);

            if (content.length() > 10000) {
                content = content.substring(0, 500);
                content = content + "...";
            }

            if (!status.equals("OK")) {
                log.error("write data to redis error, status code=" + status + ", key=" + key + " content=" + content + " ttl=" + ttl + " host=" + host + " port=" + port + " db=" + db);
            } else {
                log.info("write data to redis success, status code=" + status + ", key=" + key + " content=" + content + " ttl=" + ttl + " host=" + host + " port=" + port + " db=" + db);
            }

        } catch (Exception e) {
            log.error("write data to redis error, error=" + e + ", key=" + key + " content=" + content + " ttl=" + ttl + " host=" + host + " port=" + port + " db=" + db);
        } finally {
            returnResource(jedis);
        }
    }

}
