package com.houjiang.merchant.common.entity.mall;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 店铺账期
 * <p>
 * 承接原 business 服务的 {@code hou_shop_account_period}：该表随 business 下线已从生产库消失，
 * 而 order/payment 的账期链路仍在调用，故在 merchant 自有 schema 下重建，列名沿用旧命名，
 * 以便调用方 SQL 只改表名即可复用。
 * <p>
 * 一个店铺 + 一个用户最多一条账期记录，由唯一键 {@code uk_shop_user(shop_id, user_id)} 保证。
 *
 * @author Amos
 */
@Data
public class MallShopAccountPeriod implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "account_id", type = IdType.AUTO)
    @Schema(description = "账期id")
    private Integer accountId;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "用户id")
    private String userId;

    @Schema(description = "是否授权账期 0未授权 1已授权 2已冻结")
    private Integer isAccount;

    @Schema(description = "账期是否限额 0不限 1限额")
    private Integer accountIsLimit;

    @Schema(description = "账期额度")
    private BigDecimal accountPeriodPrice;

    @Schema(description = "账期时长（天）")
    private Integer accountPeriodTime;

    @Schema(description = "账期开通时间")
    private Date accountInsertTime;

    @Schema(description = "账期更新时间")
    private Date accountUpdateTime;

    @Schema(description = "已用金额")
    private BigDecimal amountUsed;

    @Schema(description = "可用金额")
    private BigDecimal availableAmount;

    @Schema(description = "本月已用金额")
    private BigDecimal monthAmountUsed;

}
