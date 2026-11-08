package com.houjiang.goods.vo;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 商品仓库表(StorageInfo)表实体类
 *
 * @author Amos
 * @since 2024-09-19 14:06:31
 */
@Data
public class StorageInfoVO implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    
    /**
     * 仓库id
     */
    @TableId(type = IdType.AUTO)
    private Integer storageId;
    
    /**
     * 仓库编码
     */
    private String storageCode;
    
    /**
     * 仓库名称
     */
    private String storageName;
    
    /**
     * 仓库所在地-省
     */
    private Integer storageProvince;
    
    /**
     * 仓库所在地-市
     */
    private Integer storageCity;
    
    /**
     * 仓库所在地-区
     */
    private Integer storageArea;

    /**
     * 仓库所在地-省
     */
    private String storageProvinceName;

    /**
     * 仓库所在地-市
     */
    private String storageCityName;

    /**
     * 仓库所在地-区
     */
    private String storageAreaName;
    
    /**
     * 仓库所在地-详细地址
     */
    private String storageAddress;
    
    /**
     * 仓库联系人
     */
    private String storageContacts;
    
    /**
     * 仓库联系电话
     */
    private String storageMobile;
    
    /**
     * 仓库状态 0 禁用 1 启用
     */
    private Integer storageStatus;

    /**
     * 店铺id
     */
    private Integer shopId;
    
    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

}
