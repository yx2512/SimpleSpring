package com.example.mvc.controller;

import com.example.mvc.aspect.annotation.Time;
import com.example.mvc.pojo.User;
import com.example.mvc.service.GenericService;
import com.simplespring.core.annotation.Autowired;
import com.simplespring.core.annotation.Controller;
import com.simplespring.mvc.annotation.*;
import com.simplespring.mvc.type.RequestMethod;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/user")
public class RequestParamBindingTestController {

    private GenericService service;

    @Autowired
    public void setGenericService(GenericService service) {
        this.service = service;
    }


    @RequestMapping(value = "/login",method = RequestMethod.POST)
    public void userLoginWithRequestParam(@RequestParam("email") String email, @RequestParam("password") String password) {
        log.info("Greetings ! user with email: {} and password: {}", email, password);
        service.doService();
    }

    @RequestMapping(value = "/login/{email}/{password}", method = RequestMethod.POST)
    public void userLoginWithPathVariable(@PathVariable("email") String email, @PathVariable("password") String pwd) {
        log.info("Greetings ! user with email: {} and password: {}", email, pwd);
    }

//    @RequestMapping(value = "/login", method = RequestMethod.POST)
//    @ResponseBody
//    public User userLoginWithJson(@RequestBody User user) {
//        log.info("Greetings ! " + user.toString());
//        return user;
//    }
}
