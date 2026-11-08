package com.houjiang.mall.service.plugin;

/**
 * 二开迁移幂等仓库 SPI（R2.1）。
 *
 * <p>由 {@link ExtensionSchemaMigrationRunner} 依赖，用于判定某个迁移脚本是否已成功执行
 * （幂等）以及记录执行结果。生产实现见 {@link JdbcExtensionMigrationRepository}（表
 * {@code hou_ext_migration}）；测试可注入内存实现。
 *
 * <p>幂等判定同时比较脚本内容校验和：脚本被修改后应重新执行，因此
 * {@link #isApplied(String, String, String)} 需在 <b>状态为成功且校验和一致</b> 时才返回 {@code true}。
 *
 * @author Amos
 */
public interface ExtensionMigrationRepository {

    /**
     * 判断某迁移脚本是否已成功执行且内容未变。
     *
     * @param code     二开模块 code
     * @param script   迁移脚本名（相对 {@code db/migration/{code}/}）
     * @param checksum 当前脚本内容的 SHA-256 校验和
     * @return 已成功执行且校验和一致返回 {@code true}
     */
    boolean isApplied(String code, String script, String checksum);

    /**
     * 记录一次成功执行。
     *
     * @param code     二开模块 code
     * @param version  二开版本
     * @param script   迁移脚本名
     * @param checksum 脚本内容 SHA-256
     */
    void markSuccess(String code, String version, String script, String checksum);

    /**
     * 记录一次失败执行。
     *
     * @param code     二开模块 code
     * @param version  二开版本
     * @param script   迁移脚本名
     * @param checksum 脚本内容 SHA-256
     * @param message  失败原因（实现方负责截断到列宽内）
     */
    void markFailure(String code, String version, String script, String checksum, String message);
}
