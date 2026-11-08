package com.houjiang.activity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.activity.dto.UserRedpacketSendDTO;
import com.houjiang.activity.entity.ActivityRedpacketTemplateEntity;
import com.houjiang.commons.vo.activity.ActivityRedpacketTemplateVO;
import com.houjiang.commons.vo.activity.HouRedpacketTemplateVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Entity com.houjiang.activity.entity.ActivityRedpacketTemplate
 * @author Amos
 */
@Mapper
public interface ActivityRedpacketTemplateMapper extends BaseMapper<ActivityRedpacketTemplateEntity> {

    /**
     * 查询模板信息
     * @param type
     * @return
     */
    List<ActivityRedpacketTemplateVO> selectNewRedpacketTem(@Param("list")List<Integer> type);

    /**
     * 查询可用的红包列表
     *
     * @return
     */
    IPage<ActivityRedpacketTemplateEntity> available(IPage<ActivityRedpacketTemplateEntity> page , @Param("param") Map<String, Object> param);

    /**
     * 发送红包给用户 导入方式
     *
     * @param
     * @return
     */
    UserRedpacketSendDTO selectByRedpacketTId(Integer redpacketTId);

    /**
     * 修改赠送红包用户人数
     * @param num
     */
    void updateRedpacketTGiveout(@Param("num") Integer num, @Param("redpacketTId") Integer redpacketTId);

    /**
     * 查询可赠送的红包列表
     *
     * @return
     */
    List<ActivityRedpacketTemplateEntity> giveList();

    /**
     * 平台后台红包修改
     */
    Integer updateRedpacketTemp(@Param("param") Map<String, Object> paraMap);

    /**
     * 查询当前红包是否在 有效期区间内
     * @param paraMap
     * @return
     */
    Integer selectRedpacketTime(@Param("param") Map<String, Object> paraMap);

    /**
     * 平台后台红包增加
     * @param paraMap
     * @return
     */
    Integer insertRedpacketTemp(@Param("param")Map<String, String> paraMap);

    /**
     * 查询该红包领取限制
     * @param checkMap
     * @return
     */
    Integer checkReceivedRp(Map<String, String> checkMap);

    /**
     * 后边模板详情
     * @param paraMap
     * @return
     */
    HouRedpacketTemplateVO selectRpTemById(Map<String, String> paraMap);

    /**
     * 查询预售红包列表
     * @return
     */
    List<HouRedpacketTemplateVO> selectBookingRpList();

    /**
     * 查询平台 有效红包 排除 plus 和 新人红包
     * @return
     */
    List<Integer> selectredtemplateId();

}




