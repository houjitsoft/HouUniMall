package com.houjiang.activity.controller;

import com.houjiang.activity.support.ActivitySupport;
import com.houjiang.activity.service.ActivityVoucherTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import com.houjiang.activity.dto.JoinVoucherActivityDTO;
import com.houjiang.activity.dto.VoucherTemplateUpdateDTO;
import com.houjiang.commons.annnotation.SensitiveCheck;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ResponseResult;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @ClassName ActivityVoucherTemplateController
 * @Description 代金券模版
 * @Author Amos
 * @Date 2023/7/4 11:09
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("/voucher/template")
public class ActivityVoucherTemplateController{
    @Autowired
    protected ActivityVoucherTemplateService activityVoucherTemplateService;




    /**
     * 商家后台：查询优惠券信息
     *
     */
    @PostMapping("/select")
    public ResponseResult selectVoucherTemp(@RequestBody Map<String, Object> paraMap) {
        ResponseResult result = activityVoucherTemplateService.selectVoucherTemp(paraMap);
        return result;
    }

    /**
     * 商家后台：删除代金券信息 1.1将删除代金券改为修改代金券状态
     */
    @PostMapping("/delete")
    public ResponseResult deleteVoucherTemp(@RequestBody Map<String, String> paraMap) {
        ResponseResult result = activityVoucherTemplateService.deleteVoucherTemp(paraMap);
        return result;
    }

    /**
     * 商家后台：插入代金券模板
     *
     */
    @SensitiveCheck(field = {"voucherTTitle"})
    @GlobalTransactional
    @PostMapping("/insert")
    public ResponseResult insertVoucherTemp(@RequestBody VoucherTemplateUpdateDTO param) {
        // 校验参数
        if (param.getVoucherTLimit().compareTo(param.getVoucherTPrice()) < 0) {
            return ResponseResult.fail(BizCodeEnume.INCORRECT_AMOUNT);
        }
        ResponseResult result = activityVoucherTemplateService.insertVoucherTemp(param);
        return result;
    }

    /**
     * 查询可报名的优惠券活动
     * @return
     */
    @PostMapping("/query/list")
    public ResponseResult queryPlatformVoucherList(@RequestBody Map<String,String> param) {
        ResponseResult result=activityVoucherTemplateService.queryPlatformVoucherList(param);
        return result;
    }

    /**
     * 商家参加优惠券活动
     * @param param
     * @return
     */
    @PostMapping("/update")
    public ResponseResult joinVoucherActivity(@RequestBody JoinVoucherActivityDTO param) {
        ResponseResult result=activityVoucherTemplateService.joinVoucherActivity(param);
        return result;
    }

    /**
     * 优惠商品
     * @param param
     * @return
     */
    @PostMapping("/query")
    public ResponseResult queryVoucherGoods(@RequestBody JoinVoucherActivityDTO param) {
        ResponseResult result=activityVoucherTemplateService.queryVoucherGoods(param);
        return result;
    }

    /**
     * 平删除 店铺优惠券
     *
     * @param paraMap
     * @return
     */
    @PostMapping("/platform/delete")
    public ResponseResult deleteSalesPromotionVoucher(@RequestBody Map<String, String> paraMap) {
        ResponseResult result=activityVoucherTemplateService.deleteSalesPromotionVoucher(paraMap);
        return result;

    }

    /**
     * 平台查询 店铺优惠券详情
     *
     * @param paraMap
     * @return
     */
    @PostMapping("/platform/detail")
    public ResponseResult salesPromotionVoucherDetail(@RequestBody Map<String, Object> paraMap) {
        ResponseResult result=activityVoucherTemplateService.salesPromotionVoucherDetail(paraMap);
        return result;
    }


    /**
     * 平台查询 店铺优惠券列表
     *
     * @param paraMap
     * @return
     */
    @PostMapping("/platform/list")
    public ResponseResult salesPromotionVoucherList(@RequestBody Map<String, Object> paraMap) {
        ResponseResult result=activityVoucherTemplateService.salesPromotionVoucherList(paraMap);
        return result;
    }


    /**
     * 平台修改 店铺优惠券修改
     *
     * @param paraMap
     * @return
     */
    @PostMapping("/platform/update")
    public ResponseResult salesPromotionVoucherUpdate(@RequestBody Map<String, Object> paraMap) {
        ResponseResult result=activityVoucherTemplateService.salesPromotionVoucherUpdate(paraMap);
        return result;
    }
    /**
     * 商家后台：插入商品兑换券
     *
     */
    @GlobalTransactional
    @PostMapping("/addProductVoucher")
    public ResponseResult addProductVoucher(@RequestBody VoucherTemplateUpdateDTO param) {
        ResponseResult result = activityVoucherTemplateService.addProductVoucher(param);
        return result;
    }
    /**
     * 商家后台：查询商品兑换券
     *
     */
    @PostMapping("/queryProductVoucher")
    public ResponseResult queryProductVoucher(@RequestBody Map<String, Object> param) {
        ResponseResult result = activityVoucherTemplateService.queryProductVoucher(param);
        return result;
    }
    /**
     * 商家后台：查询商品兑换券 详情
     *
     */
    @PostMapping("/queryProductVoucherDetail")
    public ResponseResult queryProductVoucherDetailDetail(@RequestBody VoucherTemplateUpdateDTO param) {
        ResponseResult result = activityVoucherTemplateService.queryProductVoucherDetail(param);
        return result;
    }
    /**
     * 商家后台：查询商品兑换券 详情 商品
     *
     */
    @PostMapping("/queryProductVoucherGoodsDetail")
    public ResponseResult queryProductVoucherGoodsDetail(@RequestBody VoucherTemplateUpdateDTO param) {
        ResponseResult result = activityVoucherTemplateService.queryProductVoucherGoodsDetail(param);
        return result;
    }

    /**
     * 查询所有兑换券 活动
     * @param param
     * @return
     */
    @PostMapping("/queryAllProductVoucherActivity")
    public ResponseResult queryAllProductVoucherActivity(@RequestBody Map<String, Object> param) {
        ResponseResult result = activityVoucherTemplateService.queryAllProductVoucherActivity(param);
        return result;
    }
    /**
     * 查询兑换券下 所有下所有商品
     * @param param
     * @return
     */
    @PostMapping("/queryProductVoucherAllGoods")
    public ResponseResult queryProductVoucherAllGoods(@RequestBody VoucherTemplateUpdateDTO param) {
        ResponseResult result = activityVoucherTemplateService.queryProductVoucherAllGoods(param);
        return result;
    }

    /**
     * 校验兑换券是否有效 feign
     * @param userId
     * @param commonId
     * @param voucherTId
     * @return
     */
    @PostMapping("/queryProductVoucherBaseId")
    public ResponseResult queryProductVoucherBaseId(@RequestParam("userId") String userId,
                                                    @RequestParam("commonId") Integer commonId,
                                                    @RequestParam("voucherTId")Integer voucherTId) {
        ResponseResult result = activityVoucherTemplateService.queryProductVoucherBaseId(userId,commonId,voucherTId);
        return result;
    }

    /**
     * 更新状态
     *
     * @param param
     */
    @PostMapping("/feign/updateVoucherState")
    void updateVoucherState(@RequestBody Map<String, Object> param){
        activityVoucherTemplateService.updateVoucherState(param);
    }
}
