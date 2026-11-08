package com.houjiang.payment.feign;

import com.houjiang.commons.dto.order.WapGoodsSaleSumDTO;
import com.houjiang.commons.dto.yiyatong.RegionByCodeOpenDTO;
import com.houjiang.commons.dto.yiyatong.SubmitOrderDTO;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author Amos
 * @date 2023/4/12 10:46
 */
@FeignClient("goods")
public interface GoodServiceClient {


    /**
     * @param wapGood wap商品
     * @return 处理结果
     */
    @PostMapping("/goods/common/update/goodsSalenum")
    ResponseResult updateGoodsSalenum(@RequestBody List<WapGoodsSaleSumDTO> wapGood);

    /**
     * 创建订单
     * @param dto
     */
    @PostMapping("/yiYaTong/submitOrder")
    ResponseResult submitOrder(@RequestBody SubmitOrderDTO dto);

    /**
     * 获取怡亚通的省市区地址
     * @param regionByCodeOpenDTO
     */
    @PostMapping("/yiYaTong/getRegionByCodeOpen")
    ResponseResult getRegionByCodeOpen(@RequestBody RegionByCodeOpenDTO regionByCodeOpenDTO);
    /**
     * 发送订单信息到京东
     * @param orderId
     * @return
     */
    @GetMapping("/jingDong/sendOrderToJingDong")
    ResponseResult sendOrderToJingDong(@RequestParam String orderId);
}
