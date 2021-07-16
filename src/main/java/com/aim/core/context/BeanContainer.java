package com.aim.core.context;

import com.aim.aop.AspectWeaver;
import com.aim.core.annotation.*;
import com.aim.core.utils.ClassUtil;
import com.aim.ioc.DependencyInjector;
import com.aim.core.exception.PackageScanningException;
import com.aim.core.exception.BeanRegistrationException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class BeanContainer {
    private boolean loaded = false;
    private final Map<String, Object> beanMap = new ConcurrentHashMap<>();
    private static final List<Class<? extends Annotation>> BEAN_ANNOTATIONS =
            Arrays.asList(Component.class, Controller.class, Service.class, Repository.class);
    public static BeanContainer getInstance() {return ContainerHolder.HOLDER.instance;}

    private enum ContainerHolder {
        HOLDER;
        BeanContainer instance;
        ContainerHolder() {instance = new BeanContainer();}
    }

    public boolean isLoaded() {
        return this.loaded;
    }

    public int size() {
        return beanMap.size();
    }

    public synchronized void init(String packageName) {
        loadBeans(packageName);
        new AspectWeaver().doAop();
        new DependencyInjector().doIoC();
    }

    public synchronized void loadBeans(String packageName){
        if(isLoaded()) {
            log.warn("BeanContainer has been loaded");
            return;
        }

        Set<Class<?>> classSet;
        try {
            classSet = ClassUtil.extractPackageClass(packageName);
        } catch (URISyntaxException | IOException e) {
            throw new PackageScanningException(e.getMessage());
        }

        if(classSet!=null && !classSet.isEmpty()) {
            for(Class<?> clazz : classSet) {
                addBean(clazz);
            }
        }

        loaded =  true;
    }

    public void addBean(Class<?> clazz) {
        try{
            beanMap.put(clazz.getSimpleName(),ClassUtil.newInstance(clazz,true));
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new BeanRegistrationException(e.getMessage());
        }
    }

    public void addBean(String key, Object value) {
        beanMap.put(key,value);
    }

    public  Set<Map.Entry<String, Object>> getBeansByAnnotation(Class<? extends Annotation> annotation) {
        Set<Map.Entry<String, Object>> entrySet = beanMap.entrySet();
        if(entrySet.isEmpty()) {
            log.warn("Nothing in beanMap");
            return null;
        }

        Set<Map.Entry<String, Object>> resSet = new HashSet<>();
        for(Map.Entry<String, Object> entry : entrySet) {
            if(entry.getValue().getClass().isAnnotationPresent(annotation)) {
                resSet.add(entry);
            }
        }

        return resSet.isEmpty() ? null : resSet;
    }

    public Set<String> getClassBySuperClass(Class<?> clazz) {
        Set<String> superSet = new HashSet<>();
        for(Map.Entry<String, Object> entry : beanMap.entrySet()) {
            Class<?> beanClazz = entry.getValue().getClass();
            if(clazz.isAssignableFrom(beanClazz) && !clazz.equals(beanClazz)) {
                superSet.add(entry.getKey());
            }
        }
        return superSet.size() == 0 ? null : superSet;
    }

    public Object getBean(String key) {
        return beanMap.get(key);
    }

    public Set<Map.Entry<String, Object>> getBeans() {
        return beanMap.entrySet();
    }
}
