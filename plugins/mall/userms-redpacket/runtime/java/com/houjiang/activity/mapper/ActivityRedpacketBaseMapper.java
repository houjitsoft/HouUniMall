package com.houjiang.activity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.activity.dto.UserAvailableRedpacketDTO;
import com.houjiang.activity.dto.UserAvailableRedpacketQueryDTO;
import com.houjiang.activity.dto.UserRedpacketSendDTO;
import com.houjiang.activity.entity.ActivityRedpacketBaseEntity;
import com.houjiang.activity.vo.AvaliableRpBaseResultVO;
import com.houjiang.activity.vo.CheckReceivedRpOrNotResultVO;
import com.houjiang.activity.vo.RedpacketDetailVO;
import com.houjiang.commons.vo.activity.RedTemplateEntityVO;
import com.houjiang.commons.vo.activity.HouRedpacketBaseVO;
import com.houjiang.commons.vo.activity.HouRedpacketTemplateVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @ClassName ActivityRedpacketBaseMapper
 * @Description 红包表
 * @Author Amos
 * @Date 2023-05-23 16:42:51
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface ActivityRedpacketBaseMapper extends BaseMapper<ActivityRedpacketBaseEntity> {

    /**
     * 查询所有用户待领取红包
     *
     * @param paraMap
     * @return
     */
    List<HouRedpacketTemplateVO> selectRedpacketTem(Map<String, String> paraMap);

    /**
     * 查询红包是否领取过
     *
     * @param checkMap
     * @return
     */
    List<String> checkReceivedRedpacketOrNot(Map<String, String> checkMap);

    /**
     * 插入用户领取红包
     *
     * @param insertMap
     * @return
     */
    Integer insertyfRedpacketBase(Map<String, Object> insertMap);

    /**
     * 增加红包领取数量
     * @param updateMap
     */
    void updateRedpGiveout(Map<String, String> updateMap);

    /**
     * 查询新人红包
     * @param paraMap
     * @return
     */
    List<HouRedpacketTemplateVO> selectNewRedpacketTem(Map<String, String> paraMap);

    /**
     * 查询红包信息
     * @param paraMap
     * @return
     */
    HouRedpacketBaseVO getRedDetail(Map<String, String> paraMap);

    /**
     * 更新红包状态
     * @param redpacketMap
     * @return
     */
    Integer updateRedpDetail(Map<String, String> redpacketMap);

    /**
     * plus 会员 查询红包信息
     * @param ids
     * @return
     */
    List<HouRedpacketTemplateVO> selectRedPacket(@Param("list")List<Integer> ids);

    /**
     * 我的红包
     *
     */
    IPage<ActivityRedpacketBaseEntity> queryBrandList(IPage<ActivityRedpacketBaseEntity> page,@Param("param") UserAvailableRedpacketDTO param);

    /**
     * plus 会员 领取红包
     * @param insertMap
     */
    void insertRedPacket(Map<String, Object> insertMap);

    /**
     * 查询红包模板
     * @param redIdList
     * @return
     */
    List<RedTemplateEntityVO> selectredtemplateId(@Param("list")List<Integer> redIdList);

    /**
     * 判断红包可用张数
     * @param ids
     * @param userId
     * @return
     */
    Integer selectuserred(@Param("list") List<Integer> ids,@Param("userId") String userId);

    /**
     * 新增红包
     * @param paraMap
     * @return
     */
    Integer insertRedpacketBase(Map<String, Object> paraMap);

    /**
     * 批量修改红包信息
     * @param users
     */
    void updateUserRedpacketBatch(@Param("users") List<UserRedpacketSendDTO> users);

    /**
     * 修改红包信息
     * @param paraMap
     */
    void updateRedpacketUserStart(@Param("param") Map<String, Object> paraMap);

    /**
     * 分页查询红包信息
     * @param page
     * @param param
     * @return
     */
    IPage<RedpacketDetailVO> querylistRedpacketDetailTemp(IPage<RedpacketDetailVO> page , @Param("param") Map<String, Object> param);

    /**
     * 查询红包id和状态
     * @param param
     * @return
     */
    List<CheckReceivedRpOrNotResultVO> checkReceivedRpOrNot2(@Param("param") UserAvailableRedpacketQueryDTO param);

    /**
     * 查询红包模板列表
     * @param param
     * @return
     */
    List<HouRedpacketTemplateVO> selectRedpacketTemList(@Param("param")UserAvailableRedpacketQueryDTO param);

    /**
     * 判断红包是否到达领取限制
     * @param checkMap
     * @return
     */
    Integer checkReceivedRpOrNot1(Map<String, String> checkMap);

    /**
     * 查询红包模板数
     * @param param
     * @return
     */
    Integer checkReceivedRpOrNot4(@Param("param")UserAvailableRedpacketQueryDTO param);

    /**
     * 查询红包信息
     * @param redpacketOwnerId
     * @param redpacketTType
     * @param string
     * @return
     */
    List<AvaliableRpBaseResultVO> selectUserAvaliableRpList(@Param("redpacketOwnerId") String redpacketOwnerId,@Param("redpacketTType") Integer redpacketTType, @Param("userAgent") String string);

    /**
     *  查询当前用户是否已经领取过该红包
     * @param redpacketId
     * @param userId
     * @return
     */
    Integer checkReceivedRpOrNot(@Param("redpacketId") Integer redpacketId, @Param("userId")String userId);
}

