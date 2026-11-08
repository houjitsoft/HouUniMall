package com.houjiang.aiagent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 中台 - 智能体档案。
 *
 * <p>一个智能体 = 一段场景化的 AI 能力（商品推荐、客服回复、文案生成……），
 * 由后台统一配置「用哪个模型 + 什么提示词 + 何时触发 + 对谁生效」，
 * 前台按 {@code code} 取回配置后调用，避免在业务代码里散落硬编码的提示词与模型。
 */
@Data
@TableName("hou_ai_agent")
public class AiAgent {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 智能体编码，前台按此调用，如 goods_recommend */
    private String code;

    private String name;

    /** 场景分组：recommend / service / order / content / marketing / insight / supply / aftersale */
    private String scene;

    /**
     * 所属端 merchant 商家端 / frontend 前端 / admin 管理后台
     * 后台智能体列表按此分选项卡展示
     */
    private String terminal;

    /** Element Plus 图标名 */
    private String icon;

    private String description;

    /** 使用位置：这个智能体在哪个页面 / 哪个入口被调用，后台列表展示，便于交接 */
    private String usagePage;

    /** PC 商城展示位置（人话描述，如「商品详情页 → 猜你喜欢推荐位」）；空表示 PC 端未接入 */
    private String pcPage;

    /** PC 商城访问路径，可带 {id} 占位；后台点一下直接开着看真实效果 */
    private String pcPath;

    /** H5 / 小程序展示位置（人话描述）；空表示移动端未接入 */
    private String h5Page;

    /** H5 访问路径（uni-app 路由），可带 {id} 占位 */
    private String h5Path;

    /** 1 启用 / 0 停用 */
    private Integer status;

    /** 绑定模型编码（hou_ai_model.code） */
    private String modelCode;

    /** 系统提示词 */
    private String prompt;

    /**
     * 执行模式：prompt 提示词（默认，按 prompt 直接调模型） / workflow 工作流（按 workflowCode 跑画布）
     */
    private String mode;

    /** 关联的工作流编码（mode=workflow 时生效，对应 hou_ai_workflow.code） */
    private String workflowCode;

    /** 触发条件：manual 手动 / auto 自动 / event 事件 / schedule 定时 */
    private String triggerType;

    /** 触发条件配置（JSON） */
    private String triggerConfig;

    /** 生效范围：all 全站 / tenant 租户 / shop 店铺 / category 品类 */
    private String scopeType;

    /** 生效范围配置（JSON） */
    private String scopeConfig;

    /** 其他参数（JSON：温度、最大 token、兜底策略等） */
    private String params;

    /** 1 内置（不可删除） */
    private Integer builtIn;

    private Integer sort;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
