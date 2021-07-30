package com.example.miaoshaproject.service;

import com.example.miaoshaproject.error.BusinessException;
import com.example.miaoshaproject.service.model.OrderModel;

public interface OrderService {

    // 1. 通过前端url 上传过来秒杀活动ID，然后下单接口内校验 对应id是否存在，是属于对应商品，且活动已开始
    // 2. 直接在下单接口内判断对应的商品是否存在秒杀活动，若存在进行中则以秒杀价下单
    OrderModel createOrder(Integer userId, Integer itemId, Integer amount,Integer promoId) throws BusinessException;
}
