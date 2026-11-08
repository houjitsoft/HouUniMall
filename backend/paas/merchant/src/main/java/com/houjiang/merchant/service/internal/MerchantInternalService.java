package com.houjiang.merchant.service.internal;

import com.houjiang.commons.dto.goods.ManageShopAddressDTO;
import com.houjiang.commons.dto.goods.ShopListQueryDTO;
import com.houjiang.commons.dto.order.ManageShopAddressDetailDTO;
import com.houjiang.commons.dto.payment.AccountPeriodParamDTO;
import com.houjiang.commons.dto.shop.ArticleShopDTO;
import com.houjiang.commons.dto.shop.ShopUserDTO;
import com.houjiang.commons.dto.shop.WebShopListQueryEsDTO;
import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.shop.*;
import com.houjiang.commons.vo.user.UserInfoVO;

import java.util.List;
import java.util.Map;

/**
 * 服务间内部店铺 API（承接原 business Feign）
 */
public interface MerchantInternalService {

    ResponseResult queryShopStatus(Map<String, String> shopMap);

    String selectShopNameByShopId(String shopId);

    ResponseResult queryInvoiceSwitch(Map<String, Object> param);

    ResponseResult delShopInfo(Map<String, String> map);

    ResponseResult queryGateShopIdByUserId(Map<String, String> gateMap);

    GdsShopDetailResultVO queryShopinfoByShopId(Integer shopId);

    List<String> queryMarketingActivitiesList(Integer shopId);

    ResponseResult insertVkShopBase(UserInfoVO userInfo);

    ResponseResult updateShopCollect(Map<String, String> paraMap);

    ResponseResult addShopUser(ShopUserDTO shopUserDTO);

    ResponseResult queryAddImageSpaceLimit(UserTokenBeanDTO userTokenBeanDTO);

    String getUserIDByShopId(Integer shopId);

    ResponseResult selectTemplateById(Integer templateId);

    ResponseResult selectAreaById(Integer areaId);

    void updateVkShopSaleNum(String orderId);

    ResponseResult recharge(Map<Object, Object> map);

    ShopUserMessageResultVO queryUserId(Integer shopId);

    ShopBankMessageVO getShopBankMessage(String shopId);

    AccountPeriodVO getAccountPeriodByIds(Integer shopId, String userId);

    void updateAccountPeriod(AccountPeriodParamDTO periodParam);

    ResponseResult getShopList(Integer status);

    ResponseResult getShopById(Integer shopId);

    ResponseResult queryShipping(String message);

    ResponseResult querySupplierShipping(String message);

    Boolean queryShopSelfSupport(Integer shopId);

    List<Integer> queryShopGoodsCatIds(Integer shopId);

    Integer queryGoodsCatByGoodsId(Integer goodsId);

    ResponseResult getGoodsDetails(Integer goodsId);

    ResponseResult queryShopInvoiceSwitchStatus(Map<String, Object> paramTmp);

    ResponseResult queryShopGateId(Map<String, Object> paramMap);

    void updateShopGateStock(String saleSum);

    ResponseResult shopScore(Map<Object, Object> map);

    ResponseResult queryShopAddressDetail(ManageShopAddressDetailDTO param);

    ManageShopAddressDTO queryShippingAddressByShopId(Integer shopId);

    ResponseResult queryGateDetail(Map<String, Object> param);

    ResponseResult saveShopEvaluationInfo(Map<String, Object> param);

    ResponseResult queryShopStampByShopId(Map<String, Object> shopMap);

    ResponseResult queryShippingAddress(Integer shippingAddressId);

    Map<String, String> queryGateStoreName(Integer orderGateShopId);

    List<Integer> queryShopIdByShopName(String shopName);

    WebShopListQueryEsDTO selectByShopId2(Integer shopId);

    InvoiceSwitchEntityVO queryShopInvoiceSwitch(Map<String, Object> paramTmp);

    Integer queryUserIsGate(Map<String, String> map);

    void setShopViewVolume(String userId, Integer shopId, Integer type, Integer goodsId);

    ResponseResult shopIndex(Integer shopId);

    List<TransportTemplateVO> selectTransportByShopId(Integer shopId);

    void deleteBindGoodsCat(Integer catId);

    void deleteClassBindTmp(Integer catId);

    List<ArticleShopDTO> selectAllShop();

    ShopListQueryDTO selectByH5ShopId(Integer shopId);

    ManageShopBaseVO selectGdsShopBaseByShopId(Integer shopId);

    List<Integer> queryJoinCat(Integer shopId);
}
