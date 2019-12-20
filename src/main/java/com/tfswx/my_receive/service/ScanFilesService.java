package com.tfswx.my_receive.service;

import com.tfswx.my_receive.entity.MyFile;
import com.tfswx.my_receive.utils.JsonResult;

public interface ScanFilesService {
    /**
     * 扫描已经下载的文件，并生成sql脚本文件
     * @param fileType
     */
    JsonResult scanDownloadedFiles(String fileType);

    void insertDownload(MyFile myfile);
}
