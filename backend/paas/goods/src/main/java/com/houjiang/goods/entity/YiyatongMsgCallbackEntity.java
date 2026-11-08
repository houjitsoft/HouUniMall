package com.houjiang.goods.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 怡亚通消息回调记录表
 * @TableName yiyatong_msg_callback
 * @author Amos
 */
@TableName(value ="yiyatong_msg_callback")
@Data
public class YiyatongMsgCallbackEntity implements Serializable {
    /**
     * 
     */
    @TableId
    private Integer id;

    /**
     * 消息id
     */
    private String messageId;

    /**
     * 应用密钥
     */
    private String appKey;

    /**
     * 枚举编号
     */
    private String enumNum;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 签名
     */
    private String sign;

    /**
     * 是否已消费:0:未消费,1:已消费
     */
    private Integer isUse;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}