package com.houjiang.activity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.activity.dto.JoinVoucherActivityDTO;
import com.houjiang.activity.dto.VoucherTemplateUpdateDTO;
import com.houjiang.activity.entity.ActivityVoucherTemplateEntity;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * 代金券模版
 * @author Amos
 */
public interface ActivityVoucherTemplateService extends IService<ActivityVoucherTemplateEntity> {

    /**
     * 商家后台：查询优惠券信息
     *
     */
    ResponseResult selectVoucherTemp(@RequestBody Map<String, Object> paraMap);

    /**
     * 商家后台：删除代金券信息 1.1将删除代金券改为修改代金券状态
     * @param paraMap
     * @return
     */
    ResponseResult deleteVoucherTemp(@RequestBody Map<String, String> paraMap);

    /**
     * 商家后台：插入代金券模板
     * @param param
     * @return
     */
    ResponseResult insertVoucherTemp(VoucherTemplateUpdateDTO param);

    /**
     * 查询可报名的优惠券活动
     * @return
     */
    ResponseResult queryPlatformVoucherList(Map<String,String> param);

    /**
     * 商家参加优惠券活动
     * @param param
     * @return
     */
    ResponseResult joinVoucherActivity(JoinVoucherActivityDTO param);

    /**
     * 优惠商品
     * @param param
     * @return
     */
    ResponseResult queryVoucherGoods(JoinVoucherActivityDTO param);


    /**
     * 平删除 店铺优惠券
     *
     * @param paraMap
     * @return
     */
    ResponseResult deleteSalesPromotionVoucher(Map<String, String> paraMap);

    /**
     * 平台查询 店铺优惠券详情
     *
     * @param paraMap
     * @return
     */
    ResponseResult salesPromotionVoucherDetail(Map<String, Object> paraMap);

    /**
     * 平台查询 店铺优惠券列表
     *
     * @param paraMap
     * @return
     */
    ResponseResult salesPromotionVoucherList(Map<String, Object> paraMap);



    /**
     * 平台修改 店铺优惠券修改
     *
     * @param paraMap
     * @return
     */

    /**
     * TODO 补充方法说明
     * @param paraMap TODO
     * @return TODO
     */
    ResponseResult salesPromotionVoucherUpdate(Map<String, Object> paraMap);

    /**
     * 商家后台：插入商品兑换券
     * @param param
     * @return
     */
    ResponseResult addProductVoucher(VoucherTemplateUpdateDTO param);

    /**
     * 商家后台：查询商品兑换券
     * @param param
     * @return
     */
    ResponseResult queryProductVoucher(Map<String, Object> param);

    /**
     * 商家后台：查询商品兑换券 详情
     * @param param
     * @return
     */
    ResponseResult queryProductVoucherDetail(VoucherTemplateUpdateDTO param);

    /**
     *  商家后台：查询商品兑换券 详情 商品
     * @param param
     * @return
     */
    ResponseResult queryProductVoucherGoodsDetail(VoucherTemplateUpdateDTO param);

    /**
     * 查询所有兑换券 活动
     * @param param
     * @return
     */
    ResponseResult queryAllProductVoucherActivity(Map<String, Object> param);

    /**
     * 查询兑换券下 所有下所有商品
     * @param param
     * @return
     */
    ResponseResult queryProductVoucherAllGoods(VoucherTemplateUpdateDTO param);

    /**
     *  校验兑换券是否有效 feign
     * @param userId
     * @param commonId
     * @param voucherTId
     * @return
     */
    ResponseResult queryProductVoucherBaseId(String userId, Integer commonId, Integer voucherTId);


    /**
     * 更新状态
     *
     * @param param
     */
    void updateVoucherState(Map<String, Object> param);
}
