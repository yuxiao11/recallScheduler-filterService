package com.ifeng.recallScheduler.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Created by wupeng1 on 2017/3/2.
 * 日期处理工具类
 */
public class LogicDateUtil {

    private static final Logger log = LoggerFactory.getLogger(LogicDateUtil.class);


    public static Date strToDate(String strDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ParsePosition pos = new ParsePosition(0);
        Date strtodate = formatter.parse(strDate, pos);
        return strtodate;
    }

    // false 不过期   true 过期
    public static Boolean expire(Date date, int hours) {
        LocalDateTime nowTime = LocalDateTime.now();
        LocalDateTime docTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime calculateTime = nowTime.minusHours(hours);
        if (docTime.isAfter(calculateTime)) return false;
        return true;
    }

    public static Boolean expire(LocalDateTime docTime, int hours) {
        LocalDateTime nowTime = LocalDateTime.now();
        LocalDateTime calculateTime = nowTime.minusHours(hours);
        if (docTime.isAfter(calculateTime)) return false;
        return true;
    }

    public static Boolean isWorkDay(){
        LocalDateTime nowTime = LocalDateTime.now();
        long dayNumber = nowTime.getDayOfWeek().getValue();
        if (dayNumber == 6 || dayNumber == 7) return false;
        int hourNumber = nowTime.getHour();
        if (hourNumber >= 9 && hourNumber <= 18 ) return true;
        return false;
    }
    public static void main(String[] args) {
        isWorkDay();
    }
}
