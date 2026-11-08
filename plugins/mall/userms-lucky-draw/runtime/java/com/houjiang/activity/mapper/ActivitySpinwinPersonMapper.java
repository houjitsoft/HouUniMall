package com.houjiang.activity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.activity.dto.ActivityExplainParamDTO;
import com.houjiang.activity.entity.ActivitySpinwinPersonEntity;
import com.houjiang.activity.vo.PrizeRecordResultVO;
import com.houjiang.activity.vo.SpinwinLuckyDrawVO;
import com.houjiang.activity.vo.SpinwinPersonListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * @Entity com.houjiang.activity.entity.ActivitySpinwinPerson
 * @author Amos
 */
@Mapper
public interface ActivitySpinwinPersonMapper extends BaseMapper<ActivitySpinwinPersonEntity> {

    /**
     * 查询参与幸运大转盘活动的用户列表
     *
     * @param param
     * @return
     */
    IPage<SpinwinPersonListVO> querySpinwinPersonList(IPage<SpinwinPersonListVO> page, @Param("param") Map<String, Object> param);

    /**
     * 获奖记录
     * @param page
     * @param param
     * @return
     */
    IPage<PrizeRecordResultVO> selectPrizeRecord(IPage<PrizeRecordResultVO> page, @Param("param")ActivityExplainParamDTO param);

    /**
     * 查询获奖记录数量
     * @param param
     * @return
     */
    Integer selectPrizeRecordCount(@Param("param")ActivityExplainParamDTO param);

    /**
     * 新增幸运大转盘抽奖记录
     * @param luckyDraw
     */
    void insertLuckyDraw(SpinwinLuckyDrawVO luckyDraw);
}




