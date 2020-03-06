package com.ifeng.recallScheduler.utils.esUtil;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Created by lilg1 on 2018/5/11.
 */


public class PreloadQueryUtil {

    private final static Logger logger = LoggerFactory.getLogger(PreloadQueryUtil.class);

    private final static String INDEX = "preload-news";

    private final static String TYPE = "all";

    private static String[] noFetchSource = new String[]{};
    private static String[] needFetchSource = new String[]{"docId"};
    private static TransportClient client ;

    static {
        client = EsClientFactory.getClient();
    }

    public static String queryDocIdByGuid(String guid) {
        QueryBuilder qb = QueryBuilders.matchQuery("url", guid);

        SearchResponse response = client.prepareSearch(INDEX)
                .setScroll(new TimeValue(60000))
                .setTimeout(new TimeValue(500))
                .setFetchSource(needFetchSource, noFetchSource)
                .setQuery(qb)
                .setSize(1).get();

        return Arrays.stream(response.getHits().getHits()).map(hit -> hit.getSourceAsMap().get("docId").toString()).findFirst().orElse("");

    }

}
