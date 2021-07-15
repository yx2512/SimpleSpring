package com.aim.aop;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

public class CGlibDynamicProxy extends DefaultDynamicProxy implements MethodInterceptor {

    public CGlibDynamicProxy(Class<?> targetClass) {
        super(targetClass,false);
    }

    @Override
    public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        Method originalMethod = getTargetClass().getMethod(method.getName(), method.getParameterTypes());

        if(beforeMapContains(originalMethod)) {
            invokeBeforeAdvice(method, args, beforeMapGet(originalMethod));
        }

        try {
            Object returnValue;

            returnValue = methodProxy.invokeSuper(proxy, args);

            if(afterReturningMapContains(originalMethod)) {
                returnValue = invokeAfterReturningAdvice(method, args, returnValue, afterReturningMapGet(originalMethod));
            }

            return returnValue;
        } catch (Exception e) {
            if(afterThrowingMapContains(originalMethod)) {
                invokeAfterThrowingAdvice(method, args, e, afterThrowingMapGet(originalMethod));
            }
        }

        return null;
    }

}
