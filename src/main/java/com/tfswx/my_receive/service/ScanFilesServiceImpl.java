package com.tfswx.my_receive.service;

import com.tfswx.my_receive.entity.MyFile;
import com.tfswx.my_receive.mapper.FileReceiveMapper;
import com.tfswx.my_receive.utils.DateUtil;
import com.tfswx.my_receive.utils.JsonResult;
import com.tfswx.my_receive.utils.Parameters;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.tfswx.my_receive.utils.Parameters.*;


/**
 * 数量太大，第一次扫描3层：单位编码\年份\案件类别
 */
@Service
@Transactional
public class ScanFilesServiceImpl implements ScanFilesService {
    private static Logger log = LoggerFactory.getLogger(ScanFilesServiceImpl.class);

    private static String RegxBmsh = "[^\\\\]+\\[\\d{4}\\]\\d+号";//部门受案号正则[\\u4e00-\\u9fa5]+\\[\\d{4}\\]\\d+号
    private String fileType;//类型，
    private String prefix;//前缀
    private String scanFolderPath;//需要进行文件扫描的文件夹路径
    private String saveSQLPath;//保存sql的文件夹路径

    private File wsSqlText;
    private File dzjzSqlText;
    private FileWriter writer;

    private int file_xh = 0;//w文件序号

    @Autowired
    FileReceiveMapper fileReceiveMapper;

    // 获取事务控制管理器
    @Autowired
    private DataSourceTransactionManager transactionManager;

    public void Init(String fileType, String folder, String saveSQLPath) {
        this.fileType = fileType;
        this.saveSQLPath = saveSQLPath;
        this.scanFolderPath = folder.replaceAll("\\\\\\\\", "");

        if (wsType.equals(this.fileType)) {
            prefix = "ws";
            if (!this.scanFolderPath.endsWith(File.separator)) {
                this.scanFolderPath = this.scanFolderPath + File.separator;
                log.info("文书路径前缀" + this.scanFolderPath);
            }
        }
        if (dzjzType.equals(this.fileType)) {
            prefix = "dzjz";
            if (this.scanFolderPath.endsWith(File.separator)) {
                //卷宗删除结尾的\
                this.scanFolderPath = this.scanFolderPath.substring(0, this.scanFolderPath.lastIndexOf(File.separator));
                log.info("电子卷宗路径前缀" + this.scanFolderPath);
            }
        }
    }

    /**
     * linkedList实现 F:\doc\\\\
     **/
    private LinkedList<File> queueFiles = new LinkedList<>();

