package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.UserServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 会员画像 BFF 适配器
 *
 * <p>只做请求转发，不含业务逻辑。对外暴露 {@code /web/member/portrait/**}，
 * 内部通过 {@link UserServiceFeign} 调用 userms 服务（{@code /user/ms/portrait/**}）。</p>
 *
 * <p>画像接口常开，不受旧版 BFF 兼容开关控制。</p>
 *
 * <p>认证方式：统一依赖 commons 中的 JWT Token 过滤器，不放开匿名访问。</p>
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/web/member/portrait")
public class WebMemberPortraitController {

    @Autowired
    private UserServiceFeign userServiceFeign;

    /**
     * 会员画像分页查询
     *
     * @param params keyword / layer / minOrders / page / size
     * @return 分页结果 + 汇总统计 + 数据完整度
     */
    @PostMapping("/page")
    public ResponseResult page(@RequestBody(required = false) Map<String, Object> params) {
        return userServiceFeign.memberPortraitPage(params == null ? new HashMap<>() : params);
    }

    /**
     * 单个会员画像详情
     *
     * @param params userId
     * @return 画像详情
     */
    @PostMapping("/detail")
    public ResponseResult detail(@RequestBody Map<String, Object> params) {
        return userServiceFeign.memberPortraitDetail(params == null ? new HashMap<>() : params);
    }

    /** RFM 八分群矩阵 */
    @PostMapping("/rfm")
    public ResponseResult rfm() {
        return userServiceFeign.memberPortraitRfm(new HashMap<>());
    }

    /** 智能分群圈人预览 */
    @PostMapping("/segment/preview")
    public ResponseResult segmentPreview(@RequestBody(required = false) Map<String, Object> params) {
        return userServiceFeign.memberPortraitSegmentPreview(params == null ? new HashMap<>() : params);
    }

    /** 保存人群包 */
    @PostMapping("/crowd/save")
    public ResponseResult crowdSave(@RequestBody Map<String, Object> params) {
        return userServiceFeign.memberPortraitCrowdSave(params == null ? new HashMap<>() : params);
    }

    /** 人群包列表 */
    @PostMapping("/crowd/list")
    public ResponseResult crowdList() {
        return userServiceFeign.memberPortraitCrowdList(new HashMap<>());
    }

    /** 删除人群包 */
    @PostMapping("/crowd/delete")
    public ResponseResult crowdDelete(@RequestBody Map<String, Object> params) {
        return userServiceFeign.memberPortraitCrowdDelete(params == null ? new HashMap<>() : params);
    }

    /** 标签字典 */
    @PostMapping("/tag/list")
    public ResponseResult tagList() {
        return userServiceFeign.memberPortraitTagList(new HashMap<>());
    }

    /** 新建 / 更新标签 */
    @PostMapping("/tag/save")
    public ResponseResult tagSave(@RequestBody Map<String, Object> params) {
        return userServiceFeign.memberPortraitTagSave(params == null ? new HashMap<>() : params);
    }

    /** 删除标签 */
    @PostMapping("/tag/delete")
    public ResponseResult tagDelete(@RequestBody Map<String, Object> params) {
        return userServiceFeign.memberPortraitTagDelete(params == null ? new HashMap<>() : params);
    }

    /** 批量打标 */
    @PostMapping("/tag/attach")
    public ResponseResult tagAttach(@RequestBody Map<String, Object> params) {
        return userServiceFeign.memberPortraitTagAttach(params == null ? new HashMap<>() : params);
    }

    /** 批量取消打标 */
    @PostMapping("/tag/detach")
    public ResponseResult tagDetach(@RequestBody Map<String, Object> params) {
        return userServiceFeign.memberPortraitTagDetach(params == null ? new HashMap<>() : params);
    }
}
