package com.houjiang.mall.mapper.artifact;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.mall.entity.artifact.PluginArtifact;
import org.apache.ibatis.annotations.Mapper;

/**
 * 插件制品 Mapper。
 *
 * <p><b>包路径不能改</b>：mall 未声明 {@code @MapperScan}，MyBatis-Plus 自动扫描范围是启动类
 * 所在包 {@code com.houjiang.mall}，Mapper 只有落在这个包下才会被注册成 Bean。
 *
 * <p><b>M2 归位说明</b>：从 plugin-publish 的 {@code com.houjiang.mall.mapper.publish} 迁到本包，
 * 让 plugin-artifact 成为 {@code hou_plugin_artifact} 的唯一归属模块
 * （DDL 与下载链路都在这里），避免 plugin-artifact 反向依赖 plugin-publish 成环。
 *
 * @author Amos
 * @since M1（M2 迁移到 plugin-artifact）
 */
@Mapper
public interface PluginArtifactMapper extends BaseMapper<PluginArtifact> {
}
