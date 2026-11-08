package com.houjiang.activity.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @ClassName
 * @Description
 * @Author Amos
 * @Date 2023-06-13 09:05:20
 * @Version 1.0
 * @author Amos
 */
// contextId 必须显式指定：同包下 ThirdPartyConfigClient 也是 name=userms，
// 两者会各注册一个 userms.FeignClientSpecification，宿主开启插件扫描后直接 BeanDefinitionOverrideException。
@FeignClient(name = "userms", contextId = "activityUserServiceClient", configuration = ActivityFeignConfig.class)
public interface UserServiceClient {


    /**
     * 更新用户新人状态
     * @param paraMap
     */
    @PostMapping("user/ms/updateUser/isnew")
    Integer updateIsNew(@RequestBody Map<String, String> paraMap);
    /**
     * 根据会员id 查询会员账号 feign
     * @param userId
     * @return
     */
    @PostMapping("user/ms/queryUserMobileByuserId")
    String queryUserMobileByuserId(@RequestParam String userId);


    /**
     * 更新是否是新人
     * @param paraMap
     */
    @PostMapping("/user/ms/feign/updateIsNew")
    void updateIsNewFeign(@RequestBody Map<String, String> paraMap);

    /**
     * 更新红包状态（券/红包联动：下单或退款后由券侧回调）。
     *
     * <p>红包是用户权益，数据归属 userms 宿主。券侧（order-voucher 插件）原先直接注入
     * ActivityRedpacketBaseMapper 跨插件读红包表，插件化后插件间不允许互相 import，
     * 直接共享数据层也会让两个插件在同一进程争同一张表。改由本 Feign 调用 userms 侧接口。
     *
     * @param redpacketId 红包 ID
     * @param state       目标状态（1=未发放/可用，2=已发放/已使用）
     * @param orderId     关联订单号，可空
     * @return true 表示更新成功
     */
    @PostMapping("user/ms/redpacket/updateState")
    Boolean updateRedpacketState(@RequestParam String redpacketId,
                                 @RequestParam Integer state,
                                 @RequestParam(required = false) String orderId);
}
