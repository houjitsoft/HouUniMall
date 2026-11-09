package com.houjiang.userms.service;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.userms.dto.MemberPortraitQueryDTO;
import com.houjiang.userms.dto.PortraitCrowdDTO;
import com.houjiang.userms.dto.PortraitSegmentDTO;
import com.houjiang.userms.dto.PortraitTagDTO;

/**
 * 会员画像服务
 *
 * <p>只做「真实数据聚合 + 可解释计算」，不做任何模拟数据、不做补数。
 * 缺失维度通过 {@code dataQuality} 与每个会员的 {@code hasXxxData} 显式暴露。</p>
 *
 * <p>能力对齐同行业电商 CRM（有赞/微盟口径）：RFM 八分群、生命周期分层、
 * 智能分群圈人、人群包、标签体系、单客 360 行为时间线。</p>
 *
 * @author Amos
 */
public interface MemberPortraitService {

    /**
     * 会员画像分页查询（含全局汇总、数据完整度、RFM 与生命周期分布）
     */
    ResponseResult queryPortraitPage(MemberPortraitQueryDTO dto);

    /**
     * 单个会员画像详情（含偏好、价格带、行为时间线）
     *
     * @param userId 会员ID
     */
    ResponseResult queryPortraitDetail(Long userId);

    /**
     * RFM 八分群矩阵：每群的会员数、占比、人均消费、代表成员与运营动作
     */
    ResponseResult queryRfmMatrix();

    /**
     * 智能分群：按条件实时圈人，返回规模与名单预览
     */
    ResponseResult previewSegment(PortraitSegmentDTO dto);

    /**
     * 保存人群包（同时落成员快照，便于复盘与导出）
     */
    ResponseResult saveCrowd(PortraitCrowdDTO dto);

    /** 人群包列表 */
    ResponseResult queryCrowdList();

    /** 删除人群包（逻辑删除） */
    ResponseResult deleteCrowd(Long id);

    /** 标签字典：每个标签的名称、说明、覆盖人数 */
    ResponseResult queryTagList();

    /** 新建或更新标签 */
    ResponseResult saveTag(PortraitTagDTO dto);

    /** 删除标签（置为停用，保留历史关系） */
    ResponseResult deleteTag(Integer tagId);

    /** 批量给会员打标 */
    ResponseResult attachTag(PortraitTagDTO dto);

    /** 批量取消会员的某个标签 */
    ResponseResult detachTag(PortraitTagDTO dto);
}
