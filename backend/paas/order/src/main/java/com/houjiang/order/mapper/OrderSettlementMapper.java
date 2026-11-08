package com.houjiang.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.commons.dto.PageUserTokenBeanDTO;
import com.houjiang.commons.dto.job.ShopSettleAccountInfoDTO;
import com.houjiang.commons.dto.order.SupplierShopSettleAccountInfoDTO;
import com.houjiang.commons.vo.order.OrderBaseListVO;
import com.houjiang.order.dto.FinancialSettlementDTO;
import com.houjiang.order.dto.WaitOrderDTO;
import com.houjiang.order.entity.OrderSettlementEntity;
import com.houjiang.order.vo.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Entity com.houjiang.order.entity.OrderSettlement
 * @author Amos
 */
@Mapper
public interface OrderSettlementMapper extends BaseMapper<OrderSettlementEntity> {

    /**
     *商家提现未提现金额查询
/**
     * @param param
     * @return
     */
    List<FinancialSettlementDTO> getMerchantAmountExp(@Param("param") FinancialSettlementDTO param);

    /**
     * 分页查询结算业务
     * @param page
     * @param paraMap
     * @return
     */
    IPage<OrderSettlementVO> businessList(Page<OrderSettlementVO> page,@Param("paraMap") Map<String, String> paraMap);

    /**
     * 查询结算业务列表
     * @param paraMap
     * @return
     */
    List<OrderSettlementVO> exportexcelSettlementBusiness(@Param("paraMap") Map<String, String> paraMap);

    /**
     * 查询结算详情
     * @param paraMap
     * @return
     */
    OrderSettlementVO settlementDetail(@Param("paraMap") Map<String, String> paraMap);

    /**
     * 获取结算时间
     * @param paraMap
     * @return
     */
    OrderSettlementVO getSettlementDate(@Param("paraMap") Map<String, Object> paraMap);

    /**
     * 分页查询基础订单信息
     * @param page
     * @param paraMap
     * @return
     */
    IPage<OrderBaseInfoVO>listSupplyBaseInfo(IPage<OrderBaseInfoVO> page,@Param("paraMap") Map<String, Object> paraMap);

    /**
     * 分页查询商家基础订单信息
     * @param page
     * @param paraMap
     * @return
     */
    IPage<OrderBaseInfoVO>listShopSupplyBaseInfo(IPage<OrderBaseInfoVO> page,@Param("paraMap") Map<String, Object> paraMap);

    /**
     * 分页查询基础订单信息
     * @param page
     * @param paraMap
     * @return
     */
    IPage<OrderBaseInfoVO> listBaseInfo2(IPage<OrderBaseInfoVO> page,@Param("paraMap") Map<String, Object> paraMap);

    /**
     * 分页查询基础订单信息
     * @param page
     * @param paraMap
     * @return
     */
    IPage<OrderBaseInfoVO> listDealerBaseInfo(IPage<OrderBaseInfoVO> page,@Param("paraMap") Map<String, Object> paraMap);

    /**
     * 分页查询商家基础订单信息
     * @param page
     * @param paraMap
     * @return
     */
    IPage<OrderBaseInfoVO> listShopDealerBaseInfo(IPage<OrderBaseInfoVO> page,@Param("paraMap") Map<String, Object> paraMap);

    /**
     * 分页查询基础订单信息
     * @param page
     * @param paraMap
     * @return
     */
    IPage<OrderBaseInfoVO> listBaseInfo(IPage<OrderBaseInfoVO> page,@Param("paraMap") Map<String, Object> paraMap);

    /**
     * 分页查询商家基础订单信息
     * @param page
     * @param paraMap
     * @return
     */
    IPage<OrderBaseInfoVO> listShopBaseInfo(IPage<OrderBaseInfoVO> page,@Param("paraMap") Map<String, Object> paraMap);

    /**
     * 分页查询返回订单详情信息
     * @param page
     * @param paraMap
     * @return
     */
    IPage<ReturnOrderDetailListVO> listSupplyReutrnOrder(IPage<ReturnOrderDetailListVO> page,@Param("paraMap") Map<String, Object> paraMap);

    /**
     * 分页查询返回订单详情信息
     * @param page
     * @param paraMap
     * @return
     */
    IPage<ReturnOrderDetailListVO> listReutrnOrder2(IPage<ReturnOrderDetailListVO> page,@Param("paraMap") Map<String, Object> paraMap);

    /**
     * 分页查询返回订单详情信息
     * @param page
     * @param paraMap
     * @return
     */
    IPage<ReturnOrderDetailListVO> listDealerReutrnOrder(IPage<ReturnOrderDetailListVO> page,@Param("paraMap") Map<String, Object> paraMap);

    /**
     * 分页查询返回订单详情信息
     * @param page
     * @param paraMap
     * @return
     */
    IPage<ReturnOrderDetailListVO> listReutrnOrder(IPage<ReturnOrderDetailListVO> page,@Param("paraMap") Map<String, Object> paraMap);

