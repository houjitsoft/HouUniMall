package com.houjiang.goods.controller;

import com.houjiang.commons.annnotation.SensitiveCheck;
import com.houjiang.commons.dto.goods.WebGoodsCatQueryDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageHelper;
import com.houjiang.commons.vo.goods.*;
import com.houjiang.commons.vo.goodscat.WebCatMenuVO;
import com.houjiang.commons.vo.shop.ShopGoodsCatVO;
import com.houjiang.goods.dto.ManageItemCatQueryDTO;
import com.houjiang.goods.dto.PlatFormGoodsCatsDTO;
import com.houjiang.goods.dto.ShopClassBindQueryDTO;
import com.houjiang.goods.vo.ManageCatResultVO;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @ClassName GoodsCatController
 * @Description 店铺类目表
 * @Author Amos
 * @Date 2023/6/6 15:24
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("/goods/cat")
public class GoodsCatController extends BaseController{


    /**
     * 查询商品分类
     *
     * @return
     */
    @GetMapping("/queryAll")
    public ResponseResult queryAll() {
        ResponseResult result = goodsCatService.queryAll();
        return result;
    }

    /**
     * 获取商品分类树形结构
     * 用于前端商品分类管理页面
     *
     * @return ResponseResult 分类树形数据
     */
    @PostMapping("/category/tree")
    public ResponseResult getCategoryTree() {
        try {
            // 直接调用queryAll方法获取分类数据
            ResponseResult result = goodsCatService.queryAll();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.error("获取分类数据失败: " + e.getMessage());
        }
    }

    /**
     * 查询商品分类列表
     * 支持分页和条件查询
     *
     * @param param 查询参数
     * @return ResponseResult 分类列表数据
     */
    @PostMapping("/category/query")
    public ResponseResult queryCategoryList(@RequestBody Map<String, Object> param) {
        // 如果参数包含tree标识，返回树形结构
        if (param.containsKey("tree") && Boolean.TRUE.equals(param.get("tree"))) {
            return goodsCatService.queryAll();
        }
        // 否则返回列表数据
        return goodsCatService.queryAll();
    }

    /**
     * 查询父类类目id  feign
     * @param param
     * @return
     */
    @PostMapping("/query/parent/cat/id")
    public Integer queryParentCatId(@RequestBody ShopClassBindQueryDTO param) {
        Integer integer = goodsCatService.queryParentCatId(param);
        return integer;
    }

    /**
     * 查询当前类目id的总数  feign
     * @param param
     * @return
     */
    @PostMapping("/query/child/count")
    public Integer queryChildCount(@Param("param") Integer param) {
        Integer integer = goodsCatService.queryChildCount(param);
        return integer;
    }

    /**
     * 查询商品分类
     *
     * @param shopId
     * @return
     */
    @GetMapping(value = "/cat/query")
    public ResponseResult queryAllCat(Integer shopId) {
        ResponseResult result = goodsCatService.queryAllCat(shopId);
        return result;
    }


    /**
     * 查询商品分类
     *
     * @return
     */
    @PostMapping(value = "/itemcat/query")
    public ResponseResult queryItemCat(@RequestBody ManageItemCatQueryDTO param) {
        ResponseResult result = goodsCatService.queryItemCat(param);
        return result;
    }

    /**
     * 根据入住分类查询商品分类
     *
     * @return
     */
    @PostMapping(value = "/itemcat/queryByRuzhu")
    public ResponseResult queryByRuzhu(@RequestBody ManageItemCatQueryDTO param) {
        ResponseResult result = goodsCatService.queryByRuzhu(param);
        return result;
    }


    /**
     * 查询商品分类
     *
     * @return
     */
    @PostMapping("/platform/goods/cats/query")
    public ResponseResult queryGoodsCats(@RequestBody PlatFormGoodsCatsDTO dto) {
        ResponseResult result=goodsCatService.queryGoodsCats(dto);
        return result;
    }

    /**
     * 查询商品分类 3级
     * @param map
     * @return
     */
    @PostMapping("/platform/goodscat")
    public ResponseResult goodscat(@RequestBody Map<String,Object> map) {
        ResponseResult result=goodsCatService.goodscat(map);
        return result;
    }

