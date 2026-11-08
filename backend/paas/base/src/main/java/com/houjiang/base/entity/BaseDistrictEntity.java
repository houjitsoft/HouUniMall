package com.houjiang.base.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 地区表
 * @TableName base_district
 * @author Amos
 */
@TableName(value ="hou_base_district")
@Data
public class BaseDistrictEntity implements Serializable {
    /**
     * 地区id
     */
    @TableId(type = IdType.AUTO)
    private Integer districtId;

    /**
     * 地区名称
     */
    private String districtName;

    /**
     * 父id
     */
    private Integer districtParentId;

    /**
     * 等级
     */
    private Boolean districtIsLevel;

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
        BaseDistrictEntity other = (BaseDistrictEntity) that;
        return (this.getDistrictId() == null ? other.getDistrictId() == null : this.getDistrictId().equals(other.getDistrictId()))
            && (this.getDistrictName() == null ? other.getDistrictName() == null : this.getDistrictName().equals(other.getDistrictName()))
            && (this.getDistrictParentId() == null ? other.getDistrictParentId() == null : this.getDistrictParentId().equals(other.getDistrictParentId()))
            && (this.getDistrictIsLevel() == null ? other.getDistrictIsLevel() == null : this.getDistrictIsLevel().equals(other.getDistrictIsLevel()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getDistrictId() == null) ? 0 : getDistrictId().hashCode());
        result = prime * result + ((getDistrictName() == null) ? 0 : getDistrictName().hashCode());
        result = prime * result + ((getDistrictParentId() == null) ? 0 : getDistrictParentId().hashCode());
        result = prime * result + ((getDistrictIsLevel() == null) ? 0 : getDistrictIsLevel().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", districtId=").append(districtId);
        sb.append(", districtName=").append(districtName);
        sb.append(", districtParentId=").append(districtParentId);
        sb.append(", districtIsLevel=").append(districtIsLevel);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}