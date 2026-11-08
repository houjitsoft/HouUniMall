package com.houjiang.base.controller;

import com.houjiang.commons.dto.base.BaseSystemMessageDTO;
import com.houjiang.commons.dto.base.SysMessageDTO;
import com.houjiang.commons.util.ResponseResult;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @ClassName BaseSystemMessageController
 * @Description 系统消息
 * @Author Amos
 * @Date 2023/7/19 16:04
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("base/systemMessage")
public class BaseSystemMessageController extends BaseController {

    /**
     * 查询下单消息
     *
     * @param map
     * @return
     */
    @PostMapping("/query/sysOrderNews")
    public ResponseResult querySysOrderNews(@RequestBody Map<String, Object> map) {
        return baseSystemMessageService.querySysOrderNews(map);
    }

    /**
     * 设置已读订单消息
     *
     * @param map
     * @return
     */
    @PostMapping("/read/sysOrderNews")
    public ResponseResult readSysOrderNews(@RequestBody Map<Object, Object> map) {
        return baseSystemMessageService.readSysOrderNews(map);
    }

    /**
     * 删除系统下单消息(伪删除)
     *
     * @param map
     * @return
     */
    @PostMapping("/delete/sysOrderNews")
    public ResponseResult deleteSysOrderNews(@RequestBody Map<Object, Object> map) {
        return baseSystemMessageService.deleteSysOrderNews(map);
    }
    /**
     * 添加系统下单消息
     *
     * @param baseSystemMessageDTO
     * @return
     */
    @PostMapping({"/add/sysOrderNews"})
    public ResponseResult addSysOrderNews(@RequestBody BaseSystemMessageDTO baseSystemMessageDTO) {
        return this.baseSystemMessageService.addSysOrderNews(baseSystemMessageDTO);
    }

    /**
     * feign调用添加系统消息
     * sysMessageDTO
     * @return
     */
    @PostMapping("/feign/insertSysMessage")
    public void insertSysMessage(@RequestBody Map<String, Object> map){
         baseSystemMessageService.insertSysMessage(map);

    }

}
