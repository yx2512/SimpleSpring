package com.simplespring.aop;

import com.simplespring.aop.annotation.Aspect;
import com.simplespring.aop.annotation.Order;
import com.simplespring.aop.aspect.AspectInfo;
import com.simplespring.aop.aspect.DefaultAspect;
import com.simplespring.core.context.BeanContainer;

import java.lang.annotation.Annotation;
import java.util.*;

public class AspectWeaver {
    private BeanContainer beanContainer;

    public AspectWeaver() {
        this.beanContainer = BeanContainer.getInstance();
    }

    public void doAop() {
        Set<Class<?>> aspectSet = beanContainer.getClassesByAnnotation(Aspect.class);
        Map<Class<? extends Annotation>, List<AspectInfo>> categorizedMap = new HashMap<>();
        if(aspectSet == null || aspectSet.isEmpty()) {
            return;
        }

        for(Class<?> clazz : aspectSet) {
            if(verifyAspect(clazz)) {
                categorizeAspect(categorizedMap, (Class<DefaultAspect>) clazz);
            } else {
                throw new RuntimeException();
            }
        }

        if(categorizedMap.size() == 0) {
            return;
        }

        for(Class<? extends Annotation> category : categorizedMap.keySet()) {
            weaveByCategory(category, categorizedMap.get(category));
        }
    }

    private void weaveByCategory(Class<? extends Annotation> category, List<AspectInfo> aspectInfoList) {
        Set<Class<?>> classSet = beanContainer.getClassesByAnnotation(category);

        if(classSet == null || classSet.isEmpty()) {
            return;
        }

        for(Class<?> targetClass : classSet) {
            AspectListExecutor aspectListExecutor = new AspectListExecutor(targetClass, aspectInfoList);
            Object proxy = ProxyCreator.createProxy(targetClass, aspectListExecutor);
            beanContainer.addBean(targetClass, proxy);
        }
    }

    private void categorizeAspect(Map<Class<? extends Annotation>, List<AspectInfo>> categorizedMap, Class<DefaultAspect> clazz) {
        Order orderTag = clazz.getAnnotation(Order.class);
        Aspect aspectTag = clazz.getAnnotation(Aspect.class);

        DefaultAspect bean = beanContainer.getBean(clazz);

        AspectInfo aspectInfo = new AspectInfo(orderTag.value(), bean);
        List<AspectInfo> aspectInfoList = categorizedMap.getOrDefault(aspectTag.value(), new ArrayList<>());
        aspectInfoList.add(aspectInfo);
    }

    private boolean verifyAspect(Class<?> clazz) {
        return clazz.isAnnotationPresent(Aspect.class) &&
                clazz.isAnnotationPresent(Order.class) &&
                DefaultAspect.class.isAssignableFrom(clazz) &&
                clazz.getAnnotation(Aspect.class).value() != Aspect.class;
    }
}
