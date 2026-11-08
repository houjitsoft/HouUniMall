package com.houjiang.goods.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.dto.goods.WebGoodsCatQueryDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageHelper;
import com.houjiang.commons.vo.goods.*;
import com.houjiang.commons.vo.goodscat.WebCatMenuVO;
import com.houjiang.commons.vo.shop.ShopGoodsCatVO;
import com.houjiang.goods.dto.ManageItemCatQueryDTO;
import com.houjiang.goods.dto.PlatFormGoodsCatsDTO;
import com.houjiang.goods.dto.ShopClassBindQueryDTO;
import com.houjiang.goods.entity.GoodsCatEntity;
import com.houjiang.goods.vo.ManageCatResultVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * @ClassName GoodsCatService
 * @Description 店铺类目表(goods_cat)
 * @Author Amos
 * @Date 2023/6/6 15:27
 * @Version 1.0
 * @author Amos
 */
public interface GoodsCatService extends IService<GoodsCatEntity> {

    /**
     * 查询商品分类
     *
     * @return
     */
    ResponseResult queryAll();

    /**
     * 查询父类类目id
     * @param param
     * @return
     */
    Integer queryParentCatId(@Param("param") ShopClassBindQueryDTO param);

    /**
     * 查询当前类目id的总数
     * @param param
     * @return
     */
    Integer queryChildCount(@Param("param") Integer param);

    /**
     * 查询商品分类
     *
     * @param shopId
     * @return
     */
    ResponseResult queryAllCat(Integer shopId);

    /**
     * 查询商品分类
     *
     * @return
     */
    ResponseResult queryItemCat(ManageItemCatQueryDTO param);

    /**
     * 根据入住分类查询商品分类
     *
     * @return
     */
    ResponseResult queryByRuzhu(ManageItemCatQueryDTO param);

    /**
     * 查询商品分类
     *
     * @return
     */
    ResponseResult queryGoodsCats(PlatFormGoodsCatsDTO dto);

    /**
     * 查询商品分类 3级
     * @param map
     * @return
     */
    ResponseResult goodscat(Map<String,Object> map);


    /**
     * 查询商品分类  不查询积分分类
     *
     * @param map
     * @return
     */
    ResponseResult queryNotIntegral(Map<String,Object> map);



    /**
     * 根据分类id 查询商品
     *
     * @param catId
     * @return
     */
    ResponseResult queryByGoodsCatId(Integer catId,Integer goodsType,String token);

    /**
     * 商品分类查询（一级，二级，三级，品牌属性）
     * @param param
     * @return
     */
    ResponseResult queryCatListFilter(Map<String, Object> param);

    /**
     * 查询商品分类 fegin business
     *
     * @param catId
     * @return
     */
    ResponseResult queryCatByCatId(Integer catId);


    /**
     * 查询商品分类 fegin business
     *
     * @param map
     * @return
     */
    ResponseResult queryCatList(Map<String, Object> map);

    /**
     * 根据三级分类id获取所有父分类的名字
     *
     * @param productClassId
     * @return
     */
    List<String> querySonCatByParentId(Integer productClassId);

    /**
     * 查询积分商品分类
     *
     * @param map
     * @return
     */
    ResponseResult queryIntegralItemCat(Map<String, Object> map);

    /**
     * 查询积分商品分类
     * @param catParentId
     * @return
     */
    ResponseResult queryIntegralItemCat(Integer catParentId);


    /**
     * 判断分类名称是否重复
     *
     * @param map
     * @return
     */
    ResponseResult checkCatName(Map<String, Object> map);

    /**
     * 平台  查询平台所有积分分类
     *
     * @param pageHelper
     * @return
     */
    ResponseResult queryIntegralGoodsCatsAll(PageHelper pageHelper);

    /**
     * 平台添加积分商品
     * @param param
     * @return
     */
    ResponseResult addIntegralCat(PlatFormGoodsCatsDTO param);

