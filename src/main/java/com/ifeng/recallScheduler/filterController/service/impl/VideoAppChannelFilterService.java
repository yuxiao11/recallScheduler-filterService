package com.ifeng.recallScheduler.filterController.service.impl;

import com.beust.jcommander.internal.Sets;
import com.ctrip.framework.apollo.Apollo;
import com.ifeng.recallScheduler.constant.GyConstant;
import com.ifeng.recallScheduler.constant.RecomChannelEnum;
import com.ifeng.recallScheduler.item.Document;
import com.ifeng.recallScheduler.request.RequestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Created by jibin on 2018/1/22.
 */
@Service
public class VideoAppChannelFilterService {

    private final static Logger log = LoggerFactory.getLogger(VideoAppChannelFilterService.class);

    public static Set<String> docSourceSet = Sets.newHashSet();

    private static Logger logger = LoggerFactory.getLogger(Apollo.class);

    static {
        docSourceSet.add(GyConstant.sourceMrMdm);
        docSourceSet.add(GyConstant.sourceLyYy);
    }


    /**
     * 针对视频app的内容过滤
     *
     * @param requestInfo
     * @param doc
     * @return
     */
    public boolean needFilter(RequestInfo requestInfo, Document doc) {

        String recomChannel = requestInfo.getRecomChannel();
        if (RecomChannelEnum.videoapp.getValue().equals(recomChannel)) {
            if (docSourceSet.contains(doc.getSource())) {
                log.info("{} videoapp needFilter [doc source] {},type:[{}],source:[{}],title:[{}]", requestInfo.getUserId(), doc.getDocId(), doc.getDocType(), doc.getSource(), doc.getTitle());
                return true;
            }
        }
        return false;
    }
}
