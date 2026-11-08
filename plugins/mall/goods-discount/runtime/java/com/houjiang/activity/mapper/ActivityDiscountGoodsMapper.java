package com.houjiang.activity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.activity.dto.DiscountBaseDetailQueryDTO;
import com.houjiang.activity.dto.DiscountGoodsAddParamDTO;
import com.houjiang.activity.entity.ActivityDiscountGoodsEntity;
import com.houjiang.activity.vo.CheckDiscountLimitVO;
import com.houjiang.activity.vo.DiscountGoodsListQueryResultVO;
import com.houjiang.commons.dto.goods.DiscountGoodsListVO;
import com.houjiang.commons.vo.activity.AppDiscountGoodsQueryResultVO;
import com.houjiang.commons.vo.activity.CheckDiscountLimitResultVO;
import com.houjiang.commons.vo.activity.ModularGoodsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @ClassName ActivityDiscountGoodsMapper
 * @Description 限时折扣商品表
 * @Author Amos
 * @Date 2023-05-25 14:10:08
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface ActivityDiscountGoodsMapper extends BaseMapper<ActivityDiscountGoodsEntity> {

    /**
     * 查询限时折扣活动信息
     * @param goodsId
     * @return
     */
    AppDiscountGoodsQueryResultVO selectDiscountByGoodsId(@Param("goodsId") Integer goodsId);

    /**
     * 下单根据goodsid 查询活动详情
     * @param goodsId
     * @return
     */
    CheckDiscountLimitResultVO queryDiscountInfoByGoodsId(Integer goodsId);

    /**
     * 根据当前时间查询限时折扣活动信息 feign
     * @param goodsId
     * @return
     */
    AppDiscountGoodsQueryResultVO selectDiscountByGoodsIdAndTime(@Param("goodsId") Integer goodsId);

    /**
     * 查询限时折扣商品
     * @param param
     * @return
     */
    List<DiscountGoodsListQueryResultVO> queryDiscountLimitIndexGoods(@Param("param")Map<String, Object> param);

    /**
     * 查询限时折扣详情
     * @param commonId
     * @return
     */
    CheckDiscountLimitVO queryDiscountInfo(@Param("commonId") Integer commonId);

    /**
     * 查询商品的commonId
     * @param discountId
     * @return
     */
    List<Integer> selectDiscountGoods(@Param("discountId")Integer discountId);

    /**
     * 获取其他正在进行的活动
     * @param discountId
     * @param commonId
     * @return
     */
    List<Integer> selectActiveDiscountGoods(@Param("discountId")Integer discountId,@Param("commonId")Integer commonId);

    /**
     * 删除
     * @param discountId
     */
    void deleteDiscountGoods(@Param("discountId")Integer discountId);

    /**
     * 是否存在或重复
     * @param commonId
     * @return
     */
    Integer checkSpecNum(Integer commonId);

    /**
     * 插入限时折扣的商品
     * @param param
     */
    void insertDiscountGoods(@Param("param") DiscountGoodsAddParamDTO param);

    /**
     * 修改限时折扣的商品
     * @param param
     */
    void updateDiscountGoods(@Param("param")DiscountGoodsAddParamDTO param);

    /**
     * 当前活动的商品信息
     * @param param
     * @return
     */
    IPage<DiscountGoodsListVO> selectDiscountGoodsDetail(IPage<DiscountGoodsListVO> page , @Param("param") DiscountBaseDetailQueryDTO param);


    /**
     * 当前活动的商品总数
     * @param param
     * @return
     */
    Integer selectDiscountGoodsDetailCount(@Param("param")  DiscountBaseDetailQueryDTO param);

    /**
     * 删除限时折扣的商品
     * @param param
     */
    void deleteDiscountGoods2(@Param("param") DiscountBaseDetailQueryDTO param);

    /**
     * 更新限时折扣价格
     * @param param
     */
    void updateDiscountGoodsPrice(@Param("param")DiscountGoodsAddParamDTO param);

    /**
     * 查询活动规格的活动价格
     * @param param
     * @return
     */
    List<DiscountGoodsListVO> selectDiscountGoodsDetail2(@Param("param")DiscountBaseDetailQueryDTO param);

    /**
     * 查询活动商品价格
     */
    ModularGoodsVO selectPriceByGoodsId(@Param("goodsId")Integer goodsId);


    /**
     * 查询折扣商品信息，返回分页对象
     * 该方法用于处理分页查询逻辑，允许通过不同的参数组合来筛选查询结果
     *
     * @param page 分页对象，包含分页查询所需的信息，如当前页码、页面大小等
     * @param param 查询参数，一个键值对映射，可以包含任意查询条件
     * @return 返回填充了查询结果的分页对象
     */
    IPage<DiscountGoodsListVO> queryDiscountGoodsInfo(IPage<DiscountGoodsListVO> page , @Param("param") Map<String, Object> param);



    /**
     * 根据ID选择折扣商品的基础详情
     * 该方法用于获取特定折扣商品的基础详细信息，通过商品ID进行查询
     *
     * @param id 折扣商品的ID
     * @return 返回包含基础详情的ActivityDiscountGoodsEntity对象列表
     */
    List<ActivityDiscountGoodsEntity> selectDiscountBaseDetail(@Param("id")Integer id);

}

