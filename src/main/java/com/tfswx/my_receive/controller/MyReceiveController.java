package com.tfswx.my_receive.controller;

import com.tfswx.my_receive.service.MyReceiveService;
import com.tfswx.my_receive.utils.JsonResult;
import com.tfswx.my_receive.utils.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


//@Scope("prototype")
@RequestMapping("/myReceive")
@RestController
public class MyReceiveController {
    private static Logger log = LoggerFactory.getLogger(MyReceiveController.class);
    @Autowired
    MyReceiveService myReceiveService;

    /**
     * 修改文件存放地址
     * @param isWs 是否是文书
     * @param filePathTitle 文件存放地址
     * @return
     */
    @RequestMapping("/updateFilePathTitle")
    @ResponseBody
    public JsonResult updateFilePathTitle(Boolean isWs, String filePathTitle) {
        log.info("修改文件存放地址");
        return myReceiveService.updateFilePathTitle(isWs, filePathTitle);
    }

    /**
     * 修改文件是否解密
     * @param isWs 是否是文书
     * @return
     */
    @RequestMapping("/updateFileIsDecrypt")
    @ResponseBody
    public JsonResult updateFileIsDecrypt(Boolean isWs) {
        log.info("修改文件是否解密");
        return myReceiveService.updateFileIsDecrypt(isWs);
    }

    /**
     * 修改需要同步的单位编码
     * @param dwbm 单位编码
     * @return
     */
    @RequestMapping("/updateDwbm")
    @ResponseBody
    public JsonResult updateDwbm(String dwbm) {
        log.info("修改需要同步的单位编码");
        return myReceiveService.updateDwbm(dwbm);
    }

    /**
     * 修改同步的种类限制
     * @param typeRestriction 同步种类，如：0201(侦监)
     * @return
     */
    @RequestMapping("/updateTypeRestriction")
    @ResponseBody
    public JsonResult updateTypeRestriction(String typeRestriction) {
        log.info("修改同步的种类限制");
        return myReceiveService.updateTypeRestriction(typeRestriction);
    }

    /**
     * 修改获取文件的请求服务器IP
     * @param sendIP 文件发送端IP
     * @return
     */
    @RequestMapping("/updateSendIP")
    @ResponseBody
    public JsonResult updateSendIP(Boolean isWs , String sendIP) {
        return myReceiveService.updateSendIP(isWs,sendIP);
    }

    /**
     * 修改文件是否实时同步，会判断文件是否正在实时同步，是的话就停，不是就开
     * @param isWs 是否是文书
     * @return
     */
    @RequestMapping("/updateFileSynchronizationNow")
    @ResponseBody
    public JsonResult updateFileSynchronizationNow(Boolean isWs) {
        log.info("修改文件是否实时同步");
        return myReceiveService.updateFileSynchronizationNow(isWs);
    }

    /**
     * 开始文件同步（手动进行文件同步，开始时间和结束时间都为空时，进行Parameters.START_YEAR至今的所有数据同步，按年份进行一一同步）
     * @param isWs 是否是文书
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return
     */
    @RequestMapping("/startFileSynchronization")
    @ResponseBody
    public JsonResult startFileSynchronization(Boolean isWs, String startTime, String endTime) {
        log.info("开始文件同步（手动进行文件同步，开始时间和结束时间都为空时，进行"+Parameters.START_YEAR+"至今的所有数据同步，按年份进行由近及远）");
        return  myReceiveService.startFileSynchronization(isWs, startTime, endTime);
    }

    /**
     * 查找所有未找到的文件，有就进行同步
     */
    @RequestMapping("/findNoFile")
    public void findNoFile() {
        log.info("查找所有未找到的文件，有就进行同步");
        myReceiveService.findNoFile99();
    }

    /**
     * 获取配置信息
     * @return
     */
    @RequestMapping("/getInfo")
    @ResponseBody
    public Map<String, Object> getInfo() {
        return myReceiveService.getInfo();
    }


    /**
     * 获取配置信息
     * @return
     */
    @RequestMapping("/toDecrypt")
    @ResponseBody
    public String toDecrypt() {
        Parameters.canDecrypt = true;
        return "可以进行一次解密命令";
    }


    /**
     * 是否【能】进行实时同步
     * @param isMyCanSendNow
     */
    @RequestMapping("/isMyCanSendNow")
    @ResponseBody
    public JsonResult isMyCanSendNow(Boolean isMyCanSendNow) {
        log.warn("修改是否【能】进行实时同步状态"+isMyCanSendNow);
        return myReceiveService.isMyCanSendNow(isMyCanSendNow);
    }

    /**
     * 扫描已经下载的文件，并生成sql脚本文件
     * @param fileType w：文书 d:电子卷宗
     */
    @RequestMapping("/scanDownloadedFiles")
    @ResponseBody
    public JsonResult scanDownloadedFiles(String fileType) {
        return myReceiveService.scanDownloadedFiles(fileType);
    }

}
