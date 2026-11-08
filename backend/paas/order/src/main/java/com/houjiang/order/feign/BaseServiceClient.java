package com.houjiang.order.feign;

import com.houjiang.commons.dto.base.BaseSystemMessageDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.base.PointsCashRatioVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author Amos
 * @date 2023/3/20 15:49
 */
@FeignClient("base")
public interface BaseServiceClient {
    /**
     * 根据会员等级id查询会员等级
     * @param id
     * @return
     */
    @PostMapping("/base/membershipGrade/getMemberNameByClassId")
    ResponseResult getMemberNameByClassId(@RequestParam("id") String id);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @PostMapping("baseDealerSet/settingDistribute/query")
    ResponseResult settingDistribute();

    /**
     * 查询微客配置
     * @return
     */
    @GetMapping("baseVkSet/queryGoodsVkSet")
    ResponseResult queryGoodsVkSet();


    /**
     * 查询plus 会员规则  feign
     */
    @PostMapping("base/merchantPlusRule/query")
    ResponseResult queryMerchantPlusRule();

    /**
     * 查询积分规则
     *
     * @return
     */
    @PostMapping("/base/integralRules/queryIntegralRules")
    ResponseResult queryIntegralRules();

    /**
     * 查询快递表信息
     *
     * @param orderShippingExpressId
     * @return
     */
    @PostMapping("/base/kdniaoExpresss/query/kdniaoExpresssInfo")
    ResponseResult queryKdniaoExpresssInfo(@RequestBody Integer orderShippingExpressId);

    /**
     * 查询快递公司列表
     *
     * @param map
     * @return
     */
    @PostMapping("/base/express/list/query")
    ResponseResult queryExpressList(@RequestBody Map<String, Object> map);

    /**
     * 根据id查询快递信息
     *
     * @param expressId
     * @return
     */
    @GetMapping("/base/express/getInfoById")
    ResponseResult getBaseExpressInfoById(@RequestParam("expressId") Integer expressId);


    /**
     * 查询快递鸟配置  服务间feign 调用使用
     *
     * @return
     */
    @PostMapping("/base/kdniaoConfig/feign/queryConfig")
    ResponseResult queryConfigFeign();

    /**
     * 得到地址信息
     *
     * @param provinceId
     * @param cityId
     * @param districtId
     * @return
     */
    @GetMapping("/base/district/getAddressString")
    ResponseResult getAddressString(@RequestParam("province") Integer provinceId, @RequestParam("city") Integer cityId,
                            @RequestParam("county") Integer districtId);


    /**
     * 查询地区表所对应的名称
     *
     * @param districtMap
     * @return
     */
    @PostMapping("/base/district/queryRegionName")
    List<String> queryRegionName(@RequestBody Map<String, Integer> districtMap);

    /**
     * 查询地区
     * @param provinceId
     * @return
     */
    @PostMapping("/base/district/queryProvinceport")
    String queryProvinceport(@RequestParam("provinceId") String provinceId);

    /**
     * 根据configKey 查询配置 feign
     */
    @PostMapping("base/sysConfig/queryConfigKeyFeign")
    String selectConfigValueFeign(@RequestParam("configKey") String configKey);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @PostMapping("/baseVkSet/updateOrderAfterSale")
    ResponseResult updateOrderAfterSale(@RequestParam("orderId") String orderId);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @GetMapping("/base/express/queryBaseExpressById")
    String queryBaseExpressById(@RequestParam("orderShippingExpressId") Integer orderShippingExpressId);

    /**
     * 根据configKey查configValue
     *
     * @param configKey
     * @return
     */
    @GetMapping("/base/sysConfig/selectConfigValueByKey")
    String selectConfigValueByKey(@RequestParam("configKey") String configKey);

    /**
     * 添加系统下单消息
     *
     * @param baseSystemMessageDTO
     * @return
     */
    @PostMapping({"base/systemMessage/add/sysOrderNews"})
    ResponseResult addSysOrderNews(@RequestBody BaseSystemMessageDTO baseSystemMessageDTO);
    /**
     * 查询配置
     * @param configKey
     * @return
     */
    @GetMapping("/base/webConfig/delivery/config/{configKey}")
    ResponseResult deliveryDetail(@PathVariable("configKey") String configKey);

    /**
     * 检测应用中心状态
     *
     * @return
     */
    @RequestMapping("/web/index/detectingApplicationStatus")
    ResponseResult detectingApplicationStatus();

    /**
     * 使用POST方法发送消息模板
     *
     * @param param 包含消息模板参数的Map对象，通过请求体发送
     * @return 返回服务器的响应结果
     * <p>
     * 通过此端点，客户端可以提交一个包含消息模板所需参数的Map对象
     * 具体的参数和逻辑处理由baseMessageTemplateService的sendMessageTemplate方法实现
     */
    @PostMapping("/baseMessageTemplate/sendMessageTemplate")
    ResponseResult sendMessageTemplate(@RequestBody Map<String, Object> param);

   /**
	 * 检测Im应用中心状态 及查询im配置
	 *
	 * @return
	 */
	@RequestMapping("/web/index/detectingImStatus")
	ResponseResult detectingImStatus(@RequestParam("configKey") String config);

    /**
     * 查询地区行政编码
     * @param provinceId
     * @return
     */
    @GetMapping("/base/district/queryDistrictCode")
    String queryDistrictCode(@RequestParam("districtId") String provinceId);

    /**
     * 平台 商城配置  商城设置 查询
     *
     * @param param
     * @return
     */
    @PostMapping("/base/sysConfig/supply/slide/query")
    ResponseResult querySetting(@RequestBody Map<String,Object> param);

    /**
     * 获取积分与现金的兑换比例
     * @return
     */
    @GetMapping("/base/sysConfig/getPointsCashRatio")
    ResponseResult<PointsCashRatioVo> getPointsCashRatio();
}
