package com.tfswx.my_receive.entity;

import com.tfswx.my_receive.utils.DataEncryption;
import com.tfswx.my_receive.utils.FileUtil;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import sun.nio.ch.IOUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 文件写入类，将文件byte[]、地址、是否解密传入，进行文件写入
 */
public class WriteFile implements Runnable {

    byte[] fileBytes;
    String filePath;
    Boolean IsDecrypt;

    public WriteFile(Boolean IsDecrypt, String filePath, byte[] fileBytes) {
        this.IsDecrypt = IsDecrypt;
        this.fileBytes = fileBytes;
        this.filePath = filePath;
    }


    @Override
    public void run() {
        if(fileBytes==null){
            return;
        }
        if (IsDecrypt) {
            if (isEncryFile(filePath)) {
                writeDecryptFile(fileBytes, filePath);
            } else {
                writeFile(fileBytes, filePath);
            }
        } else {
            writeFile(fileBytes, filePath);
        }
    }

    public boolean isEncryFile(String filePath) {
        return filePath.lastIndexOf(".encry") > -1;
    }

    public String getFileSavePath(String filePath) {
        return filePath.substring(0, filePath.length() - 6);
    }

    private void writeDecryptFile(byte[] fileBytes, String filePath) {
        writeFile(DataEncryption.ByteXor(fileBytes), getFileSavePath(filePath));
    }

    public void writeFile(byte[] fileBytes, String filePath) {
        FileOutputStream fos = null;
        File file = FileUtil.createFile(filePath);
        try {
            fos = new FileOutputStream(file);
            fos.write(fileBytes);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(fos);
        }
    }


}
