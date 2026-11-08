package com.houjiang.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.dto.sensitive.BaseSensitiveRecordDTO;
import com.houjiang.base.entity.BaseSensitiveRecordEntity;
import com.houjiang.commons.util.ResponseResult;

/**
 * @ClassName BaseSensitiveRecordService
 * @Description  敏感词命中记录
 * @Author Amos
 * @Date 2024-01-30 16:48:44
 * @Version 1.0
 * @author Amos
 */
public interface BaseSensitiveRecordService extends IService<BaseSensitiveRecordEntity> {
    /**
     * 平台查询敏感词命中记录
     * @param param
     * @return
     */
    ResponseResult querybaseSensitiveRecord(BaseSensitiveRecordDTO param);

    /**
     * 添加敏感词命中记录
     * @param param
     * @return
     */
    ResponseResult addbaseSensitiveRecord(BaseSensitiveRecordDTO param);
}
