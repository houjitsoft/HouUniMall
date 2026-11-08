package com.houjiang.mall.controller.distribution;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.entity.distribution.DistributionSetting;
import com.houjiang.mall.service.distribution.DistributionSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 分销设置控制器
 *
 * 挂在 /mall/distribution/setting 下，经 BFF 商城模块直通通道
 * （/web/mall/module/distribution/setting/**）访问，白名单已覆盖，无需改 BFF。
 *
 * 后台保存配置；C 端分销中心读取 enabled/maxLevel 控制入口显隐与层级权益。
 *
 * @author Amos
 */
@RestController
@RequestMapping("/mall/distribution/setting")
@RequiredArgsConstructor
public class DistributionSettingController {

    private final DistributionSettingService distributionSettingService;

    /**
     * 读取分销配置
     *
     * @param params 查询参数（可包含 shopId，缺省为平台配置）
     * @return 分销设置（无记录时返回默认值）
     */
    @PostMapping("/detail")
    public ResponseResult<DistributionSetting> detail(@RequestBody Map<String, Object> params) {
        Long shopId = params.get("shopId") != null ? Long.parseLong(params.get("shopId").toString()) : null;
        return ResponseResult.success(distributionSettingService.getSetting(shopId));
    }

    /**
     * 保存分销配置（无则新建，有则更新；层级数强制收敛到 1..2）
     *
     * @param setting 分销设置
     * @return 保存后的配置
     */
    @PostMapping("/save")
    public ResponseResult<DistributionSetting> save(@RequestBody DistributionSetting setting) {
        return ResponseResult.success(distributionSettingService.saveSetting(setting));
    }
}
