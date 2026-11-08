package com.houjiang.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.dto.sensitive.BaseSensitiveDTO;
import com.houjiang.base.entity.BaseSensitiveEntity;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.sensitive.BaseSensitiveVO;

import java.util.List;

/**
 * @ClassName BaseSensitiveService
 * @Description  敏感词词库
 * @Author Amos
 * @Date 2024-01-30 16:45:08
 * @Version 1.0
 * @author Amos
 */
public interface BaseSensitiveService extends IService<BaseSensitiveEntity> {

    /**
     * 平台查询敏感词库
     * @param param
     * @return
     */
    ResponseResult querySensitiveConfig(BaseSensitiveDTO param);

    /**
     * 平台 新增或修改 敏感词库
     * @param param
     * @return
     */
    ResponseResult add(BaseSensitiveDTO param);

    /**
     * 平台 删除 敏感词库
     * @param param
     * @return
     */
    ResponseResult delete(BaseSensitiveDTO param);

    /**
     * 平台 批量添加 敏感词
     * @param param
     * @return
     */
    ResponseResult addList(BaseSensitiveDTO param);

    /**
     * 查询所有敏感词 feign
     * @return
     */
    List<BaseSensitiveVO> selectAllSensitive();

}
