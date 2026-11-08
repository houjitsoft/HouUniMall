package com.houjiang.job.mapper;

import com.houjiang.commons.dto.goods.AppSeckillGoodsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @ClassName SeckillGoodsMapper
 * @Description
 * @Author Amos
 * @Date 2023/9/4 14:48
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface SeckillGoodsMapper {


    //查询秒杀库存为零的商品
    /**
     * TODO 补充方法说明
     * @return TODO
     */
    List<AppSeckillGoodsVO> selectSeckillNum();


    //查询秒杀活动商品
    /**
     * TODO 补充方法说明
     * @return TODO
     */
    List<AppSeckillGoodsVO> selectSeckillGoods(@Param("seckillId")Integer seckillId);


    // 秒杀商品过期状态更新
    /**
     * TODO 补充方法说明
     * @return TODO
     */
    Integer updateSkillGoods1(@Param("goodsId")Integer goodsId);

    // 商品状态更新
    /**
     * TODO 补充方法说明
     * @return TODO
     */
    Integer updateSkillGoodsStatus(@Param("commonId")Integer commonId);

    //秒杀商品过期状态更新
    /**
     * TODO 补充方法说明
     * @return TODO
     */
    Integer updateSkillGoods(@Param("seckillId")Integer seckillId);

    //秒杀过期状态更新
    /**
     * TODO 补充方法说明
     * @return TODO
     */
    Integer updateGoodsStatus(@Param("seckillId")Integer seckillId);
}