    /**
     * 查询商品分类  不查询积分分类
     *
     * @param map
     * @return
     */
    @PostMapping(value = "/goods/cats/queryNotIntegral")
    public ResponseResult queryNotIntegral(@RequestBody Map<String,Object> map) {
        ResponseResult result=goodsCatService.queryNotIntegral(map);
        return result;
    }

    /**
     * 根据分类id 查询商品
     *
     * @param catId
     * @return
     */
    @GetMapping("/goods/cat/id/query")
    public ResponseResult queryByGoodsCatId(@RequestParam("catId") Integer catId,Integer goodsType,String token) {
        ResponseResult resultResp = goodsCatService.queryByGoodsCatId(catId,goodsType,token);
        return resultResp;
    }

    /**
     * 商品分类查询（一级，二级，三级，品牌属性）
     *
     * @param param
     * @return
     */
    @PostMapping("/query/filter")
    public ResponseResult queryCatListFilter(@RequestBody Map<String,Object> param) {
        ResponseResult result = goodsCatService.queryCatListFilter(param);
        return result;
    }


    /**
     * 查询商品分类 fegin business
     *
     * @param catId
     * @return
     */
    @GetMapping("/queryCatByCatId")
    public ResponseResult queryCatByCatId(@RequestParam("catId") Integer catId) {
        ResponseResult result = goodsCatService.queryCatByCatId(catId);
        return result;
    }


    /**
     * 查询商品分类 fegin business
     *
     * @param map
     * @return
     */
    @PostMapping("/queryCatList")
    public ResponseResult queryCatList(@RequestBody Map<String, Object> map) {
        ResponseResult result = goodsCatService.queryCatList(map);
        return result;
    }

    /**
     * 根据三级分类id获取所有父分类的名字
     *
     * @param productClassId
     * @return
     */
    @GetMapping("/querySonCatByParentId")
    public List<String> querySonCatByParentId(@RequestParam("productClassId") Integer productClassId) {
        List<String> result = goodsCatService.querySonCatByParentId(productClassId);
        return result;
    }

    /**
     * 查询积分商品分类
     *
     * @param map
     * @return
     */
    @PostMapping("/integral/itemcat/query")
    public ResponseResult queryIntegralItemCat(@RequestBody Map<String, Object> map) {
        ResponseResult result = goodsCatService.queryIntegralItemCat(map);
        return result;
    }



    /**
     * 查询积分商品分类
     *
     * @return
     */
    @GetMapping(value = "/itemcat/queryIntegral")
    public ResponseResult queryIntegralItemCat(@RequestParam Integer catParentId) {
        ResponseResult result = goodsCatService.queryIntegralItemCat(catParentId);
        return result;
    }


    /**
     * 判断分类名称是否重复
     *
     * @param map
     * @return
     */
    @PostMapping("/catNameCheck")
    public ResponseResult catNameCheck(@RequestBody Map<String, Object> map) {
        ResponseResult result = goodsCatService.checkCatName(map);
        return result;

    }

    /**
     * 平台  查询平台所有积分分类
     *
     * @param pageHelper
     * @return
     */
    @PostMapping("/integral/queryGoodsAll")
    public ResponseResult queryIntegralGoodsCatsAll(@RequestBody PageHelper pageHelper) {
        return goodsCatService.queryIntegralGoodsCatsAll(pageHelper);
    }

    /**
     * 平台添加积分分类
     *
     * @param param
     * @return
     */
    @PostMapping("/add/integralCat")
    public ResponseResult addIntegralCat(@RequestBody PlatFormGoodsCatsDTO param) {
        return goodsCatService.addIntegralCat(param);
    }

    /**
     * 平台删除积分分类
     *
     * @param param
     * @return
     */
    @PostMapping("/delete/integralCat")
    public ResponseResult deleteIntegralCat(@RequestBody PlatFormGoodsCatsDTO param) {
        return goodsCatService.deleteIntegralCat(param);
    }


