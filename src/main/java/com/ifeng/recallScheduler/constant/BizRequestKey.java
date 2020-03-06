package com.ifeng.recallScheduler.constant;


/**新闻客户端内视频频道请求参数，推荐业务需要的key
 *
 *
 */
public enum BizRequestKey {


	/**
	 * 推荐频道标记
	 */
	RecomChannel("recomChannel"),

	/**动作  */
	OPERATION("operation"),    //头条是operation

	/**省份  */
	PROVINCE("province"),
	/**省份  */
	CITY("city"),
	/*下拉次数*/
	PULLNUM("pullNum"),
	/**网络制式  */
	NW("nw"),
	/**客户端 */
	PROID("proid"),
	/**渠道号 */
	PUBLISHID("publishid"),
	/**操作系统 */
	OS("os"),
	/**版本号 */
	GV("gv"),
	/**视频频道内最近一次的4个点击   lastDoc */
	LASTDOC("lastDoc"),
	/**请求的业务来源   videoapp[视频app], videochannel[新闻app内视频频道],sy[新闻app内头条频道]*/
	FROM("from"),
	/**首页频道内部单独调用时需要的条数  num [0---5]条 */
	SIZE("size"),
	/**
	 * 用户id
	 */
	UID("uid"),

	/**
	 * 热点事件名称
	 */
	EventName("eventName"),

	/**
	 * 详情页，页码，和列表页单独区分
	 */
	pageNum("pageNum"),

	/**
	 * 返回结果的格式
	 */
	resultFormat("resultFormat"),

	/**
	 * 话题事件名称
	 */
	ThemeName("themeName"),

	/**
	 * 话题Id
	 */
	ThemeId("themeId"),

    /**
     * 编辑固定位内容
     */
    EditorList("editorList");
	;

	private String value;

	private BizRequestKey(String value) {
		this.value = value;
	}


	public String getValue() {
		return value;
	}
}
