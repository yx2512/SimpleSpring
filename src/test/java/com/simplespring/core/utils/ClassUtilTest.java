package com.simplespring.core.utils;

import com.example.bean.BeanA;
import com.example.bean.BeanB;
import com.simplespring.core.context.BeanContainer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
@Slf4j
class ClassUtilTest {

    @Test
    void extractPackageClass() {
        BeanContainer beanContainer = BeanContainer.getInstance();
        beanContainer.init("com.example");
        BeanA beanA = beanContainer.getBean(BeanA.class);

        beanA.receiveRequest();
        System.out.println(" ");
        beanA.sendResponse();
        System.out.println(" ");

        BeanB beanB = beanContainer.getBean(BeanB.class);
        beanB.receiveRequest();
        beanB.getRandom();
    }
}