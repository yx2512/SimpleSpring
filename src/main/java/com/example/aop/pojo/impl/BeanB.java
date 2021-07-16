package com.example.aop.pojo.impl;

import com.example.aop.pojo.subBean.BeanC;
import com.aim.ioc.annotation.Autowired;
import com.aim.core.annotation.Component;

import java.util.Random;

@Component
public class BeanB extends BeanA {

    BeanC beanC;

    @Autowired("BeanC")
    public void setBeanC(BeanC bean) {
        this.beanC = bean;
    }
//    @Time
    public int getRandom() {
        System.out.println("I'm getting a random int now");

        return new Random().nextInt();
    }
}
