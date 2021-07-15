package com.example.aop.pojo.subBean;

import com.example.aop.pojo.BeanB;
import com.example.aop.pojo.BeanInterface;
import com.simplespring.core.annotation.Autowired;
import com.simplespring.core.annotation.Component;

@Component
public class BeanC implements BeanInterface{
    BeanB beanB;
    BeanInterface beanInterface;

    @Autowired
    public void setBeanB(BeanB bean) {
        this.beanB = bean;
    }

    @Autowired("BeanA")
    public void setBeanInterface(BeanInterface beanInterface) {
        this.beanInterface = beanInterface;
    }

    public void getRandom() {
        this.beanB.getRandom();
    }

    public String receiveRequest() {
        return beanInterface.receiveRequest();
    }
}
