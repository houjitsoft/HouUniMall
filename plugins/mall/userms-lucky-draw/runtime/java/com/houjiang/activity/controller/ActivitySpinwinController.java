package com.houjiang.activity.controller;

import com.houjiang.activity.support.ActivitySupport;
import com.houjiang.activity.service.ActivitySpinwinService;
import org.springframework.beans.factory.annotation.Autowired;
import com.houjiang.activity.dto.ActivityExplainParamDTO;
import com.houjiang.commons.util.ResponseResult;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ClassName ActivitySpinwinController
 * @Description
 * @Author Amos
 * @Date 2023/7/11 9:54
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("spinwin")
public class ActivitySpinwinController{
    @Autowired
    protected ActivitySpinwinService activitySpinwinService;


    /**
     * 更新抽奖限制
     *
     * @param param
     * @return
     */
    @PostMapping("/limit/update")
    public ResponseResult updateSpinwinLimit(@RequestBody Map<String, Object> param) {
        ResponseResult result = activitySpinwinService.updateSpinwinLimit(param);
        return result;
    }

    /**
     * 查询抽奖限制
     *
     * @param param
     * @return
     */
    @PostMapping("/limit/query")
    public ResponseResult querySpinwinLimit(@RequestBody Map<String, Object> param) {
        ResponseResult result = activitySpinwinService.querySpinwinLimit(param);
        return result;
    }

    /**
     * 删除幸运大转盘活动
     *
     * @param param
     * @return
     */
    @PostMapping("/delete")
    public ResponseResult deleteSpinwinActivity(@RequestBody Map<String, Object> param) {
        ResponseResult result = activitySpinwinService.deleteSpinwinActivity(param);
        return result;
    }

    /**
     * 查询幸运大转盘活动详情
     *
     * @param param
     * @return
     */
    @PostMapping("/query/detail")
    public ResponseResult querySpinwinActivityDetail(@RequestBody Map<String, Object> param) {
        ResponseResult result = activitySpinwinService.querySpinwinActivityDetail(param);
        return result;
    }

    /**
     * 查询幸运大转盘活动列表
     *
     * @param param
     * @return
     */
    @PostMapping("/query/list")
    public ResponseResult querySpinwinActivityList(@RequestBody Map<String, Object> param) {
        ResponseResult result = activitySpinwinService.querySpinwinActivityList(param);
        return result;
    }

    /**
     * 增加或更新幸运大转盘活动
     *
     * @param param
     * @return
     */
    @PostMapping("/update")
    public ResponseResult updateSpinwinActivity(@RequestBody Map<String, Object> param) {
        ResponseResult result = activitySpinwinService.updateSpinwinActivity(param);
        return result;
    }

    /**
     * 查询用户可抽奖次数和总的参与人数
     *
     * @param param
     * @return
     */
    @PostMapping("/draw/times/query")
    public ResponseResult querySpinWinDrawTimes(@RequestBody ActivityExplainParamDTO param) {
        ResponseResult result = activitySpinwinService.querySpinWinDrawTimes(param);
        return result;
    }
    /**
     * 查询活动说明
     *
     * @param param
     * @return
     */
    @PostMapping("/activity/explain/query")
    public ResponseResult queryActivityExplain(@RequestBody ActivityExplainParamDTO param) {
        ResponseResult result = activitySpinwinService.queryActivityExplain(param);
        return result;
    }

    /**
     * 用户抽奖
     *
     * @param param
     * @return
     */
    @GlobalTransactional
    @PostMapping("/lottery/draw/update")
    public ResponseResult updateLotteryDrawTimes(@RequestBody ActivityExplainParamDTO param) {
        ResponseResult result = activitySpinwinService.updateLotteryDraw(param);
        return result;
    }

}
