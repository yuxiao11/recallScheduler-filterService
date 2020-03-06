package com.ifeng.recallScheduler.service;

import com.ifeng.recallScheduler.item.Document;

import java.util.List;

public interface FilterService {
    boolean add(Document doc);

    Document get(Long id);

    List<Document> list();
}
