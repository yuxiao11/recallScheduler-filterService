package com.ifeng.recallScheduler.user;

import lombok.Getter;
import lombok.Setter;

/**
 * 不同登录平台下的session信息
 * Created by jibin on 2018/2/23.
 */
@Getter
@Setter
public class NetStatSession {

    /**
     * 初始视频条数
     */
    private int videoNumBase = 3;

    public NetStatSession() {

    }

    public NetStatSession(int videoNumBase) {
        this.videoNumBase = videoNumBase;
    }
}
