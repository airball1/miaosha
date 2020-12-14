package com.miaoshaproject.service;

import com.miaoshaproject.error.BussinessException;
import com.miaoshaproject.service.model.UserModel;

/**
 * @Author liuzike
 * @Date 12/1/20
 **/
public interface UserService {

    //通过用户ID获取用户对象的方法
    UserModel getUserById(Integer id);

    void register(UserModel userModel) throws BussinessException;
}
