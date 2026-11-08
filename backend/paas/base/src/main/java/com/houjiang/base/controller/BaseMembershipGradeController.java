package com.houjiang.base.controller;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.houjiang.base.entity.BaseMembershipGradeEntity;
import com.houjiang.base.entity.BaseTagClassEntity;
import com.houjiang.commons.dto.base.UpMemberClassDTO;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @ClassName BaseMembershipGradeController
 * @Description 会员等级
 * @Author Amos
 * @Date 2023/5/18 17:51
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("base/membershipGrade")
public class BaseMembershipGradeController extends BaseController {


    /**
     * 根据会员等级id查询会员等级
     *
     * @param id
     * @return
     */
    @PostMapping("/getMemberNameByClassId")
    public ResponseResult getMemberNameByClassId(@RequestParam("id") String id) {
        ResponseResult result = baseMembershipGradeService.getMemberNameByClassId(id);
        return result;
    }

    /**
     * feign调用 根据用户id查询会员等级名称
     *
     * @param userId
     * @return
     */
    @PostMapping("/queryByUserId")
    public Map<String, Object> queryMemberShipGradeByUserId(@RequestParam("userId") String userId) {

        return baseMembershipGradeService.queryMemberShipGradeByUserId(userId);
    }

    /**
     * feign调用 查询会员积分最大值
     *
     * @param
     * @return
     */
    @PostMapping("/getMaxMemberIntegral")
    public Integer getMaxMemberIntegral() {

        return baseMembershipGradeService.getMaxMemberIntegral();
    }

    /**
     * feign调用 查询会员用户距离下一级积分
     *
     * @param userId
     * @return
     */
    @PostMapping("/getMemberIntegralDistance")
    ResponseResult getMemberIntegralDistance(@RequestParam("userId") String userId) {
        return baseMembershipGradeService.getMemberIntegralDistance(userId);
    }

    /**
     * 查询最低会员折扣
     *
     * @return
     */
    @PostMapping("/selectDiscount")
    public ResponseResult selectdiscount() {
        ResponseResult result = baseMembershipGradeService.selectdiscount();
        return result;
    }

    /**
     * 删除会员等级
     *
     * @param id
     * @return
     */
    @GetMapping("/delete")
    public ResponseResult MembershipGradedelete(@RequestParam("id") String id) {
        ResponseResult result = baseMembershipGradeService.MembershipGradedelete(id);
        return result;
    }

    /**
     * 添加 或 修改会员等级
     *
     * @param map
     * @return
     */
    @PostMapping("/addOrUpdate")
    public ResponseResult MembershipGradeAddOrUpdate(@RequestBody Map<String, String> map) {
        ResponseResult result = baseMembershipGradeService.MembershipGradeAddOrUpdate(map);
        return result;
    }

    /**
     * 根据会员等级id更新会员等级
     *
     * @param dto
     * @return
     */
    @PostMapping("/upMemberClassDto")
    public ResponseResult upMemberClassDto(@RequestBody UpMemberClassDTO dto) {

        ResponseResult result = baseMembershipGradeService.upMemberClassDto(dto);
        return result;
    }

    /**
     * 查询所有会员等级
     *
     * @param map
     * @return
     */
    @PostMapping("/queryAll")
    public ResponseResult queryMembershipGrade(@RequestBody Map<String, Object> map) {
        ResponseResult result = baseMembershipGradeService.queryMembershipGrade(map);
        return result;
    }


    /**
     * 查询更新会员等级
     *
     * @param map
     * @return
     */
    @PostMapping("/queryByGradeRule")
    public ResponseResult queryMembershipGradeByGradeRule(@RequestBody Map<String, Object> map) {
        ResponseResult result = baseMembershipGradeService.queryMembershipGradeByGradeRule(map);
        return result;
    }


    /**
     * 修改会员等级
     *
     * @param memberLevel
     * @param userId
     */
    @PostMapping("/updateMemberGrade")
    public void updateMemberGrade(@RequestParam("customerType") Integer customerType,
                                  @RequestParam("memberLevel") String memberLevel,
                                  @RequestParam("userId") String userId) {


        BaseMembershipGradeEntity grade = baseMembershipGradeService.getOne(new LambdaQueryWrapper<BaseMembershipGradeEntity>()
                .eq(BaseMembershipGradeEntity::getGradeName, memberLevel));

        if (BeanUtil.isEmpty(grade)) {
            return;
        }
        BaseTagClassEntity tagClass = baseTagClassService.getOne(new LambdaQueryWrapper<BaseTagClassEntity>()
                .eq(BaseTagClassEntity::getMemberId, userId));

        if (BeanUtil.isEmpty(tagClass)) {
            tagClass = new BaseTagClassEntity();
            tagClass.setMemberId(userId);
            tagClass.setMemberClassId(grade.getId());
            baseTagClassService.saveOrUpdate(tagClass);
        } else {
            tagClass.setMemberClassId(grade.getId());
            baseTagClassService.updateById(tagClass);
        }

    }
}
