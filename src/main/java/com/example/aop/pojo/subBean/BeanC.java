package com.example.aop.pojo.subBean;

import com.example.aop.pojo.impl.BeanB;
import com.example.aop.pojo.FirstBeanInterface;
import com.aim.ioc.annotation.Autowired;
import com.aim.core.annotation.Component;

@Component
public class FirstBeanC implements FirstBeanInterface {
    BeanB beanB;
    FirstBeanInterface firstBeanInterface;

    @Autowired
    public void setBeanB(BeanB bean) {
        this.beanB = bean;
    }

    @Autowired("BeanA")
    public void setBeanInterface(FirstBeanInterface firstBeanInterface) {
        this.firstBeanInterface = firstBeanInterface;
    }

    public void getRandom() {
        this.beanB.getRandom();
    }

    public String receiveRequest() {
        return firstBeanInterface.receiveRequest();
    }
}
