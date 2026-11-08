package com.houjiang.merchant.controller.internal;

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
import com.houjiang.merchant.service.internal.MerchantInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 服务间内部接口（替代原 business 服务 Feign 入口）
 */
@RestController
@RequestMapping("/merchant/internal")
@RequiredArgsConstructor
public class MerchantInternalController {

    private final MerchantInternalService merchantInternalService;

    @PostMapping("/queryShopStatus")
    public ResponseResult queryShopStatus(@RequestBody Map<String, String> shopMap) {
        return merchantInternalService.queryShopStatus(shopMap);
    }

    @PostMapping("/selectShopName")
    public String selectShopName(@RequestParam("shopId") String shopId) {
        return merchantInternalService.selectShopNameByShopId(shopId);
    }

    @GetMapping("/selectShopName")
    public String selectShopNameGet(@RequestParam("shopId") String shopId) {
        return merchantInternalService.selectShopNameByShopId(shopId);
    }

    @PostMapping("/queryShopNameByShopId")
    public String queryShopNameByShopId(@RequestParam("shopId") Integer shopId) {
        return merchantInternalService.selectShopNameByShopId(String.valueOf(shopId));
    }

    @PostMapping("/queryInvoiceSwitch")
    public ResponseResult queryInvoiceSwitch(@RequestBody Map<String, Object> param) {
        return merchantInternalService.queryInvoiceSwitch(param);
    }

    @PostMapping("/delShopInfo")
    public ResponseResult delShopInfo(@RequestBody Map<String, String> map) {
        return merchantInternalService.delShopInfo(map);
    }

    @PostMapping("/queryGateShopIdByUserId")
    public ResponseResult queryGateShopIdByUserId(@RequestBody Map<String, String> gateMap) {
        return merchantInternalService.queryGateShopIdByUserId(gateMap);
    }

    @GetMapping("/queryShopinfoByShopId")
    public GdsShopDetailResultVO queryShopinfoByShopId(@RequestParam("shopId") Integer shopId) {
        return merchantInternalService.queryShopinfoByShopId(shopId);
    }

    @GetMapping("/queryMarketingActivitiesList")
    public List<String> queryMarketingActivitiesList(@RequestParam("shopId") Integer shopId) {
        return merchantInternalService.queryMarketingActivitiesList(shopId);
    }

    @PostMapping("/insertVkShopBase")
    public ResponseResult insertVkShopBase(@RequestBody UserInfoVO userInfo) {
        return merchantInternalService.insertVkShopBase(userInfo);
    }

    @PostMapping("/updateShopCollect")
    public ResponseResult updateShopCollect(@RequestBody Map<String, String> paraMap) {
        return merchantInternalService.updateShopCollect(paraMap);
    }

    @PostMapping("/addShopUser")
    public ResponseResult addShopUser(@RequestBody ShopUserDTO shopUserDTO) {
        return merchantInternalService.addShopUser(shopUserDTO);
    }

    @PostMapping("/queryAddImageSpaceLimit")
    public ResponseResult queryAddImageSpaceLimit(@RequestBody UserTokenBeanDTO userTokenBeanDTO) {
        return merchantInternalService.queryAddImageSpaceLimit(userTokenBeanDTO);
    }

    @GetMapping("/getUserIDByShopId")
    public String getUserIDByShopId(@RequestParam("shopId") Integer shopId) {
        return merchantInternalService.getUserIDByShopId(shopId);
    }

    @GetMapping("/selectTemplateById")
    public ResponseResult selectTemplateById(@RequestParam("templateId") Integer templateId) {
        return merchantInternalService.selectTemplateById(templateId);
    }

    @GetMapping("/selectAreaById")
    public ResponseResult selectAreaById(
            @RequestParam(value = "areaId", required = false) Integer areaId,
            @RequestParam(value = "transportAreaId", required = false) Integer transportAreaId) {
        return merchantInternalService.selectAreaById(areaId != null ? areaId : transportAreaId);
    }

    @PostMapping("/updateVkShopSaleNum")
    public void updateVkShopSaleNum(@RequestParam("orderId") String orderId) {
        merchantInternalService.updateVkShopSaleNum(orderId);
    }

