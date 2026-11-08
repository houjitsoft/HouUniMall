package com.houjiang.mall.service.plugin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.houjiang.mall.entity.plugin.MallPluginInstall;
import com.houjiang.mall.mapper.plugin.MallPluginInstallMapper;
import com.houjiang.plugin.engine.core.PluginBootGate;
import com.houjiang.plugin.engine.core.PluginLifecycleManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Mall 重启后的插件恢复器。
 *
 * <p>市场安装把 JAR 写入运行目录并记录在安装表；进程重启后 PF4J 不会自动读取
 * 嵌套版本目录，因此由宿主在应用就绪时按安装记录恢复，并把停用状态重新应用。
 * 单个插件恢复失败只隔离该插件，不阻断商城启动。</p>
 */
@Slf4j
@Component
public class PluginRuntimeBootstrap {

    private final MallPluginInstallMapper installMapper;
    private final PluginLifecycleManager lifecycleManager;

    /**
     * 启动装载窗口闸门：插件是在应用就绪之后才逐个装载的（本类跑在
     * {@code ApplicationReadyEvent} 上），大插件（GEO 的 240MB jar、600 条路由）要近两分钟。
     * 这段窗口里 {@code /plugin/{code}/**} 查不到路由，必须回 503 而不是 404，
     * 否则每次发版重启前端都会吃到一批假 404。
     *
     * <p>以 {@link ObjectProvider} 注入：引擎未装配该 Bean 时保持原行为。
     */
    private final PluginBootGate bootGate;

    @Autowired
    public PluginRuntimeBootstrap(MallPluginInstallMapper installMapper,
                                  PluginLifecycleManager lifecycleManager,
                                  ObjectProvider<PluginBootGate> bootGate) {
        this.installMapper = installMapper;
        this.lifecycleManager = lifecycleManager;
        this.bootGate = bootGate == null ? null : bootGate.getIfAvailable();
    }

    /** 无闸门的构造（引擎未装配 {@link PluginBootGate} 时使用，行为与改造前一致）。 */
    public PluginRuntimeBootstrap(MallPluginInstallMapper installMapper,
                                  PluginLifecycleManager lifecycleManager) {
        this(installMapper, lifecycleManager, null);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void restoreInstalledPlugins() {
        // 装载窗口必须覆盖到最后一个插件装完为止；无论成功失败都要关掉，否则会永久假 503。
        if (bootGate != null) {
            bootGate.begin();
        }
        try {
            installMapper.selectList(new LambdaQueryWrapper<MallPluginInstall>()
                            .eq(MallPluginInstall::getInstalled, 1))
                    .forEach(this::restore);
        } catch (RuntimeException e) {
            // 数据库尚未就绪或表不存在时，保留商城启动能力，下一次部署再恢复。
            log.error("插件运行态恢复失败，商城继续启动", e);
        } finally {
            if (bootGate != null) {
                bootGate.complete();
                log.info("插件装载窗口已关闭，插件路由全部就绪");
            }
        }
    }

    private void restore(MallPluginInstall install) {
        String code = install.getPluginCode();
        if (code == null || code.isBlank() || install.getJarPath() == null || install.getJarPath().isBlank()) {
            log.warn("跳过无效插件安装记录: code={}, jarPath={}", code, install.getJarPath());
            return;
        }
        if (!Files.isRegularFile(Path.of(install.getJarPath()))) {
            log.warn("插件安装包不存在，保持未加载: code={}, jarPath={}", code, install.getJarPath());
            return;
        }
        try {
            if (!lifecycleManager.isLoaded(code)) {
                lifecycleManager.install(code, Path.of(install.getJarPath()));
            }
            if (!Integer.valueOf(1).equals(install.getEnabled())) {
                lifecycleManager.disable(code);
            }
            log.info("插件运行态恢复完成: code={}, enabled={}", code, install.getEnabled());
        } catch (RuntimeException e) {
            log.error("插件运行态恢复失败，已隔离该插件: code={}", code, e);
        }
    }
}
