package com.houjiang.goods.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.goods.GoodsBrandVO;
import com.houjiang.goods.dto.GoodsBrandDTO;
import com.houjiang.goods.entity.GoodsBrandEntity;

import java.util.List;
import java.util.Map;

/**
 * @author Amos
 */
public interface GoodsBrandService extends IService<GoodsBrandEntity> {

    /**
     * 查询所有品牌
     *
     * @param
     * @return
     */
    ResponseResult queryBrandGoods();


    /*
     *查询品牌名称是否重复
     * */
    /**
     * @param map map
     * @return 处理结果
     */
    ResponseResult selectBrandName(Map<String, Object> map);

    /**
     * 品牌 删除
     *
     * @param dto
     * @return
     */
    ResponseResult deleteBrand(GoodsBrandDTO dto);


    /**
     * 品牌 新增 或 修改
     *
     * @param dto
     * @return
     */
    ResponseResult updateBrand(GoodsBrandDTO dto);

    /**
     * 查询品牌列表
     *
     * @param dto
     * @return
     */
   ResponseResult queryBrandList(GoodsBrandDTO dto);

    /**
     * 根据品牌id查询商品品牌
     *
     * @param param
     * @return
     */
    ResponseResult queryGoodsBrandById(GoodsBrandDTO param);


    /**
     * 商家中心-商品品牌查询列表
     *
     * @param param
     * @return
     */
    ResponseResult queryGoodsBrandList(GoodsBrandDTO param);

    /**
     * 根据品牌id集合查询品牌集合
     *
     * @param brandIdList 品牌id集合
     * @return 品牌集合
     */
    List<GoodsBrandVO> selectGoodsBrandList(List<Integer> brandIdList);

    /**
     * 获取分类关联品牌
     *
     * @param param
     * @return
     */
    ResponseResult findListByName(GoodsBrandDTO param);
}
