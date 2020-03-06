package com.ifeng.recallScheduler.preloadDocument2Cache;


import com.beust.jcommander.internal.Maps;
import com.ifeng.recallScheduler.constant.GyConstant;
import com.ifeng.recallScheduler.item.Document;
import com.ifeng.recallScheduler.itemUtil.DocClient;
import com.ifeng.recallScheduler.threadUtil.BaseThreadPool;
import com.ifeng.recallScheduler.timer.TimerEntity;
import com.ifeng.recallScheduler.timer.TimerEntityUtil;
import com.ifeng.recallScheduler.utils.DocUtil;
import com.ifeng.recallScheduler.utils.JsonUtil;
import com.ifeng.recallScheduler.utils.MathUtil;
import org.apache.hadoop.hbase.client.Table;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

@Service
public class LoadDocument2cache implements FactoryBean<TransportClient> {

    private static final Logger logger = LoggerFactory.getLogger(LoadDocument2cache.class);

    private final static String INDEX = "preload-news";


    private static Settings settings;
    private static TransportClient client;

    private final static String[] needFetchSource = {"docId",
            "date",
            "hotBoost",
            "expireTime",
            "_score",
            "on_line"};

    private final static String[] noNeedFetchSource = {};


    private static void initEsClient() {
        System.setProperty("es.set.netty.runtime.available.processors", "false");
        settings = Settings.builder().put("cluster.name", "docInfo_es")
                .put("client.transport.ignore_cluster_name", true).build();

        long start = System.currentTimeMillis();
        try {
            logger.warn("start load document infomation to cache ...");
            client = new PreBuiltTransportClient(settings)
                    .addTransportAddress(new TransportAddress(InetAddress.getByName("10.80.85.139"), 9300))
                    .addTransportAddress(new TransportAddress(InetAddress.getByName("10.80.71.142"), 9300))
                    .addTransportAddress(new TransportAddress(InetAddress.getByName("10.80.87.139"), 9300))
                    .addTransportAddress(new TransportAddress(InetAddress.getByName("10.80.77.140"), 9300))
                    .addTransportAddress(new TransportAddress(InetAddress.getByName("10.80.88.140"), 9300));
        } catch (Exception e) {
            logger.error("connect to documentEs cluster error: {}", e);
            e.printStackTrace();
        }
        logger.info("initJpEsClient init cost:{}", System.currentTimeMillis() - start);
    }


    public static synchronized TransportClient getClient() {
        if (client == null) {
            initEsClient();
        }
        return client;
    }

    @Override
    public TransportClient getObject() throws Exception {
        if (client == null) {
            initEsClient();
        }
        return client;
    }

    @Override
    public Class<?> getObjectType() {
        return TransportClient.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public boolean updateDocCacheBatchLimit(List<String> docIdList) {
        Future<Boolean> future = BaseThreadPool.THREAD_POOL_UpdateIndex.submit(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                updateDocCacheBatch(docIdList);
                return true;
            }
        });

        boolean finish = false;
        try {
            finish = future.get(GyConstant.MaxTimeOut_UpadteDocOnline, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            logger.error("updateDocCacheBatch docid:{} is error!:{}", JsonUtil.object2jsonWithoutException(docIdList), e);
            logger.error("THREAD_POOL_LOG:{}", BaseThreadPool.THREAD_POOL_UpdateIndex.toString());
            future.cancel(true);
        }
        return finish;
    }

