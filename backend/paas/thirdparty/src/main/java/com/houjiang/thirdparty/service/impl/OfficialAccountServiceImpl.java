package com.houjiang.thirdparty.service.impl;

import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.StringUtils;
import com.houjiang.thirdparty.enums.OfficialAccountStrategy;
import com.houjiang.thirdparty.feign.UserServiceClient;
import com.houjiang.thirdparty.service.OfficialAccountService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.Map;

/**
 * @author Amos
 */
@Service
public class OfficialAccountServiceImpl implements OfficialAccountService {

    @Resource
    private Map<String, OfficialAccountStrategy> strategies;

    @Resource
    private UserServiceClient userServiceClient;

    /**
     * 发送公众号通知
     *
     * @param param 包含用户ID和模板代码的参数映射
     * @return 如果成功发送通知，则返回成功结果；如果用户未绑定微信，则返回失败结果
     */
    @Override
    public ResponseResult sendOfficialAccountNotice(Map<String, Object> param) {
        // 从参数中提取用户ID
        String userId = param.get("userId").toString();
        // 调用用户服务客户端查询用户的OpenID
        Map<String, String> map = userServiceClient.queryUserOpenId(userId);
        // 检查是否获取到OpenID信息，如果没有，则返回用户未绑定微信的错误
        if (map == null || StringUtils.isEmpty(map.get("officialAccountOpenId"))) {
            return ResponseResult.fail(BizCodeEnume.USER_NO_BIND_WECHAT);
        }
        // 将查询到的OpenID信息添加到参数映射中
        param.putAll(map);
        // 从参数中提取模板代码
        String str = param.get("templateCode").toString();
        // 根据模板代码选择相应的策略，并执行
        strategies.get(str).execute(param);
        // 返回成功结果
        return ResponseResult.success();
    }

}
