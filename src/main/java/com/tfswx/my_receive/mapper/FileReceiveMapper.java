package com.tfswx.my_receive.mapper;


import com.tfswx.my_receive.entity.MyFile;
import com.tfswx.my_receive.entity.MyFileInfo;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface FileReceiveMapper {


    /**
     * 获取同步的【文书】
     * @param map
     * @return
     */
    List<MyFile> getWsFilePathes(Map<String,Object> map);
    int countWsFilePathes(Map<String,Object> map);

    /**
     * 获取同步的【电子卷宗】
     * @param map
     * @return
     */
    List<MyFile> getDzjzFilePathes(Map<String,Object> map);
    int countDzjzFilePathes(Map<String,Object> map);

    /**
     * 添加找不到的文件
     * @param myFile
     * @return
     */
    void insertNoFile(MyFile myFile);

    /**
     * 获取最新发现的文件数量
     * @param
     * @return
     */
    int getNewestNum(Map<String,Object> map);

    /**
     * 获取最新发现的文件
     * @param
     * @return
     */
    List<MyFile> getNewest(Map<String,Object> map);

    /**
     * 通过【ID】删除最新发现的文件
     * @param
     * @return
     */
    void deleteNewestById(String id);

    /**
     * 通过【ID】修改没有找到的文件信息
     * @param
     * @return
     */
    void updateNoFileById(MyFile myFile);

    /**
     * 通过【findTime】没找到的次数获取没有找到的文件数量，小于
     * @param
     * @return
     */
    int getNoFileNumByFindTime(Map<String,Object> map);

    /**
     * 通过【findTime】没找到的次数获取没有找到的文件，小于
     * @param map
     * @return
     */
    List<MyFile> getNoFileByFindTime(Map<String,Object> map);

    /**
     * 通过【ID】删除没找到的文件
     * @param id
     * @return
     */
    void deleteNoFileById(String id);
    /**
     * 通过【ID】删除没找到的文件
     * @param fileType
     * @return
     */
//    void deleteNewestByFileType(String fileType);


    /**
     * 找表
     * @param tableName
     * @return
     */
    Integer findTables(String tableName);
    /**
     * 找触发器
     * @param triggerName
     * @return
     */
    Integer findTrigger(String triggerName);


    /**
     * 创newfile表
     * @return
     */
    void createNewFileTable();

    /**
     * 创nofile表
     * @return
     */
    void createNoFileTable();


    /**
     * 创触发器
     * @return
     */
    void createDzjzTrigger(Map map);

    void createWsTrigger(Map map);

    void createDownloadTable();

    void insertDownload(MyFile myFile);

    void cleanDownloadTable();
}
