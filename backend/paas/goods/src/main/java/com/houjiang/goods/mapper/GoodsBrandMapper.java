package com.houjiang.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.commons.vo.goods.GoodsBrandVO;
import com.houjiang.commons.vo.goodscat.WebCatMenuVO;
import com.houjiang.goods.dto.GoodsBrandDTO;
import com.houjiang.goods.entity.GoodsBrandEntity;
import com.houjiang.goods.vo.GoodsBrandByCatIdVO;
import com.houjiang.goods.vo.GoodsBrandCatParamVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity com.houjiang.goods.entity.GoodsBrand
 * @author Amos
 */
@Mapper
public interface GoodsBrandMapper extends BaseMapper<GoodsBrandEntity> {

    /**
     * 查询品牌分类列表
     * @param webCatMenus
     * @return
     */
    List<GoodsBrandCatParamVO> selectBrandCats(@Param("list")List<WebCatMenuVO> webCatMenus);


    /**
     * 新增品牌
     * @param param
     */
    void insertBrand(@Param("param") GoodsBrandDTO param);

    /**
     * 修改品牌
     * @param param
     */
    void updateBrand(@Param("param")GoodsBrandDTO param);


    /**
     * 分页查询品牌集合
     *
     * @param page
     * @param merchantGoodsBrandReq
     * @return
     */
    IPage<GoodsBrandEntity> queryBrandList(IPage<GoodsBrandEntity> page, @Param("param") GoodsBrandDTO merchantGoodsBrandReq);


    /**
     * 查询店铺品牌信息
     * @param param
     * @return
     */
    List<GoodsBrandEntity> selectByShopId(@Param("param")GoodsBrandDTO param);

    /**
     * 根据品牌id集合查询品牌集合
     *
     * @param brandIdList 品牌id集合
     * @return 品牌集合
     */
    List<GoodsBrandVO> selectGoodsBrandList(@Param("brandIdList") List<Integer> brandIdList);

    /**
     * 查询所有品牌
     *
     * @return
     */
    List<GoodsBrandByCatIdVO> selectAllBrand();

    /**
     * 查询分类绑定的品牌
     *
     * @param param
     * @return
     */
    List<GoodsBrandVO> findListByName(GoodsBrandDTO param);
}




