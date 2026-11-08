package com.houjiang.activity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.activity.dto.PromotionIndexQueryParamDTO;
import com.houjiang.activity.entity.ActivitySeckillGoodsEntity;
import com.houjiang.activity.vo.CheckSeckillLimitVO;
import com.houjiang.activity.vo.SeckillGoodsListQueryResultVO;
import com.houjiang.activity.vo.ShopSeckillGoodsListVO;
import com.houjiang.commons.dto.goods.AppSeckillGoodsVO;
import com.houjiang.commons.vo.activity.AppSeckillGoodsQueryResultVO;
import com.houjiang.commons.vo.activity.CheckSeckillLimitResultVO;
import com.houjiang.commons.vo.activity.ModularGoodsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @ClassName ActivitySeckillGoodsMapper
 * @Description 限时秒杀商品表
 * @Author Amos
 * @Date 2023-05-25 13:58:28
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface ActivitySeckillGoodsMapper extends BaseMapper<ActivitySeckillGoodsEntity> {

    /**
     * 查询秒杀活动信息
     *
     * @param param
     * @return
     */
    AppSeckillGoodsQueryResultVO selectSeckillByGoodsId(@Param("param")Map<String, Object> param);

    /**
     * 查询秒杀活动信息
     *
     * @param goodsId
     * @return
     */
    AppSeckillGoodsQueryResultVO selectSeckillByGoodsId2(@Param("goodsId") Integer goodsId);

    /**
     * 查询秒杀商品信息
     * @param param
     * @return
     */
    CheckSeckillLimitVO querySeckillInfo(@Param("param")Map<String, Object> param);

    /**
     * 查询商品信息
     * @param param
     * @return
     */
    Integer queryOrderGoodsCount(@Param("param")Map<String, Object> param);

    /**
     * 下单根据goodsid 查询活动详情
     * @param goodsId
     * @return
     */
    CheckSeckillLimitResultVO querySeckillInfoByGoodsId(@Param("goodsId")Integer goodsId);

    /**
     * 查询秒杀商品库存
     * @param paramMap
     * @return
     */
    Integer querySeckillNum(@Param("param")Map<String, Object> paramMap);

    /**
     * 更新秒杀商品库存
     * @param paramMap
     * @return
     */
    Integer updateSaleForSeckill(@Param("param")Map<String, Object> paramMap);



    /**
     * 通过商品id查询
     *
     * @param goodsId
     * @return
     */
    AppSeckillGoodsVO selectByGoodsId(@Param("goodsId") Integer goodsId);

    /**
     * 查询秒杀首页 活动未开始
     * @param page
     * @param param
     * @return
     */
    IPage<SeckillGoodsListQueryResultVO> selectSeckillIndexGoods(IPage<SeckillGoodsListQueryResultVO> page, @Param("param") PromotionIndexQueryParamDTO param);

    /**
     * 查询秒杀首页 活动已开始
     * @param page
     * @param param
     * @return
     */
    IPage<SeckillGoodsListQueryResultVO> selectSeckillIndexGoods2(IPage<SeckillGoodsListQueryResultVO> page, @Param("param") PromotionIndexQueryParamDTO param);

    /**
     *查询活动商品价格
     */
    ModularGoodsVO seckillGoodsPrice(@Param("goodsId")Integer goodsId);


    /**
     * 查询秒杀相差30%以上得商品
     * @param param
     * @return
     */
    List<SeckillGoodsListQueryResultVO> querySeckillDiffer(@Param("param")PromotionIndexQueryParamDTO param);

    /**
     * 按商品销量查询
     * @param map
     * @return
     */
    List<Integer> querySeckillGoodsHot(@Param("param")Map map);

    /**
     * 查询秒杀商品信息
     * @param map
     * @return
     */
    List<SeckillGoodsListQueryResultVO> querySeckillDifferHot(@Param("param")Map map);

    /**
     * 查询秒杀商品信息
     * @param page
     * @return
     */
    IPage<AppSeckillGoodsQueryResultVO> querySeckillGoodsInfo(IPage<AppSeckillGoodsQueryResultVO> page , @Param("param") Map<String, Object> param);


    /**
     * 根据commonId查询秒杀的规格
     *
     * @param param
     * @return
     */
    List<AppSeckillGoodsQueryResultVO> selectSeckillGoodsDetail2(@Param("param")Map<String, Object> param);

    /**
     * 获取其他正在进行的活动
     * @param seckillId
     * @param commonId
     * @return
     */
    List<Integer> selectActiveSeckillGoods(@Param("seckillId")String seckillId,@Param("commonId")String commonId);


    /**
     * 删除描述商品
     * @param param
     */
    void deleteSeckillGoods2(@Param("param")Map<String, Object> param);

    /**
     * 判断商品是否已存在 存在就不添加
     * @param seckillId
     * @param goodsId
     * @return
     */
    Integer goodsIsRepetition(@Param("seckillId")Integer seckillId, @Param("goodsId")Integer goodsId);


    /**
     * 查询商品
     *
     * @param param
     * @return
     */
    IPage<ShopSeckillGoodsListVO> selectShopSeckillGoodsList(IPage<ShopSeckillGoodsListVO> page,@Param("param") Map<String, Object> param);


    /**
     * 查询某个商品（commonId）下的全部规格。
     *
     * <p>秒杀商品表以 goods_id（规格）为粒度落库，秒杀价也是规格价；
     * 后台配置时需要把该商品的所有规格列出来逐条填秒杀价，因此这里直接透出规格清单。
     *
     * @param commonId 商品（spu）ID
     * @return 规格行：goodsId / specName / goodsPrice / goodsStock
     */
    List<Map<String, Object>> selectCommonGoodsSpecs(@Param("commonId") Integer commonId);


}

