package com.tfswx.my_receive.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.tfswx.my_receive.entity.MyFile;
import com.tfswx.my_receive.entity.MyFileInfo;
import com.tfswx.my_receive.entity.WriteFile;
import com.tfswx.my_receive.init.FileInfoInit;
import com.tfswx.my_receive.mapper.FileReceiveMapper;
import com.tfswx.my_receive.utils.*;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.io.*;
import java.util.*;

import static com.tfswx.my_receive.utils.DateUtil.getStr4Date;
import static com.tfswx.my_receive.utils.Parameters.*;

@Service
@Transactional
public class MyReceiveServiceImpl implements MyReceiveService {
    private static Logger log = LoggerFactory.getLogger(MyReceiveServiceImpl.class);

    @Autowired
    ThreadPoolTaskScheduler threadPoolTaskScheduler;

    @Autowired
    FileReceiveMapper fileReceiveMapper;

    // 1.获取事务控制管理器
    @Autowired
    private DataSourceTransactionManager transactionManager;

    //写日志的类
//    static FileWriter fw;


    /**
     * 建立存放文件的文件夹
     *
     * @param
     * @return
     */
    @Override
    public void createLogAndNoFile() {

        //获取当前时间字符串
        String dateTime = DateUtil.getStr4Date2(new Date());
        //存放文书日志地址
        String wsLogFilePath = Parameters.rootPath + "SJFH2\\ws\\log\\log_" + dateTime + ".txt";
        //存放未找到文书的地址
        String wsNoFilePath = Parameters.rootPath + "SJFH2\\ws\\nofile\\nofile_" + dateTime + ".txt";

        //创建文件
        FileUtil.createFile(wsLogFilePath);
        FileUtil.createFile(wsNoFilePath);

        //存放电子卷宗日志地址
        String dzjzLogFilePath = Parameters.rootPath + "SJFH2\\dzjz\\log\\log_" + dateTime + ".txt";
        //存放未找到电子卷宗日志地址
        String dzjzNoFilePath = Parameters.rootPath + "SJFH2\\dzjz\\nofile\\nofile_" + dateTime + ".txt";
        //创建文件
        FileUtil.createFile(dzjzLogFilePath);
        FileUtil.createFile(dzjzNoFilePath);
        //将信息写入配置文件
        Parameters.wsFileInfo.setLogFilePath(wsLogFilePath);
        Parameters.wsFileInfo.setNoFilePath(wsNoFilePath);
        Parameters.dzjzFileInfo.setLogFilePath(dzjzLogFilePath);
        Parameters.dzjzFileInfo.setNoFilePath(dzjzNoFilePath);
        //将信息写入log文件
        writeFile(Parameters.wsFileInfo.getLogFilePath(), "文书日志地址改为：" + wsLogFilePath + "\r\n\r\n文书未找到地址改为：" + wsNoFilePath + "\r\n\r\n------文书日志地址更新-------");
        writeFile(Parameters.dzjzFileInfo.getLogFilePath(), "电子卷宗日志地址改为：" + dzjzLogFilePath + "\r\n\r\n电子卷宗未找到地址改为：" + dzjzNoFilePath + "\r\n\r\n------电子卷宗日志地址更新-------");

        String wsSaveSQLPath = Parameters.rootPath + "SJFH2\\ws\\download\\";
        //扫描存放已下载文书sql文件夹
        FileUtil.mkdirsFile(wsSaveSQLPath);
        String dzjzSaveSQLPath = Parameters.rootPath + "SJFH2\\dzjz\\download\\";
        //扫描存放已下载电子卷宗sql文件夹
        FileUtil.mkdirsFile(dzjzSaveSQLPath);

    }


    /**
     * 对文件进行写入
     *
     * @param filePath
     * @param str
     */
/*    public void writeFile(String filePath, String str) {
        try {
            fw = new FileWriter(filePath, true);//true 追加写入
            fw.write(str + "\r\n\r\n");
            fw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fw != null) {
                    fw.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
         }
        }*/
    public void writeFile(String filePath, String str) {
        FileOutputStream outSTr = null;
        BufferedOutputStream buff = null;
        try {
            outSTr = new FileOutputStream(new File(filePath), true);//追加写入
            buff = new BufferedOutputStream(outSTr);
            buff.write((str + "\r\n\r\n").getBytes());
            buff.flush();
            buff.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(buff);
            IOUtils.closeQuietly(outSTr);
        }
    }

