package com.simplespring.aop;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class ProxyCreator {
    public static Object createProxy(Class<?> clazz, DefaultDynamicProxy dynamicProxy) {
        if(dynamicProxy.isJDK()) {
            return Proxy.newProxyInstance(clazz.getClassLoader(), clazz.getInterfaces(), (InvocationHandler) dynamicProxy);
        } else {
            return Enhancer.create(clazz, (MethodInterceptor) dynamicProxy);
        }
    }
}
