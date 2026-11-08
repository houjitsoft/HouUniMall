package com.houjiang.activity.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
 /**
 * @ClassName
 * @Description  拼团信息表 参与者所属团 及拼团类型
 * @Author Amos
 * @Date 2023-06-01 11:59:15
 * @Version 1.0
 * @author Amos
 */
@TableName("hou_addon_activity_piecing_together_temp")
@Data
public class ActivityPiecingTogetherTempEntity  implements Serializable {
    /**
     * 主键
     */
    private String id;
    /**
     * 活动详情id
     */
    private Integer detailId;
    /**
     * 团长id,0表示当前用户为团长
     */
    private Long markId;
    /**
     * 商品id
     */
    private Long goodsId;
    /**
     * 订单id
     */
    private String orderId;
    /**
     * 用户id
     */
    private String userId;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 0拼团，1单独购买
     */
    private Integer type;
    /**
     * 支付状态 0 未付款 1 已付款 2 以退款
     */
    private Integer payStart;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
