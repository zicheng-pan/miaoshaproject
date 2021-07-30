package com.example.miaoshaproject.controller;

import com.example.miaoshaproject.controller.viewobject.ItemVO;
import com.example.miaoshaproject.error.BusinessException;
import com.example.miaoshaproject.response.CommonRetureType;
import com.example.miaoshaproject.service.ItemService;
import com.example.miaoshaproject.service.model.ItemModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Controller("item")

@CrossOrigin(allowCredentials="true",allowedHeaders="*")
@RequestMapping("/item")
public class ItemController extends BaseController {

    @Autowired
    ItemService itemService;

    @ResponseBody
    @RequestMapping(value = "/create", method = RequestMethod.POST, consumes = CONTENT_TYPE_FORMED)
    public CommonRetureType createItem(
            @RequestParam(name = "title") String title,
            @RequestParam(name = "description") String description,
            @RequestParam(name = "price") BigDecimal price,
            @RequestParam(name = "stock") Integer stock,
            @RequestParam(name = "imgUrl") String imgUrl
    ) throws BusinessException {
        ItemModel itemModel = new ItemModel();
        itemModel.setStock(stock);
        itemModel.setPrice(price);
        itemModel.setDescription(description);
        itemModel.setImgUrl(imgUrl);
        itemModel.setTitle(title);
        ItemModel result = itemService.createItem(itemModel);
        ItemVO itemVO = convertFromItemModel(result);

        return CommonRetureType.create(itemVO);
    }

    private ItemVO convertFromItemModel(ItemModel result) {
        ItemVO itemVO = new ItemVO();
        BeanUtils.copyProperties(result, itemVO);
        return itemVO;
    }

    @ResponseBody
    @RequestMapping(value = "/get", method = {RequestMethod.GET})
    public CommonRetureType getItem(@RequestParam(name = "id") Integer id) {
        ItemVO itemVO = convertFromItemModel(itemService.getItemById(id));
        return CommonRetureType.create(itemVO);
    }

    @ResponseBody
    @RequestMapping(value = "/list", method = {RequestMethod.GET})
    public CommonRetureType listItem() {
        List<ItemVO> collect = itemService.listItem().stream().map(itemModel -> {
            ItemVO itemVO = convertFromItemModel(itemModel);
            return itemVO;
        }).collect(Collectors.toList());
        return CommonRetureType.create(collect);
    }
}
