package com.ifeng.recallScheduler.remoteCache.editor;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.ifeng.recallScheduler.constant.GyConstant;
import com.ifeng.recallScheduler.constant.RecWhy;
import com.ifeng.recallScheduler.constant.cache.CacheFactory;
import com.ifeng.recallScheduler.esSearch.EsJpPoolQueryUtil;
import com.ifeng.recallScheduler.filterController.service.impl.FilterService;
import com.ifeng.recallScheduler.item.Document;
import com.ifeng.recallScheduler.request.RequestInfo;
import com.ifeng.recallScheduler.utils.DocUtil;
import com.ifeng.recallScheduler.utils.EhCacheUtil;
import com.ifeng.recallScheduler.utils.SpringConstantUtil;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.testng.collections.Lists;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by lijs
 * on 2017/9/27.
 */
@Service
public class EditorFocusUtil {
    @Autowired
    private EhCacheUtil ehCacheUtil;

    @Autowired
    private DocUtil docUtil;

    @Autowired
    private FilterService filterService;

    @Autowired
    private EsJpPoolQueryUtil esJpPoolQueryUtil;

    @Autowired
    private SpringConstantUtil springConstantUtil;

    protected static Logger log = LoggerFactory.getLogger(EditorFocusUtil.class);

    private volatile List<Document> focusList = Lists.newArrayList();

    public static LoadingCache<String, Integer> EditorFocusCache = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(3, TimeUnit.HOURS)
            .build(
                    new CacheLoader<String, Integer>() {
                        @Override
                        public Integer load(String s) throws Exception {
                            return 0;
                        }
                    }
            );


    @PostConstruct
    public void loadCache() {
        if (springConstantUtil.init_dev_close()) {
            return;
        }


        long start=System.currentTimeMillis();
        try {
            //获取ES中的焦点图数据
            List<Document> focusList = esJpPoolQueryUtil.getAllFocusPicDocument(1000, 24);

            log.info("load focus from es, size: {}", focusList == null ? 0 : focusList.size());


            //聚合数据并排重
            if (CollectionUtils.isEmpty(focusList)) {
                log.error("solr focus is Empty");
                focusList = Lists.newArrayList();
            }

            focusList.forEach(this::setFocusType);
            updateFocus(focusList);

            ehCacheUtil.put(CacheFactory.CacheName.EditorFocus.getValue(), CacheFactory.CacheName.EditorFocus.getValue(), focusList);
            log.info("update updateEditorFocus from solr {}", focusList.size());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("updateEditorFocus ERROR:{}", e);
        }finally {
            log.info("EditorFocusUtil init cost:{}", System.currentTimeMillis() - start);
        }
    }


    /**
     * 更新焦点图列表
     * 注意：初始化时被调用一次，后续只应由后台定时线程调用，避免并发问题
     *
     * @param allFocus
     */
    public void updateFocus(List<Document> allFocus) {
        List<Document> list = new ArrayList<>(allFocus.size());
        list.addAll(allFocus);

        //根据热度从高到低排序
        Comparator<Document> hotComparator = (o1, o2) -> o2.getHotBoost().compareTo(o1.getHotBoost());
        list.sort(hotComparator);
        focusList = list;
    }

    private void setFocusType(Document focus) {
        focus.setWhy(RecWhy.WhyJpPoolFocus);
        if (focus.getExt() == null) {
            Map<String, Object> ext = new HashMap<>();
            focus.setExt(ext);
        }
        focus.getExt().put(GyConstant.displayType, GyConstant.focusDisplayType);
    }


    /**
     * 根据用户id轮播焦点图
     *
     * @param requestInfo
     * @param result
     * @param neeNum
     */
    public void fillWithHotFocus(RequestInfo requestInfo, List<Document> result, List<Document> allFocus, int neeNum) {
        //获得用户浏览的游标
        List<Document> list = allFocus;
        List<Document> doclist = new ArrayList<>();
        doclist.addAll(allFocus);
        Collections.shuffle(doclist);

        for (Document doc : doclist) {
            if (result.size() >= neeNum) {
                break;
            }
            if (!contains(result, doc)) {
                result.add(doc);
            }
        }
    }

    /**
     * 判断集合中是否有指定的文章
     *
     * @param documents 集合
     * @param target    要判断的文章
     * @return
     */
    private boolean contains(List<Document> documents, Document target) {
        String simId = target.getSimId();
        for (Document document : documents) {
            if (document.getSimId().equals(simId)) {
                return true;
            }
        }
        return false;
    }


    private Integer getFocusCursor(String uid) {
        try {
            return EditorFocusCache.get(uid);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("EditorFocusCache获取游标失败!!!  error:{}", e);
        }
        return null;
    }
}
