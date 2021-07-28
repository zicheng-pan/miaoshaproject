package com.example.miaoshaproject.service.impl;

import com.example.miaoshaproject.dao.ItemDOMapper;
import com.example.miaoshaproject.dao.ItemStockDOMapper;
import com.example.miaoshaproject.dataobject.ItemDO;
import com.example.miaoshaproject.dataobject.ItemStockDO;
import com.example.miaoshaproject.error.BusinessException;
import com.example.miaoshaproject.error.EmBusinessError;
import com.example.miaoshaproject.service.ItemService;
import com.example.miaoshaproject.service.model.ItemModel;
import com.example.miaoshaproject.validator.ValidationResult;
import com.example.miaoshaproject.validator.ValidatorImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {


    @Autowired
    ValidatorImpl validator;

    @Autowired
    ItemDOMapper itemDOMapper;

    @Autowired
    ItemStockDOMapper itemStockDOMapper;

    @Override
    @Transactional
    public ItemModel createItem(ItemModel itemModel) throws BusinessException {
        // 校验入参，入库前的最后一次校验
        ValidationResult validationResult = validator.validate(itemModel);
        if (validationResult.isHasErrors()) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, validationResult.getErrMsg());
        }

        ItemDO itemDO = convertFromItemModel(itemModel);
        if (itemDO != null) {
            int count = itemDOMapper.insertSelective(itemDO);
            itemModel.setId(itemDO.getId());
            ItemStockDO itemStockDO = converItermStockFromItemModel(itemModel);
            itemStockDOMapper.insertSelective(itemStockDO);
        }
        //讲itemModel 转成itemDO
        // 返回创建完成的对象
        return this.getItemById(itemModel.getId());
    }

    private ItemStockDO converItermStockFromItemModel(ItemModel itemModel) {
        if (itemModel == null) {
            return null;
        }
        ItemStockDO itemStockDO = new ItemStockDO();
        itemStockDO.setItemId(itemModel.getId());
        itemStockDO.setStock(itemModel.getStock());
        return itemStockDO;
    }

    private ItemDO convertFromItemModel(ItemModel itemModel) {
        if (itemModel == null) {
            return null;
        }
        ItemDO itemDO = new ItemDO();
        BeanUtils.copyProperties(itemModel, itemDO);
        // 不会拷贝类型不一样的对象，所以手动拷贝sizeprice属性
        itemDO.setPrice(itemModel.getPrice().doubleValue());
        return itemDO;
    }

    @Override
    public List<ItemModel> listItem() {
        List<ItemModel> collect = itemDOMapper.listItem().stream().map(item -> {
            ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(item.getId());
            ItemModel itemModel1 = converFromDO(item, itemStockDO);
            return itemModel1;
        }).collect(Collectors.toList());
        return collect;
    }

    @Override
    public ItemModel getItemById(Integer id) {
        ItemDO itemDO = itemDOMapper.selectByPrimaryKey(id);
        if (itemDO == null)
            return null;
        ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());
        ItemModel itemModel = converFromDO(itemDO, itemStockDO);
        return itemModel;
    }

    private ItemModel converFromDO(ItemDO itemDO, ItemStockDO itemStockDO) {
        ItemModel itemModel = new ItemModel();
        BeanUtils.copyProperties(itemDO, itemModel);
        itemModel.setPrice(new BigDecimal(itemDO.getPrice()));
        itemModel.setStock(itemStockDO.getStock());
        return itemModel;
    }
}
