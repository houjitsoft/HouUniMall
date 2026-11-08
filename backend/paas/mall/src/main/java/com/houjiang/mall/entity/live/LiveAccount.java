package com.houjiang.mall.entity.live;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 直播账号实体类
 * 表名: hou_mall_live_account
 * @author Amos
 */
@Data
@TableName("hou_mall_live_account")
public class LiveAccount implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.AUTO)
    private Long id;
    private String accountName;
    private String platform;
    private String avatar;
    private String phone;
    private String description;
    private Integer followerCount;
    private Integer liveCount;
    /** 状态: 0-禁用 1-正常 */
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
