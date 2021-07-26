package com.example.miaoshaproject.controller;

import com.example.miaoshaproject.error.BusinessException;
import com.example.miaoshaproject.error.EmBusinessError;
import com.example.miaoshaproject.response.CommonRetureType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class BaseController {

    // 处理所有的 Controller 中没有解决的 Exception 在tomcat 容器中，将返回结果都包装成自己的errorMessage
    // 对于这种方案 ExceptionHanlder 方法返回的Object 只能找一些返回值对应的资源文件，处理不像@ResponseBody一样，将字符串返回
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Object HandlerException(HttpServletRequest request, Exception exception) {
        Map<String, Object> responseData = new HashMap<>();
        if (exception instanceof BusinessException) {
            BusinessException businessException = (BusinessException) exception;
            responseData.put("errCode", businessException.getErrCode());
            responseData.put("errMsg", businessException.getErrMsg());
            return CommonRetureType.create(responseData, "fail");
        }
        responseData.put("errCode", EmBusinessError.UNKNOWN_ERROR.getErrCode());
        responseData.put("errMsg", EmBusinessError.UNKNOWN_ERROR.getErrMsg());
        return CommonRetureType.create(responseData, "fail");
    }
}
