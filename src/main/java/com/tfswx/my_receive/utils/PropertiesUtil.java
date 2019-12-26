package com.tfswx.my_receive.utils;


import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.PropertiesConfigurationLayout;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.lang.StringUtils;

import java.io.File;

/**
 * Properties文件处理类
 */
public class PropertiesUtil {

    //文书信息管理类
    public static final String propertiesFilePath =Parameters.rootPath+ "SJFH2" + File.separator + "my_sjfh.properties";

    //默认编码格式，每次写入要设置，不然会造成写入乱码
    private static final String  DEFAULT_ENCODE = "UTF-8";

    private static PropertiesConfiguration propConfig;

    private static final PropertiesUtil PROPERTIES = new PropertiesUtil();

    /**
     *      * 自动保存
     *      
     */
    private static boolean autoSave = true;

    private PropertiesUtil() {
    }

    public static PropertiesUtil getInstance() {
        //执行初始化 
        init();
        return PROPERTIES;
    }

    /**
     *      * 初始化
     *      *
     *      * @param propertiesFile
     *      * @throws ConfigurationException 
     *      * @see
     *      
     */
    public static void init() {
        try {
            propConfig = new PropertiesConfiguration(propertiesFilePath);
            propConfig.setEncoding(DEFAULT_ENCODE);
            //自动重新加载 
            propConfig.setReloadingStrategy(new FileChangedReloadingStrategy());
            //自动保存 
            propConfig.setAutoSave(autoSave);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *      * 根据Key获得对应的value
     *      *
     *      * @param key
     *      * @return
     *      * @see
     *      
     */
    public Object getValue(String key) {
        return propConfig.getProperty(StringUtils.trim(key));
    }

    /**
     *      * 设置属性
     *      *
     *      * @param key
     *      * @param value
     *      * @see
     *      
     */
    public void setProperty(String key, String value) {
        propConfig.setEncoding(DEFAULT_ENCODE);
        propConfig.setProperty(key, value);
    }

    /**
     *      * 设置属性
     *      *
     *      * @param key
     *      * @param value
     *      * @see
     *      
     */
    public void setComment(String key, String comment) {
        propConfig.setEncoding(DEFAULT_ENCODE);
        PropertiesConfigurationLayout layout=propConfig.getLayout();
        layout.setComment(key,comment);
    }
   /**
     *      * 设置属性
     *      *
     *      * @param key
     *      * @param value
     *      * @see
     *      
     */
//    public String getComment(String key) {
//        propConfig.setEncoding("UTF-8");
//        PropertiesConfigurationLayout layout=propConfig.getLayout();
//        return layout.getComment(key);
//    }

}
