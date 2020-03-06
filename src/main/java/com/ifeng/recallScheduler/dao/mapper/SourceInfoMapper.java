package com.ifeng.recallScheduler.dao.mapper;

import com.ifeng.recallScheduler.item.SourceInfoItem;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Created by lilg1 on 2018/3/21.
 */
public interface SourceInfoMapper {

    //获取机构媒体名称
    @Select("select manuscriptName from evalLevel_used where mediaType = 2")
    List<String> selectOrganizationSource();

    //获取媒体评级信息
    @Select("select manuscriptName,comEvalLevel from evalLevel_used")
    List<SourceInfoItem> selectSourceInfo();

}
