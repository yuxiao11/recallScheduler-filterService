package com.ifeng.recallScheduler.bloomFilter;

//import cn.pezy.lightning.util.ConfigurationUtil;
//import org.apache.commons.collections.map.HashedMap;
//import org.apache.hadoop.hbase.util.Hash;
//import org.apache.hadoop.hbase.util.Pair;

import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.Map.Entry;

public class ConfBloomFilterLocal {
	private static final Logger logger = LoggerFactory.getLogger(ConfBloomFilterLocal.class);
	private boolean stop = false;
	private ByteBuffer bb = null;

	private ByteBloomFilter filter;
//	private static final int conflictKey = 20000000;//@fuyao max size
	 private final static int conflictKey= 20000000;
	private static final float errorRate = 0.0001f;//@fuyao max error rate
	private static final int keyFold = 7;
	private Date lastDate = new Date();
	private long lastUpdate = System.currentTimeMillis();
	private String PATH = "BLOOMFILTER_DUMP_FILE_CONF.dump";
	private String PATH_TMP = "BLOOMFILTER_DUMP_FILE.dump.temp";
	private String id;
	private int index = -1;
	//private int folders = ConfigurationUtil.getConf().getInt("cn.pezy.lightning.bloomfilter.folders", 8);
	private int folders = 8;
	private String DIR = "D:\\documents\\test";
	private final HashMap<String, Pair<Long, Long>> filterRate = new HashMap<String, Pair<Long, Long>>();
	private HashMap<Integer, ConfBloomFilterLocal> maps = new HashMap<Integer, ConfBloomFilterLocal>();
	private int refreshInterInSec = 3600 * 24 * 30 * 6;
	private static long bbLimit;
	//private static final String DIR = ConfigurationUtil.getConf().get("bloom.dump.dir", "/opt/mrd/bloomdump/");
//	static
//	{
//		try {
//			File f=new File(DIR);
//			if(!f.exists())
//			{
//				f.mkdirs();
//			}
//		} catch (Throwable e) {
//			LOG.error("",e);
//		}
//	}
	private static final float fullRate = 0.6f;
	//
	static {
		try {
			ByteBloomFilter bbf = new ByteBloomFilter(conflictKey, errorRate, Hash.MURMUR_HASH, keyFold);
			bbf.allocBloom();//分配空间
			ByteArrayOutputStream bOut = new ByteArrayOutputStream();
			bbf.writeBloom(new DataOutputStream(bOut));
			bOut.flush();
			ByteBuffer bb = ByteBuffer.wrap(bOut.toByteArray());
			bbLimit = bb.limit();
		} catch (Throwable e) {
			logger.error("", e);
		}
	}

	// public ConfBloomFilterLocal(String ID,int refreshInterInSec)
	// {
	// this.id=ID;
	// this.PATH=id+"_"+PATH;
	// this.PATH_TMP=id+"_"+PATH_TMP;
	// this.refreshInterInSec=refreshInterInSec;
	// }
	public ConfBloomFilterLocal(String dir, String ID, int folders) {
		init(dir,ID);
		this.folders = folders;

	}

	public ConfBloomFilterLocal(String dir, String ID) {
		init(dir,ID);
	}

	public void init(String dir,String ID){
		this.DIR=dir;
		this.id = ID;
		this.PATH = DIR+"/"+id + "_" + PATH;
		this.PATH_TMP = DIR+"/"+id + "_" + PATH_TMP;
		initDir(this.DIR);
	}

	public void initDir(String DIR){
		try {
			File f=new File(DIR);
			if(!f.exists())
			{
				f.mkdirs();
			}
		} catch (Throwable e) {
			logger.error("DIR="+DIR,e);
		}
	}



