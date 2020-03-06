package com.ifeng.recallScheduler.utils;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;

public class JsonUtil {

    private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);




    /** app客户端使用的date格式，这里保持统一 */
    public final static String TIME_FORMAT ="MMM d, yyyy h:mm:ss a";


    private final static Gson gson = new GsonBuilder()
            .setDateFormat(TIME_FORMAT)
            .create();

    private final static Gson exposeGson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();


    /**
     * 把对象转换为jsonString，并捕获异常
     *
     * @param object
     * @return
     */
    public static String object2jsonWithoutException(Object object) {
        try {
            return gson.toJson(object);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("jsonstr Exception：{}", e);
            return "";
        }
    }

    public static String object2jsonExpose(Object object) {
        try {
            return exposeGson.toJson(object);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("jsonstr Exception：{}", e);
            return "";
        }
    }

    /**
     * 把对象转换为jsonString，并捕获异常
     *
     * @param object obj
     * @return string
     */
    public static String object2jsonWithoutExceptionNew(Object object) {
        try {
            return gson.toJson(object);
        } catch (Exception e) {
            logger.error("jsonstr Exception：{}", e);
            return "";
        }
    }

    /**
     * 将jackson  json转化为对象
     *
     * @param text
     * @param clazz
     * @param <T>
     * @return
     * @throws IOException
     */
    public static final <T> T json2ObjectWithoutException(String text, Class<T> clazz) {
        try {
            return gson.fromJson(text, clazz);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("jsonstr:{},Exception：{}", text, e);
            return null;
        }
    }


    /**
     * 将jackson  json转化为list、map等复杂对象
     * 例如： List<Bean> beanList = mapper.readValue(jsonString, new TypeReference<List<Bean>>() {});
     *
     * @param text
     * @param typeReference
     * @param <T>
     * @return
     * @throws IOException
     */
    public static final <T> T json2Object(String text, Type typeReference) {
        try {
            return gson.fromJson(text, typeReference);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("jsonstr:{},Exception：{}", text, e);
            return null;
        }
    }

}
