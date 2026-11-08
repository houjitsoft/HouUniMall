package com.houjiang.job.feign;

import com.houjiang.commons.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * @ClassName ActivityServiceClient
 * @Description
 * @Author Amos
 * @Date 2023/9/5 9:25
 * @Version 1.0
 * @author Amos
 */
@FeignClient("activity")
public interface ActivityServiceClient {

    /**
     * 修改红包接口
     *
     * @param paraMap
     * @return
     */
    @PostMapping("/activity/voucherBase/salesprm/update")
    ResponseResult updateSalesPrm(@RequestBody Map<String, Object> paraMap);

    /**
     * 修改组合商品状态
     */
    @PostMapping("/activityCombinationBase/editCombinationBaseStatusByJob")
    void editCombinationBaseStatusByJob();

}
