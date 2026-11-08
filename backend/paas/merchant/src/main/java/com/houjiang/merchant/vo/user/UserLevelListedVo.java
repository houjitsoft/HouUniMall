package com.houjiang.merchant.vo.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.houjiang.merchant.vo.mall.MallGoodsUserLevelDiscountVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class UserLevelListedVo implements Serializable {

    private static final long serialVersionUID = 1L;


    @TableId(value="id", type= IdType.AUTO)
    @Schema(description = "ID")
    private Integer id;
    @Schema(description = "创建时间")
    private Long createTime;
    @Schema(description = "更新时间")
    private Long updateTime;
    @Schema(description = "删除时间")
    private Long deleteTime;
    @Schema(description = "名称")
    private String name;
    @Schema(description = "等级")
    private Integer level;
    @Schema(description = "图标")
    private String icon;
    @Schema(description = "背景")
    private String bg;
    @Schema(description = "描述")
    private String description;
    @Schema(description = "等级折扣 1参与等级折扣 0无等级折扣")
    private Integer isDiscount;

    @Schema(description = "等级折扣比例")
    private BigDecimal discountRatio;

    @Schema(description = "是否默认等级")
    private Integer isDefault;

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

    @Schema(description = "优惠列表")
    private List<MallGoodsUserLevelDiscountVo> discountList;
}
