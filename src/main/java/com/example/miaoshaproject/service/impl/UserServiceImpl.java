package com.example.miaoshaproject.service.impl;

import com.example.miaoshaproject.dao.UserDOMapper;
import com.example.miaoshaproject.dao.UserPasswordDOMapper;
import com.example.miaoshaproject.dataobject.UserDO;
import com.example.miaoshaproject.dataobject.UserPasswordDO;
import com.example.miaoshaproject.service.UserService;
import com.example.miaoshaproject.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDOMapper userDOMapper;

    @Autowired
    private UserPasswordDOMapper userPasswordDOMapper;

    @Override
    public UserModel getUserById(Integer id) {
        // 这个userDO 是一定不能吐出给用户端的 因为这个是数据库层的类型，应该返回前端的model
        //这个 model 才是spring mvc 交互模型的概念
        UserDO userDO = userDOMapper.selectByPrimaryKey(id);
        if (userDO == null)
            return null;
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());
        return convertFromDataObject(userDO, userPasswordDO);
    }

    private UserModel convertFromDataObject(UserDO userDO, UserPasswordDO userPasswordDO) {
        if (userDO == null)
            return null;
        UserModel userModel = new UserModel();
        BeanUtils.copyProperties(userDO, userModel);
        if (userPasswordDO != null)
            userModel.setEncrptPassword(userPasswordDO.getEncrptPassword());
        return userModel;
    }
}
