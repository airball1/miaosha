package com.miaoshaproject.controller;

import com.alibaba.druid.util.StringUtils;
import com.miaoshaproject.controller.viewobject.UserVO;
import com.miaoshaproject.error.BussinessException;
import com.miaoshaproject.error.EmBussinessError;
import com.miaoshaproject.response.CommonReturnType;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import sun.misc.BASE64Encoder;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @Author liuzike
 * @Date 12/1/20
 **/
@Controller("user")
@RequestMapping("/user")
@CrossOrigin(allowCredentials = "true", allowedHeaders = "*")
public class UserController extends BaseController{

    @Autowired
    private UserService userService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private RedisTemplate redisTemplate;


    //用户登入接口
    @RequestMapping(value = "/login", method = {RequestMethod.POST}, consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    @ResponseBody
    public CommonReturnType login(@RequestParam(name = "telphone") String telphone,
                                  @RequestParam(name = "password") String password) throws BussinessException, UnsupportedEncodingException, NoSuchAlgorithmException {

        //入参校验
        if (org.apache.commons.lang3.StringUtils.isEmpty(telphone) ||
                StringUtils.isEmpty(password)) {
            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR);
        }

        //用户登入服务，用来校验用户登入是否合法
        UserModel userModel = userService.validateLogin(telphone, this.EncodeByMd5(password));

        //生成登入凭证token, UUID
        String uuidToken  = UUID.randomUUID().toString();
        uuidToken = uuidToken.replace("-", "");

        //建立token和用户登陆态之间的联系
        redisTemplate.opsForValue().set(uuidToken, userModel);
        redisTemplate.expire(uuidToken, 1,TimeUnit.HOURS);

//        //将登入凭着加入到用户登入成功的session内
//        this.httpServletRequest.getSession().setAttribute("IS_LOGIN", true);
//        this.httpServletRequest.getSession().setAttribute("LOGIN_USER", userModel);

        //下发了token
        return CommonReturnType.create(uuidToken);
    }


    //用户注册接口
    @RequestMapping(value = "/register", method = {RequestMethod.POST}, consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    @ResponseBody
    public CommonReturnType register(@RequestParam(name = "telphone") String telphone,
                                     @RequestParam(name = "otpCode") String otpCode,
                                     @RequestParam(name = "name") String name,
                                     @RequestParam(name = "gender") Byte gender,
                                     @RequestParam(name = "age") Integer age,
                                     @RequestParam(name = "password") String password) throws BussinessException, UnsupportedEncodingException, NoSuchAlgorithmException {

        //验证手机号和对应的otpcode相符合
        String inSessionOtpCode = (String)this.httpServletRequest.getSession().getAttribute(telphone);
        if(!StringUtils.equals(otpCode, inSessionOtpCode)) {
            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR, "短信验证码不符合");
        }


        //用户的注册流程
        UserModel userModel = new UserModel();
        userModel.setName(name);
        userModel.setGender(gender);
        userModel.setAge(age);
        userModel.setTelphone(telphone);
        userModel.setRegisterMode("byphone");
        userModel.setEncrptPassword(this.EncodeByMd5(password));

        userService.register(userModel);

        return CommonReturnType.create(null);

    }

    public String EncodeByMd5(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        //确定计算方法
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        BASE64Encoder base64en = new BASE64Encoder();

        //加密字符串
        String newstr = base64en.encode(md5.digest(str.getBytes("utf-8")));

        return newstr;

    }


    @RequestMapping(value = "/getotp", method = {RequestMethod.POST}, consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    @ResponseBody
    public CommonReturnType getOtp(@RequestParam(name= "telphone") String telphone) {
        //需要按照一定的规则生成OTP验证码
        Random random = new Random();
        int randomInt = random.nextInt(99999);
        randomInt += 10000;
        String otpCode = String.valueOf(randomInt);


        //将OTP验证码同对应用户的手机号关联,使用httpsession的方式绑定他的手机号与OTPCODE
        httpServletRequest.getSession().setAttribute(telphone, otpCode);

        //将OTP验证码通过短信通道发送给用户，省略
        System.out.println("telphone = " + telphone + "& otpCode = " + otpCode);

        return CommonReturnType.create(null);

    }

    @RequestMapping("/get")
    @ResponseBody
    public CommonReturnType getUser(@RequestParam(name = "id") Integer id) throws BussinessException{
        //调用service服务获取对应id的用户对象并返回给前端
        UserModel userModel = userService.getUserById(id);

        //若获取的对应用户信息不存在
        if (userModel == null) {
            throw new BussinessException(EmBussinessError.USER_NOT_EXIST);
        }

        //将核心领域模型用户对象转化为可供UI使用的viewobject
        UserVO userVO = convertFromModel(userModel);

        //返回通用对象
        return CommonReturnType.create(userVO);

    }

    private UserVO convertFromModel(UserModel userModel) {
        if (userModel == null) {
            return null;
        }

        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userModel, userVO);

        return userVO;
    }

}
