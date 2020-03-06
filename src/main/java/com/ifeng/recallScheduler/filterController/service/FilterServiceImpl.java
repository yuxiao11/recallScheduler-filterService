package com.ifeng.recallScheduler.filterController.service;

import com.ifeng.recallScheduler.item.Document;
import com.ifeng.recallScheduler.request.RequestInfo;

import java.util.Set;

public interface FilterServiceImpl {

    /**
     * 对增量doc结果 进行判断，判读是否需要过滤
     *
     * @param requestInfo
     * @param doc
     * @return
     */


    boolean needFilter(RequestInfo requestInfo, Document doc, Set<String> focusSimids, Set<String> regularSimids, Set<String> sansuSimIdSet, boolean needLocalFilter, String recomReason);

}