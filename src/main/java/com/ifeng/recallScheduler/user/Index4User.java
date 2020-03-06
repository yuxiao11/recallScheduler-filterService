package com.ifeng.recallScheduler.user;

import com.google.gson.annotations.Expose;
import com.ifeng.recallScheduler.utils.DateUtils;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * 
 * <PRE>
 * 作用 : 用户推荐文章索引信息记录实体 
 *   
 * 使用 : 离线部分
 *   
 * 示例 :
 *   
 * 注意 :
 *   
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0          2017年7月4日        zhangyang6          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
@Getter
@Setter
public class Index4User implements Cloneable,Serializable {

	private static final long serialVersionUID = 3987923320776614410L;

	static final Logger log = LoggerFactory.getLogger(Index4User.class);

	//------------------公共字段-----------------------------------------
	/**
	 * docid
	 */
	@Expose
	public String i; //docid
	/**
	 * ctr score
	 */
	@Expose(serialize = false,deserialize = true)
	public String c; //ctr score
	/**
	 * protobuf的压缩信息 (召回透传详细信息字段)
	 */
	@Expose
	public String p; //protobuf的压缩信息 --- 召回透传详细信息字段

	/**
	 * 标识增量缓存新旧
	 */
	@Expose(serialize = false,deserialize = true)
	public String ci;
	//------------------以下是mix召回使用的字段，涉及面较多-----------------------------------------
	/**
	 * strategy推荐策略，分流标记使用  (召回透传详细信息字段)
	 */
	@Expose(serialize = false,deserialize = true)
	private String s;
	/**
	 * reason 召回通道  (召回透传详细信息字段)
	 */
	@Expose
	private String r;

    /**
     * 多通道召回信息列表
     */
	@Expose(serialize = false,deserialize = true)
	private List<String> ch;

    /**
     * 多召回标签信息列表
     */
	@Expose(serialize = false,deserialize = true)
	private List<String> tags;

	/**
	 * recallTag多值召回标签  (召回透传详细信息字段)
	 */
	@Expose(serialize = false,deserialize = true)
	private String rT;

	/**
	 * debug信息  只有debug用户添加(召回透传详细信息字段)
	 */
	@Expose(serialize = false,deserialize = true)
	private String d;

	/**
	 * hotBoost 热度值(召回透传详细信息字段)
	 */
	@Expose(serialize = false,deserialize = true)
	private Double h;

	//试探标记
	@Expose(serialize = false,deserialize = true)
	private String ex;

	//------------------以下是头条引擎自己使用的字段-----------------------------------------
	/**
	 * q值 video排序使用(头条引擎自己使用的字段)
	 */
	@Deprecated
	@Expose(serialize = false,deserialize = true)
	private String q;

	/**
	 * ctr old 原有的CTR值(头条引擎自己使用的字段)
	 */
	@Deprecated
	@Expose(serialize = false,deserialize = true)
	private String co;

    /**
     * 召回recallid
     */
	@Expose(serialize = false,deserialize = true)
	private String recallid;

	/**
	 * 目前为冷启动试探ucb 专用 透传给头条自己使用
	 */
	@Expose(serialize = false,deserialize = true)
	private Double u;

	/**
	 * hotid 为热点事件专用
	 */
	@Expose(serialize = false,deserialize = true)
	private String hd;


	/**
	 * 文章类型
	 */
	@Expose(serialize = false,deserialize = true)
	private String type;

	/**
	 * 文章题目 热点专题会用到
	 */
	@Expose(serialize = false,deserialize = true)
	private String title;

	/**
	 *  热点文章insertTime
	 */
	@Expose(serialize = false,deserialize = true)
	private String ht;

	@Expose(serialize = false,deserialize = true)
	private String vt; //viewType 冷启动使用

	@Expose(serialize = false,deserialize = true)
	private String cp; //是否自动播放 冷启动使用

	public Index4User(){

	}




	@Override
	public Index4User clone() {
		Index4User index4User = null;
		try {
			index4User = (Index4User) super.clone();
		} catch (CloneNotSupportedException e) {
			log.error("CloneNotSupportedException", e);
		}
		return index4User;
	}

	/**
	 * 比较器，按照HotBoost排序
	 */
	public static final Comparator<Index4User> hotBoostComparator = new Comparator<Index4User>() {
		@Override
		public int compare(Index4User index1, Index4User index2) {

			// 按照hotBoost降序排序
			final double rank1 = Double.valueOf(index1.getH());
			final double rank2 = Double.valueOf(index2.getH());
			return rank1 == rank2 ? 0 : ((rank1 < rank2) ? 1 : -1);

		}
	};

	public static final Comparator<Index4User> ctrComparator = new Comparator<Index4User>() {
		@Override
		public int compare(Index4User index1, Index4User index2) {

			// 按照ctr降序排序
			final double rank1 = Double.valueOf(index1.getC());
			final double rank2 = Double.valueOf(index2.getC());
			return rank1 == rank2 ? 0 : ((rank1 < rank2) ? 1 : -1);

		}
	};

	public static final Comparator<Index4User> ucbComparator = new Comparator<Index4User>() {
		@Override
		public int compare(Index4User index1, Index4User index2) {
			// 按照ucb降序排序
			double one=index1.getU()==null?0:index1.getU();
			double two=index2.getU()==null?0:index1.getU();
			final double rank1 = Double.valueOf(one);
			final double rank2 = Double.valueOf(two);
			return rank1 == rank2 ? 0 : ((rank1 < rank2) ? 1 : -1);

		}
	};


    public static final Comparator<Index4User> updateTimeComparator = new Comparator<Index4User>() {
        @Override
        public int compare(Index4User index1, Index4User index2) {
            try{
                // 按照时间降序排序
                Date date1=DateUtils.strToDate(index1.getHt());
                Date date2=DateUtils.strToDate(index2.getHt());

                return date1.getTime()==date2.getTime()?0:((date1.getTime() < date2.getTime()) ? 1 : -1);
            }catch (Exception e){
                e.printStackTrace();
            }
            return 0;
        }
    };

	public static final Comparator<Index4User> index4userComparator = new Comparator<Index4User>() {
        @Override
        public int compare(Index4User o1, Index4User o2) {
            return 0;
        }
    };
}
