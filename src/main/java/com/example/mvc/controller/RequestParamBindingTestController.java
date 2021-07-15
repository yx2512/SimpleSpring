package com.example.mvc.controller;

import com.example.mvc.pojo.User;
import com.simplespring.core.annotation.Controller;
import com.simplespring.mvc.annotation.PathVariable;
import com.simplespring.mvc.annotation.RequestBody;
import com.simplespring.mvc.annotation.RequestMapping;
import com.simplespring.mvc.annotation.RequestParam;
import com.simplespring.mvc.type.RequestMethod;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/user")
public class RequestParamBindingTestController {

    @RequestMapping(value = "/login",method = RequestMethod.POST)
    public void userLoginWithRequestParam(@RequestParam("email") String email, @RequestParam("password") String password) {
        log.info("Greetings ! user with email: {} and password: {}", email, password);
    }

    @RequestMapping(value = "/login/{email}/{password}", method = RequestMethod.GET)
    public void userLoginWithPathVariable(@PathVariable("email") String email, @PathVariable("password") String pwd) {
        log.info("Greetings ! user with email: {} and password: {}", email, pwd);
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public User userLoginWithJson(@RequestBody User user) {
        log.info("Greetings ! " + user.toString());
        return user;
    }
}
