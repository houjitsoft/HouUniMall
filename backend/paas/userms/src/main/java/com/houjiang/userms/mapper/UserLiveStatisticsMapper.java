package com.houjiang.userms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.userms.entity.UserLiveStatisticsEntity;
import com.houjiang.userms.vo.MemberMessageListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Entity com.houjiang.userms.entity.UserLiveStatistics
 * @author Amos
 */
@Mapper
public interface UserLiveStatisticsMapper extends BaseMapper<UserLiveStatisticsEntity> {

    /**
     * 查询各个端口日活详情
     * @param param
     * @return
     */
    IPage<MemberMessageListVO> queryUserNikkatsuDetailPort(IPage<MemberMessageListVO> page , @Param("param") Map<String, Object> param);

    /**
     * 查询各个端口日活
     * @param param
     * @return
     */
    List<UserLiveStatisticsEntity> queryUserNikkatsuPort(@Param("param")Map<String, Object> param);

    /**
     * 统计当天该用户在该平台的登录记录数
     * @param liveStatisticsEntity
     * @param todayTime
     * @return
     */
    int selectUserLiveStatisticsCount(@Param("param") UserLiveStatisticsEntity liveStatisticsEntity, @Param("todayTime") String todayTime);
}




