package com.houjiang.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.base.entity.BaseSystemMessageEntity;
import com.houjiang.commons.dto.base.BaseSystemMessageDTO;
import com.houjiang.commons.dto.base.SysMessageDTO;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * @ClassName BaseSystemMessageService
 * @Description 系统消息服务
 * @Author Amos
 * @Date 2023/7/19 16:02
 * @Version 1.0
 * @author Amos
 */
public interface BaseSystemMessageService extends IService<BaseSystemMessageEntity> {

    /**
     * 查询下单消息
     *
     * @param map
     * @return
     */
    ResponseResult querySysOrderNews(@RequestBody Map<String, Object> map);

    /**
     * 设置已读订单消息
     *
     * @param map
     * @return
     */
    ResponseResult readSysOrderNews(@RequestBody Map<Object, Object> map);

    /**
     * 删除系统下单消息(伪删除)
     *
     * @param map
     * @return
     */
    ResponseResult deleteSysOrderNews(@RequestBody Map<Object, Object> map);

    /**
     * 添加系统下单消息
     * @param baseSystemMessageDTO
     * @return
     */
    ResponseResult addSysOrderNews(BaseSystemMessageDTO baseSystemMessageDTO);

    /**
     * feign调用添加系统消息
     * sysMessageDTO
     * @return
     */
    void insertSysMessage(Map<String, Object> map);
}
