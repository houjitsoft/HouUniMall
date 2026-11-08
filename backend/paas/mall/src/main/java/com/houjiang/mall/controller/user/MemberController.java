package com.houjiang.mall.controller.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.entity.user.Member;
import com.houjiang.mall.service.user.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 会员管理 Controller
 * 路径：/mall/user/member/*
 * 
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/mall/user/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /**
     * 获取会员列表（分页）
     * POST /mall/user/member/list
     * 
     * @param params 查询参数
     * @return 会员列表分页数据
     */
    @PostMapping("/list")
    public ResponseResult<IPage<Member>> getMemberList(@RequestBody Map<String, Object> params) {
        try {
            log.info("查询会员列表，参数：{}", params);
            IPage<Member> page = memberService.pageList(params);
            return ResponseResult.success(page);
        } catch (Exception e) {
            log.error("查询会员列表失败", e);
            return ResponseResult.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取会员详情
     * POST /mall/user/member/detail
     * 
     * @param params 查询参数（包含 userId）
     * @return 会员详情
     */
    @PostMapping("/detail")
    public ResponseResult<Member> getMemberDetail(@RequestBody Map<String, Object> params) {
        try {
            log.info("查询会员详情，参数：{}", params);
            
            if (!params.containsKey("userId")) {
                return ResponseResult.error("缺少用户ID 参数");
            }
            
            Long userId = Long.parseLong(params.get("userId").toString());
            Member member = memberService.getMemberDetail(userId);
            
            if (member == null) {
                return ResponseResult.error("会员不存在");
            }
            
            return ResponseResult.success(member);
        } catch (Exception e) {
            log.error("查询会员详情失败", e);
            return ResponseResult.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 更新会员等级
     * POST /mall/user/member/updateLevel
     * 
     * @param params 参数（包含 userId 和 gradeId）
     * @return 是否成功
     */
    @PostMapping("/updateLevel")
    public ResponseResult<Boolean> updateMemberLevel(@RequestBody Map<String, Object> params) {
        try {
            log.info("更新会员等级，参数：{}", params);
            
            if (!params.containsKey("userId")) {
                return ResponseResult.error("缺少用户ID 参数");
            }
            if (!params.containsKey("gradeId")) {
                return ResponseResult.error("缺少等级ID 参数");
            }
            
            boolean result = memberService.updateMemberLevel(params);
            return ResponseResult.success(result);
        } catch (Exception e) {
            log.error("更新会员等级失败", e);
            return ResponseResult.error("更新失败：" + e.getMessage());
        }
    }
}