	// {
	// Thread d=new Thread()
	// {
	// public void run()
	// {
	// int times=0;
	// while(!stop)
	// {
	// try {
	// printRate();
	// times++;
	// if (times % 10 == 0) {
	// synchronized (filterRate) {
	// filterRate.clear();
	// }
	// }
	// sleep(60000);
	// Date d=new Date();
	// long timeNow=System.currentTimeMillis();
	// if(d.getHours()==3&&Math.abs(d.getDate()-lastDate.getDate())>2&&(timeNow-lastUpdate)/1000>refreshInterInSec)
	// {
	// lastDate=d;
	// lastUpdate=timeNow;
	// synchronized(maps)
	// {
	// maps.clear();
	// }
	// }
	// } catch (Exception e) {
	// LOG.error("",e);
	// }
	// }
	// }
	// };
	// d.setDaemon(true);
	// d.start();

	private ConfBloomFilterLocal getFilter(byte[] key) {
		int mod = Math.abs(Arrays.hashCode(key)) % folders;
		ConfBloomFilterLocal bf = maps.get(mod);
		if (bf == null) {
			synchronized (maps) {
				bf = maps.get(mod);
				if (bf == null) {
					ConfBloomFilterLocal bfn = new ConfBloomFilterLocal(DIR,id + "_" + mod);
					maps.put(mod, bfn);
					bf = bfn;
				}
			}
		}
		return bf;
	}

	public boolean checkAndPut(byte[] key) {
		return getFilter(key).checkAndPutInner(key);
	}

	public boolean onlyCheck(byte[] key) {
		return getFilter(key).onlyCheckInner(key);
	}

	final private Pair<Long, Long> getRate(String engine) {
		Pair<Long, Long> rate = filterRate.get(engine);
		if (rate == null) {
			synchronized (filterRate) {
				rate = filterRate.get(engine);
				if (rate == null) {
					rate = new Pair<Long, Long>(0L, 0L);
					filterRate.put(engine, rate);
				}
			}
		}
		return rate;
	}
	
	public void onlySet(byte[] key) {
		getFilter(key).onlySetInner(key);
	}


	public static HashSet<Integer> maxRate = new HashSet<Integer>();
	public void checkBloom(boolean delete) {
		checkBloom(delete,fullRate) ;
	}
	
	public void checkBloom(boolean delete,float rateToDelete) {

		HashSet<Integer> toClean = new HashSet<Integer>();
		HashSet<Integer> keySet = new HashSet<Integer>();
		int num = 0;
		while (num < 10) {
			try {
				keySet.clear();
				keySet.addAll(maps.keySet());
				for (Integer i : keySet) {
	
					ConfBloomFilterLocal value = maps.get(i);
					if (value != null) {
						try {
							float rate = value.checkRate();
							logger.info("full rate of " + rate + " "
									+ maps.get(i).PATH);
							if (rate > rateToDelete) {
								toClean.add(i);
							}
						} catch (Throwable e1) {
							logger.error("", e1);
						}
					}
				}
				break;
			} catch (Throwable e) {
				num++;
				logger.error("", e);
			}
		}
		maxRate.clear();
		maxRate.addAll(toClean);
		logger.info("not to put index:" + maxRate);
		if (delete)
			for (Integer i : toClean) {
				maps.get(i).cleanBB();
			}
	}

	private float checkRate() {
		try {
			if (this.bb != null && this.bb.hasArray()) {
				byte[] array = bb.array();
				int off = bb.arrayOffset();
				int limit = bb.limit();
				int isOne = 0;
				int notOne=0;
				for (int i = off; i < off + limit; i++) {
					if(i%10==0)
					{
						if (ByteBloomFilter.get(i, array, 0)) {
							isOne++;
						}
						else
						{
							notOne++;
						}
					}
				}
				logger.info("full rate is: " + (isOne / (float) (isOne+notOne))
						+ "  path is:" + this.PATH);
				return isOne / (float)(float) (isOne+notOne);

			}
		} catch (Throwable e) {
			logger.error("", e);
		}
		return 0;
	}

	public ConfBloomFilterLocal(int index) {
		super();
		this.index = index;
		PATH = DIR+"/"+index + PATH;
		PATH_TMP = DIR+"/"+index + PATH_TMP;
	}

