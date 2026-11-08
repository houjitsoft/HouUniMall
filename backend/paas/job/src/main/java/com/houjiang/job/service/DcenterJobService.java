package com.houjiang.job.service;



import com.houjiang.commons.util.ResponseResult;

import java.util.concurrent.CompletableFuture;

/**
 * @ClassName DcenterJobService
 * @Description
 * @Author Amos
 * @Date 2025/1/14 10:21
 * @Version 1.0
 * @author Amos
 */

public interface DcenterJobService {

    /**
     * 更新dcenter相关表数据
     *
     * @param paraMap
     * @return
     */
    CompletableFuture<ResponseResult<?>> updateDcenterData();
}
