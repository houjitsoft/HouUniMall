package com.houjiang.order.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.base.DeliveryWebConfigDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.exception.RiderCodeEnume;
import com.houjiang.commons.util.RedisUtil;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.vo.user.PlatformUserInfoVO;
import com.houjiang.commons.vo.user.ShopGateInfo;
import com.houjiang.order.dto.OrderBaseRiderDTO;
import com.houjiang.order.entity.OrderBaseEntity;
import com.houjiang.order.feign.BaseServiceClient;
import com.houjiang.order.mapper.OrderBaseMapper;
import com.houjiang.order.mapper.OrderRiderBaseMapper;
import com.houjiang.order.service.OrderRiderBaseService;
import com.houjiang.order.vo.OrderDetailGoodsItemVO;
import com.houjiang.order.vo.OrderRiderBasePageVO;
import com.houjiang.order.vo.OrderRiderBaseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName OrderRiderBaseServiceImpl
 * @Description 骑手订单接口实现
 * @Author Amos
 * @Date 2024/4/29 14:04
 * @Version 1.0
 * @author Amos
 */
@Service
public class OrderRiderBaseServiceImpl implements OrderRiderBaseService {

    @Resource
    private OrderRiderBaseMapper orderRiderBaseMapper;

    @Resource
    private OrderBaseMapper orderBaseMapper;

    @Autowired
    private BaseServiceClient baseServiceClient;

    @Override
    public ResponseResult queryMerchantConfirmationPage(OrderBaseRiderDTO param) {
        //判断token是否过期
        boolean isExist = RedisUtil.exists(HouConfig.HOU_PLATFORM_TOKEN_USER + param.getToken());
        if (!isExist) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
        }
        String data = RedisUtil.get(HouConfig.HOU_PLATFORM_TOKEN_USER + param.getToken());
        PlatformUserInfoVO platformUserInfo = JSON.parseObject(data, PlatformUserInfoVO.class);
        //获取用户di
        String userId = platformUserInfo.getUserId();
        String distance = null;
        //从获取配送费距离
        ResponseResult responseResult = baseServiceClient.deliveryDetail("qs_delivery");
        if (responseResult != null && responseResult.getCode() == CommonStatusEnum.SUCCESS.getCode()) {
            DeliveryWebConfigDTO webConfigDTO = JSON.parseObject(JSON.toJSONString(responseResult.getData()),DeliveryWebConfigDTO.class);
            distance = webConfigDTO.getDistance();
        }
        //分页查询订单管理列表-商家确认
        Page<OrderRiderBasePageVO> page = new Page<>(param.getPage(),param.getLimit());
        IPage<OrderRiderBasePageVO> result = orderRiderBaseMapper.queryMerchantConfirmationPage(page, param);
        List<OrderRiderBasePageVO> records = result.getRecords();
        for (OrderRiderBasePageVO record : records) {
            String addr = "";
            if(null != record.getProvince()){
                String s = baseServiceClient.queryProvinceport(record.getProvince());
                addr = addr + s;
            }
            if(null != record.getProvince()){
                String s1 = baseServiceClient.queryProvinceport(record.getCity());
                addr = addr + s1;
            }
            if(null != record.getProvince()){
                String s2 = baseServiceClient.queryProvinceport(record.getCounty());
                addr = addr + s2;
            }
            record.setDetailAddress(addr+record.getDetailAddress());
            record.setDistance(distance);
        }
        // 待取货
        Integer notPickedUpNum = orderRiderBaseMapper.queryRiderProcessingOrderNum(userId, CommonType.orderStatus.TO_BE_SHIPPED.getCode(), CommonType.DeliveryOrderStart.NOT_PICKUP.getCode());
        // 待配送
        Integer notdeliveredNum = orderRiderBaseMapper.queryRiderProcessingOrderNum(userId, CommonType.orderStatus.TO_BE_SHIPPED.getCode(), CommonType.DeliveryOrderStart.PICKUP.getCode());
        //配送中
        Integer deliveredNum = orderRiderBaseMapper.queryRiderProcessingOrderNum(userId, CommonType.orderStatus.SHIPPED.getCode(), CommonType.DeliveryOrderStart.PICKUP.getCode());

        Map<String,Object> resultMap= new HashMap<>();
        resultMap.put("totalCount",PageUtils.getPage(result).getTotalCount());
        resultMap.put("list",PageUtils.getPage(result).getList());
        resultMap.put("notPickedUpNum",notPickedUpNum);
        resultMap.put("notdeliveredNum",notdeliveredNum);
        resultMap.put("deliveredNum",deliveredNum);

