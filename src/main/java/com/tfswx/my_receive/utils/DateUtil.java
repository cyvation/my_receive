package com.tfswx.my_receive.utils;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 时间处理类
 */
public class DateUtil {
    /**
     * 检验时间格式，String转Date（基本上所有格式），失败则返回null;
     *
     * @param dateString
     * @return
     */
    public static Date getDate4Str(String dateString) {
            if (dateString != null && !"".equals(dateString) && dateString.matches("^\\d{1,4}||\\d{1,4}\\D+\\d{1,2}||\\d{1,4}\\D+\\d{1,2}\\D+\\d{1,2}||\\d{1,4}\\D+\\d{1,2}\\D+\\d{1,2}\\s{1}\\d{2}\\:\\d{2}\\:\\d{2}$")) {
                dateString = dateString.trim().replaceAll("\\D+", "-");
                try {
                    switch (dateString.length()) {
                        case 10:
                            return new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
                        case 9:
                            if (dateString.lastIndexOf("-") == 7) {
                                return new SimpleDateFormat("yyyy-MM-d").parse(dateString);
                            } else {
                                return new SimpleDateFormat("yyyy-M-dd").parse(dateString);
                            }
                        case 8:
                            return new SimpleDateFormat("yyyy-M-d").parse(dateString);
                        case 7:
                            return new SimpleDateFormat("yyyy-MM").parse(dateString);
                        case 6:
                            return new SimpleDateFormat("yyyy-M").parse(dateString);
                        case 4:
                            return new SimpleDateFormat("yyyy").parse(dateString);
                        case 19:
                            return new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").parse(dateString);
                    }
                } catch (ParseException e) {
                    return null;
                }
            }
            return null;
    }

    /**
     * 传入Date类，返回特点格式时间字符串
     * @param date
     * @return
     */
    public static String getStr4Date(Date date) {
        if(date==null){
            return "";
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }
    /**
     * 传入Date类，返回特点格式时间字符串
     * @param date
     * @return
     */
    public static String getStr4Date2(Date date) {
        if(date==null){
            return "";
        }
        return new SimpleDateFormat("yyyy-MM").format(date);
    }

    public static String getStr4DateYMD(Date date) {
        if(date==null){
            return "";
        }
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    /**
     * 获取今年的年份
     *
     * @return
     */
    public static int getYear() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }

}
