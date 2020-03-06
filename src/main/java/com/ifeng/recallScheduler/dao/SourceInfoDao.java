package com.ifeng.recallScheduler.dao;


import com.ifeng.recallScheduler.dao.mapper.SourceInfoMapper;
import com.ifeng.recallScheduler.item.SourceInfoItem;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by lilg1 on 2018/3/21.
 */
@Repository
public class SourceInfoDao {

    @Resource(name = "sourceInfoMapper")
    private SourceInfoMapper mediaInfoMapper;

    public List<String> getOrganizationMedia() {
        return mediaInfoMapper.selectOrganizationSource();
    }

    public List<SourceInfoItem> getSourceInfo(){
        return mediaInfoMapper.selectSourceInfo();
    }

}
