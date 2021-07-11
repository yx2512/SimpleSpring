package com.simplespring.aop.aspect;

public class AspectInfo {
    private int order;
    private DefaultAspect aspect;

    public AspectInfo(int order, DefaultAspect aspect) {
        this.order = order;
        this.aspect = aspect;
    }

    public int getOrder() {
        return order;
    }

    public DefaultAspect getAspect() {
        return aspect;
    }
}
