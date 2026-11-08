package com.houjiang.activity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.activity.dto.ActivityExplainParamDTO;
import com.houjiang.activity.entity.ActivitySpinwinGiftEntity;
import com.houjiang.activity.vo.SpinwinGiftListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Entity com.houjiang.activity.entity.ActivitySpinwinGift
 * @author Amos
 */
@Mapper
public interface ActivitySpinwinGiftMapper extends BaseMapper<ActivitySpinwinGiftEntity> {

    /**
     * 查询奖项数量
     * @param param
     * @return
     */
    Integer selectGiftActivityCount(@Param("param") Map<String, Object> param);

    /**
     * 新增奖项
     * @param param
     */
    void insertSpinwinGift(@Param("param") Map<String, Object> param);

    /**
     * 更新奖项
     * @param param
     */
    void updateSpinwinGift(@Param("param") Map<String, Object> param);

    /**
     * 分页查询奖项信息
     * @param page
     * @param param
     * @return
     */
    IPage<SpinwinGiftListVO> selectSpinwinGiftList(IPage<SpinwinGiftListVO> page , @Param("param") Map<String, Object> param);

    /**
     * 查询奖项列表
     * @param param
     * @return
     */
    List<SpinwinGiftListVO> selectSpinWinGift(@Param("param") ActivityExplainParamDTO param);
}




