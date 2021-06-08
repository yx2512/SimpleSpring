package com.simplespring.core.utils;

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
        try {
            Set<Class<?>> classSet = ClassUtil.extractPackageClass("com.pojo");
            assert classSet != null;
            log.info(classSet.toString());
        } catch (IOException | URISyntaxException e) {
            log.warn(e.getMessage());
        }
    }
}