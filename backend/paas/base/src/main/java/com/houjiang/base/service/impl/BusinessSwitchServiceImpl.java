package com.houjiang.base.service.impl;


import com.houjiang.base.mapper.BusinessSwitchMapper;
import com.houjiang.base.service.BusinessSwitchService;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ResponseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
/**
 * @ClassName BusinessSwitchServiceImpl
 * @Description
 * @Author Amos
 * @Date 2023-06-13 09:05:20
 * @Version 1.0
 * @author Amos
 */
@Service
public class BusinessSwitchServiceImpl implements BusinessSwitchService {

    private static final Logger LOG = LoggerFactory.getLogger(BusinessSwitchServiceImpl.class);

    @Autowired
    private BusinessSwitchMapper businessSwitchMapper;


    /**
     * 根据传入的参数查询业务开关信息。
     * @param param 查询参数，以键值对形式传入。
     * @return 查询结果的响应对象。
     */
    @Override
    public ResponseResult query(Map<String, Object> param) {
        ResponseResult resp = new ResponseResult();
        try {
            // 通过mapper查询业务开关信息
            Integer result = businessSwitchMapper.query(param);
            // 返回查询成功的响应结果，包含业务开关信息
            return ResponseResult.success(CommonStatusEnum.SUCCESS.getCode(), BizCodeEnume.OPERATION_SUCCESSFUL.getMsg(), result);
        } catch (Exception e) {
            // 捕获异常，打印堆栈跟踪，并返回系统错误的响应结果
            e.printStackTrace();
            return ResponseResult.fail(BizCodeEnume.SYSTEM_ERROR.getCode(), BizCodeEnume.SYSTEM_ERROR.getMsg());
        }
    }

    /**
     * 根据传入的参数更新业务开关信息。
     * @param param 更新参数，以键值对形式传入。
     * @return 更新操作的响应对象。
     */
    @Override
    public ResponseResult update(Map<String, Object> param) {
        try {
            // 通过mapper更新业务开关的值
            businessSwitchMapper.updateValue(param);
            // 返回更新成功的响应结果
            return ResponseResult.success();
        } catch (Exception e) {
            // 捕获异常，打印堆栈跟踪，并返回系统错误的响应结果
            e.printStackTrace();
            return ResponseResult.fail(BizCodeEnume.SYSTEM_ERROR.getCode(), BizCodeEnume.SYSTEM_ERROR.getMsg());
        }
    }

    /**
     * 查询密码配置信息，包括密码格式和密码长度。
     * @param param 查询参数，用于传递配置项的键值对。
     * @return 包含密码格式和长度信息的响应对象。
     */
    @Override
    public ResponseResult queryPwdConfig(Map<String, Object> param) {
        try {
            // 查询密码格式
            param.put("configKey", "password_format");
            String passwordFormat = businessSwitchMapper.queryValue(param);
            // 查询密码长度
            param.put("configKey", "password_length");
            String passwordLength = businessSwitchMapper.queryValue(param);
            // 清空参数并重新设置为查询结果
            param.clear();
            param.put("passwordLength", passwordLength);
            param.put("passwordFormat", passwordFormat);
            // 返回查询成功的响应结果，包含密码格式和长度信息
            return ResponseResult.success(param);
        } catch (Exception e) {
            // 捕获异常，打印堆栈跟踪，并返回系统错误的响应结果
            e.printStackTrace();
            return ResponseResult.fail(BizCodeEnume.SYSTEM_ERROR.getCode(), BizCodeEnume.SYSTEM_ERROR.getMsg());
        }
    }

    /**
     * 更新密码配置信息，包括密码格式和密码长度。
     * @param param 更新参数，包含密码格式和长度信息。
     * @return 更新操作的响应对象。
     */
    @Override
    public ResponseResult updatePwdConfig(Map<String, Object> param) {
        try {
            // 更新密码格式
            param.put("configKey", "password_format");
            param.put("configValue", param.get("passwordFormat"));
            businessSwitchMapper.updateValue(param);
            // 更新密码长度
            param.put("configKey", "password_length");
            param.put("configValue", param.get("passwordLength"));
            businessSwitchMapper.updateValue(param);
            // 返回更新成功的响应结果
            return ResponseResult.success();
        } catch (Exception e) {
            // 捕获异常，打印堆栈跟踪，并返回系统错误的响应结果
            e.printStackTrace();
            return ResponseResult.fail(BizCodeEnume.SYSTEM_ERROR.getCode(),BizCodeEnume.SYSTEM_ERROR.getMsg());
        }
    }
}
