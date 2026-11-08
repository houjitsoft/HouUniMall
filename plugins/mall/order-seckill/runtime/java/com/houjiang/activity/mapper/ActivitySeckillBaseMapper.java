package com.houjiang.activity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.activity.dto.FinishTimeSecondkillDTO;
import com.houjiang.activity.entity.ActivitySeckillBaseEntity;
import com.houjiang.activity.vo.GdbSeckillBaseVO;
import com.houjiang.commons.dto.goods.ManageSeckillBaseUpdateDTO;
import com.houjiang.commons.dto.goods.SeckillGoodsListDTO;
import com.houjiang.commons.vo.activity.AppSeckillGoodsQueryResultVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @ClassName ActivitySeckillBaseMapper
 * @Description  限时秒杀活动表
 * @Author Amos
 * @Date 2023-07-04 17:26:45
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface ActivitySeckillBaseMapper extends BaseMapper<ActivitySeckillBaseEntity> {

    /**
     * 装修组件化 限时秒杀活动列表查询
     */
    IPage<Map<String,Object>> selectFinishTimeSecondkillPojo(IPage<Map<String,Object>> page, @Param("param") FinishTimeSecondkillDTO param);

    /**
     * 装修组件化 限时秒杀活动列表查询
     */
    IPage<Map<String,Object>> getByPage(IPage<Map<String,Object>> page, @Param("param") FinishTimeSecondkillDTO param);

    /**
     * 查询秒杀详情
     * @param seckillId
     * @return
     */
    Map<String, Object> querySecondKill(@Param("seckillId")Integer seckillId);

    /**
     * 选择店铺秒杀基础列表
     *
     * @param page 分页对象，用于封装分页信息和查询结果
     * @param param 查询参数，用于指定查询条件
     * @return 返回封装了秒杀活动基础信息的分页对象
     */
    IPage<ActivitySeckillBaseEntity> selectShopSeckillBaseList(IPage<ActivitySeckillBaseEntity> page , @Param("param") ManageSeckillBaseUpdateDTO param);

    /**
     * 根据秒杀ID分页选择秒杀商品信息
     *
     * @param page 分页对象，用于封装分页信息和查询结果
     * @param param 查询参数，包含秒杀ID等查询条件
     * @return 返回封装了秒杀商品详细信息的分页对象
     */
    IPage<AppSeckillGoodsQueryResultVO> selectBySeckillIdByPage(IPage<AppSeckillGoodsQueryResultVO> page , @Param("param") Map<String, Object> param);

    /**
     * 查询活动详情
     * @param seckillId
     * @return
     */
    GdbSeckillBaseVO selectByPrimaryKey(Integer seckillId);


    /**
     * 新增秒杀信息
     * @param param
     */
    void insertShopSeckillBase(@Param("param") ManageSeckillBaseUpdateDTO param);


    /**
     * 更新秒杀信息
     * @param param
     */
    void updateShopSeckillBase(@Param("param") ManageSeckillBaseUpdateDTO param);


    /**
     * 查询秒杀活动信息
     * @param seckillId
     * @return
     */
    ManageSeckillBaseUpdateDTO selectSeckilById(@Param("seckillId")Integer seckillId);

    /**
     * 查询秒杀商品信息
     * @param seckillId
     * @return
     */
    List<SeckillGoodsListDTO> selectSeckilGoodsById(@Param("seckillId")Integer seckillId);
}
