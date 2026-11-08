package com.houjiang.activity.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
 /**
 * @ClassName
 * @Description  购买人
 * @Author Amos
 * @Date 2023-06-01 11:58:24
 * @Version 1.0
 * @author Amos
 */
@TableName("hou_addon_activity_piecing_together_buyer")
@Data
public class ActivityPiecingTogetherBuyerEntity  implements Serializable {

    private Integer id;
    /**
     * 拼团详情
     */
    private Integer detailId;
    /**
     * 购买者ID
     */
    private String userId;
    /**
     * 购买时间
     */
    private Date createdTime;
    /**
     * 是否是同一个团的，对应hou_pingtuan_mark表
     */
    private String markId;
    /**
     * 在团购，人中排第几。0为单独团
     */
    private Integer ranking;
    /**
     * 用户昵称
     */
    private String userName;
    /**
     * 头像地址
     */
    private String userAvatar;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
