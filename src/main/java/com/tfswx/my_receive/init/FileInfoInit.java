package com.tfswx.my_receive.init;

import com.tfswx.my_receive.entity.MyFileInfo;
import com.tfswx.my_receive.service.MyReceiveService;
import com.tfswx.my_receive.utils.DateUtil;
import com.tfswx.my_receive.utils.FileUtil;
import com.tfswx.my_receive.utils.Parameters;
import com.tfswx.my_receive.utils.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.ArrayList;

/**
 * 配置初始化类，用于找寻配置和创建配置
 */
@Component
@Order(value = 1)
public class FileInfoInit implements CommandLineRunner {
    private static Logger log = LoggerFactory.getLogger(FileInfoInit.class);

    @Autowired
    MyReceiveService myReceiveService;

    //文书信息前缀
    public static final String WS = "ws_";
    //电子卷宗信息前缀
    public static final String DZJZ = "dzjz_";


    @Value("${sendIp.wsSendIp}")
    private String ws_send_ip ;//文书发送端IP

    @Value("${sendIp.dzjzSendIp}")
    private String dzjz_send_ip ;//卷宗发送端IP

    @Value("${findTimes}")
    private int findTimes ;//再次查找次数基数，最大会重复2倍

   @Value("${startYear}")
    private int startYear ;////数据同步开始默认年份

   @Value("${sqlMaxLines}")
    private int sqlMaxLines ;////数据同步开始默认年份


    @Override
    public void run(String... args) throws IOException {
        Parameters.No_Find_Times=findTimes;
        Parameters.START_YEAR=startYear;
        Parameters.SQL_MAX_LINES=sqlMaxLines;
        //获取当前目录，并找到根目录，创建各类文件和文件夹
        getRootPath(ClassUtils.getDefaultClassLoader().getResource("").getPath());
    }


    /**
     * 获取根目录，将根目录放入配置类的rootPath中，用于创建跟类文件和文件夹
     * @param path
     * @throws IOException
     */
    private void getRootPath(String path) throws IOException {
//        System.out.println("我就是文件夹目录:"+path);
        boolean isFindMyProperties = false;
        File[] files = File.listRoots();
        File myProperties = null;
        for(File file : files){
            if(path.indexOf(file.getAbsolutePath().substring(0,2))!=-1){
//                System.out.println("我就是根目录"+file.getAbsolutePath());
                Parameters.rootPath = file.getAbsolutePath();
                myProperties = new File(PropertiesUtil.propertiesFilePath);
                if(myProperties.exists()){
                    isFindMyProperties = true;
                }
            }
        }
        testTables();
        if(isFindMyProperties){//已存在 my_sjfh.properties
            initFileInfoFromProp();
            log.warn("运行时配置信息从my_sjfh.properties加载完成");
        }else {//不存在my_sjfh.properties
            initFileInfoNotProp();
            log.warn("运行时配置信息初始化完成，保存在my_sjfh.properties");
        }


    }