    /**
     * 分页查询返回商家自销订单详情信息
     * @param page
     * @param paraMap
     * @return
     */
    IPage<ReturnOrderDetailListVO> listShopReutrnOrder(IPage<ReturnOrderDetailListVO> page,@Param("paraMap") Map<String, Object> paraMap);

    /**
     * 查询供应商基本信息导出
     * @param paraMap
     * @return
     */
    List<OrderBaseInfoVO> listSupplyBaseInfoExport(@Param("paraMap") Map<String, Object> paraMap);

    /**
     * 查询基本信息导出
     * @param paraMap
     * @return
     */
    List<OrderBaseInfoVO> listBaseInfoExport(@Param("paraMap") Map<String, Object> paraMap);

    /**
     * 查询供应商回收订单导出
     * @param paraMap
     * @return
     */
    List<ReturnOrderDetailListVO> listSupplyReutrnOrderExport(@Param("paraMap") Map<String, Object> paraMap);

    /**
     * 查询返回订单导出
     * @param paraMap
     * @return
     */
    List<ReturnOrderDetailListVO> listReutrnOrderExport(@Param("paraMap") Map<String, Object> paraMap);

    /**
     * 获取时间
     * @param shopId
     * @param osOrderType
     * @return
     */
    Date getBeformDate(@Param("shopId") String shopId,@Param("osOrderType") Integer osOrderType);

    /**
     * 获取结算账户订单
     * @return
     */
    ShopSettleAccountInfoDTO getSettleAllAccountOrder(@Param("param")Map<String, Object> mapAccount);
    /**
     * 获取结算账户订单信息
     * 根据提供的账户信息映射，获取相关的结算账户订单数据主要用于财务结算和对账
     *
     * @param mapAccount 包含账户信息的映射，用于查询特定条件下的结算账户订单
     * @return ShopSettleAccountInfoDTO 返回结算账户订单的信息数据传输对象
     */
    ShopSettleAccountInfoDTO getSettleOrderCommission(@Param("param")Map<String, Object> mapAccount);

    /**
     * 查询结算单号
     * @param mapAccount
     * @return
     */
    List<String> getSettleAllOrderList(@Param("param")Map<String, Object> mapAccount);
    /**
     * 获取结算账户返回
     * @return
     */
    ShopSettleAccountInfoDTO getSettleAllAccountReturn(@Param("param")Map<String, Object> mapAccount);
    /**
     * 退款订单包含经销商品的退款金额和佣金
     */
    ShopSettleAccountInfoDTO getSettleSupplierAndShopOrder(@Param("param")Map<String, Object> mapAccount);
    /**
     * 经销订单退款金额和佣金
     */
    ShopSettleAccountInfoDTO getReturnSellOnSupplierAndShopOrder(@Param("param")Map<String, Object> mapAccount);

    /**
     * 获取退还订单的协议价
     */
    BigDecimal getReturnSellOnAmount(@Param("param")Map<String, Object> mapAccount);

    /**
     * 查询商家查询商家要结算的退单
     * @param mapAccount
     * @return
     */
    List<String> getSettleAllReturnOrderIdList(@Param("param")Map<String, Object> mapAccount);

    /**
     * 获取结算账户订单
     * @param shopId
     * @param dropShipping
     * @param dBefore
     * @return
     */
    ShopSettleAccountInfoDTO getCombinationOrder(@Param("shopId") String shopId,@Param("dropShipping") Integer dropShipping,@Param("dBefore") Date dBefore);

    /**
     * 获取结算账户订单
     * @param shopId
     * @param dropShipping
     * @param dBefore
     * @return
     */
    ShopSettleAccountInfoDTO getCombinationOrderReturn(@Param("shopId") String shopId,@Param("dropShipping") Integer dropShipping,@Param("dBefore") Date dBefore);

    /**
     * 分页查询等待订单列表
     * @param page
     * @param param
     * @param dBefore
     */
    IPage<OrderBaseListVO> selectWaitOrderList(Page<OrderBaseListVO> page, @Param("param") WaitOrderDTO param, @Param("dBefore") Date dBefore);


    /**
     * 分页查询等待返回列表
     * @param page
     * @param param
     * @param dBefore
     * @return
     */
    IPage<ReturnBaseListVO> selectWaitReturnList(Page<ReturnBaseListVO> page,@Param("param") WaitOrderDTO param,
                                                 @Param("dBefore") Date dBefore);

    /**
     * 商家等待结算对应退款列表
     * @param param
     * @return
     */
    IPage<ReturnBaseListVO> selectWaitDealerReturnList(IPage<FinancialSettlementDTO> page,@Param("shopId") String shopId, @Param("param")WaitOrderDTO param, @Param("dBefore")Date dBefore);

    /**
     * 商家等待结算对应对账单 订单列表
     */
    IPage<OrderBaseListVO> selectWaitDealerOrderList(IPage<FinancialSettlementDTO> page, @Param("param")WaitOrderDTO param, @Param("dBefore")Date dBefore);


