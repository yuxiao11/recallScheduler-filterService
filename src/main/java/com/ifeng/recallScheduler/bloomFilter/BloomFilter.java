package com.ifeng.recallScheduler.bloomFilter;


import com.ifeng.recallScheduler.bloomFIlter_long.BloomFilterLong;
import com.ifeng.recallScheduler.constant.GyConstant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BloomFilter {

    private static final Logger LOG = LoggerFactory.getLogger(BloomFilter.class);

    private static MasterSlaveBloomFilter masterSlaveBloomFilter;

    static {
//        String configFileDir = "/data/prod/service/recom-toutiao/output/bloomdump/";
        String configFileDir = "/data/service/recom-toutiao/output/bloomdump/";
        masterSlaveBloomFilter = new MasterSlaveBloomFilter(configFileDir);
    }


    /**
     * 如果已经存在 则返回true
     *
     * @param uid
     * @param simId
     * @return
     */
    public static boolean onlyCheck(String uid, String simId) {
        if (uid.endsWith(GyConstant.UidEnd_Test)) {
            return false;
        }

        boolean result_1 = masterSlaveBloomFilter.onlyCheck(uid, simId);

        boolean result_2 = false;

        if (GyConstant.online_switch_init && !GyConstant.linuxLocalIp.equals("10.21.6.95")) {
            result_2 = BloomFilterLong.onlyCheck(uid, simId);
        }

        boolean result = (result_1 || result_2);
        return result;
    }

    /**
     * 加入groupId 布隆
     * @param uid uid
     * @param simId simId
     * @param groupID groupId
     * @return bool
     */
    public static boolean onlyCheck(String uid, String simId, String groupID) {

        if (uid.endsWith(GyConstant.UidEnd_Test)) {
            return false; //测试用户不检查
        }

        boolean result_1 = masterSlaveBloomFilter.onlyCheck(uid, simId); //检查simId

        boolean result_2 = false;
        if(StringUtils.isNotBlank(groupID)) { //检查groupId
            result_2 = masterSlaveBloomFilter.onlyCheck(uid, groupID);
        }

        boolean result_3 = false; //检查长效布隆
        if (GyConstant.online_switch_init && !GyConstant.linuxLocalIp.equals("10.21.6.95")) {
            result_3 = BloomFilterLong.onlyCheck(uid, simId, groupID);
        }

        return (result_1 || result_2 || result_3);
    }

    /**
     * 将展现结果插入布隆过滤器
     * 运维监控的测uid都是以 _test结尾，如果以 _test结尾，则不走布隆过滤器
     *
     * @param uid
     * @param simId
     */
    @Deprecated
    public static void onlyPut(String uid, String simId) {
        if (uid.endsWith(GyConstant.UidEnd_Test)) {
            return;
        }
        masterSlaveBloomFilter.onlyPut(uid, simId);

        //双写,测试环境内存不够，不写入
        if (GyConstant.online_switch_init && !GyConstant.linuxLocalIp.equals("10.21.6.95")) {
            BloomFilterLong.onlyPut(uid, simId);
        }
    }


    public static void onlyPut(String uid, String simId, String groupID) {
        if (uid.endsWith(GyConstant.UidEnd_Test)) {
            return;
        }
        masterSlaveBloomFilter.onlyPut(uid, simId);
        if(StringUtils.isNotBlank(groupID)) {
            masterSlaveBloomFilter.onlyPut(uid, groupID);
        }

        //双写,测试环境内存不够，不写入
        if (GyConstant.online_switch_init && !GyConstant.linuxLocalIp.equals("10.21.6.95")) {
            BloomFilterLong.onlyPut(uid, simId, groupID);
        }
    }

    /**
     * 将展现结果插入布隆过滤器
     * 运维监控的测uid都是以 _test结尾，如果以 _test结尾，则不走布隆过滤器
     *
     * @param uid
     * @param simId
     */
    @Deprecated
    public static void reAddOnlyPut(String uid, String simId) {
        BloomFilterLong.onlyPut(uid, simId);
    }

}
