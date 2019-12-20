package com.tfswx.my_receive.service;

import com.tfswx.my_receive.entity.MyFile;
import com.tfswx.my_receive.utils.JsonResult;
import java.util.Map;

public interface MyReceiveService {

    /**
     * 修改文件存放地址
     * @param isWs 是否是文书
     * @param filePathTitle 文件存放地址
     * @return
     */
    JsonResult updateFilePathTitle(Boolean isWs, String filePathTitle);

    /**
     * 文件是否解密
     * @param isWs 是否是文书
     * @return
     */
    JsonResult updateFileIsDecrypt(Boolean isWs);

    /**
     * 修改需要同步的单位编码
     * @param dwbm 单位编码
     * @return
     */
    JsonResult updateDwbm(String dwbm);

    /**
     * 修改同步的种类限制
     * @param typeRestriction 同步种类，如：0201(侦监)
     * @return
     */
    JsonResult updateTypeRestriction(String typeRestriction);

    /**
     * 修改获取文件的请求服务器IP
     * @param sendIP 文件发送端IP
     * @return
     */
    JsonResult updateSendIP(Boolean isWs , String sendIP);

    /**
     * 修改文件同步开关，会判断文件是否正在实时同步，是的话就停，不是就开
     * @param isWs 是否是文书
     * @return
     */
    JsonResult updateFileSynchronousSwitch(Boolean isWs);

    /**
     * 开始文件同步（手动进行文件同步，开始时间和结束时间都为空时，进行2013至今的所有数据同步，按年份进行一一同步）
     * @param isWs 是否是文书
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return
     */
    JsonResult startFileSynchronization(Boolean isWs, String startTime, String endTime);

    /**
     * 获取配置信息
     * @return
     */
    Map<String, Object> getInfo();

    /**
     * 创建日志记录和未找到文件的记录
     */
    void createLogAndNoFile();

    /**
     * 判断加载的配置是否有数据，有的话启动就进行同步
     */
    void testIsBreak();

    /**
     * 查找所有未找到的文件，有就进行同步
     */
    void findNoFile99();

    /**
     * 查找所有未找到的文件，有就进行同步
     */
    Integer findTables(String tableName);

    /**
     * 查找所有未找到的文件，有就进行同步
     */
    Integer findTrigger(String triggerName);


    /**
     * 查找所有未找到的文件，有就进行同步
     */
    void createNewFileTable();

    /**
     * 查找所有未找到的文件，有就进行同步
     */
    void createNoFileTable();

    /**
     * 用于分析本地已下载文件
     */
    void createDownloadTable();

    void createWsTrigger(String dwbm);

    /**
     * 查找所有未找到的文件，有就进行同步
     */
    void createDzjzTrigger(String dwbm);

    /**
     * 是否【能】进行实时同步
     * @param isMyCanSendNow
     */
    JsonResult isMyCanSendNow(Boolean isMyCanSendNow);
    /**
     * 扫描已经下载的文件，并生成sql脚本文件
     * @param fileType
     */
//    JsonResult scanDownloadedFiles(String fileType);

}
