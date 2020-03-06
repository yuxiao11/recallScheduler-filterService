package com.ifeng.recallScheduler.utils;

import java.util.UUID;

/**
 * Created by liligeng on 2019/9/4.
 * rToken生成工具类，用于rToken及sessionId
 *
 *
 */
public class RTokenGenerator {

    /**
     * 生成sessionId
     * @return
     */
    public static String sessionIdBuilder(){
        UUID uuid = UUID.randomUUID();
        uuid.getMostSignificantBits();
        return Long.toHexString(uuid.getMostSignificantBits());
    }

    /**
     * 旧版rToken去掉连字符
     * @return
     */
    public static String rTokenBuilder(){
        UUID rTokenUUID = UUID.randomUUID();
        rTokenUUID.getMostSignificantBits();
        StringBuilder sb = new StringBuilder();
        sb.append(Long.toHexString(rTokenUUID.getMostSignificantBits()));
        sb.append(Long.toHexString(rTokenUUID.getLeastSignificantBits()));
        return sb.toString();
    }

    /**
     *
     * 生成新版rToken，带sessionId
     * @param sessionId
     * @return
     */
    public static String sessionRTokenBuilder(String sessionId){
        UUID rTokenUUID = UUID.randomUUID();
        rTokenUUID.getMostSignificantBits();
        StringBuilder sb = new StringBuilder();
        sb.append(sessionId).append("-");
        sb.append(Long.toHexString(rTokenUUID.getMostSignificantBits()));
        sb.append(Long.toHexString(rTokenUUID.getLeastSignificantBits()));
        return sb.toString();
    }

    /**
     * uuid去掉连字符
     * @return
     */
    public static String uuidWithoutHyphen(){
        return rTokenBuilder();
    }

    /**
     * uuid带连字符
     * @return
     */
    public static String uuidWithHyphen(){
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }


    public static void main(String[] args) {
        for(int i = 0; i<100; i++) {
            String rToken = rTokenBuilder();
            System.out.println(rToken);
        }

    }

}
