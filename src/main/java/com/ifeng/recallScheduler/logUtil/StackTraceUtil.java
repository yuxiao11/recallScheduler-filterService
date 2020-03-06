package com.ifeng.recallScheduler.logUtil;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by liligeng on 2019/7/3.
 */
public class StackTraceUtil {

    public static String getStackTrace(Throwable t){
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        String tmp = sw.toString();
        try {
            sw.close();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tmp;
    }
}
