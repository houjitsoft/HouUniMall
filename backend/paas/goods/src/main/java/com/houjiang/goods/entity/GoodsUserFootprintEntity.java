package com.houjiang.goods.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
 /**
 * @ClassName
 * @Description  商品访问足迹表
 * @Author Amos
 * @Date 2023-05-25 16:03:05
 * @Version 1.0
 * @author Amos
 */
@TableName("goods_user_footprint")
@Data
public class GoodsUserFootprintEntity  implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer footprintId;
    /**
     * 用户id
     */
    private String userId;
    /**
     * 商品id
     */
    private Integer commonId;
    /**
     * 商品分类
     */
    private Integer catId;
    /**
     * 时间
     */
    private String footprintTime;
    /**
     * 足记 - 年月日
     */
    private String footprintDate;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
