package com.simplespring.core.context;

import com.simplespring.aop.AspectWeaver;
import com.simplespring.core.annotation.*;
import com.simplespring.core.utils.ClassUtil;
import com.simplespring.core.utils.ValidationUtil;
import com.simplespring.exception.DependencyInjectionException;
import com.simplespring.exception.PackageScanningException;
import com.simplespring.exception.BeanRegistrationException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
        doIoC();
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

        if(ValidationUtil.NotNullOrEmpty(classSet)) {
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

    private void doIoC() {
        if(this.size() == 0) {
            log.warn("Empty container");
            return;
        }

        Set<String> nameSet = beanMap.keySet();
        for(String beanName : nameSet) {
            Class<?> clazz = beanMap.get(beanName).getClass();
            Method[] declaredMethods = clazz.getDeclaredMethods();
            for(Method method : declaredMethods) {
                if(method.isAnnotationPresent(Autowired.class)) {
                    if(method.getParameterCount() != 1) {
                        throw new DependencyInjectionException("Wrong number of arguments");
                    } else {
                        Autowired autowiredAnnotation = method.getAnnotation(Autowired.class);
                        String alias = autowiredAnnotation.value();
                        Class<?> parameterType = method.getParameterTypes()[0];
                        Object parameterInstance = getParameterInstance(parameterType.getSimpleName(), parameterType, alias);

                        Object bean = beanMap.get(beanName);
                        method.setAccessible(true);

                        if(parameterInstance.getClass().getSimpleName().equals(beanName)) {
                            throw new DependencyInjectionException("Circular dependency found in bean with class: " + clazz);
                        }
                        try {
                            method.invoke(bean,parameterInstance);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new DependencyInjectionException(e.getMessage());
                        }
                    }
                }
            }
        }
    }

    private Object getParameterInstance(String name, Class<?> clazz, String alias) {
        Object bean = beanMap.get(name);
        if(bean != null) {
            return bean;
        } else {
            String candidateBean = getCandidateClass(clazz, alias);
            return beanMap.get(candidateBean);
        }
    }

    private String getCandidateClass(Class<?> clazz, String alias) {
        Set<String> superSet = getClassBySuperClass(clazz);
        if(!ValidationUtil.NotNullOrEmpty(superSet)) {
            throw new DependencyInjectionException("No bean of type " + clazz.toString());
        }

        if(alias.isEmpty()) {
            if(superSet.size() == 1) return superSet.iterator().next();
            else {
                throw new DependencyInjectionException("Too many options for bean of type " + clazz.toString());
            }
        } else {
            for(String candi : superSet) {
                if(candi.equals(alias)) {
                    return candi;
                }
            }
            throw new DependencyInjectionException("Cannot find bean with alias " + alias);
        }
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

    private Set<String> getClassBySuperClass(Class<?> clazz) {
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
