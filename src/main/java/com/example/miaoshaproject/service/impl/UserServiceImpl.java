package com.example.miaoshaproject.service.impl;

import com.example.miaoshaproject.dao.UserDOMapper;
import com.example.miaoshaproject.dao.UserPasswordDOMapper;
import com.example.miaoshaproject.dataobject.UserDO;
import com.example.miaoshaproject.dataobject.UserPasswordDO;
import com.example.miaoshaproject.error.BusinessException;
import com.example.miaoshaproject.error.EmBusinessError;
import com.example.miaoshaproject.service.UserService;
import com.example.miaoshaproject.service.model.UserModel;
import com.example.miaoshaproject.validator.ValidationResult;
import com.example.miaoshaproject.validator.ValidatorImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDOMapper userDOMapper;

    @Autowired
    private UserPasswordDOMapper userPasswordDOMapper;

    @Autowired
    private ValidatorImpl validator;

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

    @Override
    @Transactional
    public void register(UserModel userModel) throws BusinessException {
        if (userModel == null) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }
//        if (StringUtils.isEmpty(userModel.getName()) ||
//                userModel.getAge() == null ||
//                StringUtils.isEmpty(userModel.getEncrptPassword()) ||
//                userModel.getAge() == null
//        ) {

        // 通过这种简单的方式，就可以在我们想要校验的model类上添加相应的注解就可以了
        ValidationResult validationResult = validator.validate(userModel);
        if (validationResult.isHasErrors()) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, validationResult.getErrMsg());
        }

        // 将UserModel 转成UserDO
        UserDO userDO = convertFromModel(userModel);

        // 可以看到用insertSelective 会进行判空的判断，如果为空，那么这个字段就用数据库的默认值，如果不进行判断
        // 直接使用insert 那么就会用null 来覆盖掉数据库的默认值
        // 在数据库插值的时候，尽量避免使用null字段
        try {
            userDOMapper.insertSelective(userDO);
            userModel.setId(userDO.getId());
        } catch (DuplicateKeyException ex) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "手机号已重复注册");
        }
        UserPasswordDO userPasswordDO = convertUserPasswordDOFromUserModel(userModel);
        userPasswordDOMapper.insertSelective(userPasswordDO);

    }

    @Override
    public UserModel validateLogin(String telephone, String password) throws BusinessException {
        // 通过用户手机获取用户信息
        UserDO userDO = userDOMapper.selectByTelephone(telephone);
        // 校验用户输入密码和加密密码是否一致
        if (userDO == null) {
            throw new BusinessException(EmBusinessError.USER_LOGIN_FAIL);
        }
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());
        String userPassword = userPasswordDO.getEncrptPassword();

        if (!StringUtils.equals(password, userPassword)) {
            throw new BusinessException(EmBusinessError.USER_LOGIN_FAIL);
        }

        return convertFromDataObject(userDO, userPasswordDO);// 用户登录成功
    }

    private UserPasswordDO convertUserPasswordDOFromUserModel(UserModel userModel) {
        if (userModel == null)
            return null;
        UserPasswordDO userPasswordDO = new UserPasswordDO();
        userPasswordDO.setEncrptPassword(userModel.getEncrptPassword());
        userPasswordDO.setUserId(userModel.getId());
        return userPasswordDO;
    }

    private UserDO convertFromModel(UserModel userModel) {
        UserDO userDO = new UserDO();
        if (userModel == null)
            return null;
        BeanUtils.copyProperties(userModel, userDO);
        return userDO;
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
