package com.example.aspect;

import com.example.annotation.Greeting;
import com.example.annotation.Time;
import com.simplespring.aop.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;

@Aspect
@Order(2)
public class Timer {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    @Before(Time.class)
    public void tellStartTime() {
        System.out.println("Method call start at " + sdf.format(new Date(System.currentTimeMillis())));
    }

    @AfterReturning(Time.class)
    public void tellEndTime(Object returnValue) {
        System.out.println("Method call end at " + sdf.format(new Date(System.currentTimeMillis())) + " with return value " + returnValue);
    }

    @AfterThrowing(Time.class)
    public void tellErrorTime(Exception e) {
        System.out.println("Error Time: " + System.currentTimeMillis() + " , error detail is " + e);
    }
}
