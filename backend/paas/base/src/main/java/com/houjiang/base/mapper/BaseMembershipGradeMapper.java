package com.houjiang.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.base.entity.BaseMembershipGradeEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * @Entity com.houjiang.base.entity.BaseMembershipGrade
 * @author Amos
 */
@Mapper
public interface BaseMembershipGradeMapper extends BaseMapper<BaseMembershipGradeEntity> {
    /**
     * 查询等级信息
     * @param tagId
     * @return
     */
    Map<String, Object> getMemberNameByClassId(Integer tagId);

    /**
     * 查询会员积分最大值
     * @return
     */
    Integer getMaxMemberIntegral();

    /**
     * 用户到下级需要多少积分
     * @param presentInteger
     * @return
     */
    Map<String, Object> distanceClassIntegral(Integer presentInteger);


    /**
     * 新增会员等级
     * @param map
     */
    void MembershipGradeAdd(@Param("param") Map<String, String> map);

    /**
     * 修改会员等级
     * @param map
     */
    void MembershipGradeUpdate(@Param("param")  Map<String, String> map);

    /**
     * 查询平台最低会员折扣
     * @return
     */
    String selectmemberdiscount();

}




