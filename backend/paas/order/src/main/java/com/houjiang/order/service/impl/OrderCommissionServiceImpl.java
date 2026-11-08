package com.houjiang.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.vo.order.OrderCommissionVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.order.dto.UserFxOrderListDTO;
import com.houjiang.order.entity.OrderCommissionEntity;
import com.houjiang.order.mapper.OrderCommissionMapper;
import com.houjiang.order.service.OrderCommissionService;
import com.houjiang.order.vo.UserFxOrderGoodsVO;
import com.houjiang.order.vo.UserFxOrderListVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * @ClassName OrderCommissionServiceImpl
 * @Description
 * @Author Amos
 * @Date 2023/7/14 9:42
 * @Version 1.0
 * @author Amos
 */
@Service
public class OrderCommissionServiceImpl extends ServiceImpl<OrderCommissionMapper, OrderCommissionEntity>
        implements OrderCommissionService {

    /**
     * 查询用户分销订单列表
     *
     * @param param 包含分页信息和查询条件的参数对象
     * @return 返回查询结果的响应对象
     */
    @Override
    public ResponseResult queryUserFxOrderList(UserFxOrderListDTO param) {
        // 设置分页默认值，防止前端未传参导致NPE
        Integer page = param.getPage() != null ? param.getPage() : 1;
        Integer limit = param.getLimit() != null ? param.getLimit() : 10;
        // 初始化分页对象，使用用户的请求参数设置页码和每页数量
        Page<UserFxOrderListVO> pageObj = new Page<>(page, limit);

        // 调用底层Mapper接口查询用户外汇订单列表，传入分页对象和查询条件
        IPage<UserFxOrderListVO> result = baseMapper.queryUserFxOrderList(pageObj, param);

        // 将查询结果转换为统一的响应结果对象返回
        return getResponseResult(result);
    }

    /**
     * 查询用户分销订单商品列表
     *
     * @param param 用户外汇订单列表查询参数DTO
     * @return 返回查询结果的响应对象
     * <p>
     * 本方法用于根据用户查询其外汇订单商品的列表。通过接收一个UserFxOrderListDTO对象作为参数，
     * 实现分页查询用户外汇订单商品的信息。查询结果将被包装在一个ResponseResult对象中返回，
     * 其中包含分页信息和查询到的订单商品数据。
     */
    @Override
    public ResponseResult queryUserFxOrderGoodsList(UserFxOrderListDTO param) {
        // 设置分页默认值，防止前端未传参导致NPE
        Integer page = param.getPage() != null ? param.getPage() : 1;
        Integer limit = param.getLimit() != null ? param.getLimit() : 10;
        // 初始化分页参数
        Page<UserFxOrderGoodsVO> pageObj = new Page<>(page, limit);

        // 调用baseMapper的queryUserFxOrderGoodsList方法进行查询
        IPage<UserFxOrderGoodsVO> result = baseMapper.queryUserFxOrderGoodsList(pageObj, param);

        // 构建成功响应结果，包含分页数据
        return ResponseResult.success(PageUtils.getPage(result));
    }

    /**
     * 根据店铺ID查询用户的返现订单列表。
     * <p>
     * 该方法首先通过解析token获取用户信息，然后根据获取的店铺ID来查询用户的返现订单列表。
     * 如果token解析失败，则直接返回解析结果，否则继续查询返现订单。
     *
     * @param param 包含用户token和其它查询条件的DTO对象。
     * @return 返回查询结果的ResponseResult对象。
     */
    @Override
    public ResponseResult queryUserFxOrderListByShop(UserFxOrderListDTO param) {
        // 解析token获取用户信息
        ResponseResult rr = ParseTokenUtils.getShopUserInfo(param.getToken());

        // 检查token解析是否成功 如果获取用户信息失败，直接返回错误信息
        if (rr.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
            return rr;
        }

        // 将解析得到的用户信息转换为UserInfoVO对象
        UserInfoVO ui = JSON.parseObject(JSON.toJSONString(rr.getData()), UserInfoVO.class);

        // 设置查询参数中的店铺ID
        param.setShopId(ui.getShopId());

        // 查询用户的返现订单列表
        // 设置分页默认值，防止前端未传参导致NPE
        Integer page = param.getPage() != null ? param.getPage() : 1;
        Integer limit = param.getLimit() != null ? param.getLimit() : 10;
        // 初始化分页对象，使用用户的请求参数设置页码和每页数量
        Page<UserFxOrderListVO> pageObj = new Page<>(page, limit);

        // 调用底层Mapper接口查询用户外汇订单列表，传入分页对象和查询条件
        IPage<UserFxOrderListVO> result = baseMapper.queryUserFxOrderListByShop(pageObj, param);
        // 将查询结果转换为统一的响应结果对象返回
        return getResponseResult(result);
    }

    /**
     * 根据传入的参数查询今日分享商品。
     *
     * @param param 包含用户token、页码和每页数量等信息的请求参数。
     * @return 返回查询结果，如果用户信息不存在，则返回失败响应。
     * <p>
     * 此方法首先通过解析token获取用户信息，然后设置查询参数中的用户ID。
     * 使用分页参数构造Page对象，并调用baseMapper的selectVkTodayShareGoods方法查询今日推广商品。
     * 最后，根据查询结果返回相应的响应结果。
     */
    @Override
    public ResponseResult selectVkTodayShareGoods(UserFxOrderListDTO param) {
        // 使用token解析获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.getToken());
        if (userInfo != null) {
            // 如果用户信息存在，设置用户ID并进行查询
            param.setUserId(userInfo.getUserId());
            // 设置分页默认值，防止前端未传参导致NPE
            Integer page = param.getPage() != null ? param.getPage() : 1;
            Integer limit = param.getLimit() != null ? param.getLimit() : 10;
            Page<UserFxOrderGoodsVO> pageObj = new Page<>(page, limit);
            //  查询今日推广商品
            IPage<UserFxOrderGoodsVO> result = baseMapper.selectVkTodayShareGoods(pageObj, param);
            // 返回查询成功的响应结果
            return ResponseResult.success(PageUtils.getPage(result));
        }
        // 如果用户信息不存在，返回用户不存在的失败响应
        return ResponseResult.fail(BizCodeEnume.USER_DOES_NOT_EXIST);
    }

    /**
     * 根据参数查询VK总量订单列表。
     *
     * @param param 包含订单查询条件及分页信息的数据传输对象。
     * @return 返回查询结果的响应对象。
     */
    @Override
    public ResponseResult queryVkTotalTgOrderList(UserFxOrderListDTO param) {
        // 通过token解析获取用户信息。
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.getToken());
        // 如果用户信息存在。
        if (userInfo != null) {
            // 设置查询参数中的用户ID，用于过滤订单。
            param.setUserId(userInfo.getUserId());
            // 设置分页默认值，防止前端未传参导致NPE
            Integer page = param.getPage() != null ? param.getPage() : 1;
            Integer limit = param.getLimit() != null ? param.getLimit() : 10;
            // 创建分页对象，用于指定查询的页码和每页数量。
            Page<UserFxOrderListVO> pageObj = new Page<>(page, limit);
            // 调用基础Mapper接口查询VK总量订单列表。
            IPage<UserFxOrderListVO> result = baseMapper.queryVkTotalTgOrderList(pageObj, param);
            // 将查询结果封装成响应结果对象返回。
            return getResponseResult(result);
        }
        // 如果用户信息不存在，返回用户不存在的错误响应。
        return ResponseResult.fail(BizCodeEnume.USER_DOES_NOT_EXIST);
    }

    /**
     * 根据用户token查询VK结算订单列表。
     *
     * @param param 包含用户token、页码和每页数量的查询参数。
     * @return 返回查询结果的响应对象。
     * 如果用户信息不存在，返回用户不存在的错误响应。
     */
    @Override
    public ResponseResult selectVkSettlementOrder(UserFxOrderListDTO param) {
        // 通过token解析获取用户信息。
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.getToken());
        // 如果用户信息存在。
        if (userInfo != null) {
            // 设置查询参数中的用户ID，用于过滤订单。
            param.setUserId(userInfo.getUserId());
            // 设置分页默认值，防止前端未传参导致NPE
            Integer page = param.getPage() != null ? param.getPage() : 1;
            Integer limit = param.getLimit() != null ? param.getLimit() : 10;
            // 创建分页对象，用于指定查询的页码和每页数量。
            Page<UserFxOrderListVO> pageObj = new Page<>(page, limit);
            // 调用基础Mapper接口查询VK总量订单列表。
            IPage<UserFxOrderListVO> result = baseMapper.selectVkSettlementOrder(pageObj, param);
            // 将查询结果封装成响应结果对象返回。
            return getResponseResult(result);
        }
        // 如果用户信息不存在，返回用户不存在的错误响应。
        return ResponseResult.fail(BizCodeEnume.USER_DOES_NOT_EXIST);
    }

    /**
     * 查询用户预估收益
     */
    @Override
    public ResponseResult selectVkEstimated(UserFxOrderListDTO param) {
        // 通过token解析获取用户信息。
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.getToken());
        // 如果用户信息存在。
        if (userInfo != null) {
            // 设置查询参数中的用户ID，用于过滤订单。
            param.setUserId(userInfo.getUserId());
            // 设置分页默认值，防止前端未传参导致NPE
            Integer page = param.getPage() != null ? param.getPage() : 1;
            Integer limit = param.getLimit() != null ? param.getLimit() : 10;
            // 创建分页对象，用于指定查询的页码和每页数量。
            Page<UserFxOrderListVO> pageObj = new Page<>(page, limit);
            // 调用基础Mapper接口查询VK总量订单列表。
            IPage<UserFxOrderListVO> result = baseMapper.selectVkEstimated(pageObj, param);
            // 将查询结果封装成响应结果对象返回。
            return getResponseResult(result);
        }
        // 如果用户信息不存在，返回用户不存在的错误响应。
        return ResponseResult.fail(BizCodeEnume.USER_DOES_NOT_EXIST);
    }

    @Override
    public void updateOrderCommissionState(String orderId) {
        this.baseMapper.updateOrderCommissionState(orderId);
    }

    /**
     * 查询当前登录用户的佣金记录（BFF 层 queryCommissionList 对应实现）
     *
     * <p>实现说明：</p>
     * <ol>
     *   <li>通过 token 解析当前用户信息，没有登录则返回错误。</li>
     *   <li>基于 order_commission 表，按 shareUserId = 当前用户查询，支持分页和 shopId 筛选。</li>
     *   <li>以 MyBatis-Plus Page 查询直接返回分页结果。</li>
     * </ol>
     *
     * @param param 查询参数 DTO（包含 token、分页、可选筛选条件）
     * @return 分页结果；无登录或 token 失效时返回 USER_DOES_NOT_EXIST
     */
    @Override
    public ResponseResult queryCommissionList(UserFxOrderListDTO param) {
        // 1. 解析 token 获取用户信息（与 queryVkTotalTgOrderList 保持一致的鉴权方式）
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.getToken());
        if (userInfo == null) {
            return ResponseResult.fail(BizCodeEnume.USER_DOES_NOT_EXIST);
        }

        // 2. 构造查询条件：当前用户作为分享人，满足 BFF 层「佣金记录」语义
        String currentUserId = userInfo.getUserId();
        Integer shareUserId = null;
        try {
            shareUserId = Integer.valueOf(currentUserId);
        } catch (NumberFormatException ignored) {
            // userId 非数字时，设置分页默认值
            Integer page = param.getPage() != null ? param.getPage() : 1;
            Integer limit = param.getLimit() != null ? param.getLimit() : 10;
            // 直接返回空分页，避免污染数据
            return ResponseResult.success(PageUtils.getPage(new Page<>(page, limit)));
        }

        LambdaQueryWrapper<OrderCommissionEntity> qw = new LambdaQueryWrapper<>();
        qw.eq(OrderCommissionEntity::getShareUserId, shareUserId);
        if (param.getShopId() != null) {
            qw.eq(OrderCommissionEntity::getShopId, param.getShopId());
        }
        if (param.getOrderId() != null && !param.getOrderId().isEmpty()) {
            qw.eq(OrderCommissionEntity::getOrderId, param.getOrderId());
        }
        qw.orderByDesc(OrderCommissionEntity::getOrderCommissionId);

        // 3. 分页查询并返回
        // 设置分页默认值，防止前端未传参导致NPE
        Integer page = param.getPage() != null ? param.getPage() : 1;
        Integer limit = param.getLimit() != null ? param.getLimit() : 10;
        Page<OrderCommissionEntity> pageObj = new Page<>(page, limit);
        IPage<OrderCommissionEntity> result = this.baseMapper.selectPage(pageObj, qw);
        return ResponseResult.success(PageUtils.getPage(result));
    }

    @Override
    public OrderCommissionVO selectOrderCommission(String orderId) {
        OrderCommissionVO orderCommission = this.baseMapper.selectOrderCommission(orderId);
        return orderCommission;
    }

    /**
     * 根据查询结果，计算并设置每个订单的详细信息。
     *
     * @param result 分页查询结果，包含用户外汇订单列表。
     * @return 处理后的响应结果。
     */
    private ResponseResult getResponseResult(IPage<UserFxOrderListVO> result) {
        // 遍历查询结果中的每个订单
        for (UserFxOrderListVO vo : result.getRecords()) {
            // 初始化订单金额、运费、退款金额为0
            BigDecimal orderAmount = new BigDecimal(0);
            BigDecimal totalShippingFee = new BigDecimal(0);
            BigDecimal totalReturnPrice = new BigDecimal(0);

            // 根据订单ID查询订单商品详情
            List<UserFxOrderGoodsVO> goodsList = baseMapper.queryUserShareGoods(vo.getOrderId());

            // 遍历订单商品列表，计算订单金额、运费、退款金额，并判断订单是否涉及退款
            for (UserFxOrderGoodsVO userFxOrderGoodsVO : goodsList) {
                totalShippingFee = totalShippingFee.add(userFxOrderGoodsVO.getShippingFee());
                totalReturnPrice = totalReturnPrice.add(userFxOrderGoodsVO.getReturnPrice());
                orderAmount = orderAmount.add(userFxOrderGoodsVO.getOrderGoodsAmount());

                // 如果订单商品存在已退款或已退货的情况，则设置订单为已退款状态
                if (2 == userFxOrderGoodsVO.getGoodsRefundStatus() || 2 == userFxOrderGoodsVO.getGoodsReturnStatus()) {
                    vo.setRefund(true);
                }else{
                    userFxOrderGoodsVO.setOrderRefundNum(0);
                }
            }

            // 设置订单商品列表、订单金额、运费、退款金额到订单详情中
            vo.setGoodsList(goodsList);
            vo.setOrderAmount(orderAmount);
            vo.setTotalShippingFee(totalShippingFee);
            vo.setTotalReturnPrice(totalReturnPrice);
        }

        // 返回处理后的响应结果，包含分页信息
        return ResponseResult.success(PageUtils.getPage(result));
    }


}