    @Override
    public JsonResult updateFilePathTitle(Boolean isWs, String filePathTitle) {
        //判断是否是文书
        if (isWs) {
            //判断路径是否包含“\”，不包含则添加一个
            if (!filePathTitle.endsWith("\\")) {
                filePathTitle = filePathTitle + "\\";
            }
            //创建文件夹
            FileUtil.mkdirsFile(filePathTitle);
            //判断文件夹是否修改，更新配置类的信息，将信息写入配置文件和log文件
            if (new File(filePathTitle).exists()) {
                writeFile(Parameters.wsFileInfo.getLogFilePath(), "文件接收地址改为：" + filePathTitle);
                Parameters.wsFileInfo.setFilePathTitle(filePathTitle);
                updateProperties(Parameters.wsFileInfo, "FilePathTitle", Parameters.wsFileInfo.getFilePathTitle());
                return new JsonResult("修改成功");
            }
            return new JsonResult("修改失败");
        } else {
            //判断是否包含“\”，若包含则删除
            if (filePathTitle.endsWith("\\")) {
                filePathTitle = retrunFilePathTitle(filePathTitle);
            }
            //创建文件夹目录
            FileUtil.mkdirsFile(filePathTitle);
            //判断目录是否已经生成成功，更新配置类的信息，将信息写入配置文件和log文件
            if (new File(filePathTitle).exists()) {
                writeFile(Parameters.dzjzFileInfo.getLogFilePath(), "文件接收地址改为：" + filePathTitle);
                Parameters.dzjzFileInfo.setFilePathTitle(filePathTitle);
                updateProperties(Parameters.dzjzFileInfo, "FilePathTitle", Parameters.dzjzFileInfo.getFilePathTitle());
                return new JsonResult("修改成功");
            }
            return new JsonResult("修改失败");
        }
    }

    //删除末尾的“\”
    public String retrunFilePathTitle(String filePathTitle) {
        if (filePathTitle.endsWith("\\")) {
            return retrunFilePathTitle(filePathTitle.substring(0, filePathTitle.length() - 1));
        }
        return filePathTitle;
    }

    @Override
    public JsonResult updateFileIsDecrypt(Boolean isWs) {
        if (!Parameters.canDecrypt) {
            return new JsonResult(JsonResult.ERROR, "您没有权限执行该操作");
        }
        Parameters.canDecrypt = false;

        //是否是文书
        if (isWs) {
            //判断配置类目前的属性，再进行属性修改
            if (Parameters.wsFileInfo.getFileIsDecrypt()) {
                Parameters.wsFileInfo.setFileIsDecrypt(false);
            } else {
                Parameters.wsFileInfo.setFileIsDecrypt(true);
            }
            writeFile(Parameters.wsFileInfo.getLogFilePath(), "文件是否解密改为：" + Parameters.wsFileInfo.getFileIsDecrypt());
            updateProperties(Parameters.wsFileInfo, "FileIsDecrypt", String.valueOf(Parameters.wsFileInfo.getFileIsDecrypt()));
        } else {
            if (Parameters.dzjzFileInfo.getFileIsDecrypt()) {
                Parameters.dzjzFileInfo.setFileIsDecrypt(false);
            } else {
                Parameters.dzjzFileInfo.setFileIsDecrypt(true);
            }
            writeFile(Parameters.dzjzFileInfo.getLogFilePath(), "文件是否解密改为：" + Parameters.dzjzFileInfo.getFileIsDecrypt());
            updateProperties(Parameters.dzjzFileInfo, "FileIsDecrypt", String.valueOf(Parameters.dzjzFileInfo.getFileIsDecrypt()));
        }
        return new JsonResult("修改成功");
    }


    @Override
    public JsonResult updateDwbm(String dwbm) {
        //正则验证单位是否正确
        if ("".equals(dwbm) || dwbm.matches("^\\d{6}||\\d{6}([,]\\d{6})+||\\d{6}[-]\\d{6}$")) {
            Parameters.dwbm = dwbm;
            Parameters.propertiesUtil.setProperty("dwbm", dwbm);
            return new JsonResult("修改成功");
        } else {
            return new JsonResult(JsonResult.ERROR, "单位编码格式有误");
        }
    }

    @Override
    public JsonResult updateTypeRestriction(String typeRestriction) {
        //正则验证种类是否正确
        if ("".equals(typeRestriction) || typeRestriction.matches("^\\d{4}||\\d{4}([,]\\d{4})+$")) {
            Parameters.typeRestriction = typeRestriction;
            Parameters.propertiesUtil.setProperty("typeRestriction", typeRestriction);
            return new JsonResult("修改成功");
        } else {
            return new JsonResult(JsonResult.ERROR, "同步类型格式有误");
        }
    }


    @Override
    public JsonResult updateSendIP(Boolean isWs, String sendIP) {
//        if (Parameters.dzjzFileInfo.getFileIsSynchronizationNow() || Parameters.dzjzFileInfo.getFileIsSynchronization() || Parameters.wsFileInfo.getFileIsSynchronizationNow() || Parameters.wsFileInfo.getFileIsSynchronization()) {
//            return new JsonResult(JsonResult.ERROR,"请先关闭所有同步服务");
//        }


        //判断IP格式
        if (!"".equals(sendIP) && sendIP.matches("^\\d{1,3}[.]\\d{1,3}[.]\\d{1,3}[.]\\d{1,3}$")) {
            if (isWs) {
                Parameters.sendIPMap.put("w", sendIP);
                Parameters.sendPathMap.put("w", Parameters.SEND_TITLE + sendIP + Parameters.SEND_LAST);
                Parameters.propertiesUtil.setProperty("wsSendIP", sendIP);
            } else {
                Parameters.sendIPMap.put("d", sendIP);
                Parameters.sendPathMap.put("d", Parameters.SEND_TITLE + sendIP + Parameters.SEND_LAST);
                Parameters.propertiesUtil.setProperty("dzjzSendIP", sendIP);
            }
            return new JsonResult("修改成功");
        } else {
            return new JsonResult(JsonResult.ERROR, "IP格式有误");
        }
    }


