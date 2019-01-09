package com.starry.fastsky.config;

import com.starry.fastsky.util.LoggerBuilder;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashSet;

/**
 * ClassName: LoadClass
 * Description: 根据root class加载所有的class
 *
 * @author: starryfei
 * Date: 2019-01-09 22:13
 **/
public class LoadClass {
    private static Logger logger = LoggerBuilder.getLogger(LoadClass.class);

    public static void autoLoadClass(Class<?> rootClass) {
        HashSet<Class<?>> classes = new HashSet<>();
        if (rootClass.getPackage() != null) {
            String packAgeName = rootClass.getPackage().getName();
            String packAge = packAgeName.replace(".", "/");
            ApplicationConfig.getInstance().setPackageName(packAge);
            Enumeration<URL> dirs; //枚举元素
            try {
                // 加载包下面所有的资源
                dirs = Thread.currentThread().getContextClassLoader().getResources(packAge);
                while (dirs.hasMoreElements()) {
                    URL url = dirs.nextElement();
                    //获取资源字符串的协议
                    String protocol = url.getProtocol();
                    if ("file".equals(protocol)) {
                        String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                        loadClassByPackagePath(packAgeName, filePath, classes);
                    }
                }
            } catch (IOException e) {
                logger.error("error", e);
            }

        }
    }

    /**
     * 从根结点遍历所有子包的class，并且加载成class对象
     *
     * @param filePath
     * @param classs
     */
    private static void loadClassByPackagePath(String packAgeName, String filePath, HashSet<Class<?>> classs) {
        File file = new File(filePath);
        if (!file.exists() || !file.isDirectory()) {
            return;
        }
        File[] classFiles = file.listFiles(cla -> (cla.isDirectory()) || cla.getName().endsWith(".class"));
        for (File classFile : classFiles) {
            if (classFile.isDirectory()) {
                loadClassByPackagePath(packAgeName + "." + classFile.getName(),
                        classFile.getAbsolutePath(), classs);
            } else {
                String className = classFile.getName().substring(0,
                        classFile.getName().length() - 6);
                try {
                    // 类加载
                    classs.add(Thread.currentThread().getContextClassLoader().loadClass(String.format("%s.%s", packAgeName, className)));
                } catch (ClassNotFoundException e) {
                    logger.error("ClassNotFoundException", e);
                }
            }

        }
    }
}
