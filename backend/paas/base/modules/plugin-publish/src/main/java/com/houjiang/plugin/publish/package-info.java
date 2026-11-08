/**
 * 插件发布模块（{@code plugin-publish}）。
 *
 * <p>职责：发布侧能力——制品上传（幂等）、清单校验、版本状态机、审核上下架。
 * M0 只落地地基（模块骨架 + 版本比较等公共能力），业务接口在 M1 实现。
 *
 * <p>依赖方向：{@code commons / plugin-signature / plugin-artifact / plugin-market}，
 * 严格单向，不被 plugin-artifact、plugin-update 反向依赖。
 *
 * @author Amos
 * @since M0
 */
package com.houjiang.plugin.publish;
