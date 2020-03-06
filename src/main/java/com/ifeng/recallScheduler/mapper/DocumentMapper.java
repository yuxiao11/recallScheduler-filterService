package com.ifeng.recallScheduler.mapper;


import com.ifeng.recallScheduler.item.Document;

import java.util.List;

//@Mapper //在启动类上标识@MapperScan
public interface DocumentMapper {
    Document findById(Long pid);

    List<Document> findAll();

    boolean addProduct(Document product);
}