    @Override
    public JsonResult updateFileSynchronizationNow(Boolean isWs) {
        //是否是文书
        if (isWs) {
            //判断当前文件格式，然后修改配置类信息
            if (Parameters.wsFileInfo.getFileIsSynchronizationNow()) {
                Parameters.wsFileInfo.setFileIsSynchronizationNow(false);
            } else {
                Parameters.wsFileInfo.setFileIsSynchronizationNow(true);
            }
            //写入配置和log文件
            writeFile(Parameters.wsFileInfo.getLogFilePath(), "文件是否实时更新改为：" + Parameters.wsFileInfo.getFileIsSynchronizationNow());
            updateProperties(Parameters.wsFileInfo, "FileIsSynchronizationNow", String.valueOf(Parameters.wsFileInfo.getFileIsSynchronizationNow()));
        } else {
            if (Parameters.dzjzFileInfo.getFileIsSynchronizationNow()) {
                Parameters.dzjzFileInfo.setFileIsSynchronizationNow(false);
            } else {
                Parameters.dzjzFileInfo.setFileIsSynchronizationNow(true);
            }
            writeFile(Parameters.dzjzFileInfo.getLogFilePath(), "文件是否实时更新改为：" + Parameters.dzjzFileInfo.getFileIsSynchronizationNow());
            updateProperties(Parameters.dzjzFileInfo, "FileIsSynchronizationNow", String.valueOf(Parameters.dzjzFileInfo.getFileIsSynchronizationNow()));
        }
        return new JsonResult("修改成功");
    }


    @Override
    public JsonResult startFileSynchronization(Boolean isWs, String startTime, String endTime) {
        log.info("文书源端IP：" + Parameters.sendIPMap.get("w"));
        log.info("卷宗源端IP：" + Parameters.sendIPMap.get("d"));
        if (isWs) {
            if ("".equals(Parameters.sendIPMap.get("w"))) {
                return new JsonResult(JsonResult.ERROR, "请先配置文书请求IP地址");
            }

            isMyCanSendNow(true);

            //记录操作信息
            String msg = "收到文书同步请求:开始时间：" + startTime + "结束时间：" + endTime;
            writeFile(Parameters.wsFileInfo.getLogFilePath(), msg);
            log.info(msg);

            if ("".equals(startTime) && "".equals(endTime)) {
                //根据年份全同步
                synchronizationByYear(Parameters.wsFileInfo, DateUtil.getYear());
            } else {
                //根据时间同步
                synchronizationByDate(Parameters.wsFileInfo, startTime, endTime);
            }
        } else {

            if ("".equals(Parameters.sendIPMap.get("d"))) {
                return new JsonResult(JsonResult.ERROR, "请先配置电子卷宗请求IP地址");
            }
            isMyCanSendNow(true);
            String msg = "收到电子卷宗同步请求:startTime：" + startTime + "endTime：" + endTime;
            writeFile(Parameters.dzjzFileInfo.getLogFilePath(), msg);
            log.info(msg);
            if ("".equals(startTime) && "".equals(endTime)) {
                synchronizationByYear(Parameters.dzjzFileInfo, DateUtil.getYear());
            } else {
                synchronizationByDate(Parameters.dzjzFileInfo, startTime, endTime);
            }
        }
        return new JsonResult("同步完成");
    }

    /**
     * 是否【能】进行实时同步
     *
     * @param isMyCanSendNow
     */
    public JsonResult isMyCanSendNow(Boolean isMyCanSendNow) {
        if (isMyCanSendNow == null) {
            isMyCanSendNow = true;
        }
        Parameters.isMyCanSendNow = isMyCanSendNow;
        Parameters.propertiesUtil.setProperty("isMyCanSendNow", String.valueOf(isMyCanSendNow));
        Parameters.isCanSendNow = isMyCanSendNow;
        return new JsonResult("修改成功");
    }

    @Override
    public JsonResult scanDownloadedFiles(String fileType) {
        String msg = "文书";
        if (dzjzType.equals(fileType)) {
            msg = "电子卷宗";
        }
        log.warn("扫描已下载" + msg + "文件，" + fileType);
        try {
            String wsSaveSQLPath = Parameters.rootPath + "SJFH2\\ws\\download\\";
            String dzjzSaveSQLPath = Parameters.rootPath + "SJFH2\\dzjz\\download\\";
            long currentTimeMillis = System.currentTimeMillis();
            long count = -1;
            if (wsType.equals(fileType)) {
                //存放已下载文书扫描sql文件
                count = new ScanFiles(fileType, Parameters.wsFileInfo.getFilePathTitle(), wsSaveSQLPath).scanFilesWithNoRecursion();
            } else if (dzjzType.equals(fileType)) {
                //存放已下载电子卷宗扫描sql文件
                count = new ScanFiles(fileType, Parameters.dzjzFileInfo.getFilePathTitle(), dzjzSaveSQLPath).scanFilesWithNoRecursion();
            }
            log.warn(msg + "已下载文件总数：" + count);
            long currentTimeMillis2 = System.currentTimeMillis();
            log.warn("耗时(分)：" + (currentTimeMillis2 - currentTimeMillis) / 60000);
            return new JsonResult(msg + "已下载文件扫描完成，总数：" + count + " ，耗时：" + (currentTimeMillis2 - currentTimeMillis) / 60000
                    + "分钟 ；sql文件保存在：" + wsSaveSQLPath);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return new JsonResult(JsonResult.ERROR, "扫描失败" + e.getMessage());
        }
    }

