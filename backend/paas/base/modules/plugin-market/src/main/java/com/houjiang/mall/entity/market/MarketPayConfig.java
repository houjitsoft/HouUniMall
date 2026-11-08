package com.houjiang.mall.entity.market;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 插件支付配置（hou_market_pay_config）。
 *
 * <p><b>为什么要有这张表</b>：支付参数原先只能在 Nacos / 环境变量里写死，运营改一次售价
 * 或切一次沙箱都要改配置再重启服务。这里把「业务参数」搬进数据库，改完调一次刷新接口即生效；
 * 而<b>密钥类参数不入库</b>——库里只保存环境变量名（{@code ${HOU_PAY_XXX}}），真正的值仍从
 * 环境变量 / Nacos 注入，避免「库被拖走 = 商户密钥泄露」。
 *
 * <p><b>EAV 形态</b>：一行一个配置项（scope + scope_key + sub_key + config_key 唯一）。
 * 配置项的增删改不需要改表结构，加一个新参数只要加一行数据。
 *
 * @author Amos
 */
@Data
@TableName("hou_market_pay_config")
public class MarketPayConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 全局配置 */
    public static final String SCOPE_GLOBAL = "GLOBAL";

    /** 渠道配置：scope_key = 渠道编码 */
    public static final String SCOPE_CHANNEL = "CHANNEL";

    /** 价格套餐：scope_key = 插件编码，sub_key = 套餐编码 */
    public static final String SCOPE_PLAN = "PLAN";

    /** GLOBAL 作用域的固定 scope_key */
    public static final String GLOBAL_KEY = "*";

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 作用域：见 SCOPE_* 常量 */
    private String scope;

    /** GLOBAL=*；CHANNEL=渠道编码；PLAN=插件编码 */
    private String scopeKey;

    /** PLAN 时为套餐编码，其余为空串 */
    private String subKey;

    /** 配置键，短横线风格，如 public-base-url / valid-days */
    private String configKey;

    /** 配置值；secret=1 时只能是 {@code ${ENV_NAME}} */
    private String configValue;

    /** 值类型：STRING / INT / DECIMAL / BOOL */
    private String valueType;

    /** 1=密钥类，库里只存引用不明文 */
    private Integer secret;

    /** 运营备注，会下发到配置页 */
    private String remark;

    /** 最后修改人 */
    private String updateBy;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
