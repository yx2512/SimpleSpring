package com.simplespring.aop;

import com.simplespring.aop.annotation.*;
import com.simplespring.aop.aspect.AspectInfo;
import com.simplespring.core.context.BeanContainer;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

@Slf4j
public class AspectWeaver {
    private final BeanContainer beanContainer;
    private final Map<String, DefaultDynamicProxy> cache;
    private final Map<Class<? extends Annotation>, List<AspectInfo>> beforeMap;
    private final Map<Class<? extends Annotation>, List<AspectInfo>> afterReturningMap;
    private final Map<Class<? extends Annotation>, List<AspectInfo>> afterThrowingMap;


    public AspectWeaver() {
        cache = new HashMap<>();
        this.beanContainer = BeanContainer.getInstance();
        this.beforeMap = new HashMap<>();
        this.afterReturningMap = new HashMap<>();
        this.afterThrowingMap = new HashMap<>();
    }

    public void doAop() {
        Set<Map.Entry<String, Object>> aspectSet = beanContainer.getBeansByAnnotation(Aspect.class);


        if(aspectSet == null || aspectSet.isEmpty()) {
            return;
        }

        for(Map.Entry<String, Object> entry : aspectSet) {
            Class<?> clazz = entry.getValue().getClass();
            if(verifyAspect(clazz)) {
                categorizeAspect(clazz);
            } else {
                throw new RuntimeException();
            }
        }

        if(beforeMap.size() + afterReturningMap.size() + afterThrowingMap.size() == 0) {
            return;
        }

        Set<Class<? extends Annotation>> totalSet = new HashSet<>(beforeMap.keySet());
        totalSet.addAll(afterReturningMap.keySet());
        totalSet.addAll(afterThrowingMap.keySet());

        Set<Map.Entry<String, Object>> beanClasses = beanContainer.getBeans();

        for(Class<? extends Annotation> targetAnnotation : totalSet) {
            weaveByCategory(targetAnnotation, beanClasses);
        }

        for(String name : cache.keySet()) {
            Object proxy = ProxyCreator.createProxy(cache.get(name).getTargetClass(), cache.get(name));
            beanContainer.addBean(name, proxy);
        }
    }

    private void weaveByCategory(Class<? extends Annotation> annotation, Set<Map.Entry<String, Object>> entries) {


        if(entries == null || entries.isEmpty()) {
            log.warn("Nothing in beanMap");
            return;
        }

        for(Map.Entry<String, Object> entry : entries) {
            boolean hasInterface = false;
            Class<?> clazz = entry.getValue().getClass();
            if(clazz.getAnnotation(Aspect.class) != null) {
                continue;
            }

            if(clazz.getInterfaces().length != 0) {
                hasInterface = true;
            }

            Method[] methods = clazz.getMethods();
            for(Method method : methods) {
                if(method.isAnnotationPresent(annotation)) {
                    if(hasInterface) {
                        jdkProxyHandler(annotation, method, entry.getKey(), entry.getValue());
                    } else{
                        cglibProxyHandler(annotation, method, clazz, entry.getKey());
                    }
                }
            }
        }
    }

    private void jdkProxyHandler(Class<? extends Annotation> annotation, Method method, String name, Object object) {
        JDKDynamicProxy jdkDynamicProxy;
        if(!cache.containsKey(name)) {
            jdkDynamicProxy = new JDKDynamicProxy(object);
        } else {
            jdkDynamicProxy = (JDKDynamicProxy) cache.get(name);
        }

        jdkDynamicProxy.addToBeforeMap(method, beforeMap.get(annotation));
        jdkDynamicProxy.addToAfterReturningMap(method, afterReturningMap.get(annotation));
        jdkDynamicProxy.addToAfterThrowingMap(method, afterThrowingMap.get(annotation));
        cache.put(name, jdkDynamicProxy);
    }

    private void cglibProxyHandler(Class<? extends Annotation> annotation, Method method, Class<?> clazz, String name) {
        CGlibDynamicProxy cglibDynamicProxy;
        if(!cache.containsKey(name)) {
            cglibDynamicProxy = new CGlibDynamicProxy(clazz);
        } else {
            cglibDynamicProxy = (CGlibDynamicProxy) cache.get(name);
        }

        cglibDynamicProxy.addToBeforeMap(method, beforeMap.get(annotation));
        cglibDynamicProxy.addToAfterReturningMap(method, afterReturningMap.get(annotation));
        cglibDynamicProxy.addToAfterThrowingMap(method, afterThrowingMap.get(annotation));
        cache.put(name,cglibDynamicProxy);
    }

    private void categorizeAspect(Class<?> clazz) {
        Order orderTag = clazz.getAnnotation(Order.class);
        Method[] declaredMethods = clazz.getDeclaredMethods();
        for(Method method : declaredMethods) {
            Map<Class<? extends Annotation>, List<AspectInfo>> auxMap = null;
            Class <? extends Annotation> targetAnnotation = null;

            if(method.isAnnotationPresent(Before.class)) {
                auxMap = beforeMap;
                targetAnnotation = method.getAnnotation(Before.class).value();
            } else if (method.isAnnotationPresent(AfterReturning.class)) {
                auxMap = afterReturningMap;
                targetAnnotation = method.getAnnotation(AfterReturning.class).value();
            } else if (method.isAnnotationPresent(AfterThrowing.class)) {
                auxMap = afterThrowingMap;
                targetAnnotation = method.getAnnotation(AfterThrowing.class).value();
            }

            if(targetAnnotation == null) {
                throw new RuntimeException("No point cut found for aspect " + clazz.getSimpleName());
            }
            List<AspectInfo> aspectInfoList = auxMap.getOrDefault(targetAnnotation, new ArrayList<>());
            aspectInfoList.add(new AspectInfo(orderTag.value(),method, beanContainer.getBean(clazz.getSimpleName())));
            auxMap.put(targetAnnotation,aspectInfoList);
        }
    }

    private boolean verifyAspect(Class<?> clazz) {
        return clazz.isAnnotationPresent(Aspect.class) &&
                clazz.isAnnotationPresent(Order.class);
    }
}
