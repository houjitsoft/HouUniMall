package com.houjiang.activity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.activity.dto.ActivityExplainParamDTO;
import com.houjiang.activity.dto.GiftDTO;
import com.houjiang.activity.entity.ActivitySpinwinEntity;
import com.houjiang.activity.vo.SpinWinDrawDetailVO;
import com.houjiang.activity.vo.SpinwinLuckyDrawVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Entity com.houjiang.activity.entity.ActivitySpinwin
 * @author Amos
 */
@Mapper
public interface ActivitySpinwinMapper extends BaseMapper<ActivitySpinwinEntity> {

    /**
     * 更新抽奖限制
     *
     * @param param
     * @return
     */
    void updateSpinwinLimit(@Param("param") Map<String, Object> param);

    /**
     * 查询幸运大转盘活动列表
     *
     * @param param
     * @return
     */
    IPage<ActivitySpinwinEntity> selectSpinwinActivityList(IPage<ActivitySpinwinEntity> page , @Param("param") Map<String, Object> param);

    /**
     * 新增幸运大转盘
     * @param param
     */
    void insertSpinwinActivity(@Param("param") Map<String, Object> param);

    /**
     * 更新幸运大转盘
     * @param param
     */
    void updateSpinwinActivity(@Param("param") Map<String, Object> param);

    /**
     * 查询幸运大转盘详情
     * @param param
     * @return
     */
    SpinWinDrawDetailVO selectDrawDetail(@Param("param")ActivityExplainParamDTO param);

    /**
     * 查询图文说明
     * @param param
     * @return
     */
    String selectActivityExplain(@Param("param")ActivityExplainParamDTO param);

    /**
     * 查询可用的礼品
     * @param param
     * @return
     */
    List<GiftDTO> selectActivityGift(@Param("param")ActivityExplainParamDTO param);

    /**
     * 更新参与人数
     * @param luckyDraw
     */
    void updateActivity(@Param("param")SpinwinLuckyDrawVO luckyDraw);
}




