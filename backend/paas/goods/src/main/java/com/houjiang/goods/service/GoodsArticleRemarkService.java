package com.houjiang.goods.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.goods.entity.GoodsArticleRemarkEntity;

import java.util.Map;

/**
 * @author Amos
 */
public interface GoodsArticleRemarkService extends IService<GoodsArticleRemarkEntity> {

    /**
     * 更改社区评论状态
     * @param
     * @return
     */
    ResponseResult updateStatus(Map<Object, Object> map);


    /**
     * 查询社区评论详情列表
     * @param
     * @return
     */
    ResponseResult selectDetailsList(Map<String, Object> map);


    /**
     * 查询社区评论列表
     * @param
     * @return
     */
    ResponseResult selectList(Map<String, Object> map);

    /**
     * 获取评论列表
     * @param param
     * @return
     */
    ResponseResult queryRemarkList(Map<String, Object> param);

    /**
     * 发表评论
     * @param param
     * @return
     */
    ResponseResult addRemark(Map<Object, Object> param);
}
