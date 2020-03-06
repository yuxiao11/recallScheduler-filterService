package com.ifeng.recallScheduler.dao.mapper;

import org.apache.ibatis.annotations.Select;

import java.util.Set;

/**
 * Created by lilg1 on 2018/3/21.
 */
public interface SansuDocMapper {

    //获取三俗文章  且没有被 解除黑名单 的帖子
    @Select("SELECT simid from GarbageNewsAssess WHERE isRecover=0")
    Set<String> selectSansuDocs();


    //获取全量三俗文章  (包含解除黑名单 的帖子）  应对wxb检查时使用
    @Select("SELECT simid from GarbageNewsAssess ")
    Set<String> selectSansuDocs_all();


}
