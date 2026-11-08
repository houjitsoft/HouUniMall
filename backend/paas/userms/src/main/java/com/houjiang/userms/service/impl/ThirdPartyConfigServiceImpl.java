package com.houjiang.userms.service.impl;

import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.dto.ThirdPartyConfigDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.userms.feign.ThirdPartyConfigFeignClient;
import com.houjiang.userms.service.ThirdPartyConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 第三方集成配置Service实现类
 * 
 * @author Amos
 * @since 2025-01-07
 */
@Slf4j
@Service
public class ThirdPartyConfigServiceImpl implements ThirdPartyConfigService {

    @Autowired
    private ThirdPartyConfigFeignClient thirdPartyConfigFeignClient;

    @Override
    public ThirdPartyConfigDTO getConfigByPlatform(String platform) {
        try {
            ResponseResult<ThirdPartyConfigDTO> response = thirdPartyConfigFeignClient.getConfig(platform);
            if (response != null && response.getCode() == CommonStatusEnum.SUCCESS.getCode()) {
                return response.getData();
            }
            return createDefaultConfig(platform);
        } catch (Exception e) {
            log.error("通过Feign获取第三方配置失败: {}", e.getMessage());
            return createDefaultConfig(platform);
        }
    }

    @Override
    public boolean saveConfig(String platform, ThirdPartyConfigDTO config) {
        try {
            ResponseResult<String> response = thirdPartyConfigFeignClient.saveConfig(platform, config);
            return response != null && response.getCode() == CommonStatusEnum.SUCCESS.getCode();
        } catch (Exception e) {
            log.error("通过Feign保存第三方配置失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean testConnection(String platform, ThirdPartyConfigDTO config) {
        try {
            ResponseResult<String> response = thirdPartyConfigFeignClient.testConnection(platform, config);
            return response != null && response.getCode() == CommonStatusEnum.SUCCESS.getCode();
        } catch (Exception e) {
            log.error("通过Feign测试第三方连接失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteConfig(String platform) {
        try {
            ResponseResult<String> response = thirdPartyConfigFeignClient.deleteConfig(platform);
            return response != null && response.getCode() == CommonStatusEnum.SUCCESS.getCode();
        } catch (Exception e) {
            log.error("通过Feign删除第三方配置失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 创建默认配置
     */
    private ThirdPartyConfigDTO createDefaultConfig(String platform) {
        ThirdPartyConfigDTO dto = new ThirdPartyConfigDTO();
        dto.setPlatform(platform);
        dto.setEnabled(false);
        dto.setDescription("");

        switch (platform) {
            case "dingtalk":
                dto.setDingtalk(new ThirdPartyConfigDTO.DingtalkConfig());
                break;
            case "wechat-work":
                dto.setWechatWork(new ThirdPartyConfigDTO.WechatWorkConfig());
                break;
            case "feishu":
                dto.setFeishu(new ThirdPartyConfigDTO.FeishuConfig());
                break;
            case "yuque":
                dto.setYuque(new ThirdPartyConfigDTO.YuqueConfig());
                break;
        }

        return dto;
    }
}
