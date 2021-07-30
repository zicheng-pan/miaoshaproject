package com.example.miaoshaproject.service.impl;

import com.example.miaoshaproject.dao.OrderDOMapper;
import com.example.miaoshaproject.dao.SequenceDOMapper;
import com.example.miaoshaproject.dataobject.OrderDO;
import com.example.miaoshaproject.dataobject.SequenceDO;
import com.example.miaoshaproject.error.BusinessException;
import com.example.miaoshaproject.error.EmBusinessError;
import com.example.miaoshaproject.service.ItemService;
import com.example.miaoshaproject.service.OrderService;
import com.example.miaoshaproject.service.PromoService;
import com.example.miaoshaproject.service.UserService;
import com.example.miaoshaproject.service.model.ItemModel;
import com.example.miaoshaproject.service.model.OrderModel;
import com.example.miaoshaproject.service.model.PromoModel;
import com.example.miaoshaproject.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderDOMapper orderDOMapper;


    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private SequenceDOMapper sequenceDOMapper;

    @Autowired
    private PromoService promoService;

    @Transactional
    @Override
    public OrderModel createOrder(Integer userId, Integer itemId, Integer amount, Integer promoId) throws BusinessException {

        // 首先需要校验状态
        // 1. 校验下单状态，下单商品是否存在， 用户是否合法，购买数量是否正确
        ItemModel itemModel = itemService.getItemById(itemId);
        if (itemModel == null)
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "下单商品不存在");

        UserModel userModel = userService.getUserById(userId);
        if (userModel == null)
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "非法用户");

        if (amount <= 0 || amount > 99) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "下单数量不正确");
        }


        //2. 落单减库存，否则支付减库存，那么用户支付后，才减库存，会存在用户超买的情况，商家可以接受，有多余的备货，那么可以使用这种方案
        // 这里采用先冻结库存，然后下单落库的操作
        boolean result = itemService.decreaseStock(itemId, amount);
        if (!result) {
            throw new BusinessException(EmBusinessError.STOCK_NOT_ENOUTH);
        }
        // 3. 订单落库
        OrderModel orderModel = new OrderModel();
        orderModel.setAmount(amount);
        orderModel.setItemId(itemId);
        if (promoId != null) {
            PromoModel promoModel = promoService.getPromoById(itemId);
            if (promoId != itemModel.getPromoModel().getId()) {
                // 校验对应活动是否存在这个使用商品
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "活动信息不正确");
            } else if (itemModel.getPromoModel().getStatus() != 2) {
                // 校验活动是否正在进行
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "活动还未开始");

            }
            orderModel.setItemPrice(promoModel.getPromoItemPrice());
        } else {
            orderModel.setItemPrice(itemModel.getPrice());
        }
        orderModel.setUserId(userId);
        orderModel.setPromoId(promoId);
        orderModel.setId(generateOrderID());
        OrderDO orderDO = convertFromOrderModel(orderModel);
        orderDOMapper.insert(orderDO);

        // 添加销量
        itemService.increaseSales(itemId, amount);
        // 4. 返回前端
        return orderModel;
    }


    // 生成订单号

    /**
     * public enum Propagation {
     * REQUIRED(0),  如果这个代码已经在一个事务中了那么就不开启
     * SUPPORTS(1),
     * MANDATORY(2),
     * REQUIRES_NEW(3),   即便在一个新的事务中，也重新开启一个事务，这里的好处是，这样定义符合了sequence的意义
     * sequence 即便是执行失败了，也是需要增加的
     * NOT_SUPPORTED(4),
     * NEVER(5),
     * NESTED(6);
     *
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    String generateOrderID() {
        // 订单号16位
        StringBuilder stringBuilder = new StringBuilder();
        // 前8位为时间信息
        // 如果做归档信息，那么就可以根据订单id，的前8为小于某年某月的数据，全部归档
        LocalDateTime now = LocalDateTime.now();
        String datetime = now.format(DateTimeFormatter.ISO_DATE).replace("-", "");
        stringBuilder.append(datetime);
        // 中间6位位自增序列
        SequenceDO sequenceDO = sequenceDOMapper.getSequenceByName("order_info");
        int sequence = sequenceDO.getCurrentValue();
        int sequence_length = Integer.toString(sequence).length();
        sequenceDO.setCurrentValue(sequence + sequenceDO.getStep());
        sequenceDOMapper.updateByPrimaryKeySelective(sequenceDO);
        for (int i = sequence_length; i < 6 - sequence_length; i++) {
            stringBuilder.append(0);
        }

        stringBuilder.append(sequence);
        // 最后两位位分库分表位
        // 最后两位位分库分表位  订单的水平拆分    类似于   userid = 1000122 最后两位可以对数据库取模， 分散在不同的数据库表里，分散查询的压力
        // userId % 100 数据库表个数
        stringBuilder.append("00");
        return stringBuilder.toString();
    }

    private OrderDO convertFromOrderModel(OrderModel orderModel) {
        if (orderModel == null) {
            return null;
        }
        OrderDO orderDO = new OrderDO();
        Double itemPrice = orderModel.getItemPrice().doubleValue();
        orderDO.setItemPrice(itemPrice);
        orderDO.setOrderPrice(itemPrice * orderModel.getAmount());
        BeanUtils.copyProperties(orderModel, orderDO);
        return orderDO;
    }

}
