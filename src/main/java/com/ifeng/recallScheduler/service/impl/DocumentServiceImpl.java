package com.ifeng.recallScheduler.service.impl;

import com.ifeng.recallScheduler.item.Document;
import com.ifeng.recallScheduler.mapper.DocumentMapper;
import com.ifeng.recallScheduler.service.FilterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service //放入容器
public class DocumentServiceImpl implements FilterService {

    @Autowired
    DocumentMapper documentMapper;

    @Override
    public boolean add(Document doc) {
        return documentMapper.addProduct(doc);
    }

    @Override
    public Document get(Long id) {
        return documentMapper.findById(id);
    }

    @Override
    public List<Document> list() {
        return documentMapper.findAll();
    }
}
