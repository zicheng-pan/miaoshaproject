package com.example.miaoshaproject.response;

public class CommonRetureType {

    // status 表明请求的结果 success or fail
    private String status;

    // 如果status 为 success  则data 返回前端需要的json数据
    // 如果status 为 fail 则data内使用通用的错误码格式
    private Object data;

    public static CommonRetureType create(Object result) {
        return CommonRetureType.create(result, "success");
    }

    public static CommonRetureType create(Object result, String status) {
        CommonRetureType commonRetureType = new CommonRetureType();
        commonRetureType.setData(result);
        commonRetureType.setStatus(status);
        return commonRetureType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
