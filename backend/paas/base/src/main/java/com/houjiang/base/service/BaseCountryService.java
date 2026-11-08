package com.houjiang.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.base.dto.BaseCountryDTO;
import com.houjiang.base.entity.BaseCountryEntity;
import com.houjiang.commons.util.ResponseResult;

import java.util.List;


/**
 * @ClassName BaseCountryService
 * @Description  国家
 * @Author Amos
 * @Date 2024-10-22 15:59:38
 * @Version 1.0
 * @author Amos
 */ 
public interface BaseCountryService extends IService<BaseCountryEntity> {
    /**
     * 查询所有国家信息
     *
     * 此方法旨在获取系统中所有国家的信息，通过BaseCountryDTO对象作为查询条件
     * 它允许调用者根据特定条件检索国家信息列表
     *
     * @param baseCountryDTO 基础国家DTO，包含查询条件
     * @return ResponseResult 包含查询结果的响应对象
     */
    ResponseResult queryCountryAll(BaseCountryDTO baseCountryDTO);

    /**
     * 更新国家信息
     *
     * 该方法通过接收一个包含国家数据的DTO对象来更新国家信息
     * 主要用于在系统中同步和更新国家数据，保持数据的时效性和准确性
     *
     * @param baseCountryDTO 包含待更新国家信息的数据传输对象
     * @return 返回一个ResponseResult对象，其中包含更新操作的结果信息
     */
    ResponseResult updateCountry(BaseCountryDTO baseCountryDTO);
    /**
     * 删除国家信息
     *
     * 该方法通过国家ID来删除国家信息从数据库中移除对应的记录
     *
     * @param baseCountryDTO 包含国家ID的请求数据对象，用于指定要删除的国家
     * @return 返回一个ResponseResult对象，其中包含删除操作的结果信息
     */
    ResponseResult deleteCountry(BaseCountryDTO baseCountryDTO);
    /**
     * 查询国家和货币信息
     *
     * 本方法用于获取系统中已配置的国家和货币信息，以响应结果的形式返回
     * 主要用途是用于界面展示或者其他系统模块调用，提供基础数据支持
     *
     * @return ResponseResult 包含国家和货币信息的响应结果对象
     */
    ResponseResult queryCountryAndCurrency();
    /**
     * 查询当前的货币信息
     *
     * 该方法通过调用运输模板查询货币信息的接口，获取当前系统中配置的货币信息
     * 主要用于在运输过程中，获取相关的货币汇率、货币符号等信息，以便在国际贸易中进行货币转换和显示
     *
     * @return ResponseResult 包含货币信息的响应结果
     */
    ResponseResult transportTemplateQueryCurrency();

    /**
     * 根据国家ID查询国家信息
     *
     * @param countryId 国家ID，用于唯一标识一个国家
     * @return ResponseResult 包含国家信息的响应结果
     */
    ResponseResult queryCountryById(Integer countryId);

    /**
     * 查询国家名称 feign
     * @param countryIds
     * @return
     */
    List<String> queryCountryName(List<Integer> countryIds);

    /**
     * 按常用、国家首字母排序查询国家
     * @param baseCountryDTO
     * @return
     */
    ResponseResult queryCountryByInitial(BaseCountryDTO baseCountryDTO);
}

