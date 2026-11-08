package com.houjiang.goods.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.goods.entity.GoodsDraftEntity;

import java.util.Map;

/**
 * @ClassName GoodsDraftService
 * @Description 草稿箱
 * @Author Amos
 * @Date 2023/8/11 9:15
 * @Version 1.0
 * @author Amos
 */
public interface GoodsDraftService extends IService<GoodsDraftEntity> {

    /**
     * 查询草稿箱列表
     * @param param
     * @return
     */
    ResponseResult queryDraft(Map<String,Object> param);

    /**
     * 删除草稿箱
     * @param param
     * @return
     */
    ResponseResult deleteDraft(Map<String,Object> param);

    /**
     * 存草稿箱
     * @param param
     * @return
     */
    ResponseResult addGoodsToDraft(Map<String,Object> param);
}
