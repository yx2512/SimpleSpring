package com.example.bean;

import com.example.annotation.Time;
import com.simplespring.core.annotation.Component;

import java.util.Random;

@Component
public class BeanB extends BeanA{

    @Time
    public int getRandom() {
        System.out.println("I'm getting a random int now");

        return new Random().nextInt();
    }
}
