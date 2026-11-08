package com.houjiang.merchant.common.entity.setting;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class MallPayScene implements Serializable {

    @TableId(value="id", type= IdType.AUTO)
    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "支付配置ID")
    private Integer mallPayId;

    @Schema(description = "场景编码: [1=微信小程序, 2=微信公众号, 3=H5, 4=PC, 5=APP]")
    private Integer scene;

    @Schema(description = "默认支付: [0=否的, 1=是的]")
    private Integer isDefault;

    @Schema(description = "方式状态: [0=关闭, 1=开启]")
    private Integer status;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "更新时间")
    private Long updateTime;

    @Schema(description = "删除时间")
    private Long deleteTime;

}
