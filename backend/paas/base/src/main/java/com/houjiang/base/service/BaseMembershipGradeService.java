package com.houjiang.base.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.base.entity.BaseMembershipGradeEntity;
import com.houjiang.commons.dto.base.UpMemberClassDTO;
import com.houjiang.commons.util.ResponseResult;

import java.util.Map;

/**
 * @ClassName BaseMembershipGradeService
 * @Description
 * @Author Amos
 * @Date 2023-05-22 16:51:47
 * @Version 1.0
 * @author Amos
 */
public interface BaseMembershipGradeService extends IService<BaseMembershipGradeEntity> {
    /**
     * 根据会员等级id查询会员等级
     * @param id
     * @return
     */
    ResponseResult getMemberNameByClassId(String id);

    /**
     * 根据用户id查询会员等级名称
     * @param userId
     * @return
     */
    Map<String, Object> queryMemberShipGradeByUserId(String userId);

    /**
     * 查询会员积分最大值
     * @return
     */
    Integer getMaxMemberIntegral();

    /**
     * 查询会员用户距离下一级积分
     * @param userId
     * @return
     */
    ResponseResult getMemberIntegralDistance(String userId);


    /**
     *查询最低会员折扣
/**
     * @return
     */
    ResponseResult selectdiscount();

    /**
     * 删除会员等级
     * @param id
     * @return
     */

    /**
     * TODO 补充方法说明
     * @param id TODO
     * @return TODO
     */
    ResponseResult MembershipGradedelete(String id);

    /**
     * 添加 或 修改会员等级
     * @param map
     * @return
     */
    ResponseResult MembershipGradeAddOrUpdate(Map<String, String> map);


    /**
     *根据会员等级id更新会员等级
/**
     * @param dto
     * @return
     */
    ResponseResult upMemberClassDto(UpMemberClassDTO dto);

    /**
     * 查询所有会员等级
     *
     * @param map
     * @return
     */
    ResponseResult queryMembershipGrade(Map<String, Object> map);


    /**
     * 查询更新会员等级
     *
     * @param map
     * @return
     */
    ResponseResult queryMembershipGradeByGradeRule(Map<String, Object> map);
}
