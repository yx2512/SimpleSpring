package com.example.mvc.service;

import com.example.mvc.aspect.annotation.Time;
import com.simplespring.core.annotation.Service;

@Service
public class ServiceImpl implements GenericService{

    @Time
    @Override
    public void doService() {
        System.out.println("Let's do some service");
    }
}
