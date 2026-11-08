package com.houjiang.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.json.JsonUtils;
import com.houjiang.commons.util.MathUtil;
import com.houjiang.commons.util.RedisUtil;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.order.dto.PaymentVocherDTO;
import com.houjiang.order.entity.OrderBaseEntity;
import com.houjiang.order.entity.OrderPaymentVocherEntity;
import com.houjiang.order.feign.BaseServiceClient;
import com.houjiang.order.mapper.OrderPaymentVocherMapper;
import com.houjiang.order.service.OrderBaseService;
import com.houjiang.order.service.OrderPaymentVocherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Amos
 */
@Service
public class OrderPaymentVocherServiceImpl extends ServiceImpl<OrderPaymentVocherMapper, OrderPaymentVocherEntity>
    implements OrderPaymentVocherService{

    @Autowired
    private OrderBaseService orderBaseService;
    @Autowired
    private BaseServiceClient baseServiceClient;

    @Override
    public ResponseResult insertPaymentVocher(PaymentVocherDTO param) {
        String token = param.getToken();
        boolean isExist = RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + token);
        if (isExist) {
            //是数字订单 是order Id
            if (MathUtil.isNumeric(param.getOrderId())) {
                // 获取表里有没有数据
                OrderPaymentVocherEntity opaymentVocher = this.baseMapper.selectOne(new LambdaQueryWrapper<OrderPaymentVocherEntity>()
                        .eq(OrderPaymentVocherEntity::getOrderId, param.getOrderId()));
                if (null != opaymentVocher) {
                    // 获取前端穿的图片数组
                    List<String> imgs = param.getOrderPaymentVoucherImgs();
                    // 判断图片数量
                    if (param.getOrderPaymentVoucherImgs().size() > 5) {
                        return ResponseResult.fail(BizCodeEnume.UPLOAD_UP_TO_5_PHOTOS);
                    } else {
                        String s = JsonUtils.toString(imgs);
                        param.setJsonImgs(s);
                        Integer integer = this.baseMapper.updatePaymentVocher(param);
                        if (integer > 0) {
                            return ResponseResult.success(CommonStatusEnum.SUCCESS.getCode(), BizCodeEnume.UPLOAD_SUCCESS.getMsg(), integer);
                        }
                    }
                } else {
                    if (param.getOrderPaymentVoucherImgs().size() > 5) {
                        return ResponseResult.fail(BizCodeEnume.UPLOAD_UP_TO_5_PHOTOS);
                    }
                    String jsonImgs = JsonUtils.toString(param.getOrderPaymentVoucherImgs());
                    param.setJsonImgs(jsonImgs);
                    Integer integer = this.baseMapper.insertPaymentVocher(param);
                    if (integer > 0) {
                        return ResponseResult.success(BizCodeEnume.UPLOAD_SUCCESS.getMsg());
                    }
                }
                sendMessage(param.getOrderId());
                //不是数字订单 payNum
            } else {
                String orderId = orderBaseService.getOne(new LambdaQueryWrapper<OrderBaseEntity>()
                        .eq(OrderBaseEntity::getPaymentNumber, param.getOrderId()).last("limit 1")).getOrderId();
                sendMessage(orderId);
                param.setOrderId(orderId);
                String jsonImgs = JsonUtils.toString(param.getOrderPaymentVoucherImgs());
                param.setJsonImgs(jsonImgs);
                Integer integer = this.baseMapper.insertPaymentVocher(param);
                if (integer > 0) {
                    return ResponseResult.success(BizCodeEnume.UPLOAD_SUCCESS.getMsg());
                }
            }
        } else {
            return ResponseResult.success(BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }
        return ResponseResult.success();
    }

    @Override
    public ResponseResult querkVocherImages(PaymentVocherDTO paymentVocherDTO) {
        // 检查用户令牌是否存在于Redis中，以确定用户身份的有效性
        boolean isExist = RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + paymentVocherDTO.getToken());
        if (isExist) {
            // 根据订单号查询已存在的支付凭证，以防止重复创建
            OrderPaymentVocherEntity paymentVocherPo = this.baseMapper.selectOne(new LambdaQueryWrapper<OrderPaymentVocherEntity>()
                    .eq(OrderPaymentVocherEntity::getOrderId, paymentVocherDTO.getOrderId()));
            if (null != paymentVocherPo) {
                // 如果支付凭证存在，则直接返回成功响应及其信息
                return ResponseResult.success(paymentVocherPo);
            } else {
                // 如果支付凭证不存在，则创建一个新的支付凭证，设置为未审核状态，并返回成功响应
                paymentVocherPo = new OrderPaymentVocherEntity();
                paymentVocherPo.setReviewStatus(0);
                return ResponseResult.success(BizCodeEnume.NO_AUDIT_INFORMATION.getMsg(), paymentVocherPo);
            }
        }
        // 如果用户令牌不存在于Redis中，则表示令牌非法，返回失败响应
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);

    }
    /**
     * 订单支付（BFF 层 payOrder 对应实现）
     *
     * <p>实现说明：</p>
     * <ol>
     *   <li>校验 token 有效性；</li>
     *   <li>校验 orderId 有效性与订单存在性；</li>
     *   <li>校验订单状态必须为待付款（1），非待付款不允许发起支付；</li>
     *   <li>返回供 payment 服务使用的支付参数（orderId / payableAmount / shopId / paymentNumber / payCode 等）。</li>
     * </ol>
     *
     * @param param 支付入参，至少包含 orderId、token
     * @return 包含支付展开参数的统一响应
     */
    @Override
    public ResponseResult payOrder(Map<String, Object> param) {
        // 1. 校验 token
        Object tokenObj = param.get("token");
        if (tokenObj == null || tokenObj.toString().isEmpty()
                || !RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + tokenObj.toString())) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
        }

        // 2. 校验 orderId
        Object orderIdObj = param.get("orderId");
        if (orderIdObj == null || orderIdObj.toString().isEmpty()) {
            return ResponseResult.fail(400, "orderId 不能为空");
        }
        String orderId = orderIdObj.toString();

        // 3. 校验订单存在性
        OrderBaseEntity order = orderBaseService.getOne(new LambdaQueryWrapper<OrderBaseEntity>()
                .eq(OrderBaseEntity::getOrderId, orderId)
                .last("limit 1"));
        if (order == null) {
            return ResponseResult.fail(404, "订单不存在");
        }
        // 4. 校验订单状态：仅待付款（1）的订单允许发起支付
        if (order.getOrderStatus() == null || order.getOrderStatus().intValue() != 1) {
            return ResponseResult.fail(409, "当前订单状态不允许支付");
        }

        // 5. 组装返回 payment 服务所需支付参数
        Map<String, Object> payParam = new HashMap<>();
        payParam.put("orderId", order.getOrderId());
        payParam.put("shopId", order.getShopId());
        payParam.put("buyerUserId", order.getBuyerUserId());
        payParam.put("payableAmount", order.getOrderPaymentAmount());
        payParam.put("paymentNumber", order.getPaymentNumber());
        // 优先使用入参指定的 payCode（前端选定的支付方式），次优选用订单上已有的值
        Object inputPayCode = param.get("payCode");
        payParam.put("payCode", inputPayCode != null ? inputPayCode : order.getPayCode());
        payParam.put("clientType", param.get("clientType"));
        payParam.put("orderFrom", order.getOrderFrom());

        return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL.getMsg(), payParam);
    }

    private void sendMessage(String orderId) {
        // 发送消息模板
        Map<String, Object> messageMap = new HashMap<>();
        List<String> messageList = new ArrayList<>();
        messageList.add(orderId);
        messageMap.put("list", messageList);
        messageMap.put("orderId", orderId);
        messageMap.put("templateId", CommonType.messageTemplate.CODE_L.getCode());
        baseServiceClient.sendMessageTemplate(messageMap);
    }
}




