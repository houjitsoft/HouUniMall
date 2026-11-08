package com.houjiang.merchant.common.entity.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class UserAddress implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value="id", type= IdType.AUTO)
    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "联系人名")
    private String contact;

    @Schema(description = "联系方式")
    private String mobile;

    @Schema(description = "省")
    private Integer provinceId;

    @Schema(description = "市")
    private Integer cityId;

    @Schema(description = "区")
    private Integer districtId;

    @Schema(description = "详细地址")
    private String info;

    @Schema(description = "经度")
    private Integer longitude;

    @Schema(description = "纬度")
    private String latitude;

    @Schema(description = "是否默认: [0=否, 1=是]")
    private Integer isDefault;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "修改时间")
    private Long updateTime;

    @Schema(description = "删除时间")
    private Long deleteTime;

}
