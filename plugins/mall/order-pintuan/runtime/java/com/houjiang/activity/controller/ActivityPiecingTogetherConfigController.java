package com.houjiang.activity.controller;

import com.houjiang.activity.support.ActivitySupport;
import com.houjiang.activity.service.ActivityPiecingTogetherConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import com.houjiang.activity.dto.PiecingTogetherConfigDTO;
import com.houjiang.activity.dto.WebPinTuanParamDTO;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ClassName ActivityPiecingTogetherConfigController
 * @Description
 * @Author Amos
 * @Date 2023/7/12 15:05
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("piecingTogetherConfig")
public class ActivityPiecingTogetherConfigController{
    @Autowired
    protected ActivityPiecingTogetherConfigService activityPiecingTogetherConfigService;


    /**
     * 删除拼团配置
     * @param param
     * @return
     */
    @PostMapping("/h5Config/modelDelete")
    public ResponseResult h5ConfigModelDelete(@RequestBody PiecingTogetherConfigDTO param) {
        ResponseResult result = activityPiecingTogetherConfigService.h5ConfigModelDelete(param);
        return result;
    }

    /**
     * 新增拼团配置
     * @param param
     * @return
     */
    @PostMapping("/h5Config/modelAdd")
    public ResponseResult h5ConfigModelAdd(@RequestBody PiecingTogetherConfigDTO param) {
        ResponseResult result = activityPiecingTogetherConfigService.h5ConfigModelAdd(param);
        return result;
    }

    /**
     * 修改拼团配置
     * @param param
     * @return
     */
    @PostMapping("/h5Config/update")
    public ResponseResult updateH5Config(@RequestBody PiecingTogetherConfigDTO param) {
        ResponseResult result = activityPiecingTogetherConfigService.updateH5Config(param);
        return result;
    }

    /**
     * 查询拼团配置
     * @param param
     * @return
     */
    @PostMapping("/h5Config/query")
    public ResponseResult queryH5Config(@RequestBody Map<String,Object> param) {
        ResponseResult result = activityPiecingTogetherConfigService.queryH5Config(param);
        return result;
    }
    /**
     * h5 获取拼团主页
     *
     * @return
     */
    @PostMapping("/queryH5Index")
    public ResponseResult queryH5PinTuanIndex(@RequestBody WebPinTuanParamDTO param) {

        return activityPiecingTogetherConfigService.queryH5PinTuanIndex(param);
    }
    /**
     * h5 获取更多拼团商品
     *
     * @return
     */
    @PostMapping("/querynGoodsAll")
    public ResponseResult queryPinTuanGoodsAll(@RequestBody WebPinTuanParamDTO param) {

        return activityPiecingTogetherConfigService.queryPinTuanGoodsAll(param);
    }
}
