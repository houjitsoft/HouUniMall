package com.houjiang.job.mapper;

import com.houjiang.commons.dto.job.ShopSettleAccountInfoDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @ClassName SettleAccountsMapper
 * @Description 账号
 * @Author Amos
 * @Date 2023/5/17 16:24
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface SettleAccountsMapper {

    /**
     * 获取Beform日期
     *
     * @param shopId
     * @param osOrderType
     * @return
     */
    String getBeformDate(@Param("shopId") String shopId, @Param("osOrderType") Integer osOrderType);

    /**
     * 获取结算账户订单
     *
     * @param paraMap
     * @return
     */
    ShopSettleAccountInfoDTO getSettleAccountOrder(Map<String, String> paraMap);

    /**
     * 获取结算账户返回
     *
     * @param paraMap
     * @return
     */
    ShopSettleAccountInfoDTO getSettleAccountReturn(Map<String, String> paraMap);

    /**
     * 新增结算
     *
     * @param paraMap
     * @return
     */
    Integer insertSettlement(Map<String, String> paraMap);

    /**
     * 获取结算账户订单Id
     *
     * @param getMap
     * @return
     */
    List<String> getSettleAccountOrderId(Map<String, String> getMap);

    /**
     * 获取结算账户退单订单Id
     *
     * @param getMap
     * @return
     */
    List<String> getSettleAccountReturnId(Map<String, String> getMap);

    /**
     * 更新订单基础结算
     *
     * @param orderIds
     * @param osId
     * @return
     */
    Integer updateOrderBaseSettlement(@Param("orderIds") List<String> orderIds, @Param("osId") String osId);

    /**
     * 更新订单退货结算
     *
     * @param orderIds
     * @param osId
     * @return
     */
    Integer updateOrderReturnSettlement(@Param("orderIds") List<String> orderIds, @Param("osId") String osId);

    /**
     * 更新店铺的结算时间
     *
     * @param shopId
     */
    void updateShopSettlement(String shopId);

    /**
     * 获取经销商结算账户订单信息
     *
     * 通过传入参数映射表，查询并返回经销商的结算账户订单信息。此方法用于处理与经销商结算相关的订单查询操作，
     * 具体的查询条件和返回数据结构由参数映射表和实际业务逻辑定义。
     *
     * @param paraMap 参数映射表，包含查询所需的条件。每个条件的键值对表示一个查询参数及其对应的值。
     * @return ShopSettleAccountInfoDTO 返回一个包含经销商结算账户订单信息的数据传输对象。
     */
    ShopSettleAccountInfoDTO getDealerSettleAccountOrder(@Param("paraMap") Map<String, String> paraMap);

    /**
     * 获取经销商结算账户返回信息
     *
     * 该方法用于根据传入的参数映射表，查询并返回经销商结算账户的相关信息。这可能包括结算周期、结算金额等信息。
     * 结算信息的详细内容和结构由实际业务逻辑和返回的数据传输对象定义。
     *
     * @param paraMap 参数映射表，包含查询所需的条件。每个条件的键值对表示一个查询参数及其对应的值。
     * @return ShopSettleAccountInfoDTO 返回一个包含经销商结算账户返回信息的数据传输对象。
     */
    ShopSettleAccountInfoDTO getDealerSettleAccountReturn(@Param("paraMap")Map<String, String> paraMap);

    /**
     * 一键上架商品供应商订单结算统计
     * @param paraMap
     * @return
     */
    ShopSettleAccountInfoDTO getSupplySettleAccountOrder(@Param("param") Map<String, String> paraMap);

    /**
     * 一键上架商品供应商退单结算统计
     * @param paraMap
     * @return
     */
    ShopSettleAccountInfoDTO getSupplySettleAccountReturn(@Param("param") Map<String, String> paraMap);

    /**
     *查询需要更新的退款订单集合
/**
     * @param paraMap
     * @return
     */
    List<String> querySupplyOrderBaseInfo(@Param("param") Map<String, String> paraMap);

    /**
     * 更新供应订单基础结算信息。
     *
     * @param paraMap 包含更新结算信息所需参数的映射。
     * @return 更新操作的结果，通常是一个表示成功或失败的整数。
     */
    Integer updateSupplyOrderBaseSettlement(@Param("param")Map<String, Object> paraMap);

    /**
     * 更新供应订单退货结算信息。
     *
     * @param paraMap 包含更新退货结算信息所需参数的映射。
     * @return 更新操作的结果，通常是一个表示成功或失败的整数。
     */
    Integer updateSupplyOrderReturnSettlement(@Param("param")Map<String, Object> paraMap);

    /**
     * 获取结算网页端pos订单信息。
     *
     * @param paraMap 包含用于检索pos订单信息的参数的映射。
     * @return pos订单的结算信息。
     */
    ShopSettleAccountInfoDTO getSettleWebPosOrder(@Param("param") Map<String, String> paraMap);

    /**
     * 获取结算网页端pos退货信息。
     *
     * @param getMap 包含用于检索pos退货信息的参数的映射。
     * @return pos退货的结算信息。
     */
    ShopSettleAccountInfoDTO getSettleWebPosReturn(@Param("param") Map<String, String> getMap);

    /**
     * 获取结算网页端pos订单ID列表。
     *
     * @param getMap 包含用于检索pos订单ID列表的参数的映射。
     * @return pos订单ID的列表。
     */
    List<String> getSettleWebPosOrderId(@Param("param") Map<String, String> getMap);

    /**
     * 获取结算网页端pos退货ID列表。
     *
     * @param getMap 包含用于检索pos退货ID列表的参数的映射。
     * @return pos退货ID的列表。
     */
    List<String> getSettleWebPosReturnId(@Param("param") Map<String, String> getMap);

    /**
     * 更新网页端pos结算信息。
     *
     * @param orderIds 需要更新结算信息的pos订单ID列表。
     * @param osId 结算系统的标识。
     * @return 更新操作的结果，通常是一个表示成功或失败的整数。
     */
    Integer updateWebPosSettlement(@Param("orderIds") List<String> orderIds, @Param("osId") String osId);

    /**
     * 获取结算账户订单
     * @param paraMap
     * @return
     */
    ShopSettleAccountInfoDTO getCombinationOrder(Map<String, String> paraMap);

    /**
     * 根据传入的映射表获取组合订单ID列表。
     *
     * @param getMap 映射表，包含用于生成组合订单ID的关键信息。
     * @return 返回一个字符串列表，包含生成的组合订单ID。
     */
    List<String> getCombinationOrderId(Map<String, String> getMap);

    /**
     * 根据传入的映射表获取组合订单结算信息。
     *
     * @param getMap 映射表，包含用于生成组合订单结算信息的关键数据。
     * @return 返回一个ShopSettleAccountInfoDTO对象，包含组合订单的结算详情。
     */
    ShopSettleAccountInfoDTO getCombinationOrderReturn(Map<String, String> getMap);

    /**
     * 根据传入的映射表获取组合订单返回ID列表。
     *
     * @param getMap 映射表，包含用于生成组合订单返回ID的关键信息。
     * @return 返回一个字符串列表，包含生成的组合订单返回ID。
     */
    List<String> getCombinationOrderReturnId(Map<String, String> getMap);

    /**
     * TODO 补充方法说明
     * @param getMap TODO
     * @return TODO
     */
    ShopSettleAccountInfoDTO getSupplierSettleAccountOrder(Map<String, String> getMap);

    /**
     * TODO 补充方法说明
     * @param getMap TODO
     * @return TODO
     */
    List<String> getSupplierSettleAccountOrderId(Map<String, String> getMap);

    /**
     * TODO 补充方法说明
     * @param getMap TODO
     * @return TODO
     */
    ShopSettleAccountInfoDTO getSupplierSettleAccountReturn(Map<String, String> getMap);

    /**
     * TODO 补充方法说明
     * @param getMap TODO
     * @return TODO
     */
    List<String> getSupplierSettleAccountReturnId(Map<String, String> getMap);

    /**
     * TODO 补充方法说明
     * @param paraMap TODO
     * @return TODO
     */
    Integer insertSupplierSettlement(Map<String, String> paraMap);

    /**
     * 更新结算
     *
     * @param orderIds
     * @param osId
     * @return
     */
    Integer updateOrderBaseSettlement2(@Param("orderIds") List<String> orderIds, @Param("osId") String osId);

    /**
     * 更新结算
     *
     * @param orderIds
     * @param osId
     * @return
     */
    Integer updateOrderReturnSettlement2(@Param("orderIds") List<String> orderIds, @Param("osId") String osId);

    /**
     * TODO 补充方法说明
     * @param shopId TODO
     */
    void updateSupplierShopSettlement(String shopId);

    /**
     * TODO 补充方法说明
     * @param orderIds TODO
     * @return TODO
     */
    ShopSettleAccountInfoDTO getSelfSellingCommission(List<String> orderIds);

    /**
     * TODO 补充方法说明
     * @param orderIds TODO
     * @return TODO
     */
    ShopSettleAccountInfoDTO getSelfSellingReturnCash(List<String> orderIds);

    /**
     * TODO 补充方法说明
     * @param paraMap TODO
     * @return TODO
     */
    ShopSettleAccountInfoDTO getSellOnAccountOrder(Map<String, String> paraMap);

    /**
     * TODO 补充方法说明
     * @param orderIds TODO
     * @return TODO
     */
    ShopSettleAccountInfoDTO getSellOnReturnCash(List<String> orderIds);
}
