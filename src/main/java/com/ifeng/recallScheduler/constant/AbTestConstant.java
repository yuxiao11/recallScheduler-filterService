package com.ifeng.recallScheduler.constant;

/**
 * 详细abtest的wiki地址：
 * http://10.90.11.12:8090/pages/viewpage.action?pageId=7176232
 * Created by jibin on 2017/11/14.
 */
public class AbTestConstant {


    //------------------ctr对比测试测试-------------------------------------
    /**
     * abTestMap key常量:ctr
     */
    public static final String Abtest_Group_ctrTest = "ctrTest";

    //Rerank切流量的标记，保证rerank视频和图文流量不交叉，只在切流量的时候使用
    //19-09-10，图文的流量已切全，视频流量未切全，和周康的服务并行
    public static final String Abtest_Rerank_Test_FlowMark = "ReRank_Test";

    public static final String Abtest_Group_CtrAB_Test = "CtrAB_Test_Group";

    //-------------------用户画像测试--------------------------------------
    /**
     * 用户画像测试
     */
    public static final String Abtest_Group_Video_Test = "videoRatingTest";
    public static final String Abtest_ExpFlag_video_ctrRerank_rate20 = "video_ReRank_rate20";
    public static final String Abtest_ExpFlag_video_Rating_rate20 = "video_Rating_rate20";
    public static final String Abtest_ExpFlag_video_base_rate20 = "video_base_rate20";
    public static final String Abtest_ExpFlag_video_base = "video_base";
    public static final String Abtest_Group_VideoRating = "VideoRating";


    public static final String Abtest_Group_Video_Test_Ctr = "videoRatingTest_Ctr";

    /**
     * 首屏去掉精品池测试
     */
    public static final String Abtest_Group_Default_Test = "defaultTest";




    //-------------------CacheUpdateCtr测试--------------------------------------

    public static final String Abtest_Group_CacheUpdateCtr_Test = "CacheUpdateCtr";


    //-------------------lite版本进行冷启动测试--------------------------------------
    /**
     * lite版本进行冷启动测试
     */
    public static final String Abtest_Group_Lite_BackUp_Test = "Lite_BackUp_Test";

    /**
     * lite版使用运营提供的长效冷启动数据做打底
     */
    public static final String Abtest_ExpFlag_Lite_BackUp_LongOperation = "Lite_BackUp_LongOperation";

    /**
     * 和晓伟约定的分流
     */
    public static final String Abtest_Group_DataType_Test = "dataType_";


    //探索版 冷启动用户 百分之五十 无焦点图
    public static final String Abtest_Group_ColdUserDiscoveryFocus = "ColdUserDiscoveryFocus";
    public static final String Abtest_ExpFlag_ColdUserDiscoveryFocus_test_rate50 = "test_ColdUserDiscoveryFocus_rate50";
    public static final String Abtest_ExpFlag_ColdUserDiscoveryFocus_base_rate50 = "base_ColdUserDiscoveryFocus_rate50";

    //专业版 冷启动用户 百分之五十 无焦点图
    public static final String Abtest_Group_ColdUserVipFocus = "ColdUserVipFocus";
    public static final String Abtest_ExpFlag_ColdUserVipFocus_test_rate50 = "test_ColdUserVipFocus_rate50";
    public static final String Abtest_ExpFlag_ColdUserVipFocus_base_rate50 = "base_ColdUserVipFocus_rate50";


    /**
     * 视频数量动态调整abtest
     */
    public static final String Abtest_Group_VideoNum_Test = "VideoNum_Test";

    public static final String Cold_Count_test = "coldCountNum";
    public static final String Cold_Count_Num = "coldCountNum_rate100";

    //ffm召回测试
    public static final String Abtest_recall_FFMTest = "recall_FFMTest";
    public static final String Abtest_FFM_test_rate10 = "FFM_test_rate10";
    public static final String Abtest_FFM_base_rate10 = "FFM_base_rate10";
    public static final String Abtest_FFM_video_rate10 = "FFM_video_rate10";


