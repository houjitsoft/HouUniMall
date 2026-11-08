package com.houjiang.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.base.dto.BaseSystemPlatformNavDTO;
import com.houjiang.base.entity.BaseSystemPlatformNavEntity;
import com.houjiang.commons.util.ResponseResult;


/**
 * 页面导航表(BaseSystemPlatformNav)
 *
 * @since 2023-03-28 17:29:45
 * @author Amos
 */
public interface BaseSystemPlatformNavService extends IService<BaseSystemPlatformNavEntity> {

    /**
     * 修改 或 添加 页面导航信息
     *
     * @param baseSystemPlatformNavDto
     * @return
     */
    ResponseResult updatePlatformNav(BaseSystemPlatformNavDTO baseSystemPlatformNavDto);

    /**
     * 删除页面导航信息
     *
     * @param baseSystemPlatformNavDto
     * @return
     */
    ResponseResult deletePlatformNav(BaseSystemPlatformNavDTO baseSystemPlatformNavDto);


    /**
     * 查询页面导航信息
     *
     * @param baseSystemPlatformNavDto
     * @return
     */
    ResponseResult querySystemPlatformNavById(BaseSystemPlatformNavDTO baseSystemPlatformNavDto);


    /**
     * 页面导航 查询列表
     *
     * @param pageHelper
     * @return
     */
    ResponseResult querySystemPlatformNavList(BaseSystemPlatformNavDTO baseSystemPlatformNavDto);
}

