package com.houjiang.merchant.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class Area implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value="id", type= IdType.AUTO)
    @Schema(description = "地区编号")
    private Integer id;

    @Schema(description = "父级地区编码")
    private Integer parentId;

    @Schema(description = "等级 0-国家；1-省份；2-地级市；3-县区")
    private Integer level;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "简称")
    @TableField("short")
    private String shortName;

    @Schema(description = "地区编码")
    private String cityCode;

    @Schema(description = "邮政编码")
    private String zipCode;

    @Schema(description = "纬度")
    private String gcj02Lng;

    @Schema(description = "经度")
    private String gcj02Lat;

    @Schema(description = "纬度")
    private String db09Lng;

    @Schema(description = "经度")
    private String db09Lat;

    @Schema(description = "备注一")
    private String remark1;

    @Schema(description = "备注二")
    private String remark2;

}