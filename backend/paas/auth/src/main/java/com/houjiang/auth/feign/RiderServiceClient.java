package com.houjiang.auth.feign;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.user.UserInfoVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @ClassName RiderServiceClient
 * @Description 骑手信息
 * @Author Amos
 * @Date 2024/4/30 10:47
 * @Version 1.0
 * @author Amos
 */
@FeignClient("mall-rider")
public interface RiderServiceClient {

    /**
     * 查询骑手信息-fegin
     * @param user
     * @return
     */
    @PostMapping("/rider/base/queryRiderInfo")
    ResponseResult queryRiderInfo(@RequestBody UserInfoVO user);
}
