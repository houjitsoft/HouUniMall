/**
 * 插件升级模块（{@code plugin-update}）。
 *
 * <p>职责：客户端安装 / 升级 / 回滚的任务编排——幂等建单、断点续做、备份与回滚锚点、
 * 任务状态查询。M0 只落地地基（模块骨架 + 任务状态枚举）；
 * <b>M3</b> 落地兼容性预检（P0-9）与安装编排（P0-7）：
 * 预检 → 二次授权 → 下载 → md5/sha256/Ed25519 三重校验 → 落盘 → 包内 schema → 引擎装载 → 登记安装态。
 * 升级与回滚编排（含备份点与流量排空）留到 M4。
 *
 * <p>依赖方向：依赖 {@code commons / plugin-signature / plugin-artifact / plugin-registry /
 * plugin-license / plugin-market / plugin-center / plugin-engine}，位于分发链路最上层，不被任何下游模块依赖。
 *
 * @author Amos
 * @since M0
 */
package com.houjiang.plugin.update;
