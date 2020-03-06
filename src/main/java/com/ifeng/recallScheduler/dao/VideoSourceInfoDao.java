package com.ifeng.recallScheduler.dao;

import com.ifeng.recallScheduler.dao.mapper.VideoSourceInfoMapper;
import com.ifeng.recallScheduler.item.SourceInfoItem;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by lilg1 on 2018/3/29.
 */
@Repository
public class VideoSourceInfoDao {

    @Resource(name="videoSourceInfoMapper")
    private VideoSourceInfoMapper videoSourceInfoMapper;

    public List<SourceInfoItem> getVideoSourceInfo(){
        return videoSourceInfoMapper.selectVideoSourceInfo();
    }

}