    @Override
    public void testIsBreak() {
        //判断是否有时间信息，有则立即进行同步
        if (Parameters.wsFileInfo.getStartDate() != null && Parameters.wsFileInfo.getEndDate() != null) {
            startFileSynchronization(Parameters.wsFileInfo);
        } else {
            if (Parameters.wsFileInfo.getYear() != 0) {
                synchronizationByYear(Parameters.wsFileInfo, Parameters.wsFileInfo.getYear());
            }
        }
        if (Parameters.dzjzFileInfo.getStartDate() != null && Parameters.dzjzFileInfo.getEndDate() != null) {
            startFileSynchronization(Parameters.dzjzFileInfo);
        } else {
            if (Parameters.dzjzFileInfo.getYear() != 0) {
                synchronizationByYear(Parameters.dzjzFileInfo, Parameters.wsFileInfo.getYear());
            }
        }
    }

    //通过年份进行同步
    private void synchronizationByYear(MyFileInfo myFileInfo, Integer year) {
        myFileInfo.setYear(year);
        myFileInfo.setStartDate(DateUtil.getDate4Str(myFileInfo.getYear() + "-01-01"));
        myFileInfo.setEndDate(DateUtil.getDate4Str((myFileInfo.getYear() + 1) + "-01-01"));
        updatePropertiesTime(myFileInfo);
        startFileSynchronization(myFileInfo);
    }

    //通过时间进行同步
    private void synchronizationByDate(MyFileInfo myFileInfo, String startTime, String endTime) {
        myFileInfo.setYear(0);
        getTime(myFileInfo, startTime, endTime);
        updatePropertiesTime(myFileInfo);
        startFileSynchronization(myFileInfo);
    }

    //开始同步操作
    public void startFileSynchronization(MyFileInfo myFileInfo) {
        //设置是否正在同步
        myFileInfo.setFileIsSynchronization(true);
        //修改配置文件
        updateProperties(myFileInfo, "FileIsSynchronization", "true");
        //添加防止同步出错的定时器
        setCron(myFileInfo);
        //接收文件信息的集合
//        List<MyFile> fileList;
        //传入数据库的信息
        Map<String, Object> map = new HashMap<>();
        map.put("fdwbm", getDwbmSql("f.dwbm", Parameters.dwbm));
        map.put("ajdwbm", getDwbmSql("aj.cbdw_bm", Parameters.dwbm));

        map.put("typeRestriction", Parameters.typeRestriction);
        map.put("startDate", myFileInfo.getStartDate());
        map.put("endDate", myFileInfo.getEndDate());

        log.info("条件参数 " + getDwbmSql("f.dwbm", Parameters.dwbm) + "；startDate:" + getStr4Date(myFileInfo.getStartDate()) + "；endDate:" + getStr4Date(myFileInfo.getEndDate()));
        int totalRows = 0;
        //判断类型进行数据查询
        if (myFileInfo.getIsWs()) {
            log.info("根据条件参数查询需要同步的文书...");
            totalRows = fileReceiveMapper.countWsFilePathes(map);
            log.info("待同步文书数量是：" + totalRows);
            if (totalRows > 0) {
                wsFileInfo.setNoFileNum(0);//手動同步有數據就重置为0重新计数
                wsFileInfo.setManualAynchNum(totalRows);
                myFileInfo.setNoFileNum(0);
                myFileInfo.setManualAynchNum(totalRows);
                updateProperties(myFileInfo, "ManualAynchNum", String.valueOf(totalRows));
            }
        } else {
            log.info("根据条件参数查询需要同步的电子卷宗...");
            totalRows = fileReceiveMapper.countDzjzFilePathes(map);
            log.info("待同步卷宗数量是：" + totalRows);
            if (totalRows > 0) {
                dzjzFileInfo.setNoFileNum(0);//手動同步有數據就重置为0重新计数
                dzjzFileInfo.setManualAynchNum(totalRows);
                myFileInfo.setNoFileNum(0);
                myFileInfo.setManualAynchNum(totalRows);
                updateProperties(myFileInfo, "ManualAynchNum", String.valueOf(totalRows));
            }
        }


        //记录同步开始时间和信息
        String synStartLog = "文件同步开始时间：" + getStr4Date(new Date()) + "\r\n" +
                "同步时间：" + getStr4Date(myFileInfo.getStartDate()) + "~" + getStr4Date(myFileInfo.getEndDate()) + "\r\n" +
                "文件数量：" + totalRows + "\r\n" +
                "-------------------时间分割线-------------------";
        writeFile(myFileInfo.getLogFilePath(), synStartLog);
        log.info(synStartLog);

        int pages =  totalRows / PAGE_SIZE + (totalRows % PAGE_SIZE == 0L ? 0 : 1);//算法借鉴com.github.pagehelper.Page::public void setTotal(long total)
        //按页面大小循环
        for(int i=1;i<=pages;i++){
            List<MyFile> fileList;
            if(myFileInfo.getIsWs()) {
                PageHelper.startPage(i,PAGE_SIZE,false);//false 只查询不用count
                fileList = fileReceiveMapper.getWsFilePathes(map);
            }else {
                PageHelper.startPage(i,PAGE_SIZE,false);
                fileList = fileReceiveMapper.getDzjzFilePathes(map);
            }
            //遍历数据信息，进行数据查询和写入
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();//获取事务定义
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);//设置事务隔离级别，开启新事务(原事务挂起)
            TransactionStatus status = transactionManager.getTransaction(def); // 获得事务状态

            for (MyFile myFile : fileList) {
                myFileInfo.setRunDate(new Date());
                myFileInfo.setStartDate(myFile.getCjsj());
                updateProperties(myFileInfo, "StartDate", getStr4Date(myFileInfo.getStartDate()));
                myFileInfo.setFileNum(myFileInfo.getFileNum() + 1);
                //从源端下载文件
                byte[] dataBtye = getFileByRequset(myFile);
                writeNowReceiveFile(myFileInfo, dataBtye, myFile);
            }
            //强制提交事务
            transactionManager.commit(status);
        }


