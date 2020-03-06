package com.ifeng.recallScheduler.expfw.otherExp;


import com.ifeng.recallScheduler.constant.AbTestConstant;
import com.ifeng.recallScheduler.constant.GyConstant;
import com.ifeng.recallScheduler.constant.cache.CacheFactory;
import com.ifeng.recallScheduler.request.RequestInfo;
import com.ifeng.recallScheduler.utils.DebugUtil;
import com.ifeng.recallScheduler.utils.JsonUtil;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 通用的abtest控制，主要针对个性化帖子
 * Created by jibin on 2017/12/25.
 */
@Service
public class OtherExpLogUtil {

    private static Logger logger = LoggerFactory.getLogger(OtherExpLogUtil.class);

    @Autowired
    private CacheManager cacheManager;




    /**
     * 和冯小伟约定的 dataType测试
     * @param requestInfo
     */
    public void doDataTypeTest(RequestInfo requestInfo) {
        try {
            StringBuilder group_key = new StringBuilder(AbTestConstant.Abtest_Group_DataType_Test);
            String os = requestInfo.getOs();
            if (StringUtils.isBlank(os)) {
                return;
            }

            if (os.contains(GyConstant.android)) {
                group_key.append(GyConstant.android);
            } else if (os.contains(GyConstant.ios)) {
                group_key.append(GyConstant.ios);
            }
            requestInfo.addAbtestInfo(group_key.toString(), requestInfo.getDataType());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("{} doDataTypeTest ERROR:{}", requestInfo.getUserId(), e);
        }
    }

    /**
     * ctr标记实验
     *
     * @param requestInfo
     */
    public void doRecallAbTest(RequestInfo requestInfo) {
        try {
            //------------更新ctr的ab测标记
            Cache abtestCache = cacheManager.getCache(CacheFactory.CacheName.AbTest.getValue());
            Element abtestElement = abtestCache.get(requestInfo.getUserId());
            Object abtest = null;
            if (abtestElement != null) {
                abtest =  abtestElement.getObjectValue();
            }

            if(abtest instanceof Map){
                Map<String,String> abtestMap=(Map<String,String>)abtest;
                for(Map.Entry<String, String> ab: abtestMap.entrySet()){
                    requestInfo.addAbtestInfo(ab.getKey(), ab.getValue());
                }
            }else{
                String abtestStr=(String)abtest;
                abtest = StringUtils.defaultString(abtestStr, GyConstant.abtest_Default);
                requestInfo.addAbtestInfo(AbTestConstant.Abtest_Group_ctrTest, abtestStr);
                requestInfo.setAbtest(abtestStr);
            }
            DebugUtil.debugLog(requestInfo.isDebugUser(),"{} doRecallAbTest abValue:{}",requestInfo.getUserId(),JsonUtil.object2jsonWithoutException(abtest));
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("{} doCtrTest ERROR:{}", requestInfo.getUserId(), e);
        }
    }
}
