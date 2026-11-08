package com.houjiang.job.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.job.dto.CategoryMonthlySalesDTO;
import com.houjiang.job.entity.JobStatisticsCatEntity;

/**
 * @author Amos
 */
public interface JobStatisticsCatService extends IService<JobStatisticsCatEntity> {

    /*
/**
     * 插入商品类型每月插入表
     */
    ResponseResult insertOdbStatisticsCat(CategoryMonthlySalesDTO param);
}
