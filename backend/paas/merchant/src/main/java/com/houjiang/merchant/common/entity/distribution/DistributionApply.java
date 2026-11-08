package com.houjiang.merchant.common.entity.distribution;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class DistributionApply implements Serializable {

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

    @Schema(description = "用户id")
    private Integer userId;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "省份")
    private Integer provinceId;

    @Schema(description = "城市")
    private Integer cityId;

    @Schema(description = "县区")
    private Integer districtId;

    @Schema(description = "申请原因")
    private String reason;

    @Schema(description = "状态：0-待审核；1-审核通过；2-审核不通过")
    private Integer auditStatus;

    @Schema(description = "审核说明")
    private String auditContent;


}