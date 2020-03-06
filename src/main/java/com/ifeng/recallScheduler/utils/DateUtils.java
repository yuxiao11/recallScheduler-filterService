package com.ifeng.recallScheduler.utils;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by jibin on 2017/6/23.
 */
public class DateUtils {



    // 定义时间日期显示格式
    // /

    public final static String DATE_FORMAT_MINUTE = "yyyy-MM-dd HH:mm";

    public final static String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public final static String DAY_FORMAT = "yyyyMMdd";

    /**
     * GMT,格式日期
     */
    public final static String GMT_FORMAT = "EEE, dd MMM yyyy HH:mm:ss 'GMT'";

    /**
     * 时区：gmt,非GMT+8
     */
    public final static String TIMESONE = "GMT";


    /**
     * 判断date2Check 发生在baseDate 之前
     * @param baseDate
     * @param date2Check
     * @return
     */
    public static boolean isBeforeDate(Date baseDate, Date date2Check) {

        if (date2Check == null) {
            return false;
        }
        //比较两个日期
        int result = date2Check.compareTo(baseDate);
        if (result < 0) {
            //小于0，参数date1就是在date2之后
            return true;
        }
        return false;
    }



    public static Date strToDate(String strDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ParsePosition pos = new ParsePosition(0);
        Date strtodate = formatter.parse(strDate, pos);
        return strtodate;
    }


    public static String dateToStr(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = sdf.format(date);
        return dateString;
    }


    public static Date strToDate(String strDate, String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
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

    /**
     * 取得当前系统时间，返回java.util.Date类型
     *
     * @return java.util.Date 返回服务器当前系统时间
     *
     * @see Date
     */
    public static Date getCurrDate() {
        return new Date();
    }


    /**
     * 得到格式化后的当前系统时间，格式为yyyy-MM-dd HH:mm:ss，如2009-10-15 15:23:45
     *
     * @return String 返回格式化后的当前服务器系统时间，格式为yyyy-MM-dd HH:mm:ss，如2009-10-15
     * 15:23:45
     *
     */
    public static String getCurrDateTimeStr() {
        return getFormatDateTime(getCurrDate());
    }

    /**
     * 根据格式得到格式化后的时间
     *
     * @param currDate 要格式化的时间
     * @param format   时间格式，如yyyy-MM-dd HH:mm:ss
     *
     * @return String 返回格式化后的时间，格式由参数<code>format</code>定义，如yyyy-MM-dd HH:mm:ss
     *
     * @see SimpleDateFormat#format(Date)
     */
    public static String getFormatDateTime(Date currDate, String format) {
        SimpleDateFormat dtFormatdB = null;
        try {
            dtFormatdB = new SimpleDateFormat(format);
            return dtFormatdB.format(currDate);
        } catch (Exception e) {
            dtFormatdB = new SimpleDateFormat(TIME_FORMAT);
            try {
                return dtFormatdB.format(currDate);
            } catch (Exception ex) {
            }
        }
        return null;
    }

    /**
     * 得到格式化后的时间，格式为yyyy-MM-dd HH:mm:ss，如2009-10-15 15:23:45
     *
     * @param currDate 要格式化的时间
     *
     * @return String 返回格式化后的时间，默认格式为yyyy-MM-dd HH:mm:ss，如2009-10-15 15:23:45
     *
     * @see #getFormatDateTime(Date, String)
     */
    public static String getFormatDateTime(Date currDate) {
        return getFormatDateTime(currDate, TIME_FORMAT);
    }


    public static String getTimestamp2DateStr(long timestamp){
        Date date = new Date(timestamp);
        String dateStr = getFormatDateTime(date);
        return dateStr;
    }


    /**
     * 通过时间秒毫秒数判断两个时间的间隔
     * @param date1
     * @param date2
     * @return
     */
    public static int getDayCount(Date date1,Date date2)
    {
        int days = (int) ((date2.getTime() - date1.getTime()) / (1000*3600*24));
        return days;
    }

    /**
     * 获取当前GMT时间格式
     * @return
     */
    public static String getCurrentDateGmtString() {
        Calendar cd = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(GMT_FORMAT, Locale.US);
        // 设置时区为GMT
        sdf.setTimeZone(TimeZone.getTimeZone(TIMESONE));
        return sdf.format(cd.getTime());
    }


    /**
     * 计算自然日 2019/11/30-2019/12/2  应该是3天
     * @param date1
     * @param date2
     * @return
     */

     public static int getNatureDayCount(Date date1,Date date2)
    {
        int days = (int) ((date2.getTime() - date1.getTime()) / (1000*3600*24))+1;
        return days;
    }

}
