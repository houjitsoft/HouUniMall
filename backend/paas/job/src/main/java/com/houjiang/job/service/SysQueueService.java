package com.houjiang.job.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.job.entity.SysQueueEntity;

import java.util.List;
import java.util.Map;

/**
 * @author Amos
 */
public interface SysQueueService extends IService<SysQueueEntity> {

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
     * @param id TODO
     * @return TODO
     */
    ResponseResult cancel(Long id);

    /**
     * TODO 补充方法说明
     * @param id TODO
     * @return TODO
     */
    ResponseResult retry(Long id);

    /**
     * TODO 补充方法说明
     * @param id TODO
     * @return TODO
     */
    ResponseResult executeNow(Long id);

    /**
     * TODO 补充方法说明
     * @param ids TODO
     * @return TODO
     */
    ResponseResult batchCancel(List<Long> ids);
}