    /**
     * 平台删除分类
     *
     * @param param
     * @return
     */
    @GlobalTransactional
    @PostMapping("/delete")
    public ResponseResult deleteGoodsCat(@RequestBody PlatFormGoodsCatsDTO param) {
        ResponseResult result = goodsCatService.deleteGoodsCat(param);
        return result;
    }


    /**
     * 平台添加分类
     *
     * @param param
     * @return
     */
    @SensitiveCheck(field = {"catName","goodsAttriParam.attributeContent","goodsAttriParam.commodityAttribute"})
    @PostMapping("/add")
    public ResponseResult addGoodsCat(@RequestBody PlatFormGoodsCatsDTO param) {
        ResponseResult result = goodsCatService.addGoodsCat(param);
        return result;
    }

    /**
     * 平台  查询平台所有分类
     *
     * @param pageHelper
     * @return
     */
    @PostMapping("/queryGoodsAll")
    public ResponseResult queryGoodsCatsAll(@RequestBody PageHelper pageHelper) {
        ResponseResult result = goodsCatService.queryGoodsCatsAll(pageHelper);
        return result;
    }

    /**
     * 查询改商品分类下是否可以添加虚拟商品
     * @param param
     * @return
     */
    @PostMapping("/itemcat/queryVirtual")
    public ResponseResult queryVirtual(@RequestBody ManageCatResultVO param) {
        ResponseResult result = goodsCatService.queryVirtual(param);
        return result;
    }


    /**
     * h5 首页查询所有一级分类
     */
    @PostMapping("/h5/queryRankCat")
    public ResponseResult queryRankCat() {
        ResponseResult result = goodsCatService.queryRankCat();
        return result;
    }


    /**
     * 查询店铺未选择的经营分类
     */
    @PostMapping("/queryNoCat")
    public ResponseResult queryNoCat(@RequestBody Map<String,Object> param) {
        ResponseResult result = goodsCatService.queryNoCat(param);
        return result;
    }

    /**
     * 申请经营分类
     */
    @PostMapping("/applyCat")
    public ResponseResult applyCat(@RequestBody Map<String,Object> param) {
        ResponseResult result = goodsCatService.applyCat(param);
        return result;
    }

    /**
     * 查询商家申请经营分类列表
     */
    @PostMapping("/qureyApplyCat")
    public ResponseResult qureyApplyCat(@RequestBody Map<String,Object> param) {
        ResponseResult result = goodsCatService.qureyApplyCat(param);
        return result;
    }


    /**
     * 查询商品分类
     */
    @PostMapping("/query/cat")
    public ResponseResult queryCat(@RequestBody Map<String,Object> param){
        ResponseResult result = goodsCatService.queryCat(param);
        return result;
    }


    /**
     * 查询分类（排除积分分类）
     * @param map
     * @return
     */
    @PostMapping("/queryGoodsCat/ByCatId")
    public ResponseResult queryGoodsCatByCatId(@RequestBody Map<String, Object> map){
        ResponseResult result = goodsCatService.queryGoodsCatByCatId(map);
        return result;
    }

    @GetMapping("/fx/queryCatList")
    ResponseResult queryFxCatList(@RequestParam("catId") Integer catId){
        return goodsCatService.queryFxCatList(catId);
    }

    /**
     * 根据分类id查询该分类下所有下级分类id
     * @param catId 分类id
     * @return 所有下级分类id
     */
    @GetMapping("/queryAllSubordinateCatIdByCatId")
    public List<Integer> queryAllSubordinateCatIdByCatId(@RequestParam("catId") Integer catId){
        return goodsCatService.queryAllSubordinateCatIdByCatId(catId);
    }

    /**
     * 根据分类id查询该分类下所有上级分类id
     * @param catId 分类id
     * @return 所有上级分类id
     */
    @GetMapping("/selectNavSaleCategories")
    public List<GoodsCatResultVO> selectNavSaleCategories(@RequestParam("catId") Integer catId,
                                                          @RequestParam("integralGoods") Integer integralGoods){
        return goodsCatService.selectNavSaleCategories(catId,integralGoods);
    }