        //记录同步结束时间和信息
        int notFindCount = myFileInfo.getIsWs() ? wsFileInfo.getNoFileNum() : dzjzFileInfo.getNoFileNum();
        updateProperties(myFileInfo, "NoFileNum ", String.valueOf(notFindCount));

        String synEndLog = "文件同步结束时间：" + getStr4Date(new Date()) + "\r\n" +
                "同步时间：" + getStr4Date(myFileInfo.getStartDate()) + "~" + getStr4Date(myFileInfo.getEndDate()) + "\r\n" +
                "文件数量：" + totalRows + "\r\n" +
                "-------------------时间分割线-------------------";
        writeFile(myFileInfo.getLogFilePath(), synEndLog);
        log.info(synEndLog);
        //判断是否结束
        if (myFileInfo.getYear() != 0 && myFileInfo.getYear() > Parameters.START_YEAR) {
            synchronizationByYear(myFileInfo, myFileInfo.getYear() - 1);
        } else {
            //若结束，则删除相关信息，并记录信息
            notFindCount = myFileInfo.getIsWs() ? wsFileInfo.getNoFileNum() : dzjzFileInfo.getNoFileNum();
            myFileInfo.setNoFileNum(notFindCount);

            finashSynchronization(myFileInfo);
            String synCountMsg = "--------------" + (myFileInfo.getIsWs() ? "文书" : "卷宗") + "同步结束-------------" + "\r\n" +
                    "******文件同步结束时间：" + getStr4Date(new Date()) + "******" + "\r\n" +
                    "******总共同步文件数量：" + myFileInfo.getFileNum() + "******" + "\r\n" + "\r\n";
            writeFile(myFileInfo.getLogFilePath(), synCountMsg);
            log.info(synCountMsg);
        }
    }

    /**
     * @param fied 表字段名
     * @param dwbm
     * @return
     */
    public String getDwbmSql(String fied, String dwbm) {
        if (dwbm.indexOf(",") != -1) {
            return " " + fied + " IN (" + dwbm + ") ";
        } else if (dwbm.indexOf("-") != -1) {
            String[] dwbms = dwbm.split("-");
            return " " + fied + " >= " + dwbms[0] + " AND " + fied + " <= " + dwbms[1] + " ";
        } else if (StringUtils.isEmpty(dwbm)) {
            return "";
        } else {
            return " " + fied + " = " + dwbm + " ";
        }
    }

    public void finashSynchronization(MyFileInfo myFileInfo) {
        myFileInfo.setYear(0);
        myFileInfo.setStartDate(null);
        myFileInfo.setEndDate(null);
        myFileInfo.setFileIsSynchronization(false);
        updatePropertiesTime(myFileInfo);
        if (myFileInfo.getFileNum() > 0) {
            updateProperties(myFileInfo, "FileNum", String.valueOf(myFileInfo.getFileNum()));
        }
        if (myFileInfo.getNoFileNum() > 0) {
            updateProperties(myFileInfo, "NoFileNum", String.valueOf(myFileInfo.getNoFileNum()));
        }
        updateProperties(myFileInfo, "FileIsSynchronization", "false");
        stopCron(myFileInfo);
        myFileInfo.setFuture(null);
    }

    //手动同步，故障检测定时器（预防方法出错或者由于网络断掉造成的错误）
    public void setCron(MyFileInfo myFileInfo) {
        if (myFileInfo.getFuture() == null) {
            myFileInfo.setFuture(threadPoolTaskScheduler.schedule(new Runnable() {
                @Override
                public void run() {
                    //当检测到15分钟没有运行，则再次启动同步
                    if (myFileInfo.getFileIsSynchronization() && (new Date().getTime() - myFileInfo.getRunDate().getTime()) > 900000) {
                        //记录故障发生
                        String gzMsg = "发生故障时间：" + getStr4Date(new Date()) + "\r\n" +
                                "系统发生故障，采用定时器重启。。。。。。。。";
                        writeFile(myFileInfo.getLogFilePath(), gzMsg);
                        log.error(gzMsg);
                        startFileSynchronization(myFileInfo);
                    }
                }
            }, new Trigger() {
                @Override
                public Date nextExecutionTime(TriggerContext triggerContext) {
                    // 定时任务触发，可修改定时任务的执行周期（目前每小时检测一次）
                    CronTrigger trigger = new CronTrigger("0 0 0/1 * * ?");
//                    CronTrigger trigger = new CronTrigger("0/5 * * * * ?");
                    Date nextExecDate = trigger.nextExecutionTime(triggerContext);
                    return nextExecDate;
                }
            }));
        }
    }

    //停止定时器
    public void stopCron(MyFileInfo myFileInfo) {
        if (myFileInfo.getFuture() != null) {
            myFileInfo.getFuture().cancel(true);
        }
    }

    //获取时间，当startTime为空则替换为new Date(0),当endTime为空，替换为new Date(),自动判断开始时间和结束时间
    private void getTime(MyFileInfo myFileInfo, String startTime, String endTime) {
        Date start = DateUtil.getDate4Str(startTime);
        Date end = DateUtil.getDate4Str(endTime);
        if (start != null && end != null) {
            if (start.getTime() < end.getTime()) {
                myFileInfo.setStartDate(start);
                myFileInfo.setEndDate(end);
            } else {
                myFileInfo.setStartDate(end);
                myFileInfo.setEndDate(start);
            }
        } else if (start != null) {
            myFileInfo.setStartDate(start);
            myFileInfo.setEndDate(new Date());
        } else if (end != null) {
            myFileInfo.setStartDate(new Date(0));
            myFileInfo.setEndDate(end);
        }
    }


    @Override
    public Map<String, Object> getInfo() {
        Map<String, Object> map = new HashMap<>();
        //同步单位
        map.put("dwbm", Parameters.dwbm);
        //同步限制
        map.put("typeRestriction", Parameters.typeRestriction);
        //发送IP
        map.put("sendIP", Parameters.sendIPMap);
        //文件同步配置信息
        map.put("ws", Parameters.wsFileInfo);
        map.put("dzjz", Parameters.dzjzFileInfo);
        //是否【能】进行实时同步
        map.put("isMyCanSendNow", Parameters.isMyCanSendNow);
        return map;
    }


    @Override
    public void findNoFile99() {
        writeNoFileByTimes(99);
    }

    @Override
    public Integer findTables(String tableName) {
        return fileReceiveMapper.findTables(tableName);
    }

    @Override
    public Integer findTrigger(String triggerName) {
        return fileReceiveMapper.findTrigger(triggerName);
    }

    @Override
    public void createNewFileTable() {
        fileReceiveMapper.createNewFileTable();
    }

    @Override
    public void createNoFileTable() {
        fileReceiveMapper.createNoFileTable();
    }

    @Override
    public void createDownloadTable() {
        fileReceiveMapper.createDownloadTable();
    }

    @Override
    public void createWsTrigger() {
        fileReceiveMapper.createWsTrigger();
    }

    @Override
    public void createDzjzTrigger() {
        fileReceiveMapper.createDzjzTrigger();
    }


    /**
     * 实时更新方法，每1分进行数据检测
     */
    @Scheduled(cron = "59 */3 * * * ?")
    public void checkNews() {
        //判断是否可以进行同步和判断是否有文件可以被更新
        if (Parameters.isCanSendNow && fileReceiveMapper.getNewestNum() > 0) {
            log.info("执行文件更新实时检测任务");
            //设置是否可以进行同步为否
            Parameters.isCanSendNow = false;
            //记录运行时间，用于检测该方法是否正常运行
            Parameters.runDate = new Date();
            //记录清楚工作，用于清除不需要同步的文件（目前暂时无用）
//            if (!Parameters.wsFileInfo.getFileIsSynchronizationNow()) {
//                fileReceiveMapper.deleteNewestByFileType(Parameters.wsFileInfo.getFileType());
//            }
//            if (!Parameters.dzjzFileInfo.getFileIsSynchronizationNow()) {
//                fileReceiveMapper.deleteNewestByFileType(Parameters.dzjzFileInfo.getFileType());
//            }
            //获取需要同步的文件
            List<MyFile> fileList = fileReceiveMapper.getNewest();
            log.info("检测发现新文件数量：" + fileList.size());
            //遍历文件并写入
            for (MyFile myFile : fileList) {
                switch (myFile.getFileType()) {
                    case Parameters.wsType:
                        writeNowReceiveFile(Parameters.wsFileInfo, getFileByRequset(myFile), myFile);
                        break;
                    case Parameters.dzjzType:
                        writeNowReceiveFile(Parameters.dzjzFileInfo, getFileByRequset(myFile), myFile);
                        break;
                    default:
                }
                fileReceiveMapper.deleteNewestById(myFile.getId());
            }
            log.info("--------------检测发现的新文件更新结束-------------");
            //设置是否可以进行同步为是
            Parameters.isCanSendNow = true;
        }
    }

    /**
     * 对未找到文件进行查找（前10次为找的的文件为5分钟找一次，预防文件传输延迟造成的错误）
     */
    @Scheduled(cron = "59 */29 * * * ?")  //每隔29分钟执行一次定时任务
    public void keepUpNews() {
        //查找10次以内未找到的文件信息
        List<MyFile> fileList = fileReceiveMapper.getNoFileByFindTime(No_Find_Times);
        if (fileList.size() == 0) {
            return;
        }
        log.info("--------" + No_Find_Times + "次内未找到再次查找未找到文件开始--------");
        log.info(No_Find_Times + "次内未找到文件的查找时间：" + getStr4Date(new Date()));
        log.info(No_Find_Times + "次内未找到文件的数量：" + fileList.size());
        //遍历数据，有文件则写入文件并删除记录，无文件则未找到次数+1
        for (MyFile myFile : fileList) {
            switch (myFile.getFileType()) {
                case Parameters.wsType:
                    writeNowReceiveFile2(Parameters.wsFileInfo, getFileByRequset(myFile), myFile);
                    break;
                case Parameters.dzjzType:
                    writeNowReceiveFile2(Parameters.dzjzFileInfo, getFileByRequset(myFile), myFile);
                    break;
                default:
            }
        }
        log.info("--------" + No_Find_Times + "次内未找到再次查找未找到文件结束--------");
    }

    /**
     * 对未找到文件进行查找（前20次为找的的文件为1小时找一次，针对网络延误过长的文件）
     */
    @Scheduled(cron = "0 30 21 */1 * ?")  //每天21:30执行一次定时任务
    public void writeNoFileDuplation() {
        //查找10次以内未找到的文件信息
        List<MyFile> fileList = fileReceiveMapper.getNoFileByFindTime(No_Find_Times * 2);
        if (fileList.size() == 0) {
            return;
        }
        log.info("--------" + (No_Find_Times * 2) + "次内未找到再次查找未找到文件开始--------");
        log.info((No_Find_Times * 2) + "次内未找到没找到的查找时间：" + getStr4Date(new Date()));
        log.info((No_Find_Times * 2) + "次内未找到没找到的文件数量：" + fileList.size());
        //遍历数据，有文件则写入文件并删除记录，无文件则未找到次数+1
        for (MyFile myFile : fileList) {
            switch (myFile.getFileType()) {
                case Parameters.wsType:
                    writeNowReceiveFile2(Parameters.wsFileInfo, getFileByRequset(myFile), myFile);
                    break;
                case Parameters.dzjzType:
                    writeNowReceiveFile2(Parameters.dzjzFileInfo, getFileByRequset(myFile), myFile);
                    break;
                default:
            }
        }
        log.info("--------" + (No_Find_Times * 2) + "次内未找到再次查找未找到文件结束--------");
    }

    /**
     * 查找未找到文件，并进行写入和删除记录操作
     *
     * @param findTime 未找到次数
     */
    public void writeNoFileByTimes(Integer findTime) {
        List<MyFile> fileList = fileReceiveMapper.getNoFileByFindTime(findTime);//TODO 分页查询的
        log.info("未找到的文件数量为：" + fileList.size());
        if (fileList.size() == 0) {
            return;
        }
        log.info("--------一直没找到再次查找未找到文件开始--------");
        log.info("一直没找到的查找时间：" + getStr4Date(new Date()));
        log.info("一直没找到的文件数量：" + fileList.size());
        for (MyFile myFile : fileList) {
            switch (myFile.getFileType()) {
                case Parameters.wsType:
                    writeNowReceiveFile3(Parameters.wsFileInfo, getFileByRequset(myFile), myFile);
                    break;
                case Parameters.dzjzType:
                    writeNowReceiveFile3(Parameters.dzjzFileInfo, getFileByRequset(myFile), myFile);
                    break;
                default:
            }
        }
        log.info("--------一直没找到再次查找未找到文件结束--------");
    }

    /**
     * 检测实时更新是否停止，若停止，则将是否可以进行同步打开（目前机制不完善，有很多错误未测试）
     */
    @Scheduled(cron = "2 2 */2 * * ?")  //每隔2小时执行一次定时任务
    public void keepUpIsError() {
        if (Parameters.isMyCanSendNow && !Parameters.isCanSendNow && (new Date().getTime() - Parameters.runDate.getTime()) > 360000) {
            Parameters.isCanSendNow = true;
        }
    }

    /**
     * 每月生成一次log和noFile文件记录地址
     */
    @Scheduled(cron = "0 0 0 1 */1 ?")  //每隔1月执行一次定时任务
    public void createLogAndNoFileEveryMonth() {
        createLogAndNoFile();
    }

    /**
     * 文件写入
     *
     * @param myFileInfo 文件配置类
     * @param fileByte   文件byte[]
     * @param myFile     文件信息
     */
    @Async //WARN:异步调用
    public void writeNowReceiveFile3(MyFileInfo myFileInfo, byte[] fileByte, MyFile myFile) {
        //若文件不为空则写入并删除记录
        if (fileByte != null && fileByte.length != 0) {
            log.info(myFile.getFileType() + " 已下载 [" + DateUtil.getStr4Date(myFile.getCjsj()) + "] " + myFile.getFilePath());
            fileReceiveMapper.deleteNoFileById(myFile.getId());
            new Thread(new WriteFile(myFileInfo.getFileIsDecrypt(), myFileInfo.getFilePathTitle() + myFile.getFilePath(), fileByte)).start();
        } else {
            log.info(myFile.getFileType() + " 未找到 [" + DateUtil.getStr4Date(myFile.getCjsj()) + "] " + myFile.getFilePath());
        }
    }

    /**
     * 文件写入
     *
     * @param myFileInfo 文件配置类
     * @param fileByte   文件byte[]
     * @param myFile     文件信息
     */
    @Async //WARN:异步调用
    public void writeNowReceiveFile2(MyFileInfo myFileInfo, byte[] fileByte, MyFile myFile) {
        //若文件不为空则写入并删除记录，否则修改数据库记录使未找到次数+1
        if (fileByte != null && fileByte.length != 0) {
            log.info(myFile.getFileType() + " 已下载 [" + DateUtil.getStr4Date(myFile.getCjsj()) + "] " + myFile.getFilePath());
            new Thread(new WriteFile(myFileInfo.getFileIsDecrypt(), myFileInfo.getFilePathTitle() + myFile.getFilePath(), fileByte)).start();
            fileReceiveMapper.deleteNoFileById(myFile.getId());
        } else {
            log.info(myFile.getFileType() + " 未找到 [" + DateUtil.getStr4Date(myFile.getCjsj()) + "] " + myFile.getFilePath());
            myFile.setFindTime(myFile.getFindTime() + 1);
            fileReceiveMapper.updateNoFileById(myFile);
        }
    }

    /**
     * 文件写入
     *
     * @param myFileInfo 文件配置类
     * @param fileByte   文件byte[]
     * @param myFile     文件信息
     */
    @Async  //WARN:异步调用
    public void writeNowReceiveFile(MyFileInfo myFileInfo, byte[] fileByte, MyFile myFile) {
        //若文件不为空则写入并删除记录，否则记录未找到文件并往未找到文件表中添加数据
        if (fileByte != null && fileByte.length != 0) {
            log.info(myFile.getFileType() + " 已下载 [" + DateUtil.getStr4Date(myFile.getCjsj()) + "] " + myFile.getFilePath());
            new Thread(new WriteFile(myFileInfo.getFileIsDecrypt(), myFileInfo.getFilePathTitle() + myFile.getFilePath(), fileByte)).start();
        } else {
            if (myFileInfo.getIsWs()) {
                wsFileInfo.setNoFileNum(wsFileInfo.getNoFileNum() + 1);
            } else {
                dzjzFileInfo.setNoFileNum(dzjzFileInfo.getNoFileNum() + 1);
            }
            log.info(myFile.getFileType() + " 未找到 [" + DateUtil.getStr4Date(myFile.getCjsj()) + "] " + myFile.getFilePath());
            writeFile(myFileInfo.getNoFilePath(), "文件名：" + myFile.getFilePath());
            fileReceiveMapper.insertNoFile(myFile);

        }
    }

    //通过httpclient传输文件信息类myFile中的文件名称和文件种类，获取文件byte[]，返回内容不为null，无文件则返回的new byte[0]
    public byte[] getFileByRequset(MyFile myFile) {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(60000) //指客户端从服务器读取数据的毫秒timeout 60秒
                .setConnectTimeout(5000) //指客户端和服务器建立连接的timeout
                .setConnectionRequestTimeout(5000) //指从连接池获取连接的timeout
                .build();
        //请求地址
        HttpPost httppost = new HttpPost(Parameters.sendPathMap.get(myFile.getFileType()) + "getFile");
        httppost.addHeader("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.7.6)");
        httppost.setConfig(requestConfig);

        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        CloseableHttpResponse response = null;
        byte[] responseContent = null;
        String fileName = null;
        try {
            fileName = new String(myFile.getFilePath().getBytes("UTF-8"), "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        multipartEntityBuilder.addTextBody("fileName", fileName);
        multipartEntityBuilder.addTextBody("fileType", myFile.getFileType());
        HttpEntity reqEntity = multipartEntityBuilder.build();
        httppost.setEntity(reqEntity);
        try {
            response = httpclient.execute(httppost);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    responseContent = EntityUtils.toByteArray(resEntity);
                }
            }
        } catch (ClientProtocolException cp) {
            cp.printStackTrace();
            log.error(cp.getMessage());
        } catch (IOException ioe) {
            ioe.printStackTrace();
            log.error(ioe.getMessage());
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return responseContent;
    }

    //多处使用，用于修改年份，数据同步起始时间
    private void updatePropertiesTime(MyFileInfo myFileInfo) {
        updateProperties(myFileInfo, "Year", String.valueOf(myFileInfo.getYear()));
        updateProperties(myFileInfo, "StartDate", getStr4Date(myFileInfo.getStartDate()));
        updateProperties(myFileInfo, "EndDate", getStr4Date(myFileInfo.getEndDate()));
    }

    //修改自定义Properties配置文件
    public void updateProperties(MyFileInfo myFileInfo, String key, String value) {
        if (myFileInfo.getIsWs()) {
            Parameters.propertiesUtil.setProperty(FileInfoInit.WS + key, value);
        } else {
            Parameters.propertiesUtil.setProperty(FileInfoInit.DZJZ + key, value);
        }
    }


}
