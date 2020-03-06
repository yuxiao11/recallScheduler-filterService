package com.ifeng.recallScheduler.filter;

import com.beust.jcommander.internal.Lists;
import com.ifeng.recallScheduler.dao.SourceInfoDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by geyl on 2017/12/14.
 */
@Service
public class ResourcesIO {
    private static final Logger logger = LoggerFactory.getLogger(ResourcesIO.class);

    private static final String SpecialUser_FILTER_WORDS_FILE = "baseconfig/specialFilterUser-filter-words.txt";

    @Autowired
    SourceInfoDao sourceInfoDao;


    public Map<String, Integer> loadSpecialUserFilterWords() {
        Map<String, Integer> wordScores = new HashMap<>();
        try {
            InputStream in = ClassLoader.getSystemResourceAsStream(SpecialUser_FILTER_WORDS_FILE);
            BufferedReader reader;
            reader = new BufferedReader(new InputStreamReader(in));
            String text;
            while ((text = reader.readLine()) != null) {
                try {
                    text = text.trim().replace("\n", "").replace("\r", "");
                    String[] wordScore = text.split(" ");
                    wordScores.put(wordScore[0].trim(), Integer.valueOf(wordScore[1].trim()));
                } catch (Exception e) {
                    logger.error("put specialFilterUser filter word to map error:{}, text:{}", e, text);
                }

            }
        } catch (Exception e) {
            logger.error("load specialFilterUser filter words error,file:{}, error:{}", SpecialUser_FILTER_WORDS_FILE, e);
        }
        return wordScores;
    }


    /**
     * 从mysql中加载机构账号
     * 获取自媒体名称集合
     * @return
     */
    public List<String> loadWeMediaName() {
        List<String> jiGouZhangHao = sourceInfoDao.getOrganizationMedia();
        logger.info("自媒体 loadWeMediaName SABC from mysql nameSize:{}", jiGouZhangHao.size());
        return jiGouZhangHao;
    }




    /**
     * 将文件按照行读入内存
     *
     * @param fileName
     * @return
     */
    private List<String> loadFile(String fileName) {
        List<String> result = Lists.newArrayList();
        try {
            InputStream in = ClassLoader.getSystemResourceAsStream(fileName);
            BufferedReader reader;
            reader = new BufferedReader(new InputStreamReader(in));
            String text;
            while ((text = reader.readLine()) != null) {
                try {
                    text = text.trim().replace("\n", "").replace("\r", "");
                    result.add(text);
                } catch (Exception e) {
                    logger.error("{} loadFile error:{}, put text:{}", fileName, e, text);
                }
            }
        } catch (Exception e) {
            logger.error("{} loadFile, error:{}", fileName, e);
        }
        return result;
    }


}
