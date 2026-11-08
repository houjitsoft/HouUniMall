package com.houjiang.base.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.base.entity.BasePlatformBrankEntity;
import com.houjiang.base.mapper.BasePlatformBrankMapper;
import com.houjiang.base.service.BasePlatformBrankService;
import com.houjiang.commons.dto.base.BasePlatformBrankDTO;
import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.user.UserInfoVO;
import org.springframework.stereotype.Service;

import java.util.Base64;

/**
 * @ClassName BasePlatformBrankServiceImpl
 * @Description  平台收款银行卡表
 * @Author Amos
 * @Date 2023-07-03 09:56:25
 * @Version 1.0
 * @author Amos
 */
@Service
public class BasePlatformBrankServiceImpl extends ServiceImpl<BasePlatformBrankMapper, BasePlatformBrankEntity>
        implements BasePlatformBrankService {


    /**
     * 根据用户令牌查询银行卡信息。
     *
     * @param param 用户令牌信息。
     * @return 银行卡信息的响应结果。
     */
    @Override
    public ResponseResult queryBrank(UserTokenBeanDTO param) {
        // 通过mapper查询银行卡信息
        BasePlatformBrankEntity platformBrankPo = this.baseMapper.queryBrank();
        // 返回查询结果
        return ResponseResult.success(platformBrankPo);
    }

    /**
     * 更新银行卡信息。
     *
     * @param basePlatformBrankDto 银行卡信息 DTO。
     * @return 更新操作的响应结果。
     */
    @Override
    public ResponseResult updateBrank(BasePlatformBrankDTO basePlatformBrankDto) {
        // 通过令牌获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(basePlatformBrankDto.getToken());
        // 如果用户信息有效
        if (null != userInfo) {
            // 插入银行卡信息到数据库
            // 更新或添加银行卡信息
            this.baseMapper.insertBrank(basePlatformBrankDto);
            // 返回成功响应，携带操作结果信息
            return ResponseResult.success(1,"操作成功");
        }
        // 如果用户信息无效，返回失败响应
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }

    /**
     * 查询加密的银行卡信息。
     *
     * @param param 用户令牌信息。
     * @return 加密后的银行卡信息的响应结果。
     */
    @Override
    public ResponseResult queryBrankEncryption(UserTokenBeanDTO param) {
        // 查询银行卡信息
        BasePlatformBrankEntity platformBrankPo = this.baseMapper.selectOne(new LambdaQueryWrapper<BasePlatformBrankEntity>());
        // 如果有查询结果
        String encodedString = null;
        if (null != platformBrankPo) {
            // 将银行卡信息转换为JSON字符串，并进行base64编码
            //base64加密返
            encodedString = Base64.getEncoder().encodeToString(JSONObject.toJSONString(platformBrankPo).getBytes());
            // 返回编码后的字符串
            return ResponseResult.success(encodedString);
        }
        // 如果没有查询结果，返回空成功响应
        return ResponseResult.success();
    }

    /**
     * 通过Feign调用查询银行卡信息
     * <p>
     * 该方法用于通过Feign远程调用查询银行卡信息，并返回查询结果。
     *
     * @return ResponseResult 包含查询结果的响应体
     */
    @Override
    public ResponseResult queryBrankFeign() {
        BasePlatformBrankEntity basePlatformBrankEntity = this.baseMapper.selectOne(new QueryWrapper<>());
        return ResponseResult.success(basePlatformBrankEntity);
    }

}
