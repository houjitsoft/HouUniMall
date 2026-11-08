package com.houjiang.goods.feign;

import com.houjiang.commons.dto.shop.ArticleShopDTO;
import com.houjiang.commons.dto.goods.ShopListQueryDTO;
import com.houjiang.commons.dto.shop.WebShopListQueryEsDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.shop.GdsShopDetailResultVO;
import com.houjiang.commons.vo.shop.InvoiceSwitchEntityVO;
import com.houjiang.commons.vo.shop.ManageShopBaseVO;
import com.houjiang.commons.vo.shop.TransportTemplateVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 商户服务Feign客户端
 * 替代原 BusinessServiceClient，指向新的 merchant 微服务
 * @author Amos
 */
@FeignClient("merchant")
public interface MerchantServiceClient {

    /**
     * 查询店铺发票开关信息
     *
     * @param paramTmp 包含shopId的参数
     * @return 发票开关实体
     */
    // 该接口已由 merchant 的 MerchantInternalController 提供（/merchant/internal/**），路径不要改。
    /**
     * @param paramTmp 参数tmp
     * @return 处理结果
     */
    @PostMapping("/merchant/internal/queryShopInvoiceSwitch")
    InvoiceSwitchEntityVO queryShopInvoiceSwitch(@RequestBody Map<String, Object> paramTmp);

    /**
     * 根据店铺ID查询店铺详情
     *
     * @param shopId 店铺ID
     * @return 店铺详情
     */
    // 该接口已由 merchant 的 MerchantInternalController 提供（/merchant/internal/**），路径不要改。
    /**
     * @return 处理结果
     */
    @GetMapping("/merchant/internal/queryShopinfoByShopId")
    GdsShopDetailResultVO queryShopinfoByShopId(@RequestParam("shopId") Integer shopId);

    /**
     * 查询用户是否是门店账号
     *
     * @param map 包含userMobile的参数
     * @return 门店店铺ID，null表示不是门店账号
     */
    // 该接口已由 merchant 的 MerchantInternalController 提供（/merchant/internal/**），路径不要改。
    /**
     * @param map map
     * @return 处理结果
     */
    @PostMapping("/merchant/internal/queryUserIsGate")
    Integer queryUserIsGate(@RequestBody Map<String, String> map);

    /**
     * 设置店铺浏览量
     *
     * @param userId 用户ID
     * @param shopId 店铺ID
     * @param type 类型
     * @param goodsId 商品ID
     */
    // 该接口已由 merchant 的 MerchantInternalController 提供（/merchant/internal/**），路径不要改。
    @PostMapping("/merchant/internal/setShopViewVolume")
    void setShopViewVolume(@RequestParam("userId") String userId, @RequestParam("shopId") Integer shopId,
                           @RequestParam("type") Integer type, @RequestParam("goodsId") Integer goodsId);

    /**
     * 查询店铺首页信息
     *
     * @param shopId 店铺ID
     * @return 店铺首页信息
     */
    // 该接口已由 merchant 的 MerchantInternalController 提供（/merchant/internal/**），路径不要改。
    /**
     * @return 处理结果
     */
    @GetMapping("/merchant/internal/shopIndex")
    ResponseResult shopIndex(@RequestParam("shopId") Integer shopId);

    /**
     * 根据模板ID查询运费模板
     *
     * @param templateId 运费模板ID
     * @return 运费模板信息
     */
    // 该接口已由 merchant 的 MerchantInternalController 提供（/merchant/internal/**），路径不要改。
    /**
     * @return 处理结果
     */
    @GetMapping("/merchant/internal/selectTemplateById")
    ResponseResult selectTemplateById(@RequestParam("templateId") Integer templateId);

    /**
     * 根据店铺ID查询运费模板列表
     *
     * @param shopId 店铺ID
     * @return 运费模板列表
     */
    // 该接口已由 merchant 的 MerchantInternalController 提供（/merchant/internal/**），路径不要改。
    /**
     * @return 处理结果
     */
    @GetMapping("/merchant/internal/selectByShopId")
    List<TransportTemplateVO> selectByShopId(@RequestParam("shopId") Integer shopId);

