package com.example.miaoshaproject.controller;

import com.example.miaoshaproject.error.BusinessException;
import com.example.miaoshaproject.error.EmBusinessError;
import com.example.miaoshaproject.response.CommonRetureType;
import com.example.miaoshaproject.service.OrderService;
import com.example.miaoshaproject.service.model.OrderModel;
import com.example.miaoshaproject.service.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/order")

@CrossOrigin(allowCredentials = "true", allowedHeaders = "*")
public class OrderController extends BaseController {


    @Autowired
    private OrderService orderService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @RequestMapping(value = "/createorder", method = {RequestMethod.POST}, consumes = CONTENT_TYPE_FORMED)
    @ResponseBody
    public CommonRetureType createOrder(@RequestParam(name = "itemId") Integer itemId,
                                        @RequestParam(name = "amount") Integer amount) throws BusinessException {

        Boolean isLogin = (Boolean) httpServletRequest.getSession().getAttribute("IS_LOGIN");
        if (isLogin == null || !isLogin) {
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN, "用户未登录, 不能下单");
        }
        UserModel userModel = (UserModel) httpServletRequest.getSession().getAttribute("LOGIN_USER");
        OrderModel order = orderService.createOrder(userModel.getId(), itemId, amount);
        return CommonRetureType.create(order);
    }

}
