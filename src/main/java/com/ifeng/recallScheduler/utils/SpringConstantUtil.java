package com.ifeng.recallScheduler.utils;

import com.ifeng.recallScheduler.constant.GyConstant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Created by jibin on 2017/9/8.
 */
@Component
public class SpringConstantUtil {
    /**
     * 当前的系统的环境变量
     */
    @Value("${environment}")
    private String environment;

    /**
     * 推荐个性化的增量回传的kafka  group
     */
    @Value("${kafka.UpdateSyncGroup}")
    private  String groupUpdateSync ;



    public boolean isDev() {
        return (GyConstant.CONFIG_DEVELOP.equals(environment));
    }


    /**
     * dev环境关闭部分非必要的初始化
     *
     * @return
     */
    public boolean init_dev_close() {
        return (!GyConstant.online_switch_init);
    }

    public String getGroupUpdateSync() {
        return groupUpdateSync;
    }
}
