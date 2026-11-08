package com.houjiang.base.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.base.entity.BaseIntegralRulesEntity;
import com.houjiang.base.entity.BaseMembershipGradeEntity;
import com.houjiang.base.entity.BaseTagClassEntity;
import com.houjiang.base.feign.UsermsClient;
import com.houjiang.base.mapper.BaseMembershipGradeMapper;
import com.houjiang.base.mapper.BaseTagClassMapper;
import com.houjiang.base.service.BaseIntegralRulesService;
import com.houjiang.base.service.BaseMembershipGradeService;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.base.UpMemberClassDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Amos
 */
@Service
public class BaseMembershipGradeServiceImpl extends ServiceImpl<BaseMembershipGradeMapper, BaseMembershipGradeEntity>
        implements BaseMembershipGradeService {

    @Autowired
    private BaseTagClassMapper baseTagClassMapper;

    @Autowired
    private UsermsClient usermsClient;

    @Autowired
    private BaseIntegralRulesService baseIntegralRulesService;

    /**
     * 根据班级ID获取成员名称。
     *
     * @param id 班级ID。
     * @return 成员名称的响应结果。
     */
    @Override
    public ResponseResult getMemberNameByClassId(String id) {
        BaseMembershipGradeEntity entity = this.baseMapper.selectById(id);
        return ResponseResult.success(entity);
    }

    /**
     * 根据用户ID查询会员等级信息。
     *
     * @param userId 用户ID。
     * @return 会员等级信息的映射集合。
     */
    @Override
    public Map<String, Object> queryMemberShipGradeByUserId(String userId) {
        Integer tagId = baseTagClassMapper.getMemberClassIdByUserId(userId);
        Map<String, Object> memberName = this.baseMapper.getMemberNameByClassId(tagId);
        return memberName;
    }

    /**
     * 获取最大会员积分。
     *
     * @return 最大会员积分。
     */
    @Override
    public Integer getMaxMemberIntegral() {
        return this.baseMapper.getMaxMemberIntegral();
    }

    /**
     * 根据用户ID获取会员积分距离。
     *
     * @param userId 用户ID。
     * @return 会员积分距离的响应结果。
     */
    @Override
    public ResponseResult getMemberIntegralDistance(String userId) {
        Integer presentInteger = usermsClient.queryMemberMessageListIntegral(userId);
        Map<String, Object> stringStringMap = this.baseMapper.distanceClassIntegral(presentInteger);
        return ResponseResult.success(stringStringMap);
    }

    /**
     * 查询最低会员折扣。
     *
     * @return 最低会员折扣的响应结果。
     */
    @Override
    public ResponseResult selectdiscount() {
        BigDecimal gradeDiscount = this.baseMapper.selectOne(new LambdaQueryWrapper<BaseMembershipGradeEntity>()
                .eq(BaseMembershipGradeEntity::getGradeStart, CommonType.commonStatus.YES.getCode())
                .orderByAsc(BaseMembershipGradeEntity::getGradeDiscount)
                .last("LIMIT 1")).getGradeDiscount();
        return ResponseResult.success(gradeDiscount);
    }

    /**
     * 删除会员等级。
     *
     * @param id 会员等级ID。
     * @return 删除操作的响应结果。
     */
    @Override
    public ResponseResult MembershipGradedelete(String id) {
        if (StringUtils.isNotEmpty(id)) {
            List<String> memberIds = baseTagClassMapper.queryByMemberClassId(Integer.valueOf(id));
            if (null != memberIds && memberIds.size() > 0) {
                return ResponseResult.fail(BizCodeEnume.MEMBER_GRADE_DELETE);
            }
            this.baseMapper.delete(new LambdaQueryWrapper<BaseMembershipGradeEntity>()
                    .in(BaseMembershipGradeEntity::getId, id));
        }
        return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
    }

    /**
     * 添加或更新会员等级
     *
     * @param map 包含会员等级信息的Map
     * @return 添加或更新结果的ResponseResult对象
     */
    @Override
    public ResponseResult MembershipGradeAddOrUpdate(Map<String, String> map) {
        try {
            // 检查是否提供了等级折扣信息
            if (ObjectUtils.isEmpty(map.get("gradeDiscount"))) {
                return ResponseResult.fail(BizCodeEnume.MISSING_PARAMETER);
            }

            // 判断折扣是否在0到1之间
            //判断折扣大于0 或者小于等于1
            if (new BigDecimal(map.get("gradeDiscount")).compareTo(new BigDecimal("0")) > 0 &&
                    new BigDecimal(map.get("gradeDiscount")).compareTo(new BigDecimal("1")) <= 0) {
                // 如果id为空，则添加会员等级；否则更新会员等级
                if (null == map.get("id")) {
                    this.baseMapper.MembershipGradeAdd(map);
                } else {
                    if ("1".equals(map.get("id"))) {
                        ResponseResult responseResult = baseIntegralRulesService.queryIntegralRules();
                        BaseIntegralRulesEntity baseIntegralRules = (BaseIntegralRulesEntity) responseResult.getData();
                        //注册会员的最大积分,注册积分
                        Integer interal = baseIntegralRules.getRuleRegister();
                        if (Integer.parseInt(map.get("gradeRule")) > interal) {
                            return ResponseResult.fail(BizCodeEnume.INTEGRAL_NOT_REGISTERED);
                        }
                    }
                    this.baseMapper.MembershipGradeUpdate(map);
                }
            } else {
                return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
    }

    /**
     * 更新会员等级信息
     *
     * @param dto 包含更新信息的DTO对象
     * @return 更新结果的ResponseResult对象
     */
    @Override
    public ResponseResult upMemberClassDto(UpMemberClassDTO dto) {
        Integer gradeRule = 0;
        // 判断入参是否为空
        if (dto == null) {
            return ResponseResult.fail(BizCodeEnume.OPERATION_FAILED);
        }
        // 根据会员等级ID查询最低积分规则
        //根据会员等级id查询最低积分
        List<BaseMembershipGradeEntity> membershipGrades = this.baseMapper.selectList(new LambdaUpdateWrapper<BaseMembershipGradeEntity>()
                .set(BaseMembershipGradeEntity::getGradeStart, CommonType.commonStatus.YES.getCode())
                .eq(BaseMembershipGradeEntity::getId, dto.getMemberClassId())
                .orderByAsc(BaseMembershipGradeEntity::getGradeRule));
        if (membershipGrades.size() != 0) {
            BaseMembershipGradeEntity membershipGrade1 = membershipGrades.get(0);
            gradeRule = membershipGrade1.getGradeRule();
        } else {
            return ResponseResult.fail(BizCodeEnume.OPERATION_FAILED);
        }
        // 遍历会员ID数组，更新会员积分和标签类关联的会员等级ID
        //遍历入参的id数组
        String[] memberArray = dto.getMemberArray();
        for (String memberId : memberArray) {
            Map<String, Object> map = new HashMap<>();
            map.put("userId", memberId);
            map.put("userIntegral", gradeRule);
            // 更新会员积分
            if (usermsClient.updateUser(map).getCode() == BizCodeEnume.OPERATION_FAILED.getCode()) {
                return ResponseResult.fail(BizCodeEnume.OPERATION_FAILED);
            }
            // 更新标签类关联的会员等级ID
            baseTagClassMapper.update(null, new LambdaUpdateWrapper<BaseTagClassEntity>()
                    .set(BaseTagClassEntity::getMemberClassId, dto.getMemberClassId())
                    .eq(BaseTagClassEntity::getMemberId, memberId));
        }

        return ResponseResult.fail(BizCodeEnume.OPERATION_SUCCESSFUL);
    }

    /**
     * 查询所有会员等级
     *
     * @param map 查询条件的Map
     * @return 会员等级列表的ResponseResult对象
     */
    @Override
    public ResponseResult queryMembershipGrade(Map<String, Object> map) {
        List<BaseMembershipGradeEntity> list = this.baseMapper.selectList(new LambdaQueryWrapper<BaseMembershipGradeEntity>()
                .eq(BaseMembershipGradeEntity::getGradeStart, CommonType.commonStatus.YES.getCode())
                .orderByAsc(BaseMembershipGradeEntity::getGradeRule));
        return ResponseResult.success(list);
    }

    /**
     * 根据积分规则查询会员等级并更新用户会员等级
     *
     * @param map 包含查询条件和用户ID的Map
     * @return 更新结果的ResponseResult对象
     */
    @Override
    public ResponseResult queryMembershipGradeByGradeRule(Map<String, Object> map) {
        QueryWrapper<BaseMembershipGradeEntity> queryWrapper = new QueryWrapper<>();

        // 根据条件构造查询Wrapper
        if (null != map && null != map.get("gradeStart")) {
            Object gradeStartObj = map.get("gradeStart");
            if (gradeStartObj != null) {
                queryWrapper.le("grade_start", gradeStartObj.toString());
            }
        }

        if (null != map && null != map.get("userIntegral")) {
            Object userIntegralObj = map.get("userIntegral");
            if (userIntegralObj != null) {
                queryWrapper.le("grade_rule", userIntegralObj.toString());
                queryWrapper.orderByDesc("grade_rule");
                queryWrapper.last("limit 1");
            }
        }

        // 获取满足条件的最高会员等级
        //获取积分信息
        BaseMembershipGradeEntity membershipGradeEntity = this.baseMapper.selectOne(queryWrapper);
        if (null != membershipGradeEntity) {
            // 根据用户ID更新用户会员等级
            //根据用户id更新会员等级
            Object userIdObj = map.get("userId");
            if (userIdObj != null) {
                baseTagClassMapper.update(null, new LambdaUpdateWrapper<BaseTagClassEntity>()
                        .eq(BaseTagClassEntity::getMemberId, userIdObj.toString())
                        .set(BaseTagClassEntity::getMemberClassId, membershipGradeEntity.getId()));
            }
        }
        return ResponseResult.success();
    }
}




