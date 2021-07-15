package com.example.aop.pojo;

import com.simplespring.core.annotation.Autowired;
import com.simplespring.core.annotation.Component;

@Component
public class BeanA implements BeanInterface, BeanInterface2{

    BeanB beanB;

    @Autowired("BeanB")
    public void setBeanB(BeanB bean) {
        this.beanB = bean;
    }
//    @Time
//    @Greeting
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
