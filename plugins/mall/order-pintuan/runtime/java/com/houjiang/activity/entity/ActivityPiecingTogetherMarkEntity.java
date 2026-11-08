package com.houjiang.activity.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
 /**
 * @ClassName
 * @Description  拼团，团长标识-我的拼团
 * @Author Amos
 * @Date 2023-06-01 11:57:59
 * @Version 1.0
 * @author Amos
 */
@TableName("hou_addon_activity_piecing_together_mark")
@Data
public class ActivityPiecingTogetherMarkEntity  implements Serializable {
    /**
     * 主键，自增长
     */
    private Integer id;
    /**
     * 团长标识
     */
    private String markId;
    /**
     * 团长ID
     */
    private String userId;
    /**
     * 拼团详情
     */
    private Integer detailId;
    /**
     * 创建时间
     */
    private Date createdTime;
    /**
     * 当前参团人数
     */
    private Integer num;
    /**
     * 0,等待成团。1为已成团，2为成团失败，3为已完成退款
     */
    private Integer status;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 还差几人成团
     */
    private Integer surplusNum;
    /**
     * 头像地址
     */
    private String userAvatar;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
