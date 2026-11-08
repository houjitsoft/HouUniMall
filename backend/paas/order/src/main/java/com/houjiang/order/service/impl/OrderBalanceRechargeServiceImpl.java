package com.houjiang.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.DateUtils;
import com.houjiang.commons.util.ParseMapUtils;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.order.dto.OrderBalanceRechargeDTO;
import com.houjiang.order.feign.PayMentServiceClient;
import com.houjiang.order.mapper.OrderBalanceRechargeMapper;
import com.houjiang.order.entity.OrderBalanceRechargeEntity;
import com.houjiang.order.mapper.OrderReturnMapper;
import com.houjiang.order.service.OrderBalanceRechargeService;
import com.houjiang.commons.vo.order.OrderBalanceRechargeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName OrderBalanceRechargeServiceImpl
 * @Description  余额充值订单表
 * @Author Amos
 * @Date 2024-09-19 09:35:39
 * @Version 1.0
 * @author Amos
 */
@Service
public class OrderBalanceRechargeServiceImpl extends ServiceImpl<OrderBalanceRechargeMapper, OrderBalanceRechargeEntity>
        implements OrderBalanceRechargeService {

    @Autowired
    private PayMentServiceClient payMentServiceClient;
    @Resource
    private OrderReturnMapper orderReturnMapper;




    /**
     * 创建余额充值订单
     *
     * @param orderBalanceRechargeDTO 订单信息，包括令牌和充值金额等
     * @return 返回操作结果，包括充值订单号
     */
    @Override
    public ResponseResult createBalanceRechargeOrder(OrderBalanceRechargeDTO orderBalanceRechargeDTO) {
        // 通过令牌获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(orderBalanceRechargeDTO.getToken());
        // 如果用户信息为空，表示令牌无效，返回错误响应
        if(null == userInfo){
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(),BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }
        // 校验充值金额是否有效，如果小于等于0则返回错误响应
        if(orderBalanceRechargeDTO.getRechargeAmount() != null && orderBalanceRechargeDTO.getRechargeAmount().compareTo(BigDecimal.ZERO) <= 0){
            return ResponseResult.fail(BizCodeEnume.ORDER_PRICE_IS_ERROR.getCode(),BizCodeEnume.ORDER_PRICE_IS_ERROR.getMsg());
        }
        // 生成充值单号
        String orderNo = "YE"+ DateUtils.getDateRandomSix();
        // 构建充值订单实体
        OrderBalanceRechargeEntity orderBalanceRechargeEntity = new OrderBalanceRechargeEntity();
        orderBalanceRechargeEntity.setRechargeOrderId(orderNo);
        orderBalanceRechargeEntity.setUserId(userInfo.getUserId());
        orderBalanceRechargeEntity.setUserMobile(userInfo.getUserMobile());
        orderBalanceRechargeEntity.setUserName(userInfo.getUserName());
        orderBalanceRechargeEntity.setRechargeAmount(orderBalanceRechargeDTO.getRechargeAmount());
        // 设置订单初始状态为等待支付
        orderBalanceRechargeEntity.setRechargeOrderStart(CommonType.balanceRechargeOrderStart.WAITING_PAY.getCode());
        // 保存订单信息到数据库
        this.baseMapper.createBalanceRechargeOrder(orderBalanceRechargeEntity);
        if(CommonType.balanceRechargeOrderStart.OFFLINE_PAY.getCode().equals(orderBalanceRechargeDTO.getPayCode())){
        // 线下支付 插入用户收支记录
            // 初始化一个Map用于存储资产详情的各项信息
            Map<String, Object> assetDetailMap = new HashMap<>();
            // 插入我的余额明细表
            assetDetailMap.put("orderId", orderNo);
            assetDetailMap.put("detailType", CommonType.detailType.BALANCE_RECHARGE.getCode());
            assetDetailMap.put("assetType", CommonType.assetType.INCOME.getCode());
            assetDetailMap.put("assetAmount", orderBalanceRechargeDTO.getRechargeAmount());
            assetDetailMap.put("assetDirection", CommonType.balanceRechargeOrderStart.OFFLINE_PAY.getDesc());
            assetDetailMap.put("payType", CommonType.payType.PAY_TYPE_BALANCE.getCode());
            assetDetailMap.put("userId", userInfo.getUserId());
            // 插入资产详情信息到数据库
            orderReturnMapper.insertDetail(assetDetailMap);
        }
        // 返回充值订单号
        return ResponseResult.success(orderNo);
    }

    /**
     * 更新余额充值订单
     *
     * 该方法通过调用baseMapper中的updateBalanceRechargeOrder方法来更新数据库中的余额充值订单信息
     * 它使用传入的map对象作为更新条件和内容
     *
     * @param map 包含更新条件和新值的键值对映射
     * @return 如果更新成功，则返回成功响应；如果更新失败，则返回失败响应
     */
    @Override
    public ResponseResult updateBalanceRechargeOrder(Map<Object, Object> map) {
        // 执行更新操作，返回受影响的行数
        Integer num = this.baseMapper.updateBalanceRechargeOrder(map);
        //  插入用户收支记录
        // 初始化一个Map用于存储资产详情的各项信息
        Map<String, Object> assetDetailMap = new HashMap<>();

        OrderBalanceRechargeEntity orderBalanceRecharge = this.baseMapper.selectOne(new LambdaQueryWrapper<OrderBalanceRechargeEntity>()
                .eq(OrderBalanceRechargeEntity::getRechargeOrderId,map.get("rechargeOrderId").toString()));

        // 插入我的余额明细表
        assetDetailMap.put("orderId", orderBalanceRecharge.getRechargeOrderId());
        assetDetailMap.put("assetType", CommonType.assetType.INCOME.getCode());
        assetDetailMap.put("assetAmount", orderBalanceRecharge.getRechargeAmount());
        assetDetailMap.put("detailType", CommonType.detailType.BALANCE_RECHARGE.getCode());
        assetDetailMap.put("payType", CommonType.payType.PAY_TYPE_BALANCE.getCode());
        assetDetailMap.put("userId", orderBalanceRecharge.getUserId());
        if(CommonType.balanceRechargeOrderStart.ALI_PAY.getCode().equals(Integer.valueOf(map.get("payCode").toString()))){
            assetDetailMap.put("assetDirection", CommonType.balanceRechargeOrderStart.ALI_PAY.getDesc());
        }else if(CommonType.balanceRechargeOrderStart.WX_PAY.getCode().equals(Integer.valueOf(map.get("payCode").toString()))){
            assetDetailMap.put("assetDirection", CommonType.balanceRechargeOrderStart.WX_PAY.getDesc());
        }else{
            assetDetailMap.put("assetDirection", CommonType.balanceRechargeOrderStart.OFFLINE_PAY.getDesc());
        }
        // 插入资产详情信息到数据库
        orderReturnMapper.insertDetail(assetDetailMap);
        if(!Integer.valueOf(map.get("payCode").toString()).equals(CommonType.balanceRechargeOrderStart.OFFLINE_PAY.getCode())){
            // 修改个人用户的余额
            this.payMentServiceClient.updateUserBalance(orderBalanceRecharge.getUserId(),orderBalanceRecharge.getRechargeAmount());
        }
        // 检查更新是否成功
        if(num >= 0){
            // 如果更新成功，返回成功响应
            return ResponseResult.success();
        }else{
            // 如果更新失败，返回失败响应
            return ResponseResult.fail();
        }
    }

    /**
     * 查询离线充值订单
     *
     * @param orderBalanceRechargeDTO 请求参数对象，包含令牌等信息
     * @return 响应结果，包含充值订单信息的分页数据
     */
    @Override
    public ResponseResult queryOfflineRechargeOrder(OrderBalanceRechargeDTO orderBalanceRechargeDTO) {
        // 通过令牌获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(orderBalanceRechargeDTO.getToken());
        // 如果用户信息为空，表示令牌无效，返回错误响应
        if(null == userInfo){
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(),BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }
        Map data = null;
        try {
            // 将请求参数对象转换为Map
            data = ParseMapUtils.beanToMap(orderBalanceRechargeDTO);
        } catch (IllegalAccessException e) {
            // 处理转换过程中的异常
            throw new RuntimeException(e);
        }
        // 调用Mapper查询离线充值订单
        IPage<OrderBalanceRechargeVO> page = this.baseMapper.queryOfflineRechargeOrder(new Query<OrderBalanceRechargeVO>().getPage(data),data);

        // 返回成功响应，携带分页数据
        return ResponseResult.success(PageUtils.getPage(page));
    }

    @Override
    public ResponseResult auditOfflineRechargeOrder(OrderBalanceRechargeDTO orderBalanceRechargeDTO) {
        // 通过令牌获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(orderBalanceRechargeDTO.getToken());
        // 如果用户信息为空，表示令牌无效，返回错误响应
        if(null == userInfo){
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(),BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }
        OrderBalanceRechargeEntity orderBalanceRechargeEntity = new OrderBalanceRechargeEntity();
        orderBalanceRechargeEntity.setRechargeOrderId(orderBalanceRechargeDTO.getRechargeOrderId());
        orderBalanceRechargeEntity.setRechargeMsg(orderBalanceRechargeDTO.getRechargeMsg());
        if(CommonType.isApproved.APPROVED.getCode().equals(orderBalanceRechargeDTO.getRechargeType())){
            orderBalanceRechargeEntity.setRechargeOrderStart(CommonType.balanceRechargeOrderStart.FINISH.getCode());
            OrderBalanceRechargeEntity orderBalanceRecharge = this.baseMapper.selectOne(new LambdaQueryWrapper<OrderBalanceRechargeEntity>()
                    .eq(OrderBalanceRechargeEntity::getRechargeOrderId,orderBalanceRechargeDTO.getRechargeOrderId()));
            // 修改个人用户的余额
            this.payMentServiceClient.updateUserBalance(orderBalanceRecharge.getUserId(),orderBalanceRecharge.getRechargeAmount());
        }else{
            orderBalanceRechargeEntity.setRechargeOrderStart(CommonType.balanceRechargeOrderStart.AUDIT_FAIL.getCode());
        }
        // 修改充值单状态
        this.baseMapper.updateOfflineRechargeOrder(orderBalanceRechargeEntity);
        return ResponseResult.success();
    }

    @Override
    public OrderBalanceRechargeVO queryRechargeOrderById(String rechargeOrderId) {
        return this.baseMapper.queryRechargeOrderById(rechargeOrderId);
    }
}
