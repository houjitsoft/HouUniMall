package com.houjiang.payment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.payment.entity.PayConfigShopEntity;
import com.houjiang.payment.mapper.PayConfigShopMapper;
import com.houjiang.payment.service.PayConfigShopService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author Amos
 */
@Service
public class PayConfigShopServiceImpl extends ServiceImpl<PayConfigShopMapper, PayConfigShopEntity>
    implements PayConfigShopService{
    @Value(value = "${firstLevel}")
    private String firstLevel;

    @Override
    public ResponseResult select(Map<String, Object> map) {
        // 通过token获取用户信息，token是用户身份验证的关键标识
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(map.get("token").toString());
        if (null != userInfo) {
            // 根据用户所属的商店ID查询支付配置信息
            // 根据shopId查询小程序
            PayConfigShopEntity entity= this.baseMapper.selectpaymentconfig(userInfo.getShopId());
            // 返回查询到的支付配置信息，操作成功
            return ResponseResult.success(entity);
        }
        // 如果用户信息为空，表示token验证失败，返回错误信息
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);

    }

    @Override
    public ResponseResult update(Map<String, Object> map) {
        // 使用token解析出用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(map.get("token").toString());
        // 如果用户信息不为空，则进行后续处理
        if (null != userInfo) {
            // 将用户的店铺ID存入map中
            map.put("shopId", userInfo.getShopId());
            // 设置退款URL
            map.put("refundUrl", "https://api.mch.weixin.qq.com/secapi/pay/refund");
            // 设置通知URL
            map.put("notifyUrl", firstLevel + "/payment/pay/WXPay/notifyWeiXinPay");
            // 设置跳转URL
            map.put("redirectUrl", firstLevel + "/h5/#/member/orderlist/orderlist");
            // 设置小程序通知URL
            map.put("appletNotifyUrl", firstLevel + "/payment/pay/WXPay/notifyWeiXinPayByApplet");
            // 初始化一个整型变量用于后续的插入或更新操作
            Integer integer = null;
            // 判断map中是否存在id，如果存在且不为空，则进行更新操作，否则进行插入操作
            if (map.get("id") != null && map.get("id") != "") {
                integer = this.baseMapper.updatepaymentconfig(map);
            } else {
                integer = this.baseMapper.insertpaymentconfig(map);
            }
            // 如果操作结果不为空，则返回成功响应，否则返回失败响应
            if (integer != null) {
                return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
            }
            return ResponseResult.fail(BizCodeEnume.OPERATION_FAILED);
        }
        // 如果用户信息为空，则返回token非法的失败响应
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);

    }

    @Override
    public ResponseResult selectPayConfigFeign(Map<String, Object> map) {
        if(null != map && map.get("shopId") != null ){
            // 根据用户所属的商店ID查询支付配置信息
            // 根据shopId查询小程序
            PayConfigShopEntity entity= this.baseMapper.selectpaymentconfig( map.get("shopId").toString());
            // 返回查询到的支付配置信息，操作成功
            return ResponseResult.success(entity);
        }
        return ResponseResult.fail();
    }
}




