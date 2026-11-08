package com.houjiang.base.controller;

import com.houjiang.base.dto.BaseOssConfigDTO;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.util.ResponseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @ClassName BaseOssConfigController
 * @Description OSS配置
 * @Author Amos
 * @Date 2023/6/21 16:39
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("base/ossConfig")
public class BaseOssConfigController extends BaseController{

    private final static Logger log = LoggerFactory.getLogger(BaseOssConfigController.class);

    /**
     * 初始化SMS配置信息 服务间feign 调用使用
     *
     * @return
     */
    @PostMapping("/feign/oss/selectOSSInfo")
    public ResponseResult selectOSSInfoFeign(@RequestParam("ossType") String ossType) {
        ResponseResult result = null;
        try {
            result = baseOssConfigService.selectOSSInfoFeign(ossType);

        } catch (Exception e) {

            return ResponseResult.error(e.getMessage());
        }

        return result;
    }

    /**
     * 初始化SMS配置信息 前端admin查看配置 调用使用
     *
     * @param param
     * @return
     */
    @PostMapping("/oss/selectOSSInfo")
    public ResponseResult selectOSSInfo(@RequestBody Map<String, Object> param) {
        ResponseResult result = null;
        try {
            result = baseOssConfigService.selectOSSInfo(param);

        } catch (Exception e) {

            return ResponseResult.error(e.getMessage());
        }

        return result;
    }

    /**
     * update oss配置 存在就更新不存在就添加
     *
     * @param baseOssConfigReq
     * @return
     */
    @PostMapping("/updateOssConfig")
    public ResponseResult updateOssConfig(@RequestBody BaseOssConfigDTO baseOssConfigReq) {
        ResponseResult result = baseOssConfigService.updateOssConfig(baseOssConfigReq);
        return result;
    }

    /**
     * 修改oss配置启用
     * @param param
     * @return
     */
    @PostMapping("/updateOssStates")
    public ResponseResult updateOssStates(@RequestBody BaseOssConfigDTO param) {
        ResponseResult result = null;
        try {
            result = baseOssConfigService.updateOssStates(param);

        } catch (Exception e) {
            System.err.println(e.getMessage());
            return ResponseResult.error(e.getMessage());
        }

        return result;
    }

    /**
     * 查看oss启用配置
     *
     * @return
     */
    @PostMapping("/queryOssStates")
    public String queryOssStates() {
        String result = null;
        try {
            result = baseOssConfigService.queryOssStates();

        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }

        return result;
    }

    /**
     * 查看全部oss配置
     *
     * @return
     */
    @PostMapping("/queryAllOssConfig")
    public ResponseResult queryAllOssConfig() {
        ResponseResult result = null;
        try {
            result = baseOssConfigService.queryAllOssConfig();

        } catch (Exception e) {
            System.err.println(e.getMessage());
            return ResponseResult.error(e.getMessage());
        }

        return result;
    }

    /**
     * 存储监控概览
     */
    @PostMapping("/monitor")
    public ResponseResult queryStorageMonitor() {
        try {
            return baseOssConfigService.queryStorageMonitor();
        } catch (Exception e) {
            log.error("查询存储监控失败", e);
            return ResponseResult.error(e.getMessage());
        }
    }

}