    @PostMapping("/recharge")
    public ResponseResult recharge(@RequestBody Map<Object, Object> map) {
        return merchantInternalService.recharge(map);
    }

    @GetMapping("/queryUserId")
    public ShopUserMessageResultVO queryUserId(@RequestParam("shopId") Integer shopId) {
        return merchantInternalService.queryUserId(shopId);
    }

    @GetMapping("/getShopBankMessage")
    public ShopBankMessageVO getShopBankMessage(@RequestParam("shopId") String shopId) {
        return merchantInternalService.getShopBankMessage(shopId);
    }

    @GetMapping("/getAccountPeriodByIds")
    public AccountPeriodVO getAccountPeriodByIds(@RequestParam("shopId") Integer shopId,
                                                 @RequestParam("userId") String userId) {
        return merchantInternalService.getAccountPeriodByIds(shopId, userId);
    }

    @PostMapping("/updateAccountPeriod")
    public void updateAccountPeriod(@RequestBody AccountPeriodParamDTO periodParam) {
        merchantInternalService.updateAccountPeriod(periodParam);
    }

    @GetMapping("/getShopList")
    public ResponseResult getShopList(@RequestParam("status") Integer status) {
        return merchantInternalService.getShopList(status);
    }

    @GetMapping("/getShopById")
    public ResponseResult getShopById(@RequestParam("shopId") Integer shopId) {
        return merchantInternalService.getShopById(shopId);
    }

    @PostMapping("/queryShipping")
    public ResponseResult queryShipping(@RequestBody String message) {
        return merchantInternalService.queryShipping(message);
    }

    @PostMapping("/querySupplierShipping")
    public ResponseResult querySupplierShipping(@RequestBody String message) {
        return merchantInternalService.querySupplierShipping(message);
    }

    @GetMapping("/queryShopSelfSupport")
    public Boolean queryShopSelfSupport(@RequestParam("shopId") Integer shopId) {
        return merchantInternalService.queryShopSelfSupport(shopId);
    }

    @GetMapping("/queryShopGoodsCatIds")
    public List<Integer> queryShopGoodsCatIds(@RequestParam("shopId") Integer shopId) {
        return merchantInternalService.queryShopGoodsCatIds(shopId);
    }

    @GetMapping("/queryGoodsCatByGoodsId")
    public Integer queryGoodsCatByGoodsId(@RequestParam("goodsId") Integer goodsId) {
        return merchantInternalService.queryGoodsCatByGoodsId(goodsId);
    }

    @GetMapping("/getGoodsDetails")
    public ResponseResult getGoodsDetails(@RequestParam("goodsId") Integer goodsId) {
        return merchantInternalService.getGoodsDetails(goodsId);
    }

    @PostMapping("/queryShopInvoiceSwitchStatus")
    public ResponseResult queryShopInvoiceSwitchStatus(@RequestBody Map<String, Object> paramTmp) {
        return merchantInternalService.queryShopInvoiceSwitchStatus(paramTmp);
    }

    @PostMapping("/queryShopGateId")
    public ResponseResult queryShopGateId(@RequestBody Map<String, Object> paramMap) {
        return merchantInternalService.queryShopGateId(paramMap);
    }

    @PostMapping("/updateShopGateStock")
    public void updateShopGateStock(@RequestBody String saleSum) {
        merchantInternalService.updateShopGateStock(saleSum);
    }

    @PostMapping("/shopScore")
    public ResponseResult shopScore(@RequestBody Map<Object, Object> map) {
        return merchantInternalService.shopScore(map);
    }

    @PostMapping("/queryShopAddressDetail")
    public ResponseResult queryShopAddressDetail(@RequestBody ManageShopAddressDetailDTO param) {
        return merchantInternalService.queryShopAddressDetail(param);
    }

    @GetMapping("/queryShippingAddressByShopId")
    public ManageShopAddressDTO queryShippingAddressByShopId(@RequestParam("shopId") Integer shopId) {
        return merchantInternalService.queryShippingAddressByShopId(shopId);
    }

