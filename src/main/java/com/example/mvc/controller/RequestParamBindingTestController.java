package com.example.mvc.controller;

import com.example.mvc.pojo.User;
import com.example.mvc.service.GenericService;
import com.aim.ioc.annotation.Autowired;
import com.aim.core.annotation.Controller;
import com.aim.mvc.annotation.*;
import com.aim.mvc.type.ModelAndView;
import com.aim.mvc.type.RequestMethod;
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
        log.info("Greetings ! You are logging in with email: {} and password: {}", email, password);
        service.doService();
    }

    @RequestMapping(value = "/login/{email}/{password}", method = RequestMethod.POST)
    public void userLoginWithPathVariable(@PathVariable(value = "email") String email, @PathVariable("password") String pwd) {
        log.info("Greetings ! You are logging in with email: {} and password: {}", email, pwd);
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public void userRegisterWithRequestParam(@RequestParam(value = "email", required = false, defaultValue = "register@gamil.com") String email,
                                             @RequestParam("password") String password) {
        log.info("Greetings ! You are registering with email: {} and password: {}", email, password);
    }

    @RequestMapping(value = "/loginJSON", method = RequestMethod.POST)
    @ResponseBody
    public User userLoginWithJson(@RequestBody User user) {
        log.info("Greetings ! " + user.toString());
        return user;
    }

    @RequestMapping(value = "/loginRedirect", method = RequestMethod.POST)
    public ModelAndView redirectAfterUserLogin(@RequestBody User user) {
        log.info("Greetings ! " + user.toString());
        ModelAndView mv = new ModelAndView();
        mv.setView("test.jsp");
        mv.addViewData("current_user",user);
        return mv;
    }
}