    /**
     * 平台添加删除积分分类
     *
     * @param param
     * @return
     */
    ResponseResult deleteIntegralCat(PlatFormGoodsCatsDTO param);

    /**
     * 平台添加删除
     *
     * @param param
     * @return
     */
    ResponseResult deleteGoodsCat(PlatFormGoodsCatsDTO param);


    /**
     * 平台添加分类
     *
     * @param param
     * @return
     */
    ResponseResult addGoodsCat(PlatFormGoodsCatsDTO param);


    /**
     * 平台  查询平台所有分类
     *
     * @param pageHelper
     * @return
     */
    ResponseResult queryGoodsCatsAll(PageHelper pageHelper);

    /**
     * 查询某个分类下是否允许发布虚拟商品
     * @param param
     * @return
     */
    ResponseResult queryVirtual(ManageCatResultVO param);


    /**
     * h5 首页查询所有一级分类
     * @return
     */
    ResponseResult queryRankCat();

    /**
     * 查询店铺未选择的经营分类
     */
    ResponseResult queryNoCat(Map<String,Object> param);


    /**
     * 申请经营分类
     */
    ResponseResult applyCat(Map<String,Object> param);

    /**
     * 查询商家申请经营分类列表
     */
    ResponseResult qureyApplyCat(Map<String,Object> param);



    /**
     * 查询商品分类
     */
    ResponseResult queryCat(Map<String,Object> param);


    /**
     * 查询分类（排除积分分类）
     * @param map
     * @return
     */
    ResponseResult queryGoodsCatByCatId(Map<String, Object> map);

    /**
     * @param catId cat主键
     * @return 处理结果
     */
    ResponseResult queryFxCatList(Integer catId);

    /**
     * 根据分类id查询该分类下所有下级分类id
     * @param catId 分类id
     * @return 所有下级分类id
     */
    List<Integer> queryAllSubordinateCatIdByCatId(Integer catId);

    /**
     * 根据分类id查询该分类下所有上级分类id
     * @param catId 分类id
     * @return 所有上级分类id
     */
    List<GoodsCatResultVO> selectNavSaleCategories(Integer catId, Integer integralGoods);

    /**
     * 根据分类id集合查询上级分类集合
     * @param catIdList 分类id集合
     * @return 上级分类集合
     */
    List<GoodsCatResultVO> selectLineSaleCategoriesByThreeCat(List<Integer> catIdList);

    /**
     * 根据分类id集合查询该分类下所有下级分类id
     * @param catId 分类id
     * @return 所有下级分类id
     */
    List<GoodsCatResultVO> selectLineSaleCategoriesByCatId(Integer catId);

    /**
     * 通过参数查询
     *
     * @param param
     * @return
     */
    ResponseResult selectByParam(WebGoodsCatQueryDTO param);


    /**
     * 查询在前台显示的类目
     *
     * @param catId
     * @return
     */
    ManageShopCatMenuVO selectByCatId2(Integer catId);


    /**
     * 查询比例
     *
     * @param catId
     * @return
     */
    CommissionRateBeanVO selectCommissionRate(Integer catId);

    /**
     * 查询所有分类
     * @return
     */
    List<ShopGoodsCatVO> selectAllCat3();

    /**
     *  查询分类详情
     * @param catId
     * @return
     */
    GdbMerchantGoodsCatVO selectGoodsCatByPrimaryKey(Integer catId);


    /**
     * 查询分类名称
     * @param catId
     * @return
     */
    List<String> queryCatName(Integer catId);

    /**
     * 根据类别ID查询类别名称
     *
     * @param catId 类别ID
     * @return 类别名称字符串
     */
    String selectByCatNameString(Integer catId);

    /**
     * 查询所有子分类
     * @return
     */
    List<WebCatMenuVO> selectAllCat2();

    /**
     * 查询分类
     * @param catId
     * @return
     */
    List<SecordNameListVO> selectByCatParentId(Integer catId);
}
