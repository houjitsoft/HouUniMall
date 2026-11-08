package com.houjiang.mall.service.plugin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.houjiang.mall.entity.plugin.MallPluginInstall;
import com.houjiang.mall.mapper.plugin.MallPluginInstallMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 插件前端产物准入判定（T11）。
 *
 * <p>产物是**公开只读**资源（浏览器 {@code import()} 无法携带 token），因此准入不能依赖登录态，
 * 改由「宿主登记 + 安装态」两道门控：
 * <ol>
 *   <li>插件编码必须在宿主登记中（{@code hou_addon_plugin} 或内置目录）→ 否则 404，随机探测拿不到任何字节；</li>
 *   <li>存在安装记录时：已卸载（{@code installed=0}）→ 404；已停用（{@code enabled=0}）→ 423；</li>
 *   <li>无安装记录（官方内置 / 演示产物 / 市场演练包）：放行，便于未安装状态下预览与开发联调。</li>
 * </ol>
 *
 * @author Amos
 */
@Service
public class PluginAssetAccessService {

    private static final Logger log = LoggerFactory.getLogger(PluginAssetAccessService.class);

    public enum Access {
        /** 允许读取 */
        ALLOWED,
        /** 宿主未登记该插件 */
        NOT_REGISTERED,
        /** 已卸载（安装态 installed=0） */
        UNINSTALLED,
        /** 已停用（enabled=0） */
        DISABLED
    }

    private final PluginCenterService pluginCenterService;

    private final MallPluginInstallMapper pluginInstallMapper;

    @Autowired
    public PluginAssetAccessService(PluginCenterService pluginCenterService,
                                    MallPluginInstallMapper pluginInstallMapper) {
        this.pluginCenterService = pluginCenterService;
        this.pluginInstallMapper = pluginInstallMapper;
    }

    /**
     * 判定某插件的前端产物是否可读。
     */
    public Access check(String pluginCode) {
        boolean registered = pluginCenterService.catalog().stream()
                .anyMatch(item -> item.getCode().equals(pluginCode));
        if (!registered) {
            return Access.NOT_REGISTERED;
        }
        MallPluginInstall install;
        try {
            install = pluginInstallMapper.selectOne(new LambdaQueryWrapper<MallPluginInstall>()
                    .eq(MallPluginInstall::getPluginCode, pluginCode)
                    .last("limit 1"));
        } catch (RuntimeException e) {
            // 安装态查询失败时按「未安装」放行，避免官方产物因库异常整体不可读（只读资源，风险可控）
            log.warn("查询插件安装态失败，按未安装处理：code={}, err={}", pluginCode, e.getMessage());
            return Access.ALLOWED;
        }
        if (install == null) {
            return Access.ALLOWED;
        }
        if (!Integer.valueOf(1).equals(install.getInstalled())) {
            return Access.UNINSTALLED;
        }
        if (!Integer.valueOf(1).equals(install.getEnabled())) {
            return Access.DISABLED;
        }
        return Access.ALLOWED;
    }
}
