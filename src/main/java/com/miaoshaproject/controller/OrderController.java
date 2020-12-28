package com.miaoshaproject.controller;

import com.miaoshaproject.error.BussinessException;
import com.miaoshaproject.error.EmBussinessError;
import com.miaoshaproject.mq.MqProducer;
import com.miaoshaproject.response.CommonReturnType;
import com.miaoshaproject.service.OrderService;
import com.miaoshaproject.service.model.UserModel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author liuzike
 * @Date 12/15/20
 **/
@Controller("order")
@RequestMapping("/order")
@CrossOrigin(allowCredentials = "true", allowedHeaders = "*")
public class OrderController extends BaseController{

    @Autowired
    private OrderService orderService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MqProducer mqProducer;

    //封装下单请求
    @RequestMapping(value = "/createorder", method = {RequestMethod.POST}, consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    @ResponseBody
    public CommonReturnType createOrder(@RequestParam(name = "itemId") Integer itemId,
                                        @RequestParam(name = "amount") Integer amount,
                                        @RequestParam(name = "promoId", required = false) Integer promoId) throws BussinessException {

        //Boolean isLogin = (Boolean)httpServletRequest.getSession().getAttribute("IS_LOGIN");

        //获取用户登陆信息
        String token = httpServletRequest.getParameterMap().get("token")[0];
        if (StringUtils.isEmpty(token)) {
            throw new BussinessException(EmBussinessError.USER_NOT_LOGIN, "用户还未登入");
        }

        UserModel userModel = (UserModel)redisTemplate.opsForValue().get(token);

        if (userModel == null) {
            throw new BussinessException(EmBussinessError.USER_NOT_LOGIN, "用户还未登入");
        }

        //UserModel userModel = (UserModel)httpServletRequest.getSession().getAttribute("LOGIN_USER");

        //OrderModel orderModel = orderService.createOrder(userModel.getId(), itemId, promoId, amount);
        if (!mqProducer.transactionAsyncReduceStock(userModel.getId(), itemId, promoId, amount)) {
            throw new BussinessException(EmBussinessError.UNKNOWN_ERROR, "下单失败");
        }


        return CommonReturnType.create(null);
    }
}
