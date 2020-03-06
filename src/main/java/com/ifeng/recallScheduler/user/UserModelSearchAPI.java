package com.ifeng.recallScheduler.user;

import com.beust.jcommander.internal.Maps;

import com.ifeng.recallScheduler.request.RequestInfo;
import com.ifeng.userpf.entity.ClientType;
import com.ifeng.userpf.upserver.UserProfileOnlineClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.ifeng.userpf.client.UserProfileManager;

import java.util.Map;
import java.util.Set;

/**
 * Created by jibin on 2018/4/8.
 */
@Service
public class UserModelSearchAPI {

    private static final Logger logger = LoggerFactory.getLogger(UserModelSearchAPI.class);

    static {
        //======资源加载
        UserProfileManager.initOnline(400, ClientType.HOT, "UserCenter_Official");; //进程启动时加载一次,重操作
    }


    /**
     * 查询主方法，根据调用方传过来的参数查询对应字段
     *
     * @param uid
     * @param UserInfo_COLUMN
     * @return
     */
    public static Map<String, String> getUserInfo(String uid, Set<String> UserInfo_COLUMN) {
        Map<String, String> part_userprofileMap = null;
        try {
            //查询部分画像内容
            part_userprofileMap = UserProfileOnlineClient.searchPartUserProfileByUid(uid, UserInfo_COLUMN);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("{} getUserInfo ERROR:{}", uid, e);
        }
        if (part_userprofileMap == null) {
            part_userprofileMap = Maps.newHashMap();
        }
        return part_userprofileMap;
    }


    /**
     * 查询主方法，根据调用方传过来的参数查询对应字段
     * 只查询cache，不写cache，依赖服务内自己的cache进行保存数据
     *
     * @param uid
     * @param realTimeCols
     * @param offLineCols
     * @return
     */
    @Deprecated
    public static Map<String, String> searchUserProfileByCols(String uid, String[] realTimeCols, String[] offLineCols) {
        Map<String, String> part_userprofileMap = null;
        try {
            //查询部分画像内容
            part_userprofileMap = UserProfileOnlineClient.searchUserProfileByCols(uid, realTimeCols, offLineCols);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("{} searchUserProfileByCols ERROR:{}", uid, e);
        }
        if (part_userprofileMap == null) {
            part_userprofileMap = Maps.newHashMap();
        }
        return part_userprofileMap;
    }



    public static void main(String[] args) {
        String uid = "6fbe328c2118421c8aaa60dafd3a6bdf"; //用户uid
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setUserId(uid);


        try {
            //查询全部画像内容
//            Map<String, String> whole_userprofileMap = UserProfileSearchClient.searchWholeUserProfileByUid(uid);
            //查询部分画像内容
//            System.out.println(whole_userprofileMap);
//            System.out.println(JsonUtil.object2jsonWithoutException(part_userprofileMap));



        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            UserProfileManager.closeOnline(); //进程关闭时释放,必须执行
        }
    }
}
