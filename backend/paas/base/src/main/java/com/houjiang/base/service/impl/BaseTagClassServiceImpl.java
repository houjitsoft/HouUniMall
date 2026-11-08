package com.houjiang.base.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.base.entity.BaseMembershipGradeEntity;
import com.houjiang.base.entity.BaseTagClassEntity;
import com.houjiang.base.mapper.BaseMembershipGradeMapper;
import com.houjiang.base.mapper.BaseTagClassMapper;
import com.houjiang.base.service.BaseMembershipGradeService;
import com.houjiang.base.service.BaseTagClassService;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author Amos
 */
@Service
public class BaseTagClassServiceImpl extends ServiceImpl<BaseTagClassMapper, BaseTagClassEntity>
    implements BaseTagClassService{

    @Autowired
    private BaseMembershipGradeService baseMembershipGradeService;

    @Autowired
    private BaseMembershipGradeMapper baseMembershipGradeMapper;
    /**
     * 根据用户ID获取会员班级ID。
     * @param userId 用户ID。
     * @return 返回包含会员班级ID的响应结果。
     */
    @Override
    public ResponseResult getMemberClassIdByUserId(String userId) {
        // 根据用户ID查询会员班级实体
        BaseTagClassEntity entity=this.baseMapper.selectOne(new LambdaQueryWrapper<BaseTagClassEntity>()
                                     .eq(BaseTagClassEntity::getMemberId,userId));
        // 返回查询结果的响应
        return ResponseResult.success(entity);
    }

    /**
     * 根据用户ID更新会员等级。
     * @param map 包含更新信息的映射对象，其中包含用户ID和等级相关信息。
     * @return 返回更新操作的响应结果。
     */
    @Override
    public ResponseResult updateTagClassByUserId(Map<String, Object> map) {
        QueryWrapper<BaseMembershipGradeEntity> queryWrapper = new QueryWrapper<>();

        // 如果map中包含gradeStart，添加小于等于条件查询
        if (null != map && null != map.get("gradeStart")) {
            queryWrapper.le("grade_start", map.get("gradeStart").toString());

        }

        // 如果map中包含userIntegral，添加小于等于条件查询，并按grade_rule降序排序，取第一条
        if (null != map && null != map.get("userIntegral")) {
            queryWrapper.le("grade_rule", map.get("userIntegral").toString());
            queryWrapper.orderByDesc("grade_rule");
            queryWrapper.last("limit 1");
        }

        // 根据查询条件获取会员等级信息
        //获取积分信息
        BaseMembershipGradeEntity membershipGradeEntity = baseMembershipGradeService.getOne(queryWrapper);

        // 更新会员班级信息，设置member_class_id为查询到的会员等级ID
        //根据用户id更新会员等级
        this.baseMapper.update(null,new LambdaUpdateWrapper<BaseTagClassEntity>()
                           .eq(BaseTagClassEntity::getMemberId,map.get("userId").toString())
                           .set(BaseTagClassEntity::getMemberClassId,membershipGradeEntity.getId()));

        // 返回操作成功的响应结果
        return ResponseResult.success();
    }

    /**
     * 保存会员班级信息。
     * @param map 包含保存信息的映射对象，其中包含用户ID和注册规则等相关信息。
     * @return 返回保存操作的响应结果。
     */
    @Override
    public ResponseResult saveTagClass(Map<String, Object> map) {
        // 创建新的会员班级实体
        BaseTagClassEntity tagClass = new BaseTagClassEntity();
        // 设置会员ID
        tagClass.setMemberId(map.get("userId").toString());

        // 查询所有等级起始为1的会员等级，并按grade_rule升序排列
        //查询所有的会员等级
        List<BaseMembershipGradeEntity> membershipGradesList =baseMembershipGradeService.list(new LambdaQueryWrapper<BaseMembershipGradeEntity>()
                    .eq(BaseMembershipGradeEntity::getGradeStart, 1)
                    // 排序的目的是高等级覆盖低等级
                    .orderByAsc(BaseMembershipGradeEntity::getGradeRule));

        // 遍历会员等级列表，根据注册规则判断应设置的会员班级ID
        //遍历所有的会员等级 判断用户注册所赠送的积分会使用户的等级升到几级
        Integer size = membershipGradesList.size() - 1;
        Integer ruleRegister = (Integer) map.get("ruleRegister");
        for (int i = size; i >0; i--) {
            Integer gradeRule = membershipGradesList.get(i - 1).getGradeRule();
            if ( ruleRegister >= gradeRule) {
                tagClass.setMemberClassId(membershipGradesList.get(i - 1).getId());
            }
        }
        // 插入新的会员班级信息
        //添加用户等级关联表
        int flag = this.baseMapper.insert(tagClass);

        // 根据插入操作的结果返回相应的响应结果
        return flag == CommonStatusEnum.SUCCESS.getCode() ? ResponseResult.success() : ResponseResult.fail();
    }

    /**
     * 根据用户ID查询会员等级名称
     *
     * @param userId 用户ID
     * @return 会员等级名称，如果找不到对应的会员等级或用户实体，则返回null
     */
    @Override
    public String queryMemberNameByUserId(String userId) {
        // 根据用户ID查询会员等级实体
        BaseTagClassEntity entity=this.baseMapper.selectOne(new LambdaQueryWrapper<BaseTagClassEntity>()
                .eq(BaseTagClassEntity::getMemberId, userId));

        if (null != entity) {
            // 根据会员等级id查询会员名称
            BaseMembershipGradeEntity membershipGradeEntity = baseMembershipGradeMapper.selectOne(new LambdaQueryWrapper<BaseMembershipGradeEntity>()
                    .eq(BaseMembershipGradeEntity::getId, entity.getMemberClassId()));
            return membershipGradeEntity == null ? null : membershipGradeEntity.getGradeName();
        } else {
            return null;
        }
    }
}