	private void loadBB() {
		if (bb != null) {
			return;
		} else {
			synchronized (this) {
				if (bb == null) {
					File f = new File(PATH);
					if (f.exists() && f.length() > 10000) {
						FileInputStream fis = null;
						try {

							fis = new FileInputStream(PATH);
							logger.debug("Load BF from :"
									+ new File(PATH).getAbsolutePath());
							DataInputStream dop = new DataInputStream(fis);
							byte[] bytes = new byte[(int) f.length()];
							if (bytes != null && bytes.length > 100000) {
								dop.readFully(bytes);
								ByteBuffer bb = ByteBuffer.wrap(bytes);

								if (bbLimit != 0) {
									if (bb.limit() != bbLimit) {

										this.cleanBB();
									} else {
										this.bb = bb;
									}
								} else {
									this.bb = bb;
								}
								logger.debug("Load BF from :"
										+ new File(PATH).getAbsolutePath()
										+ " finished size is:" + bytes.length);
							} else {
								throw new IOException(
										"bf dump file size is too small:"
												+ f.length());
							}

							// LOG.debug("Load BF from :"+new
							// File(PATH).getAbsolutePath()+" finished size is:"+bytes.length);
						} catch (Exception e) {
							this.cleanBB();
							logger.error("", e);
						} finally {
							if (fis != null) {
								try {
									fis.close();
								} catch (IOException e) {
									logger.error("", e);
								}
							}
						}
					}
				}

			}
		}
	}

	public void move(File srcFile, File destFile) {

		destFile.delete();

		srcFile.renameTo(destFile);
	}

	public void dump() {
		for (ConfBloomFilterLocal bfl : maps.values()) {
			if (bfl != null) {
				try {
					bfl.dumpBBInner();
				} catch (Exception e) {
					logger.error("", e);
				}
			}
		}
	}

	public void dumpBBInner() throws FileNotFoundException {
		boolean finished = false;
		FileOutputStream fos = null;
		try {
			
			fos = new FileOutputStream(PATH_TMP);
			DataOutputStream dop = new DataOutputStream(fos);

			if (this.bb != null && this.bb.hasArray()) {
				dop.write(bb.array(), bb.arrayOffset(), bb.limit());
				logger.debug("Dump BF to :" + new File(PATH_TMP).getAbsolutePath()
						+ " finished size is:" + bb.limit());
				finished = true;
			}
		} catch (IOException e) {
			logger.error("", e);
		} finally {
			if (fos != null) {
				try {
					fos.close();
					if (finished) {
						move(new File(PATH_TMP), new File(PATH));
						logger.debug("move from :" + PATH_TMP + " to " + PATH);
					}
				} catch (IOException e) {
					logger.error("", e);
				}
			}
		}
	}
	public void cleanAll()
	{
		HashSet<Integer> toClean = new HashSet<Integer>();
		HashSet<Integer> keySet = new HashSet<Integer>();
		int num = 0;
		while (num < 10) {
			try {
				keySet.clear();
				keySet.addAll(maps.keySet());
				for (Integer i : keySet) {
					maps.get(i).cleanBB();
				}
				break;
			}catch(Exception e)
			{
				logger.error("",e);
			}
		}
				
	}
	private void cleanBB() {
		synchronized (this) {
			try {
				ByteBloomFilter bbf = new ByteBloomFilter(conflictKey,
						errorRate, Hash.MURMUR_HASH, keyFold);
				bbf.allocBloom();
				ByteArrayOutputStream bOut = new ByteArrayOutputStream();
				bbf.writeBloom(new DataOutputStream(bOut));
				bOut.flush();
				bb = ByteBuffer.wrap(bOut.toByteArray());
			} catch (Exception e) {
				logger.error("", e);
			}
		}
	}

	public void stop() {
		this.stop = true;
	}

	public ByteBuffer getBufferedByte() throws IOException {
		if (bb == null) {
			synchronized (this) {
				if (bb == null) {
					loadBB();
				}
				if (bb == null) {
					ByteBloomFilter bbf = new ByteBloomFilter(conflictKey,
							errorRate, Hash.MURMUR_HASH, keyFold);
					bbf.allocBloom();
					ByteArrayOutputStream bOut = new ByteArrayOutputStream();
					bbf.writeBloom(new DataOutputStream(bOut));
					bOut.flush();
					bb = ByteBuffer.wrap(bOut.toByteArray());
				}
			}
		}
		return bb;
	}

