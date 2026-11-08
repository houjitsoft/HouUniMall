package com.houjiang.merchant.vo.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class UserVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID")
    private Integer id;

    @Schema(description = "用户编码")
    private Integer sn;

    @Schema(description = "用户头像")
    private String avatar;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "登录账号")
    private String account;

    @Schema(description = "手机号码")
    private String mobile;

    @Schema(description = "用户性别")
    private String sex;

    @Schema(description = "注册渠道")
    private Integer channel;

    @Schema(description = "注册渠道")
    private String channelText;

    @Schema(description = "最后登录IP")
    private String loginIp;

    @Schema(description = "用户余额")
    private BigDecimal userMoney;

    @Schema(description = "最后登录时间")
    private String loginTime;

    @Schema(description = "创建时间")
    private String shopFollowTime;

    @Schema(description = "创建时间")
    private String createTime;
    public void setSex(Integer sex) {
        switch (sex) {
            case 0:
                this.sex = "未知";
                break;
            case 1:
                this.sex = "男";
                break;
            case 2:
                this.sex = "女";
                break;
        }
    }

}
