package com.ifeng.recallScheduler.utils;

import com.ifeng.recallScheduler.item.BanItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**展现打分过滤词 滤掉的doc
 *
 * Created on 2018/1/22.
 */
@Service
public class ScoreFilterDoc2ShowUtil {


    private static final Logger logger = LoggerFactory.getLogger(ScoreFilterDoc2ShowUtil.class);

    private static JedisPool jedisPool;

    private static int dbNum = 12;

    static {
        initJedisForScoreFilter();
    }

    private static void initJedisForScoreFilter() {
        if (jedisPool == null) {
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(300);
            config.setMaxIdle(30);
            config.setMaxWaitMillis(10000);
            config.setTestOnBorrow(true);
            config.setTestOnReturn(true);
            config.setBlockWhenExhausted(true);
            jedisPool = new JedisPool(config, "10.90.14.13", 6379, 10000);
        }
    }

    public static Jedis getJedisClient() {
        Jedis jedis = jedisPool.getResource();
        if (jedis != null) {
            jedis.select(dbNum);
            return jedis;
        } else {
            return null;
        }
    }


    public static void deal2Show(BanItem banItem){
        Jedis jedis = null;
        try{
            jedis = getJedisClient();
            String result = JsonUtil.object2jsonWithoutException(banItem);
            jedis.lpush("banitems", result);
            logger.info("deal2Show,result={}",result);

        }catch(Exception e){
            logger.error("exception occur",e);
        }finally{
            try{
                jedis.close();
            }catch(Exception e){
                logger.error("jedis close exception occur",e);
                jedis = null;
            }

        }


    }

    public static void main(String[] args) {

       String docid = "45918080";
       String title = "小混混强行收取保护费，还自称是刀疤强，却被贾乃亮一招秒杀！";
       String word = "贾乃亮";
        BanItem banItem = new BanItem(docid,title,word);
        deal2Show(banItem);
        System.out.println("end");


    }



}
