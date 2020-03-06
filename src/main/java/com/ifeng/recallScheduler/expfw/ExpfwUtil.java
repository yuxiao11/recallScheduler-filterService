package com.ifeng.recallScheduler.expfw;


import com.ifeng.recallScheduler.expfw.otherExp.OtherExpLogUtil;
import com.ifeng.recallScheduler.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by jibin on 2017/12/25.
 */
@Service
public class ExpfwUtil {

    @Autowired
    private OtherExpLogUtil otherExpLogUtil;


    /**
     * abtest总的分流口
     *
     * @param requestInfo
     */
    public void doCommonAbtest(RequestInfo requestInfo) {

        //ab测实验
        otherExpLogUtil.doRecallAbTest(requestInfo);


    }
}
