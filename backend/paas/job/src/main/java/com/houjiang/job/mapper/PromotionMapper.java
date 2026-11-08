package com.houjiang.job.mapper;

import com.houjiang.job.dto.PromotionDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @ClassName PromotionMapper
 * @Description 晋升
 * @Author Amos
 * @Date 2023/5/17 9:54
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface PromotionMapper {

    /**
     * 获取会员等级删除了的用户
     *
     * @return
     */
    List<PromotionDTO> getPromotionByOrder();

    /**
     * 获取等级Id
     *
     * @param userIntegral
     * @return
     */
    Integer getGradeIdByUserIntegral(@Param("userIntegral") Integer userIntegral);

    /**
     * 更新等级
     *
     * @param userId
     * @param gradeId
     */
    void updateGradeId(@Param("gradeId") Integer gradeId, @Param("userId") String userId);
    /**
     * 根据用户ID获取用户的积分信息。
     *
     * 本方法通过查询订单信息来获取用户的积分情况。订单信息中记录了用户的积分变化，
     * 通过指定用户的ID，可以检索到该用户的所有订单，进而计算出用户的当前积分。
     *
     * @param userId 用户的唯一标识符。此参数用于在数据库中定位到特定用户的订单信息。
     * @return 返回用户的积分信息。返回值为Integer类型，表示用户的积分总数。
     */
    Integer getUserIntegralByOrder(@Param("userId") String userId);
}
