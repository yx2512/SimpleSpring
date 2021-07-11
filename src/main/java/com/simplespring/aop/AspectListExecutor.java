package com.simplespring.aop;

import com.simplespring.aop.aspect.AspectInfo;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;

public class AspectListExecutor implements MethodInterceptor {

    private Class<?> targetClass;
    private List<AspectInfo> aspectInfoList;

    public AspectListExecutor(Class<?> targetClass, List<AspectInfo> aspectInfoList) {
        this.targetClass = targetClass;
        aspectInfoList.sort(Comparator.comparingInt(AspectInfo::getOrder));
        this.aspectInfoList = aspectInfoList;
    }

    @Override
    public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        Object returnValue = null;
        invokeBeforeAdvice(method, args);

        try{
            returnValue = methodProxy.invokeSuper(proxy, args);
            returnValue = invokeAfterReturningAdvice(method, args, returnValue);
        } catch (Exception e) {
            invokeAfterThrowingAdvice(method, args, e);
        }
        return returnValue;
    }

    private void invokeBeforeAdvice(Method method, Object [] args) throws Throwable {
        for(AspectInfo item : aspectInfoList) {
            item.getAspect().before(targetClass, method, args);
        }
    }

    private Object invokeAfterReturningAdvice(Method method, Object[] args, Object returnValue) throws Throwable {
        Object result = null;
        for(int i=aspectInfoList.size() - 1; i >=0; i--) {
            result = aspectInfoList.get(i).getAspect().afterReturning(targetClass, method, args, returnValue);
        }

        return result;
    }

    private void invokeAfterThrowingAdvice(Method method, Object[] args, Throwable e) throws Throwable {
        for(int i=aspectInfoList.size() - 1; i >=0; i--) {
             aspectInfoList.get(i).getAspect().afterThrowing(targetClass, method, args, e);
        }
    }
}
