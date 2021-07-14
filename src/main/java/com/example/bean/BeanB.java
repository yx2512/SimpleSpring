package com.example.bean;

import com.example.annotation.Time;
import com.example.bean.subBean.BeanC;
import com.simplespring.core.annotation.Autowired;
import com.simplespring.core.annotation.Component;

import java.util.Random;

@Component
public class BeanB extends BeanA{

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
