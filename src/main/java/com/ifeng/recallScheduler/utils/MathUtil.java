package com.ifeng.recallScheduler.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;

/**
 * Created by jibin on 2017/7/19.
 */
public class MathUtil {

    protected static Logger logger = LoggerFactory.getLogger(MathUtil.class);

    /**
     * 实验分流的分母
     */
    private static final int rateMax = 100;

    /**
     * 实验分流的分母
     */
    private static final int rateMaxNew = 10000;

    private static final int rateMax_hundred_thousand = 100000;

    private static final int rateMax_thousand = 1000;

    /**
     * 生成0到max的随机数， 不包括max的 开区间
     *
     * @param max
     * @return
     */
    public static int getNum(int max) {
        return (int) (Math.random() * max);
    }



    /**
     * 使用uid进行abtest分组，分母为100，同一个uid 会打到同一个分组中
     *
     * @param uid
     * @return
     */
    @Deprecated
    public static long getNumByUid(String uid) {
        CRC32 crc32 = new CRC32();
        crc32.update((uid).getBytes());
        long result = crc32.getValue() % rateMax;
        return result;
    }


    /**
     * 使用uid进行abtest分组，分母为100，同一个uid 会打到同一个分组中
     *
     * @param uid
     * @return
     */
    public static long getNumByUid(String uid, String group) {
        CRC32 crc32 = new CRC32();
        crc32.update((uid + group).getBytes());
        long result = crc32.getValue() % rateMax;
        return result;
    }

    /**
     * 使用uid进行abtest分组，分母为10000，同一个uid 会打到同一个分组中
     * todo return the number which is between 0 to 99
     * @param uid
     * @return
     */
    public static long getNumByUidNew(String uid, String group) {
        CRC32 crc32 = new CRC32();
        crc32.update((uid + group).getBytes());
        long result = crc32.getValue() % rateMaxNew;
        return result;
    }

    /**
     * 使用uid进行abtest分组，分母为100000，同一个uid 会打到同一个分组中 reverse 版本
     * @param uid uid
     * @return int
     */
    public static int getNumByUidAndGroup(String uid, String group) {
        CRC32 crc32 = new CRC32();
        crc32.update(StringUtils.reverse(uid + group).getBytes());
        long crcNum = crc32.getValue() % rateMax_hundred_thousand;
        int result = (int) (crcNum / rateMax_thousand);
        return result;
    }

    public static long getNumByMd5(String uid, String group) {
        long num = 0;
        try {
            MessageDigest messageDigest = null;
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update((uid + group).getBytes());
            byte[] resultByteArray = messageDigest.digest();
            for (int offset = 0; offset < resultByteArray.length; offset++) {
                num = num + Math.abs(resultByteArray[offset]);
            }
        } catch (Exception e) {
           logger.error("{} getNumByMd5 error:{}",uid,e);
           return 101;
        }
        return num%rateMax;
    }


    /**
     * 判断是实验路流量
     *
     * @param uid
     * @param rate_MaxNum
     * @return
     */
    @Deprecated
    public static boolean isTestFlowByUid(String uid, int rate_MaxNum) {
        return (getNumByUid(uid) < rate_MaxNum);
    }


    /**
     * 格式刷数字结果进行输入
     * @param newScale
     * @param oldNumStr
     * @return
     */
    public static String getFormatNum(int newScale, String oldNumStr) {
        if (StringUtils.isBlank(oldNumStr)) {
            return oldNumStr;
        }
        String numStrNew = "";
        try {
            BigDecimal b = new BigDecimal(oldNumStr);
            double numNew = b.setScale(newScale, BigDecimal.ROUND_FLOOR).doubleValue();
            numStrNew = String.valueOf(numNew);
        } catch (Exception e) {
            logger.error("getFormatNum ERROR:{}", e);
            numStrNew = oldNumStr;
        }
        return numStrNew;
    }


    public static void main(String[] args) {
//        System.out.println(getFormatNum(GyConstant.Loc_NewScale,"116.4874318719498"));
//        System.out.println(getFormatNum(GyConstant.Loc_NewScale,"116.484318719498"));
//        System.out.println(getFormatNum(GyConstant.Loc_NewScale,"116.4894318719498"));


//        CRC32 crc32 = new CRC32();
////        crc32.update(("865969031431182" + "abtest").getBytes());
////        long result = crc32.getValue() % rateMaxNew;
////
        byte[] btInput = ("865969031431182" + "abtest1").getBytes();
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        messageDigest.update(btInput);
        byte[] resultByteArray = messageDigest.digest();
        int i = 0;
        for (int offset = 0; offset < resultByteArray.length; offset++) {
            i = i + Math.abs(resultByteArray[offset]);
        }

        int a= i%10000;
        System.out.println(a);

    }
}
