package com.houjiang.base.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.base.dto.BaseCountryDTO;
import com.houjiang.base.entity.BaseCurrencySymbolEntity;
import com.houjiang.base.vo.BaseCountryVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;
import com.houjiang.base.entity.BaseCountryEntity;


/**
 * @ClassName BaseCountryMapper
 * @Description  国家
 * @Author Amos
 * @Date 2024-10-22 15:59:38
 * @Version 1.0
 * @author Amos
 */ 
@Mapper
public interface BaseCountryMapper extends BaseMapper<BaseCountryEntity> {

    /**
     * 查询所有国家信息
     *
     * @param page 分页对象，用于封装分页查询参数和结果
     * @param baseCountryDTO 国家查询参数对象，用于传递查询条件
     * @return 返回封装了国家信息的分页对象
     */
    IPage<BaseCountryVO> queryCountryAll(Page<BaseCountryVO> page, @Param("param") BaseCountryDTO baseCountryDTO);
    /**
     * 查询并返回一系列基础货币符号实体
     * 该方法用于从运输模板中获取所有相关的货币符号信息，以供进一步处理或展示
     *
     * @return List<BaseCurrencySymbolEntity> 包含基础货币符号实体的列表
     */
    List<BaseCountryEntity> transportTemplateQueryCurrency();

    /**
     * 根据国家ID查询国家信息
     *
     * @param countryId 国家ID，用于唯一标识一个国家
     * @return 返回查询到的国家信息，如果没有找到对应的国家，则返回null
     */
    Integer queryCountryById(@Param("countryId") Integer countryId);

    /**
     * 查询国家名称
     * @param countryIds
     * @return
     */
    List<String> queryCountryName(@Param("countryIds")List<Integer> countryIds);

    /**
     * 查询所有国家 国家首字母 去重 按字母排序
     * @return
     */
    List<BaseCountryEntity> queryAllCountry(@Param("param") BaseCountryDTO baseCountryDTO);

}


