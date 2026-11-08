package com.houjiang.activity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.activity.dto.UserAvailableRedpacketQueryDTO;
import com.houjiang.activity.dto.UserRedpacketSendDTO;
import com.houjiang.activity.entity.ActivityRedpacketTemplateEntity;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * @author Amos
 */
public interface ActivityRedpacketTemplateService extends IService<ActivityRedpacketTemplateEntity> {


    /**
     * 查询红包模板信息
     *
     * @param paraMap
     * @return
     */
    ResponseResult getList(Map<String, Object> paraMap);

    /**
     * 分页查询红包模板
     * @param map
     * @return
     */
    ResponseResult selectByPage(Map<String, Object> map);


    /**
     * 查询红包列表(有效红包)
     * @param map
     * @return
     */
    ResponseResult selectList(Map<String, Object> map);
    /**
     * 大转盘 查询红包列表(有效红包)
     * @param map
     * @return
     */
    ResponseResult spinwinSelectList(Map<String, Object> map);

    /**
     * 查询可用的红包列表
     *
     * @return
     */
    ResponseResult available(Map<String, Object> map);

    /**
     * 发送红包给用户 导入方式
     *
     * @param
     * @return
     */
     ResponseResult sendUserRedpacketImport(MultipartFile file, Integer redpacketTId,String token);

    /**
     * 发送红包给用户
     *
     * @param param
     * @return
     */
    ResponseResult sending(UserRedpacketSendDTO param);

    /**
     * 查询可赠送的红包列表
     *
     * @return
     */
     ResponseResult giveList();

    /**
     * 平台后台红包修改
     */
    ResponseResult updateRedpacketTemp(Map<String, Object> paraMap);

    /**
     * 平台后台红包增加
     */
   ResponseResult insertRedpacketTemp(Map<String, String> paraMap) throws IOException;

    /**
     * 平台后台红包详情
     */
   ResponseResult getRedpacketDetail(Map<String, Object> paraMap);

    /**
     * 查询预售红包列表
     * @param param
     * @return
     */
    ResponseResult queryBookingRpList(UserAvailableRedpacketQueryDTO param);
}
