package com.tfswx.my_receive.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 文件数据类，通过数据库进行搜索，然后将信息存在此类中进行使用
 */
public class MyFile {
    String id;

    String filePath;

    String dwbm;

    Date cjsj;

    String fileType;

    String type;

    Date zhxgsj;

    int findTime;


    public int getFindTime() {
        return findTime;
    }

    public void setFindTime(int findTime) {
        this.findTime = findTime;
    }


    public String getDwbm() {
        return dwbm;
    }

    public void setDwbm(String dwbm) {
        this.dwbm = dwbm;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Date getCjsj() {
        return cjsj;
    }

    public void setCjsj(Date cjsj) {
        this.cjsj = cjsj;
    }

    public Date getZhxgsj() {
        return zhxgsj;
    }

    public void setZhxgsj(Date zhxgsj) {
        this.zhxgsj = zhxgsj;
    }
}
