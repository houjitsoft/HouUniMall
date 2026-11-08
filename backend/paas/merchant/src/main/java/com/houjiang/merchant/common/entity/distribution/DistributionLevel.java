package com.houjiang.merchant.common.entity.distribution;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class DistributionLevel implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value="id", type= IdType.AUTO)
    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "修改时间")
    private Long updateTime;

    @Schema(description = "删除时间")
    private Long deleteTime;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "分销等级")
    private Integer level;

    @Schema(description = "默认等级 1是 0否")
    private Integer isDefault;

    @Schema(description = "说明")
    private String description;

    @Schema(description = "自购佣金比例")
    private BigDecimal selfRatio;

    @Schema(description = "一级分销比例")
    private BigDecimal oneRatio;

    @Schema(description = "二级分销比例")
    private BigDecimal twoRatio;

    @Schema(description = "等级条件 -1未设置 0任意条件 1全部条件")
    @TableField(value = "`condition`")
    private Integer conditionField;

    @Schema(description = "单次消费金额 条件开启 1是0否")
    private Integer orderOneMoneyIs;

    @Schema(description = "单次消费金额")
    private BigDecimal orderOneMoney;

    @Schema(description = "累计消费金额 条件开启 1是0否")
    private Integer orderAllMoneyIs;

    @Schema(description = "累计消费金额")
    private BigDecimal orderAllMoney;

    @Schema(description = "累计消费次数 条件开启 1是0否")
    private Integer orderAllNumIs;

    @Schema(description = "累计消费次数")
    private Integer orderAllNum;

    @Schema(description = "已结算佣金收入 开启条件 1是 0否")
    private Integer commissionIs;

    @Schema(description = "已结算佣金收入")
    private BigDecimal commissionMoney;

}