package com.houjiang.mall.entity.setting;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 通知设置实体（hou_merchant_notice_setting）。
 * 对应 like-common NoticeSetting。
 *
 * @author Amos
 */
@Data
@TableName("hou_merchant_notice_setting")
public class SettingNoticeSetting implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 场景编号 */
    private Integer sceneId;

    /** 场景名称 */
    private String sceneName;

    /** 场景描述 */
    private String sceneDesc;

    /** 接收人员: [1=用户, 2=平台] */
    private Integer recipient;

    /** 通知类型: [1=业务, 2=验证码] */
    private Integer type;

    /** 系统通知设置（JSON 字符串） */
    private String systemNotice;

    /** 短信通知设置（JSON 字符串） */
    private String smsNotice;

    /** 公众号通知设置（JSON 字符串） */
    private String oaNotice;

    /** 小程序通知设置（JSON 字符串） */
    private String mnpNotice;

    /** 更新时间（秒级时间戳） */
    private Long updateTime;
}
