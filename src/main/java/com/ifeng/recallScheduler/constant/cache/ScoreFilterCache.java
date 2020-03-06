package com.ifeng.recallScheduler.constant.cache;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import redis.clients.jedis.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yeben on 2018/1/9.
 */

/**打分形式的关键词过滤，
 * 具体通过检查文章的标题进行
 *
 */
@Service
public class ScoreFilterCache {

    private static final Logger logger = LoggerFactory.getLogger(ScoreFilterCache.class);




    /**打分过滤词的key的前缀
     * eg  keyword:TMD
     *
     */
    private static String scoreFilterWordPrefix = "keyword:*";
    private static String scoreFilterWordPrefix2Replace = "keyword:";

    private static String scoreFilterWordField_level= "sensetiveLevel";
    private static String scoreFilterWordField_expireTs= "expireTs";

    private static JedisPool jedisPool;

    private static int dbNum = 14;

    static {
        initJedisForScoreFilter();
//        jedis =  new Jedis("10.90.11.60",6380, 300);
//        jedis.select(14);
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
            jedisPool = new JedisPool(config, "10.90.11.60", 6380, 10000);
        }
    }

    public static Jedis getJedisClient(int dbNum) {
        Jedis jedis = jedisPool.getResource();
        if (jedis != null) {
            jedis.select(dbNum);
            return jedis;
        } else {
            return null;
        }
    }

    /**从redis中加载出打分形式的过滤词表
     *
     * @return
     */
    public static Map<String, Integer> queryScoreFilterWordsFromRedis(){

        Map<String, Integer> scoreWordMap = new HashMap<String, Integer>();

        ScanParams params = new ScanParams();
        params.match(scoreFilterWordPrefix);   //设置正则匹配规则  keyword:
        params.count(1000);     //设置每次分页拉取数量
        Jedis jedis = getJedisClient(dbNum);


        try{
            ScanResult<String> scanResult = jedis.scan(ScanParams.SCAN_POINTER_START,params);  //SCAN_POINTER_START 就是0 从头开始

            String nextCursor = scanResult.getStringCursor();  //返回用于下次遍历的游标
            List<String> scanResultMap = scanResult.getResult();
            long now = System.currentTimeMillis();

            while (true) {
                for (String key : scanResultMap) {
                    Map<String,String> mapTemp = jedis.hgetAll(key);
                    if(!CollectionUtils.isEmpty(mapTemp)){
                        long expireTs = NumberUtils.toLong(mapTemp.get(scoreFilterWordField_expireTs),0l);
                        if(now <=  expireTs){
                            scoreWordMap.put(StringUtils.replaceOnce(key,scoreFilterWordPrefix2Replace,"").toLowerCase(),  //只替换一次 例如 StringUtils.replaceOnce("sshhhss", "ss", "p");//只替换一次-->结果是：phhhss
                                    NumberUtils.toInt(mapTemp.get(scoreFilterWordField_level),1));
                        }
//                      else{
//                          System.out.println("error expireTs,key="+key+",expireTime="+mapTemp.get("expireTime")+",ts="+mapTemp.get(scoreFilterWordField_expireTs));
//                      }
                    }
                }
                if (nextCursor == null || ScanParams.SCAN_POINTER_START.equals(nextCursor)) {  //当cursor="0" 或者为空则不继续scan
                    break;
                }
                scanResult = jedis.scan(nextCursor,params);
                nextCursor = scanResult.getStringCursor();
                scanResultMap = scanResult.getResult();
            }
            if(scoreWordMap == null){
                scoreWordMap = Collections.emptyMap();;
            }
        }catch (Exception  e){
            logger.error("ScoreFilterCache loadError:{}",e);

        }

        if(jedis != null){
            jedis.close();
        }

        return scoreWordMap;
    }






}
