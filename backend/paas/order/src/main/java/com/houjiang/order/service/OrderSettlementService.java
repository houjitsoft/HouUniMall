package com.houjiang.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.dto.PageUserTokenBeanDTO;
import com.houjiang.commons.dto.job.ShopSettleAccountInfoDTO;
import com.houjiang.commons.dto.order.SupplierShopSettleAccountInfoDTO;
import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.order.dto.FinancialSettlementDTO;
import com.houjiang.order.dto.WaitOrderDTO;
import com.houjiang.order.entity.OrderSettlementEntity;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.ibatis.annotations.Param;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * @author Amos
 */
public interface OrderSettlementService extends IService<OrderSettlementEntity> {

    /**
     * 结算单对应订单导出
     *
     * @param paraMap
     * @param response
     * @return
     * @throws IOException
     * @throws ParseException
     */

    /**
     * TODO 补充方法说明
     * @param paraMap TODO
     * @param response TODO
     * @return TODO
     */
    ResponseResult exportExcel(Map<String, Object> paraMap, HttpServletResponse response) throws IOException, ParseException;


    /**
     * 商家提现未提现金额查询导出
     *
     * @param param
     * @return
     */
    ResponseResult getMerchantAmountExp(FinancialSettlementDTO param);

    /**
     * 商家后台查询结算单列表
     *
     * @param paraMap
     * @return
     */
    ResponseResult businessList(Map<String, String> paraMap);

    /**
     * 商家导出excel
     *
     * @param paraMap
     * @return
     */
    ResponseResult exportexcelSettlementBusiness(Map<String, String> paraMap);

    /**
     * 商家结算详情
     *
     * @param paraMap
     * @return
     */
    ResponseResult settlementDetail(Map<String, String> paraMap);

    /**
     * 商家结算单对应订单
     *
     * @param paraMap
     * @return
     */
    ResponseResult listBaseInfo(Map<String, Object> paraMap);

    /**
     * 商家结算单对应退单
     *
     * @param paraMap
     * @return
     */
    ResponseResult listReutrnOrder(Map<String, Object> paraMap);

    /**
     * 商家导出订单excel
     *
     * @param paraMap
     * @return
     */
    ResponseResult exportexcelSettlementBaseInfo(Map<String, Object> paraMap);

    /**
     * 商家导出退单excel
     *
     * @param paraMap
     * @return
     */
    ResponseResult exportexcelSettlementReturnOrder(Map<String, Object> paraMap);

    /**
     * 商家自销等待结算金额
     *
     * @param param
     * @return
     */
    ResponseResult waitSettlement(UserTokenBeanDTO param);

    /**
     * 商家等待结算对应订单列表
     *
     * @param param
     * @return
     */
    ResponseResult waitOrderList(WaitOrderDTO param);

    /**
     * 商家等待结算对应退款列表
     *
     * @param param
     * @return
     */
    ResponseResult waitReturnList(WaitOrderDTO param);

    /**
     * 商家等待结算对应退款列表
     *
     * @param param
     * @return
     */
    ResponseResult waitDealerReturnList(WaitOrderDTO param) throws IllegalAccessException;

    /**
     * 商家提现未提现金额查询
     *
     * @param param
     * @return
     */
    ResponseResult getMerchantAmount(FinancialSettlementDTO param) throws IllegalAccessException;

    /**
     * 修改结算信息(店铺名)
     *
     * @param map
     * @return
     */
    ResponseResult updateOrderSettlementByMap(Map<String, Object> map);

    /**
     * 平台查询 结算单对应的退单列表 导出
     *
     * @param paraMap
     * @return
     */
    ResponseResult platFormReturnorderinfoExcel(Map<String, Object> paraMap);


    /**
     * 平台查询 结算单对应的退单列表
     *
     * @param paraMap
     * @return
     */
    ResponseResult platFormReturnorderinfo(Map<String, Object> paraMap);


    /**
     * 平台查询 结算单对应订单
     *
     * @param paraMap
     * @return
     */
    ResponseResult listBaseorderinfo(Map<String, Object> paraMap);


    /**
     * 平台查询 结算单对应订单
     *
     * @param paraMap
     * @return
     */
    ResponseResult platFormSettlementdetail(Map<String, Object> paraMap);


    /**
     * 平台查询 结算列表
     *
     * @param map
     * @return
     */
    ResponseResult queryPlatFormListSettlement(Map<String, Object> map);

    /**
     * 平台查询 结算列表 导出
     *
     * @param map
     * @return
     */
    ResponseResult queryPlatFormListSettlementExcel(Map<String, Object> map);

    /**
     * 生成结算单订单
     *
     * @param map
     * @return
     */
    ResponseResult confirmSettlement(Map<Object, Object> map);

    /**
     * 更新订单结算状态
     *
     * @param list
     * @return
     */
    boolean updateOrderSettlementStatus(List<String> list);

    /**
     * 更新结算状态
     *
     * @param list
     * @return
     */
    boolean updateSettlementStatus(List<String> list);

    /**
     *支票结算金额
/**
     * @param shopId
     * @return
     */
    boolean checkSettlementAmount(String shopId);

    /**
     * 获取商家结算数据
     * @param mapAccount
     * @return
     */
    ShopSettleAccountInfoDTO getShopSettleAccountData(Map<String, Object> mapAccount);

    /**
     * 获取供应商结算数据
     * @param mapAccount
     * @return
     */
    SupplierShopSettleAccountInfoDTO getSupplierSettleAccountData(Map<String, Object> mapAccount);
}
