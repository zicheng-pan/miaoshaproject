package com.example.miaoshaproject.service;

import com.example.miaoshaproject.error.BusinessException;
import com.example.miaoshaproject.service.model.OrderModel;

public interface OrderService {

    OrderModel createOrder(Integer userId, Integer itemId, Integer amount) throws BusinessException;
}