	public ByteBloomFilter getFilter() {
		if (filter == null) {
			synchronized (this) {
				if (filter == null) {
					filter = new ByteBloomFilter(conflictKey, errorRate,
							Hash.MURMUR_HASH, keyFold);
				}
			}
		}
		return filter;
	}
	private void onlySetInner(byte[] key)
	{
		
		try {
			ByteBloomFilter newBf1 = this.getFilter();
			if(newBf1.getBloom()==null)
			{
				newBf1.contains(key, this.getBufferedByte());
			}
			newBf1.add(key);
			
		} catch (Throwable e) {
			logger.error("", e);
		}
	}
	//可以复用下面check方法
	private boolean checkAndPutInner(byte[] key) {
		boolean ret = false;
		try {
			ByteBloomFilter newBf1 = this.getFilter();
			ret = newBf1.contains(key, this.getBufferedByte());
			newBf1.add(key);
		} catch (Throwable e) {
			logger.error("", e);
		}
		return ret;
	}

	private boolean onlyCheckInner(byte[] key) {
		boolean ret = false;
		try {
			ByteBloomFilter newBf1 = this.getFilter();
			ret = newBf1.contains(key, this.getBufferedByte());
		} catch (Throwable e) {
			logger.error("", e);
		}
		return ret;
	}

	public static Random rand = new Random();

	// static ConcurrentHashSet<String> s = new ConcurrentHashSet<String>();

	public static void main(String args[]) throws IOException,
			InterruptedException {
		Date d = new Date();
		System.out.println(d.getDate());
		byte[] key1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		byte[] key2 = { 1, 2, 3, 4, 5, 6, 7, 8, 7 };
		byte[] key3 = { 1, 2, 3, 4, 5, 6, 4, 8, 9 };
		byte[] key4 = { 1, 2, 3, 4, 5, 6, 2, 8, 7 };

//		Map<Integer,byte[]> map = new HashMap<Integer,byte[]>();
//		for(int i=0;i<200;i++){
//			Random random = new Random();
//			byte[] array =new byte[9];
//			for (int j=0;j<9;j++){
//				array[j]= (byte)random.nextInt(20);
//			}
//			map.put(i,array);
//		}

		ConfBloomFilterLocal cbf = new ConfBloomFilterLocal("","FILTER_NEWS_PER_SOURCE",3);
//		int countTrue = 0;
//		for(int i=0;i<200;i++){
//			if(cbf.onlyCheck(map.get(i))){
//				countTrue++;
//			}
//			cbf.checkAndPut(map.get(i));
//			//System.out.println(i+":"+cbf.checkAndPut(map.get(i)));
//		}
//		System.out.println("first counttrue is "+countTrue);
		System.out.println(cbf.checkAndPut(key2));
		System.out.println(cbf.checkAndPut(key2));
		System.out.println(cbf.checkAndPut(key1));
		System.out.println(cbf.checkAndPut(key3));
		System.out.println(cbf.checkAndPut(key4));
		cbf.checkBloom(true);
//		cbf.dump();
//		cbf = new ConfBloomFilterLocal("FILTER_NEWS_PER_SOURCE",3);
//		countTrue = 0;
//		for(int i=0;i<200;i++){
//			if(cbf.onlyCheck(map.get(i))){
//				countTrue++;
//			}
////			System.out.println("secend : "+i+":"+cbf.checkAndPut(map.get(i)));
////			cbf.checkAndPut(map.get(i));
//		}
//		System.out.println(" second countTrue is "+countTrue);

		System.out.println(cbf.checkAndPut(key1));
		System.out.println(cbf.checkAndPut(key2));
		System.out.println(cbf.checkAndPut(key1));
		System.out.println(cbf.checkAndPut(key3));
		System.out.println(cbf.checkAndPut(key4));
		System.out.print(cbf.getBufferedByte().remaining());
		String test = "test";
		int right = 0;
		int wrong = 0;
		int i = 0;
		long time = 0;
		int last = 0;

	}

}
