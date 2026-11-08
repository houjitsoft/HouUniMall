package com.houjiang.mall.entity.plugin;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商城插件安装状态。
 * @author Amos
 */
@Data
@TableName("hou_addon_plugin_install")
public class MallPluginInstall implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String pluginCode;

    /** 是否已安装：0 否，1 是 */
    private Integer installed;

    /** 是否启用：0 否，1 是 */
    private Integer enabled;

    private String configJson;

    private String version;

    /** 安装包落盘路径（市场在线安装时写入，位于 pluginsRoot 下的 jar） */
    private String jarPath;

    /** 安装包 MD5 摘要（完整性校验留痕） */
    private String checksum;

    private LocalDateTime installTime;

    private LocalDateTime updateTime;

    /**
     * 更新锁：0=空闲 1=安装中 2=升级中 3=回滚中 4=上次失败（M4，DDL §3.5）。
     *
     * <p>与 {@code hou_plugin_update_task} 的非终态任务互为双保险：任务表拦并发请求，
     * 本列拦「任务表还没来得及建单」的竞态，同时也是运维一眼能看懂的「这台站点正在动哪个插件」。
     */
    private Integer updateStatus;

    /**
     * 本轮更新开始时间（M4）：孤儿任务扫描按它判断「锁了太久 = 进程被杀」。
     */
    private LocalDateTime updateStartedAt;

    /**
     * 上一版本（M4）：升级成功后指向升级前的版本，是回滚锚点。
     *
     * <p>它必须落库而不只是留在内存：进程重启后仍要知道「能退回哪里」，
     * 而且版本目录清理必须排除它——把回滚点删掉的清理比不清理更糟。
     */
    private String previousVersion;

    /**
     * 缓存：线上存在的最新更高版本（P0-10 目录页用，M4 只落列不写值）。
     */
    private String upgradableVersion;

    /**
     * 最近一次更新失败原因（M4）：前端不必回查任务表就能展示「上次为什么失败」。
     */
    private String lastError;

    @TableLogic
    private Integer deleted;
}
