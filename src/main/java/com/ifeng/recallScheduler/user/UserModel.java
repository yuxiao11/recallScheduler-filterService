package com.ifeng.recallScheduler.user;


import com.beust.jcommander.internal.Lists;
import com.google.gson.reflect.TypeToken;
import com.ifeng.recallScheduler.utils.JsonUtil;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * 用户画像的对象
 */
@Setter
@Getter
public class UserModel {

    //实时画像更新时间戳，根据时间戳判定是否刷新
    private long realTimestamp = -1;

    //离线画像更新时间戳
    private long offlineTimestamp = -1;

    private static final Logger logger = LoggerFactory.getLogger(UserModel.class);
    private String userId;

    private String t1;
    private String t2;
    private List<RecordInfo> t1RecordList;
    private List<RecordInfo> t2RecordList;
    /**
     * docpic subcate
     */
    private  String dsc;
    private List<RecordInfo> dscRecordList;
    /**
     * docpic cate
     */
    private  String dc;
    private List<RecordInfo> dcRecordList;

    private  String vsc;
    private List<RecordInfo> vscRecordList;

    private  String vc;
    private List<RecordInfo> vcRecordList;

    private  String rvsc;
    private List<RecordInfo> rvscRecordList;

    private  String rdsc;
    private List<RecordInfo> rdscRecordList;

    private  String rdc;
    private List<RecordInfo> rdcRecordList;

    private  String rvc;
    private List<RecordInfo> rvcRecordList;

    private String loc;

    private String ua_v;

    private String recent_t1;

    private String first_in;

    private String docpic_cotag;

    private String video_cotag;

    private int dayCount = -1;


    /**
     * 文章特征（近两天）
     */
    private String last_t1;
    /***/
    private String last_t2;
    /***/
    private String last_t3;

    private String last_dis_t1;
    private String last_dis_t2;
    private String last_dis_t3;


    private String last_t2_sourceSims;
    private String last_ub_sourceSims;
    private String last_ucombineTag;
    private String last_dis_ucombineTag;

    private String lng;
    private String lat;
    private String subLocality;
    private String city;
    private String province;
    private String Country;


    private String generalloc;

    //用户手机型号
    private String umos;

    /**
     * 手机型号，品牌
     * mix_2,xiaomi
     */
    private String umt;

    private String general_interestVert;

    private String group_ub;

    private String ub;

    //画像熔断时间 是否超时 专为引擎内部使用
    private boolean isTimeOut=false;

    //画像字段 秒级生成，可以判断用户是否未生成画像
    private String lastUtime;

    private String userGroup;

    private String fullness;

    private String daily_pullnum;

    //用户多样性控制等级
    private  String cateLevel;
    private List<CateInfo> cateLevelList;

    private List<String> ubList;

    private String uver;
    private String userSchannel;

    private String last_lda_topic;

    private String channelCtr;
    private ChannelCtr channelCtrM;

    private String u_retention;
    public UserModel() {

    }


    public UserModel(String userId) {
        this.userId = userId;
    }


    public List<RecordInfo> getRvscRecordList() {
        if (this.rvscRecordList == null) {
            this.rvscRecordList = convertStr2NewT(this.rvsc);
        }
        return rvscRecordList;
    }

    public List<RecordInfo> getRdscRecordList() {
        if (this.rdscRecordList == null) {
            this.rdscRecordList = convertStr2NewT(this.rdsc);
        }
        return rdscRecordList;
    }

    public List<RecordInfo> getRdcRecordList() {
        if (this.rdcRecordList == null) {
            this.rdcRecordList = convertStr2NewT(this.rdc);
        }
        return rdcRecordList;
    }

    public List<RecordInfo> getRvcRecordList() {
        if (this.rvcRecordList == null) {
            this.rvcRecordList = convertStr2NewT(this.rvc);
        }
        return rvcRecordList;
    }

    public ChannelCtr getChannelCtrM() {
        if (this.channelCtrM == null) {
            this.channelCtrM = convertStr2Model(this.channelCtr);
        }
        return channelCtrM;
    }

    public List<CateInfo> getCateLevelList() {
        if (this.cateLevelList == null) {
            this.cateLevelList = convertStr2CateInfo(this.cateLevel);
        }
        return cateLevelList;
    }

    public void setCateLevelList(List<CateInfo> cateLevelList) {
        this.cateLevelList = cateLevelList;
    }


