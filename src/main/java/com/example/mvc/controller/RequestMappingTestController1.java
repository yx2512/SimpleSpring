package com.example.mvc.controller;

import com.aim.core.annotation.Controller;
import com.aim.mvc.annotation.RequestMapping;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class RequestMappingTestController1 {
    @RequestMapping("/")
    public String home() {
        return "index.jsp";
    }
    @RequestMapping("/say")
    public void sayHello() {
      log.info("Hello there");
    }

    @RequestMapping("/time")
    public void displayTime() {
        log.info(String.valueOf(System.currentTimeMillis()));
    }
}
