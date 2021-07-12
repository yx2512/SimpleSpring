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
    private final Map<Class<?>, AspectListExecutor> cache;

    public AspectWeaver() {
        cache = new HashMap<>();
        this.beanContainer = BeanContainer.getInstance();
    }

    public void doAop() {
        Set<Class<?>> aspectSet = beanContainer.getClassesByAnnotation(Aspect.class);
        Map<Class<? extends Annotation>, List<AspectInfo>> beforeMap = new HashMap<>();
        Map<Class<? extends Annotation>, List<AspectInfo>> afterReturningMap = new HashMap<>();
        Map<Class<? extends Annotation>, List<AspectInfo>> afterThrowingMap = new HashMap<>();

        if(aspectSet == null || aspectSet.isEmpty()) {
            return;
        }

        for(Class<?> clazz : aspectSet) {
            if(verifyAspect(clazz)) {
                categorizeAspect(beforeMap, afterReturningMap, afterThrowingMap, clazz);
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
            weaveByCategory(targetAnnotation, beforeMap,afterReturningMap,afterThrowingMap,beanClasses);
        }

        for(Class <?> clazz : cache.keySet()) {
            Object proxy = ProxyCreator.createProxy(clazz, cache.get(clazz));
            beanContainer.addBean(clazz, proxy);
        }
    }

    private void weaveByCategory(Class<? extends Annotation> annotation,
                                 Map<Class<? extends Annotation>, List<AspectInfo>> beforeMap,
                                 Map<Class<? extends Annotation>, List<AspectInfo>> afterReturningMap,
                                 Map<Class<? extends Annotation>, List<AspectInfo>> afterThrowingMap,
                                 Set<Class<?>> classes) {


        if(classes == null || classes.isEmpty()) {
            log.warn("Nothing in beanMap");
            return;
        }

        for(Class<?> clazz : classes) {
            Method[] methods = clazz.getDeclaredMethods();
            for(Method method : methods) {
                if(method.isAnnotationPresent(annotation)) {
                    if(clazz.getDeclaredAnnotation(Aspect.class) != null) {
                        throw new RuntimeException("Point cut cannot be applied to aspect");
                    }
                    AspectListExecutor listExecutor = null;
                    if(!cache.containsKey(clazz)) {
                        listExecutor = new AspectListExecutor(clazz);
                    } else {
                        listExecutor = cache.get(clazz);
                    }

                    listExecutor.addToBeforeMap(method, beforeMap.get(annotation));
                    listExecutor.addToAfterReturningMap(method, afterReturningMap.get(annotation));
                    listExecutor.addToAfterThrowingMap(method, afterThrowingMap.get(annotation));
                    cache.put(clazz,listExecutor);
                }
            }
        }
    }

    private void categorizeAspect(Map<Class<? extends Annotation>, List<AspectInfo>> beforeMap,
                                  Map<Class<? extends Annotation>, List<AspectInfo>> afterReturningMap,
                                  Map<Class<? extends Annotation>, List<AspectInfo>> afterThrowingMap,
                                  Class<?> clazz) {
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

//    private void weaveByCategory(Class<? extends Annotation> category, List<AspectInfo> aspectInfoList) {
//        Set<Class<?>> classSet = beanContainer.getClassesByAnnotation(category);
//
//        if(classSet == null || classSet.isEmpty()) {
//            return;
//        }
//
//        for(Class<?> targetClass : classSet) {
//            AspectListExecutor aspectListExecutor = new AspectListExecutor(targetClass, aspectInfoList);
//            Object proxy = ProxyCreator.createProxy(targetClass, aspectListExecutor);
//            beanContainer.addBean(targetClass, proxy);
//        }
//    }

//    private void categorizeAspect(Map<Class<? extends Annotation>, List<AspectInfo>> categorizedMap, Class<DefaultAspect> clazz) {
//        Order orderTag = clazz.getAnnotation(Order.class);
//        Aspect aspectTag = clazz.getAnnotation(Aspect.class);
//
//        DefaultAspect bean = beanContainer.getBean(clazz);
//
//        AspectInfo aspectInfo = new AspectInfo(orderTag.value(), bean);
//        List<AspectInfo> aspectInfoList = categorizedMap.getOrDefault(aspectTag.value(), new ArrayList<>());
//        aspectInfoList.add(aspectInfo);
//    }

    private boolean verifyAspect(Class<?> clazz) {
        return clazz.isAnnotationPresent(Aspect.class) &&
                clazz.isAnnotationPresent(Order.class);
    }
}
