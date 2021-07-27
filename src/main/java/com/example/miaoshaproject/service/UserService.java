package com.example.miaoshaproject.service;

import com.example.miaoshaproject.error.BusinessException;
import com.example.miaoshaproject.service.model.UserModel;

public interface UserService {

    UserModel getUserById(Integer id);

    void register(UserModel userModel) throws BusinessException;
}