    /**
     * 商家提现未提现金额查询
     * @param param
     * @return
     */
    IPage<FinancialSettlementDTO> getMerchantAmount(IPage<FinancialSettlementDTO> page , @Param("param") FinancialSettlementDTO param);


    /**
     * 更改结算单信息
     *
     * @param map
     * @return
     */
    Integer updateOrderSettlementByMap(@Param("param") Map<String, Object> map);


    /**
     * 分页查询平台表单列表结算
     *
     * @param page
     * @param map
     * @return
     */
    IPage<PlatFormOrderSettlementVO> queryPlatFormListSettlement(IPage<PlatFormOrderSettlementVO> page, @Param("refundMap") Map<String, Object> map);


    /**
     * 查询平台表单列表结算Excel
     *
     * @param map
     * @return
     */
    List<PlatFormOrderSettlementVO> queryPlatFormListSettlementExcel(@Param("refundMap") Map<String, Object> map);

    /**
     * 供应商 对账单 退单列表
     * @param page
     * @param shopId
     * @param dBefore
     * @return
     */
    IPage<ReturnBaseListVO> selectSupplyWaitReturnList(Page<ReturnBaseListVO> page, @Param("shopId") String shopId, @Param("dBefore") Date dBefore);

    /**
     * 供应商中心未结算业绩订单查询
     * @param page
     * @param shopId
     * @param dBefore
     * @return
     */
    IPage<OrderBaseListVO> selectSupplyWaitOrderList(Page<OrderBaseListVO> page, @Param("shopId")String shopId, @Param("dBefore")Date dBefore);

    /**
     * 获取单个店铺所有未结算的经销订单金额
     * @param shopId
     * @param dBefore
     * @return
     */
    ShopSettleAccountInfoDTO getSupplySettleAccountOrder(@Param("shopId") String shopId, @Param("dBefore") Date dBefore);

    /**
     * 获取单个店铺所有未结算的退单金额
     * @param shopId
     * @param dBefore
     * @return
     */
    ShopSettleAccountInfoDTO getSupplySettleAccountReturn(@Param("shopId")String shopId, @Param("dBefore")Date dBefore);


    /**
     * 获取单个店铺所有未结算的经销订单金额
     * @param shopId
     * @param dBefore
     * @return
     */
    ShopSettleAccountInfoDTO getSettleDealerAccountOrder(@Param("shopId") Integer shopId, @Param("dBefore") Date dBefore);


    /**
     * 获取单个店铺所有未结算的退单金额
     * @param shopId
     * @param dBefore
     * @return
     */
    ShopSettleAccountInfoDTO getSettleDealerAccountReturn(@Param("shopId") Integer shopId, @Param("dBefore") Date dBefore);


    /**
     * 更改结算状态
     *
     * @param settlementId
     * @param shopId
     */
    void updateSettlementOrder(String settlementId, Integer shopId);


    /**
     * 查询指定商店的网上销售总额。
     *
     * @param shopId 商店的唯一标识ID，用于指定查询的商店。
     * @return 返回指定商店的所有网上销售订单的总金额，以BigDecimal形式表示，确保精确的货币计算。
     */
    BigDecimal queryWebPostAmount(@Param("shopId") Integer shopId);

    /**
     * 查询指定商店的网上销售退货总额。
     *
     * @param shopId 商店的唯一标识ID，用于指定查询的商店。
     * @return 返回指定商店的所有网上销售退货订单的总金额，以BigDecimal形式表示，确保精确的货币计算。
     */
    BigDecimal queryWebPostReturnAmount(@Param("shopId") Integer shopId);

    /**
     * 更新订单结算状态
     *
     * @param list
     * @return
     */
    boolean updateOrderSettlementStatus(@Param("list") List<String> list);


    /**
     * 更新结算状态
     *
     * @param list
     * @return
     */
    boolean updateSettlementStatus(@Param("list") List<String> list);

    /**
     *支票结算金额
/**
     * @param shopId
     * @return
     */
    boolean checkSettlementAmount(@Param("shopId") String shopId);

    /**
     * 查询供应商订单数据
     * @param mapAccount
     * @return
     */
    SupplierShopSettleAccountInfoDTO getSettleAccountOrderFeign(@Param("param")Map<String, Object> mapAccount);

    /**
     * 查询供应商订单id列表
     * @param mapAccount
     * @return
     */
    List<String> getSettleAccountOrderIdListFeign(@Param("param")Map<String, Object> mapAccount);

    /**
     * 查询供应商退单数据
     * @param mapAccount
     * @return
     */
    SupplierShopSettleAccountInfoDTO getSettleAccountReturnOrderFeign(@Param("param")Map<String, Object> mapAccount);

    /**
     * 查询供应商退单id列表
     * @param mapAccount
     * @return
     */
    List<String> getSettleAccountReturnOrderIdListFeign(@Param("param")Map<String, Object> mapAccount);
}




