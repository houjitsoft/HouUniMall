package com.houjiang.mall.vo.setting;

import lombok.Data;

import java.io.Serializable;

/**
 * 字典类型 VO（前端响应对象）
 *
 * @author Amos
 */
@Data
public class SettingDictTypeVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Integer id;

    /** 字典名称 */
    private String name;

    /** 字典类型编码 */
    private String type;

    /** 字典备注 */
    private String remark;

    /** 字典状态：0=停用，1=正常 */
    private Integer status;

    /** 创建时间（格式化字符串 yyyy-MM-dd HH:mm:ss） */
    private String createTime;

    /** 更新时间（格式化字符串 yyyy-MM-dd HH:mm:ss） */
    private String updateTime;
}
