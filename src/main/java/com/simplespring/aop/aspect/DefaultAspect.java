package com.simplespring.aop.aspect;

import java.lang.reflect.Method;

public class DefaultAspect {
    public void before(Class<?> targetClass, Method method, Object[] args) throws Throwable {}

    public Object afterReturning(Class<?> targetClass, Method method, Object [] args, Object returnValue) throws Throwable {
        return returnValue;
    }

    public void afterThrowing(Class<?> targetClass, Method method, Object[]args, Throwable e) throws Throwable {}
}
