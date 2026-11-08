package com.houjiang.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.job.dto.CategoryMonthlySalesDTO;
import com.houjiang.job.entity.JobStatisticsCatEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Entity com.houjiang.job.entity.JobStatisticsCatEntity
 * @author Amos
 */
@Mapper
public interface JobStatisticsCatMapper extends BaseMapper<JobStatisticsCatEntity> {

    /**
     * 查询月份真实交易
     * @param param
     * @return
     */
    List<CategoryMonthlySalesDTO> queryCatTurnoverReal(CategoryMonthlySalesDTO param);

    /**
     * 查询月份自身交易
     * @param param
     * @return
     */
    List<CategoryMonthlySalesDTO> queryCatTurnoverSelf(CategoryMonthlySalesDTO param);
}




