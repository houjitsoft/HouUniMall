package com.houjiang.activity.feign;

import com.houjiang.commons.dto.goods.NavSortLogoDTO;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @author Amos
 */
@FeignClient(name = "base", contextId = "activityBaseServiceClient", configuration = ActivityFeignConfig.class)
public interface BaseServiceClient {

    /**
     * 查询首页logo
     */
    @PostMapping("base/sysConfig/quertNavLogo")
    ResponseResult quertNavLogo(@RequestBody NavSortLogoDTO gdbNavSortLogoDTO);
    /**
     * 根据用户id查询会员等级名称 feign
     * @param userId
     * @return
     */
    @PostMapping("base/tagClass/queryMemberName/ByUserId")
    String queryMemberNameByUserId(@RequestParam("userId") String userId);

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
}
