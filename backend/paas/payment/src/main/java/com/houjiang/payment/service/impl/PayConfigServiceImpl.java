package com.houjiang.payment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.payment.entity.PayConfigEntity;
import com.houjiang.payment.mapper.PayConfigMapper;
import com.houjiang.payment.service.PayConfigService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 支付配置信息表(PayConfig)表
 * @author Amos
 */
@Service
public class PayConfigServiceImpl extends ServiceImpl<PayConfigMapper, PayConfigEntity>
        implements PayConfigService {


    /**
     * 根据支付方式查询支付配置信息。
     * 如果支付配置不存在，则尝试创建新的支付配置并返回。
     * 如果支付配置已存在，则直接返回该配置。
     *
     * @param map 包含支付方式的键值对，其中paymentMode为支付方式的关键字。
     * @return 返回查询或创建的支付配置信息。
     */
    @Override
    public ResponseResult queryFeign(Map<String, Object> map) {
        // 获取支付方式
        String paymentMode = map.get("paymentMode").toString();

        // 查询支付配置信息
        PayConfigEntity result = this.baseMapper.selectPayConfigEntity(paymentMode);

        // 如果支付配置不存在
        if (Objects.isNull(result)){
            result = new PayConfigEntity();
            // 设置默认支付状态
            result.setPaymentStatus("01");
            // 尝试插入新的支付配置
            int res = this.baseMapper.insert(result);
            // 如果插入成功
            if (res > 0) {
                // 获取新插入的支付配置ID
                int tagId = 0;
                tagId = this.baseMapper.getPayConfigID();
                // 设置支付配置ID
                result.setTagId(tagId);
                // 设置标记为新创建
                result.setFlag(0);
                // 返回成功结果
                return ResponseResult.success(result);
            } else {
                // 返回给前端的标识 0 初始化成功但无配置信息  1已经存在配置信息并返回  2初始化失败
                result.setFlag(2);
                // 返回失败结果
                return ResponseResult.fail(result);
            }
        }
        // 设置标记为已存在配置
        result.setFlag(1);
        // 返回成功结果
        return ResponseResult.success(result);
    }


    /**
     * 更新支付宝或微信的支付配置。
     *
     * @param map 包含支付方式和其他必要配置信息的映射。
     * @return 根据更新操作的结果返回不同的响应结果。
     */
    @Override
    public ResponseResult updateZFBPayConfig(Map<String, Object> map) {
        // 初始化操作结果标志
        int res = this.baseMapper.updatePayConfigALi(map);
        // 根据更新操作的结果返回相应的响应
        if (res > 0) {
            return ResponseResult.success(BizCodeEnume.UPDATE_SUCCEEDED);
        } else {
            return ResponseResult.success(BizCodeEnume.UPDATE_ERROR);
        }
    }

    /**
     * 查询支付宝支付配置信息。
     *
     * @param map 包含支付模式的键值对参数。
     * @return 返回支付配置的结果集。
     */
    @Override
    public ResponseResult queryZFBPayConfig(Map<String, Object> map) {
        // 获取支付模式
        String paymentMode = map.get("paymentMode").toString();
        // 通过支付模式查询支付配置信息
        Map<String, Object> result = this.baseMapper.selectPayConfig(paymentMode);

        if (Objects.isNull(result)){
            result = Maps.newHashMap();
            // 如果支付配置不存在，则插入新的支付信息
            int res =  this.baseMapper.insertPayInfo();
            if (res > 0) {
                // 插入成功后，获取新插入的支付配置ID
                int tagId = 0;
                tagId = this.baseMapper.getPayConfigID();
                // 将支付配置ID和标记放入结果集中
                result.put("tagId", tagId);
                result.put("flag", "0");
                // 返回成功结果，包含新的支付配置信息
                return ResponseResult.success(BizCodeEnume.OSS_STACT_SUCCESS.getCode(),BizCodeEnume.OSS_STACT_SUCCESS.getMsg(),result);
            } else {
                // 插入失败，返回失败结果
                // 返回给前端的标识 0 初始化成功但无配置信息  1已经存在配置信息并返回  2初始化失败
                result.put("flag", "2");
                return ResponseResult.success(BizCodeEnume.OSS_FAIL.getCode(),BizCodeEnume.OSS_FAIL.getMsg(),result);
            }
        }
        // 如果支付配置已存在，更新结果集标记，并返回成功结果
        result.put("flag", "1");
        return ResponseResult.success(result);
    }

    /**
     * 更新微信支付配置信息。
     *
     * 通过传入的参数更新微信支付的配置，包括但不限于appID和appSecret等关键信息。
     * 此方法首先验证用户的token有效性，只有有效的用户才能进行配置更新。
     * 如果token验证成功，则调用baseMapper的两个方法分别更新微信支付配置和微信小程序的appID和appSecret。
     * 如果token验证失败，则返回相应的错误信息。
     *
     * @param map 包含更新配置所需信息的参数映射，其中必须包含token用于用户验证。
     * @return 如果更新成功，返回成功的响应结果；如果token非法，返回相应的错误响应结果。
     */
    @Override
    public ResponseResult updateWXPayConfig(Map<String, Object> map) {
        // 通过token获取用户信息，用于验证用户身份。
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(map.get("token").toString());
        if (null != userInfo) {
            // 更新微信支付配置信息。
            this.baseMapper.updateWXPayConfig(map);
            // 更新微信小程序的appID和appSecret。
            this.baseMapper.updateWXPayConfig2(map);
            // 修改微信公众号的appID和appSecret。
            this.baseMapper.updateWXPayConfig3(map);
            // 返回更新成功的响应结果。
            return ResponseResult.success(BizCodeEnume.UPDATE_SUCCEEDED);
        }
        // 如果token验证失败，返回token非法的错误响应结果。
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }

    /**
     * 根据用户令牌查询微信支付配置信息。
     *
     * @param map 包含用户令牌的映射表。
     * @return 如果查询成功，返回包含支付配置信息的响应结果；如果查询失败或令牌无效，返回失败的响应结果。
     */
    @Override
    public ResponseResult queryWXPayConfig(Map<String, Object> map) {
        // 通过令牌获取用户信息。
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(map.get("token").toString());

        // 如果用户信息存在。
        if (null != userInfo) {
            // 查询微信支付的通用配置信息。
            Map<String, Object> result = this.baseMapper.selectPayConfig(CommonType.payStart.PAY_CONF_00.getType());
            //查询微信小程序的 appid appSecret
            Map<String, Object> result2 = this.baseMapper.selectPayConfig(CommonType.payStart.PAY_CONF_02.getType());
            // 查询下微信公帐号
            Map<String, Object> result3 = this.baseMapper.selectPayConfig(CommonType.payStart.PAY_CONF_04.getType());
            // 将小程序支付的appSecret和appId整合到通用配置信息中。
            result.put("miniAppSecret", result2.get("appSecret"));
            result.put("miniAppId", result2.get("appID"));
            // 将微信公众号的appSecret和appId整合到通用配置信息中。
            result.put("GzAppSecret", result3.get("appSecret"));
            result.put("GzAppId", result3.get("appID"));
            // 返回成功的响应结果，包含整合后的支付配置信息。
            return ResponseResult.success(result);
        }
        // 如果令牌无效，返回失败的响应结果。
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }

    /**
     * 查询提现配置信息
     *
     * 本方法通过调用底层的查询方法，获取提现配置列表，并将其封装在一个成功的响应结果中返回。
     * 这对于前端或调用方来说，提供了获取提现规则或配置的途径，有助于他们在用户提现操作时做出正确的逻辑判断。
     *
     * @return ResponseResult 包含提现配置列表的成功响应结果
     */
    @Override
    public ResponseResult queryWithdrawConfig() {
        // 通过基础Mapper查询提现配置列表
        List<BigDecimal> withdrawConfigList = this.baseMapper.queryWithdrawConfig();
        // 返回包含提现配置列表的成功响应结果
        return ResponseResult.success(withdrawConfigList);
    }

    /**
     * 删除用户交易信息。
     *
     * @param userId 用户ID，用于确定要删除交易信息的用户。
     * @return 返回操作结果，如果操作成功，则返回一个成功的响应结果。
     *
     * 本方法通过调用基本映射器（baseMapper）的三个删除方法，分别删除用户的交易账户信息、交易记录信息和交易信息。
     * 这样做的目的是为了在用户不再需要或要求删除其交易历史时，彻底从数据库中移除相关数据，保护用户隐私。
     * 方法最后返回一个表示操作成功的响应结果，表明所有相关交易数据都已成功删除。
     */
    @Override
    public ResponseResult delFxInfo(String userId) {
        // 删除用户的交易账户信息
        this.baseMapper.deletePdbFxAccount(userId);
        // 删除用户的交易记录信息
        this.baseMapper.deletePdbFxRecord(userId);
        // 删除用户的交易信息
        this.baseMapper.deletePdbFxTrade(userId);

        // 返回操作成功的结果
        return ResponseResult.success();
    }
    /**
     * 根据支付方式查询支付配置信息。
     * 如果传递了paymentMode参数，则根据paymentMode查询相应的支付配置信息并返回。
     *
     * @param map 用于传递查询参数的映射表。
     * @return 包含支付配置信息的ResponseResult对象，如果参数无效则返回失败结果。
     */
    @Override
    public ResponseResult queryPayConfigFeign(Map<String, Object> map) {
        // 检查map和paymentMode参数是否有效
        if (null != map && map.get("paymentMode") != null) {
            // 根据paymentMode查询支付配置
            Map<String, Object> result = this.baseMapper.selectPayConfig(map.get("paymentMode").toString());
            // 返回查询成功的结果
            return ResponseResult.success(result);
        }
        // 如果参数无效，返回失败结果
        return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION);
    }
}
