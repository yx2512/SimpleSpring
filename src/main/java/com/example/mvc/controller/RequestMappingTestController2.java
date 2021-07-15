package com.example.mvc.controller;

import com.aim.core.annotation.Controller;
import com.aim.mvc.annotation.RequestMapping;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class RequestMappingTestController2 {
    @RequestMapping("/hi")
    public void sayHi() {
        log.info("hi");
    }
}
