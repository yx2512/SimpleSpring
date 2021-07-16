package com.aim.ioc;

import com.aim.core.context.BeanContainer;
import com.aim.ioc.annotation.Autowired;
import com.aim.ioc.exception.DependencyInjectionException;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

@Slf4j
public class DependencyInjector {
    private final BeanContainer beanContainer;

    public DependencyInjector() {
        this.beanContainer = BeanContainer.getInstance();
    }

    public void doIoC() {
        if(beanContainer.size() == 0) {
            log.warn("Empty container");
            return;
        }

        Set<Map.Entry<String, Object>> beanEntrySet = beanContainer.getBeans();
        for(Map.Entry<String, Object> entry : beanEntrySet) {
            Class<?> clazz = entry.getValue().getClass();
            Method[] declaredMethods = clazz.getDeclaredMethods();
            for(Method method : declaredMethods) {
                if(method.isAnnotationPresent(Autowired.class)) {
                    if(method.getParameterCount() != 1) {
                        throw new DependencyInjectionException("Wrong number of arguments");
                    } else {
                        Autowired autowiredAnnotation = method.getAnnotation(Autowired.class);
                        String alias = autowiredAnnotation.value();
                        Class<?> parameterType = method.getParameterTypes()[0];
                        Object parameterInstance = getParameterInstance(parameterType.getSimpleName(), parameterType, alias);

                        Object bean = entry.getValue();
                        method.setAccessible(true);

                        if(parameterInstance.getClass().getSimpleName().equals(entry.getKey())) {
                            throw new DependencyInjectionException("Circular dependency found in bean with class: " + clazz);
                        }
                        try {
                            method.invoke(bean,parameterInstance);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new DependencyInjectionException(e.getMessage());
                        }
                    }
                }
            }
        }
    }

    private Object getParameterInstance(String name, Class<?> clazz, String alias) {
        Object bean = beanContainer.getBean(name);
        if(bean != null) {
            return bean;
        } else {
            String candidateBean = getCandidateBeanName(clazz, alias);
            return beanContainer.getBean(candidateBean);
        }
    }

    private String getCandidateBeanName(Class<?> clazz, String alias) {
        Set<String> superSet = beanContainer.getClassBySuperClass(clazz);
        if(superSet == null || superSet.size() == 0) {
            throw new DependencyInjectionException("No bean of type " + clazz.toString());
        }

        if(alias.isEmpty()) {
            if(superSet.size() == 1) return superSet.iterator().next();
            else {
                throw new DependencyInjectionException("Too many options for bean of type " + clazz.toString());
            }
        } else {
            for(String candi : superSet) {
                if(candi.equals(alias)) {
                    return candi;
                }
            }
            throw new DependencyInjectionException("Cannot find bean with alias " + alias);
        }
    }
}
