package com.example.mvc.controller;

import com.simplespring.core.annotation.Controller;
import com.simplespring.mvc.annotation.RequestMapping;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class RequestMappingTestController2 {
    @RequestMapping("/hi")
    public void sayHi() {
        log.info("hi");
    }
}
