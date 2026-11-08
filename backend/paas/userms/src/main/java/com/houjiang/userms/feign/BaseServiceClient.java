package com.houjiang.userms.feign;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.base.PointsCashRatioVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author Amos
 */
@FeignClient("base")
public interface BaseServiceClient {

    /**
     * 修改会员等级
     *
     * @param map
     * @return
     */
    @PostMapping("/base/tagClass/updateTagClassByUserId")
    ResponseResult updateTagClassByUserId(@RequestBody Map<String, Object> map);

    /**
     * 查询所有会员等级
     *
     * @param map
     * @return
     */
    @PostMapping("base/membershipGrade/queryMembershipGrade")
    ResponseResult queryMembershipGrade(@RequestBody Map<String, Object> map);


    /**
     * 查询积分规则
     *
     * @return
     */
    @PostMapping("/base/integralRules/queryIntegralRules")
    ResponseResult queryIntegralRules();

    /**
     * 根据configKey查configValue
     *
     * @param configKey
     * @return
     */
    @GetMapping("/base/sysConfig/selectByConfigKey")
    ResponseResult selectByConfigKey(@RequestParam("configKey") String configKey);


    /**
     * 查询plus 会员 套餐信息  feign
     */
    @PostMapping("/base/plusRuleThali/query")
    ResponseResult queryMerchantPlusThali(@RequestBody Map<String, Object> map) throws IllegalAccessException;


    /**
     * 注册修改会员等级
     *
     * @param map
     * @return
     */
    @PostMapping("/base/tagClass/saveTagClass")
    ResponseResult saveTagClass(@RequestBody Map<String, Object> map);

    /**
     * feign调用 根据用户id查询会员等级名称
     *
     * @param userId
     * @return
     */
    @PostMapping("base/membershipGrade/queryByUserId")
    public Map<String, Object> queryMemberShipGradeByUserId(@RequestParam("userId") String userId);

    /**
     * 根据查询会员积分最大值
     *
     * @return
     */
    @PostMapping("base/membershipGrade/getMaxMemberIntegral")
    public Integer getMaxMemberIntegral();

    /**
     * feign调用 查询会员用户距离下一级积分
     *
     * @param userId
     * @return
     */
    @PostMapping("base/membershipGrade/getMemberIntegralDistance")
    ResponseResult getMemberIntegralDistance(@RequestParam("userId") String userId);

    /**
     * 查询地区 feign
     *
     * @param provinceId
     * @return
     */
    @PostMapping("/base/district/queryProvinceport")
    String queryProvinceport(@RequestParam("provinceId") String provinceId);

    /**
     * 根据用户id查询会员等级id
     *
     * @param userId
     * @return
     */
    @PostMapping("/base/tagClass/queryByUserId")
    ResponseResult getMemberClassIdByUserId(@RequestParam("userId") String userId);


    /**
     * 根据会员等级id查询会员等级
     *
     * @param id
     * @return
     */
    @PostMapping("/base/membershipGrade/getMemberNameByClassId")
    ResponseResult getMemberNameByClassId(@RequestParam("id") String id);


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
     * 查询积分规则
     *
     * @param map
     * @return
     */
    @PostMapping("/base/membershipGrade/queryByGradeRule")
    ResponseResult queryMembershipGradeByGradeRule(@RequestBody Map<String, Object> map);


    /**
     * 通过key查询广告位
     *
     * @return
     */
    @PostMapping("/base/sysConfig/queryMePage")
    ResponseResult queryMePage(@RequestBody Map<String, String> paraMap);


    /**
     * 根据configKey 查询配置 feign
     */
    @PostMapping("base/sysConfig/queryConfigKeyFeign")
    String selectConfigValueFeign(@RequestParam String configKey);

    /**
     * 根据configKey查configValue
     *
     * @param configKey
     * @return
     */
    @GetMapping("/base/sysConfig/selectConfigValueByKey")
    String selectConfigValueByKey(@RequestParam("configKey") String configKey);

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
     * 根据地址名得到地址Id
     *
     * @param addressName 地址名
     * @return 地址Id
     */
    @GetMapping("/base/district/getAddressIdByName")
    Integer getAddressIdByName(@RequestParam("addressName") String addressName,
                               @RequestParam("level") Integer level);

    /**
     * 获取积分与现金的兑换比例
     * @return
     */
    @GetMapping("/base/sysConfig/getPointsCashRatio")
    ResponseResult<PointsCashRatioVo> getPointsCashRatio();

    /**
     * 提交公司登记申请
     * @param companyData 公司登记信息
     * @return 提交结果
     */
    @PostMapping("/organization/register")
    ResponseResult submitCompanyRegistration(@RequestBody Map<String, Object> companyData);

    /**
     * 检查用户是否已经提交过公司登记申请
     * @param userId 用户ID
     * @return 检查结果
     */
    @GetMapping("/organization/checkUserRegistration")
    ResponseResult checkUserCompanyRegistration(@RequestParam("userId") String userId);

    /**
     * 创建用户组织关联关系
     * @param userOrgData 用户组织关联数据
     * @return 创建结果
     */
    @PostMapping("/organization/createUserOrg")
    ResponseResult createUserOrganization(@RequestBody Map<String, Object> userOrgData);

    /**
     * 删除组织
     * @param orgId 组织ID
     * @return 删除结果
     */
    @DeleteMapping("/organization/delete")
    ResponseResult deleteOrganization(@RequestParam("orgId") String orgId);

    /**
     * 根据ID查询组织信息
     * @param orgId 组织ID
     * @return 组织信息
     */
    @GetMapping("/organization/queryOrganizationById")
    ResponseResult getOrganizationById(@RequestParam("orgId") String orgId);

    /**
     * 保障用户拥有「个人空间」组织（幂等，按 PERSONAL_&lt;userId&gt; 编码）
     *
     * @param data userId（必填）、displayName（可选）
     * @return orgId / orgCode / isNewOrg
     */
    @PostMapping("/organization/ensurePersonalOrganization")
    ResponseResult ensurePersonalOrganization(@RequestBody Map<String, Object> data);
}
