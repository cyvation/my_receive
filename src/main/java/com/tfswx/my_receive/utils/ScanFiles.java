package com.tfswx.my_receive.utils;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class ScanFiles {
    private static Logger log = LoggerFactory.getLogger(ScanFiles.class);
    private static String RegxBmsh = "[^\\\\]+\\[\\d{4}\\]\\d+号";//部门受案号正则[\\u4e00-\\u9fa5]+\\[\\d{4}\\]\\d+号
    private String fileType;//类型，
    private String prefix;//前缀
    private String scanFolderPath;//需要进行文件扫描的文件夹路径
    private String saveSQLPath;//保存sql的文件夹路径

    private File wsSqlText;
    private File dzjzSqlText;
    private FileWriter writer;

    private int file_xh = 0;//w文件序号

    public ScanFiles(String fileType, String folderPrefix, String saveSQLPath) {
        this.fileType = fileType;
        this.saveSQLPath=saveSQLPath;
        this.scanFolderPath = folderPrefix.replaceAll("\\\\\\\\", "");

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

    public static void main(String[] args) throws Exception {
        long currentTimeMillis = System.currentTimeMillis();
        long wsCount = new ScanFiles(wsType, "E:\\doc\\", "E:\\").scanFilesWithNoRecursion();
        log.warn("文书下载总数：" + wsCount);
        long jzCount = new ScanFiles(dzjzType, "F:\\JdNewData", "E:\\").scanFilesWithNoRecursion();
        log.warn("卷宗下载总数：" + jzCount);
        long currentTimeMillis2 = System.currentTimeMillis();
        log.info("耗时(分)：" + (currentTimeMillis2 - currentTimeMillis) / 60000);
    }

    /**
     * 非递归方式扫描指定文件夹下面的所有文件
     *
     * @return ArrayList<Object>
     * @author
     * @time 2017年11月3日
     */
    public long scanFilesWithNoRecursion() throws Exception {
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

            int pageSize = 0;//每个文件最大记录50万条 500000
            //先删除先前的文件
            File file = new File(saveSQLPath);
            File[] tempFile = file.listFiles();
            for (File old : tempFile) {
                if (!old.isDirectory() && old.getName().startsWith(prefix + "_downloaded_")) {
                    old.delete();
                }
            }
            createFile(this.fileType);

            //如果linkedList非空遍历linkedList
            while (!queueFiles.isEmpty()) {
                //移出linkedList中的第一个
                File headDirectory = queueFiles.removeFirst();
                File[] currentFiles = headDirectory.listFiles();
                if (currentFiles != null) { //隐藏文件夹或无访问权限会报files空指针错误
                    for (int j = 0; j < currentFiles.length; j++) {
                        if (currentFiles[j].isDirectory()) {
                            //如果仍然是文件夹，将其放入linkedList中
                            queueFiles.add(currentFiles[j]);
                        } else {
                            if (pageSize >= SQL_MAX_LINES) {
                                writer.write("COMMIT;");
                                writer.flush();
                                IOUtils.closeQuietly(writer);
                                createFile(this.fileType);
                                pageSize = 0;
                            }
                            writeToTxt(currentFiles[j].getAbsolutePath());
                            ++fileCount;
                            ++pageSize;
                        }
                    }
                }
            }
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

    private void writeToTxt(String absolutePath) throws IOException {
        String sql;
        //文书
        if (wsType.equals(this.fileType)) {
            String wjlj = absolutePath.replace(scanFolderPath, "");
            String bmsah = getBmsah(wjlj);
            sql = "INSERT INTO SJFH_FILE_DOWNLOAD (BMSAH,FILEPATH,FILETYPE) VALUES ('" + bmsah + "','" + wjlj + "','w');\n";
            writer.write(sql);
            writer.flush();
            //log.info(sql);
        }
        //卷宗
        else if (dzjzType.equals(this.fileType)) {
            String wjlj = absolutePath.replace(scanFolderPath, "");
            String bmsah = getBmsah(wjlj);
            sql = "INSERT INTO SJFH_FILE_DOWNLOAD (BMSAH,FILEPATH,FILETYPE) VALUES ('" + bmsah + "','" + wjlj + "','d');\n";
            writer.write(sql);
            writer.flush();
            //log.info(sql);
        }

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

}
