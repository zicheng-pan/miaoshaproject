package com.example.miaoshaproject.controller;

import com.example.miaoshaproject.controller.viewobject.UserVO;
import com.example.miaoshaproject.error.BusinessException;
import com.example.miaoshaproject.error.EmBusinessError;
import com.example.miaoshaproject.response.CommonRetureType;
import com.example.miaoshaproject.service.UserService;
import com.example.miaoshaproject.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller("user")
@RequestMapping("/user")
public class UserController extends BaseController {

    @Autowired
    UserService userService;

    @RequestMapping("/get")
    @ResponseBody
    public CommonRetureType getUser(@RequestParam(name = "id") Integer id) throws BusinessException {
        // 这里我们将java业务的领域模型直接返回给了前端，不可取，所以需要viewobject
        UserModel userModel = userService.getUserById(id);
        if (userModel == null) {
            throw new BusinessException(EmBusinessError.USER_NOT_EXIST);
        }
        UserVO userVO = convertUserModeltoUserVO(userModel);

        return CommonRetureType.create(userVO);
    }

    private UserVO convertUserModeltoUserVO(UserModel userModel) {
        if (userModel == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userModel, userVO);
        return userVO;
    }

}
