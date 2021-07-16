# AIM-framework

AIM, an abbreviation of **A**OP + **I**OC + **M**VC, is a light-weighted framework which provides AOP and IOC support for your Java project.
AIM also encapsulates an MVC framework which is built around a DispatchServlet.

## Highlights

### IOC container
⭐️ Method level dependency injection\
⭐️ Resolve circular dependency

### AOP
⭐️ Support both JDK dynamic proxy and CGLib dynamic proxy\
⭐️ Point cut declaration with custom annotation

### MVC framework
⭐️ Automatic type conversion between request parameters/payload and controller arguments

## AIM capabilities

### Bean Container

BeanContainer is the core container of AIM framework, and it is responsible for instantiating, assembling and managing 
the beans annotated by the following annotations:```@Component```, ```@Controller```,```@Service```,```@Repository```,```@Aspect```

### IOC
AIM supports method level dependency injection as shown below with the help of ```@Autowired``` annotation. Inorder to resolve
possible ambiguity, user could specify the name of the bean to be injected in the ```@Autowired``` annotation. 

```java
import com.aim.core.annotation.Component;
import com.aim.ioc.annotation.Autowired;

@Component
public class BeanA {
    BeanB beanB;

    @Autowired
    public void setBeanB(BeanB bean) {
        this.beanB = bean;
    }
}
```

### AOP

AIM supports two kinds of dynamic proxy mechanism, JDK dynamic proxy and CGlib dynamic proxy. JDK dynamic proxy will be
applied only when the proxied class implements at least one interface. Otherwise, CGlib dynamic proxy will be used.

A class annotated with ```@Aspect``` is considered as an aspect class. Advices can be declared in such class with the following annotations:
```@Before```,```@After```,```@AfterReturning```,```@AfterThrowing```.

```java

import com.aim.aop.annotation.AfterReturning;
import com.aim.aop.annotation.Aspect;

@Aspect
public class Aspect1 {
    @Before(Time.class)
    public void beforeAdvice() {
        ...
    }

    @AfterReturning(Time.class)
    public Object afterReturningAdvice() {
        ...
    }
}
```

Point cut in AIM is declared as custom annotation created by the user and it is needed when declaring an Advice.

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Time {
}

```

```java
import com.aim.core.annotation.Component;
import com.example.mvc.aspect.annotation.Time;

@Component
public class component1 {
    
    @Time // Join Point
    public void method1() {
        ...
    }
} 
```

In case of having multiple aspects applied to the same join point, ```@Order``` annotation can be used above aspect class to specify the ordering
of each aspect with smaller number having higher priority.

```java
import com.aim.aop.annotation.Aspect;
import com.aim.aop.annotation.Order;

@Aspect
@Order(1)
public class Aspect1{}
```

### MVC





