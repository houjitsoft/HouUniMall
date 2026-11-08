package com.houjiang.mall.service.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.mall.entity.user.Member;
import com.houjiang.mall.mapper.user.MemberMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * 会员管理 Service
 * 
 * @author Amos
 */
@Service
public class MemberService extends ServiceImpl<MemberMapper, Member> {
    
    /**
     * 分页查询会员列表
     * @param params 查询参数
     * @return 分页结果
     */
    public IPage<Member> pageList(Map<String, Object> params) {
        Integer page = (Integer) params.getOrDefault("page", 1);
        Integer size = (Integer) params.getOrDefault("size", 10);
        String userMobile = (String) params.get("userMobile");
        String userNickname = (String) params.get("userNickname");
        Integer userState = params.get("userState") != null ? Integer.parseInt(params.get("userState").toString()) : null;
        
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        
        // 搜索手机号码
        wrapper.like(StringUtils.hasText(userMobile), Member::getUserMobile, userMobile);
        
        // 搜索昵称
        wrapper.like(StringUtils.hasText(userNickname), Member::getUserNickname, userNickname);
        
        // 搜索状态
        wrapper.eq(userState != null, Member::getUserState, userState);
        
        // 默认只查询未删除的会员
        wrapper.eq(Member::getIsDelete, 0);
        
        // 按创建时间降序排序
        wrapper.orderByDesc(Member::getCreateTime);
        
        return this.page(new Page<>(page, size), wrapper);
    }
    
    /**
     * 根据 ID 查询会员详情
     * @param userId 用户ID
     * @return 会员实体
     */
    public Member getMemberDetail(Long userId) {
        Member member = this.getById(userId);
        if (member != null && member.getIsDelete() == 1) {
            // 已删除的会员返回 null
            return null;
        }
        return member;
    }
    
    /**
     * 更新会员等级
     * @param params 参数（包含 userId 和 gradeId）
     * @return 是否成功
     */
    public boolean updateMemberLevel(Map<String, Object> params) {
        Long userId = Long.parseLong(params.get("userId").toString());
        Long gradeId = Long.parseLong(params.get("gradeId").toString());
        Member member = this.getById(userId);
        if (member == null || member.getIsDelete() == 1) {
            return false;
        }
        member.setGradeId(gradeId);
        return this.updateById(member);
    }

    /**
     * 编辑会员信息
     * @param params 会员信息参数
     * @return 是否成功
     */
    public boolean editMember(Map<String, Object> params) {
        Long userId = Long.parseLong(params.get("userId").toString());
        Member member = this.getById(userId);
        
        if (member == null || member.getIsDelete() == 1) {
            return false;
        }
        
        // 更新允许的字段
        if (params.containsKey("userNickname")) {
            member.setUserNickname(params.get("userNickname").toString());
        }
        if (params.containsKey("userSignature")) {
            member.setUserSignature(params.get("userSignature").toString());
        }
        if (params.containsKey("communityBackground")) {
            member.setCommunityBackground(params.get("communityBackground").toString());
        }
        if (params.containsKey("userState")) {
            member.setUserState(Integer.parseInt(params.get("userState").toString()));
        }
        if (params.containsKey("gradeId")) {
            member.setGradeId(Long.parseLong(params.get("gradeId").toString()));
        }
        
        return this.updateById(member);
    }

}
