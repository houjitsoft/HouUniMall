/**
 * 插件制品模块（{@code plugin-artifact}）。
 *
 * <p>职责：制品存储抽象（{@code ArtifactStorage} SPI + 本地磁盘 P0 实现）、
 * 存储键契约、制品元信息 DDL。
 *
 * <p>依赖方向：{@code commons} ← {@code plugin-signature} ← 本模块 ← {@code plugin-publish / plugin-update}，
 * 严格单向，本模块不依赖发布与安装业务。
 *
 * <p>配置前缀统一为 {@code houcloud.artifact.*}。
 *
 * @author Amos
 * @since M0
 */
package com.houjiang.plugin.artifact;