    //已创建存在my_sjfh.properties 时初始化操作
    private void initFileInfoFromProp() throws IOException {
        //创建文书存放路径
        Parameters.propertiesUtil = PropertiesUtil.getInstance();
        if(ArrayList.class.equals(Parameters.propertiesUtil.getValue("dwbm").getClass())){
            ArrayList<String> list = (ArrayList) Parameters.propertiesUtil.getValue("dwbm");
            String dwbm = "";
            for(String str : list){
                dwbm += str+",";
            }
            Parameters.dwbm = dwbm.substring(0,dwbm.length()-1);
        }else {
            Parameters.dwbm = String.valueOf(Parameters.propertiesUtil.getValue("dwbm"));
        }

//        if(List.class.isAssignableFrom(Parameters.propertiesUtil.getValue("dwbm").getClass())){
//            System.out.println("Listequals没问题");
//        }

        if(ArrayList.class.equals(Parameters.propertiesUtil.getValue("typeRestriction").getClass())){
            ArrayList<String> list = (ArrayList) Parameters.propertiesUtil.getValue("typeRestriction");
            String typeRestriction = "";
            for(String str : list){
                typeRestriction += str+",";
            }
            Parameters.typeRestriction = typeRestriction.substring(0,typeRestriction.length()-1);
        }else {
            Parameters.typeRestriction = getStrValue(Parameters.propertiesUtil.getValue("typeRestriction"));
        }
        Parameters.rootPath = getStrValue(Parameters.propertiesUtil.getValue("rootPath"));
        Parameters.sendIPMap.put("w","");
        Parameters.sendIPMap.put("d","");

        String wsSendIP = getStrValue(Parameters.propertiesUtil.getValue("wsSendIP"));
        String dzjzSendIP= getStrValue(Parameters.propertiesUtil.getValue("dzjzSendIP"));

        //运行配置文件my_sjfh.properties为空时用 application.yml里的配置初始化赋值
        if(StringUtils.isEmpty(wsSendIP)){
            wsSendIP= ws_send_ip;
        }
        if(StringUtils.isEmpty(dzjzSendIP)){
            dzjzSendIP= dzjz_send_ip;
        }

        if(!"".equals(wsSendIP)){
            Parameters.sendIPMap.put("w",wsSendIP);
            Parameters.sendPathMap.put("w",Parameters.SEND_TITLE + wsSendIP + Parameters.SEND_LAST);
        }
        if(!"".equals(dzjzSendIP)){
            Parameters.sendIPMap.put("d",dzjzSendIP);
            Parameters.sendPathMap.put("d",Parameters.SEND_TITLE + dzjzSendIP + Parameters.SEND_LAST);
        }

        Parameters.isMyCanSendNow = Boolean.parseBoolean(getStrValue(Parameters.propertiesUtil.getValue("isMyCanSendNow")));

        if (Parameters.isMyCanSendNow && !Parameters.isCanSendNow) {
            Parameters.isCanSendNow = true;
        }

        //存放文书信息
        Parameters.wsFileInfo = new MyFileInfo();

        //存放卷宗信息
        Parameters.dzjzFileInfo = new MyFileInfo();
//        Parameters.wsFileInfo.setLogFilePath(getStrValue(Parameters.propertiesUtil.getValue(WS+"LogFilePath")));
//        Parameters.wsFileInfo.setNoFilePath(getStrValue(Parameters.propertiesUtil.getValue(WS+"NoFilePath")));
        Parameters.wsFileInfo.setFilePathTitle(getStrValue(Parameters.propertiesUtil.getValue(WS+"FilePathTitle")));
        Parameters.wsFileInfo.setFileType(getStrValue(Parameters.propertiesUtil.getValue(WS+"FileType")));
        Parameters.wsFileInfo.setFileIsDecrypt(Boolean.parseBoolean(getStrValue(Parameters.propertiesUtil.getValue(WS+"FileIsDecrypt"))));
        Parameters.wsFileInfo.setYear(Integer.parseInt(getStrValue(Parameters.propertiesUtil.getValue(WS+"Year"))));
        Parameters.wsFileInfo.setFileIsSynchronizationNow(Boolean.parseBoolean(getStrValue(Parameters.propertiesUtil.getValue(WS+"FileIsSynchronizationNow"))));
        Parameters.wsFileInfo.setFileIsSynchronization(Boolean.parseBoolean(getStrValue(Parameters.propertiesUtil.getValue(WS+"FileIsSynchronization"))));
        Parameters.wsFileInfo.setManualAynchNum(Integer.parseInt(getStrValue(Parameters.propertiesUtil.getValue(WS+"ManualAynchNum"))));
        Parameters.wsFileInfo.setFileNum(Integer.parseInt(getStrValue(Parameters.propertiesUtil.getValue(WS+"FileNum"))));
        Parameters.wsFileInfo.setNoFileNum(Integer.parseInt(getStrValue(Parameters.propertiesUtil.getValue(WS+"NoFileNum"))));
        Parameters.wsFileInfo.setIsWs(Boolean.parseBoolean(getStrValue(Parameters.propertiesUtil.getValue(WS+"IsWs"))));
        Parameters.wsFileInfo.setStartDate(DateUtil.getDate4Str(getStrValue(Parameters.propertiesUtil.getValue(WS+"StartDate"))));
        Parameters.wsFileInfo.setEndDate(DateUtil.getDate4Str(getStrValue(Parameters.propertiesUtil.getValue(WS+"EndDate"))));
//        Parameters.wsFileInfo.setRunDate(DateUtil.getDate4Str(getStrValue(Parameters.propertiesUtil.getValue(WS+"RunDate"))));

//        Parameters.dzjzFileInfo.setLogFilePath(getStrValue(Parameters.propertiesUtil.getValue(DZJZ+"LogFilePath")));
//        Parameters.dzjzFileInfo.setNoFilePath(getStrValue(Parameters.propertiesUtil.getValue(DZJZ+"NoFilePath")));
        Parameters.dzjzFileInfo.setFilePathTitle(getStrValue(Parameters.propertiesUtil.getValue(DZJZ+"FilePathTitle")));
        Parameters.dzjzFileInfo.setFileType(getStrValue(Parameters.propertiesUtil.getValue(DZJZ+"FileType")));
        Parameters.dzjzFileInfo.setFileIsDecrypt(Boolean.parseBoolean(getStrValue(Parameters.propertiesUtil.getValue(DZJZ+"FileIsDecrypt"))));
        Parameters.dzjzFileInfo.setYear(Integer.parseInt(getStrValue(Parameters.propertiesUtil.getValue(DZJZ+"Year"))));
        Parameters.dzjzFileInfo.setFileIsSynchronizationNow(Boolean.parseBoolean(getStrValue(Parameters.propertiesUtil.getValue(DZJZ+"FileIsSynchronizationNow"))));
        Parameters.dzjzFileInfo.setFileIsSynchronization(Boolean.parseBoolean(getStrValue(Parameters.propertiesUtil.getValue(DZJZ+"FileIsSynchronization"))));
        Parameters.dzjzFileInfo.setManualAynchNum(Integer.parseInt(getStrValue(Parameters.propertiesUtil.getValue(DZJZ+"ManualAynchNum"))));
        Parameters.dzjzFileInfo.setFileNum(Integer.parseInt(getStrValue(Parameters.propertiesUtil.getValue(DZJZ+"FileNum"))));
        Parameters.dzjzFileInfo.setNoFileNum(Integer.parseInt(getStrValue(Parameters.propertiesUtil.getValue(DZJZ+"NoFileNum"))));
        Parameters.dzjzFileInfo.setIsWs(Boolean.parseBoolean(getStrValue(Parameters.propertiesUtil.getValue(DZJZ+"IsWs"))));
        Parameters.dzjzFileInfo.setStartDate(DateUtil.getDate4Str(getStrValue(Parameters.propertiesUtil.getValue(DZJZ+"StartDate"))));
        Parameters.dzjzFileInfo.setEndDate(DateUtil.getDate4Str(getStrValue(Parameters.propertiesUtil.getValue(DZJZ+"EndDate"))));
//        Parameters.dzjzFileInfo.setRunDate(DateUtil.getDate4Str(getStrValue(Parameters.propertiesUtil.getValue(DZJZ+"RunDate"))));

        myReceiveService.createLogAndNoFile();
        myReceiveService.testIsBreak();
    }

