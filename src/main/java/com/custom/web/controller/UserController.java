package com.custom.web.controller;


import com.custom.springmvc.annotation.AutoWired;
import com.custom.springmvc.annotation.Controller;
import com.custom.springmvc.annotation.RequestMapping;
import com.custom.springmvc.annotation.ResponseBody;
import com.custom.web.bean.User;
import com.custom.web.service.UserService;

/**
 * @author 白起老师
 */
@Controller
public class UserController {

       @AutoWired(value="userService")
       private UserService userService;


       //定义方法
       @RequestMapping("/findUser")
       public  String  findUser(String name){
           //调用服务层
           userService.findUser();
           return "forward:/success.jsp";
       }

    @RequestMapping("/getData")
    @ResponseBody  //返回json格式的数据
    public User getData(){
        //调用服务层
        return userService.getUser();
    }
}
