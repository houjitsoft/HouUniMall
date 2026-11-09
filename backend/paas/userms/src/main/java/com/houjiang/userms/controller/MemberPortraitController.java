package com.houjiang.userms.controller;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.userms.dto.MemberPortraitQueryDTO;
import com.houjiang.userms.dto.PortraitCrowdDTO;
import com.houjiang.userms.dto.PortraitSegmentDTO;
import com.houjiang.userms.dto.PortraitTagDTO;
import com.houjiang.userms.service.MemberPortraitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 会员画像（真实数据）
 *
 * <p>数据全部来自库内：会员主表登录信息、订单表、商品足迹、运营手工标签。
 * 不做模拟、不做补数；缺失维度由接口的 dataQuality / hasXxxData 如实标记。</p>
 *
 * @author Amos
 */
@RestController
@RequestMapping("user/ms/portrait")
public class MemberPortraitController {

    @Autowired
    private MemberPortraitService memberPortraitService;

    /**
     * 会员画像分页（含汇总与数据完整度）
     */
    @PostMapping("/page")
    public ResponseResult page(@RequestBody(required = false) MemberPortraitQueryDTO dto) {
        return memberPortraitService.queryPortraitPage(dto);
    }

    /**
     * 单个会员画像详情
     *
     * @param body userId（会员ID）
     */
    @PostMapping("/detail")
    public ResponseResult detail(@RequestBody Map<String, Object> body) {
        if (body == null || body.get("userId") == null) {
            return ResponseResult.fail();
        }
        try {
            Long userId = Long.valueOf(String.valueOf(body.get("userId")));
            return memberPortraitService.queryPortraitDetail(userId);
        } catch (NumberFormatException e) {
            return ResponseResult.fail();
        }
    }

    /**
     * RFM 八分群矩阵（含每群成员与运营动作）
     */
    @PostMapping("/rfm")
    public ResponseResult rfm() {
        return memberPortraitService.queryRfmMatrix();
    }

    /**
     * 智能分群：按条件实时圈人，返回规模与名单
     */
    @PostMapping("/segment/preview")
    public ResponseResult segmentPreview(@RequestBody(required = false) PortraitSegmentDTO dto) {
        return memberPortraitService.previewSegment(dto);
    }

    /**
     * 保存人群包
     */
    @PostMapping("/crowd/save")
    public ResponseResult crowdSave(@RequestBody PortraitCrowdDTO dto) {
        return memberPortraitService.saveCrowd(dto);
    }

    /**
     * 人群包列表
     */
    @PostMapping("/crowd/list")
    public ResponseResult crowdList() {
        return memberPortraitService.queryCrowdList();
    }

    /**
     * 删除人群包
     *
     * @param body {"id": 1}
     */
    @PostMapping("/crowd/delete")
    public ResponseResult crowdDelete(@RequestBody Map<String, Object> body) {
        if (body == null || body.get("id") == null) {
            return ResponseResult.fail();
        }
        try {
            return memberPortraitService.deleteCrowd(Long.valueOf(String.valueOf(body.get("id"))));
        } catch (NumberFormatException e) {
            return ResponseResult.fail();
        }
    }

    /**
     * 标签字典（含覆盖人数）
     */
    @PostMapping("/tag/list")
    public ResponseResult tagList() {
        return memberPortraitService.queryTagList();
    }

    /**
     * 新建 / 更新标签
     */
    @PostMapping("/tag/save")
    public ResponseResult tagSave(@RequestBody PortraitTagDTO dto) {
        return memberPortraitService.saveTag(dto);
    }

    /**
     * 删除标签（置为停用）
     *
     * @param body {"tagId": 1}
     */
    @PostMapping("/tag/delete")
    public ResponseResult tagDelete(@RequestBody Map<String, Object> body) {
        if (body == null || body.get("tagId") == null) {
            return ResponseResult.fail();
        }
        try {
            return memberPortraitService.deleteTag(Integer.valueOf(String.valueOf(body.get("tagId"))));
        } catch (NumberFormatException e) {
            return ResponseResult.fail();
        }
    }

    /**
     * 批量打标
     */
    @PostMapping("/tag/attach")
    public ResponseResult tagAttach(@RequestBody PortraitTagDTO dto) {
        return memberPortraitService.attachTag(dto);
    }

    /**
     * 批量取消打标
     */
    @PostMapping("/tag/detach")
    public ResponseResult tagDetach(@RequestBody PortraitTagDTO dto) {
        return memberPortraitService.detachTag(dto);
    }
}
