package com.houjiang.base.dto;


import com.baomidou.mybatisplus.annotation.TableField;
import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 资讯分类表(BaseInformationClassify)表实体类
 *
 * @author Amos
 * @since 2024-01-17 13:50:42
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseInformationClassifyDTO extends UserTokenBeanDTO implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 分类id
     */
    private Integer classifyId;

    /**
     * 分类名称
     */
    private String classifyName;

    /**
     * 父类
     */
    private Integer classifyParentId;

    /**
     * 排序
     */
    private Integer classifySort;

    /**
     * 分类级别
     */
    private Integer classifyLevel;

    /**
     * 是否在前台显示
     */
    private Integer classifyShowStatus;

    /**
     * 商品分类菜单集合
     */
    @TableField(exist = false)
    private List<BaseInformationClassifyDTO> childMenu;

}