    public void updateDocCacheBatch(List<String>  docIdList){
        if (docIdList == null || docIdList.size() == 0) {
            return;
        }
        DocClient docClient = new DocClient();
        Map<Integer, Writer> writerMap = Maps.newHashMap();


        Table cntTable = docClient.getDocTable(DocClient.CONTENT_TableName);
        Table indexTable = docClient.getDocTable(DocClient.INDEX_TableName);

        try {
            Map<String, Document> reMap = docClient.getDocBatch(docIdList, cntTable, indexTable);

            long startwrite = System.currentTimeMillis();

            for (int i = 0; i < GyConstant.doc_txt_Num; i++) {
                Writer writerTmp = new FileWriter(GyConstant.personalcacheOfpath + GyConstant.Symb_Underline + i, false);  // 写入的文本不附加在原来的后面而是直接覆盖
                writerMap.put(i, writerTmp);
            }

            int mod = 0;

            Document onedocument = null;
            String jsonString = "";
            String key = "";
            long sizewrite = 0L;
            Writer writer = null;


            for (int i = 0; i < docIdList.size(); i++) {
                try {
                    key = docIdList.get(i);
                    onedocument = reMap.get(key);
                    if (onedocument == null) {
                        continue;
                    }

                    jsonString = JsonUtil.object2jsonWithoutException(onedocument);

                    mod = i % GyConstant.doc_txt_Num;


                    //如果文章个数大于baseNum，后续内容按照几率进行更新，从而避免缓存内容媒体刷到磁盘上而不更新
                    if (sizewrite > GyConstant.doc_txt_BaseNum && MathUtil.getNum(100) > 50) {
                        continue;
                    }

                    writer = writerMap.get(mod);
                    writer.write(jsonString + "\n");
                    sizewrite++;
                } catch (Exception e) {
                    logger.error("write to localfile error:{}", e);
                }
            }
            long cost = System.currentTimeMillis() - startwrite;

            logger.info("write to local file size={},sizewrite={},writecost={}", docIdList.size(), sizewrite, cost);

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("updateDocCacheBatch ERROR:{}", e);
        } finally {

            try {
                if (indexTable != null)
                    indexTable.close();
            } catch (IOException e) {
                logger.error("[HBase] Error while closing table {} {} ", DocClient.INDEX_TableName
                        , e.getMessage());
            }

            try {
                if (cntTable != null)
                    cntTable.close();
            } catch (IOException e) {
                logger.error("[HBase] Error while closing table {} {} ", DocClient.CONTENT_TableName
                        , e);
            }
            try {
                for (Map.Entry<Integer, Writer> entry : writerMap.entrySet()) {
                    Writer tmp = entry.getValue();
                    if (tmp != null) {
                        tmp.close();
                        logger.info("Writer close");
                    }
                }
            } catch (Exception e) {
                logger.error("write to local file close e:{}", e);
            }
        }
    }

    public void init() {
        TimerEntity timer = TimerEntityUtil.getInstance();

        logger.info("");
        timer.addStartTime("getDocId from ES");
        List<String> docList = new ArrayList<>();
        TransportClient client = LoadDocument2cache.getClient();
        RangeQueryBuilder rangeQuery = rangeQuery("date").gte("now-" + 240 + "h/h");
        RangeQueryBuilder rangeQuery2 = rangeQuery("expireTime").gte("now");
        RangeQueryBuilder rangeQuery3 = rangeQuery("on_line").gt("0");

        BoolQueryBuilder qb = boolQuery().must(QueryBuilders.constantScoreQuery(rangeQuery))
                .must(QueryBuilders.constantScoreQuery(rangeQuery2))
                .must(QueryBuilders.constantScoreQuery(rangeQuery3))
                .must(termQuery("available", true));


        SearchResponse searchResponse = client.prepareSearch(INDEX)
                .setFetchSource(needFetchSource, noNeedFetchSource)
                .setQuery(qb)
                .addSort("_score", SortOrder.DESC).addSort("hotBoost",SortOrder.DESC)
                .setSize(100000).get(); //TODO

        for (SearchHit hit : searchResponse.getHits().getHits()) {
            Map<String, Object> map = hit.getSourceAsMap();
            docList.add((String) map.get("docId"));
        }


        /**
         * 根据docId查询内容画像 并写入缓存
         */

        DocUtil docUtil = new DocUtil();
        LoadDocument2cache hbase2local = new LoadDocument2cache();

        /**
         * 根据docId查询Hbase 并进行本地磁盘持久化
         */
        timer.addStartTime("writetolocalfile");
        hbase2local.updateDocCacheBatchLimit(docList);
        timer.addEndTime("writetolocalfile");

        logger.info("Documents Persisit Process Finish!");
    }

}
