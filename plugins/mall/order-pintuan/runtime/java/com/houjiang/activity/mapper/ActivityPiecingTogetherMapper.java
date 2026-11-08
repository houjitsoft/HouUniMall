package com.houjiang.activity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.activity.dto.*;
import com.houjiang.activity.entity.ActivityPiecingTogetherEntity;
import com.houjiang.activity.vo.*;
import com.houjiang.commons.dto.goods.ListPageQueryParamDTO;
import com.houjiang.commons.vo.activity.AppPintuanGoodsQueryResultVO;
import com.houjiang.commons.vo.activity.JoinPintuanParamVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @ClassName ActivityPiecingTogetherMapper
 * @Description 拼团主表
 * @Author Amos
 * @Date 2023-05-25 14:27:05
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface ActivityPiecingTogetherMapper extends BaseMapper<ActivityPiecingTogetherEntity> {


    /**
     * 查询拼团信息
     * @param commonId
     * @param goodsId
     * @return
     */
    AppPintuanGoodsQueryResultVO selectByGoodsId(@Param("commonId") Integer commonId, @Param("goodsId") Integer goodsId);

    /**
     * 查询剩余拼团人数
     * @param markId
     * @return
     */
    Integer selectSurplusNum(@Param("markId") String markId);

    /**
     * 查询拼团信息
     *
     * @param commonId
     * @param goodsId
     * @return
     */
    PintuanBaseMessageVO selectPintuanId(@Param("commonId") Integer commonId,@Param("goodsId") Integer goodsId);

    /**
     * 查询拼团id
     * @param commonId
     * @return
     */
    Integer selectPintuanByCommonId(@Param("commonId") Integer commonId);

    /**
     * 查询团长信息
     * @param markId
     * @return
     */
    PintuanMarkMessageVO selectMarkMessage(@Param("markId")String markId);

    /**
     * 查询所有拼团商品
     * @param page
     * @param param
     * @return
     */
    IPage<Map<String, Object>> queryBrandList(IPage<Map<String, Object>> page, @Param("param")ListPageQueryParamDTO param);

    /**
     * pc 查询拼团商品
     * @param page
     * @param param
     * @return
     */
    IPage<GoodsPinTuanResultVO> queryPinTuanGoods(IPage<GoodsPinTuanResultVO> page, @Param("param")WebPinTuanParamDTO param);

    /**
     * 分页查询拼团信息
     * @param page
     * @param param
     * @return
     */
    IPage<GoodsPinTuanResultVO> queryGoodsAll(IPage<GoodsPinTuanResultVO> page , @Param("param") Map<String, Object> param);

    /**
     * 查询拼团详情
     * @param commonid
     * @return
     */
    GoodsPinTuanResultVO queryPinTunGoods(String commonid);

    /**
     * 查询拼团活动列表
     *
     * @param param
     * @return
     */
    IPage<ReductionPolicyBaseListVO> queryPintuanBaseList(IPage<ReductionPolicyBaseListVO> page , @Param("param") Map<String, Object> param);

    /**
     * 查询拼团详情
     * @param pinTuanId
     * @return
     */
    Map<String, Object> selectModularization(@Param("pinTuanId")Integer pinTuanId);

    /**
     *  查询拼团商品详情
     * @param pinTuanId
     * @return
     */
    Map<String, Object> selectModularizationPintuanBaseById (@Param("pinTuanId") Integer pinTuanId);

    /**
     * 查询拼团活动信息
     *
     * @param commonId
     * @return
     */
    List<PintuanGoodsSpecListVO> selectByCommonId5(@Param("commonId") Integer commonId, @Param("pintuanId") String pintuanId);


    /**
     * 新增拼团信息
     * @param param
     */
    void insertPintuanBase(@Param("param") PintuanBaseAddDTO param);

    /**
     * 新增拼团详情
     * @param param
     */
    void insertPintuanDetail(@Param("param") PintuanDetailSpecAddDTO param);

    /**
     * 更新拼团信息
     * @param param
     */
    void updatePintuanBase(@Param("param") PintuanBaseAddDTO param);

    /**
     * 更新拼团详情
     * @param param
     */
    void updatePintuanDetail(@Param("param")PintuanDetailSpecAddDTO param);

    /**
     * 分页查询拼团信息
     * @param page
     * @param param
     * @return
     */
    IPage<ActivityPiecingTogetherEntity> selectPinTuanBaseList(IPage<ActivityPiecingTogetherEntity> page , @Param("param") Map<String, Object> param);

    /**
     * 查询购买者信息
     * @param markId
     * @return
     */
    List<PintuanBuyerMassageDTO> selectBuyerMassage(@Param("markId")String markId);

    /**
     * 查询拼团详情
     * @param param
     * @return
     */
    JoinPintuanDetailDTO selectUserIsJoin(@Param("param") JoinPintuanParamVO param);

    /**
     * 分页查询拼团详情
     * @param param
     * @return
     */
    IPage<PintuanListResultVO> selectPintuanList(IPage<PintuanListResultVO> page , @Param("param") Map<String, Object> param);
}

