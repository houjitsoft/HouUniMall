package com.houjiang.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.base.dto.BaseOssConfigDTO;
import com.houjiang.base.entity.BaseOssConfigEntity;
import com.houjiang.commons.util.ResponseResult;

import java.util.Map;

/**
 * @ClassName BaseOssConfigService
 * @Description OSS
 * @Author Amos
 * @Date 2023/6/21 16:43
 * @Version 1.0
 * @author Amos
 */
public interface BaseOssConfigService extends IService<BaseOssConfigEntity> {

    /**
     * 初始化SMS配置信息 服务间feign 调用使用
     *
     * @return
     */
    ResponseResult selectOSSInfoFeign(String ossType);

    /**
     * 初始化SMS配置信息 前端页面 调用使用
     *
     * @param param
     * @return
     */
    ResponseResult selectOSSInfo(Map<String, Object> param);


    /**
     * update oss配置 存在就更新不存在就添加
     *
     * @param baseOssConfigReq
     * @return
     */
    ResponseResult updateOssConfig(BaseOssConfigDTO baseOssConfigReq);

    /**
     * 修改oss配置启用
     * @param param
     * @return
     */
    ResponseResult updateOssStates(BaseOssConfigDTO param);

    /**
     * 查看oss启用配置
     *
     * @return
     */
    String queryOssStates();

    /**
     * 查看全部oss配置
     *
     * @return
     */
    ResponseResult queryAllOssConfig();

    /**
     * 存储监控概览（配置统计 + 文件库统计）
     */
    ResponseResult queryStorageMonitor();
}
