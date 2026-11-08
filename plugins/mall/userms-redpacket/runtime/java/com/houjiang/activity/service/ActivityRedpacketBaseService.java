package com.houjiang.activity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.activity.dto.GetAvaliableRedpDTO;
import com.houjiang.activity.dto.UserAvailableRedpacketDTO;
import com.houjiang.activity.dto.UserAvailableRedpacketQueryDTO;
import com.houjiang.activity.entity.ActivityRedpacketBaseEntity;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.activity.RedTemplateEntityVO;
import com.houjiang.commons.vo.activity.HouRedpacketBaseVO;
import com.houjiang.commons.vo.activity.HouRedpacketTemplateVO;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @ClassName ActivityRedpacketBaseService
 * @Description 红包表
 * @Author Amos
 * @Date 2023-05-23 16:42:52
 * @Version 1.0
 * @author Amos
 */
public interface ActivityRedpacketBaseService extends IService<ActivityRedpacketBaseEntity> {
    /**
     * 查询是否 有可领红包
     *
     * @param param
     * @return
     */
    ResponseResult checkReceivedRpOrNot(UserAvailableRedpacketQueryDTO param);

    /**
     * 一键领取红包（pc使用）
     *
     * @param paraMap
     * @return
     */
    ResponseResult insertAllAvaliable(Map<String, String> paraMap);

    /**
     * 获取红包信息
     * @param paraMap
     * @return
     */
    HouRedpacketBaseVO getRedDetail(Map<String, String> paraMap);


    /**
     * 我的红包
     *
     * @param param
     * @return
     */
    ResponseResult selectMyRedPacket(UserAvailableRedpacketDTO param);

    /**
     * plus 会员 查询红包信息
     * @param id
     * @return
     */
    List<HouRedpacketTemplateVO> selectRedPacket(List<Integer> id);

    /**
     * plus 会员 领取红包
     * @param insertMap
     * @return
     */
    ResponseResult insertRedPacket(Map<String, Object> insertMap);

    /**
     * 修改红包领取数量
     * @param updateMap
     * @return
     */
    ResponseResult updateRedpGiveout(Map<String, String> updateMap);

    /**
     * 查询红包模板
     * @param redIdList
     * @return
     */
    List<RedTemplateEntityVO> selectredtemplateId(List<Integer> redIdList);

    /**
     * 判断红包可用张数
     * @param redIdList
     * @param userId
     * @return
     */
    Integer selectuserred(List<Integer> redIdList, String userId);


    /**
     * 添加红包
     *
     * @param paraMap
     * @return
     */
    ResponseResult insertRedpacketBase(Map<String, Object> paraMap);


    /**
     * 平台后台查询红包的用户信息
     */
   ResponseResult userList(Map<String, Object> paraMap);

    /**
     * 平台后台红包列表导出
     *
     *
     */
   ResponseResult DetaillistExp(Map<String, Object> param, HttpServletResponse response) throws IOException;


    /**
     * 平台后台红包详情列表
     */
    ResponseResult listRedpacketDetailTemp(Map<String, Object> param);

    /**
     * 查询可用红包
     * @param param
     * @return
     */
    ResponseResult queryList(UserAvailableRedpacketQueryDTO param);

    /**
     * 用户领取红包
     * @param paraMap
     * @return
     */
    ResponseResult receive(Map<String, String> paraMap);

    /**
     * 用户根据订单金额查询可用的红包
     * @param getAvaliableRedp
     * @return
     */
    ResponseResult selectByDeposit(GetAvaliableRedpDTO getAvaliableRedp);
}

