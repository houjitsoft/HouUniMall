package com.houjiang.userms.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Amos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VKSetVO {
    public static final Integer VK_SET_ID = 1;
    /**
     *
     */
    private Integer id;
    /**
     *微客名称
     */
    private String vkName;
    /**
     *顶部标题
     */
    private String vkTitle;
    /**
     *顶部图片
     */
    private String vkImage;
    /**
     *申请条件  1自动成为  2客户申请
     */
    private Integer applyType;
    /**
     *满足条件  1满足任一勾选条件  2同时满足所有条件
     */
    private Integer applyCondition;
    /**
     *条件类型 1购买指定商品  2邀请会员 3购买+邀请(如果条件是满足任一条件 则两者满足一样即可)
     */
    private Integer conditionType;
    /**
     *需要邀请的人数
     */
    private Integer inviteNum;
    /**
     *海报图片
     */
    private String posterImage;
    /**
     *是否开启 1开启 2关闭
     */
    private Integer vkState;
    /**
     * 微客须知
     */
    private String vkNotice;
}
