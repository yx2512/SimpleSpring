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
    private final Map<Class<?>, DefaultDynamicProxy> cache;
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
        Set<Class<?>> aspectSet = beanContainer.getClassesByAnnotation(Aspect.class);


        if(aspectSet == null || aspectSet.isEmpty()) {
            return;
        }

        for(Class<?> clazz : aspectSet) {
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

        Set<Class<?>> beanClasses = beanContainer.getBeanClasses();

        for(Class<? extends Annotation> targetAnnotation : totalSet) {
            weaveByCategory(targetAnnotation, beanClasses);
        }

        for(Class <?> clazz : cache.keySet()) {
            Object proxy = ProxyCreator.createProxy(clazz, cache.get(clazz));
            beanContainer.addBean(clazz, proxy);
        }
    }

    private void weaveByCategory(Class<? extends Annotation> annotation, Set<Class<?>> classes) {


        if(classes == null || classes.isEmpty()) {
            log.warn("Nothing in beanMap");
            return;
        }

        for(Class<?> clazz : classes) {
            boolean hasInterface = false;
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
                        jdkProxyHandler(annotation, method, clazz, beanContainer.getBean(clazz));
                    } else{
                        cglibProxyHandler(annotation, method, clazz);
                    }
                }
            }
        }
    }

    private void jdkProxyHandler(Class<? extends Annotation> annotation, Method method, Class<?> clazz, Object object) {
        JDKDynamicProxy jdkDynamicProxy;
        if(!cache.containsKey(clazz)) {
            jdkDynamicProxy = new JDKDynamicProxy(object);
        } else {
            jdkDynamicProxy = (JDKDynamicProxy) cache.get(clazz);
        }

        jdkDynamicProxy.addToBeforeMap(method, beforeMap.get(annotation));
        jdkDynamicProxy.addToAfterReturningMap(method, afterReturningMap.get(annotation));
        jdkDynamicProxy.addToAfterThrowingMap(method, afterThrowingMap.get(annotation));
        cache.put(clazz, jdkDynamicProxy);
    }

    private void cglibProxyHandler(Class<? extends Annotation> annotation, Method method, Class<?> clazz) {
        CGlibDynamicProxy cglibDynamicProxy;
        if(!cache.containsKey(clazz)) {
            cglibDynamicProxy = new CGlibDynamicProxy(clazz);
        } else {
            cglibDynamicProxy = (CGlibDynamicProxy) cache.get(clazz);
        }

        cglibDynamicProxy.addToBeforeMap(method, beforeMap.get(annotation));
        cglibDynamicProxy.addToAfterReturningMap(method, afterReturningMap.get(annotation));
        cglibDynamicProxy.addToAfterThrowingMap(method, afterThrowingMap.get(annotation));
        cache.put(clazz,cglibDynamicProxy);
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
            aspectInfoList.add(new AspectInfo(orderTag.value(),method, beanContainer.getBean(clazz)));
            auxMap.put(targetAnnotation,aspectInfoList);
        }
    }

    private boolean verifyAspect(Class<?> clazz) {
        return clazz.isAnnotationPresent(Aspect.class) &&
                clazz.isAnnotationPresent(Order.class);
    }
}
