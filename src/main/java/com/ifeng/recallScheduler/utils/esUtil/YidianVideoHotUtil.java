package com.ifeng.recallScheduler.utils.esUtil;

import com.beust.jcommander.internal.Lists;
import com.ifeng.recallScheduler.item.YidianVideoBean;
import com.ifeng.recallScheduler.utils.JsonUtil;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.common.lucene.search.function.FieldValueFactorFunction;
import org.elasticsearch.common.lucene.search.function.FunctionScoreQuery;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.*;
import static org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders.fieldValueFactorFunction;
import static org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders.gaussDecayFunction;


/**
 * 一点咨询的视频热补的召回通道
 * Created by jibin on 2017/9/13.
 */
public class YidianVideoHotUtil {

    private static final Logger logger = LoggerFactory.getLogger(YidianVideoHotUtil.class);
    private static TransportClient client;
    private static String[] noFetchSource = new String[]{};
    private static String[] needFetchSource = new String[]{"title", "guid"};

    /**
     * 一点资讯的es  key
     */
    private static final String INDEX = "yidian-sync";


    static {
        client = EsClientFactory.getClient();
    }


    /**
     * 查询一点咨询的video视频集合
     * @param qb
     * @param returnNum
     */
    public static List<YidianVideoBean>  getVideoHotlist(QueryBuilder qb, int returnNum) {
        SearchResponse scrollResp = client.prepareSearch(INDEX)
                .setScroll(new TimeValue(60000))
                .setFetchSource(needFetchSource, noFetchSource)
                .setQuery(qb)
                .setSize(5000).get();

        long startTime = System.currentTimeMillis();
        long itemCount = 0L;

        logger.debug("getVideoHotlist query json ={}", qb.toString());

        List<YidianVideoBean> result= Lists.newArrayList();


//----------------------游标方式获取--------------------------------
        String json="";
        YidianVideoBean yidianVideoBean = null;
        do {
            for (SearchHit hit : scrollResp.getHits().getHits()) {
                json = hit.getSourceAsString();
                yidianVideoBean = JsonUtil.json2ObjectWithoutException(json, YidianVideoBean.class);
                yidianVideoBean.setScore(hit.getScore());

                result.add(yidianVideoBean);
                itemCount++;

                if (itemCount >= returnNum) {
                    break;
                }
            }

            scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute().actionGet();
        }
        while (scrollResp.getHits().getHits().length != 0); // Zero hits mark the end of the scroll and the while loop.



        logger.info("getVideoHotlist query es finished, count=" + itemCount + ", spent time=" + (System.currentTimeMillis() - startTime));

        return result;
    }

    /**
     * 获取查询串
     * @return
     */
    public static QueryBuilder getQueryBuilder() {
        QueryBuilder query = QueryBuilders.boolQuery().must(existsQuery("ClickDoc")).must(existsQuery("ShareDoc")).must(existsQuery("ViewComment"))
                .must(termQuery("online", "1")).mustNot(termQuery("state","0"))
                .must(rangeQuery("syncTime").gte("now-720h/h"));


        FunctionScoreQueryBuilder.FilterFunctionBuilder[] functions = {
                new FunctionScoreQueryBuilder.FilterFunctionBuilder(fieldValueFactorFunction("ClickDoc").modifier(FieldValueFactorFunction.Modifier.LOG1P).factor(0.5f)),  //设置权重
                new FunctionScoreQueryBuilder.FilterFunctionBuilder(fieldValueFactorFunction("ShareDoc").modifier(FieldValueFactorFunction.Modifier.LOG1P).factor(0.2f)),
                new FunctionScoreQueryBuilder.FilterFunctionBuilder(fieldValueFactorFunction("ViewComment").modifier(FieldValueFactorFunction.Modifier.LOG1P).factor(0.1f)),
                new FunctionScoreQueryBuilder.FilterFunctionBuilder(gaussDecayFunction("syncTime", "now", "2d", "5h", 0.3)),  //设置时间衰减
        };

        QueryBuilder qb = QueryBuilders.functionScoreQuery(query, functions).boostMode(CombineFunction.SUM).scoreMode(FunctionScoreQuery.ScoreMode.MULTIPLY);
        return qb;
    }

    public static void main(String[] args) {
        getVideoHotlist(getQueryBuilder(), 1000);
    }
}