    public static final String Abtest_RegularDisplay_Test = "regularDisplayTest";
    public static final String Abtest_RegularDisplay_rate50_base = "TinyCity_display_base_rate50";
    public static final String Abtest_RegularUnDisplay_rate50_test = "TinyCity_unDisplay_test_rate50";


    //展现优质账号内容配置
    public static final String Abtest_HighQualitySource_Test = "highQualitySourceTest";
    public static final String Abtest_HighQualitySource_base_NoLimit_rate10 = "HighQuality_base_noLimit_rate10";
    public static final String Abtest_HighQualitySource_test_onlyHigh_rate10 = "HighQuality_test_onlyHigh_rate10";

    //精品池 试验 减量 TODO 鹤群试验
    public static final String Abtest_JpPoolReduce_Test = "JpPool_Reduce_Test_V3";


    //阶梯流量控制实验 测试流量影响
    public static final String CRCFlow_Test = "CRCFlow_Test";
    public static final String Abtest_CRCFlow_test1= "Abtest_CRCFlow_test1";
    public static final String Abtest_CRCFlow_test5= "Abtest_CRCFlow_test5";
    public static final String Abtest_CRCFlow_test10= "Abtest_CRCFlow_test10";
    public static final String Abtest_CRCFlow_test20= "Abtest_CRCFlow_test20";
    public static final String Abtest_CRCFlow_test40= "Abtest_CRCFlow_test40";
    public static final String Abtest_CRCFlow_test24= "Abtest_CRCFlow_test24";


    //对外冷启动试验
    public static final String Abtest_OutCold_Test = "OutCold_Test";
    public static final String OutCold_test_rate = "OutCold_test_rate";
    public static final String OutCold_base_rate = "OutCold_base_rate";

    //新增用户试验
    public static final String Abtest_newIncrease_Test = "NewIncrease_Test";
    public static final String NewIncrease_test_rate30 = "NewIncrease_test_rate30";
    public static final String NewIncrease_base_rate30 = "NewIncrease_base_rate30";
    public static final String NewIncrease_guide_rate30 = "NewIncrease_guide_rate30";

    //实时调用mix试验
    public static final String Abtest_RealTimeRecall_Test = "RealTimeRecall_Test";
    public static final String RealTimeRecall_test_rate = "RealTimeRecall_test_rate";
    public static final String RealTimeRecall_base_rate = "RealTimeRecall_base_rate";

    public static final String Abtest_RealTimeOut_Test = "RealTimeOut_Test";
    public static final String RealTimeRecall_timeOut_rate = "RealTimeRecall_timeOut_rate";
    public static final String RealTimeRecall_noTimeOut_rate = "RealTimeRecall_noTimeOut_rate";


    //mix算法实验组
    public static final String Abtest_CateFilter_Test = "CateFilterTest";
    public static final String CateFilter_base_rate20 = "CateFilter_base_rate20";
    public static final String CateFilter_test1_rate20 = "CateFilter_test1_rate20";
    public static final String CateFilter_test2_rate20 = "CateFilter_test2_rate20";
    public static final String Abtest_GraphCotag_Test = "GraphCotagTest";
    public static final String GraphCotag_test_rate50 = "GraphCotag_test_rate50";
    public static final String GraphCotag_base_rate50 = "GraphCotag_base_rate50";
    //于潇实验
    public static final String Abtest_FinalNegative_Test = "FinalNegativeTest";
    public static final String FinalNegative_test_rate20 = "FinalNegative_test_rate20";
    public static final String FinalNegative_base_rate20 = "FinalNegative_base_rate20";

    //于潇实验
    public static final String Abtest_Graph_Test = "Graph_Test";
    public static final String Graph_test_rate5 = "Graph_test_rate10";
    public static final String Graph_base_rate5 = "Graph_base_rate10";
    public static final String Graph_base2_rate5 = "Graph_base2_rate10";

    public static final String Abtest_UserInsertExp_Test = "CommonExp_Test";
    public static final String Abtest_CommonExp_test_rate10 = "CommonExp_test_rate10";
    public static final String Abtest_CommonExp_base_rate10 = "CommonExp_base_rate10";
    public static final String Abtest_CommonExp_testMix_rate10 = "CommonExp_testMix_rate10";



}
