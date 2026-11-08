package com.houjiang.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.goods.dto.UserFootprintDTO;
import com.houjiang.goods.dto.WapUserFootPrintAddParamDTO;
import com.houjiang.goods.dto.WapUserFootPrintDTO;
import com.houjiang.goods.entity.GoodsUserFootprintEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @ClassName GoodsUserFootprintMapper
 * @Description 商品访问足迹表
 * @Author Amos
 * @Date 2023-05-25 16:03:04
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface GoodsUserFootprintMapper extends BaseMapper<GoodsUserFootprintEntity> {
    /**
     * 查询商品是否已添加到足迹
     * @param userId
     * @param commonId
     * @return
     */
    UserFootprintDTO selectByUserId(@Param("userId") String userId, @Param("commonId") Integer commonId);

    /**
     * 添加足迹
     * @param userFoot
     */
    void addUserFoot(@Param("param") WapUserFootPrintAddParamDTO userFoot);

    /**
     * 更新足迹
     * @param selectByUserId
     */
    void updateUserFoot(@Param("param") UserFootprintDTO selectByUserId);

    /**
     * 查询用户足迹商品 的所属分类
     * @param limitSize
     * @param userId
     * @return
     */
    List<Integer> selectCartGuessLikeGoods(@Param("limitSize")Integer limitSize,@Param("userId")String userId);


    /**
     * 查询用户
     *
     * @param param
     * @return
     */
    List<Integer> selectUserFoot(@Param("param") WapUserFootPrintDTO param);


    /**
     * 查询指定店铺商品
     *
     * @param catIds
     * @return
     */
    List<Map<String, Object>> queryCatRandomGoods(@Param("catIds") List<Integer> catIds,@Param("shopId")Integer shopId);

    /**
     * 查询商品
     *
     * @param catIds
     * @return
     */
    List<Map<String, Object>> queryCatRandomGoods1(@Param("catIds") List<Integer> catIds);
}
