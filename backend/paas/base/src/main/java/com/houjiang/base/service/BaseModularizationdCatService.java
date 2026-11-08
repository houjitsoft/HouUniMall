package com.houjiang.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.base.dto.ModularizationdCatDTO;
import com.houjiang.base.dto.ModularizationdCatDataDTO;
import com.houjiang.base.entity.BaseModularizationdCatEntity;
import com.houjiang.commons.util.ResponseResult;

/**
 * @author Amos
 */
public interface BaseModularizationdCatService extends IService<BaseModularizationdCatEntity> {

    /**
     * TODO 补充方法说明
     * @param modularizationdCatParam TODO
     * @return TODO
     */
    ResponseResult selectAll(ModularizationdCatDTO modularizationdCatParam) throws IllegalAccessException;



    /**
     * 分类页 添加
     */
    ResponseResult addModularizationdCat(ModularizationdCatDTO ModularizationdCatDTO);


    /**
     * 分类页 修改
     */
   ResponseResult updateModularizationdCat(ModularizationdCatDTO ModularizationdCatDTO);

    /**
     * 分类页 删除
     */
    ResponseResult deleteModularizationdCat(ModularizationdCatDTO ModularizationdCatDTO);

    /**
     * 使用页面
     */
    ResponseResult updatePageApply(ModularizationdCatDTO ModularizationdCatDTO);

    /**
     * 查询分类页详情
     */
   ResponseResult querPageDataById(ModularizationdCatDTO ModularizationdCatDTO);


    /**
     * 解析分类页 数据
     */
   ResponseResult analysisPageData(ModularizationdCatDataDTO ModularizationdCatDTO);


    /**
     * 查询使用中的页面
     * @return
     */
    ResponseResult queryApplyPage();
}
