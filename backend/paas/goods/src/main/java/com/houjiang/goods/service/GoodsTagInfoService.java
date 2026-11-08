package com.houjiang.goods.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.goods.entity.GoodsTagInfoEntity;

import java.util.Map;

/**
 * @author Amos
 */
public interface GoodsTagInfoService extends IService<GoodsTagInfoEntity> {

    /**
     * 编辑话题
     * @param
     * @return
     */
     ResponseResult updateCommunity(Map<String, Object> map);


    /**
     * 增加话题
     * @param
     * @return
     */
    ResponseResult insertCommunity(Map<String, Object> map);


    /**
     * 删除话题
     * @param
     * @return
     */
    ResponseResult deleteCommunity(Map<String, Object> map);


    /**
     * 查询话题
     * @param
     * @return
     */
     ResponseResult selectCommunity(Map<String, Object> map);

    /**
     * 查询话题 总帖数、阅读数
     * @param param
     * @return
     */
    ResponseResult queryCount(Map<Object, Object> param);

    /**
     * 发表社区心得-查询话题
     * @param param
     * @return
     */
    ResponseResult queryTags(Map<Object, Object> param);


    /**
     * 查询话题
     *
     * @return
     */
    ResponseResult queryCommunity(Map<String, String> map);



}