    private ChannelCtr convertStr2Model(String t) {
        if (StringUtils.isBlank(t)) return null;
        ChannelCtr channelCtr=new ChannelCtr();
        try {
            channelCtr=JsonUtil.json2ObjectWithoutException(this.channelCtr,ChannelCtr.class);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("convertStr2Model failed!",e);
        }
        return channelCtr == null? new ChannelCtr():channelCtr;
    }
    /**
     * 用于新版画像 以及 隐性标签字段的兼容
     * 武器装备_21_31_0.798#lt_84_15_22_0.685
     */
    private List<RecordInfo> convertStr2T(String t) {
        if (StringUtils.isBlank(t)) return null;
        List<RecordInfo> recordList = Lists.newArrayList();
        try {
            int ind = t.indexOf("$");
            if (ind >= 0) t = t.substring(0, ind);
            String[] itemArr = t.split("#");
            for (int i = 0; i < itemArr.length; i++) {
                String item = itemArr[i];
                //解析每个特征对象
                String[] recordObj = item.split("_");
                if (recordObj.length < 3)
                    continue;

                String recordName;
                int frequecy;
                double weight;
                if (item.contains("lt")) {
                    if (recordObj.length == 4) { //旧画像
                        recordName = recordObj[0] + "_" + recordObj[1];
                        frequecy = Integer.parseInt(recordObj[2]);
                        weight = Double.parseDouble(recordObj[3]);
                        RecordInfo recordInfo = new RecordInfo(recordName, frequecy, weight);
                        recordList.add(recordInfo);
                    } else if (recordObj.length == 5) {
                        recordName = recordObj[0] + "_" + recordObj[1];
                        frequecy = Integer.parseInt(recordObj[2]);
                        weight = Double.parseDouble(recordObj[4]);
                        RecordInfo recordInfo = new RecordInfo(recordName, frequecy, weight);
                        recordList.add(recordInfo);
                    } else {
                        continue;
                    }
                } else {
                    if (recordObj.length == 3) {//旧画像
                        recordName = recordObj[0];
                        frequecy = Integer.parseInt(recordObj[1]);
                        weight = Double.parseDouble(recordObj[2]);
                        RecordInfo recordInfo = new RecordInfo(recordName, frequecy, weight);
                        recordList.add(recordInfo);
                    } else if (recordObj.length == 4) {
                        recordName = recordObj[0];
                        frequecy = Integer.parseInt(recordObj[1]);
                        weight = Double.parseDouble(recordObj[3]);
                        RecordInfo recordInfo = new RecordInfo(recordName, frequecy, weight);
                        recordList.add(recordInfo);
                    } else {
                        continue;
                    }
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return recordList;
    }

    /**
     * docpic_subcate 转 recordInfo
     * @param t
     * @return
     */
    private List<RecordInfo> convertStr2NewT(String t) {
        if (StringUtils.isBlank(t)){
            return null;
        }
        List<RecordInfo> recordList = null;
        try {

            recordList= JsonUtil.json2Object(t, new TypeToken<List<RecordInfo>>() {
            }.getType());
        } catch (NumberFormatException e) {
            e.printStackTrace();
            logger.error("convertStr2NewT failed!",e);
        }
        return recordList == null? Collections.emptyList():recordList;
    }

    private List<CateInfo> convertStr2CateInfo(String t) {
        if (StringUtils.isBlank(t)){
            return null;
        }
        List<CateInfo> recordList = null;
        try {

            recordList= JsonUtil.json2Object(t, new TypeToken<List<CateInfo>>() {
            }.getType());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("convertStr2CateInfo failed!",e);
        }
        return recordList == null? Collections.emptyList():recordList;
    }

    public String getUserId() {
        return userId;
    }

    public List<RecordInfo> getT1RecordList() {
        if (this.t1RecordList == null) {
            this.t1RecordList = convertStr2T(this.t1);
        }
        return t1RecordList;
    }

    public void setT1RecordList(List<RecordInfo> t1) {
        this.t1RecordList = t1;
    }

    public List<RecordInfo> getT2RecordList() {
        if (this.t2RecordList == null) {
            this.t2RecordList = convertStr2T(this.t2);
        }
        return t2RecordList;
    }
    public List<RecordInfo> getDscRecordList() {
        if (this.dscRecordList == null) {
            this.dscRecordList = convertStr2NewT(this.dsc);
        }
        return dscRecordList;
    }

    public List<RecordInfo> getVcRecordList() {
        if (this.vcRecordList == null) {
            this.vcRecordList = convertStr2NewT(this.vc);
        }
        return vcRecordList;
    }

    public List<RecordInfo> getVscRecordList() {
        if (this.vscRecordList == null) {
            this.vscRecordList = convertStr2NewT(this.vsc);
        }
        return vscRecordList;
    }
    public List<RecordInfo> getDcRecordList() {
        if (this.dcRecordList == null) {
            this.dcRecordList = convertStr2NewT(this.dc);
        }
        return dcRecordList;
    }



    public String getLoc() {
        return loc;
    }

    public void setLoc(String loc) {
        this.loc = loc;
    }


    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRecent_t1() {
        return recent_t1;
    }

    public static void main(String[] args) {
        String dsc = "[{\"n\":\"天文宇宙\",\"c\":16,\"e\":121,\"s\":0.55492},{\"n\":\"区块链\",\"c\":18,\"e\":174,\"s\":0.55344},{\"n\":\"心灵鸡汤\",\"c\":10,\"e\":214,\"s\":0.54482},{\"n\":\"文学\",\"c\":10,\"e\":200,\"s\":0.5418},{\"n\":\"互联网\",\"c\":53,\"e\":1090,\"s\":0.5415},{\"n\":\"财经人物\",\"c\":11,\"e\":208,\"s\":0.54069},{\"n\":\"手机\",\"c\":10,\"e\":330,\"s\":0.53976},{\"n\":\"理财\",\"c\":8,\"e\":153,\"s\":0.53794},{\"n\":\"银行\",\"c\":7,\"e\":136,\"s\":0.53612},{\"n\":\"民生\",\"c\":6,\"e\":61,\"s\":0.5345},{\"n\":\"电影\",\"c\":7,\"e\":134,\"s\":0.53274},{\"n\":\"宇宙大观\",\"c\":7,\"e\":130,\"s\":0.53234},{\"n\":\"职业规划\",\"c\":7,\"e\":141,\"s\":0.53138},{\"n\":\"社会治安\",\"c\":6,\"e\":93,\"s\":0.5311},{\"n\":\"潮流\",\"c\":6,\"e\":110,\"s\":0.52899},{\"n\":\"奇闻轶事\",\"c\":24,\"e\":588,\"s\":0.52446},{\"n\":\"股市\",\"c\":5,\"e\":18,\"s\":0.52373},{\"n\":\"新房楼盘\",\"c\":14,\"e\":179,\"s\":0.52257},{\"n\":\"经济\",\"c\":5,\"e\":74,\"s\":0.52132},{\"n\":\"地方党政民生\",\"c\":5,\"e\":68,\"s\":0.5188},{\"n\":\"大陆时政\",\"c\":17,\"e\":403,\"s\":0.5188},{\"n\":\"亲子教育\",\"c\":5,\"e\":68,\"s\":0.51854},{\"n\":\"期货\",\"c\":5,\"e\":113,\"s\":0.5183},{\"n\":\"养生\",\"c\":5,\"e\":225,\"s\":0.51713},{\"n\":\"境外游\",\"c\":5,\"e\":155,\"s\":0.51483},{\"n\":\"互联网金融\",\"c\":4,\"e\":44,\"s\":0.51318},{\"n\":\"基金\",\"c\":4,\"e\":35,\"s\":0.51269},{\"n\":\"思想\",\"c\":4,\"e\":66,\"s\":0.51108},{\"n\":\"职业培训\",\"c\":4,\"e\":65,\"s\":0.51012},{\"n\":\"篮球\",\"c\":3,\"e\":18,\"s\":0.50999},{\"n\":\"人工智能\",\"c\":4,\"e\":55,\"s\":0.5092},{\"n\":\"国学\",\"c\":4,\"e\":201,\"s\":0.5081},{\"n\":\"科技前沿\",\"c\":4,\"e\":62,\"s\":0.50565},{\"n\":\"中国古代史\",\"c\":4,\"e\":114,\"s\":0.50437},{\"n\":\"家居\",\"c\":3,\"e\":79,\"s\":0.50368},{\"n\":\"新房\",\"c\":4,\"e\":116,\"s\":0.50367},{\"n\":\"健康其他\",\"c\":2,\"e\":25,\"s\":0.50354},{\"n\":\"电视剧\",\"c\":4,\"e\":103,\"s\":0.50237},{\"n\":\"宏观经济\",\"c\":3,\"e\":116,\"s\":0.50203},{\"n\":\"大陆明星\",\"c\":9,\"e\":284,\"s\":0.50186},{\"n\":\"财经其他\",\"c\":3,\"e\":111,\"s\":0.50182},{\"n\":\"美容美发\",\"c\":2,\"e\":28,\"s\":0.50181},{\"n\":\"移动互联网\",\"c\":5,\"e\":114,\"s\":0.5017},{\"n\":\"体育其他\",\"c\":1,\"e\":14,\"s\":0.50144},{\"n\":\"两性孕育\",\"c\":1,\"e\":5,\"s\":0.50143},{\"n\":\" 大陆明星\",\"c\":1,\"e\":2,\"s\":0.50143},{\"n\":\"教育培训\",\"c\":1,\"e\":1,\"s\":0.50143},{\"n\":\"数码\",\"c\":2,\"e\":52,\"s\":0.5013},{\"n\":\"装备技术\",\"c\":1,\"e\":30,\"s\":0.49021},{\"n\":\"求职\",\"c\":1,\"e\":16,\"s\":0.48897},{\"n\":\"武器装备\",\"c\":1,\"e\":17,\"s\":0.48608},{\"n\":\"网游\",\"c\":1,\"e\":18,\"s\":0.48602},{\"n\":\"改装汽车\",\"c\":1,\"e\":27,\"s\":0.48258},{\"n\":\"生活妙招\",\"c\":1,\"e\":45,\"s\":0.48041},{\"n\":\"明星\",\"c\":18,\"e\":460,\"s\":0.47803},{\"n\":\"新车上市\",\"c\":1,\"e\":29,\"s\":0.47658},{\"n\":\"留学\",\"c\":1,\"e\":16,\"s\":0.47605},{\"n\":\"综合体育\",\"c\":1,\"e\":26,\"s\":0.47579},{\"n\":\"房产其他\",\"c\":1,\"e\":21,\"s\":0.47478},{\"n\":\"IT\",\"c\":1,\"e\":41,\"s\":0.47064},{\"n\":\"穿衣搭配\",\"c\":1,\"e\":26,\"s\":0.46893},{\"n\":\"明星人物\",\"c\":1,\"e\":69,\"s\":0.46847},{\"n\":\"股票\",\"c\":3,\"e\":45,\"s\":0.46824},{\"n\":\"中国军情\",\"c\":1,\"e\":52,\"s\":0.46724},{\"n\":\"风水\",\"c\":1,\"e\":22,\"s\":0.46609},{\"n\":\"A股\",\"c\":1,\"e\":40,\"s\":0.46578},{\"n\":\"国际足球\",\"c\":1,\"e\":33,\"s\":0.46154},{\"n\":\"顶级豪车\",\"c\":1,\"e\":52,\"s\":0.45893},{\"n\":\"国内财经\",\"c\":1,\"e\":11,\"s\":0.45889},{\"n\":\"备孕怀孕\",\"c\":1,\"e\":37,\"s\":0.45776},{\"n\":\"电玩\",\"c\":1,\"e\":16,\"s\":0.45597},{\"n\":\"动物世界\",\"c\":1,\"e\":28,\"s\":0.45249},{\"n\":\"台湾\",\"c\":1,\"e\":57,\"s\":0.44974},{\"n\":\"中国足球\",\"c\":1,\"e\":78,\"s\":0.44293},{\"n\":\"穿搭\",\"c\":1,\"e\":85,\"s\":0.4399},{\"n\":\"中国现代史\",\"c\":1,\"e\":30,\"s\":0.4366},{\"n\":\"社会其他\",\"c\":8,\"e\":295,\"s\":0.43301},{\"n\":\"内地明星\",\"c\":1,\"e\":29,\"s\":0.42978},{\"n\":\"排球\",\"c\":1,\"e\":57,\"s\":0.40296},{\"n\":\"军事其他\",\"c\":0,\"e\":32,\"s\":0.13939},{\"n\":\"国际时政\",\"c\":0,\"e\":65,\"s\":0.13117},{\"n\":\"美食其他\",\"c\":0,\"e\":63,\"s\":0.12352},{\"n\":\"装修家居\",\"c\":0,\"e\":48,\"s\":0.11389},{\"n\":\"用车养车\",\"c\":0,\"e\":33,\"s\":0.1123},{\"n\":\"CBA\",\"c\":0,\"e\":26,\"s\":0.10704},{\"n\":\"汽车行业\",\"c\":0,\"e\":51,\"s\":0.08653},{\"n\":\"NBA\",\"c\":0,\"e\":38,\"s\":0.08422},{\"n\":\"国际军情\",\"c\":0,\"e\":59,\"s\":0.08094},{\"n\":\"新车\",\"c\":0,\"e\":33,\"s\":0.0795},{\"n\":\"旅游资讯\",\"c\":0,\"e\":26,\"s\":0.07479},{\"n\":\"战史战例\",\"c\":0,\"e\":54,\"s\":0.07465},{\"n\":\"电竞\",\"c\":0,\"e\":32,\"s\":0.07212},{\"n\":\"历史其他\",\"c\":0,\"e\":40,\"s\":0.06851},{\"n\":\"电视娱乐\",\"c\":0,\"e\":44,\"s\":0.05825},{\"n\":\"境内游\",\"c\":0,\"e\":78,\"s\":0.0471},{\"n\":\"中国近代史\",\"c\":0,\"e\":40,\"s\":0.04241},{\"n\":\"天气\",\"c\":0,\"e\":36,\"s\":0.01908}]";
//        UserModel u = new UserModel();
//        u.convertStr2NewT(dsc);
        Object o = JsonUtil.json2Object(dsc, new TypeToken<List<RecordInfo>>() {
        }.getType());
        System.out.println(o);
    }

}