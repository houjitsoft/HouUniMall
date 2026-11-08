package com.houjiang.base.controller;

import com.houjiang.base.dto.BasekdiniaoConfigDTO;
import com.houjiang.base.entity.BaseKdniaoConfigEntity;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ClassName BaseKdniaoConfigController
 * @Description 快递鸟配置
 * @Author Amos
 * @Date 2023/6/21 10:50
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("base/kdniaoConfig")
public class BaseKdniaoConfigController  extends BaseController{

    /**
     * 查询快递鸟配置  服务间feign 调用使用
     *
     * @return
     */
    @PostMapping("/feign/queryConfig")
    public ResponseResult queryConfigFeign() {
        return baseKdniaoConfigService.queryConfigFeign();
    }

    /**
     * update 快递鸟配置 存在就更新不存在就添加
     *
     * @param basekdiniaoConfigReq
     * @return
     */
    @PostMapping("/updateKdiniaoConfig")
    public ResponseResult updateKdiniaoConfig(@RequestBody BasekdiniaoConfigDTO basekdiniaoConfigReq) {
        ResponseResult result=baseKdniaoConfigService.updateKdiniaoConfig(basekdiniaoConfigReq);
        return result;
    }

    /**
     * 查询快递鸟配置
     *
     * @param param
     * @return
     */
    @PostMapping("/queryConfig")
    public ResponseResult queryConfig(@RequestBody Map<String, Object> param) {
        ResponseResult result=baseKdniaoConfigService.queryConfig(param);
        return result;
    }

    /**
     * 查询快递鸟账号
     *
     * @param paraMap
     * @return
     */
    @PostMapping("/queryShopKdNiao")
    public ResponseResult queryShopKdNiao(@RequestBody Map<String, Object> paraMap) {
        return baseKdniaoConfigService.queryShopKdNiao(paraMap);
    }

    /**
     * 删除快递鸟账号
     * @param paraMap
     * @return
     */
    @PostMapping("/deleteShopKdniao")
    public ResponseResult deleteShopkdniaoParam(@RequestBody Map<String, Object> paraMap) {
        return baseKdniaoConfigService.deleteShopkdniaoParam(paraMap);

    }
}
