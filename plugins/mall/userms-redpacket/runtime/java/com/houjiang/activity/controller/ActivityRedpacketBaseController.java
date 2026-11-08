package com.houjiang.activity.controller;


import com.houjiang.activity.support.ActivitySupport;
import com.houjiang.activity.service.ActivityRedpacketBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import com.houjiang.activity.dto.GetAvaliableRedpDTO;
import com.houjiang.activity.dto.UserAvailableRedpacketDTO;
import com.houjiang.activity.dto.UserAvailableRedpacketQueryDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.activity.RedTemplateEntityVO;
import com.houjiang.commons.vo.activity.HouRedpacketBaseVO;
import com.houjiang.commons.vo.activity.HouRedpacketTemplateVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @ClassName ActivityRedpacketBaseController
 * @Description 红包表
 * @Author Amos
 * @Date 2023-05-23 16:42:51
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("redpacketBase")
public class ActivityRedpacketBaseController {
    @Autowired
    protected ActivityRedpacketBaseService activityRedpacketBaseService;


    /**
     * 查询是否 有可领红包
     *
     * @param param
     * @return
     */
    @PostMapping("/allAvaliable")
    public ResponseResult checkReceivedRpOrNot(@RequestBody UserAvailableRedpacketQueryDTO param) {
        ResponseResult result = activityRedpacketBaseService.checkReceivedRpOrNot(param);
        return result;
    }

    /**
     * 一键领取红包（pc使用）
     *
     * @param paraMap
     * @return
     */
    @PostMapping("/insertRed/all")
    public ResponseResult insertAllAvaliable(@RequestBody Map<String, String> paraMap) {
        return activityRedpacketBaseService.insertAllAvaliable(paraMap);
    }

    /**
     * 获取红包信息 feign
     */
    @PostMapping("/get/redDetail")
    public HouRedpacketBaseVO getRedDetail(@RequestBody Map<String, String> paraMap) {
        HouRedpacketBaseVO houRedpacketTemplate = activityRedpacketBaseService.getRedDetail(paraMap);
        return houRedpacketTemplate;

    }

    /**
     * 我的红包
     *
     * @param param
     * @return
     */
    @PostMapping("/select/myRedPacket")
    public ResponseResult selectMyRedPacket(@RequestBody UserAvailableRedpacketDTO param) {
        ResponseResult result = activityRedpacketBaseService.selectMyRedPacket(param);
        return result;
    }

    /**
     * plus 会员 查询红包信息
     */
    @PostMapping("/plus/selectRedPacket")
    public List<HouRedpacketTemplateVO> selectRedPacket(@RequestParam List<Integer> id) {
        List<HouRedpacketTemplateVO> houRedpacketTemplateVOS = activityRedpacketBaseService.selectRedPacket(id);
        return houRedpacketTemplateVOS;
    }

    /**
     * plus 会员 领取红包
     */
    @PostMapping("/plus/insertRedPacket")
    public ResponseResult insertRedPacket(@RequestBody Map<String, Object> insertMap) {
        ResponseResult result = activityRedpacketBaseService.insertRedPacket(insertMap);
        return result;
    }

    /**
     * 修改红包领取数量
     *
     * @param updateMap
     * @return
     */
    @PostMapping("/plus/update")
    public ResponseResult updateRedpGiveout(@RequestBody Map<String, String> updateMap) {
        ResponseResult result = activityRedpacketBaseService.updateRedpGiveout(updateMap);
        return result;
    }

    /**
     * 查询红包模板
     *
     * @param redIdList
     * @return
     */
    @PostMapping("/plus/selectRedByTemplateId")
    public List<RedTemplateEntityVO> selectredtemplateId(@RequestParam("redIdList") List<Integer> redIdList) {
        List<RedTemplateEntityVO> redTemplateEntityVOS = activityRedpacketBaseService.selectredtemplateId(redIdList);
        return redTemplateEntityVOS;
    }

    /**
     * 判断红包可用张数
     *
     * @param redIdList
     * @param userId
     * @return
     */
    @PostMapping("/plus/selectUserRedNum")
    public Integer selectuserred(@RequestParam("redIdList") List<Integer> redIdList, @RequestParam("userId") String userId) {
        Integer selectuserred = activityRedpacketBaseService.selectuserred(redIdList, userId);
        return selectuserred;
    }

    /**
     * 添加红包
     *
     * @param paraMap
     * @return
     */
    @PostMapping("/insertRedpacketBase")
    public ResponseResult insertRedpacketBase(@RequestBody Map<String, Object> paraMap) {
        ResponseResult result = activityRedpacketBaseService.insertRedpacketBase(paraMap);
        return result;
    }

    /**
     * 平台后台查询红包的用户信息
     */
    @PostMapping("/platform/userList")
    public ResponseResult userList(@RequestBody Map<String, Object> paraMap) {
        ResponseResult result = activityRedpacketBaseService.userList(paraMap);
        return result;
    }

    /**
     * 平台后台红包列表导出
     *
     * @throws IOException
     */
    @PostMapping("/platform/detailListExp")
    public ResponseResult DetaillistExp(@RequestBody Map<String, Object> param, HttpServletResponse response) throws IOException {
        ResponseResult result = activityRedpacketBaseService.DetaillistExp(param, response);
        return result;
    }


    /**
     * 平台后台红包详情列表
     */
    @PostMapping("/platform/detailList")
    public ResponseResult listRedpacketDetailTemp(@RequestBody Map<String, Object> param) {
        ResponseResult result = activityRedpacketBaseService.listRedpacketDetailTemp(param);
        return result;
    }

    /**
     * 查询可用红包
     */
    @RequestMapping("/query/list")
    public ResponseResult queryList(@RequestBody UserAvailableRedpacketQueryDTO param) {
        ResponseResult result = activityRedpacketBaseService.queryList(param);
        return result;

    }

    /**
     * 用户领取红包
     *
     * @param paraMap
     * @return
     */
    @PostMapping("/receive")
    public ResponseResult receive(@RequestBody Map<String, String> paraMap) {
        return activityRedpacketBaseService.receive(paraMap);
    }

    /**
     * 用户根据订单金额查询可用的红包
     */
    @PostMapping("/selectByDeposit")
    public ResponseResult selectByDeposit(@RequestBody GetAvaliableRedpDTO getAvaliableRedp) {
        ResponseResult result = activityRedpacketBaseService.selectByDeposit(getAvaliableRedp);
        return result;
    }
}

