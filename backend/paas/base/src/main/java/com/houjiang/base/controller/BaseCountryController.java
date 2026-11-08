package com.houjiang.base.controller;



import com.houjiang.base.dto.BaseCountryDTO;
import com.houjiang.base.service.BaseCountryService;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * @ClassName BaseCountryController
 * @Description  国家
 * @Author Amos
 * @Date 2024-10-22 15:59:38
 * @Version 1.0
 * @author Amos
 */ 
@RestController
@RequestMapping("baseCountry")
public class BaseCountryController  {

    @Autowired
    private BaseCountryService baseCountryService;

    /**
     * 查询所有国家信息
     * 该方法通过POST请求接收一个BaseCountryDTO对象作为参数，用于查询国家信息
     * 主要用途是提供一个接口，用于获取数据库中所有国家的详细信息
     *
     * @param baseCountryDTO 包含查询条件的基础国家信息DTO
     * @return 包含查询结果的ResponseResult对象
     */
    @PostMapping("/queryCountryAll")
    public ResponseResult queryCountryAll(@RequestBody BaseCountryDTO baseCountryDTO){
        return baseCountryService.queryCountryAll(baseCountryDTO);
    }
    /**
     * 更新国家信息
     *
     * 该方法通过接收一个BaseCountryDTO对象作为请求体，将其传递给baseCountryService的updateCountry方法进行处理
     * 主要用途是当需要更新国家信息时，通过发送PUT请求来实现
     *
     * @param baseCountryDTO 包含更新后的国家信息的数据传输对象
     * @return 返回一个ResponseResult对象，其中包含更新操作的结果信息
     */
    @PostMapping("/updateCountry")
    public ResponseResult updateCountry(@RequestBody BaseCountryDTO baseCountryDTO){
        return baseCountryService.updateCountry(baseCountryDTO);
    }

    /**
     * 删除国家信息
     *
     * 通过调用基础国家服务的删除方法，传递国家数据传输对象以实现国家信息的删除
     *
     * @param baseCountryDTO 国家数据传输对象，包含需要删除的国家的相关信息
     * @return 返回删除操作的结果，包括操作状态和可能的响应消息
     */
    @PostMapping("/deleteCountry")
    public ResponseResult deleteCountry(@RequestBody BaseCountryDTO baseCountryDTO){
        return baseCountryService.deleteCountry(baseCountryDTO);
    }

    /**
     * 查询国家和货币信息
     *
     * 该方法通过POST请求处理查询国家和货币信息的请求它没有输入参数，
     * 并返回一个包含查询结果的ResponseResult对象
     *
     * @return ResponseResult 包含查询结果的响应对象
     */
    @PostMapping("/queryCountryAndCurrency")
    public ResponseResult queryCountryAndCurrency(){
        return baseCountryService.queryCountryAndCurrency();
    }
    /**
     * 处理 POST 请求，用于查询运输模板的货币信息
     *
     * 此方法调用 baseCountryService 的 transportTemplateQueryCurrency 方法来获取货币信息
     * 主要目的是为前端提供运输模板相关的货币数据，以便在需要时进行展示或进一步处理
     *
     * @return ResponseResult 包含货币信息的响应结果
     */
    @PostMapping("/transportTemplate/queryCurrency")
    public ResponseResult transportTemplateQueryCurrency(){
        return baseCountryService.transportTemplateQueryCurrency();
    }
    /**
     * 根据国家ID查询国家信息
     *
     * @param countryId 国家ID，用于标识特定的国家
     * @return 返回包含查询结果的ResponseResult对象
     */
    @PostMapping("/queryCountryById")
    public ResponseResult queryCountryById(@RequestParam("countryId") Integer countryId){
        return baseCountryService.queryCountryById(countryId);
    }
    /**
     * 查询国家名称 feign
     *
     * @return
     */
    @PostMapping(value = "/query/countryName")
    public List<String> queryCountryName(@RequestBody List<Integer> countryIds) {
        return baseCountryService.queryCountryName(countryIds);
    }

    /**
     * 按常用、国家首字母排序查询国家
     * @param baseCountryDTO
     * @return
     */
    @PostMapping("/queryCountryByInitial")
    public ResponseResult queryCountryByInitial(@RequestBody BaseCountryDTO baseCountryDTO){
        return baseCountryService.queryCountryByInitial(baseCountryDTO);
    }
}

