package com.houjiang.mall.vo.setting;

import lombok.Data;

import java.io.Serializable;

/**
 * 字典数据 VO（对外输出）。
 * 时间字段统一格式化为 yyyy-MM-dd HH:mm:ss 字符串。
 *
 * @author Amos
 */
@Data
public class SettingDictDataVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private Integer typeId;

    private String typeValue;

    private String name;

    private String value;

    private String remark;

    private Integer sort;

    private Integer status;

    private String createTime;

    private String updateTime;
}
