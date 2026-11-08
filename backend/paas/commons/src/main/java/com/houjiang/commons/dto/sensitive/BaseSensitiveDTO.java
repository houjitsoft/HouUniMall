package com.houjiang.commons.dto.sensitive;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @ClassName
 * @Description  敏感词词库 dto
 * @Author Amos
 * @Date 2024-01-30 16:45:08
 * @Version 1.0
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class BaseSensitiveDTO extends PageHelper {
    /**
     * id
     */
    private Integer id;
    /**
     * 敏感词id 集合
     */
    private List<Integer> idList;
    /**
     * 敏感词
     */
    private String sensitiveName;
    /**
     * 敏感词 逗号分割
     */
    private String sensitiveNameStr;
    /**
     * 敏感词分类id
     */
    private Integer sensitiveClassificationId;
    /**
     * 添加时间
     */
    private Date sensitiveAddTime;
    /**
     * 状态 1启用  0 不启用
     */
    private Integer sensitiveState;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
