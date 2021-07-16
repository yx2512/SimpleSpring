package com.aim.aop.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class JDKDynamicProxy extends DefaultDynamicProxy implements InvocationHandler {
    private final Object targetObject;

    public JDKDynamicProxy(Object object) {
        super(object.getClass(),true);
        this.targetObject = object;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Method originalMethod = getTargetClass().getMethod(method.getName(), method.getParameterTypes());

        if(beforeMapContains(originalMethod)) {
            invokeBeforeAdvice(method, args, beforeMapGet(originalMethod));
        }

        try {
            Object returnValue;

            returnValue = method.invoke(targetObject, args);

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
