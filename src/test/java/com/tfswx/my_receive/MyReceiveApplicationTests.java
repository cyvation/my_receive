package com.tfswx.my_receive;

import com.tfswx.my_receive.entity.MyFile;
import com.tfswx.my_receive.entity.MyFileInfo;
import com.tfswx.my_receive.mapper.FileReceiveMapper;
import com.tfswx.my_receive.utils.DateUtil;
import com.tfswx.my_receive.utils.FileUtil;
import com.tfswx.my_receive.utils.Parameters;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MyReceiveApplicationTests {

    @Autowired
    FileReceiveMapper mapper;

    /**
     * 扫描本地已下载的电子卷宗
     */
    public void scanLocalDzjzFiles(){

    }

    @Test //创建文书测试文件
    public void createWsFiles() {
        System.out.println("test文书源端IP：" + Parameters.sendIPMap.get("w"));
        Map<String, Object> map = new HashMap<>();

        MyFileInfo wsFileInfo = Parameters.wsFileInfo;
        map.put("typeRestriction", Parameters.typeRestriction);
//        map.put("typeRestriction", "0301,0201");
//        map.put("fdwbm", getDwbmSql("f.dwbm","500101,500102,500103"));
//        map.put("ajdwbm", getDwbmSql("aj.cbdw_bm","500101-500103"));
        map.put("startDate", DateUtil.getDate4Str("2015-03-26"));
        map.put("endDate", DateUtil.getDate4Str("2016-01-05"));
        List<MyFile> fileList = mapper.getWsFilePathes(map);
        System.out.println("文书数量：" + fileList.size());

        int i = 0;
        for (MyFile myFile : fileList) {
            String fileBytes = getRandombyte("文书测试内容" + myFile.getFilePath(), 21);
            createFile("F:\\doc\\" + myFile.getFilePath(), fileBytes);
            i++;
            System.out.println(i + "   F:\\doc\\" + myFile.getFilePath() + "  大小(K):" + (fileBytes.length() / 1024));
        }
    }

    @Test //创建卷宗测试文件 67639
    public void createDzjzFiles() {
        System.out.println("test卷宗源端IP：" + Parameters.sendIPMap.get("d"));
        Map<String, Object> map = new HashMap<>();

        MyFileInfo dzjzFileInfo = Parameters.dzjzFileInfo;
        map.put("typeRestriction", Parameters.typeRestriction);
//        map.put("typeRestriction", "0301,0201");
//        map.put("fdwbm", getDwbmSql("f.dwbm","500000,500102,501103"));
//        map.put("ajdwbm", getDwbmSql("aj.cbdw_bm","500000-511103"));
        map.put("startDate", DateUtil.getDate4Str("2013-12-24"));
        map.put("endDate", DateUtil.getDate4Str("2019-08-11"));
        List<MyFile> fileList = mapper.getDzjzFilePathes(map);
        System.out.println("卷宗数量：" + fileList.size());

        int i = 0;
        for (MyFile myFile : fileList) {
            String fileBytes = getRandombyte("卷宗测试内容" + myFile.getFilePath(), 31);
            createFile("F:\\JdNewData\\" + myFile.getFilePath(), fileBytes);
            i++;
            System.out.println(i + "   F:\\JdNewData\\" + myFile.getFilePath() + "  大小(K):" + (fileBytes.length() / 1024));
        }
    }

    /**
     * 文件写入本地
     *
     * @param filePath
     * @param fileBytes
     */
    private void createFile(String filePath, String fileBytes) {
        FileOutputStream outSTr = null;
        BufferedOutputStream buff = null;
        File file = FileUtil.createFile(filePath);
        try {
            outSTr = new FileOutputStream(file);
            buff = new BufferedOutputStream(outSTr);
            buff.write(fileBytes.getBytes());
            buff.flush();
            buff.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(buff);
            IOUtils.closeQuietly(outSTr);
        }
    }

    /**
     * 生成文件测试内容
     *
     * @param t
     * @return
     */
    private String getRandombyte(String t, int size) {
        Random random = new Random();
        String s = RandomStringUtils.randomAlphanumeric(random.nextInt(100) * size);
        return (t + s);
    }


    /**
     * @param fied 别名
     * @param dwbm
     * @return
     */
    private String getDwbmSql(String fied, String dwbm) {
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

    //    @Test
    public void contextLoads() {
        System.out.println("testing...");
    }
}
