package com.houjiang.thirdparty.feign;

import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.dto.goods.NavSortLogoDTO;
import com.houjiang.commons.util.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Base服务降级处理
 *
 * @description 当base服务不可用时，提供默认的降级处理
 * @author Amos
 * @date 2024-01-01
 */
@Slf4j
@Component
public class BaseServiceClientFallback implements BaseServiceClient {

    @Override
    public ResponseResult selectOSSInfo(String ossType) {
        log.warn("Base服务不可用，使用默认OSS配置");
        return ResponseResult.error("Base服务暂时不可用");
    }

    @Override
    public String queryOssStates() {
        log.warn("Base服务不可用，使用默认OSS状态");
        return "default";
    }

    @Override
    public ResponseResult<String> querySmsStates() {
        log.warn("Base服务不可用，使用默认短信配置");
        return ResponseResult.success("default");
    }

    @Override
    public ResponseResult<Map<String, Object>> selectSMSInfoByType(String type) {
        log.warn("Base服务不可用，使用默认短信配置");
        return ResponseResult.<Map<String, Object>>fail(CommonStatusEnum.FAIL.getCode(), "Base服务暂时不可用");
    }

    @Override
    public ResponseResult quertNavLogo(NavSortLogoDTO gdbNavSortLogoDTO) {
        log.warn("Base服务不可用，使用默认导航配置");
        return ResponseResult.error("Base服务暂时不可用");
    }

    @Override
    public ResponseResult selectListByTemplateId(Integer templateId) {
        log.warn("Base服务不可用，使用默认模板配置");
        return ResponseResult.error("Base服务暂时不可用");
    }
    
    @Override
    public ResponseResult getSmsSetting() {
        log.warn("Base服务不可用，使用默认短信设置");
        Map<String, Object> defaultSetting = new java.util.HashMap<>();
        // 默认30分钟
        defaultSetting.put("codeExpireMinutes", 30);
        // 默认90天
        defaultSetting.put("recordExpireDays", 90);
        return ResponseResult.success(defaultSetting);
    }
}
