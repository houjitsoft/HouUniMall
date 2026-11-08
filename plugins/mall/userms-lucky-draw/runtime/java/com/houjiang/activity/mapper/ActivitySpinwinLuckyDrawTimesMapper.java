package com.houjiang.activity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.activity.dto.ActivityExplainParamDTO;
import com.houjiang.activity.entity.ActivitySpinwinLuckyDrawTimesEntity;
import com.houjiang.activity.vo.DrawTimesDetailsVO;
import com.houjiang.activity.vo.SpinwinLuckyDrawVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * @Entity com.houjiang.activity.entity.ActivitySpinwinLuckyDrawTimes
 * @author Amos
 */
@Mapper
public interface ActivitySpinwinLuckyDrawTimesMapper extends BaseMapper<ActivitySpinwinLuckyDrawTimesEntity> {

    /**
     * 查询大转盘用户抽奖次数
     * @param param
     * @return
     */
    DrawTimesDetailsVO selectDrawTimes(@Param("param") ActivityExplainParamDTO param);

    /**
     * 分页查询大转盘用户抽奖次数
     * @param page
     * @param param
     * @return
     */
    IPage<ActivitySpinwinLuckyDrawTimesEntity> selectLuckyDrawTimesList(IPage page,@Param("param") Map<String, Object> param);

    /**
     * 新增大转盘用户抽奖次数
     * @param param
     */
    void insertDrawTimes(ActivityExplainParamDTO param);

    /**
     * 新增大转盘用户抽奖次数
     * @param luckyDraw
     */
    void insertDrawTimes2(SpinwinLuckyDrawVO luckyDraw);

    /**
     * 更新大转盘用户抽奖次数
     * @param luckyDraw
     */
    void updateDrawTimes(SpinwinLuckyDrawVO luckyDraw);
}




