package com.example.aop.pojo.impl;

import com.example.aop.annotation.Greeting;
import com.example.aop.annotation.Time;
import com.example.aop.pojo.FirstBeanInterface;
import com.example.aop.pojo.SecondBeanInterface;
import com.aim.ioc.annotation.Autowired;
import com.aim.core.annotation.Component;

@Component
public class BeanA implements FirstBeanInterface, SecondBeanInterface {

    BeanB beanB;

    @Autowired("BeanB")
    public void setBeanB(BeanB bean) {
        this.beanB = bean;
    }
    @Time
    @Greeting
    public String receiveRequest() {
        System.out.println("request received!");
        return "request received";
    }

//    @Time
    public void sendResponse() {
        System.out.println("response sent!");
//        throw new RuntimeException();
    }
}
