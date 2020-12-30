package com.miaoshaproject.service;

import com.miaoshaproject.error.BussinessException;
import com.miaoshaproject.service.model.OrderModel;

/**
 * @Author liuzike
 * @Date 12/15/20
 **/
public interface OrderService {

    //通过前端url上传过来秒杀活动id, 然后下单接口校验对应id是否属于对应商品且活动已开始
    OrderModel createOrder(Integer userId, Integer ItemId, Integer promoId, Integer amount, String stockLogId) throws BussinessException;

    String generateOrderNo();
}
