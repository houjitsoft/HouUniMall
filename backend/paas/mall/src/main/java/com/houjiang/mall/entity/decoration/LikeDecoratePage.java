package com.houjiang.mall.entity.decoration;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * likeshop 原始装修页面实体
 * 对应数据库表：hou_base_decorate_page（从 BBC 系统迁移过来的表结构，2026-10-16 由
 * hou_decorate_page 改名而来，统一归入 base 命名规范）
 *
 * type 含义：1=首页 2=个人中心 3=客服 4=分类 5=商品详情 6=商城风格 7=秒杀 8=拼团
 *
 * @author Amos
 */
@Data
@TableName("hou_base_decorate_page")
public class LikeDecoratePage implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 页面类型：1=首页 2=个人中心 3=客服 4=分类 5=商品详情 6=商城风格 7=秒杀 8=拼组 */
    private Integer type;

    /** 页面名称 */
    private String name;

    /** 装修数据 JSON（数组） */
    private String data;

    /** 元数据 JSON */
    private String meta;

    /**
     * 页面所属端：mobile=移动端(H5/小程序) pc=PC端。
     * 表默认值 mobile，历史数据全部为 mobile，uniapp/H5 前台行为不变。
     */
    private String pagePlatform;

    /**
     * 已套用的移动端页面模板 code（simple / vivid / premium），NULL 表示不套用。
     * 由后台「装修管理 → 模板选择」写入，H5 首页经 H5DecorateBridge 下发为 templateView。
     */
    @TableField("page_template_code")
    private String pageTemplateCode;

    /** 创建时间（Unix 秒） */
    @TableField("create_time")
    private Long createTime;

    /** 更新时间（Unix 秒） */
    @TableField("update_time")
    private Long updateTime;
}
