package com.houjiang.commons.vo.base;


import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

/**
 * 配置表
 *
 * @since 2023-05-10 14:00:51
 * @author Amos
 */
@Data
public class BaseSysConfigsVO {
    /**
     * 配置键
     */
    @TableId
    private String configKey;
    /**
     * 配置值:
     */
    private String configValue;
    /**
     * 配置类型
     */
    private String configType;
    /**
     * 配置内容描述
     */
    private String configComment;
    /**
     * 配置排序
     */
    private Integer configSort;
    /**
     * 操作类型
    private String opType;
    /**
     * 操作类型值
     */
    private String opValue;
    /**
     * 状态值，1可用，0不可用
     */
    private Integer configEnable;
    /**
     * 终端选择，1pc，2移动端
     */
    private Integer terminalType;
    /**
     * 创建人
     */
    private String createBy;
    /**
     * 创建时间
     */
    private Date createDate;
    /**
     * 更新人
     */
    private String updateBy;
    /**
     * 更新时间
     */
    private Date updateDate;

}

