package com.houjiang.base.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 会员等级
 * @TableName base_membership_grade
 * @author Amos
 */
@TableName(value ="hou_base_membership_grade")
@Data
public class BaseMembershipGradeEntity implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 等级名称
     */
    private String gradeName;

    /**
     * 等级规则
     */
    private Integer gradeRule;

    /**
     * 折扣
     */
    private BigDecimal gradeDiscount;

    /**
     * 1正常 2 删除
     */
    private Integer gradeStart;

    /**
     * 等级图片url
     */
    private String gradeUrl;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        BaseMembershipGradeEntity other = (BaseMembershipGradeEntity) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getGradeName() == null ? other.getGradeName() == null : this.getGradeName().equals(other.getGradeName()))
            && (this.getGradeRule() == null ? other.getGradeRule() == null : this.getGradeRule().equals(other.getGradeRule()))
            && (this.getGradeDiscount() == null ? other.getGradeDiscount() == null : this.getGradeDiscount().equals(other.getGradeDiscount()))
            && (this.getGradeStart() == null ? other.getGradeStart() == null : this.getGradeStart().equals(other.getGradeStart()))
                        && (this.getGradeUrl() == null ? other.getGradeUrl() == null : this.getGradeUrl().equals(other.getGradeUrl()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getGradeName() == null) ? 0 : getGradeName().hashCode());
        result = prime * result + ((getGradeRule() == null) ? 0 : getGradeRule().hashCode());
        result = prime * result + ((getGradeDiscount() == null) ? 0 : getGradeDiscount().hashCode());
        result = prime * result + ((getGradeStart() == null) ? 0 : getGradeStart().hashCode());
        result = prime * result + ((getGradeUrl() == null) ? 0 : getGradeUrl().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", gradeName=").append(gradeName);
        sb.append(", gradeRule=").append(gradeRule);
        sb.append(", gradeDiscount=").append(gradeDiscount);
        sb.append(", gradeStart=").append(gradeStart);
        sb.append(", gradeUrl=").append(gradeUrl);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}