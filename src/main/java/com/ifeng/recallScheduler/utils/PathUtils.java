package com.ifeng.recallScheduler.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;


/**
 * 获取文件路径
 * Created by jibin on 2017/5/8.
 */
public class PathUtils {

    private static final Logger logger = LoggerFactory.getLogger(PathUtils.class);

    /**
     * 获取当前类的系统路径
     * @param cls
     * @return
     */
    public static String getCurrentPath(Class<?> cls) {
        String path = cls.getProtectionDomain().getCodeSource().getLocation().getPath();
        path = path.replaceFirst("file:/", "");

        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.indexOf("window") >= 0) {
            if (path.substring(0, 1).equalsIgnoreCase("/")) {
                path = path.substring(1);
            }
        }else {
            int firstIndex = path.lastIndexOf(System.getProperty("path.separator")) + 1;
            int lastIndex = path.lastIndexOf(File.separator) + 1;
            path = path.substring(firstIndex, lastIndex);
        }

        logger.info("The path is:{}",path);

        return path;
    }

}
