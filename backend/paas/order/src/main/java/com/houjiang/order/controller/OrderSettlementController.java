package com.houjiang.order.controller;

import com.houjiang.commons.dto.PageUserTokenBeanDTO;
import com.houjiang.commons.dto.job.ShopSettleAccountInfoDTO;
import com.houjiang.commons.dto.order.SupplierShopSettleAccountInfoDTO;
import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.order.dto.FinancialSettlementDTO;
import com.houjiang.order.dto.WaitOrderDTO;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * @ClassName OrderSettlementController
 * @Description 订单结算
 * @Author Amos
 * @Date 2023/7/6 9:23
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("order/settlement")
public class OrderSettlementController extends BaseController {

    /**
     * 结算单对应订单导出
     *
     * @param paraMap
     * @param response
     * @return
     * @throws IOException
     * @throws ParseException
     */
    @PostMapping("/exportExcel")
    public ResponseResult exportExcel(@RequestBody Map<String, Object> paraMap, HttpServletResponse response) throws IOException, ParseException {
        ResponseResult result = orderSettlementService.exportExcel(paraMap, response);
        return result;
    }

    /**
     * 商家提现未提现金额查询导出
     *
     * @param param
     * @return
     */
    @PostMapping("/platform/getMerchantAmountExp")
    public ResponseResult getMerchantAmountExp(@RequestBody FinancialSettlementDTO param) {
        ResponseResult result = orderSettlementService.getMerchantAmountExp(param);
        return result;
    }

    /**
     * 商家后台查询结算单列表
     *
     * @param paraMap
     * @return
     */
    @PostMapping("/business/list")
    public ResponseResult businessList(@RequestBody Map<String, String> paraMap) {
        ResponseResult result = orderSettlementService.businessList(paraMap);
        return result;
    }

    /**
     * 商家导出excel
     *
     * @param paraMap
     * @return
     */
    @PostMapping("/business/export/excel")
    public ResponseResult exportexcelSettlementBusiness(@RequestBody Map<String, String> paraMap) {
        ResponseResult result = orderSettlementService.exportexcelSettlementBusiness(paraMap);
        return result;
    }

    /**
     * 商家结算详情
     *
     * @param paraMap
     * @return
     */
    @PostMapping("/detail")
    public ResponseResult settlementDetail(@RequestBody Map<String, String> paraMap) {
        ResponseResult result = orderSettlementService.settlementDetail(paraMap);
        return result;
    }

    /**
     * 商家结算单对应订单
     *
     * @param paraMap
     * @return
     */
    @PostMapping("/detail/base/orderInfo")
    public ResponseResult listBaseInfo(@RequestBody Map<String, Object> paraMap) {
        ResponseResult result = orderSettlementService.listBaseInfo(paraMap);
        return result;
    }

    /**
     * 商家结算单对应退单
     *
     * @param paraMap
     * @return
     */
    @PostMapping("/detail/return/orderInfo")
    public ResponseResult listReutrnOrder(@RequestBody Map<String, Object> paraMap) {
        ResponseResult result = orderSettlementService.listReutrnOrder(paraMap);
        return result;
    }

    /**
     * 商家导出订单excel
     *
     * @param paraMap
     * @return
     */
    @PostMapping("/platform/export/excel")
    public ResponseResult exportexcelSettlementBaseInfo(@RequestBody Map<String, Object> paraMap) {
        ResponseResult result = orderSettlementService.exportexcelSettlementBaseInfo(paraMap);
        return result;
    }

    /**
     * 商家导出退单excel
     *
     * @param paraMap
     * @return
     */
    @PostMapping("/platform/export/excel/returnList")
    public ResponseResult exportexcelSettlementReturnOrder(@RequestBody Map<String, Object> paraMap) {
        ResponseResult result = orderSettlementService.exportexcelSettlementReturnOrder(paraMap);
        return result;
    }

    /**
     * 商家自销等待结算金额
     *
     * @param param
     * @return
     */
    @PostMapping("/wait/detail")
    public ResponseResult waitSettlement(@RequestBody UserTokenBeanDTO param) {
        ResponseResult result = orderSettlementService.waitSettlement(param);
        return result;
    }

    /**
     * 商家等待结算对应订单列表
     *
     * @param param
     * @return
     */
    @PostMapping("/wait/order/list")
    public ResponseResult waitOrderList(@RequestBody WaitOrderDTO param) {
        ResponseResult result = orderSettlementService.waitOrderList(param);
        return result;
    }

    /**
     * 商家等待结算对应退款列表
     *
     * @param param
     * @return
     */
    @PostMapping("/wait/return/list")
    public ResponseResult waitReturnList(@RequestBody WaitOrderDTO param) {
        ResponseResult result = orderSettlementService.waitReturnList(param);
        return result;
    }


    /**
     * 商家等待结算对应退款列表
     *
     * @param param
     * @return
     */
    @PostMapping("/wait/dealerReturn/list")
    public ResponseResult waitDealerReturnList(@RequestBody WaitOrderDTO param) throws IllegalAccessException {
        ResponseResult result = orderSettlementService.waitDealerReturnList(param);
        return result;
    }

