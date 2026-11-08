package com.houjiang.activity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.activity.dto.DiscountBaseAddParamDTO;
import com.houjiang.activity.dto.DiscountBaseDetailQueryDTO;
import com.houjiang.activity.dto.DiscountBaseListQueryDTO;
import com.houjiang.activity.dto.FinishBaseListQueryDTO;
import com.houjiang.activity.entity.ActivityDiscountBaseEntity;
import com.houjiang.activity.vo.CheckDiscountLimitVO;
import com.houjiang.activity.vo.DiscountBaseDetailResultVO;
import com.houjiang.activity.vo.DiscountBaseDetailVO;
import com.houjiang.activity.vo.DiscountBaseListVO;
import com.houjiang.commons.dto.goods.DiscountGoodsListVO;
import com.houjiang.commons.dto.goods.ListPageQueryParamDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 限时折扣活动表Mapper接口
 *
 * @author Amos
 * @date 2026-01-04
 */
@Mapper
public interface ActivityDiscountBaseMapper extends BaseMapper<ActivityDiscountBaseEntity> {

    /**
     * 查询商品的限时折扣限购信息
     */
    CheckDiscountLimitVO queryDiscountInfo(@Param("goodsId") Integer goodsId);

    /**
     * 查询所有折扣商品列表（分页）
     */
    IPage<Map<String, Object>> queryAllZheKou(IPage<Map<String, Object>> page, @Param("param") ListPageQueryParamDTO param);

    /**
     * 平台分页查询限时折扣列表
     */
    IPage<DiscountBaseListVO> queryPlatformDiscountBaseList(IPage<DiscountBaseListVO> page, @Param("param") DiscountBaseListQueryDTO param);

    /**
     * 查询限时折扣列表（手动分页）
     */
    List<DiscountBaseListVO> selectDiscountBaseList(@Param("param") DiscountBaseListQueryDTO param);

    /**
     * 查询限时折扣总数
     */
    Integer selectDiscountBaseListCount(@Param("param") DiscountBaseListQueryDTO param);

    /**
     * 查询限时折扣详情
     */
    DiscountBaseDetailVO selectDiscountBaseDetail(@Param("discountId") Integer discountId);

    /**
     * 查询限时折扣详情V2
     */
    DiscountBaseDetailResultVO selectDiscountBaseDetail2(@Param("discountId") Integer discountId);

    /**
     * 查询折扣关联商品详情（分页）
     */
    IPage<DiscountGoodsListVO> selectDiscountGoodsDetail(IPage<DiscountGoodsListVO> page, @Param("param") Map<String, Object> param);

    /**
     * 新增限时折扣基础信息
     */
    void insertDiscountBase(@Param("param") DiscountBaseAddParamDTO param);

    /**
     * 更新限时折扣基础信息
     */
    void updateDiscountBase(@Param("param") DiscountBaseAddParamDTO param);

    /**
     * 查询已完成的折扣活动列表
     */
    IPage<Map<String, Object>> selectFinishBaseList(IPage<Map<String, Object>> page, @Param("param") FinishBaseListQueryDTO param);

    /**
     * 删除限时折扣活动
     */
    void deleteByPrimaryKey(@Param("discountId") Integer discountId);

    /**
     * 查询折扣基础信息（用于拼团服务调用）
     */
    Map<String, Object> queryDiscountBase(@Param("discountId") Integer discountId);
}
