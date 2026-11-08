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
public class MallShopAdminJob implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value="id", type= IdType.AUTO)
    @Schema(description = "ID")
    private Integer id;
    @Schema(description = "店铺id")
    private Integer shopId;
    @Schema(description = "管理员id")
    private Integer adminId;
    @Schema(description = "岗位id")
    private Integer jobId;
    @Schema(description = "创建时间")
    private Long createTime;
    @Schema(description = "更新时间")
    private Long updateTime;
    @Schema(description = "删除时间")
    private Long deleteTime;


}
