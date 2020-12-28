package com.miaoshaproject.service;

import com.miaoshaproject.error.BussinessException;
import com.miaoshaproject.service.model.ItemModel;

import java.util.List;

/**
 * @Author liuzike
 * @Date 12/14/20
 **/
public interface ItemService {

    //创建商品
    ItemModel createItem(ItemModel itemModel) throws BussinessException;

    //商品列表浏览
    List<ItemModel> listItem();

    //商品详情浏览
    ItemModel getItemById(Integer id);

    //验证item及promo model缓存模式
    ItemModel getItemByInCache(Integer id);

    //库存扣减
    boolean decreaseStock(Integer itemId, Integer amount) throws BussinessException;

    //库存回补
    boolean increaseStock(Integer itemId, Integer amount);

    //异步更新库存
    boolean asyncDecreaseStock(Integer itemId, Integer amount);

    //商品销量增加
    void increaseSales(Integer itemId, Integer amount) throws BussinessException;

}
