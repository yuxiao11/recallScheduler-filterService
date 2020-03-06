package com.ifeng.recallScheduler.bloomFIlter_long;


import com.ifeng.recallScheduler.bloomFilter.MasterSlaveBloomFilter;
import com.ifeng.recallScheduler.constant.GyConstant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BloomFilterLong {
	
	private static final Logger LOG = LoggerFactory.getLogger(BloomFilterLong.class);
	
	private static MasterSlaveBloomFilter masterSlaveBloomFilter;

	static {
//		String configFileDir = "/data/prod/service/recom-toutiao/output/bloomdump_long/";
		String configFileDir = "/data/service/recom-toutiao/output/bloomdump_long/";
		masterSlaveBloomFilter = new MasterSlaveBloomFilter(configFileDir);
	}

	public static boolean checkIsInAndPut(String uid, String docID) {
		return masterSlaveBloomFilter.checkIsInAndPut(uid, docID);
	}

	/**
	 * 如果已经存在 则返回true
	 * @param uid
	 * @param docID
	 * @return
	 */
	public static boolean onlyCheck(String uid, String docID) {
		return masterSlaveBloomFilter.onlyCheck(uid,docID);
	}

	public static boolean onlyCheck(String uid, String docID, String groupID) {
		if (masterSlaveBloomFilter.onlyCheck(uid,docID)){
			return true;
		}
		if(StringUtils.isNotBlank(groupID)) {
			return masterSlaveBloomFilter.onlyCheck(uid, groupID);
		}
		return false;
	}

	/**
	 * 将展现结果插入布隆过滤器
	 * 运维监控的测uid都是以 _test结尾，如果以 _test结尾，则不走布隆过滤器
	 * @param uid
	 * @param docID
	 */
	public static void onlyPut(String uid, String docID) {
		if (uid.endsWith(GyConstant.UidEnd_Test)) {
			return;
		}
		masterSlaveBloomFilter.onlyPut(uid,docID);
	}


    public static void onlyPut(String uid, String docID, String groupID) {
        if (uid.endsWith(GyConstant.UidEnd_Test)) {
            return;
        }
        masterSlaveBloomFilter.onlyPut(uid,docID);
        if(StringUtils.isNotBlank(groupID)) {
            masterSlaveBloomFilter.onlyPut(uid, groupID);
        }
    }
}
