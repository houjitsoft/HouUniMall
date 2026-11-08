package com.houjiang.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.base.dto.BaseCurrencySymbolDTO;
import com.houjiang.base.entity.BaseCurrencySymbolEntity;
import com.houjiang.commons.util.ResponseResult;

/**
 * (BaseCurrencySymbol)表服务接口
 *
 * @author Amos
 * @since 2024-05-23 16:56:35
 */
public interface BaseCurrencySymbolService extends IService<BaseCurrencySymbolEntity> {



    /**
     * 货币符号列表
     *
     * @param currencySymbolDTO 包含货币符号信息的对象，用于插入操作
     * @return 返回操作的结果，成功或失败的信息封装在ResponseResult对象中
    //     */
    /**
     * TODO 补充方法说明
     * @param currencySymbolDTO TODO
     * @return TODO
     */
    ResponseResult listCurrencySymbol(BaseCurrencySymbolDTO currencySymbolDTO);

    /**
     * 提供开启货币符号功能的接口。
     *
     * @return ResponseResult 返回操作的结果，包括成功或失败的信息。
     */
    ResponseResult openCurrencySymbol();
    /**
     * 插入货币符号到数据库
     *
     * @param currencySymbolDTO 包含货币符号信息的对象，用于插入操作
     * @return 返回操作的结果，成功或失败的信息封装在ResponseResult对象中
     */
    ResponseResult insertCurrencySymbol(BaseCurrencySymbolDTO currencySymbolDTO);

    /**
     * 更新货币符号到数据库
     *
     * @param currencySymbolDTO 包含货币符号信息的对象，用于插入操作
     * @return 返回操作的结果，成功或失败的信息封装在ResponseResult对象中
     */
    ResponseResult updateCurrencySymbol(BaseCurrencySymbolDTO currencySymbolDTO);

    /**
     * 删除货币符号到数据库
     *
     * @param currencySymbolDTO 包含货币符号信息的对象，用于插入操作
     * @return 返回操作的结果，成功或失败的信息封装在ResponseResult对象中
     */
    ResponseResult deleteCurrencySymbol(BaseCurrencySymbolDTO currencySymbolDTO);
    /**
     * 根据货币代码获取相关信息
     *
     * @param currencyCode 货币代码，用于标识特定的货币
     * @return ResponseResult 包含货币相关信息的响应结果
     */
    ResponseResult getByCurrencyDetection(String currencyCode);
}