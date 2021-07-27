package com.example.miaoshaproject.controller;

import com.alibaba.druid.util.StringUtils;
import com.example.miaoshaproject.controller.viewobject.UserVO;
import com.example.miaoshaproject.error.BusinessException;
import com.example.miaoshaproject.error.EmBusinessError;
import com.example.miaoshaproject.response.CommonRetureType;
import com.example.miaoshaproject.service.UserService;
import com.example.miaoshaproject.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import sun.misc.BASE64Encoder;

import javax.servlet.http.HttpServletRequest;
import java.security.MessageDigest;
import java.util.Random;

@Controller("user")
@RequestMapping("/user")
//(allowCredentials = "true", allowedHeaders = "*")
@CrossOrigin
public class UserController extends BaseController {

    @Autowired
    UserService userService;

    // springbean 里面包装的httprequest ， 通过threadlocal 里面的map获取用户线程自己的request对象
    @Autowired
    private HttpServletRequest httpServletRequest;

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

    @RequestMapping(value = "/login", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonRetureType login(@RequestParam(name = "telephone") String telephone,
                                  @RequestParam(name = "password") String password) throws Exception {
        if (org.apache.commons.lang3.StringUtils.isEmpty(telephone) && org.apache.commons.lang3.StringUtils.isEmpty(password))
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "用户名密码不能为空");
        UserModel userModel = userService.validateLogin(telephone, this.EncodeByMd5(password));

        httpServletRequest.getSession().setAttribute("IS_LOGIN", true); // 单点设置登录成功
        this.httpServletRequest.getSession().setAttribute("LOGIN_USER", userModel);
        return CommonRetureType.create("用户登录成功");
    }

    //解析post里面的 form表单里面的请求数据 同样通过@RequestParam的参数
    @RequestMapping(value = "/getotp", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    //用户获取otp短信接口
    public CommonRetureType getOtp(@RequestParam("telephone") String telephone) {
        // 需要按照一定的规则生成OTP验证码
        String optCode = generateOTPCode();
        //将OTP验证码同对应用户的手机号关联  redis 中 ，并且可以进行expire 和 覆盖 分布式的额方案
        // 这里采用httpSession的方案
        httpServletRequest.getSession().setAttribute(telephone, optCode);

        //将OTP验证码通过短信通道发送给用户    --》 买对应的短信业务
        System.out.println("telephone=" + telephone + "& optCode=" + optCode);

        return CommonRetureType.create(null);
    }

    @RequestMapping(value = "/register", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonRetureType register(@RequestParam("telephone") String telephone,
                                     @RequestParam("otpCode") String otpCode,
                                     @RequestParam("name") String name,
                                     @RequestParam("gender") Integer gender,
                                     @RequestParam("age") Integer age,
                                     @RequestParam("password") String password) throws BusinessException {

        // 验证手机号和otpcode相符合
        Object inSessionOtpCode = httpServletRequest.getSession().getAttribute(telephone);
        if (StringUtils.equals(otpCode, (String) inSessionOtpCode)) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "短信验证码不符");
        }

        UserModel userModel = new UserModel();
        try {
            userModel.setEncrptPassword(EncodeByMd5(password));
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        userModel.setAge(age);
        userModel.setGender(gender.byteValue());
        userModel.setName(name);
        userModel.setTelephone(telephone);

        userService.register(userModel);
        return CommonRetureType.create("注册成功！");
    }


    private String generateOTPCode() {
        //        采用随机数的方式
        Random random = new Random();
        int randomInt = random.nextInt(99999);
        randomInt += 100000;
        String optCode = String.valueOf(randomInt);
        return optCode;
    }

    private UserVO convertUserModeltoUserVO(UserModel userModel) {
        if (userModel == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userModel, userVO);
        return userVO;
    }

    public String EncodeByMd5(String str) throws Exception {
        // 确定计算方法
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        BASE64Encoder base64Encoder = new BASE64Encoder();
        // 加密字符串
        String newstr = base64Encoder.encode(md5.digest(str.getBytes("utf-8")));
        return newstr;
    }

}
