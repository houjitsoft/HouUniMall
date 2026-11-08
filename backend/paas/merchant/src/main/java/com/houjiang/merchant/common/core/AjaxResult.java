package com.houjiang.merchant.common.core;

import com.houjiang.merchant.common.enums.ErrorEnum;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Data
public class AjaxResult<T> {

    /** 状态码 **/
    private Integer code;

    /** 提示信息 **/
    private String msg;

    /** 响应数据 **/
    private T data;

    /** 是否显示错误信息 **/
    private Integer show;

    /** 无参构造 **/
    protected AjaxResult() {}

    /**
     * 带参构造
     *
     * @author Amos
     * @param code 状态码
     * @param msg 提示信息
     * @param data 响应数据
     */
    public AjaxResult(Integer code, String msg, T data, Integer show) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.show = show;
    }

    /**
     * 成功返回结果
     *
     * @author Amos
     * @return AjaxResult
     */
    public static AjaxResult<Object> success() {
        return new AjaxResult<>(ErrorEnum.SUCCESS.getCode(), ErrorEnum.SUCCESS.getMsg(), new ArrayList<>(), ErrorEnum.SHOW_MSG.getCode());
    }


    /**
     * 成功返回结果
     *
     * @author Amos
     * @return AjaxResult
     */
    public static AjaxResult<Object> successHideTips() {
        return new AjaxResult<>(ErrorEnum.SUCCESS.getCode(), ErrorEnum.SUCCESS.getMsg(), new ArrayList<>(), ErrorEnum.HIDE_MSG.getCode());
    }

    /**
     * 成功返回结果
     *
     * @author Amos
     * @param code 状态码
     * @return AjaxResult
     */
    public static AjaxResult<Object> success(Integer code) {
        return new AjaxResult<>(code, ErrorEnum.SUCCESS.getMsg(), new ArrayList<>(), ErrorEnum.SHOW_MSG.getCode());
    }

    /**
     * 成功返回结果
     *
     * @author Amos
     * @param msg 提示信息
     * @return AjaxResult
     */
    public static AjaxResult<Object> success(String msg) {
        return new AjaxResult<>(ErrorEnum.SUCCESS.getCode(), msg, new ArrayList<>(), ErrorEnum.SHOW_MSG.getCode());
    }

    /**
     * 成功返回结果
     *
     * @author Amos
     * @param msg 提示信息
     * @return AjaxResult
     */
    public static AjaxResult<Object> success(String msg, Integer show) {
        return new AjaxResult<>(ErrorEnum.SUCCESS.getCode(), msg, new ArrayList<>(), show);
    }

    /**
     * 成功返回结果
     *
     * @author Amos
     * @param data 响应数据
     * @return AjaxResult
     */
    public static <T> AjaxResult<T> success(T data) {
        return new AjaxResult<>(ErrorEnum.SUCCESS.getCode(), ErrorEnum.SUCCESS.getMsg(), data, ErrorEnum.HIDE_MSG.getCode());
    }

    /**
     * 成功返回结果
     *
     * @author Amos
     * @param code 状态码
     * @param msg 提示信息
     * @return AjaxResult
     */
    public static AjaxResult<Object> success(Integer code, String msg) {
        return new AjaxResult<>(code, msg, new ArrayList<>(), ErrorEnum.SHOW_MSG.getCode());
    }

    /**
     * 成功返回结果
     *
     * @author Amos
     * @param code 状态码
     * @param msg 提示信息
     * @return AjaxResult
     */
    public static AjaxResult<Object> success(Integer code, String msg, Integer show) {
        return new AjaxResult<>(code, msg, new ArrayList<>(), show);
    }

    public static <T>AjaxResult<T> success(Integer code, T data, Integer show) {
        return new AjaxResult<>(code, ErrorEnum.SUCCESS.getMsg(), data, show);
    }

    /**
     * 成功返回结果
     *
     * @author Amos
     * @param msg 提示信息
     * @param data 响应数据
     * @return AjaxResult
     */
    public static <T> AjaxResult <T> success(String msg, T data) {
        return new AjaxResult<>(ErrorEnum.SUCCESS.getCode(), msg, data, ErrorEnum.SHOW_MSG.getCode());
    }

    /**
     * 成功返回结果
     *
     * @author Amos
     * @param code 状态码
     * @param msg 提示信息
     * @param data 响应数据
     * @return AjaxResult
     */
    public static <T> AjaxResult<T> success(Integer code, String msg, T data) {
        return new AjaxResult<>(code, msg, data, ErrorEnum.SHOW_MSG.getCode());
    }

    /**
     * 成功返回结果
     *
     * @author Amos
     * @param code 状态码
     * @param msg 提示信息
     * @param data 响应数据
     * @return AjaxResult
     */
    public static <T> AjaxResult<T> success(Integer code, String msg, T data, Integer show) {
        return new AjaxResult<>(code, msg, data, show);
    }

    /**
     * 响应失败结果
     *
     * @author Amos
     * @param code 状态码
     * @return AjaxResult
     */
    public static AjaxResult<Object> failed(Integer code) {
        return new AjaxResult<>(code, ErrorEnum.FAILED.getMsg(), new ArrayList<>(), ErrorEnum.SHOW_MSG.getCode());
    }

    /**
     * 响应失败结果
     *
     * @author Amos
     * @param msg 提示信息
     * @return AjaxResult
     */
    public static AjaxResult<Object> failed(String msg) {
        return new AjaxResult<>(ErrorEnum.FAILED.getCode(), msg, new ArrayList<>(), ErrorEnum.SHOW_MSG.getCode());
    }

    /**
     * 响应失败结果
     *
     * @author Amos
     * @param data 响应数据
     * @return AjaxResult
     */
    public static <T> AjaxResult<T> failed(T data) {
        return new AjaxResult<T>(ErrorEnum.FAILED.getCode(), ErrorEnum.FAILED.getMsg(), data, ErrorEnum.SHOW_MSG.getCode());
    }

    /**
     * 响应失败结果
     *
     * @author Amos
     * @param code 状态码
     * @param msg 提示信息
     * @return AjaxResult
     */
    public static AjaxResult<Object> failed(Integer code, String msg) {
        return new AjaxResult<>(code, msg, new ArrayList<>(), ErrorEnum.SHOW_MSG.getCode());
    }

    /**
     * 响应失败结果
     *
     * @author Amos
     * @param code 状态码
     * @param msg 提示信息
     * @param data 响应数据
     * @return AjaxResult
     */
    public static <T> AjaxResult<T> failed(Integer code, String msg, T data) {
        return new AjaxResult<>(code, msg, data, ErrorEnum.SHOW_MSG.getCode());
    }

    /**
     * 响应失败结果
     *
     * @author Amos
     * @param code 状态码
     * @param msg 提示信息
     * @param data 响应数据
     * @return AjaxResult
     */
    public static <T> AjaxResult<T> failed(Integer code, String msg, T data, Integer show) {
        return new AjaxResult<>(code, msg, data, show);
    }

    /**
     * 将 AjaxResult 转换为 ResponseResult 格式的 Map
     * 用于 BFF 层 Feign 调用时的格式兼容
     *
     * AjaxResult 使用 code=1 表示成功，code=0 表示失败
     * ResponseResult 使用 code=200 表示成功，code=500 表示失败
     *
     * @return Map<String, Object> 包含 code、message、data 三个字段，与 ResponseResult 格式一致
     */
    public Map<String, Object> toResponseFormat() {
        Map<String, Object> map = new HashMap<>();
        map.put("code", this.code != null && this.code == 1 ? 200 : 500);
        map.put("message", this.msg);
        map.put("data", this.data);
        return map;
    }
}
