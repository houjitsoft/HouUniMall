package com.houjiang.activity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.activity.dto.PintuanBaseAddDTO;
import com.houjiang.activity.dto.PintuanBaseListQueryDTO;
import com.houjiang.activity.dto.WebPinTuanParamDTO;
import com.houjiang.activity.entity.ActivityPiecingTogetherEntity;
import com.houjiang.commons.dto.goods.ListPageQueryParamDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.activity.AppPintuanGoodsQueryResultVO;
import com.houjiang.commons.vo.activity.JoinPintuanParamVO;

import java.util.Map;

/**
 * @ClassName ActivityPiecingTogetherService
 * @Description 拼团主表
 * @Author Amos
 * @Date 2023-05-25 14:27:05
 * @Version 1.0
 * @author Amos
 */
public interface ActivityPiecingTogetherService extends IService<ActivityPiecingTogetherEntity> {
    /**
     * 查询拼团活动信息
     *
     * @param commonId
     * @param goodsId
     * @return
     */
    AppPintuanGoodsQueryResultVO selectByGoodsId(Integer commonId, Integer goodsId);

    /**
     * 查询拼团详情
     *
     * @param markId
     * @return
     */
    ResponseResult querySurplusNum(String markId);

    /**
     * 发起拼团
     * @param param
     * @return
     */
    ResponseResult addJoinPintuan(JoinPintuanParamVO param);

    /**
     * 支付成功后回写已付款并按已付款人数判定成团（供 order 支付回调调用）
     *
     * @param param markId + orderId
     * @return {formed:0/1, orderIdList:已付款订单号}
     */
    ResponseResult markPaidAndEvaluate(Map<String, Object> param);

    /**
     * 查询所有 限时拼团商品
     * @param param
     * @return
     */
    ResponseResult queryAll(ListPageQueryParamDTO param);

    /**
     * 取消订单 删除当前用户的拼团信息
     */
    ResponseResult delete(Map<String, Object> param);

    /**
     * pc 查询拼团商品
     * @param webPinTuanParam
     * @return
     */
    ResponseResult queryGoods(WebPinTuanParamDTO webPinTuanParam);


    /**
     * 查询拼团信息 feign
     */

    /**
     * TODO 补充方法说明
     * @param param TODO
     * @return TODO
     */
    ResponseResult selectMarkInfo(Map<String,Object> param);

    /**
     * 修改拼团支付状态
     */
    ResponseResult updatePayStart(Map<String,Object> param);

    /**
     * 组件化查询拼团活动列表
     **/
    /**
     * TODO 补充方法说明
     * @param param TODO
     * @return TODO
     */
    ResponseResult queryList(PintuanBaseListQueryDTO param);

    /**
     * 查询活动商品价格
     */
    ResponseResult queryGoodsPrice(Integer goodsId);


    /**
     * 查询所有拼团商品
     * @param param
     * @return
     */
    ResponseResult queryGoodsAll(Map<String, Object>  param);


    /**
     * 更新权重
     * @param param
     * @return
     */
    ResponseResult updateDisorder(Map<String, Object>  param);


    /**
     * 查询拼团活动列表
     *
     * @param param
     * @return
     */
    ResponseResult queryPintuanBaseList(Map<String, Object> param);

    /**
     * 组件化查询拼团、 限时秒杀 、限时折扣 活动商品详情
     *
     * @param param
     * @return
     */
    ResponseResult modularizationQueryPintuanBasebyId(Map<String,Object> param);

    /**
     * 生成 h5 或 小程序 邀请好友拼团海报
     * @param param
     * @return
     */
    ResponseResult getPinTuanPoster(Map<String, Object> param);


    /**
     * 查询拼团活动详情
     *
     * @param param
     * @return
     */
    ResponseResult queryPinTuanBaseDetail(Map<String, Object>  param);


    /**
     * 新增拼团活动
     *
     * @param param
     * @return
     */
    ResponseResult addPinTuanBase(PintuanBaseAddDTO param);



    /**
     * 删除拼团活动详情
     *
     * @param param
     * @return
     */
    ResponseResult deletePinTuanBase(Map<String, Object>  param);

    /**
     * 启停拼团活动（status：1 启用 / 0 停用）
     *
     * @param param pintuanId + status
     * @return 操作结果
     */
    ResponseResult updatePinTuanBaseStatus(Map<String, Object> param);

    /**
     * 查询拼团活动列表
     *
     * @param param
     * @return
     */
    ResponseResult queryPinTuanBaseList(Map<String, Object> param);


    /**
     * 查询拼团详情
     *
     * @param param
     * @return
     */
    ResponseResult queryDetail(JoinPintuanParamVO param);

    /**
     * 正在拼团的用户列表
     */
    ResponseResult queryList(Map<String,Object>  param);


    /**
     * 通过扫小程序二维码 获取类型参数 根据参数从redis中获取具体参数
     */
     ResponseResult getParameterByToken(Map<String,Object> param);
}

