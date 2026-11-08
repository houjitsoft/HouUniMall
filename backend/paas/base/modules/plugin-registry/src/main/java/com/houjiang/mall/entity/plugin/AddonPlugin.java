package com.houjiang.mall.entity.plugin;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author Amos
 */
@Data
@TableName("hou_addon_plugin")
public class AddonPlugin implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String serviceCode;
    private String pluginCode;
    private String name;
    private String category;
    /**
     * 插件分类外键，对应 hou_addon_cate.id（int）；0 表示未分类。
     *
     * <p>与 {@code sort} 同属「人工维护优先」列：manifest 单向同步
     * （AddonPluginMapper.upsertFromManifest）仅按字典名称初始化空分类，
     * 已有正数分类 ID 不会被 plugin.yaml 覆盖。
     */
    private Integer cateId;
    private String version;
    private String description;
    private String terminalsJson;
    private String dependenciesJson;
    private String entryPath;
    private String configSchema;
    private Integer status;
    private Integer sort;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
