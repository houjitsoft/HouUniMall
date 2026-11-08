package com.houjiang.merchant.vo.mall;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class MallOrderAfterSaleLogListedVo implements Serializable {

    private static final long serialVersionUID = 1L;


    @TableId(value="id", type= IdType.AUTO)
    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "订单id")
    private Integer afterSaleId;

    @Schema(description = "名称")
    private String operator;
    @Schema(description = "类型 0系统 1管理员 2商家 3用户")
    private Integer operatorType;

    @Schema(description = "操作人id")
    private Integer operatorId;

    @Schema(description = "内容")
    private String content;

}
