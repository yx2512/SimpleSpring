package com.aim.aop.proxy;

import com.aim.aop.aspect.AspectInfo;

import java.lang.reflect.Method;
import java.util.*;

public class DefaultDynamicProxy {
    private final boolean isJDK;
    private final Class<?> targetClass;
    private final Map<Method, Set<AspectInfo>> beforeAspectInfoMap;
    private final Map<Method, Set<AspectInfo>> afterReturningAspectInfoMap;
    private final Map<Method, Set<AspectInfo>> afterThrowingAspectInfoMap;

    public DefaultDynamicProxy(Class<?> clazz, boolean isJDK) {
        this.isJDK = isJDK;
        this.targetClass = clazz;
        this.beforeAspectInfoMap = new HashMap<>();
        this.afterReturningAspectInfoMap = new HashMap<>();
        this.afterThrowingAspectInfoMap = new HashMap<>();
    }

    public void invokeBeforeAdvice(Method method, Object [] args, Set<AspectInfo> aspectInfoSet) throws Throwable {
        for(AspectInfo item : aspectInfoSet) {
            item.getMethod().invoke(item.getAspectObj());
        }
    }

    public Object invokeAfterReturningAdvice(Method method, Object[] args, Object returnValue, Set<AspectInfo> aspectInfoSet) throws Throwable {
        Object result = returnValue;
        for(AspectInfo item : aspectInfoSet) {
            result = item.getMethod().invoke(item.getAspectObj(), result);
        }

        return result;
    }

    public void invokeAfterThrowingAdvice(Method method, Object[] args, Throwable e, Set<AspectInfo> aspectInfoSet) throws Throwable {
        for(AspectInfo item : aspectInfoSet) {
            item.getMethod().invoke(item.getAspectObj(),e);
        }
    }

    public void addToBeforeMap(Method key, List<AspectInfo> value) {
        if(key == null || value == null) {
            return;
        }
        Set<AspectInfo> aspectInfoList = this.beforeAspectInfoMap.getOrDefault(key, new TreeSet<>(Comparator.comparingInt(AspectInfo::getOrder)));
        aspectInfoList.addAll(value);
        this.beforeAspectInfoMap.put(key, aspectInfoList);
    }

    public void addToAfterReturningMap(Method key, List<AspectInfo> value) {
        if(key == null || value == null) {
            return;
        }

        Set<AspectInfo> aspectInfoList = this.afterReturningAspectInfoMap.getOrDefault(key, new TreeSet<>((a,b)->(-1*Integer.compare(a.getOrder(),b.getOrder()))));
        aspectInfoList.addAll(value);
        this.afterReturningAspectInfoMap.put(key, aspectInfoList);
    }

    public void addToAfterThrowingMap(Method key, List<AspectInfo> value) {
        if(key == null || value == null) {
            return;
        }
        Set<AspectInfo> aspectInfoList = this.afterThrowingAspectInfoMap.getOrDefault(key, new TreeSet<>((a,b)->(-1*Integer.compare(a.getOrder(),b.getOrder()))));
        aspectInfoList.addAll(value);
        this.afterThrowingAspectInfoMap.put(key, aspectInfoList);
    }

    public boolean beforeMapContains(Method method) {
        return beforeAspectInfoMap.containsKey(method);
    }

    public Set<AspectInfo> beforeMapGet(Method method) {
        return beforeAspectInfoMap.get(method);
    }

    public boolean afterReturningMapContains(Method method) {
        return afterReturningAspectInfoMap.containsKey(method);
    }

    public Set<AspectInfo> afterReturningMapGet(Method method) {
        return afterReturningAspectInfoMap.get(method);
    }

    public boolean afterThrowingMapContains(Method method) {
        return afterThrowingAspectInfoMap.containsKey(method);
    }

    public Set<AspectInfo> afterThrowingMapGet(Method method) {
        return afterThrowingAspectInfoMap.get(method);
    }

    public boolean isJDK() {return this.isJDK;}

    public Class<?> getTargetClass() {
        return targetClass;
    }
}
