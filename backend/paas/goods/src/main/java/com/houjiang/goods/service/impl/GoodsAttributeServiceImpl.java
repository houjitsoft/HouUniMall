package com.houjiang.goods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.goods.entity.GoodsAttributeContentEntity;
import com.houjiang.goods.entity.GoodsAttributeEntity;
import com.houjiang.goods.entity.GoodsBrandCatEntity;
import com.houjiang.goods.mapper.GoodsAttributeContentMapper;
import com.houjiang.goods.mapper.GoodsAttributeMapper;
import com.houjiang.goods.mapper.GoodsBrandCatMapper;
import com.houjiang.goods.service.GoodsAttributeService;
import com.houjiang.goods.vo.GoodsAttributeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
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
public class GoodsAttributeServiceImpl extends ServiceImpl<GoodsAttributeMapper, GoodsAttributeEntity>
        implements GoodsAttributeService {

    @Autowired
    private GoodsAttributeContentMapper goodsAttributeContentMapper;

    @Autowired
    private GoodsBrandCatMapper goodsBrandCatMapper;

    /**
     * 根据分类ID查询商品属性
     * @param map 包含分类ID的Map
     * @return 返回查询结果的ResponseResult对象
     */
    @Override
    public ResponseResult selectCatAttribute(Map<String, Object> map) {
        // 检查map和catId是否存在
        if (null != map && null != map.get("catId")) {
            Map<String,Object> result = new HashMap<>();

            List<GoodsAttributeVO> volist = new ArrayList<>();

            // 根据catId查询商品属性实体列表
            List<GoodsAttributeEntity> goodsAttributeResult=this.baseMapper.selectList(new LambdaQueryWrapper<GoodsAttributeEntity>()
                                                            .eq(GoodsAttributeEntity::getCatId,Integer.valueOf(map.get("catId").toString())));

            // 遍历商品属性实体列表
            if(!goodsAttributeResult.isEmpty()) {
                for (GoodsAttributeEntity goodsAttriResult : goodsAttributeResult) {
                    GoodsAttributeVO goodsAttResult =new GoodsAttributeVO();
                    // 根据属性ID查询属性内容实体列表
                    List<GoodsAttributeContentEntity> AttrContentResult = goodsAttributeContentMapper.selectList(new LambdaQueryWrapper<GoodsAttributeContentEntity>()
                                                                            .eq(GoodsAttributeContentEntity::getAttributeContentId,goodsAttriResult.getId()));

                    // 组装属性内容列表
                    List<String> list = new ArrayList<>();
                    for (GoodsAttributeContentEntity AttriContentRes : AttrContentResult) {
                        list.add(AttriContentRes.getAttributeContent());
                    }
                    goodsAttResult.setAttributeContent(list);
                    // 设置属性其他信息
                    goodsAttResult.setId(goodsAttriResult.getId());
                    goodsAttResult.setCommodityAttribute(goodsAttriResult.getCommodityAttribute());
                    goodsAttResult.setAttributeStatus(goodsAttriResult.getAttributeStatus());

                    volist.add(goodsAttResult);
                    // 将属性信息添加到结果集中
                    result.put("goodsAttResult",volist);
                }
            }

            // 根据catId查询品牌-分类关联列表
            List<GoodsBrandCatEntity> goodsBrand =  goodsBrandCatMapper.selectList(new LambdaQueryWrapper<GoodsBrandCatEntity>()
                        .eq(GoodsBrandCatEntity::getCatId,Integer.valueOf(map.get("catId").toString()))
                        .groupBy(GoodsBrandCatEntity::getBrandId));

            // 如果查询结果不为空，添加到结果集中
            if(!goodsBrand.isEmpty()){
                result.put("goodsBrand",goodsBrand);
            }
            // 返回成功结果，包含查询到的商品属性和品牌信息
            return ResponseResult.success(result);
        }
        // 如果catId不存在，返回参数缺失错误信息
        return ResponseResult.fail(BizCodeEnume.MISSING_PARAMETER);
    }

    /**
     * 根据分类ID和属性内容查询商品属性列表
     * @param map 包含分类ID和属性内容的Map
     * @return 返回查询结果的ResponseResult对象
     */
    @Override
    public ResponseResult queryBrandCatAttributeList(Map<String, Object> map) {
        // 从map中获取分类ID和属性内容
        Integer catId =(Integer)map.get("catId");
        String attributeContent =(String)map.get("attributeContent");

        // 根据catId查询商品属性实体列表
        List<GoodsAttributeEntity> goodsAttributeResult=this.baseMapper.selectList(new LambdaQueryWrapper<GoodsAttributeEntity>()
                                                            .eq(GoodsAttributeEntity::getCatId,catId));

        // 如果查询结果不为空，进行遍历并关联属性内容
        if(goodsAttributeResult.size()>0) {
            for (GoodsAttributeEntity goodsAttriResult : goodsAttributeResult) {
                // 根据属性ID和属性内容查询属性内容实体列表
                List<GoodsAttributeContentEntity> attrContentList = goodsAttributeContentMapper.selectList(new LambdaQueryWrapper<GoodsAttributeContentEntity>()
                                                                        .eq(GoodsAttributeContentEntity::getAttributeContentId,goodsAttriResult.getId())
                                                                        .like(GoodsAttributeContentEntity::getAttributeContent,attributeContent));

                // 给商品属性实体添加属性内容列表
                goodsAttriResult.setAttrContentList(attrContentList);
            }
            // 返回成功结果，包含查询到的商品属性列表
            return ResponseResult.success(goodsAttributeResult);
        }else{
            // 如果查询结果为空，返回未获取到信息的错误信息
            return ResponseResult.fail(BizCodeEnume.NO_INFORMATION_OBTAINED);
        }
    }
}
