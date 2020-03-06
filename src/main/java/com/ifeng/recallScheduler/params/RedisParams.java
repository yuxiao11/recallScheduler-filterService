package com.ifeng.recallScheduler.params;


import com.ifeng.recallScheduler.params.loader.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedisParams {

	protected static Logger logger = LoggerFactory.getLogger(RedisParams.class);
	
	private static volatile ConfigLoader configLoader;

	// ==================getters=========================
	private static ConfigLoader getConfigLoader() {

		if (configLoader == null) {
			synchronized (RedisParams.class) {
				if (configLoader == null) {
					configLoader = new ConfigLoader(
							"baseconfig/redis.properties");
				}
			}
		}

		return configLoader;
	}
	

	



	//凤凰热闻榜
	public static String getLongHotNews_IP() {
		return getConfigLoader().getString("LongHotNews_IP");
	}
	public static int getLongHotNews_port() {
		return getConfigLoader().getInt("LongHotNews_port");
	}
	public static int getLongHotNews_db() {
		return getConfigLoader().getInt("LongHotNews_db");
	}

	//凤凰热闻榜
	public static String getIfengHotVideo_IP() {
		return getConfigLoader().getString("ifengHotVideo_IP");
	}

	public static int getIfengHotVideo_port() {
		return getConfigLoader().getInt("ifengHotVideo_port");
	}

	public static int getIfengHotVideo_db() {
		return getConfigLoader().getInt("ifengHotVideo_db");
	}




	//报国故事
	public static String getServeCountryHotNews_IP() { return getConfigLoader().getString("ServeCountryHotNews_IP"); }

	public static int getServeCountryHotNews_port() {
		return getConfigLoader().getInt("ServeCountryHotNews_port");
	}

	public static int getServeCountryHotNews_db() {
		return getConfigLoader().getInt("ServeCountryHotNews_db");
	}

	//网信静海
	public static String getWxSilenceSeaHotNews_IP() { return getConfigLoader().getString("WxSilenceSeaHotNews_IP"); }

	public static int getWxSilenceSeaHotNews_port() {
		return getConfigLoader().getInt("WxSilenceSeaHotNews_port");
	}

	public static int getWxSilenceSeaHotNews_db() {
		return getConfigLoader().getInt("WxSilenceSeaHotNews_db");
	}


	//f凤凰卫视
	public static String getIfengVideoNews_IP() { return getConfigLoader().getString("IfengVideoNews_IP"); }

	public static int getIfengVideoNews_port() { return getConfigLoader().getInt("IfengVideoNews_port");	}

	public static int getIfengVideoNews_db() {
		return getConfigLoader().getInt("IfengVideoNews_db");
	}

	//正能量热榜
	public static String getPositiveEnergyHotNews_IP() {
		return getConfigLoader().getString("PositiveEnergyHotNews_IP");
	}

	public static int getPositiveEnergyHotNews_port() {
		return getConfigLoader().getInt("PositiveEnergyHotNews_port");
	}

	public static int getPositiveEnergyHotNews_db() {
		return getConfigLoader().getInt("PositiveEnergyHotNews_db");
	}

	//精品池曝光排序榜
	public static String getJPPoolEVConvert_IP() { return getConfigLoader().getString("JPPoolEVConvert_IP"); }

	public static int getJPPoolEVConvert_port() {
		return getConfigLoader().getInt("JPPoolEVConvert_port");
	}

	public static int getJPPoolEVConvert_db() {
		return getConfigLoader().getInt("JPPoolEVConvert_db");
	}


	//运营长效榜s
	public static String getLongOperationNews_IP() {return getConfigLoader().getString("LongOperationNews_IP");}
	public static int getLongOperationNews_port() {
		return getConfigLoader().getInt("LongOperationNews_port");
	}
	public static int getLongOperationNews_db() {
		return getConfigLoader().getInt("LongOperationNews_db");
	}


	//新闻黑名单
	public static String getBlackList_IP() {
		return getConfigLoader().getString("BlackList_IP");
	}
	public static int getBlackList_port() {
		return getConfigLoader().getInt("BlackList_port");
	}
	public static int getBlackList_db() {
		return getConfigLoader().getInt("BlackList_db");
	}


   //用户黑名单
   public static String getBlackUserList_IP() {
	   return getConfigLoader().getString("BlackUserList_IP");
   }
	public static int getBlackUserList_port() {
		return getConfigLoader().getInt("BlackUserList_Port");
	}
	public static int getBlackUserList_db() {
		return getConfigLoader().getInt("BlackUserList_DB");
	}




	/**
	 * 获取编辑固定位置的强插逻辑数据
	 * @return
	 */
	public static String getEditorRegularPositionDataIP() {return getConfigLoader().getStringListToString("regularPosition_IP");}
	public static int getEditorRegularPositionDataPort() {
		return getConfigLoader().getInt("regularPosition_Port");
	}
	public static int getEditorRegularPositionDataDB() {
		return getConfigLoader().getInt("regularPosition_DB");
	}
	public static String getEditorRegularPositionDatakey() {
		return getConfigLoader().getString("regularPosition_key");
	}
	public static String getEditorRegularPositionDatakeyNew() {
		return getConfigLoader().getString("regularPosition_key_new");
	}

	//------------------------------

	//------------------------------
	//三俗过滤
	public static String getSansuDocFilter_IP() {
		return getConfigLoader().getString("SansuDocFilter_IP");
	}

	public static int getSansuDocFilter_port() {
		return getConfigLoader().getInt("SansuDocFilter_port");
	}

	public static int getSansuDocFilter_db() {
		return getConfigLoader().getInt("SansuDocFilter_db");
	}




	//------------------------------

	public static void main(String[] args) {

	}
	

}
