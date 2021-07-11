package com.simplespring.core.context;

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

    public synchronized void init(String packageName) {
        loadBeans(packageName);

        doIoC();
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

    public void addBean(Class<?> clazz, Object obj) {
        beanMap.put(clazz,obj);
    }

    private void doIoC() {
        if(this.size() == 0) {
            log.warn("Empty container");
            return;
        }

        Set<Class<?>> classSet = beanMap.keySet();
        for(Class<?> clazz : classSet) {
            Method[] declaredMethods = clazz.getDeclaredMethods();
            for(Method method : declaredMethods) {
                if(method.isAnnotationPresent(Autowired.class)) {
                    if(method.getParameterCount() != 1) {
                        throw new DependencyInjectionException("Wrong number of arguments");
                    } else {
                        Autowired autowiredAnnotation = method.getAnnotation(Autowired.class);
                        String alias = autowiredAnnotation.value();
                        Class<?> parameterType = method.getParameterTypes()[0];
                        Object parameterInstance = getParameterInstance(parameterType, alias);

                        Object bean = beanMap.get(clazz);
                        method.setAccessible(true);

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

    private Object getParameterInstance(Class<?> clazz, String alias) {
        Object bean = beanMap.get(clazz);
        if(bean != null) {
            return bean;
        } else {
            Class<?> candidateClass = getCandidateClass(clazz, alias);
            return beanMap.get(candidateClass);
        }
    }

    private Class<?> getCandidateClass(Class<?> clazz, String alias) {
        Set<Class<?>> superSet = getClassBySuperClass(clazz);
        if(!ValidationUtil.NotNullOrEmpty(superSet)) {
            throw new DependencyInjectionException("No bean of type " + clazz.toString());
        }

        if(alias.isEmpty()) {
            if(superSet.size() == 1) return superSet.iterator().next();
            else {
                throw new DependencyInjectionException("Too many options for bean of type " + clazz.toString());
            }
        } else {
            for(Class<?> candi : superSet) {
                if(candi.getSimpleName().equals(alias)) {
                    return candi;
                }
            }
            throw new DependencyInjectionException("No bean of type " + clazz.toString());
        }
    }

    public  Set<Class<?>> getClassesByAnnotation(Class<? extends Annotation> annotation) {
        Set<Class<?>> classes = beanMap.keySet();
        if(classes.isEmpty()) {
            log.warn("Nothing in beanMap");
            return null;
        }

        Set<Class<?>> resSet = new HashSet<>();
        for(Class<?> clazz : classes) {
            if(clazz.isAnnotationPresent(annotation)) {
                resSet.add(clazz);
            }
        }

        return resSet.isEmpty() ? null : resSet;
    }

    private Set<Class<?>> getClassBySuperClass(Class<?> clazz) {
        Set<Class<?>> superSet = new HashSet<>();
        for(Class<?> item : beanMap.keySet()) {
            if(clazz.isAssignableFrom(item) && !clazz.equals(item)) {
                superSet.add(item);
            }
        }
        return superSet.size() == 0 ? null : superSet;
    }

    public <T> T getBean(Class<T> clazz) {
        return (T) beanMap.get(clazz);
    }
}
