package com.example.bean;

import com.example.annotation.Greeting;
import com.example.annotation.Time;
import com.simplespring.core.annotation.Component;

@Component
public class BeanA implements BeanInterface{

    @Time
    @Greeting
    public String receiveRequest() {
        System.out.println("request received!");
        return "request received";
    }

    @Time
    public void sendResponse() {
        System.out.println("response sent!");
        throw new RuntimeException();
    }
}
