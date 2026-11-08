package com.houjiang.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.commons.dto.goods.WebGoodsCatQueryDTO;
import com.houjiang.commons.vo.base.BaseSysConfigsVO;
import com.houjiang.commons.vo.goods.*;
import com.houjiang.commons.vo.goodscat.WebCatMenuVO;
import com.houjiang.commons.vo.shop.ApplyCatVO;
import com.houjiang.commons.vo.shop.ShopGoodsCatVO;
import com.houjiang.goods.dto.ManageItemCatQueryDTO;
import com.houjiang.goods.dto.PlatFormGoodsCatsDTO;
import com.houjiang.goods.dto.ShopClassBindQueryDTO;
import com.houjiang.goods.entity.GoodsCatEntity;
import com.houjiang.goods.vo.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @ClassName GoodsCatMapper
 * @Description 店铺类目表
 * @Author Amos
 * @Date 2023/6/6 15:31
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface GoodsCatMapper extends BaseMapper<GoodsCatEntity> {

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
     * 查询所有分类
     * @return
     */
    List<WebCatMenuVO> selectAllCat();

    /**
     * 查询商品分类
     *
     * @param
     * @return
     */
    List<ManageShopCatMenuVO> selectAllCat2();

    /**
     * 查询商品分类
     *
     * @return
     */
    List<ManageCatVO> selectByCatParentId(@Param("catParentId")Integer catParentId, @Param("shopType") Integer shopType);

    /**
     * 查询商家入职的分类
     * @param shopId
     * @return
     */
    List<Integer> queryRuZhuCat(@Param("shopId") Integer shopId);

    /**
     * 查询当前分类的信息
     * @param catId
     * @return
     */
    ManageCatVO queryByCatId2(Integer catId);

    /**
     * 查询所有当前分类的信息
     * @param jiaoList
     * @return
     */
    List<ManageCatVO> queryByCatIds2(@Param("list") Set<Integer> jiaoList);

    /**
     * 根据一级分类id 查询所有二级分类
     * @param catParentId
     * @return
     */
    List<Integer> queryByPranent(@Param("catParentId")Integer catParentId);

    /**
     * 根据入职的分类id查询入职分类的上级id
     * @param ruZhuCatids
     * @return
     */
    List<Integer> queryRuZhuPranentCat(@Param("list") List<Integer> ruZhuCatids);

    /**
     * 查询所有当前分类的信息
     * @param jiaoList
     * @return
     */
    List<ManageCatVO> queryByCatIds(@Param("list") List<Integer> jiaoList);

    /**
     * 查询分类
     * @param param
     * @return
     */
    List<GoodsCatVO> selectByParam(@Param("param") WebGoodsCatQueryDTO param);

    /**
     * 查询商品分类 3级
     * @param param
     * @return
     */
    IPage<GoodsCatEntity> getGoodscatByPage(IPage<GoodsCatEntity> page , @Param("param") Map<String,Object> param);

    /**
     * 查询商品类型分类
     * @param param
     * @return
     */
    List<ManageShopCatMenuVO> selectPlatFormCats(@Param("param") Map<String, Object> param);

    /**
     * 查询商品分类
     */
    List<ManageShopCatMenuVO> selectPlatFormCats2(@Param("catShowStatus")Integer catShowStatus);
    /**
     * 查询商品分类总数
     */
    Integer selectPlatFormCatsCount2(@Param("catShowStatus")Integer catShowStatus);

    /**
     * 查询商品分类总数
     */
    Integer selectPlatFormCatsCount(@Param("param") Map<String, Object> param);

    /**
     * 查询店铺经营类目商品分类id
     * @param param
     * @return
     */
    List<Integer> getShopCats(@Param("param") Map<String, Object> param);

    /**
     * 根据是否在前台显示查询分类
     * @param catShowStatus
     * @return
     */
    List<PlatFormGoodsCatsVO> selectPlatFormCatsNotIntegral(@Param("catShowStatus")Integer catShowStatus);
    /**
     * 根据是否在前台显示查询分类数量
     * @param catShowStatus
     * @return
     */
    Integer selectPlatFormCatsCountNotIntegral(@Param("catShowStatus")Integer catShowStatus);

    /**
     * 查询登录用户已申请经销商成功的店铺id
     * @param dealerShopId
     * @param supplierShopList
     * @return
     */
    List<BaseDealerApplyInfoVO> queryDealerApplyListByShop(@Param("dealerShopId") String dealerShopId, @Param("supplierShopList") List<Integer> supplierShopList);

    /**
     * 根据三级分类id获取所有父分类的名字
     *
     * @param productClassId
     * @return
     */
    List<String> querySonCatByParentId(Integer productClassId);


    /**
     * 查看店铺类型
     * @param shopId
     * @return
     */
    Integer selectShopType(@Param("shopId") Integer shopId);

    /**
     * 查询所有下级分类
     * @param catId
     * @return
     */
    List<WebCatMenuVO> selectByCatParentId2(@Param("catParentId")Integer catId);

    /**
     * 批量查询所有下级分类
     * @param list
     * @return
     */
    List<WebCatMenuVO> selectByCatParentId3(@Param("list")List<Integer> list);

    /**
     * 查询积分商品分类
     *
     * @param catParentId
     * @return
     */
    List<GoodsCatEntity> queryIntegralItemCat(@Param("catParentId") Integer catParentId);

    /**
     * 查询积分商品分类
     * @param catId
     * @return
     */
    List<GoodsCatResultVO> selectIntegralCatByParentId(@Param("catId")Integer catId);
    /**
     * 查询积分商品分类
     * @param catId
     * @return
     */
    List<GoodsCatResultVO> selectIntegralCat(@Param("catId")Integer catId);


    /**
     * 查询分类id
     *
     * @param catId
     * @return
     */
    List<Integer> selectCatLevel(Integer catId);

    /**
     * 通过catId查询商品数量
     *
     * @param catIds
     * @return
     */
    Integer selectGoodsCountByCatIds(@Param("catIds") List<Integer> catIds);

    /**
     * 查询当前分类是否 是商家的经营类目
     *
     * @param catIdList
     * @return
     */
    Integer queryShopBindCat(@Param("catIds") List<Integer> catIdList);

    /**
     * 修改分类
     *
     * @param param
     * @return
     */
    void updateGoodsCat(@Param("param")PlatFormGoodsCatsDTO param);


    /**
     * 添加分类
     * @param param
     * @return
     */
    int addPlatFormCatsBatch2(@Param("param")PlatFormGoodsCatsDTO param);

    /**
     * 查询分类信息
     * @param catId
     * @return
     */
    WebCatMenuVO queryByCatId(@Param("catId")Integer catId);

    /**
     * 查询某个分类下是否允许发布虚拟商品
     * @param catId
     * @return
     */
    ManageCatResultVO queryVirtualByCatId(@Param("catId")Integer catId);


    /**
     * 查询所有分类
     * @param shopId
     * @param catName
     * @return
     */
    List<ManageShopCatMenuVO> selectAllCat1(@Param("shopId") Integer shopId,@Param("catName") String catName);

    /**
     * 根据id查询分类名
     * @param catId
     * @return
     */
    List<String> queryCatName(@Param("catId") Integer catId);

    /**
     * 新增店铺经营类目
     * @param map
     * @return
     */
    Integer insertclassbind(@Param("param") Map<String, Object> map);

    /**
     * 删除店铺经营类目
     * @param map
     * @return
     */
    Integer deleteclassbind(@Param("param") Map<String, Object> map);

    /**
     * 查询商家申请经营分类列表
     */
    IPage<ApplyCatVO> qureyApplyCat(IPage<ApplyCatVO> page , @Param("param") Map<String, Object> param);

    /**
     * 查询商品分类
     */
    List<ApplyCatVO> querycat(@Param("param")Map<String, Object> map);


    /**
     * 查询平台默认质保金
     * @param configType
     * @return
     */
    List<BaseSysConfigsVO> selectByConfigType(@Param("configType") String configType);

    /**
     * @return 处理结果
     */
    List<ApplyCatVO> queryJoinCat(@Param("param")Map<String, Object> param);

    /**
     * 查询全部积分分类
     * @return
     */
    List<ManageShopCatMenuVO> selectIntegralCats();

    /**
     * 查询全部积分分类条数
     * @return
     */
    Integer selectIntegralCatsCount();

    /**
     * 更新商品积分分类
     *
     * @param param
     */
    void updateIntegralGoodsCat(@Param("param") PlatFormGoodsCatsDTO param);

    /**
     * 新增积分分类
     * @param goodsCats
     */
    void addIntegralCatsBatch(@Param("goodsCats") List<PlatFormGoodsCatsDTO> goodsCats);

    /**
     * 查询父猫
     *
     * @param parentId
     * @return
     */
    PlatFormGoodsCatsVO selectParentCat(Integer parentId);

    /**
     * 根据分类id得到商品属性组id
     * @param catId 分类id
     * @return 商品属性组id
     */
    Integer getCatGroupId(@Param("catId") Integer catId);

    /**
     * @return 处理结果
     */
    List<GoodsCatResultVO> selectNavSaleCategories(@Param("catId") Integer catId);

    /**
     * @return 处理结果
     */
    List<SecordNameListVO> selectLevelOneCategory(@Param("integralGoods") Integer integralGoods);

    /**
     * @return 处理结果
     */
    List<SecordNameListVO> selectLineSaleCategoriesByThreeCat(@Param("catId") Integer catId);

    /**
     * @return 处理结果
     */
    List<GoodsCatResultVO> selectLineSaleCategoriesByCatId(@Param("catId") Integer catId);

    /**
     * 根据三级分类id得到一至三级分类名称
     *
     * @param catId
     * @return
     */
    String getAllCatName(@Param("catId") Integer catId);

    /**
     * @param catId cat主键
     * @return 处理结果
     */
    List<GoodscCatVO> selectCatList(Integer catId);

    /**
     * @return 处理结果
     */
    List<ManageShopCatMenuVO> queryAllCatByAdmin();

    /**
     * @return 处理结果
     */
    List<ManageCatVO> querySupplierLevelCatByShopId(@Param("param") ManageItemCatQueryDTO param);

    /**
     * @return 处理结果
     */
    List<ManageCatVO> queryLevelCatBySelfShop(@Param("param") ManageItemCatQueryDTO param);

    /**
     * @return 处理结果
     */
    List<ManageShopCatMenuVO> selectAll();

    /**
     * @return 处理结果
     */
    List<Integer> getAllSonLevelCat(@Param("catId") Integer catId);

    /**
     * 查询在前台显示的类目
     *
     * @param catId
     * @return
     */
    ManageShopCatMenuVO selectByCatId2(@Param("catId") Integer catId);

    /**
     * 查询比例
     *
     * @param catId
     * @return
     */
    CommissionRateBeanVO selectCommissionRate(@Param("catId") Integer catId);


    /**
     * 查询所有分类
     * @return
     */
    List<ShopGoodsCatVO> selectAllCat3();


    /**
     * 查询分类
     * @param catId
     * @return
     */
    GdbMerchantGoodsCatVO selectGoodsCatByPrimaryKey(Integer catId);


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
    List<WebCatMenuVO> selectAllCatFeign();

    /**
     * 查询分类
     * @param catId
     * @return
     */
    List<SecordNameListVO> selectByCatParentIdFeign(@Param("catParentId")Integer catId);
}
