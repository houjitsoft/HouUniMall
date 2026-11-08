package com.houjiang.userms.feign;

import com.houjiang.commons.dto.shop.ShopUserDTO;
import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.user.UserInfoVO;
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
     * 查询店铺状态
     * TODO: merchant 服务需要创建对应的内部 API 接口
     *
     * @param shopMap 包含shopId、userId、shopStatus、shopType等参数
     * @return 返回店铺详情信息
     */
    @PostMapping("/merchant/internal/queryShopStatus")
    ResponseResult queryShopStatus(@RequestBody Map<String, String> shopMap);

    /**
     * 查询店铺的营销活动列表
     * TODO: merchant 服务需要创建对应的内部 API 接口
     *
     * @param shopId 店铺ID
     * @return 返回营销活动列表
     */
    @GetMapping("/merchant/internal/queryMarketingActivitiesList")
    List<String> queryMarketingActivitiesList(@RequestParam("shopId") Integer shopId);

    /**
     * 根据店铺ID查询店铺详情
     * TODO: merchant 服务需要创建对应的内部 API 接口
     *
     * @param shopId 店铺ID
     * @return 返回店铺详情结果VO
     */
    @GetMapping("/merchant/internal/queryShopinfoByShopId")
    com.houjiang.commons.vo.shop.GdsShopDetailResultVO queryShopinfoByShopId(@RequestParam("shopId") Integer shopId);

    /**
     * 添加微客店铺基础信息
     * TODO: merchant 服务需要创建对应的内部 API 接口
     *
     * @param userInfo 用户信息
     * @return 返回操作结果
     */
    @PostMapping("/merchant/internal/insertVkShopBase")
    ResponseResult insertVkShopBase(@RequestBody UserInfoVO userInfo);

    /**
     * 更新店铺收藏状态
     * TODO: merchant 服务需要创建对应的内部 API 接口
     *
     * @param paraMap 包含shopId、type等参数
     * @return 返回操作结果
     */
    @PostMapping("/merchant/internal/updateShopCollect")
    ResponseResult updateShopCollect(@RequestBody Map<String, String> paraMap);

    /**
     * 添加店铺会员
     * TODO: merchant 服务需要创建对应的内部 API 接口
     *
     * @param shopUserDTO 店铺用户信息
     * @return 返回操作结果
     */
    @PostMapping("/merchant/internal/addShopUser")
    ResponseResult addShopUser(@RequestBody ShopUserDTO shopUserDTO);

    /**
     * 查询用户剩余的图片上传空间
     * TODO: merchant 服务需要创建对应的内部 API 接口
     *
     * @param userTokenBeanDTO 用户Token信息
     * @return 返回图片空间限制信息
     */
    @PostMapping("/merchant/internal/queryAddImageSpaceLimit")
    ResponseResult queryAddImageSpaceLimit(@RequestBody UserTokenBeanDTO userTokenBeanDTO);

    /**
     * 根据用户ID查询门店店铺ID
     * TODO: merchant 服务需要创建对应的内部 API 接口
     *
     * @param gateMap 包含userId等参数
     * @return 返回门店店铺ID信息
     */
    @PostMapping("/merchant/internal/queryGateShopIdByUserId")
    ResponseResult queryGateShopIdByUserId(@RequestBody Map<String, String> gateMap);
}
