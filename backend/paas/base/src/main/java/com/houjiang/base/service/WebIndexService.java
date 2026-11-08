package com.houjiang.base.service;

import com.houjiang.commons.dto.goods.WebGoodsListQueryDTO;
import com.houjiang.commons.util.ResponseResult;

import java.util.Map;

/**
 * @ClassName WebIndexService
 * @Description pc首页装修
 * @Author Amos
 * @Date 2023/5/24 9:18
 * @Version 1.0
 * @author Amos
 */
public interface WebIndexService {

    /**
     * 查询首页楼层
     *
     * @return
     */
    ResponseResult queryAllAdpageFloor();

    /**
     * 查询热搜词,首页导航,轮播,热销商品
     *
     * @return
     */
    ResponseResult queryIndex(Integer integralType);

    /**
     * pc 首页 天天特价 特色专题 为你推荐 查询解析
     */
    ResponseResult selectPcIndexConfigAll();

    /**
     * pc首页查询分类信息
     *
     * @param catId
     * @param goodsType
     * @param token
     * @return
     */
    ResponseResult queryByGoodsCatId(Integer catId, Integer goodsType, String token);

    /**
     * 查询首页楼层
     *
     * @param map
     * @return
     */
    ResponseResult queryAdpageFloorGoods(Map<String, Object> map);

    /**
     * 查询商品分类
     *
     * @param catParentId
     * @return
     */
    ResponseResult queryItemCat(Integer catParentId, Integer shopId);

    /**
     * 查询导航
     *
     * @return
     */
    ResponseResult queryPlatformNav();

    /**
     * 商品列表头部信息
     *
     * @param param
     * @return
     */
    ResponseResult queryGoodsListHeaderFilter(WebGoodsListQueryDTO param);

    /**
     * 获取上传图片配置地址
     *
     * @param configKey
     * @return
     */
    ResponseResult getImageConfigUrl(String configKey);

    /**
     * 查询首页底部
     *
     * @return
     */
    ResponseResult queryButtomHelp();

    /**
     * 查询 pc 首页 天天特价 特色专题 为你推荐 楼层装修 智能推荐
     *
     * @param map
     * @return
     */
    ResponseResult newQueryPcIndex(Map<String, Object> map);

    /**
     * 关闭 或 开启 pc 首页 楼层
     *
     * @param map
     * @return
     */
    ResponseResult pcIndexFloorUpdate(Map<String, Object> map);

    /**
     * 关闭 或 开启 pc 首页 天天特技 特色专题 为你推荐 楼层
     *
     * @param map
     * @return
     */
    ResponseResult pcIndexConfigUpdate(Map<String, Object> map);

    /**
     * 检测应用中心状态
     *
     * @return
     */
    ResponseResult detectingApplicationStatus();

    /**
     * 检测Im应用中心状态 及查询im配置
     *
     * @param config
     * @return
     */
    ResponseResult detectingImStatus(String config);

    /**
     * 检测Im应用中心状态 及查询imAppKey
     *
     * @return
     */
    ResponseResult detectingImAppKey();

    /**
     * 检测Im应用中心状态 及查询用户是否有未读消息
     *
     * @param token
     * @return
     */
    ResponseResult detectingImChatHistoryRead(String token);
}
