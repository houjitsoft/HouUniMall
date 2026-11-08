package com.houjiang.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.base.entity.BaseSearchWordEntity;
import com.houjiang.commons.dto.goods.GoodsMerchantSearchWordDTO;
import com.houjiang.commons.util.ResponseResult;

/**
 * 搜索热门词(BaseSearchWord)
 *
 * @since 2023-03-28 14:13:14
 * @author Amos
 */
public interface BaseSearchWordService extends IService<BaseSearchWordEntity> {
    /**
     * 查询热搜词 feign
     * @param commonName
     * @return
     */
    GoodsMerchantSearchWordDTO selectBySearchName(String commonName);
    /**
     *   修改热搜词 feign
     * @return
     */
    Integer updateByPrimaryKeySelective(GoodsMerchantSearchWordDTO goodsMerchantSearchWordDto);
    /**
     *   添加热搜词 feign
     * @return
     */
    Integer insertSelective(GoodsMerchantSearchWordDTO goodsMerchantSearchWordDto);


    /**
     * 查询热搜词
     *
     * @param baseSearchWordDtoReq
     * @return
     */
    ResponseResult querySearchWord(GoodsMerchantSearchWordDTO baseSearchWordDtoReq);

    /**
     * 移动端查询热搜词
     * @return
     */
    ResponseResult queryKeyWords();

}

