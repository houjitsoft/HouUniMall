package com.houjiang.goods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.goods.entity.GoodsCommonAttributeAssociationEntity;
import com.houjiang.goods.mapper.GoodsAttributeContentMapper;
import com.houjiang.goods.mapper.GoodsAttributeMapper;
import com.houjiang.goods.mapper.GoodsCommonAttributeAssociationMapper;
import com.houjiang.goods.service.GoodsCommonAttributeAssociationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author Amos
 */
@Service
public class GoodsCommonAttributeAssociationImpl extends ServiceImpl<GoodsCommonAttributeAssociationMapper, GoodsCommonAttributeAssociationEntity>
    implements GoodsCommonAttributeAssociationService {

    @Autowired
    private GoodsAttributeMapper goodsAttributeMapper;

    @Autowired
    private GoodsAttributeContentMapper goodsAttributeContentMapper;

    /**
     * 根据属性ID查询属性关联信息，检查属性是否已被发布。
     *
     * @param map 包含属性ID的映射对象。
     * @return 如果属性未被发布，则返回成功结果；如果属性已发布，则返回失败结果，提示属性已发布。
     */
    @Override
    public ResponseResult selectAttribute(Map<String, Object> map) {
        // 从传入的映射对象中获取属性ID
        Integer attriId = (Integer) map.get("attriId");

        // 检查属性ID是否为空，如果不为空，则查询该属性ID对应的属性关联信息
        if (null != attriId) {
            // 使用Lambda表达式构建查询条件，查询属性关联实体列表
            List<GoodsCommonAttributeAssociationEntity> attrAssParams = this.baseMapper.selectList(new LambdaQueryWrapper<GoodsCommonAttributeAssociationEntity>()
                    .eq(GoodsCommonAttributeAssociationEntity::getAttributeId, attriId));

            // 如果查询结果的大小大于0，说明该属性已发布，返回失败结果
            if (attrAssParams.size() > 0) {
                return ResponseResult.fail(BizCodeEnume.PRODUCT_ATTRIBUTE_HAS_BEEN_PUBLISHED);
            }else{
                // 删除类目属性
                goodsAttributeContentMapper.deleteContent(attriId);
                //删除类目属性名
                goodsAttributeMapper.deleteAttris(attriId);
            }
        }

        // 如果属性ID为空，或者查询结果为空，说明该属性未被发布，返回成功结果
        return ResponseResult.success();
    }
}




