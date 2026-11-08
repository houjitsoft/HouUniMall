package com.houjiang.goods.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.goods.dto.CartGuessLikeGoodsQueryDTO;
import com.houjiang.goods.dto.WapUserFootPrintDTO;
import com.houjiang.goods.entity.GoodsUserFootprintEntity;

import java.util.Map;

/**
 * @ClassName GoodsUserFootprintService
 * @Description  商品访问足迹表
 * @Author Amos
 * @Date 2023-06-08 10:01:59
 * @Version 1.0
 * @author Amos
 */
public interface GoodsUserFootprintService extends IService<GoodsUserFootprintEntity> {
    /**
     * 查询购物车底部猜你喜欢
     * @param param
     * @return
     */
    ResponseResult queryCartGuessLikeGoods(CartGuessLikeGoodsQueryDTO param);

    /**
     * 查询足迹
     *
     * @param param
     * @return
     */
    ResponseResult queryUserFoot(WapUserFootPrintDTO param);

    /**
     * 查询用户足迹信息
     * 该方法通过用户的相关参数查询用户的足迹记录，并返回查询结果
     * 主要用于用户行为分析、个性化推荐等场景
     *
     * @param param 包含查询用户足迹所需信息的数据传输对象
     * @return 返回一个ResponseResult对象，其中包含查询状态和查询到的用户足迹信息
     */
    ResponseResult imQueryFootPrint(WapUserFootPrintDTO param);


    /**
     * 删除所有足迹
     *
     * @param map
     * @return
     */
    ResponseResult deleteFootPrint(Map<String, Object> map);


    /**
     * 根据用户商品足迹 查询对应分类下的商品
     *
     * @param map
     * @return
     */
    ResponseResult queryUserRandomGoods(Map<String, Object> map);
}
