package com.houjiang.goods.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.goods.entity.GoodsTagClassInfoEntity;

import java.util.Map;

/**
 * @author Amos
 */
public interface GoodsTagClassInfoService extends IService<GoodsTagClassInfoEntity> {

    /**
     * 删除分类
     * @param
     * @return
     */
    ResponseResult deleteCommunityTopic(Map<String, Object> map);


    /**
     * 增加分类
     * @param
     * @return
     */
   ResponseResult insertCommunityTopic(Map<String, Object> map);



    /**
     * 查询分类
     * @param
     * @return
     */
    ResponseResult selectCommunityTopic(Map<String, Object> map);

    /**
     * 发表社区心得-查询话题分类
     * @param param
     * @return
     */
    ResponseResult queryTagClass(Map<Object, Object> param);


}
