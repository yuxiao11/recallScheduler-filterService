package com.ifeng.recallScheduler.dao;

import com.ifeng.recallScheduler.dao.mapper.NegCommentDocMapper;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Set;

/**
 * Created by lilg1 on 2018/3/21.
 */
@Repository
public class NegCommentDocDao {

    @Resource(name = "negCommentDocMapper")
    private NegCommentDocMapper negCommentDocMapper;

    public Set<String> getNegCommentDocsLevel5Set() {
            return negCommentDocMapper.selectLevel5NegCommentDocs();
    }


    public Set<String> getNegCommentDocsLevel1To4Set() {
        return negCommentDocMapper.selectLevel1To4NegCommentDocs();
    }

}
