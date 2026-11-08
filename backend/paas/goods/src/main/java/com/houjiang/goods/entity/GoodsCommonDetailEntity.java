package com.houjiang.goods.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;
 /**
 * @ClassName
 * @Description  商品公共内容详情表
 * @Author Amos
 * @Date 2023-05-25 17:51:41
 * @Version 1.0
 * @author Amos
 */
@TableName("goods_common_detail")
@Data
@Accessors(chain = true)
public class GoodsCommonDetailEntity  implements Serializable {

    @TableId("common_id")
    private Integer commonId;
    /**
     * 商品内容
     */
    private String commonBody;
    /**
     * 时间戳
     */
    private Date ts;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
