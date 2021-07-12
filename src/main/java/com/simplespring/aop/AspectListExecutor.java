package com.simplespring.aop;

import com.simplespring.aop.aspect.AspectInfo;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.*;

public class AspectListExecutor implements MethodInterceptor {

    private final Class<?> targetClass;
    private final Map<Method, Set<AspectInfo>> beforeAspectInfoMap;
    private final Map<Method, Set<AspectInfo>> afterReturningAspectInfoMap;
    private final Map<Method, Set<AspectInfo>> afterThrowingAspectInfoMap;

    public AspectListExecutor(Class<?> targetClass) {
        this.targetClass = targetClass;
        this.beforeAspectInfoMap = new HashMap<>();
        this.afterReturningAspectInfoMap = new HashMap<>();
        this.afterThrowingAspectInfoMap = new HashMap<>();
    }

    @Override
    public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        Method originalMethod = targetClass.getDeclaredMethod(method.getName(), method.getParameterTypes());

        if(beforeAspectInfoMap.containsKey(originalMethod)) {
            invokeBeforeAdvice(method, args, beforeAspectInfoMap.get(originalMethod));
        }

        try {
            Object returnValue = null;

            returnValue = methodProxy.invokeSuper(proxy, args);

            if(afterReturningAspectInfoMap.containsKey(originalMethod)) {
                returnValue = invokeAfterReturningAdvice(method, args, returnValue, afterReturningAspectInfoMap.get(originalMethod));
            }

            return returnValue;
        } catch (Exception e) {
            if(afterThrowingAspectInfoMap.containsKey(originalMethod)) {
                invokeAfterThrowingAdvice(method, args, e, afterThrowingAspectInfoMap.get(originalMethod));
            }
        }

        return null;
    }

    private void invokeBeforeAdvice(Method method, Object [] args, Set<AspectInfo> aspectInfoSet) throws Throwable {
        for(AspectInfo item : aspectInfoSet) {
            item.getMethod().invoke(item.getAspectObj());
        }
    }

    private Object invokeAfterReturningAdvice(Method method, Object[] args, Object returnValue, Set<AspectInfo> aspectInfoSet) throws Throwable {
        Object result = returnValue;
        for(AspectInfo item : aspectInfoSet) {
            result = item.getMethod().invoke(item.getAspectObj(), result);
        }

        return result;
    }

    private void invokeAfterThrowingAdvice(Method method, Object[] args, Throwable e, Set<AspectInfo> aspectInfoSet) throws Throwable {
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
}
