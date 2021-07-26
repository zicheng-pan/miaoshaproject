package com.example.miaoshaproject.error;

// 设计模式：包装器业务异常类 实现 可以通过EmBusinessError 或者直接通过Exception都可以来生成 CommonError对象
public class BusinessException extends Exception implements CommonError {

    private CommonError commonError;

    // 直接接受EmBusinessError 的传参用于构造业务异常
    public BusinessException(CommonError commonError) {
        super(); // Exception 本身有一些初始化的机制在里面
        this.commonError = commonError;
    }

    //接受自定义errMsg的方式构造业务异常
    public BusinessException(CommonError commonError, String errMsg) {
        super();
        this.commonError = commonError;
        this.commonError.setErrMsg(errMsg);
    }

    @Override
    public int getErrCode() {
        return this.commonError.getErrCode();
    }

    @Override
    public String getErrMsg() {
        return this.commonError.getErrMsg();
    }

    @Override
    public CommonError setErrMsg(String errMsg) {
        this.commonError.setErrMsg(errMsg);
        return this;
    }
}
