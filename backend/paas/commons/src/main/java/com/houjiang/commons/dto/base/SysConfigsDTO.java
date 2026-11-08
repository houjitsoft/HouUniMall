package com.houjiang.commons.dto.base;

import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 配置表
 *
 * @author Amos
 * @TableName merchant_sys_configs
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysConfigsDTO extends UserTokenBeanDTO implements Serializable {

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
     * 操作类型
     */
    private String opType;

    /**
     * 操作类型值
     */
    private String opValue;

    /**
     * 配置排序
     */
    private Integer configSort;

    /**
     * 状态值，1可用，0不可用
     */
    private Boolean configEnable;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateDate;

    /**
     * 终端选择，1pc，2移动端
     */
    private Integer terminalType;

}
