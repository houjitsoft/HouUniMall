package com.houjiang.mall.entity.live;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 直播活动实体类
 * 表名: hou_mall_live_activity
 * @author Amos
 */
@Data
@TableName("hou_mall_live_activity")
public class LiveActivity implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String coverImage;
    private String description;
    private Long anchorId;
    private String anchorName;
    private String roomId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer viewerCount;
    private Integer likeCount;
    /** 状态: 0-未开始 1-直播中 2-已结束 3-已取消 */
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
