package com.houjiang.auth.feign;

import com.houjiang.commons.dto.webPos.ReceptionLoginDTO;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * webpos client
 * @author Amos
 */
@FeignClient("webpos")
public interface WebPosServiceClient {

    /**
     * webPos员工用户登录
     */
    @PostMapping("/webPos/staff/login")
    ResponseResult webPosLogin(@RequestBody ReceptionLoginDTO form);

    /**
     * webPos员工退出登录
     */
    @PostMapping("/webPos/staff/logout")
    ResponseResult webPosLogout();
}
