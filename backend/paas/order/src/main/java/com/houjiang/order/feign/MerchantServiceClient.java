package com.houjiang.order.feign;

import com.houjiang.commons.dto.goods.ManageShopAddressDTO;
import com.houjiang.commons.dto.order.ManageShopAddressDetailDTO;
import com.houjiang.commons.dto.shop.ShopUserDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.shop.GdsShopDetailResultVO;
import com.houjiang.commons.vo.shop.ShopUserMessageResultVO;
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
     * 查询商品运费信息
     *
     * @param message 包含商品信息和配送区域的消息字符串
     * @return 运费查询结果
     */
    // TODO: merchant 服务需要创建对应的内部 API 接口
    /**
     * TODO 补充方法说明
     * @param message TODO
     * @return TODO
     */
    @PostMapping("/merchant/internal/queryShipping")
    ResponseResult queryShipping(@RequestBody String message);

    /**
     * 查询供应商运费信息
     *
     * @param message 包含供应商商品信息和配送区域的消息字符串
     * @return 运费查询结果
     */
    // TODO: merchant 服务需要创建对应的内部 API 接口
    /**
     * TODO 补充方法说明
     * @param message TODO
     * @return TODO
     */
    @PostMapping("/merchant/internal/querySupplierShipping")
    ResponseResult querySupplierShipping(@RequestBody String message);

    /**
     * 查询店铺是否自营
     *
     * @param shopId 店铺ID
     * @return 是否自营
     */
    // TODO: merchant 服务需要创建对应的内部 API 接口
    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @GetMapping("/merchant/internal/queryShopSelfSupport")
    Boolean queryShopSelfSupport(@RequestParam("shopId") Integer shopId);

    /**
     * 查询店铺经营类目ID列表
     *
     * @param shopId 店铺ID
     * @return 经营类目ID列表
     */
    // TODO: merchant 服务需要创建对应的内部 API 接口
    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @GetMapping("/merchant/internal/queryShopGoodsCatIds")
    List<Integer> queryShopGoodsCatIds(@RequestParam("shopId") Integer shopId);

    /**
     * 根据商品ID查询商品所属经营类目
     *
     * @param goodsId 商品ID
     * @return 类目ID
     */
    // TODO: merchant 服务需要创建对应的内部 API 接口
    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @GetMapping("/merchant/internal/queryGoodsCatByGoodsId")
    Integer queryGoodsCatByGoodsId(@RequestParam("goodsId") Integer goodsId);

    /**
     * 获取商品详情
     *
     * @param goodsId 商品ID
     * @return 商品详情
     */
    // TODO: merchant 服务需要创建对应的内部 API 接口
    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @GetMapping("/merchant/internal/getGoodsDetails")
    ResponseResult getGoodsDetails(@RequestParam("goodsId") Integer goodsId);

    /**
     * 查询店铺发票开关状态
     *
     * @param paramTmp 包含shopId的参数
     * @return 发票开关状态
     */
    // TODO: merchant 服务需要创建对应的内部 API 接口
    /**
     * TODO 补充方法说明
     * @param paramTmp TODO
     * @return TODO
     */
    @PostMapping("/merchant/internal/queryShopInvoiceSwitchStatus")
    ResponseResult queryShopInvoiceSwitchStatus(@RequestBody Map<String, Object> paramTmp);

    /**
     * 根据店铺ID查询店铺用户信息
     *
     * @param shopId 店铺ID
     * @return 店铺用户信息
     */
    // TODO: merchant 服务需要创建对应的内部 API 接口
    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @GetMapping("/merchant/internal/queryUserId")
    ShopUserMessageResultVO queryUserId(@RequestParam("shopId") Integer shopId);

    /**
     * 查询门店自提点ID
     *
     * @param paramMap 查询参数
     * @return 门店自提点信息
     */
    // TODO: merchant 服务需要创建对应的内部 API 接口
    /**
     * TODO 补充方法说明
     * @param paramMap TODO
     * @return TODO
     */
    @PostMapping("/merchant/internal/queryShopGateId")
    ResponseResult queryShopGateId(@RequestBody Map<String, Object> paramMap);

    /**
     * 添加店铺用户
     *
     * @param shopUserDTO 店铺用户信息
     */
    // TODO: merchant 服务需要创建对应的内部 API 接口
    /**
     * TODO 补充方法说明
     * @param shopUserDTO TODO
     */
    @PostMapping("/merchant/internal/addShopUser")
    void addShopUser(@RequestBody ShopUserDTO shopUserDTO);

    /**
     * 更新门店库存
     *
     * @param saleSum 销售汇总信息
     */
    // TODO: merchant 服务需要创建对应的内部 API 接口
    /**
     * TODO 补充方法说明
     * @param saleSum TODO
     */
    @PostMapping("/merchant/internal/updateShopGateStock")
    void updateShopGateStock(@RequestBody String saleSum);

    /**
     * 查询店铺评分
     *
     * @param map 包含shopId的参数
     * @return 店铺评分信息
     */
    // TODO: merchant 服务需要创建对应的内部 API 接口
    /**
     * TODO 补充方法说明
     * @param map TODO
     * @return TODO
     */
    @PostMapping("/merchant/internal/shopScore")
    ResponseResult shopScore(@RequestBody Map<Object, Object> map);

    /**
     * 根据店铺ID查询店铺详情
     *
     * @param shopId 店铺ID
     * @return 店铺详情
     */
    // TODO: merchant 服务需要创建对应的内部 API 接口
    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @GetMapping("/merchant/internal/queryShopinfoByShopId")
    GdsShopDetailResultVO queryShopinfoByShopId(@RequestParam("shopId") Integer shopId);

    /**
     * 查询店铺收货地址详情
     *
     * @param param 收货地址参数
     * @return 收货地址详情
     */
    // TODO: merchant 服务需要创建对应的内部 API 接口
    /**
     * TODO 补充方法说明
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/merchant/internal/queryShopAddressDetail")
    ResponseResult queryShopAddressDetail(@RequestBody ManageShopAddressDetailDTO param);

    /**
     * 根据店铺ID查询发货地址
     *
     * @param shopId 店铺ID
     * @return 发货地址信息
     */
    // TODO: merchant 服务需要创建对应的内部 API 接口
    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @GetMapping("/merchant/internal/queryShippingAddressByShopId")
    ManageShopAddressDTO queryShippingAddressByShopId(@RequestParam("shopId") Integer shopId);

    /**
     * 查询门店详情
     *
     * @param param 包含shopGateId的参数
     * @return 门店详情
     */
    // TODO: merchant 服务需要创建对应的内部 API 接口
    /**
     * TODO 补充方法说明
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/merchant/internal/queryGateDetail")
    ResponseResult queryGateDetail(@RequestBody Map<String, Object> param);

    /**
     * 保存店铺评价信息
     *
     * @param param 评价参数
     * @return 保存结果
     */
    // TODO: merchant 服务需要创建对应的内部 API 接口
    /**
     * TODO 补充方法说明
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/merchant/internal/saveShopEvaluationInfo")
    ResponseResult saveShopEvaluationInfo(@RequestBody Map<String, Object> param);

    /**
     * 更新分销小店销量
     *
     * @param orderId 订单ID
     */
    // TODO: merchant 服务需要创建对应的内部 API 接口
    /**
     * TODO 补充方法说明
     */
    @PostMapping("/merchant/internal/updateVkShopSaleNum")
    void updateVkShopSaleNum(@RequestParam("orderId") String orderId);

    /**
     * 根据店铺ID查询店铺印章
     *
     * @param shopMap 包含shopId的参数
     * @return 店铺印章信息
     */
    // TODO: merchant 服务需要创建对应的内部 API 接口
    /**
     * TODO 补充方法说明
     * @param shopMap TODO
     * @return TODO
     */
    @PostMapping("/merchant/internal/queryShopStampByShopId")
    ResponseResult queryShopStampByShopId(@RequestBody Map<String, Object> shopMap);

    /**
     * 查询发货地址信息
     *
     * @param shippingAddressId 发货地址ID
     * @return 发货地址信息
     */
    // TODO: merchant 服务需要创建对应的内部 API 接口
    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @GetMapping("/merchant/internal/queryShippingAddress")
    ResponseResult queryShippingAddress(@RequestParam("shippingAddressId") Integer shippingAddressId);

    /**
     * 根据门店ID查询门店名称
     *
     * @param orderGateShopId 门店ID
     * @return 门店名称和位置信息
     */
    // TODO: merchant 服务需要创建对应的内部 API 接口
    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @GetMapping("/merchant/internal/queryGateStoreName")
    Map<String, String> queryGateStoreName(@RequestParam("orderGateShopId") Integer orderGateShopId);
}