    @PostMapping("/queryGateDetail")
    public ResponseResult queryGateDetail(@RequestBody Map<String, Object> param) {
        return merchantInternalService.queryGateDetail(param);
    }

    @PostMapping("/saveShopEvaluationInfo")
    public ResponseResult saveShopEvaluationInfo(@RequestBody Map<String, Object> param) {
        return merchantInternalService.saveShopEvaluationInfo(param);
    }

    @PostMapping("/queryShopStampByShopId")
    public ResponseResult queryShopStampByShopId(@RequestBody Map<String, Object> shopMap) {
        return merchantInternalService.queryShopStampByShopId(shopMap);
    }

    @GetMapping("/queryShippingAddress")
    public ResponseResult queryShippingAddress(@RequestParam("shippingAddressId") Integer shippingAddressId) {
        return merchantInternalService.queryShippingAddress(shippingAddressId);
    }

    @GetMapping("/queryGateStoreName")
    public Map<String, String> queryGateStoreName(@RequestParam("orderGateShopId") Integer orderGateShopId) {
        return merchantInternalService.queryGateStoreName(orderGateShopId);
    }

    @GetMapping("/queryShopIdByShopName")
    public List<Integer> queryShopIdByShopName(@RequestParam("shopName") String shopName) {
        return merchantInternalService.queryShopIdByShopName(shopName);
    }

    @GetMapping("/selectByShopId2")
    public WebShopListQueryEsDTO selectByShopId2(@RequestParam("shopId") Integer shopId) {
        return merchantInternalService.selectByShopId2(shopId);
    }

    @PostMapping("/queryShopInvoiceSwitch")
    public InvoiceSwitchEntityVO queryShopInvoiceSwitch(@RequestBody Map<String, Object> paramTmp) {
        return merchantInternalService.queryShopInvoiceSwitch(paramTmp);
    }

    @PostMapping("/queryUserIsGate")
    public Integer queryUserIsGate(@RequestBody Map<String, String> map) {
        return merchantInternalService.queryUserIsGate(map);
    }

    @PostMapping("/setShopViewVolume")
    public void setShopViewVolume(@RequestParam("userId") String userId,
                                  @RequestParam("shopId") Integer shopId,
                                  @RequestParam("type") Integer type,
                                  @RequestParam("goodsId") Integer goodsId) {
        merchantInternalService.setShopViewVolume(userId, shopId, type, goodsId);
    }

    @GetMapping("/shopIndex")
    public ResponseResult shopIndex(@RequestParam("shopId") Integer shopId) {
        return merchantInternalService.shopIndex(shopId);
    }

    @GetMapping("/selectByShopId")
    public List<TransportTemplateVO> selectByShopId(@RequestParam("shopId") Integer shopId) {
        return merchantInternalService.selectTransportByShopId(shopId);
    }

    @PostMapping("/deleteBindGoodsCat")
    public void deleteBindGoodsCat(@RequestParam("catId") Integer catId) {
        merchantInternalService.deleteBindGoodsCat(catId);
    }

    @PostMapping("/deleteClassBindTmp")
    public void deleteClassBindTmp(@RequestParam("catId") Integer catId) {
        merchantInternalService.deleteClassBindTmp(catId);
    }

    @GetMapping("/selectAllShop")
    public List<ArticleShopDTO> selectAllShop() {
        return merchantInternalService.selectAllShop();
    }

    @GetMapping("/selectByH5ShopId")
    public ShopListQueryDTO selectByH5ShopId(@RequestParam("shopId") Integer shopId) {
        return merchantInternalService.selectByH5ShopId(shopId);
    }

    @GetMapping("/selectGdsShopBaseByShopId")
    public ManageShopBaseVO selectGdsShopBaseByShopId(@RequestParam("shopId") Integer shopId) {
        return merchantInternalService.selectGdsShopBaseByShopId(shopId);
    }

    @GetMapping("/queryJoinCat")
    public List<Integer> queryJoinCat(@RequestParam("shopId") Integer shopId) {
        return merchantInternalService.queryJoinCat(shopId);
    }
}
