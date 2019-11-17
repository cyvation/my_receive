package com.tfswx.my_receive.entity;

import java.util.Date;
import java.util.concurrent.ScheduledFuture;

/**
 * 文件配置类，所有文件相关配置在此类中进行保存并使用
 */
public class MyFileInfo {


    boolean isWs;

    String fileType;

    String logFilePath;

    String noFilePath;

    String filePathTitle;

    Date runDate;

    Boolean fileIsDecrypt;

    //手动同步执行状态
    Boolean fileSynchState;

    //实时监测新文件更新状态
    Boolean fileSynchronousSwitch;

    Integer year;

    Date startDate;

    Date endDate;

    Integer manualAynchNum;

    Integer fileNum;//

    Integer noFileNum;

    ScheduledFuture<?> future;

    public MyFileInfo() {
    }
    public MyFileInfo(Boolean isWs, String filePathTitle, String fileType) {
        this.isWs = isWs;
        this.filePathTitle = filePathTitle;
        this.fileIsDecrypt = false;
        this.fileSynchState = false;
        this.year = 0;
        this.manualAynchNum = 0;
        this.fileNum = 0;
        this.noFileNum = 0;
        this.fileSynchronousSwitch = true;
        this.fileType = fileType;
    }

    public String getFilePathTitle() {
        return filePathTitle;
    }

    public void setFilePathTitle(String filePathTitle) {
        this.filePathTitle = filePathTitle;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public Boolean getFileSynchState() {
        return fileSynchState;
    }

    public void setFileSynchState(Boolean fileSynchState) {
        this.fileSynchState = fileSynchState;
    }

    public Boolean getFileSynchronousSwitch() {
        return fileSynchronousSwitch;
    }

    public void setFileSynchronousSwitch(Boolean fileSynchronousSwitch) {
        this.fileSynchronousSwitch = fileSynchronousSwitch;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }


    public void setIsWs(boolean isWs) {
        this.isWs = isWs;
    }

    public Boolean getIsWs() {
        return isWs;
    }

    public Boolean getFileIsDecrypt() {
        return fileIsDecrypt;
    }

    public void setFileIsDecrypt(Boolean fileIsDecrypt) {
        this.fileIsDecrypt = fileIsDecrypt;
    }

    public Date getRunDate() {
        return runDate;
    }

    public void setRunDate(Date runDate) {
        this.runDate = runDate;
    }


    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public ScheduledFuture<?> getFuture() {
        return future;
    }

    public void setFuture(ScheduledFuture<?> future) {
        this.future = future;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Integer getFileNum() {
        return fileNum;
    }

    public void setFileNum(Integer fileNum) {
        this.fileNum = fileNum;
    }

    public String getLogFilePath() {
        return logFilePath;
    }

    public void setLogFilePath(String logFilePath) {
        this.logFilePath = logFilePath;
    }

    public String getNoFilePath() {
        return noFilePath;
    }

    public void setNoFilePath(String noFilePath) {
        this.noFilePath = noFilePath;
    }


    public Integer getNoFileNum() {
        return noFileNum;
    }

    public void setNoFileNum(Integer noFileNum) {
        this.noFileNum = noFileNum;
    }

    public Integer getManualAynchNum() {
        return manualAynchNum;
    }

    public void setManualAynchNum(Integer manualAynchNum) {
        this.manualAynchNum = manualAynchNum;
    }
}
