package com.example.aop.pojo.impl;

import com.example.aop.pojo.subBean.FirstBeanC;
import com.aim.core.annotation.Autowired;
import com.aim.core.annotation.Component;

import java.util.Random;

@Component
public class BeanB extends BeanA {

    FirstBeanC beanC;

    @Autowired("BeanC")
    public void setBeanC(FirstBeanC bean) {
        this.beanC = bean;
    }
//    @Time
    public int getRandom() {
        System.out.println("I'm getting a random int now");

        return new Random().nextInt();
    }
}
