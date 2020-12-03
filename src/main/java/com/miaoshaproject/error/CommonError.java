package com.miaoshaproject.error;

/**
 * @Author liuzike
 * @Date 12/2/20
 **/
public interface CommonError {
    public int getErrCode();
    public String getErrMsg();
    public CommonError setErrMsg(String errMsg);
}
