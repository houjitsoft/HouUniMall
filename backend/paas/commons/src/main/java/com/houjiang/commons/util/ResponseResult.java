package com.houjiang.commons.util;

import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.exception.RiderCodeEnume;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Amos
 * @PROJECT_NAME: UniMall
 * @DESCRIPTION:
 * @USER: Amos
 * @DATE: 2023/2/25 11:27
 */
@Data
@Accessors(chain = true)
public class ResponseResult<T> {

    /**
     * 状态码
     */
    private int code;

    /**
     * 返回内容
     */
    private String message;

    /**
     * 数据对象
     */
    private T data;


    /**
     * 成功响应的方法
     * @param <T>
     * @return
     */
    public static <T> ResponseResult<T> success(){
        return new ResponseResult<T>().setCode(CommonStatusEnum.SUCCESS.getCode()).setMessage(CommonStatusEnum.SUCCESS.getValue());
    }

    /**
     * 成功响应的方法
     * @param data
     * @param <T>
     * @return
     */
    public static <T> ResponseResult<T> success(T data){
        return new ResponseResult<T>().setCode(CommonStatusEnum.SUCCESS.getCode()).setMessage(CommonStatusEnum.SUCCESS.getValue()).setData(data);
    }
    
    /**
     * 成功响应的方法
     * @param data
     * @return
     */
    public static ResponseResult<String> successData(String data) {
        return new ResponseResult<String>().setCode(CommonStatusEnum.SUCCESS.getCode()).setMessage(CommonStatusEnum.SUCCESS.getValue()).setData(data);
    }
    
    /**
     * 失败：自定义BizCodeEnume类
     *
     * @param data
     * @param <T>
     * @return
     */
    public static <T> ResponseResult<T> success(BizCodeEnume data) {
        return new ResponseResult<T>().setCode(CommonStatusEnum.SUCCESS.getCode()).setMessage(data.getMsg());
    }
    /**
     * 成功响应的方法
     *
     * @param message
     * @param <T>
     * @return
     */
    public static <T> ResponseResult<T> success(int code, String message) {
        return new ResponseResult<T>().setCode(code).setMessage(message);
    }

    /**
     * 成功响应的方法
     *
     * @param data
     * @param <T>
     * @return
     */
    public static <T> ResponseResult<T> success(String message, T data) {
        return new ResponseResult<T>().setCode(CommonStatusEnum.SUCCESS.getCode()).setMessage(message).setData(data);
    }

    /**
     * 成功响应的方法
     *
     * @param data
     * @param <T>
     * @return
     */
    public static <T> ResponseResult<T> success(int code, String message, T data) {
        return new ResponseResult<T>().setCode(code).setMessage(message).setData(data);
    }

    /**
     * 失败响应的方法
     *
     * @param <T>
     * @return
     */
    public static <T> ResponseResult<T> fail(){
        return new ResponseResult<T>().setCode(CommonStatusEnum.FAIL.getCode()).setMessage(CommonStatusEnum.FAIL.getValue());
    }

    /**
     * 失败：自定义BizCodeEnume类
     *
     * @param data
     * @param <T>
     * @return
     */
    public static <T> ResponseResult<T> fail(BizCodeEnume data) {
        return new ResponseResult<T>().setCode(data.getCode()).setMessage(data.getMsg());
    }

    /**
     * 失败：自定义RiderCodeEnume类
     *
     * @param data
     * @param <T>
     * @return
     */
    public static <T> ResponseResult<T> fail(RiderCodeEnume data) {
        return new ResponseResult<T>().setCode(data.getCode()).setMessage(data.getMsg());
    }


    /**
     * 失败：统一的失败
     * @param data
     * @param <T>
     * @return
     */
    public static <T> ResponseResult<T> fail(T data){
        return new ResponseResult<T>().setCode(CommonStatusEnum.FAIL.getCode()).setMessage(CommonStatusEnum.FAIL.getValue()).setData(data);
    }

    /**
     * 失败：自定义失败 错误码和提示信息
     *
     * @param code
     * @param message
     * @return
     */
    public static <T> ResponseResult<T> fail(int code, String message) {
        return new ResponseResult<T>().setCode(code).setMessage(message);
    }

    /**
     * 失败：自定义、提示信息、具体错误
     *
     * @param message
     * @param data
     * @return
     */
    public static <T> ResponseResult<T> fail(String message, T data) {
        return new ResponseResult<T>().setCode(CommonStatusEnum.FAIL.getCode()).setMessage(message).setData(data);
    }

    /**
     * 失败：自定义失败 错误码、提示信息、具体错误
     *
     * @param code
     * @param message
     * @param data
     * @return
     */
    public static <T> ResponseResult<T> fail(int code, String message, T data) {
        return new ResponseResult<T>().setCode(code).setMessage(message).setData(data);
    }

    /**
     * 失败：自定义BizCodeEnume类
     *
     * @param data 返回对象
     * @param <T> 对象类型
     * @return ResponseResult<T>
     */
    public static <T> ResponseResult<T> fail(BizCodeEnume bizCodeEnume, T data) {
        return new ResponseResult<T>().setCode(bizCodeEnume.getCode()).setMessage(bizCodeEnume.getMsg()).setData(data);
    }

    /**
     * 错误响应：简化版本，使用默认错误码
     * @param message 错误信息
     * @param <T>
     * @return
     */
    public static <T> ResponseResult<T> error(String message) {
        return fail(CommonStatusEnum.FAIL.getCode(), message);
    }

    /**
     * 错误响应：自定义错误码
     * @param code 错误码
     * @param message 错误信息
     * @param <T>
     * @return
     */
    public static <T> ResponseResult<T> error(int code, String message) {
        return fail(code, message);
    }

    /**
     * 错误响应：带数据
     * @param message 错误信息
     * @param data 数据
     * @param <T>
     * @return
     */
    public static <T> ResponseResult<T> error(String message, T data) {
        return fail(message, data);
    }

    /**
     * 错误响应：自定义错误码和数据
     * @param code 错误码
     * @param message 错误信息
     * @param data 数据
     * @param <T>
     * @return
     */
    public static <T> ResponseResult<T> error(int code, String message, T data) {
        return fail(code, message, data);
    }

}
