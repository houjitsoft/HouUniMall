package com.houjiang.goods.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.goods.entity.GoodsBrandCatEntity;
import com.houjiang.goods.mapper.GoodsBrandCatMapper;
import com.houjiang.goods.service.GoodsBrandCatService;
import com.houjiang.goods.vo.GoodsBrandCatParamVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @ClassName GoodsBrandCatServiceImpl
 * @Description  商品品牌分类表
 * @Author Amos
 * @Date 2023-06-26 13:50:11
 * @Version 1.0
 * @author Amos
 */
@Service
public class GoodsBrandCatServiceImpl extends ServiceImpl<GoodsBrandCatMapper, GoodsBrandCatEntity>
        implements GoodsBrandCatService {

    /**
     * 根据品牌和类别查询商品信息。
     * <p>
     * 通过映射对象map查询商品品牌和类别的列表。此方法用于在系统中根据用户提供的品牌和类别条件，
     * 查询相应的商品信息。返回的结果包含满足条件的所有商品品牌和类别信息。
     *
     * @param objectMap 映射对象，包含查询所需的条件参数。参数的具体含义取决于查询的需要，
     *                  可能包括品牌ID、类别ID等。
     * @return ResponseResult 对象，包含查询结果。如果查询成功，结果将包装在成功响应对象中返回；
     *         如果查询失败，可以通过该对象获取失败信息。
     */
    @Override
    public ResponseResult selectByBrandCat(Map<String, Object> objectMap) {
        // 调用baseMapper的selectBrandCatList方法查询商品品牌和类别列表
        List<GoodsBrandCatParamVO> goodsBrandCatParam = this.baseMapper.selectBrandCatList(objectMap);
        // 将查询结果包装在成功响应对象中返回
        return ResponseResult.success(goodsBrandCatParam);
    }
}
