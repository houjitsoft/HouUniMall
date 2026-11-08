package com.houjiang.job.service;

import com.houjiang.commons.util.ResponseResult;

import java.util.Map;

/**
 * 定时任务管理（系统设置）
 * @author Amos
 */
public interface JobManageService {

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    ResponseResult stats();

    /**
     * TODO 补充方法说明
     * @param params TODO
     * @return TODO
     */
    ResponseResult list(Map<String, Object> params);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    ResponseResult modules();

    /**
     * TODO 补充方法说明
     * @param params TODO
     * @return TODO
     */
    ResponseResult detail(Map<String, Object> params);

    /**
     * TODO 补充方法说明
     * @param params TODO
     * @return TODO
     */
    ResponseResult create(Map<String, Object> params);

    /**
     * TODO 补充方法说明
     * @param params TODO
     * @return TODO
     */
    ResponseResult update(Map<String, Object> params);

    /**
     * TODO 补充方法说明
     * @param jobId TODO
     * @return TODO
     */
    ResponseResult delete(Long jobId);

    /**
     * TODO 补充方法说明
     * @param jobId TODO
     * @return TODO
     */
    ResponseResult pause(Long jobId);

    /**
     * TODO 补充方法说明
     * @param jobId TODO
     * @return TODO
     */
    ResponseResult resume(Long jobId);

    /**
     * TODO 补充方法说明
     * @param jobId TODO
     * @return TODO
     */
    ResponseResult trigger(Long jobId);

    /**
     * TODO 补充方法说明
     * @param jobId TODO
     * @param enabled TODO
     * @return TODO
     */
    ResponseResult toggleEnabled(Long jobId, Boolean enabled);
}
