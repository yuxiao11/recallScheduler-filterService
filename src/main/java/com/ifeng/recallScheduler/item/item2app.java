package com.ifeng.recallScheduler.item;

import lombok.Data;

/**
 * <PRE>
 * 作用 : 
 *   solr中读取的item2app字段反序列化后的对象模型
 *   
 *   {
    "docId": "1296712",
    "title": "北京环保局释红色预警：为何没提前24小时发布？",
    "date": "2015-12-08 11:17:20",
    "hotLevel": "D",
    "docChannel": "时政",
    "why": "",
    "score": 0,
    "hotBoost": 0.5,
    "docType": "docpic",
    "readableFeatures": "c=时政|!|c=大陆时事|!|cn=关注民生|!|e=北京雾霾红色预警|!|e=北京未来限行更严|!|et=环保局|!|",
    "others": "|!|url=http://news.sohu.com/20151208/n430213992.shtml|!|illegal"
	}
	
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0          2014-9-27        jiangmm          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
@Data
public class item2app {
	
	private String docId;
	
	private String title;
	
	private String date;
	
	private String hotLevel;
	
	private String docChannel;
	
	private String docType;
	
	private String why;

	private String url;
	
	private String score;
	
	private String hotBoost;

	private String others;

	private String readableFeatures;

	private String source;

	private String simId;

	private String specialParam;

	private Long updatetime;//文章重编辑更新时间


	/**
	 * 分类扩展信息，例如小视频
	 */
	private String partCategory;

	/**
	 * 过滤使用的扩展信息，例如世界杯内容
	 */
	private String partCategoryExt;


	private boolean hasThumnail;

	public String getDocID() {
		return get(docId);
	}

	public String getTitle() {
		return get(title);
	}

	public String getDate() {
		return get(date);
	}


	public String getHotLevel() {
		return get(hotLevel);
	}

	public String getDocChannel() {
		if(docChannel==null||docChannel.indexOf("null")>=0){
			docChannel="other";
		}
		return docChannel;
	}

	public void setDocChannel(String docChannel) {
		this.docChannel = docChannel;
	}

	public String getDocType() {
		return get(docType);
	}

	public String getWhy() {
		return get(why);
	}

	public void setWhy(String why) {
		this.why = why;
	}

	public String getScore() {
		return get(score);
	}

	public void setScore(String score) {
		this.score = score;
	}

	public String getHotBoost() {
		return get(hotBoost);
	}

	public String getOthers() {
		return get(others);
	}
	

	public String getReadableFeatures() {
		return readableFeatures;
	}

	/**
	 * str不为null返回原值，否则返回""
	 * @param str
	 * @return
	 */
	private String get(String str){
		return (str==null?"":str);
	}

	public String getDocId() {
		return docId;
	}

	public void setDocId(String docId) {
		this.docId = docId;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public void setHotLevel(String hotLevel) {
		this.hotLevel = hotLevel;
	}

	public void setDocType(String docType) {
		this.docType = docType;
	}

	public void setHotBoost(String hotBoost) {
		this.hotBoost = hotBoost;
	}

	public void setOthers(String others) {
		this.others = others;
	}

	public void setReadableFeatures(String readableFeatures) {
		this.readableFeatures = readableFeatures;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}


	public String getSimId() {
		return simId;
	}

	public void setSimId(String simId) {
		this.simId = simId;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getSpecialParam() {
		return specialParam;
	}

	public void setSpecialParam(String specialParam) {
		this.specialParam = specialParam;
	}


}
