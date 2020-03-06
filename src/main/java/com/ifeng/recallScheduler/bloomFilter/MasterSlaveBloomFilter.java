package com.ifeng.recallScheduler.bloomFilter;

//import cn.pezy.engine.model.cluster.SimNewsCluster;
//import cn.pezy.engine.model.subchannel.ElaborationChannel;
//import cn.pezy.engine.recom.module.RecomModuleManager.RecomType;
//import cn.pezy.engine.recom.util.SampleUser;

import com.ifeng.recallScheduler.constant.GyConstant;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MasterSlaveBloomFilter {
	
	private static final Logger logger = LoggerFactory.getLogger(MasterSlaveBloomFilter.class);
	
	private final int folder = 20;
	private ConfBloomFilterLocal cbf1 = null;
	private ConfBloomFilterLocal cbf2 = null;
	private ConcurrentHashMap<String,byte[]> bytesCache = new ConcurrentHashMap<String,byte[]>();
	private final String DATA_FILTER_CBF1_ID="LOG_FILTER_TO_TEST_CBF1";
	private final String DATA_FILTER_CBF2_ID="LOG_FILTER_TO_TEST_CBF2";
	private final String TOKEN_CBF1="cb11";
	private final String TOKEN_CBF2="cb22";

	/**
	 * 初始日期,布隆过滤器定时任务计算时间周期使用，不能轻易修改
	 */
	public final String firstDateStr = "2017-7-11";
	
	private static ExecutorService executorService = Executors.newFixedThreadPool(5);

    public MasterSlaveBloomFilter(String configFileDir) {
		//String configFileDir= Config.getLocal().getProperty(ConstantEngine.BLOOM_FILTER_LOCAL_DUMP_DIR,"/opt/bloomdump/");
		try {
			cbf1 = new ConfBloomFilterLocal(configFileDir,DATA_FILTER_CBF1_ID, folder);
		} catch (Throwable e) {
			logger.error("", e);
		}
		try {
			cbf2 = new ConfBloomFilterLocal(configFileDir,DATA_FILTER_CBF2_ID, folder-5);
		} catch (Throwable e) {
			logger.error("", e);
		}
        alternativeDump();
        refreshCache();
	}


	/**
	 * 获取距离初始时间的时间间隔，用来计算当前状态应该使用的redis的库的编号
	 *
	 * @return
	 */
	private long getDayCount() {
		long dayCount = 0;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date startDate = sdf.parse(firstDateStr);
			Date nowDate = new Date();//取时间

			long nd = 1000 * 24 * 60 * 60;
			// 获得两个时间的毫秒时间差异
			long diff = nowDate.getTime() - startDate.getTime();

			// 计算差多少天
			dayCount = diff / nd;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("getDayNum ERROR:{}", e);
		}
		return dayCount;
	}

	public void alternativeDump() {

		//如果非线上环境则不用持久化，避免本地debug内存不足
		if (!GyConstant.online_switch_init) {
			logger.info("非线上环境，布隆不进行持久化！！！ 请谨慎使用");
			return;
		}

		Thread d = new Thread() {
			public void run() {
				int times=0;
				int hourLast = new Date().getHours();
				while (true) {
					try {
						if (times % 5 == 0) { //times=0->2->3->4->5  ->0->2......
							times = 1;
							try {
								cbf1.checkBloom(true);
								cbf2.checkBloom(true);
							} catch (Exception e1) {
								logger.error("", e1);
							}
						}
						times++;
						sleep(200*1000);
						try {
							long startTime=System.currentTimeMillis();
							logger.info("bloom dump start");
							cbf1.dump();
							cbf2.dump();
							logger.info("bloom dump finish,cost:{}",System.currentTimeMillis()-startTime);
						} catch (Throwable e2) {
							logger.error("", e2);
						}

						//date 1 hour 3 clean cbf1; date 3 hour 3 clean cbf2
						Date d = new Date();
						int curHours=d.getHours();
						long curDateCount=getDayCount();
						if (hourLast != curHours) {//with the follow `hourLast = d.getHours()`,so every hour run one time
							if (curHours == 3) {//date 1 hour 3 clean cbf1; date 3 hour 3 clean cbf2
								// 每隔20天清空其中一个cbf，这样线上的布隆过滤器的数据有效期范围是 2天
								if (curDateCount % 20 == 0) {// 每隔20天清空一次，cbf1  只有被20整除的时候清理
										try {
											cbf1.cleanAll();
											cbf1.stop();
										} catch (Exception e) {
											logger.error("curDate="+curDateCount+",curHours="+curHours, e);
										}
								} else if(curDateCount % 20 == 10) {// 每隔20天清空一次，cbf2  只有被20除，余数为10的时候清理
										try {
											cbf2.cleanAll();
											cbf2.stop();
										} catch (Exception e) {
											logger.error("curDate="+curDateCount+",curHours="+curHours, e);
										}
									}
							}
							hourLast = curHours;
						}
					} catch (Throwable e) {
						logger.error("", e);
					}
				}
			}
		};
		d.setDaemon(true);
/////	d.start();
		executorService.execute(d);
	}

	public void refreshCache() {
		Thread dc=new Thread(){
			public void run()
			{
				while(true)
				{
					try {
						bytesCache=new ConcurrentHashMap<String,byte[]>();
					} catch (Throwable e) {
						logger.error("",e);
					}
					try {
						sleep(3000);
					} catch (InterruptedException e) {
						logger.error("",e);
					}
				}
			}
		};
		dc.setDaemon(true);
////	dc.start();
		executorService.execute(dc);
	}



	private byte[] getByte(String key)
	{
		return Bytes.toBytes(key);
	}
	public boolean checkIsInAndPut(String uid, String docID) {

		try {
			String checkKey = uid + ":" + docID;
			boolean docIDCheck = false;
			boolean doubleCheck = false;
			try {
				doubleCheck = cbf1.checkAndPut(getByte(TOKEN_CBF1+":"+checkKey));
			} catch (Throwable e) {
				logger.error("", e);
			}
			boolean doubleCheck2 = false;
			try {
				doubleCheck2 = cbf2.checkAndPut(getByte(TOKEN_CBF2+":"+checkKey));
			} catch (Throwable e) {
				logger.error("", e);
			}

//			logger.debug("double check 检查docID uid：{} get cluster ID of:{}  first check:{} second check:{} third check:{}", uid, docID, docIDCheck, doubleCheck, doubleCheck2);

			if (docIDCheck) {
				return docIDCheck;
			}

			if (doubleCheck2) {
				return doubleCheck2;
			}
			if (doubleCheck) {
				return doubleCheck;
			}

//			try {
//				SimNewsCluster ins = SimNewsCluster.getInstance();
//				String clusterID = ins.getClusterId(docID);
//				if (isdebug) {
//					LOG.info("获取clusterID type" + type + " 用户：" + uid + ""
//							+ " get cluster ID of:" + docID + " clusterID is:"
//							+ clusterID);
//				}
//				if (clusterID != null && !clusterID.equals("")
//						&& !clusterID.startsWith("-1")) {
//					String toset = "c:" + uid + ":" + clusterID;
//					boolean clusterIDCheck = false;
//					clusterIDCheck =cbf1.checkAndPut(getByte(
//							toset));
//					if (isdebug) {
//						LOG.info("检查clusterID type" + type + " 用户：" + uid
//								+ " set cluster ID of:" + toset + " result is:"
//								+ (clusterIDCheck ? "看过" : "没看过"));
//					}
//					if (clusterIDCheck)
//						return clusterIDCheck;
//				}
//			} catch (Throwable e) {
//				LOG.error("", e);
//			}
			return false;
		} catch (Exception e) {
			logger.error("", e);
			return false;
		}
	}

	public boolean onlyCheck(String uid, String docID) {

		return
				cbf1.onlyCheck(Bytes.toBytes(TOKEN_CBF1+":"+uid + ":" + docID)) ||
				cbf2.onlyCheck(Bytes.toBytes(TOKEN_CBF2+":"+uid + ":" + docID));
	}

	public void onlyPut(String uid, String docID) {
		String checkKey = uid + ":" + docID;
		
		try {
			cbf1.onlySet(Bytes.toBytes(TOKEN_CBF1+":"+checkKey));
			cbf2.onlySet(Bytes.toBytes(TOKEN_CBF2+":"+checkKey));
		} catch (Throwable e) {
			logger.error("", e);
		}
	}