        return ResponseResult.success(resultMap);
    }

    @Override
    public ResponseResult queryMerchantNotConfirmationPage(OrderBaseRiderDTO param) {
        //判断token是否过期
        boolean isExist = RedisUtil.exists(HouConfig.HOU_PLATFORM_TOKEN_USER + param.getToken());
        if (!isExist) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
        }
        //判断条件
        judgingConditions(param);
        //分页查询订单管理列表-商家不确认
        Page<OrderRiderBasePageVO> page = new Page<>(param.getPage(),param.getLimit());
        IPage<OrderRiderBasePageVO> result = orderRiderBaseMapper.queryMerchantNotConfirmationPage(page, param);
        return ResponseResult.success(PageUtils.getPage(result));
    }

    /**
     * 判断条件
     * @param param
     */
    private void judgingConditions(OrderBaseRiderDTO param) {
        // 根据param的状态进行不同的处理，调整param的状态及相关属性
        switch (param.getStatus()){
            case 1:
                // 当状态为1时，设置状态为null，取消派送，并取消分配配送员
                param.setStatus(null);
                param.setPickup(null);
//                param.setDistributorId(null);
                break;
            case 2:
                // 当状态为2时，设置状态为1，表示待派送，设置派送状态为0，即未派送
                param.setStatus(1);
                param.setPickup(0);
//                param.setDistributorId("0");
                break;
            case 3:
                // 当状态为3时，设置状态为3，表示已揽收，设置派送状态为0，即未派送，并取消分配配送员
                param.setStatus(3);
                param.setPickup(0);
                param.setDistributorId("0");
                break;
            case 4:
                // 当状态为4时，设置状态为3，表示已揽收，设置派送状态为0，即未派送，并分配配送员ID为1
                param.setStatus(3);
                param.setPickup(0);
                param.setDistributorId("1");
                break;
            case 5:
                // 当状态为5时，设置状态为3，表示已揽收，设置派送状态为1，即已派送，并分配配送员ID为1
                param.setStatus(3);
                param.setPickup(1);
//                param.setDistributorId("1");
                break;
            case 6:
                // 当状态为6时，设置状态为4，表示已签收，设置派送状态为1，即已派送，并分配配送员ID为1
                param.setStatus(4);
                param.setPickup(1);
//                param.setDistributorId("1");
                break;
            case 7:
                // 当状态为7时，设置状态为6，表示已拒收，设置派送状态为1，即已派送，并分配配送员ID为1
                param.setStatus(6);
                param.setPickup(1);
//                param.setDistributorId("1");
                break;
            default:
                // 当状态为其他值时，输出提示信息
                System.out.println("Not found");
        }

    }

    /**
     * 根据订单ID查询订单详情。
     *
     * @param orderId 订单ID。
     * @return 返回订单详情的响应结果。
     */
    @Override
    public ResponseResult detail(String orderId) {
        // 查询订单基础信息
        //查询订单信息
        OrderRiderBaseVO orderRiderBaseVO = orderRiderBaseMapper.queryOrderRiderBase(orderId);
        // 如果订单基础信息不存在，则返回订单不存在的错误信息
        if (orderRiderBaseVO == null) {
            return ResponseResult.fail(RiderCodeEnume.ORDER_NOT_EXIST);
        }
        // 查询订单商品详情
        List<OrderDetailGoodsItemVO> itemVoList = orderRiderBaseMapper.queryOrderGoodsItem(orderId);
        // 如果订单商品详情存在，则对商品名称添加规格信息
        if (CollUtil.isNotEmpty(itemVoList)) {
            itemVoList.forEach(houOrderDetailGoodsItemVo -> {
                if (StrUtil.isNotBlank(houOrderDetailGoodsItemVo.getOrderSpecChineseValue())) {
                    houOrderDetailGoodsItemVo.setGoodsName(houOrderDetailGoodsItemVo.getGoodsName() + "(" + houOrderDetailGoodsItemVo.getOrderSpecChineseValue() + ")");
                }
            });
        }
        // 将订单商品详情设置到订单基础信息中
        orderRiderBaseVO.setGoodsVoList(itemVoList);
        // 查询门店入口信息
        List<ShopGateInfo> shopGateId = orderRiderBaseMapper.shopGateInfoItem(orderRiderBaseVO.getOrderGateShopId());
        // 将门店入口信息设置到订单基础信息中
        orderRiderBaseVO.setShopGateInfoList(shopGateId);
        // 如果门店入口信息存在，则设置门店名称
        if(null != shopGateId){
            orderRiderBaseVO.setShopGateName(shopGateId.get(0).getShopGateName());
        }
        // 返回查询成功的响应结果，包含订单基础信息和详情
        return ResponseResult.success(orderRiderBaseVO);
    }


    /**
     * 删除订单。
     *
     * @param orderId 订单ID，用于查询和标识特定订单。
     * @return 删除操作的结果，成功返回success，失败则根据具体情况返回相应的错误代码。
     */
    @Override
    public ResponseResult delete(String orderId) {
        // 根据订单ID查询订单基本信息
        //查询订单信息
        OrderBaseEntity orderBaseEntity = getOrderBaseEntity(orderId);

        // 检查订单是否存在，如果不存在则返回订单不存在的错误代码
        if (orderBaseEntity == null) {
            return ResponseResult.fail(RiderCodeEnume.ORDER_NOT_EXIST);
        }

        // 检查订单状态是否为完成状态，如果不是则返回订单未完成的错误代码
        if (orderBaseEntity.getOrderStatus() != 6) {
            return ResponseResult.fail(RiderCodeEnume.ORDER_NOT_COMPLETED);
        }

        // 设置订单为已配送状态
        orderBaseEntity.setIsDeliveryDel(1);
        // 更新订单状态到数据库
        orderBaseMapper.updateById(orderBaseEntity);

        // 删除操作成功，返回成功结果
        return ResponseResult.success();
    }

    @Override
    public ResponseResult shopAffirm(String orderId) {
        //查询订单信息
        OrderBaseEntity orderBaseEntity = getOrderBaseEntity(orderId);
        // 检查订单基础实体是否存在，如果不存在则返回订单不存在的错误码
        if (orderBaseEntity == null) {
            return ResponseResult.fail(RiderCodeEnume.ORDER_NOT_EXIST);
        }

        // 检查商店是否已经确认过订单，如果已经确认过则返回不能再确认的错误码
        if (orderBaseEntity.getShopAffirm() == 1) {
            return ResponseResult.fail(RiderCodeEnume.CANNOT_CONFIRM_REPEATEDLY);
        }

        // 检查订单的退款状态，如果正在退款中则返回错误码提示退款进行中
        if (orderBaseEntity.getOrderRefundStatus().equals("1")) {
            return ResponseResult.fail(RiderCodeEnume.REFUND_IN_PROGRESS);
        }

        // 检查订单的退款状态，如果已经退款完成则返回错误码提示订单已退款
        if (orderBaseEntity.getOrderRefundStatus().equals("2")) {
            return ResponseResult.fail(RiderCodeEnume.ORDER_REFUNDED);
        }

        // 设置商店确认状态为已确认，并更新到数据库
        orderBaseEntity.setShopAffirm(1);
        orderBaseMapper.updateById(orderBaseEntity);

        // 返回操作成功的响应结果
        return ResponseResult.success();

    }

    /**
     * 抢订单接口
     * 用于骑手在平台抢夺订单
     *
     * @param orderId 需要抢单的订单ID
     * @param token 骑手的登录令牌，用于验证身份
     * @return 返回抢单结果，成功或失败及原因
     */
    @Override
    public ResponseResult orderGrabbing(String orderId,String token) {
        // 检查token是否存在于Redis中，如果不存在则返回令牌非法错误
        boolean isExist = RedisUtil.exists(HouConfig.HOU_PLATFORM_TOKEN_USER + token);
        if (!isExist) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
        }

        // 根据订单ID查询订单基本信息，如果订单不存在则返回错误
        OrderBaseEntity orderBaseEntity = getOrderBaseEntity(orderId);
        if (orderBaseEntity == null) {
            return ResponseResult.fail(RiderCodeEnume.ORDER_NOT_EXIST);
        }

        // 检查订单是否已被绑定分销商，如果已被绑定则返回错误
        if (!orderBaseEntity.getDistributorId().equals("0")) {
            return ResponseResult.fail(RiderCodeEnume.ORDER_HAS_BEEN_BOUND);
        }

        // 检查订单状态是否允许抢单，如果状态不符则返回错误
        if (orderBaseEntity.getOrderStatus() != 3) {
            return ResponseResult.fail(RiderCodeEnume.ORDER_CANNOT_BOUND);
        }

        // 从Redis中获取骑手的用户信息
        String data = RedisUtil.get(HouConfig.HOU_PLATFORM_TOKEN_USER + token);
        PlatformUserInfoVO platformUserInfo =  JSON.parseObject(data, PlatformUserInfoVO.class);
        String userId = platformUserInfo.getUserId();

        // 将抢单的骑手ID绑定到订单上
        orderBaseEntity.setDistributorId(userId);
        // 修改骑手订单信息
        orderBaseMapper.updateById(orderBaseEntity);

        // 记录当前时间作为骑手接单时间
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY-MM-dd");
        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String format = simpleDateFormat1.format(date);
        // 更新骑手接单时间到订单骑手基本信息中
        //修改骑手接单时间
        orderRiderBaseMapper.updateDeliveryTime(format, orderId);

        // 格式化当前时间为订单创建时间格式，用于生成提货单编号
        String orderCreateTime = simpleDateFormat.format(date);

        // 根据订单创建时间查询当前的提货单编号
        // 查询提货编号
        String serial = orderRiderBaseMapper.querySerial(orderCreateTime);
        String serialCode = null;
        // 根据当前提货单编号生成新的提货单编号
        if (serial != "" && serial != null) {
            int ii = Integer.valueOf(serial).intValue();
            if ((ii + 1) == 10000) {
                serialCode = "0001";
            } else {
                String aa = String.valueOf(ii + 1);
                int a1 = aa.length();
                if (a1 == 1) {
                    serialCode = "000" + aa;
                } else if (a1 == 2) {
                    serialCode = "00" + aa;
                } else if (a1 == 3) {
                    serialCode = "0" + aa;
                } else {
                    serialCode = aa;
                }
            }
        } else {
            serialCode = "0001";
        }
        // 更新订单的提货单编号
        //修改提货编号
        orderRiderBaseMapper.updatePickupNum(serialCode, orderId);

        // 抢单成功，返回成功结果
        return ResponseResult.success();
    }

    @Override
    public ResponseResult complete(String orderId) {
        //查询订单信息
        OrderBaseEntity orderBaseEntity = getOrderBaseEntity(orderId);
        // 检查订单基础实体是否存在，如果不存在，则返回订单不存在的错误信息
        if (orderBaseEntity == null) {
            return ResponseResult.fail(RiderCodeEnume.ORDER_NOT_EXIST);
        }
        // 检查订单状态是否为已完成，如果是，则返回订单已完成的错误信息
        if (orderBaseEntity.getOrderStatus() == 6) {
            return ResponseResult.fail(RiderCodeEnume.ORDER_COMPLETED);
        }
        // 检查订单状态是否不为待配送，如果不是，则返回订单状态错误的错误信息
        if (orderBaseEntity.getOrderStatus() != 4) {
            return ResponseResult.fail(RiderCodeEnume.ORDER_ERROR);
        }
        // 检查订单是否为配送中，如果不是，则返回订单配送状态错误的错误信息
        if (orderBaseEntity.getIsDelivery() != 1) {
            return ResponseResult.fail(RiderCodeEnume.ORDERS_DELIVERED_DIFFERENT_CITIES);
        }
        // 如果订单有配送费且不为未定义，更新订单总价格
        //加配送费
        if (orderBaseEntity.getDeliveryCharge() != null && !orderBaseEntity.getDeliveryCharge().equals("undefined")) {
            orderRiderBaseMapper.updateTotalPrice(orderBaseEntity.getDeliveryCharge(), orderBaseEntity.getDistributorId());
        }
        // 设置订单配送结束时间、更新订单状态及相关时间信息
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String aaa = formatter.format(date);
        orderBaseEntity.setDeliveryEndTime(aaa);
        orderBaseEntity.setOrderStatus(6);
        orderBaseEntity.setOrderShippingTime(new Date());
        orderBaseEntity.setOrderReceiverDate(DateUtil.offsetDay(date, 7));
        // 赋值订单完成时间
        orderBaseEntity.setOrderFinishedTime(new Date());
        orderBaseMapper.updateById(orderBaseEntity);

        // 返回操作成功的响应结果
        return ResponseResult.success();

    }

    /**
     * 更新订单状态为已取餐，并标记为已揽收。
     * 此方法用于处理骑手取餐后的订单状态更新，确保订单状态正确反映骑手的操作。
     *
     * @param orderId 订单ID，用于查询和更新特定订单的信息。
     * @return 如果订单存在且状态正确，则更新订单状态并返回成功响应；否则，返回相应的错误响应。
     */
    @Override
    public ResponseResult picking(String orderId) {
        // 根据订单ID获取订单基本信息
        OrderBaseEntity orderBaseEntity = getOrderBaseEntity(orderId);

        // 检查订单是否存在，如果不存在则返回订单不存在的错误码
        if (orderBaseEntity == null) {
            return ResponseResult.fail(RiderCodeEnume.ORDER_NOT_EXIST);
        }

        // 检查订单状态是否为待取餐，如果不是则返回订单状态错误的错误码
        if (orderBaseEntity.getOrderStatus() != 3) {
            return ResponseResult.fail(RiderCodeEnume.ORDER_ERROR);
        }

        // 检查订单是否为同城配送，如果不是则返回订单配送城市不同的错误码
        if (orderBaseEntity.getIsDelivery() != 1) {
            return ResponseResult.fail(RiderCodeEnume.ORDERS_DELIVERED_DIFFERENT_CITIES);
        }

        // 更新订单状态为已取餐，标记为已揽收
        orderBaseEntity.setOrderStatus(3);
        orderBaseEntity.setPickup(1);
        // 通过Mapper更新订单信息
        orderBaseMapper.updateById(orderBaseEntity);

        // 返回操作成功的响应
        return ResponseResult.success();
    }

    /**
     * 查询订单信息
     * @param orderId
     * @return
     */
    private OrderBaseEntity getOrderBaseEntity(String orderId) {
        // 创建一个LambdaQueryWrapper实例，用于构建查询条件
        LambdaQueryWrapper<OrderBaseEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();

        // 设置查询条件：订单ID等于指定值
        lambdaQueryWrapper.eq(OrderBaseEntity::getOrderId, orderId);

        // 设置查询条件：订单状态为已发货
        lambdaQueryWrapper.eq(OrderBaseEntity::getIsDelivery, 1);

        // 根据构建的查询条件，查询并返回第一条满足条件的订单基本信息
        return orderBaseMapper.selectOne(lambdaQueryWrapper);

    }

    /**
     * 发送订单
     *
     * @param orderId 订单ID
     * @return 发送结果
     *
     * 本方法用于处理订单的发送操作。它首先通过订单ID查询订单信息，然后根据订单的状态进行一系列的判断，
     * 以确定是否可以发送订单。如果订单不存在、订单已发送、订单状态不正确或者订单不在同一城市配送，则返回相应的错误信息。
     * 如果所有检查都通过，则更新订单状态为已发送，并返回成功信息。
     */
    @Override
    public ResponseResult delivery(String orderId) {
        // 根据订单ID查询订单基本信息
        //查询订单信息
        OrderBaseEntity orderBaseEntity = getOrderBaseEntity(orderId);

        // 检查订单是否存在，如果不存在则返回订单不存在错误码
        if (orderBaseEntity == null) {
            return ResponseResult.fail(RiderCodeEnume.ORDER_NOT_EXIST);
        }

        // 检查订单是否已经点击了发送，如果已经点击了发送则返回订单已发送错误码
        if (orderBaseEntity.getOrderStatus() == 4) {
            return ResponseResult.fail(RiderCodeEnume.ORDER_CLICKED_DELIVERY);
        }

        // 检查订单状态是否为待发送，如果不是则返回订单状态错误错误码
        if (orderBaseEntity.getOrderStatus() != 3) {
            return ResponseResult.fail(RiderCodeEnume.ORDER_ERROR);
        }

        // 检查订单是否为同一城市配送，如果不是则返回订单配送城市不同的错误码
        if (orderBaseEntity.getIsDelivery() != 1) {
            return ResponseResult.fail(RiderCodeEnume.ORDERS_DELIVERED_DIFFERENT_CITIES);
        }

        // 更新订单状态为已发送
        orderBaseEntity.setOrderStatus(4);
        // 通过Mapper更新订单状态
        orderBaseMapper.updateById(orderBaseEntity);

        // 返回发送成功的结果
        return ResponseResult.success();
    }


    @Override
    public ResponseResult supplyDetail(Map<String, Object> param) throws IOException {
        Object address = param.get("address");
        Object key = param.get("key");
        String ui="https://restapi.amap.com/v3/geocode/geo?key="+key+"&address="+address;
        // 创建URL对象
        URL url = new URL(ui);
        // 打开连接
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            // 设置请求方法为GET
            connection.setRequestMethod("GET");
            // 发送请求并获取状态码
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 如果成功收到响应，则从输入流中读取数据
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                StringBuilder resultBuilder = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    resultBuilder.append(line);
                }
                // 关闭输入流
                reader.close();
                // 将字符串转换为JSON或其他格式的数据
                String result = resultBuilder.toString();
                JSONObject jsonObject =  JSON.parseObject(result);
                return ResponseResult.success(jsonObject);
            }
            return ResponseResult.fail(responseCode);
        } finally {
            // 断开连接
            connection.disconnect();
        }
    }


}
