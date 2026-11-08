package com.houjiang.merchant.common.entity.mall;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class MallShopApply implements Serializable {

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

    @Schema(description = "主营类目")
    private Integer cid;

    @Schema(description = "联系人")
    private String contact;

    @Schema(description = "手机")
    private String mobile;

    @Schema(description = "省")
    private Integer provinceId;

    @Schema(description = "市")
    private Integer cityId;

    @Schema(description = "区")
    private Integer districtId;

    @Schema(description = "地址详情")
    private String addressInfo;

    @Schema(description = "申请说明")
    private String applyContent;

    @Schema(description = "状态 0待审核 1通过 2拒绝")
    private Integer auditStatus;

    @Schema(description = "审核说明")
    private String auditContent;

    @Schema(description = "跟进 0未跟进 1跟进中")
    private Integer follow;

}