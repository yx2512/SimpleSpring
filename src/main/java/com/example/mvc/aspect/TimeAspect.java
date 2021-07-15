package com.example.mvc.aspect;

import com.example.mvc.aspect.annotation.Time;
import com.aim.aop.annotation.Aspect;
import com.aim.aop.annotation.Before;

@Aspect
public class TimeAspect {
    @Before(Time.class)
    public void displayTime() {
        System.out.println("current time millis is : " + System.currentTimeMillis());
    }
}
