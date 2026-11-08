package com.houjiang.payment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.RedisUtil;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.payment.dto.CheckRechargeCardDTO;
import com.houjiang.payment.entity.PayCardEntity;
import com.houjiang.payment.mapper.PayCardMapper;
import com.houjiang.payment.service.PayCardService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Amos
 */
@Service
public class PayCardServiceImpl extends ServiceImpl<PayCardMapper, PayCardEntity>
    implements PayCardService{

    @Override
    public ResponseResult query(CheckRechargeCardDTO param) {
        List<PayCardEntity> res = null;
        Map<Object, Object> map=new HashMap<>();
        if (RedisUtil.exists(HouConfig.HOU_PLATFORM_TOKEN_USER + param.getToken())) {
            // 根据参数查询支付卡信息
            res = this.baseMapper.query(param);
            // 将查询结果及结果数量放入map中
            map.put("result",res);
            map.put("count",res.size());
        } else {
            // 如果用户token不存在，返回权限不足的错误信息
            return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
        }
        // 返回查询成功的信息，并包含查询结果及数量
        return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL.getMsg(),map);
    }
}




