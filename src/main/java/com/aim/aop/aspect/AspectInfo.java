package com.aim.aop.aspect;

import java.lang.reflect.Method;

public class AspectInfo {
    private int order;
    private Method method;
    private Object aspectObj;

    public AspectInfo(int order, Method method, Object aspectObj) {
        this.order = order;
        this.method = method;
        this.aspectObj = aspectObj;
    }

    public int getOrder() {
        return order;
    }

    public Method getMethod() {
        return method;
    }

    public Object getAspectObj() {
        return aspectObj;
    }
}
