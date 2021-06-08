package com.simplespring.core.context;

import com.simplespring.core.annotation.Component;
import com.simplespring.core.annotation.Controller;
import com.simplespring.core.annotation.Repository;
import com.simplespring.core.annotation.Service;
import com.simplespring.core.utils.ClassUtil;
import com.simplespring.core.utils.ValidationUtil;
import com.simplespring.exception.PackageScanningException;
import com.simplespring.exception.BeanRegistrationException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class BeanContainer {
    private boolean loaded = false;
    private final Map<Class<?>, Object> beanMap = new ConcurrentHashMap<>();
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

    public synchronized void loadBeans(String packageName){
        if(isLoaded()) {
            log.warn("BeanContainer has been loaded");
            return;
        }

        Set<Class<?>> classSet = null;
        try {
            classSet = ClassUtil.extractPackageClass(packageName);
        } catch (URISyntaxException | IOException e) {
            throw new PackageScanningException(e.getMessage());
        }

        if(ValidationUtil.NotNullOrEmpty(classSet)) {
            for(Class<?> clazz : classSet) {
                addBean(clazz);
            }
        }

        loaded =  true;
    }

    public void addBean(Class<?> clazz) {
        try{
            beanMap.put(clazz,ClassUtil.newInstance(clazz,true));
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new BeanRegistrationException(e.getMessage());
        }
    }
}