    //不存在my_sjfh.properties 时初始化操作
    private void initFileInfoNotProp() throws IOException {
        //创建文书存放路径
        FileUtil.createFile(PropertiesUtil.propertiesFilePath);


        //创建文书存放路径
        String wsFilePath = Parameters.rootPath +"WS" + File.separator;
        FileUtil.mkdirsFile(wsFilePath);

        //创建卷宗存放路径
        String dzjzFilePath = Parameters.rootPath + "DZJZ";
        FileUtil.mkdirsFile(dzjzFilePath);

        //存放文书信息
        Parameters.wsFileInfo = new MyFileInfo(true, wsFilePath, Parameters.wsType);

        //存放卷宗信息
        Parameters.dzjzFileInfo = new MyFileInfo(false, dzjzFilePath, Parameters.dzjzType);
        myReceiveService.createLogAndNoFile();

        Parameters.sendIPMap.put("w", ws_send_ip);
        Parameters.sendIPMap.put("d", dzjz_send_ip);

        Parameters.propertiesUtil = PropertiesUtil.getInstance();

        Parameters.propertiesUtil.setProperty(WS+"LogFilePath",Parameters.wsFileInfo.getLogFilePath());
        ////Parameters.propertiesUtil.setComment(WS+"LogFilePath","文书日志文件路径");
        Parameters.propertiesUtil.setProperty(WS+"NoFilePath",Parameters.wsFileInfo.getNoFilePath());
        ////Parameters.propertiesUtil.setComment(WS+"NoFilePath","文书未找到文件路径");
        Parameters.propertiesUtil.setProperty(WS+"FilePathTitle",Parameters.wsFileInfo.getFilePathTitle());
        ////Parameters.propertiesUtil.setComment(WS+"FilePathTitle","文书存放路径");
        Parameters.propertiesUtil.setProperty(WS+"FileType",Parameters.wsFileInfo.getFileType());
        ////Parameters.propertiesUtil.setComment(WS+"FileType","文书类型");
        Parameters.propertiesUtil.setProperty(WS+"FileIsDecrypt",String.valueOf(Parameters.wsFileInfo.getFileIsDecrypt()));
        ////Parameters.propertiesUtil.setComment(WS+"FileIsDecrypt","文书是否解密");
        Parameters.propertiesUtil.setProperty(WS+"Year",String.valueOf(Parameters.wsFileInfo.getYear()));
        //Parameters.propertiesUtil.setComment(WS+"Year","文书当前同步年份");
        Parameters.propertiesUtil.setProperty(WS+"FileIsSynchronizationNow",String.valueOf(Parameters.wsFileInfo.getFileIsSynchronizationNow()));
        //Parameters.propertiesUtil.setComment(WS+"FileIsSynchronizationNow","文书是否实时同步");
        Parameters.propertiesUtil.setProperty(WS+"FileIsSynchronization",String.valueOf(Parameters.wsFileInfo.getFileIsSynchronization()));
        //Parameters.propertiesUtil.setComment(WS+"FileIsSynchronization","文书是否正在同步");

        Parameters.propertiesUtil.setProperty(WS+"ManualAynchNum",String.valueOf(Parameters.wsFileInfo.getManualAynchNum()));
        //Parameters.propertiesUtil.setComment(WS+"FileIsSynchronization","文书手动同步任务的总数");

        Parameters.propertiesUtil.setProperty(WS+"FileNum",String.valueOf(Parameters.wsFileInfo.getFileNum()));
        //Parameters.propertiesUtil.setComment(WS+"FileNum","文书同步数量");
        Parameters.propertiesUtil.setProperty(WS+"NoFileNum",String.valueOf(Parameters.wsFileInfo.getFileNum()));
        //Parameters.propertiesUtil.setComment(WS+"NoFileNum","文书未找到数量");
        Parameters.propertiesUtil.setProperty(WS+"IsWs",String.valueOf(Parameters.wsFileInfo.getIsWs()));
        //Parameters.propertiesUtil.setComment(WS+"IsWs","是否是文书");
        Parameters.propertiesUtil.setProperty(WS+"StartDate",DateUtil.getStr4Date(Parameters.wsFileInfo.getStartDate()));
        //Parameters.propertiesUtil.setComment(WS+"StartDate","文书同步开始时间");
        Parameters.propertiesUtil.setProperty(WS+"EndDate",DateUtil.getStr4Date(Parameters.wsFileInfo.getEndDate()));
        //Parameters.propertiesUtil.setComment(WS+"EndDate","文书同步结束时间");
//        Parameters.propertiesUtil.setProperty(WS+"RunDate",DateUtil.getStr4Date(Parameters.wsFileInfo.getRunDate()));
//        //Parameters.propertiesUtil.setComment(WS+"RunDate","文书同步最后运行时间");


        Parameters.propertiesUtil.setProperty(DZJZ+"LogFilePath",Parameters.dzjzFileInfo.getLogFilePath());
        //Parameters.propertiesUtil.setComment(DZJZ+"LogFilePath","电子卷宗日志文件路径");
        Parameters.propertiesUtil.setProperty(DZJZ+"NoFilePath",Parameters.dzjzFileInfo.getNoFilePath());
        //Parameters.propertiesUtil.setComment(DZJZ+"NoFilePath","电子卷宗未找到文件路径");
        Parameters.propertiesUtil.setProperty(DZJZ+"FilePathTitle",Parameters.dzjzFileInfo.getFilePathTitle());
        //Parameters.propertiesUtil.setComment(DZJZ+"FilePathTitle","电子卷宗存放路径");
        Parameters.propertiesUtil.setProperty(DZJZ+"FileType",Parameters.dzjzFileInfo.getFileType());
        //Parameters.propertiesUtil.setComment(DZJZ+"FileType","电子卷宗类型");
        Parameters.propertiesUtil.setProperty(DZJZ+"FileIsDecrypt",String.valueOf(Parameters.dzjzFileInfo.getFileIsDecrypt()));
        //Parameters.propertiesUtil.setComment(DZJZ+"FileIsDecrypt","电子卷宗是否解密");
        Parameters.propertiesUtil.setProperty(DZJZ+"Year",String.valueOf(Parameters.dzjzFileInfo.getYear()));
        //Parameters.propertiesUtil.setComment(DZJZ+"Year","电子卷宗当前同步年份");
        Parameters.propertiesUtil.setProperty(DZJZ+"FileIsSynchronizationNow",String.valueOf(Parameters.dzjzFileInfo.getFileIsSynchronizationNow()));
        //Parameters.propertiesUtil.setComment(DZJZ+"FileIsSynchronizationNow","电子卷宗是否实时同步");
        Parameters.propertiesUtil.setProperty(DZJZ+"FileIsSynchronization",String.valueOf(Parameters.dzjzFileInfo.getFileIsSynchronization()));
        //Parameters.propertiesUtil.setComment(DZJZ+"FileIsSynchronization","电子卷宗是否正在同步");

        Parameters.propertiesUtil.setProperty(DZJZ+"ManualAynchNum",String.valueOf(Parameters.dzjzFileInfo.getManualAynchNum()));
        //Parameters.propertiesUtil.setComment(DZJZ+"FileIsSynchronization","电子卷宗手动同步任务的总数");

        Parameters.propertiesUtil.setProperty(DZJZ+"FileNum",String.valueOf(Parameters.dzjzFileInfo.getFileNum()));
        //Parameters.propertiesUtil.setComment(DZJZ+"FileNum","电子卷宗同步数量");
        Parameters.propertiesUtil.setProperty(DZJZ+"NoFileNum",String.valueOf(Parameters.wsFileInfo.getFileNum()));
        //Parameters.propertiesUtil.setComment(DZJZ+"NoFileNum","电子卷宗未找到数量");
        Parameters.propertiesUtil.setProperty(DZJZ+"IsWs",String.valueOf(Parameters.dzjzFileInfo.getIsWs()));
        //Parameters.propertiesUtil.setComment(DZJZ+"IsWs","是否是文书");
        Parameters.propertiesUtil.setProperty(DZJZ+"StartDate",DateUtil.getStr4Date(Parameters.dzjzFileInfo.getStartDate()));
        //Parameters.propertiesUtil.setComment(DZJZ+"StartDate","电子卷宗同步开始时间");
        Parameters.propertiesUtil.setProperty(DZJZ+"EndDate",DateUtil.getStr4Date(Parameters.dzjzFileInfo.getEndDate()));
        //Parameters.propertiesUtil.setComment(DZJZ+"EndDate","电子卷宗同步结束时间");
//        Parameters.propertiesUtil.setProperty(DZJZ+"RunDate",DateUtil.getStr4Date(Parameters.dzjzFileInfo.getRunDate()));
//        //Parameters.propertiesUtil.setComment(DZJZ+"RunDate","电子卷宗同步最后运行时间");

        Parameters.propertiesUtil.setProperty("dwbm",Parameters.dwbm);
        //Parameters.propertiesUtil.setComment("dwbm","同步的单位编码");
        Parameters.propertiesUtil.setProperty("typeRestriction",Parameters.typeRestriction);
        //Parameters.propertiesUtil.setComment("typeRestriction","同步类型");
        Parameters.propertiesUtil.setProperty("rootPath",Parameters.rootPath);
        //Parameters.propertiesUtil.setComment("rootPath","文件存放根目录");
        Parameters.propertiesUtil.setProperty("wsSendIP",Parameters.sendIPMap.get("w"));
        //Parameters.propertiesUtil.setComment("wsSendIP","文书请求地址");
        Parameters.propertiesUtil.setProperty("dzjzSendIP",Parameters.sendIPMap.get("d"));
        //Parameters.propertiesUtil.setComment("dzjzSendIP","电子卷宗请求地址");
        Parameters.propertiesUtil.setProperty("isMyCanSendNow", String.valueOf(Parameters.isMyCanSendNow));
        //Parameters.propertiesUtil.setComment("isMyCanSendNow", "是否开启同步");
    }


    public String getStrValue(Object obj){
        return String.valueOf(obj);
    }

    //检测创建数据返还文件类必要的表和触发器
    public void testTables() {

        //新文件表
        if (myReceiveService.findTables("SJFH_FILE_NEWEST") == 0) {
            myReceiveService.createNewFileTable();
        }
        //文书获取触发器
        if (myReceiveService.findTrigger("FILE_WS_UPDATE") == 0) {
            myReceiveService.createWsTrigger();
        }
        //电子卷宗获取触发器
        if (myReceiveService.findTrigger("FILE_DZJZ_UPDATE") == 0) {
            myReceiveService.createDzjzTrigger();
        }
        //未找到文件表
        if (myReceiveService.findTables("SJFH_FILE_NOFILE") == 0) {
            myReceiveService.createNoFileTable();
        }
        //未找到文件表
        if (myReceiveService.findTables("SJFH_FILE_DOWNLOAD") == 0) {
            myReceiveService.createDownloadTable();
        }
    }

}
