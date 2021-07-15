package com.example.aop.aspect;

import com.example.aop.annotation.Greeting;
import com.aim.aop.annotation.AfterReturning;
import com.aim.aop.annotation.Aspect;
import com.aim.aop.annotation.Before;
import com.aim.aop.annotation.Order;

@Aspect
@Order(1)
public class Hello {
    @Before(Greeting.class)
    public void sayHello() {
        System.out.println("Hello there!");
    }

    @AfterReturning(Greeting.class)
    public Object sayBye(Object returnValue) {
        if(returnValue == null) {
            System.out.println("Good Bye!");
            return null;
        } else {
            System.out.println("Good Bye with return value " + returnValue);
            return returnValue;
        }
    }
}
