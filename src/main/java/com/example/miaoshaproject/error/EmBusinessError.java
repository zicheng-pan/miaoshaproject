package com.example.miaoshaproject.error;

public enum EmBusinessError implements CommonError {


    // 通用错误类型 10001
    PARAMETER_VALIDATION_ERROR(10001, "参数不合法"),
    UNKNOWN_ERROR(10002, "未知错误"),
    //20000 开头为用户相关错误定义， 全局错误码，用来不同的开发组互通，不同的模块用不同开头的错误码
    USER_NOT_EXIST(20001, "用户不存在"),
    USER_LOGIN_FAIL(20002, "用户手机号或密码不正确"),
    USER_NOT_LOGIN(20003, "用户还未登录"),
    //30000 开头为交易信息错误
    STOCK_NOT_ENOUTH(30001, "库存不足");

    private int errCode;
    private String errMsg;

    EmBusinessError(int errorcode, String errMsg) {
        this.errCode = errorcode;
        this.errMsg = errMsg;
    }

    @Override
    public int getErrCode() {
        return this.errCode;
    }

    @Override
    public String getErrMsg() {
        return this.errMsg;
    }

    @Override
    public CommonError setErrMsg(String errMsg) {
        // 留了一个接口去改动 00001 的errorMessage  因为error错误很多，无尽，所以需要通过不同的场景定义不同的errorMsg
        this.errMsg = errMsg;
        return this;
    }
}
