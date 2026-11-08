package com.houjiang.activity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.activity.dto.ActivityExplainParamDTO;
import com.houjiang.activity.entity.ActivitySpinwinPrizeEntity;
import com.houjiang.activity.vo.SpinwinPrizeListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Entity com.houjiang.activity.entity.ActivitySpinwinPrize
 * @author Amos
 */
@Mapper
public interface ActivitySpinwinPrizeMapper extends BaseMapper<ActivitySpinwinPrizeEntity> {

    /**
     * 新增大转盘活动奖项
     * @param param
     */
    void insertSpinwinPrize(@Param("param") Map<String, Object> param);

    /**
     * 更新大转盘活动奖项
     * @param param
     */
    void updateSpinwinPrize(@Param("param") Map<String, Object> param);

    /**
     * 大转盘活动奖项列表
     * @param param
     * @return
     */
    List<SpinwinPrizeListVO> selectSpinwinPrizeList(@Param("param") Map<String, Object> param);

    /**
     * 更新给出的礼品数量
     * @param param
     */
    void updateGiveOutGiftNum(@Param("param")ActivityExplainParamDTO param);
}




