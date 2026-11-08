package com.houjiang.base.vo;

import com.houjiang.base.entity.BaseCountryEntity;
import com.houjiang.base.entity.BaseCurrencySymbolEntity;
import lombok.Data;

import java.util.List;

/**
 * @ClassName CountryAndCurrencyVO
 * @Author Amos
 * @Date 2025/2/21 10:50
 * @Version 1.0
 * @author Amos
 */
@Data
public class CountryAndCurrencyVO {
    /**
     * 存储基础国家实体的列表
     * 用于管理多个国家对象，提供对国家数据的集合操作
     */
    private List<BaseCountryEntity> baseCountryEntities;

    /**
     * 存储基础货币符号实体的列表
     * 用于管理多种货币符号对象，提供对货币符号数据的集合操作
     */
    private List<BaseCurrencySymbolEntity> baseCurrencySymbolEntities;
}
