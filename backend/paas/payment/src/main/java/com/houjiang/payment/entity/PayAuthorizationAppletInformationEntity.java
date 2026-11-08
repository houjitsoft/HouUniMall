package com.houjiang.payment.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
/**
 * @ClassName
 * @Description  授权小程序信息表
 * @Author Amos
 * @Date 2023-07-20 09:39:49
 * @Version 1.0
 * @author Amos
 */
@TableName("hou_pay_authorization_applet_information")
@Data
public class PayAuthorizationAppletInformationEntity  implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer authorizerId;
    /**
     * 授权方appid
     */
    private String authorizerAppid;
    /**
     * 小程序secret
     */
    private String secret;
    /**
     * 接口刷新令牌
     */
    private String authorizerRefreshToken;
    /**
     * 商家id
     */
    private Integer shopId;
    /**
     * 预览信息 视频
     */
    private String videoIdList;
    /**
     * 预览信息 图片
     */
    private String picIdList;
    /**
     * 审核编号
     */
    private String auditId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
