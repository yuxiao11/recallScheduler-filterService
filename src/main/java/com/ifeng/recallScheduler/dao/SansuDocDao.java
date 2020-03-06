package com.ifeng.recallScheduler.dao;


import com.ifeng.recallScheduler.apolloConf.ApolloConstant;
import com.ifeng.recallScheduler.apolloConf.SpecialApplicationConfig;
import com.ifeng.recallScheduler.dao.mapper.SansuDocMapper;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Set;

/**
 * Created by lilg1 on 2018/3/21.
 */
@Repository
public class SansuDocDao {

    @Resource(name = "sansuDocMapper")
    private SansuDocMapper sansuDocMapper;

    public Set<String> getSansuDocSet() {
        if (ApolloConstant.Switch_on.equals(SpecialApplicationConfig.getProperty(ApolloConstant.WxbContentSecuritySwitch))) {
            return sansuDocMapper.selectSansuDocs_all();
        } else {
            return sansuDocMapper.selectSansuDocs();
        }

    }

}
