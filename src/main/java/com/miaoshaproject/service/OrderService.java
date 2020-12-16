package com.miaoshaproject.service;

import com.miaoshaproject.error.BussinessException;
import com.miaoshaproject.service.model.OrderModel;

/**
 * @Author liuzike
 * @Date 12/15/20
 **/
public interface OrderService {

    OrderModel createOrder(Integer userId, Integer ItemId, Integer amount) throws BussinessException;

    String generateOrderNo();
}
