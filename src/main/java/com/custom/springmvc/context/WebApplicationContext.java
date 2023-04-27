package com.custom.springmvc.context;

import com.custom.springmvc.annotation.AutoWired;
import com.custom.springmvc.annotation.Controller;
import com.custom.springmvc.annotation.Service;
import com.custom.springmvc.xml.XmlPaser;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class WebApplicationContext {
    private String contextLocation;
    private List<String> classNameList = new ArrayList<>();
    private Map<String, Object> iocMap = new HashMap<>();

    public Map<String, Object> getIocMap() {
        return iocMap;
    }

    public WebApplicationContext(String contextLocation) {
        this.contextLocation = contextLocation;
    }

    public void onRefresh() {
        String res = XmlPaser.getbasePackage(this.contextLocation.split(":")[1]);
        String[] packages = res.split(",");
        for (String pack : packages) {
            executeScanPackageName(pack);
        }
        executeNewInstance();

        //4、进行 自动注入操作
        executeAutoWired();
    }

    private void executeAutoWired() {
        try {
            for (Entry<String, Object> entry : iocMap.entrySet()) {
                Object bean = entry.getValue();
                Field[] fields = bean.getClass().getDeclaredFields();
                for (Field field : fields) {
                    if(field.isAnnotationPresent(AutoWired.class)){
                        //获取注解中的value值|该值就是bean的name
                        AutoWired autoWiredAno =  field.getAnnotation(AutoWired.class);
                        String beanName = autoWiredAno.value();
                        //取消检查机制
                        field.setAccessible(true);
                        field.set(bean,iocMap.get(beanName));

                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void executeNewInstance() {

        try {
            for (String className : classNameList) {
                Class<?> clazz = Class.forName(className);
                if(clazz.isAnnotationPresent(Controller.class)) {
                    String beanName = clazz.getSimpleName().substring(0,1).toLowerCase()+ clazz.getSimpleName().substring(1);
                    iocMap.put(beanName,clazz.newInstance());
                } else if (clazz.isAnnotationPresent(Service.class)) {
                    Service serviceAn = clazz.getAnnotation(Service.class);
                    String beanName = serviceAn.value();
                    iocMap.put(beanName,clazz.newInstance());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void executeScanPackageName(String pack) {
        System.out.println(pack);
        URL url = this.getClass().getClassLoader().getResource(pack.replaceAll("\\.", "/"));
        String path = url.getFile();
        File file = new File(path);
        for(File f : file.listFiles()) {
            if (f.isDirectory()) {
                executeScanPackageName(pack + "." + f.getName());
            } else {
                String className = pack + "." + f.getName().replaceAll(".class", "");
                classNameList.add(className);
            }
        }
    }

    public static void main(String[] args) {
        WebApplicationContext  webApplicationContext = new WebApplicationContext("classpath:springmvc.xml");
        webApplicationContext.onRefresh();
    }
}
