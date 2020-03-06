package com.ifeng.recallScheduler.dao.mapper;

import org.apache.ibatis.annotations.Select;

import java.util.Set;

/**
 * Created by lilg1 on 2018/3/21.
 */
public interface NegCommentDocMapper {

    //获取负评文章  等级为 5 的帖子
    @Select("SELECT simid FROM neg_comment_info WHERE expireTime > now() AND negLevel = 5")
    Set<String> selectLevel5NegCommentDocs();


    //获取负评文章  等级为 1-4 的文章  应对 老板 使用
    @Select("SELECT simid FROM neg_comment_info WHERE expireTime > now() AND negLevel BETWEEN 1 AND 4")
    Set<String> selectLevel1To4NegCommentDocs();


}
