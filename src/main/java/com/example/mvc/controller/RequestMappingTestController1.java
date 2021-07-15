package com.example.mvc.controller;

import com.simplespring.core.annotation.Controller;
import com.simplespring.mvc.annotation.RequestMapping;
import com.simplespring.mvc.type.RequestMethod;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping(value = "/hello", method = RequestMethod.GET)
public class RequestMappingTestController1 {
    @RequestMapping("/say")
    public void sayHello() {
      log.info("Hello there");
    }

    @RequestMapping("/time")
    public void displayTime() {
        log.info(String.valueOf(System.currentTimeMillis()));
    }
}
