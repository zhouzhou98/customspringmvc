package com.custom.web.service.impl;

import com.custom.springmvc.annotation.Service;
import com.custom.web.bean.User;
import com.custom.web.service.UserService;

/**
 * @author 白起老师
 */
@Service(value="userService")
public class UserServiceImpl implements UserService {

    @Override
    public  void  findUser() {
        System.out.println("====调用UserServiceImpl==findUser===");
    }
    @Override
    public User getUser() {

       return new User(1,"老王","admin");
    }

}
