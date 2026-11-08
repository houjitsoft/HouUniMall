package com.houjiang.goods.dto;

import com.houjiang.commons.group.Delete;
import com.houjiang.commons.group.Insert;
import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StorageInfoDTO extends PageHelper {

    /**
     * 仓库id
     */
    @NotNull(message = "仓库不能为空", groups = {Delete.class})
    private Integer storageId;

    /**
     * 仓库编码
     */
    private String storageCode;

    /**
     * 仓库名称
     */
    @NotEmpty(message = "仓库名称不能为空", groups = {Insert.class})
    private String storageName;

    /**
     * 仓库所在地-省
     */
    @NotNull(message = "仓库所在地-省不能为空", groups = {Insert.class})
    private Integer storageProvince;

    /**
     * 仓库所在地-市
     */
    @NotNull(message = "仓库所在地-市不能为空", groups = {Insert.class})
    private Integer storageCity;

    /**
     * 仓库所在地-区
     */
    @NotNull(message = "仓库所在地-区不能为空", groups = {Insert.class})
    private Integer storageArea;

    /**
     * 仓库所在地-详细地址
     */
    @NotEmpty(message = "仓库所在地-详细地址不能为空", groups = {Insert.class})
    private String storageAddress;

    /**
     * 仓库联系人
     */
    @NotEmpty(message = "仓库联系人不能为空", groups = {Insert.class})
    private String storageContacts;

    /**
     * 仓库联系电话
     */
    @NotEmpty(message = "仓库联系电话不能为空", groups = {Insert.class})
    private String storageMobile;

    /**
     * 仓库状态 0 禁用 1 启用
     */
    private Integer storageStatus;

    /**
     * 店铺id
     */
    private Integer shopId;

}