    /**
     * 非递归方式扫描指定文件夹下面的所有文件
     *
     * @return ArrayList<Object>
     * @author
     * @time 2017年11月3日
     */
    public long scanFilesWithNoRecursion(String fileType, String folder, String saveSQLPath) throws Exception {
        Init(fileType, folder, saveSQLPath);
        long fileCount = 0;
        File directory = new File(scanFolderPath);
        if (!directory.isDirectory()) {
            throw new Exception('"' + scanFolderPath + '"' + " input path is not a Directory , please input the right path of the Directory. ^_^...^_^");
        } else {
            //1.扫描单位编码层
            File[] dwbmDirs = directory.listFiles();
            //遍历扫出的文件数组，如果是文件夹，将其放入到linkedList中稍后处理
            long folderCount = 0;
            if (dwbmDirs != null) {//隐藏文件夹或无访问权限会报files空指针错误
                for (File dwbm : dwbmDirs) {
                    if (dwbm.isDirectory()) {
                        //2.扫描年份编码层
                        File[] yearDirs = dwbm.listFiles();
                        if (yearDirs != null) {
                            for (File year : yearDirs) {
                                //3.扫描案件类别层
                                File[] ajlbDirs = year.listFiles();
                                if (ajlbDirs != null) {
                                    for (File ajlbDir : ajlbDirs) {
                                        queueFiles.add(ajlbDir);
                                        ++folderCount;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            log.warn(scanFolderPath + "路径下的三级件夹（文单位\\年份\\案件类别）数量 ：" + folderCount);
            if (folderCount == 0) return 0;

            int countForSQLline = 0;//每个文件最大记录50万条 500000
            int countForInsert = 0;//批量提交 每1000条，在配置文件pageSize
            //先删除先前的文件
            File file = new File(saveSQLPath);
            File[] tempFile = file.listFiles();
            for (File old : tempFile) {
                if (!old.isDirectory() && old.getName().startsWith(prefix + "_downloaded_")) {
                    old.delete();
                }
            }
            fileReceiveMapper.cleanDownloadTable();

            createFile(this.fileType);


            //如果linkedList非空遍历linkedList
            while (!queueFiles.isEmpty()) {
                //移出linkedList中的第一个
                File headDirectory = queueFiles.removeFirst();
                File[] currentFiles = headDirectory.listFiles();
                if (currentFiles != null) { //隐藏文件夹或无访问权限会报files空指针错误

                    DefaultTransactionDefinition def = new DefaultTransactionDefinition();//获取事务定义
                    def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);//设置事务隔离级别，开启新事务(原事务挂起)
                    TransactionStatus status = transactionManager.getTransaction(def); // 获得事务状态

                    for (int j = 0; j < currentFiles.length; j++) {
                        if (currentFiles[j].isDirectory()) {
                            //如果仍然是文件夹，将其放入linkedList中
                            queueFiles.add(currentFiles[j]);
                        } else {
                            if (countForSQLline >= SQL_MAX_LINES) {
                                writer.write("COMMIT;");
                                writer.flush();
                                IOUtils.closeQuietly(writer);
                                createFile(this.fileType);
                                countForSQLline = 0;
                            }

                            if (countForInsert >= PAGE_SIZE) {//分页批量commit
                                transactionManager.commit(status);//强制提交事务
                                countForInsert = 0;

                                def = new DefaultTransactionDefinition();//获取事务定义
                                def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);//设置事务隔离级别，开启新事务(原事务挂起)
                                status = transactionManager.getTransaction(def); // 获得事务状态
                            }

                            writeToTxt(currentFiles[j].getAbsolutePath());//写入SQL文件

                            ++fileCount;
                            ++countForSQLline;
                            ++countForInsert;
                        }
                    }
                    transactionManager.commit(status);//强制提交事务
                }
            }
            writer.write("COMMIT;");
            writer.flush();
            IOUtils.closeQuietly(writer);

        }
        return fileCount;
    }

    private void createFile(String type) {
        try {
            if (wsType.equals(type)) {
                //关闭先前的
                IOUtils.closeQuietly(writer);
                wsSqlText = new File(saveSQLPath + prefix + "_downloaded_" + DateUtil.getStr4DateShort(new Date()) + "_" + file_xh + ".sql");
                if (wsSqlText.exists()) { // 判断文件是否存在
                    wsSqlText.delete();// 如果存在先执行删除
                    wsSqlText.createNewFile();
                } else {
                    wsSqlText.createNewFile();// 如果不存在则直接创建
                }
                file_xh++;
                //打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
                writer = new FileWriter(wsSqlText, true);
            } else if (dzjzType.equals(type)) {
                //关闭先前的
                IOUtils.closeQuietly(writer);
                dzjzSqlText = new File(saveSQLPath + prefix + "_downloaded_" + DateUtil.getStr4DateShort(new Date()) + "_" + file_xh + ".sql");
                if (dzjzSqlText.exists()) { // 判断文件是否存在
                    dzjzSqlText.delete();// 如果存在先执行删除
                    dzjzSqlText.createNewFile();
                } else {
                    dzjzSqlText.createNewFile();// 如果不存在则直接创建
                }
                file_xh++;
                writer = new FileWriter(dzjzSqlText, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
    }

    public void writeToTxt(String absolutePath) throws IOException {
        String sql;
        MyFile myfile = new MyFile();
        //文书
        if (wsType.equals(this.fileType)) {
            String wjlj = absolutePath.replace(scanFolderPath, "");
            String bmsah = getBmsah(wjlj);
            sql = "INSERT INTO SJFH_FILE_DOWNLOAD (BMSAH,FILEPATH,FILETYPE) VALUES ('" + bmsah + "','" + wjlj + "','w');\n";
            writer.write(sql);
            writer.flush();
            //log.info(sql);
            myfile.setFilePath(wjlj);
            myfile.setBmsah(bmsah);
            myfile.setFileType(this.fileType);
        }
        //卷宗
        else if (dzjzType.equals(this.fileType)) {
            String wjlj = absolutePath.replace(scanFolderPath, "");
            String bmsah = getBmsah(wjlj);
            sql = "INSERT INTO SJFH_FILE_DOWNLOAD (BMSAH,FILEPATH,FILETYPE) VALUES ('" + bmsah + "','" + wjlj + "','d');\n";
            writer.write(sql);
            writer.flush();
            //log.info(sql);
            myfile.setFilePath(wjlj);
            myfile.setBmsah(bmsah);
            myfile.setFileType(this.fileType);
        }
        insertDownload(myfile);//写入表
    }

    public static String getBmsah(String text) {
        Pattern pattern = Pattern.compile(RegxBmsh);
        Matcher m = pattern.matcher(text);
        String str = null;
        if (m.find()) {
            str = m.group();
        }
        return str;
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
                count = scanFilesWithNoRecursion(fileType, Parameters.wsFileInfo.getFilePathTitle(), wsSaveSQLPath);
            } else if (dzjzType.equals(fileType)) {
                //存放已下载电子卷宗扫描sql文件
                count = scanFilesWithNoRecursion(fileType, Parameters.dzjzFileInfo.getFilePathTitle(), dzjzSaveSQLPath);
            }
            long times = (System.currentTimeMillis() - currentTimeMillis) / 60000;
            msg = msg + "已下载文件扫描完成，总数：" + count + " ，耗时：" + times + "分钟 ；sql文件保存在：" + wsSaveSQLPath;
            log.warn(msg);
            return new JsonResult(msg);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return new JsonResult(JsonResult.ERROR, "扫描失败" + e.getMessage());
        }
    }

    public void insertDownload(MyFile myfile) {
        fileReceiveMapper.insertDownload(myfile);
    }


}