//	//@fuyao test
//	public static String getRandomString(int length) {
//		String base = "abcdefghijklmnopqrstuvwxyz0123456789";
//		Random random = new Random();
//		StringBuffer sb = new StringBuffer();
//		for (int i = 0; i < length; i++) {
//			int number = random.nextInt(base.length());
//			sb.append(base.charAt(number));
//		}
//		return sb.toString();
//	}
//
//	public static void main(String[] args){
////		System.out.println(cbf1.getFilter().getHashCount());
//		String engine = "engine";
//		for (int i =0;i<100;i++){
//			String news ="15212235487"+i;
//			String uid = "512321554234";
////			System.out.println(checkAndPut(uid, news, engine,false,false));
//			//System.out.println(NewsCheckAndPut.checkAndPut(uid, news, engine,false,true));
//		}
//		//cbf1.dump();
//		//cbf2.dump();
//		System.out.println("second check : ");
//		for (int i =0;i<100;i++){
//			String news ="15212235487"+i;
//			String uid = "512321554234";
//			if (onlyCheck(uid, news)){
//				System.out.println("Number "+i+" is TRUE");
//			}
////			System.out.println(checkAndPut(uid, news, engine,false,false));
//			//System.out.println(NewsCheckAndPut.checkAndPut(uid, news, engine,false,true));
//		}
//
//	}

}