    /**
     * 商家提现未提现金额查询
     *
     * @param param
     * @return
     */
    @PostMapping("/platform/getMerchantAmount")
    public ResponseResult getMerchantAmount(@RequestBody FinancialSettlementDTO param) throws IllegalAccessException {
        ResponseResult result = orderSettlementService.getMerchantAmount(param);
        return result;
    }

    /**
     * 修改结算信息(店铺名)
     *
     * @param map
     * @return
     */
    @PostMapping("/updateByMap")
    public ResponseResult updateOrderSettlementByMap(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderSettlementService.updateOrderSettlementByMap(map);
        return result;
    }


    /**
     * 平台查询 结算单对应的退单列表 导出
     *
     * @param paraMap
     * @return
     */
    @PostMapping("/platForm/returnOrderInfoExcel")
    public ResponseResult platFormReturnorderinfoExcel(@RequestBody Map<String, Object> paraMap) {
        ResponseResult result = orderSettlementService.platFormReturnorderinfoExcel(paraMap);
        return result;
    }

    /**
     * 平台查询 结算单对应的退单列表
     *
     * @param paraMap
     * @return
     */
    @PostMapping("/platForm/returnOrderInfo")
    public ResponseResult platFormReturnorderinfo(@RequestBody Map<String, Object> paraMap) {
        ResponseResult result = orderSettlementService.platFormReturnorderinfo(paraMap);
        return result;
    }

    /**
     * 平台查询 结算单对应订单
     *
     * @param paraMap
     * @return
     */
    @PostMapping("platForm/baseOrderInfo")
    public ResponseResult platFormSettlementBaseorderinfo(@RequestBody Map<String, Object> paraMap) {
        ResponseResult result = orderSettlementService.listBaseorderinfo(paraMap);
        return result;
    }

    /**
     * 平台查询 结算单对应订单
     *
     * @param paraMap
     * @return
     */
    @PostMapping("/platForm/detail")
    public ResponseResult platFormSettlementdetail(@RequestBody Map<String, Object> paraMap) {
        ResponseResult result = orderSettlementService.platFormSettlementdetail(paraMap);
        return result;
    }


    /**
     * 平台查询 结算列表
     *
     * @param map
     * @return
     */
    @PostMapping("/platForm/list")
    public ResponseResult queryPlatFormListSettlement(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderSettlementService.queryPlatFormListSettlement(map);
        return result;
    }

    /**
     * 生成结算单订单
     *
     * @param map
     * @return
     */
    @PostMapping("/confirmSettlement")
    public ResponseResult confirmSettlement(@RequestBody Map<Object, Object> map) {
        ResponseResult result = orderSettlementService.confirmSettlement(map);
        return result;
    }

    /**
     * 手动执行结算（BFF 层 processSettlement 对应接口）
     * 内部复用 confirmSettlement 业务逻辑：支持管理端按订单/店铺维度手动触发结算，
     * 入参与 /confirmSettlement 保持一致：{orderIds:[], shopId:xx, settlementType:xx ...}。
     *
     * @param map 手动结算入参
     * @return 结算处理结果
     */
    @PostMapping("/manual/settle")
    public ResponseResult manualSettle(@RequestBody Map<Object, Object> map) {
        return orderSettlementService.confirmSettlement(map);
    }

    /**
     * 更新订单结算状态
     *
     * @param list
     * @return
     */
    @PostMapping("/feign/updateOrderSettlementStatus")
    public boolean updateOrderSettlementStatus(@RequestBody List<String> list){
        boolean  result = orderSettlementService.updateOrderSettlementStatus(list);
        return result;
    }

    /**
     * 更新结算状态
     *
     * @param list
     * @return
     */
    @PostMapping("/feign/updateSettlementStatus")
    public boolean updateSettlementStatus(@RequestBody List<String> list){
        boolean  result = orderSettlementService.updateSettlementStatus(list);
        return result;
    }

    /**
     *支票结算金额
/**
     * @param shopId
     * @return
     */
    @GetMapping("/feign/checkSettlementAmount")
    public boolean checkSettlementAmount(@RequestParam("shopId") String shopId){
        boolean  result = orderSettlementService.checkSettlementAmount(shopId);
        return result;
    }
    /**
     * 获取商家结算数据
     *
     * @param mapAccount
     * @return
     */
    @PostMapping("/feign/getShopSettleAccountData")
    public ShopSettleAccountInfoDTO getShopSettleAccountData(@RequestBody Map<String, Object> mapAccount){
        ShopSettleAccountInfoDTO  result = orderSettlementService.getShopSettleAccountData(mapAccount);
        return result;
    }
    /**
     * 获取供应商结算数据
     *
     * @param mapAccount
     * @return
     */
    @PostMapping("/feign/getSupplierSettleAccountData")
    public SupplierShopSettleAccountInfoDTO getSupplierSettleAccountData(@RequestBody Map<String, Object> mapAccount){
        SupplierShopSettleAccountInfoDTO  result = orderSettlementService.getSupplierSettleAccountData(mapAccount);
        return result;
    }
}