    /**
     * 根据分类id集合查询上级分类集合
     * @param catIdList 分类id集合
     * @return 上级分类集合
     */
    @GetMapping("/selectLineSaleCategoriesByThreeCat")
    public List<GoodsCatResultVO> selectLineSaleCategoriesByThreeCat(@RequestParam("catIdList") List<Integer> catIdList){
        return goodsCatService.selectLineSaleCategoriesByThreeCat(catIdList);
    }

    /**
     * 根据分类id集合查询该分类下所有下级分类id
     * @param catId 分类id
     * @return 所有下级分类id
     */
    @GetMapping("/selectLineSaleCategoriesByCatId")
    List<GoodsCatResultVO> selectLineSaleCategoriesByCatId(@RequestParam("catId") Integer catId){
        return goodsCatService.selectLineSaleCategoriesByCatId(catId);
    }

    /**
     * 根据入住分类查询商品分类
     *
     * @param param
     * @return
     */
    @PostMapping("/item/cat/supplierQueryByJoin")
    public ResponseResult supplierQueryByJoin(@RequestBody ManageItemCatQueryDTO param) {
        return goodsCommonService.supplierQueryByJoin(param);
    }

    /**
     * 通过参数查询
     *
     * @param param
     * @return
     */
    @PostMapping("/selectByParam")
    public ResponseResult selectByParam(@RequestBody WebGoodsCatQueryDTO param) {
        return goodsCatService.selectByParam(param);
    }

    /**
     * 查询在前台显示的类目
     *
     * @param catId
     * @return
     */
    @GetMapping("/feign/selectByCatId2")
    public ManageShopCatMenuVO selectByCatId2(@RequestParam("catId") Integer catId){
        ManageShopCatMenuVO vo=goodsCatService.selectByCatId2(catId);
        return  vo;
    }

    /**
     * 查询比例
     *
     * @param catId
     * @return
     */
    @GetMapping("/feign/selectCommissionRate")
    public CommissionRateBeanVO selectCommissionRate(@RequestParam("catId") Integer catId){
        CommissionRateBeanVO vo=goodsCatService.selectCommissionRate(catId);
        return vo;
    }

    /**
     * 查询所有分类
     * @return
     */
    @PostMapping("/feign/selectAllCat3")
    public List<ShopGoodsCatVO> selectAllCat3(){
        List<ShopGoodsCatVO> list=goodsCatService.selectAllCat3();
        return list;
    }

    /**
     * 根据分类id查询分类
     * @param catId
     * @return
     */
    @GetMapping("/feign/selectGoodsCatByPrimaryKey")
    public GdbMerchantGoodsCatVO selectGoodsCatByPrimaryKey(@RequestParam("catId") Integer catId){
        GdbMerchantGoodsCatVO vo=goodsCatService.selectGoodsCatByPrimaryKey(catId);
        return vo;
    }

    /**
     * 根据分类id查询分类
     * @param catId
     * @return
     */
    @GetMapping("/feign/queryCatName")
    public List<String> queryCatName(@RequestParam("catId") Integer catId){
        List<String> list=goodsCatService.queryCatName(catId);
        return list;
    }

    /**
     * 根据类别ID查询类别名称
     *
     * @param catId 类别ID
     * @return 类别名称字符串
     */
    @GetMapping("/feign/selectByCatNameString")
    public String selectByCatNameString(@RequestParam("catId") Integer catId){
        String catName=goodsCatService.selectByCatNameString(catId);
        return catName;
    }

    /**
     * 查询所有子分类
     * @return
     */
    @PostMapping("/feign/selectAllCat2")
    public List<WebCatMenuVO> selectAllCat2(){
        List<WebCatMenuVO> list=goodsCatService.selectAllCat2();
        return list;
    }

    /**
     * 查询分类
     * @param catId
     * @return
     */
    @GetMapping("/feign/selectByCatParentId")
    public List<SecordNameListVO> selectByCatParentId(@RequestParam("catParentId")Integer catId){
        List<SecordNameListVO> list=goodsCatService.selectByCatParentId(catId);
        return list;
    }

}
