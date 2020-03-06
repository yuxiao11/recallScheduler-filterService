package com.ifeng.recallScheduler.dao.mapper;

import com.ifeng.recallScheduler.item.SourceInfoItem;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Created by lilg1 on 2018/3/29.
 */
public interface VideoSourceInfoMapper {

    //获取视频媒体评级信息
    @Select("select manuscriptName,comEvalLevel from video_evalLevel_used ")
    List<SourceInfoItem> selectVideoSourceInfo();

}
