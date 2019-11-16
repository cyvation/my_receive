package com.tfswx.my_receive.utils;

import com.tfswx.my_receive.entity.MyFileInfo;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 配置信息类，用于存储所有相关配置信息
 */
@Component
public class Parameters {

    //文书信息管理类
    public static MyFileInfo wsFileInfo = null;

    //卷宗信息管理类
    public static MyFileInfo dzjzFileInfo = null;

    //是否能开启实时同步
    public static Boolean isMyCanSendNow = false;

    //是否能进行实时同步
    public static Boolean isCanSendNow = false;

    //是否可以进行解密操作
    public static Boolean canDecrypt = false;

    //运行时间(用于检测实时发送是否故障）
    public static Date runDate = new Date();


    //需要同步的单位编码
    public static String dwbm = "";

    //对文件的类型进行限制（ 案件类别）
    public static String typeRestriction = "";

    //日志存放路径title
    public static String rootPath = "";


    public static Map<String,String> sendPathMap = new HashMap<>();

    public static Map<String,String> sendIPMap = new HashMap<>();



    //文书信息管理类
    public static final String wsType = "w";

    //分页大小
    public  static int PAGE_SIZE = 5000;

    //数据同步起始默认年份
    public  static int START_YEAR = 2014;

    //每个sql文件最大行数 一百万
    public  static int SQL_MAX_LINES = 1000000;

    //卷宗信息管理类
    public static final String dzjzType = "d";

    public final static String SEND_TITLE = "http://";

    public final static String SEND_LAST = ":9527/mySend/";

    //文件未找到次数
    public static int No_Find_Times = 5;

    public static PropertiesUtil propertiesUtil;

}