    /**
     * 查询店铺评分
     *
     * @param map 包含shopId的参数
     * @return 店铺评分信息
     */
    // 该接口已由 merchant 的 MerchantInternalController 提供（/merchant/internal/**），路径不要改。
    /**
     * @param map map
     * @return 处理结果
     */
    @PostMapping("/merchant/internal/shopScore")
    ResponseResult shopScore(@RequestBody Map<Object, Object> map);

    /**
     * 根据区域ID查询运输区域信息
     *
     * @param transportAreaId 运输区域ID
     * @return 区域信息
     */
    // 该接口已由 merchant 的 MerchantInternalController 提供（/merchant/internal/**），路径不要改。
    /**
     * @return 处理结果
     */
    @GetMapping("/merchant/internal/selectAreaById")
    ResponseResult selectAreaById(@RequestParam("transportAreaId") Integer transportAreaId);

    /**
     * 删除绑定的入驻分类
     *
     * @param catId 分类ID
     */
    // 该接口已由 merchant 的 MerchantInternalController 提供（/merchant/internal/**），路径不要改。
    @PostMapping("/merchant/internal/deleteBindGoodsCat")
    void deleteBindGoodsCat(@RequestParam("catId") Integer catId);

    /**
     * 删除分类绑定模板
     *
     * @param catId 分类ID
     */
    // 该接口已由 merchant 的 MerchantInternalController 提供（/merchant/internal/**），路径不要改。
    @PostMapping("/merchant/internal/deleteClassBindTmp")
    void deleteClassBindTmp(@RequestParam("catId") Integer catId);

    /**
     * 查询所有店铺信息
     *
     * @return 店铺信息列表
     */
    // 该接口已由 merchant 的 MerchantInternalController 提供（/merchant/internal/**），路径不要改。
    /**
     * @return 处理结果
     */
    @GetMapping("/merchant/internal/selectAllShop")
    List<ArticleShopDTO> selectAllShop();

    /**
     * 根据店铺ID查询店铺信息（PC端）
     *
     * @param shopId 店铺ID
     * @return 店铺信息
     */
    // 该接口已由 merchant 的 MerchantInternalController 提供（/merchant/internal/**），路径不要改。
    /**
     * @return 处理结果
     */
    @GetMapping("/merchant/internal/selectByShopId2")
    WebShopListQueryEsDTO selectByShopId2(@RequestParam("shopId") Integer shopId);

    /**
     * 根据H5店铺ID查询店铺信息
     *
     * @param shopId 店铺ID
     * @return 店铺信息
     */
    // 该接口已由 merchant 的 MerchantInternalController 提供（/merchant/internal/**），路径不要改。
    /**
     * @return 处理结果
     */
    @GetMapping("/merchant/internal/selectByH5ShopId")
    ShopListQueryDTO selectByH5ShopId(@RequestParam("shopId") Integer shopId);

    /**
     * 根据店铺ID查询店铺基本信息
     *
     * @param shopId 店铺ID
     * @return 店铺基本信息
     */
    // 该接口已由 merchant 的 MerchantInternalController 提供（/merchant/internal/**），路径不要改。
    /**
     * @return 处理结果
     */
    @GetMapping("/merchant/internal/selectGdsShopBaseByShopId")
    ManageShopBaseVO selectGdsShopBaseByShopId(@RequestParam("shopId") Integer shopId);

    /**
     * 查询店铺入驻分类
     *
     * @param shopId 店铺ID
     * @return 入驻分类ID列表
     */
    // 该接口已由 merchant 的 MerchantInternalController 提供（/merchant/internal/**），路径不要改。
    /**
     * @return 处理结果
     */
    @GetMapping("/merchant/internal/queryJoinCat")
    List<Integer> queryJoinCat(@RequestParam("shopId") Integer shopId);
}
