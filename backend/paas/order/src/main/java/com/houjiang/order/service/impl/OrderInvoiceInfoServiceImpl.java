package com.houjiang.order.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.userms.UserInfoDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.*;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.order.entity.OrderBaseEntity;
import com.houjiang.order.entity.OrderInvoiceAddressEntity;
import com.houjiang.order.entity.OrderInvoiceInfoEntity;
import com.houjiang.order.entity.OrderReturnEntity;
import com.houjiang.order.feign.BaseServiceClient;
import com.houjiang.order.mapper.OrderBaseMapper;
import com.houjiang.order.mapper.OrderGoodsMapper;
import com.houjiang.order.mapper.OrderInvoiceInfoMapper;
import com.houjiang.order.mapper.OrderReturnMapper;
import com.houjiang.order.service.OrderInvoiceInfoService;
import com.houjiang.order.vo.*;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.houjiang.commons.util.TokenUtils.checkToken;

/**
 * @ClassName OrderInvoiceInfoServiceImpl
 * @Description  订单发票表
 * @Author Amos
 * @Date 2023-06-25 10:33:24
 * @Version 1.0
 * @author Amos
 */
@Service
public class OrderInvoiceInfoServiceImpl extends ServiceImpl<OrderInvoiceInfoMapper, OrderInvoiceInfoEntity>
        implements OrderInvoiceInfoService {

    @Autowired
    private OrderBaseMapper orderBaseMapper;

    @Autowired
    private BaseServiceClient baseServiceClient;

    @Resource
    private OrderGoodsMapper orderGoodsMapper;

    @Resource
    private OrderReturnMapper orderReturnMapper;

    @Override
    public ResponseResult applyInvoice(Map<Object, Object> param) {
        String token = (String) param.get("token");
        ResponseResult tokenResult = checkToken(token);
        if (tokenResult.getCode() == CommonStatusEnum.FAIL.getCode()) {
            return tokenResult;
        } else {
            param.put("userId", tokenResult.getData().toString());
        }
        try {
            if (!param.containsKey("invoiceHead")) {
                return ResponseResult.success(BizCodeEnume.INVOICE_ADDRESS_TITLE_IS_NULL.getMsg());
            }
            //申请开票将发票状态置为审核中
            param.put("invoiceStatus", 1);
            if (!param.containsKey("invoiceType")) {
                param.put("invoiceType", 0);
            }
            if (!param.containsKey("invoiceHeadType")) {
                param.put("invoiceHeadType", 1);
            }
            BigDecimal orderAmount = orderBaseMapper.selectById(param.get("orderId").toString()).getOrderPaymentAmount();
            param.put("invoiceAmount", orderAmount);
            if(param.get("unitTaxNumber") != null && !"".equals(param.get("unitTaxNumber").toString())){
                param.put("companyTaxId", param.get("unitTaxNumber"));
            }
            //重新申请发票
            if (null != param.get("invoiceId")) {
                this.baseMapper.updateInvoiceInfo(param);
            } else {
                //第一次申请发票
                this.baseMapper.insertInvoiceInfo(param);
            }
            OrderInvoiceInfoEntity entity = this.baseMapper.selectOne(new LambdaQueryWrapper<OrderInvoiceInfoEntity>()
                    .eq(OrderInvoiceInfoEntity::getOrderId, param.get("orderId").toString()));
            orderBaseMapper.update(null, new LambdaUpdateWrapper<OrderBaseEntity>()
                    .eq(OrderBaseEntity::getOrderId, param.get("orderId").toString())
                    .set(OrderBaseEntity::getInvoiceId, param.get("invoiceId") == null ? entity.getInvoiceId() : param.get("invoiceId").toString())
                    .set(OrderBaseEntity::getInvoiceStatus, param.get("invoiceStatus").toString()));
            return ResponseResult.success();
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseResult.fail(e.getMessage());
        }
    }

    /**
     * 批量申请发票的接口方法。
     *
     * @param param 一个Map对象，包含申请发票所需的参数，其中必须包含token用于用户身份验证。
     * @return 返回一个ResponseResult对象，表示发票申请的结果。如果申请成功，返回成功的ResponseResult；如果申请失败，返回失败的ResponseResult，包括错误代码和错误信息。
     */
    @Override
    public ResponseResult applyInvoiceBatch(Map<Object, Object> param) {
        // 通过token获取用户信息，用于身份验证
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.get("token").toString());

        // 如果用户信息为空，表示token验证失败，返回相应的错误信息
        if (null == userInfo) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(), BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        } else {
            // 将用户ID添加到参数中，用于后续的业务操作
            param.put("userId", userInfo.getUserId());
        }

        try {
            // 从参数中获取订单ID列表
            List<String> orderIds = (ArrayList<String>) param.get("orderIds");

            // 遍历订单ID列表，对每个订单进行发票申请处理
            for (String orderId : orderIds) {
                // 将当前处理的订单ID放入参数中
                param.put("orderId", orderId);

                // 获取当前订单的发票状态，如果为空，则默认为0
                Integer invoiceStatus = this.baseMapper.getInvoiceStatus(orderId);
                if (null == invoiceStatus) {
                    invoiceStatus = 0;
                }
                param.put("invoiceStatus", invoiceStatus);

                // 获取当前订单的发票金额
                BigDecimal orderAmount = this.baseMapper.getInvoiceAmount(param);

                // 获取当前订单的发票ID
                Integer invoiceId = this.baseMapper.getInvoiceId(param);

                // 将发票金额和发票ID添加到参数中
                param.put("invoiceAmount", orderAmount);
                param.put("invoiceId", invoiceId);

                // 更新发票信息和发票状态
                this.baseMapper.updateInvoiceInfo(param);
                this.baseMapper.updateInvoiceStatus(param);
            }

            // 所有订单处理完成后，返回成功结果
            return ResponseResult.success();
        } catch (Exception e) {
            // 如果处理过程中发生异常，打印异常堆栈，并返回系统错误的ResponseResult
            e.printStackTrace();
        }

        // 如果执行到此处，表示发生了异常或其他错误，返回相应的错误信息
        return ResponseResult.fail(BizCodeEnume.SYSTEM_ERROR.getCode(), BizCodeEnume.SYSTEM_ERROR.getMsg());
    }

    @Override
    public ResponseResult queryInvoiceDetail(Map<Object, Object> param) {
        // 从参数中获取token
        String token = (String) param.get("token");
        // 校验token的有效性
        ResponseResult tokenResult = checkToken(token);
        // 如果token校验失败，则直接返回错误结果
        if (tokenResult.getCode() == CommonStatusEnum.FAIL.getCode()) {
            return tokenResult;
        } else {
            // 如果token校验成功，将用户ID存入参数中
            param.put("userId", tokenResult.getData().toString());
        }
        try {
            // 根据参数查询订单发票详情
            OrderInvoiceDetailVO orderInvoiceDetailEntity = this.baseMapper.queryInvoiceDetail(param);

            // 获取发票详情中的省份、城市、区域ID
            Integer provinceId = orderInvoiceDetailEntity.getAddressProvince();
            Integer cityId = orderInvoiceDetailEntity.getAddressCity();
            Integer areaId = orderInvoiceDetailEntity.getAddressArea();
            // 如果省份、城市、区域ID都不为空，则获取完整的地址信息
            if (provinceId != null && cityId != null && areaId != null) {
                // 调用服务获取预填充的地址信息
                String preCompleteAddress = baseServiceClient.getAddressString(provinceId, cityId, areaId).getData().toString();
                // 组合完整的地址信息
                String completeAddress = preCompleteAddress + orderInvoiceDetailEntity.getAddressDetail();
                // 更新发票详情中的预填充地址和完整地址
                orderInvoiceDetailEntity.setPreCompleteAddress(preCompleteAddress);
                orderInvoiceDetailEntity.setCompleteAddress(completeAddress);
            }
            // 返回成功的响应结果，包含处理后的发票详情
            return ResponseResult.success(orderInvoiceDetailEntity);
        } catch (Exception e) {
            // 记录异常信息
            log.error(e.getMessage());
            // 返回失败的响应结果，包含异常信息
            return ResponseResult.fail(e.getMessage());
        }

    }

    /**
     * 查询平台订单发票列表。
     *
     * @param param 查询参数，包含分页信息和过滤条件。
     * @return 返回查询结果，包含分页信息和发票列表。
     */
    @Override
    public ResponseResult queryPlatformOrderInvoiceList(Map<String, Object> param) {
        // 如果发票类型为100，则将其从参数中移除，避免错误的过滤条件
        if(null != param.get("invoiceType") && 100 == (Integer)param.get("invoiceType")){
            param.put("invoiceType",null);
        }
        // 如果发票状态为100，则将其从参数中移除，避免错误的过滤条件
        if(null != param.get("invoiceStatus") && 100 == (Integer)param.get("invoiceStatus")){
            param.put("invoiceStatus",null);
        }
        // 根据参数查询平台订单发票信息，包括分页信息
        // 分页查询数据
        IPage<PlatformOrderInvoiceVO> page = this.baseMapper.queryPlatformOrderInvoiceInfo(new Query<PlatformOrderInvoiceVO>().getPage(param),param);
        // 获取脱敏开关的配置值
        String value = baseServiceClient.selectConfigValueByKey(CommonType.configKey.DESENSITIZATION_SWITCH.getDesc());
        // 如果脱敏开关打开，并且查询结果不为空，则对查询结果进行脱敏处理
        if (StrUtil.isNotEmpty(value) && value.equals(CommonType.commonStatus.YES.getCode().toString()) && !page.getRecords().isEmpty()) {
            for (PlatformOrderInvoiceVO vo : page.getRecords()) {
                BeanUtils.copyProperties(DesensitizationUtil.Desensitization(vo, PlatformOrderInvoiceVO.class), vo);
            }
        }
        // 返回处理后的查询结果
        return ResponseResult.success(PageUtils.getPage(page));
    }

    @Override
    public ResponseResult queryPlatformInvoiceDetail(Map<Object, Object> param) {
        /**
         * 根据参数查询平台订单发票详情，并封装地址信息。
         *
         * @param param 查询参数对象，包含所需订单发票详情的信息。
         * @return 返回订单发票详情的响应结果，其中包含完整的地址信息。
         */
        PlatformOrderInvoiceDetailVO platformOrderInvoiceDetailEntity = this.baseMapper.queryPlatformInvoiceDetail(param);

        /* 初始化订单发票地址信息对象 */
        OrderInvoiceAddressVO address = new OrderInvoiceAddressVO();
        /* 将订单发票详情中的地址信息填充到地址对象中 */
        address.setAddressProvince(platformOrderInvoiceDetailEntity.getAddressProvince());
        address.setAddressCity(platformOrderInvoiceDetailEntity.getAddressCity());
        address.setAddressArea(platformOrderInvoiceDetailEntity.getAddressArea());
        address.setAddressDetail(platformOrderInvoiceDetailEntity.getAddressDetail());

        /* 如果地址信息不为空，则通过基础服务客户端获取完整的地址字符串 */
        if(null != address) {
            /* 调用基础服务客户端的方法，拼接出省、市、区的完整地址 */
            String completeAddress = baseServiceClient.getAddressString(address.getAddressProvince(), address.getAddressCity(), address.getAddressArea()).getData().toString();
            /* 设置预完整地址为省、市、区的拼接结果 */
            address.setPreCompleteAddress(completeAddress);
            /* 设置完整地址为省、市、区加上详细地址的拼接结果 */
            completeAddress = completeAddress + address.getAddressDetail();
            address.setCompleteAddress(completeAddress);
        }

        /* 将地址信息设置到订单发票详情对象中 */
        platformOrderInvoiceDetailEntity.setAddress(address);
        /* 返回成功的响应结果，其中包含完整的订单发票详情和地址信息 */
        return ResponseResult.success(platformOrderInvoiceDetailEntity);
    }

    @Override
    @Transactional
    public ResponseResult auditInvoiceForSeller(Map<Object, Object> param) {
        // 初始化响应结果对象
        ResponseResult rr = new ResponseResult();
        // 从参数中获取token
        String token = (String) param.get("token");
        // 通过token解析获取用户信息
        UserInfoVO ui=ParseTokenUtils.getUserInfoByToken(token);
        if(null == ui || null == ui.getShopId()){
            rr.setCode(CommonStatusEnum.FAIL.getCode());
            rr.setMessage(BizCodeEnume.INSUFFICIENT_PERMISSIONS.getMsg());
            return rr;
        }
        // 将用户所属店铺ID转换为整型
        Integer shopId = Integer.valueOf(ui.getShopId());
        // 校验token的有效性
        rr = checkToken(token);
        // 如果token校验失败，则直接返回失败结果
        if (rr.getCode() == 2) {
            return rr;
        } else {
            // 如果token校验成功，将用户ID添加到参数中
            param.put("userId", rr.getData().toString());
        }
        try {
            // 审核发票信息
            this.baseMapper.auditInvoiceForSeller(param);
            // 更新订单发票状态
            this.baseMapper.updateOrderInvoiceStatus(param);
            // 发送发票到邮件
            sendInvoiceInfo(param);
            // 设置操作成功的响应码和消息
            rr.setCode(CommonStatusEnum.SUCCESS.getCode());
            rr.setMessage(BizCodeEnume.OPERATION_SUCCESSFUL.getMsg());
        } catch (Exception e) {
            // 设置操作失败的响应码和消息
            rr.setCode(CommonStatusEnum.FAIL.getCode());
            rr.setMessage(BizCodeEnume.OPERATION_FAILED.getMsg());
            // 记录异常信息
            log.error(e.getMessage());
        }
        // 返回处理结果
        return rr;

    }

    private void sendInvoiceInfo(Map<Object, Object> param) {
        if (param.get("userMail") != null && param.get("userMail") != "" &&
                (param.get("invoicePic") != null && param.get("invoicePic") != "")){
            // 初始化用于存储邮件发送所需信息的Map
            Map<String, Object> map = new HashMap<>();
            map.put("route", param.get("invoicePic"));
            map.put("type", "1");
            // 获取发送邮箱设置信息
            Map<String,Object> settingEmailMap = new HashMap<>();
            settingEmailMap.put("configType", "setting");
            settingEmailMap.put("token", param.get("token"));
            ResponseResult result = baseServiceClient.querySetting(settingEmailMap);
            List<Map<String, Object>> listm = JSON.parseObject(JSON.toJSONString(result.getData()), List.class);
            Map<String, Object> setting = new HashMap<>();
            for (Map<String, Object> stringObjectMap : listm) {
                setting.put(stringObjectMap.get("configKey").toString(), stringObjectMap.get("configValue"));
            }

            // 验证邮箱设置是否完整
            if (setting.get("setting_email") == null || "".equals(setting.get("setting_email"))) {
                log.error("商城未设置电子邮箱!");
            }
            if (setting.get("setting_email_code") == null || "".equals(setting.get("setting_email_code"))) {
                log.error("商城未设置电子邮箱授权码!");
            }

            // 准备邮件发送的相关信息
            String settingEmail = (String) setting.get("setting_email");
            String server = "smtp." + settingEmail.substring(settingEmail.indexOf("@") + 1);
            map.put("sendEmail", settingEmail);
            map.put("emailCode", setting.get("setting_email_code"));
            map.put("server", server);
            map.put("receivingEmail", param.get("userMail"));

            // 发送邮件
            try {
                SendMailUtil.sendInvoice(map);
            } catch (Exception e) {
                e.printStackTrace();
                log.error(BizCodeEnume.SENDING_FAIL.getMsg());
            }
        }
    }

    @Override
    public ResponseResult querySellerInvoiceDetail(Map<Object, Object> param) {
        String token = (String) param.get("token");
        // 根据code存储用户信息
        String userInfoStr = RedisUtil.get(HouConfig.HOU_USER_TOKEN_USER + token);
        // 将JSON字符串解析为UserInfoDTO对象，用于后续提取用户信息
        UserInfoDTO dto = JSON.parseObject(userInfoStr, UserInfoDTO.class);
        // 从DTO中获取用户信息对象
        UserInfoVO userInfo = dto.getUser();
        // 检查用户是否有店铺权限，若无，则返回权限不足的错误信息
        if (null == userInfo.getShopId()) {
            return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
        }
        // 将用户ID放入参数映射表中，为后续查询发票详情做准备
        param.put("userId", userInfo.getUserId());
        try {
            // 根据参数查询卖家发票详情
            SellerOrderInvoiceDetailVO sellerOrderInvoiceDetailEntity = this.baseMapper.querySellerInvoiceDetail(param.get("orderId").toString());

            // 初始化订单发票地址实体
            OrderInvoiceAddressEntity address = new OrderInvoiceAddressEntity();
            // 从发票详情实体中填充地址信息到订单发票地址实体
            address.setAddressProvince(sellerOrderInvoiceDetailEntity.getAddressProvince());
            address.setAddressCity(sellerOrderInvoiceDetailEntity.getAddressCity());
            address.setAddressArea(sellerOrderInvoiceDetailEntity.getAddressArea());
            address.setAddressDetail(sellerOrderInvoiceDetailEntity.getAddressDetail());
            // 如果地址信息不为空，则完成地址的拼接和设置
            if(null != address) {
                String completeAddress = getAddress(address.getAddressProvince() + "", address.getAddressCity() + "", address.getAddressArea() + "");
                address.setPreCompleteAddress(completeAddress);
                completeAddress = completeAddress + address.getAddressDetail();
                address.setCompleteAddress(completeAddress);
            }
            // 将订单发票地址实体设置回发票详情实体
            sellerOrderInvoiceDetailEntity.setAddress(address);
            // 返回成功的响应结果，包含完整的发票详情信息
            return ResponseResult.success(sellerOrderInvoiceDetailEntity);
        } catch (Exception e) {
            // 记录异常信息
            log.error(e.getMessage());
            // 返回操作异常的错误信息
            return ResponseResult.fail(2,"操作异常"+e.getMessage());
        }

    }

    /**
     * 根据省、市、区ID获取对应的地址字符串。
     *
     * @param provinceId 省份ID，可能为空。
     * @param cityId 城市ID，可能为空。
     * @param districtId 区县ID，可能为空。
     * @return 返回拼接后的地址字符串，如果所有ID都为空，则返回空字符串。
     */
    private String getAddress(String provinceId, String cityId, String districtId) {
        // 检查省、市、区ID是否都为空，如果都为空，则直接返回空字符串
        if ((provinceId == null || provinceId.equals(""))
                && (cityId == null || cityId.equals(""))
                && (districtId == null || districtId.equals(""))) {
            return "";
        }

        // 根据ID查询省、市、区的名称
        String provinceName = baseServiceClient.queryProvinceport(provinceId);
        String cityName = baseServiceClient.queryProvinceport(cityId);
        String districtName = baseServiceClient.queryProvinceport(districtId);

        // 返回拼接后的地址字符串
        return provinceName + cityName + districtName;
    }

    @Override
    public ResponseResult querySellerOrderInvoiceList(Map<String, Object> param) {
        // 获取当前登录的商家信息
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(param.get("token").toString());
        // 将商家ID加入查询参数
        param.put("shopId", ui.getShopId());
        // 初始化分页信息
        Integer currentPage = (Integer) param.get("page");
        Integer pageSize = (Integer) param.get("limit");
        //分页查询发票信息
        Page<SellerOrderInvoiceVO> page = new Page<>(currentPage, pageSize);
        IPage<SellerOrderInvoiceVO> datalist = orderBaseMapper.querySellerOrderInvoiceInfo(page, param);

        // 对查询结果中的每条数据，进一步查询商品详情和发票类型等信息
        for (SellerOrderInvoiceVO order : datalist.getRecords()) {
            OrderReturnEntity orderReturnEntity = orderReturnMapper.selectOne(new LambdaQueryWrapper<OrderReturnEntity>()
                    .eq(OrderReturnEntity::getOrderId, order.getOrderId()));
            BigDecimal invoicableAmount = order.getOrderPaymentAmount();
            if(orderReturnEntity!=null){
                invoicableAmount = invoicableAmount.subtract(orderReturnEntity.getReturnCash()).setScale(2, RoundingMode.UP);
            }
            order.setInvoicableAmount(invoicableAmount);
            // 根据订单ID查询订单商品详情
            param.put("orderId", order.getOrderId());
            List<OrderGoodsDetailVO> goodsInfo = orderGoodsMapper.queryOrderInvoiceGoodsInfo(param);
            order.setGoodsList(goodsInfo);
            // 根据发票ID查询发票类型
            param.put("invoiceId", order.getInvoiceId());
            if (null != order.getInvoiceId()) {
                Integer invoiceType = this.baseMapper.getInvoiceType(param);
                order.setInvoiceType(invoiceType);
            }
        }
        // 返回查询结果
        return ResponseResult.success(PageUtils.getPage(datalist));
    }
}
