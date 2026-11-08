package com.houjiang.activity.controller;

import com.houjiang.activity.support.ActivitySupport;
import com.houjiang.activity.service.ActivityRedpacketTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import com.houjiang.activity.dto.UserAvailableRedpacketQueryDTO;
import com.houjiang.activity.dto.UserRedpacketSendDTO;
import com.houjiang.commons.annnotation.SensitiveCheck;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * @ClassName ActivityRedpacketTemplateController
 * @Description 红包模板
 * @Author Amos
 * @Date 2023/7/4 11:10
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("redpacketTemplate")
public class ActivityRedpacketTemplateController{
    @Autowired
    protected ActivityRedpacketTemplateService activityRedpacketTemplateService;


    /**
     * 查询红包模板信息
     *
     * @param paraMap
     * @return
     */
    @PostMapping("/list")
    public ResponseResult getList(@RequestBody Map<String, Object> paraMap) {
        ResponseResult result = activityRedpacketTemplateService.getList(paraMap);
        return result;
    }


    /**
     * 分页查询红包模板
     * @param map
     * @return
     */
    @PostMapping("/page")
    public ResponseResult selectByPage(@RequestBody Map<String, Object> map) {
        ResponseResult result = activityRedpacketTemplateService.selectByPage(map);
        return result;
    }

    /**
     * 查询红包列表(有效红包)
     * @param map
     * @return
     */
    @PostMapping("/selectList")
    public ResponseResult selectList(@RequestBody Map<String, Object> map) {
        ResponseResult result = activityRedpacketTemplateService.selectList(map);
        return result;
    }
    /**
     * 大转盘 查询红包列表(有效红包)
     * @param map
     * @return
     */
    @PostMapping("/spinwin/selectList")
    public ResponseResult spinwinSelectList(@RequestBody Map<String, Object> map) {
        ResponseResult result = activityRedpacketTemplateService.spinwinSelectList(map);
        return result;
    }

    /**
     * 查询可用的红包列表
     *
     * @return
     */
    @PostMapping("/available")
    public ResponseResult available(@RequestBody Map<String, Object> map) {
        ResponseResult result = activityRedpacketTemplateService.available(map);
        return result;
    }

    /**
     * 发送红包给用户 导入方式
     *
     * @param
     * @return
     */
    @PostMapping("/import/update")
    public ResponseResult sendUserRedpacketImport(@RequestParam("file") MultipartFile file, @RequestParam("redpacketTId") Integer redpacketTId, @RequestParam("token") String token) {
        ResponseResult result = activityRedpacketTemplateService.sendUserRedpacketImport(file,redpacketTId,token);
        return result;
    }


    /**
     * 发送红包给用户
     *
     * @param param
     * @return
     */
    @PostMapping("/sending")
    public ResponseResult sending(@RequestBody UserRedpacketSendDTO param) {
        ResponseResult result = activityRedpacketTemplateService.sending(param);
        return result;
    }

    /**
     * 查询可赠送的红包列表
     *
     * @return
     */
    @PostMapping("/giveList")
    public ResponseResult giveList() {
        ResponseResult result = activityRedpacketTemplateService.giveList();
        return result;
    }


    /**
     * 平台后台红包修改
     */
    @SensitiveCheck(field = {"redpacketTTitle"})
    @PostMapping("/platform/update")
    public ResponseResult updateRedpacketTemp(@RequestBody Map<String, Object> paraMap) {
        ResponseResult result = activityRedpacketTemplateService.updateRedpacketTemp(paraMap);
        return result;
    }

    /**
     * 平台后台红包增加
     */
    @SensitiveCheck(field = {"redpacketTDesc","redpacketRemark","redpacketTTitle"})
    @PostMapping("/platform/insert")
    public ResponseResult insertRedpacketTemp(@RequestBody Map<String, String> paraMap) throws IOException {
        ResponseResult result = activityRedpacketTemplateService.insertRedpacketTemp(paraMap);
        return result;
    }

    /**
     * 平台后台红包详情
     */
    @PostMapping("/platform/detail")
    public ResponseResult getRedpacketDetail(@RequestBody Map<String, Object> paraMap) {
        ResponseResult result = activityRedpacketTemplateService.getRedpacketDetail(paraMap);
        return result;
    }

    /**
     * 查询预售红包列表
     * @param param
     * @return
     */
    @PostMapping("/booking/list")
    public ResponseResult queryBookingRpList(@RequestBody UserAvailableRedpacketQueryDTO param) {
        return activityRedpacketTemplateService.queryBookingRpList(param);
    }
}
